package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.server.world.data.VillageManager;
import net.minecraft.class_3222;

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
   public void receive(class_3222 player) {
      VillageManager.get(player.method_51469()).getOrEmpty(this.id).ifPresent(village -> {
         village.setTaxes(this.taxes);
         village.setPopulationThreshold(this.populationThreshold);
         village.setMarriageThreshold(this.marriageThreshold);
      });
   }
}
