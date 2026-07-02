package forge.net.mca.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;

public class UpdatableInventory extends SimpleContainer {
   public UpdatableInventory(int size) {
      super(size);
   }

   public void update(Entity entity) {
      for (int slot = 0; slot < this.m_6643_(); slot++) {
         if (!this.m_8020_(slot).m_41619_()) {
            this.m_8020_(slot).m_41666_(entity.m_9236_(), entity, slot, false);
         }
      }
   }
}
