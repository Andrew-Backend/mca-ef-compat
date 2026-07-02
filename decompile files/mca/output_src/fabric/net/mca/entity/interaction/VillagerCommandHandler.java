package fabric.net.mca.entity.interaction;

import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.Chore;
import fabric.net.mca.entity.ai.Memories;
import fabric.net.mca.entity.ai.MoveState;
import fabric.net.mca.entity.ai.relationship.RelationshipState;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.mixin.MixinVillagerEntityInvoker;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import fabric.net.mca.server.world.data.PlayerSaveData;
import fabric.net.mca.util.WorldUtils;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2752;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3852;
import net.minecraft.class_5146;
import net.minecraft.class_5535;

public class VillagerCommandHandler extends EntityCommandHandler<VillagerEntityMCA> {
   public VillagerCommandHandler(VillagerEntityMCA entity) {
      super(entity);
   }

   @Override
   public boolean handle(class_3222 player, String command) {
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
         this.entity.sendChatMessage(class_2561.method_43471("chore.success"), player);
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
            if (player.method_5685().size() >= 3) {
               ((class_1297)player.method_5685().get(0)).method_5848();
            }

            if (this.entity.method_5765()) {
               this.entity.method_5848();
            } else {
               this.entity.method_5873(player, true);
            }

            player.field_13987.method_14364(new class_2752(player));
            return false;
         case "ridehorse":
            if (this.entity.method_5765()) {
               this.entity.method_5848();
            } else {
               this.entity
                  .method_37908()
                  .method_8333(player, player.method_5829().method_1014(10.0), e -> e instanceof class_5146 && ((class_5146)e).method_6725())
                  .stream()
                  .filter(horse -> !horse.method_5782())
                  .min(Comparator.comparingDouble(a -> a.method_5858(this.entity)))
                  .ifPresentOrElse(horse -> {
                     this.entity.method_5873(horse, false);
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
            player.method_17355(this.entity);
            return false;
         case "gift":
            this.entity.getRelationships().giveGift(player, memory);
            return true;
         case "adopt":
            this.entity.sendChatMessage(player, "interaction.adopt.success");
            FamilyTree familyTree = FamilyTree.get((class_3218)player.method_37908());
            FamilyTreeNode parentNode = familyTree.getOrCreate(player);
            this.entity.getRelationships().getFamilyEntry().replaceParents(parentNode, familyTree.getOrEmpty(parentNode.partner()));
            break;
         case "procreate":
            if (memory.getHearts() < 100) {
               this.entity.sendChatMessage(player, "interaction.procreate.fail.lowhearts");
            } else if (this.entity.getRelationships().mayProcreateAgain(player.method_37908().method_8510())) {
               this.entity.getRelationships().startProcreating(player.method_37908().method_8510());
            } else {
               this.entity.sendChatMessage(player, "interaction.procreate.fail.toosoon");
            }

            return true;
         case "divorcePapers":
            player.method_31548().method_7394(new class_1799((class_1935)ItemsMCA.DIVORCE_PAPERS.get()));
            return true;
         case "divorceConfirm":
            class_1799 papers = ((class_1792)ItemsMCA.DIVORCE_PAPERS.get()).method_7854();
            Memories memories = this.entity.getVillagerBrain().getMemoriesForPlayer(player);
            if (player.method_31548().method_7379(papers)) {
               this.entity.sendChatMessage(player, "divorcePaper");
               player.method_31548().method_7378(papers);
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
            this.entity.setProfession((class_3852)ProfessionsMCA.OUTLAW.get());
            return true;
         case "pardon":
            this.entity.setProfession(class_3852.field_17051);
            return true;
         case "stay_in_village":
            this.entity.setProfession(class_3852.field_17051);
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
                  this.entity.setProfession(class_3852.field_17051);
                  this.entity.sendChatMessage(player, "profession.set.none");
                  break;
               case "guard":
                  this.entity.setProfession((class_3852)ProfessionsMCA.GUARD.get());
                  this.entity.sendChatMessage(player, "profession.set.guard");
                  break;
               case "archer":
                  this.entity.setProfession((class_3852)ProfessionsMCA.ARCHER.get());
                  this.entity.sendChatMessage(player, "profession.set.archer");
            }

            return true;
         case "apologize":
            class_243 pos = this.entity.method_19538();
            this.entity.method_37908().method_18467(VillagerEntityMCA.class, new class_238(pos, pos).method_1014(32.0)).forEach(v -> {
               if (this.entity.method_5858(v) <= (v.method_5968() == null ? 1024 : 64)) {
                  v.pardonPlayers(99);
               }
            });
            break;
         case "location":
            if (Config.getInstance().structuresInRumors.size() > 0) {
               if (arg.length() == 0) {
                  arg = Config.getInstance().structuresInRumors.get(this.entity.method_6051().method_43048(Config.getInstance().structuresInRumors.size()));
               }

               class_3218 world = (class_3218)this.entity.method_37908();
               String finalArg = arg;
               MCA.executorService.execute(() -> {
                  class_2960 identifier = new class_2960(finalArg);
                  class_2338 posx = class_5535.method_31541(this.entity.method_6051(), 1024, 0).method_10081(this.entity.method_24515());
                  Optional<class_2338> position = WorldUtils.getClosestStructurePosition(world, posx, identifier, 64);
                  if (position.isPresent()) {
                     String posString = position.get().method_10263() + "," + position.get().method_10264() + "," + position.get().method_10260();
                     this.entity.sendChatMessage(player, "dialogue.location." + identifier.method_12832(), posString);
                  } else {
                     this.entity.sendChatMessage(player, "dialogue.location.forgot");
                  }
               });
            } else {
               this.entity.sendChatMessage(player, "dialogue.location.forgot");
            }
            break;
         case "slap":
            player.method_5643(player.method_37908().method_48963().method_48823(), 1.0F);
      }

      return super.handle(player, command);
   }

   private void payEmeralds(class_3222 player, int emeralds) {
      class_1661 inventory = player.method_31548();

      for (int j = 0; j < inventory.method_5439(); j++) {
         class_1799 itemStack = inventory.method_5438(j);
         if (itemStack.method_7909().equals(class_1802.field_8687)) {
            int c = Math.min(itemStack.method_7947(), emeralds);
            itemStack.method_7934(c);
            emeralds -= c;
            if (emeralds <= 0) {
               return;
            }
         }
      }
   }
}
