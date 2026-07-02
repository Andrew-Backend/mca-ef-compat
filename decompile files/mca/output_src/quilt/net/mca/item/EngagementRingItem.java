package quilt.net.mca.item;

import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Relationship;
import quilt.net.mca.server.world.data.PlayerSaveData;

public class EngagementRingItem extends RelationshipItem {
   public EngagementRingItem(class_1793 properties) {
      super(properties);
   }

   @Override
   protected int getHeartsRequired() {
      return Config.getInstance().engagementHeartsRequirement;
   }

   @Override
   public boolean handle(class_3222 player, VillagerEntityMCA villager) {
      PlayerSaveData playerData = PlayerSaveData.get(player);
      boolean consume = false;
      if (super.handle(player, villager)) {
         return false;
      }

      String response;
      if (Relationship.IS_ENGAGED.test(villager, player)) {
         response = "interaction.engage.fail.engaged";
      } else {
         response = "interaction.engage.success";
         playerData.engage(villager);
         villager.getRelationships().engage(player);
         villager.getVillagerBrain().modifyMoodValue(10);
         consume = true;
      }

      villager.sendChatMessage(player, response);
      return consume;
   }
}
