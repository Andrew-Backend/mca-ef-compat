package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import net.minecraft.server.level.ServerPlayer;

public class SaveVillageMessage implements Message {
   private static final long serialVersionUID = -4830365225086158551L;
   private final int id;
   private final float taxes;
   private final float populationThreshold;
   private final float marriageThreshold;

   public SaveVillageMessage(Village village) {
      this.id = village.getId();
      this.taxes = village.getTaxes();
      this.populationThreshold = village.getPopulationThreshold();
      this.marriageThreshold = village.getMarriageThreshold();
   }

   @Override
   public void receive(ServerPlayer player) {
      VillageManager.get(player.m_284548_()).getOrEmpty(this.id).ifPresent(village -> {
         village.setTaxes(this.taxes);
         village.setPopulationThreshold(this.populationThreshold);
         village.setMarriageThreshold(this.marriageThreshold);
      });
   }
}
