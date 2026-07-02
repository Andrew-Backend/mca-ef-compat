package quilt.net.mca.network.c2s;

import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.server.world.data.VillageManager;

public class RenameVillageMessage implements Message {
   private static final long serialVersionUID = -7194992618247743620L;
   private final int id;
   private final String name;

   public RenameVillageMessage(int id, String name) {
      this.id = id;
      this.name = name;
   }

   @Override
   public void receive(class_3222 player) {
      VillageManager.get(player.method_51469()).getOrEmpty(this.id).ifPresent(v -> v.setName(this.name));
   }
}
