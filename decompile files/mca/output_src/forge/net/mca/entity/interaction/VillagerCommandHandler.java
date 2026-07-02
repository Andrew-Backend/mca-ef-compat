package forge.net.mca.entity.interaction;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.MoveState;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.mixin.MixinVillagerEntityInvoker;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import forge.net.mca.util.WorldUtils;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VillagerCommandHandler extends EntityCommandHandler<VillagerEntityMCA> {
   public VillagerCommandHandler(VillagerEntityMCA entity) {
      super(entity);
   }

   @Override
   public boolean handle(ServerPlayer player, String command) {
      Memories memory = this.entity.getVillagerBrain().getMemoriesForPlayer(player);
      if (MoveState.byCommand(command).filter(state -> {
         this.entity.getVillagerBrain().setMoveState(state, player);
         return true;
      }).isPresent()) {
         return true;
      }

      if (Chore.byCommand(command).filter(chore -> {
         this.entity.getVillagerBrain().assignJob(chore, player);
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(player, "chores");
         this.entity.sendChatMessage(Component.m_237115_("chore.success"), player);
         return true;
      }).isPresent()) {
         return true;
      }

      String arg = "";
      String[] split = command.split("\\.");
      if (split.length > 1) {
         command = split[0];
         arg = split[1];
      }

      switch (command) {
         case "pick_up":
            if (player.m_20197_().size() >= 3) {
               ((Entity)player.m_20197_().get(0)).m_8127_();
            }

            if (this.entity.m_20159_()) {
               this.entity.m_8127_();
            } else {
               this.entity.m_7998_(player, true);
            }

            player.f_8906_.m_9829_(new ClientboundSetPassengersPacket(player));
            return false;
         case "ridehorse":
            if (this.entity.m_20159_()) {
               this.entity.m_8127_();
            } else {
               this.entity
                  .m_9236_()
                  .m_6249_(player, player.m_20191_().m_82400_(10.0), e -> e instanceof Saddleable && ((Saddleable)e).m_6254_())
                  .stream()
                  .filter(horse -> !horse.m_20160_())
                  .min(Comparator.comparingDouble(a -> a.m_20280_(this.entity)))
                  .ifPresentOrElse(horse -> {
                     this.entity.m_7998_(horse, false);
                     this.entity.sendChatMessage(player, "interaction.ridehorse.success");
                  }, () -> this.entity.sendChatMessage(player, "interaction.ridehorse.fail.notnearby"));
            }

            return true;
         case "sethome":
            this.entity.getResidency().setHome(player);
            return true;
         case "gohome":
            this.entity.getResidency().goHome(player);
            this.stopInteracting();
            return false;
         case "setworkplace":
            this.entity.getResidency().setWorkplace(player);
            return true;
         case "trade":
            this.entity.getInteractions().stopInteracting();
            MixinVillagerEntityInvoker invoker = (MixinVillagerEntityInvoker)this.entity;
            invoker.invokeBeginTradeWith(player);
            return false;
         case "inventory":
            player.m_5893_(this.entity);
            return false;
         case "gift":
            this.entity.getRelationships().giveGift(player, memory);
            return true;
         case "adopt":
            this.entity.sendChatMessage(player, "interaction.adopt.success");
            FamilyTree familyTree = FamilyTree.get((ServerLevel)player.m_9236_());
            FamilyTreeNode parentNode = familyTree.getOrCreate(player);
            this.entity.getRelationships().getFamilyEntry().replaceParents(parentNode, familyTree.getOrEmpty(parentNode.partner()));
            break;
         case "procreate":
            if (memory.getHearts() < 100) {
               this.entity.sendChatMessage(player, "interaction.procreate.fail.lowhearts");
            } else if (this.entity.getRelationships().mayProcreateAgain(player.m_9236_().m_46467_())) {
               this.entity.getRelationships().startProcreating(player.m_9236_().m_46467_());
            } else {
               this.entity.sendChatMessage(player, "interaction.procreate.fail.toosoon");
            }

            return true;
         case "divorcePapers":
            player.m_150109_().m_36054_(new ItemStack((ItemLike)ItemsMCA.DIVORCE_PAPERS.get()));
            return true;
         case "divorceConfirm":
            ItemStack papers = ((Item)ItemsMCA.DIVORCE_PAPERS.get()).m_7968_();
            Memories memories = this.entity.getVillagerBrain().getMemoriesForPlayer(player);
            if (player.m_150109_().m_36063_(papers)) {
               this.entity.sendChatMessage(player, "divorcePaper");
               player.m_150109_().m_36057_(papers);
               memories.modHearts(-20);
            } else {
               this.entity.sendChatMessage(player, "divorce");
               memories.modHearts(-200);
            }

            this.entity.getVillagerBrain().modifyMoodValue(-5);
            this.entity.getRelationships().endRelationShip(RelationshipState.SINGLE);
            PlayerSaveData playerData = PlayerSaveData.get(player);
            playerData.endRelationShip(RelationshipState.SINGLE);
            return true;
         case "execute":
            this.entity.setProfession((VillagerProfession)ProfessionsMCA.OUTLAW.get());
            return true;
         case "pardon":
            this.entity.setProfession(VillagerProfession.f_35585_);
            return true;
         case "stay_in_village":
            this.entity.setProfession(VillagerProfession.f_35585_);
            this.entity.setDespawnDelay(0);
            return true;
         case "hire_short":
            this.payEmeralds(player, 5);
            this.entity.makeMercenary();
            this.entity.setDespawnDelay(72000);
            return true;
         case "hire_long":
            this.payEmeralds(player, 10);
            this.entity.makeMercenary();
            this.entity.setDespawnDelay(168000);
            return true;
         case "infected":
            this.entity.setInfected(!this.entity.isInfected());
            return true;
         case "stopworking":
            this.entity.getVillagerBrain().abandonJob();
            return true;
         case "armor":
            this.entity.getVillagerBrain().setArmorWear(!this.entity.getVillagerBrain().getArmorWear());
            if (this.entity.getVillagerBrain().getArmorWear()) {
               this.entity.sendChatMessage(player, "armor.enabled");
            } else {
               this.entity.sendChatMessage(player, "armor.disabled");
            }

            return true;
         case "profession":
            switch (arg) {
               case "none":
                  this.entity.setProfession(VillagerProfession.f_35585_);
                  this.entity.sendChatMessage(player, "profession.set.none");
                  break;
               case "guard":
                  this.entity.setProfession((VillagerProfession)ProfessionsMCA.GUARD.get());
                  this.entity.sendChatMessage(player, "profession.set.guard");
                  break;
               case "archer":
                  this.entity.setProfession((VillagerProfession)ProfessionsMCA.ARCHER.get());
                  this.entity.sendChatMessage(player, "profession.set.archer");
            }

            return true;
         case "apologize":
            Vec3 pos = this.entity.m_20182_();
            this.entity.m_9236_().m_45976_(VillagerEntityMCA.class, new AABB(pos, pos).m_82400_(32.0)).forEach(v -> {
               if (this.entity.m_20280_(v) <= (v.m_5448_() == null ? 1024 : 64)) {
                  v.pardonPlayers(99);
               }
            });
            break;
         case "location":
            if (Config.getInstance().structuresInRumors.size() > 0) {
               if (arg.length() == 0) {
                  arg = Config.getInstance().structuresInRumors.get(this.entity.m_217043_().m_188503_(Config.getInstance().structuresInRumors.size()));
               }

               ServerLevel world = (ServerLevel)this.entity.m_9236_();
               String finalArg = arg;
               MCA.executorService.execute(() -> {
                  ResourceLocation identifier = new ResourceLocation(finalArg);
                  BlockPos posx = RandomPos.m_217851_(this.entity.m_217043_(), 1024, 0).m_121955_(this.entity.m_20183_());
                  Optional<BlockPos> position = WorldUtils.getClosestStructurePosition(world, posx, identifier, 64);
                  if (position.isPresent()) {
                     String posString = position.get().m_123341_() + "," + position.get().m_123342_() + "," + position.get().m_123343_();
                     this.entity.sendChatMessage(player, "dialogue.location." + identifier.m_135815_(), posString);
                  } else {
                     this.entity.sendChatMessage(player, "dialogue.location.forgot");
                  }
               });
            } else {
               this.entity.sendChatMessage(player, "dialogue.location.forgot");
            }
            break;
         case "slap":
            player.m_6469_(player.m_9236_().m_269111_().m_269354_(), 1.0F);
      }

      return super.handle(player, command);
   }

   private void payEmeralds(ServerPlayer player, int emeralds) {
      Inventory inventory = player.m_150109_();

      for (int j = 0; j < inventory.m_6643_(); j++) {
         ItemStack itemStack = inventory.m_8020_(j);
         if (itemStack.m_41720_().equals(Items.f_42616_)) {
            int c = Math.min(itemStack.m_41613_(), emeralds);
            itemStack.m_41774_(c);
            emeralds -= c;
            if (emeralds <= 0) {
               return;
            }
         }
      }
   }
}
