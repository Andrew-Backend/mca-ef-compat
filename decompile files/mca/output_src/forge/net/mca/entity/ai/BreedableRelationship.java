package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.interaction.gifts.GiftType;
import forge.net.mca.entity.interaction.gifts.Response;
import forge.net.mca.item.SpecialCaseGift;
import forge.net.mca.network.s2c.AnalysisResults;
import forge.net.mca.resources.data.analysis.IntAnalysis;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;

public class BreedableRelationship extends Relationship<VillagerEntityMCA> {
   private static final CDataParameter<Boolean> IS_PROCREATING = CParameter.create("isProcreating", false);
   private static final CDataParameter<Integer> LAST_PROCREATION = CParameter.create("lastProcreation", 0);
   private int procreateTick = -1;
   private final Pregnancy pregnancy;

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return Relationship.createTrackedData(builder).addAll(IS_PROCREATING, LAST_PROCREATION).add(Pregnancy::createTrackedData);
   }

   public BreedableRelationship(VillagerEntityMCA entity) {
      super(entity);
      this.pregnancy = new Pregnancy(entity);
   }

   public Pregnancy getPregnancy() {
      return this.pregnancy;
   }

   public boolean isProcreating() {
      return this.entity.getTrackedValue(IS_PROCREATING);
   }

   public boolean mayProcreateAgain(long time) {
      int intTime = (int)time;
      Integer trackedValue = this.entity.getTrackedValue(LAST_PROCREATION);
      int delta = intTime - trackedValue;
      return trackedValue == 0 || delta < 0 || delta > Config.getInstance().procreationCooldown;
   }

   public void startProcreating(long time) {
      this.procreateTick = 60;
      this.entity.setTrackedValue(IS_PROCREATING, true);
      this.entity.setTrackedValue(LAST_PROCREATION, (int)time);
   }

   public void tick(int age) {
      if (age % 20 == 0) {
         this.pregnancy.tick();
      }

      if (this.isProcreating()) {
         if (this.procreateTick > 0) {
            this.procreateTick--;
            this.entity.m_21573_().m_26573_();
            this.entity.m_9236_().m_7605_(this.entity, (byte)12);
         } else {
            this.getFamilyTree().getOrCreate(this.entity);
            this.getPartner().ifPresent(spouse -> {
               this.pregnancy.procreate(spouse);
               this.entity.setTrackedValue(IS_PROCREATING, false);
            });
         }
      }
   }

   public void giveGift(ServerPlayer player, Memories memory) {
      ItemStack stack = player.m_21205_();
      if (!stack.m_41619_() && !this.handleSpecialCaseGift(player, stack)) {
         Optional<GiftType> gift = GiftType.bestMatching(this.entity, stack, player);
         if (gift.isPresent()) {
            this.acceptGift(stack, gift.get(), player, memory);
         } else {
            gift = this.handleDynamicGift(stack);
            if (gift.isPresent()) {
               this.acceptGift(stack, gift.get(), player, memory);
            } else {
               this.rejectGift(player, "gift.fail");
            }
         }
      }
   }

   private Optional<GiftType> handleDynamicGift(ItemStack stack) {
      if (stack.m_41720_() instanceof SwordItem sword) {
         float satisfaction = sword.m_43299_();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(new GiftType(stack.m_41720_(), (int)satisfaction, MCA.locate("swords")));
      } else if (stack.m_41720_() instanceof ProjectileWeaponItem ranged) {
         float satisfaction = ranged.m_6615_();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(new GiftType(stack.m_41720_(), (int)satisfaction, MCA.locate("archery")));
      } else if (stack.m_41720_() instanceof TieredItem tool) {
         float satisfaction = tool.m_43314_().m_6624_();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(
            new GiftType(
               stack.m_41720_(),
               (int)satisfaction,
               MCA.locate(
                  stack.m_41720_() instanceof AxeItem
                     ? "swords"
                     : (stack.m_41720_() instanceof HoeItem ? "hoes" : (stack.m_41720_() instanceof ShovelItem ? "shovels" : "pickaxes"))
               )
            )
         );
      } else if (stack.m_41720_() instanceof ArmorItem armor) {
         int satisfaction = (int)(Math.pow(armor.m_40404_(), 1.25) * 1.5 + armor.m_40401_().m_6651_() * 5.0F);
         return Optional.of(new GiftType(stack.m_41720_(), satisfaction, MCA.locate("armor")));
      } else {
         if (stack.m_41720_().m_41472_()) {
            FoodProperties component = stack.m_41720_().m_41473_();
            if (component != null) {
               int satisfaction = (int)(component.m_38744_() + component.m_38745_() * 3.0F);
               return Optional.of(new GiftType(stack.m_41720_(), satisfaction, MCA.locate("food")));
            }
         }

         return Optional.empty();
      }
   }

   private void acceptGift(ItemStack stack, GiftType gift, ServerPlayer player, Memories memory) {
      if (!this.entity.m_35311_().m_19183_(stack)) {
         this.rejectGift(player, "villager.inventory.full");
      } else {
         IntAnalysis analysis = gift.getSatisfactionFor(this.entity, stack, player);
         int satisfaction = analysis.getTotal();
         Response response = gift.getResponse(satisfaction);
         int occurrences = this.getGiftSaturation().get(stack);
         int penalty = (int)(
            occurrences * Config.getInstance().giftDesaturationFactor * Math.pow(Math.max(satisfaction, 0.0), Config.getInstance().giftDesaturationExponent)
         );
         if (penalty != 0) {
            analysis.add("desaturation", -penalty);
         }

         int desaturatedSatisfaction = analysis.getTotal();
         Response desaturatedResponse = gift.getResponse(desaturatedSatisfaction);
         desaturatedSatisfaction = (int)(desaturatedSatisfaction * Config.getInstance().giftSatisfactionFactor);
         NetworkHandler.sendToPlayer(new AnalysisResults(analysis), player);
         if (response == Response.FAIL) {
            this.rejectGift(player, gift.getDialogueFor(response));
         } else if (desaturatedResponse == Response.FAIL) {
            this.rejectGift(player, "gift.saturated");
         } else {
            this.entity.sendChatMessage(player, gift.getDialogueFor(response));
            if (response == Response.BEST) {
               this.entity.playSurprisedSound();
            }

            this.getGiftSaturation().add(stack);
            this.entity.m_9236_().m_7605_(this.entity, (byte)16);
            this.entity.m_35311_().m_19173_(stack.m_41620_(1));
         }

         this.entity
            .getVillagerBrain()
            .modifyMoodValue(
               (int)(
                  desaturatedSatisfaction * Config.getInstance().giftMoodEffect
                     + Config.getInstance().baseGiftMoodEffect * Mth.m_14205_(desaturatedSatisfaction)
               )
            );
         CriterionMCA.HEARTS_CRITERION.trigger(player, memory.getHearts(), desaturatedSatisfaction, "gift");
         memory.modHearts(desaturatedSatisfaction);
      }
   }

   private void rejectGift(Player player, String dialogue) {
      this.entity.m_9236_().m_7605_(this.entity, (byte)15);
      this.entity.sendChatMessage(player, dialogue);
   }

   private boolean handleSpecialCaseGift(ServerPlayer player, ItemStack stack) {
      Item item = stack.m_41720_();
      if (item instanceof SpecialCaseGift) {
         if (((SpecialCaseGift)item).handle(player, this.entity)) {
            stack.m_41774_(1);
         }

         return true;
      } else if (item == Items.f_42502_ && !this.entity.m_6162_()) {
         if (this.pregnancy.tryStartGestation()) {
            player.m_9236_().m_7605_(this.entity, (byte)12);
            stack.m_41774_(1);
            this.entity.sendChatMessage(player, "gift.cake.success");
         } else {
            this.entity.sendChatMessage(player, "gift.cake.fail");
         }

         return true;
      } else if (item == Items.f_42436_ && this.entity.isInfected()) {
         this.entity.setInfected(false);
         this.entity.m_5584_(this.entity.m_9236_(), stack);
         return true;
      } else if (item instanceof DyeItem dye) {
         this.entity.setHairDye(dye.m_41089_());
         stack.m_41774_(1);
         return true;
      } else {
         if (item == Items.f_41903_) {
            this.entity.clearHairDye();
            stack.m_41774_(1);
            return true;
         }

         if (item == Items.f_42656_) {
            if (stack.m_41788_()) {
               this.entity.setCustomSkin(stack.m_41786_().getString());
            } else {
               this.entity.setCustomSkin("");
            }

            stack.m_41774_(1);
            return true;
         } else if (item == Items.f_42436_ && this.entity.m_6162_()) {
            this.entity.m_146758_(24000);
            stack.m_41774_(1);
            return true;
         } else {
            return false;
         }
      }
   }
}
