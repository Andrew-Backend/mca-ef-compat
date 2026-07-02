package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.server.world.data.VillageManager;
import net.minecraft.server.level.ServerPlayer;

public class RenameVillageMessage implements Message {
   private static final long serialVersionUID = -7194992618247743620L;
   private final int id;
   private final String name;

   public RenameVillageMessage(int id, String name) {
      this.id = id;
      this.name = name;
   }

   @Override
   public void receive(ServerPlayer player) {
      VillageManager.get(player.m_284548_()).getOrEmpty(this.id).ifPresent(v -> v.setName(this.name));
   }
}
