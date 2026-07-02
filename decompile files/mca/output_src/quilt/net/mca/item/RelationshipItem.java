package quilt.net.mca.item;

import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Memories;
import quilt.net.mca.entity.ai.Relationship;
import quilt.net.mca.server.world.data.PlayerSaveData;

public abstract class RelationshipItem extends TooltippedItem implements SpecialCaseGift {
   public RelationshipItem(class_1793 properties) {
      super(properties);
   }

   abstract int getHeartsRequired();

   @Override
   public boolean handle(class_3222 player, VillagerEntityMCA villager) {
      PlayerSaveData playerData = PlayerSaveData.get(player);
      Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);
      String response;
      if (villager.method_6109()) {
         response = "interaction.relationship.fail.isbaby";
      } else if (Relationship.IS_PARENT.test(villager, player)) {
         response = "interaction.relationship.fail.isparent";
      } else if (Relationship.IS_MARRIED.test(villager, player)) {
         response = "interaction.relationship.fail.marriedtogiver";
      } else if (villager.getRelationships().isMarried()) {
         response = "interaction.relationship.fail.married";
      } else if (villager.getRelationships().isEngaged() && !Relationship.IS_ENGAGED.test(villager, player)) {
         response = "interaction.relationship.fail.engaged";
      } else if (playerData.isMarried()) {
         response = "interaction.relationship.fail.playermarried";
      } else if (memory.getHearts() < this.getHeartsRequired()) {
         response = "interaction.relationship.fail.lowhearts";
      } else {
         if (villager.canBeAttractedTo(playerData)) {
            return false;
         }

         response = "interaction.relationship.fail.incompatible";
      }

      villager.sendChatMessage(player, response);
      return true;
   }
}
