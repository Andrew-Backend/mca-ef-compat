package fabric.net.mca.entity.ai;

import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.interaction.gifts.GiftType;
import fabric.net.mca.entity.interaction.gifts.Response;
import fabric.net.mca.item.SpecialCaseGift;
import fabric.net.mca.network.s2c.AnalysisResults;
import fabric.net.mca.resources.data.analysis.IntAnalysis;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CDataParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1769;
import net.minecraft.class_1792;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1811;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_1831;
import net.minecraft.class_3222;
import net.minecraft.class_3532;
import net.minecraft.class_4174;

public class BreedableRelationship extends Relationship<VillagerEntityMCA> {
   private static final CDataParameter<Boolean> IS_PROCREATING = CParameter.create("isProcreating", false);
   private static final CDataParameter<Integer> LAST_PROCREATION = CParameter.create("lastProcreation", 0);
   private int procreateTick = -1;
   private final Pregnancy pregnancy;

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
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
            this.entity.method_5942().method_6340();
            this.entity.method_37908().method_8421(this.entity, (byte)12);
         } else {
            this.getFamilyTree().getOrCreate(this.entity);
            this.getPartner().ifPresent(spouse -> {
               this.pregnancy.procreate(spouse);
               this.entity.setTrackedValue(IS_PROCREATING, false);
            });
         }
      }
   }

   public void giveGift(class_3222 player, Memories memory) {
      class_1799 stack = player.method_6047();
      if (!stack.method_7960() && !this.handleSpecialCaseGift(player, stack)) {
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

   private Optional<GiftType> handleDynamicGift(class_1799 stack) {
      if (stack.method_7909() instanceof class_1829 sword) {
         float satisfaction = sword.method_8020();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(new GiftType(stack.method_7909(), (int)satisfaction, MCA.locate("swords")));
      } else if (stack.method_7909() instanceof class_1811 ranged) {
         float satisfaction = ranged.method_24792();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(new GiftType(stack.method_7909(), (int)satisfaction, MCA.locate("archery")));
      } else if (stack.method_7909() instanceof class_1831 tool) {
         float satisfaction = tool.method_8022().method_8027();
         satisfaction = (float)(Math.pow(satisfaction, 1.25) * 2.0);
         return Optional.of(
            new GiftType(
               stack.method_7909(),
               (int)satisfaction,
               MCA.locate(
                  stack.method_7909() instanceof class_1743
                     ? "swords"
                     : (stack.method_7909() instanceof class_1794 ? "hoes" : (stack.method_7909() instanceof class_1821 ? "shovels" : "pickaxes"))
               )
            )
         );
      } else if (stack.method_7909() instanceof class_1738 armor) {
         int satisfaction = (int)(Math.pow(armor.method_7687(), 1.25) * 1.5 + armor.method_7686().method_7700() * 5.0F);
         return Optional.of(new GiftType(stack.method_7909(), satisfaction, MCA.locate("armor")));
      } else {
         if (stack.method_7909().method_19263()) {
            class_4174 component = stack.method_7909().method_19264();
            if (component != null) {
               int satisfaction = (int)(component.method_19230() + component.method_19231() * 3.0F);
               return Optional.of(new GiftType(stack.method_7909(), satisfaction, MCA.locate("food")));
            }
         }

         return Optional.empty();
      }
   }

   private void acceptGift(class_1799 stack, GiftType gift, class_3222 player, Memories memory) {
      if (!this.entity.method_35199().method_27070(stack)) {
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
            this.entity.method_37908().method_8421(this.entity, (byte)16);
            this.entity.method_35199().method_5491(stack.method_7971(1));
         }

         this.entity
            .getVillagerBrain()
            .modifyMoodValue(
               (int)(
                  desaturatedSatisfaction * Config.getInstance().giftMoodEffect
                     + Config.getInstance().baseGiftMoodEffect * class_3532.method_17822(desaturatedSatisfaction)
               )
            );
         CriterionMCA.HEARTS_CRITERION.trigger(player, memory.getHearts(), desaturatedSatisfaction, "gift");
         memory.modHearts(desaturatedSatisfaction);
      }
   }

   private void rejectGift(class_1657 player, String dialogue) {
      this.entity.method_37908().method_8421(this.entity, (byte)15);
      this.entity.sendChatMessage(player, dialogue);
   }

   private boolean handleSpecialCaseGift(class_3222 player, class_1799 stack) {
      class_1792 item = stack.method_7909();
      if (item instanceof SpecialCaseGift) {
         if (((SpecialCaseGift)item).handle(player, this.entity)) {
            stack.method_7934(1);
         }

         return true;
      } else if (item == class_1802.field_17534 && !this.entity.method_6109()) {
         if (this.pregnancy.tryStartGestation()) {
            player.method_37908().method_8421(this.entity, (byte)12);
            stack.method_7934(1);
            this.entity.sendChatMessage(player, "gift.cake.success");
         } else {
            this.entity.sendChatMessage(player, "gift.cake.fail");
         }

         return true;
      } else if (item == class_1802.field_8463 && this.entity.isInfected()) {
         this.entity.setInfected(false);
         this.entity.method_18866(this.entity.method_37908(), stack);
         return true;
      } else if (item instanceof class_1769 dye) {
         this.entity.setHairDye(dye.method_7802());
         stack.method_7934(1);
         return true;
      } else {
         if (item == class_1802.field_8554) {
            this.entity.clearHairDye();
            stack.method_7934(1);
            return true;
         }

         if (item == class_1802.field_8448) {
            if (stack.method_7938()) {
               this.entity.setCustomSkin(stack.method_7964().getString());
            } else {
               this.entity.setCustomSkin("");
            }

            stack.method_7934(1);
            return true;
         } else if (item == class_1802.field_8463 && this.entity.method_6109()) {
            this.entity.method_5615(24000);
            stack.method_7934(1);
            return true;
         } else {
            return false;
         }
      }
   }
}
