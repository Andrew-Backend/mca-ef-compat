package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.item.BabyItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class BabyNamingVillagerMessage implements Message {
   private static final long serialVersionUID = -7160822837267592011L;
   private final int slot;
   private final String name;

   public BabyNamingVillagerMessage(int slot, String name) {
      this.slot = slot;
      this.name = name;
   }

   @Override
   public void receive(ServerPlayer player) {
      ItemStack stack = player.m_150109_().m_8020_(this.slot);
      BabyItem.getBabyNbt(stack).m_128359_("babyName", this.name);
   }
}
