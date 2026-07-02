package fabric.net.mca.item;

import fabric.net.mca.Config;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;

public class WeddingRingItem extends RelationshipItem {
   public WeddingRingItem(class_1793 properties) {
      super(properties);
   }

   @Override
   protected int getHeartsRequired() {
      return Config.getInstance().marriageHeartsRequirement;
   }

   @Override
   public boolean handle(class_3222 player, VillagerEntityMCA villager) {
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
