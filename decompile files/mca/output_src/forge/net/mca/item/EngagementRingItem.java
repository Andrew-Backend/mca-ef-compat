package forge.net.mca.item;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Relationship;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item.Properties;

public class EngagementRingItem extends RelationshipItem {
   public EngagementRingItem(Properties properties) {
      super(properties);
   }

   @Override
   protected int getHeartsRequired() {
      return Config.getInstance().engagementHeartsRequirement;
   }

   @Override
   public boolean handle(ServerPlayer player, VillagerEntityMCA villager) {
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
