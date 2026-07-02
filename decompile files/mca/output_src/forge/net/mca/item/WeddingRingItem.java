package forge.net.mca.item;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item.Properties;

public class WeddingRingItem extends RelationshipItem {
   public WeddingRingItem(Properties properties) {
      super(properties);
   }

   @Override
   protected int getHeartsRequired() {
      return Config.getInstance().marriageHeartsRequirement;
   }

   @Override
   public boolean handle(ServerPlayer player, VillagerEntityMCA villager) {
      PlayerSaveData playerData = PlayerSaveData.get(player);
      if (super.handle(player, villager)) {
         return false;
      }

      String response = "interaction.marry.success";
      playerData.marry(villager);
      villager.getRelationships().marry(player);
      villager.getVillagerBrain().modifyMoodValue(15);
      villager.sendChatMessage(player, response);
      return true;
   }
}
