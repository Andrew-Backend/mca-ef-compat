package forge.net.mca.entity.interaction.gifts;

import forge.net.mca.Config;
import forge.net.mca.util.NbtHelper;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GiftSaturation {
   private List<ResourceLocation> values = new LinkedList<>();

   public void add(ItemStack stack) {
      if (!stack.m_41619_()) {
         ResourceLocation id = BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_());
         this.values.add(id);

         while (this.values.size() > Config.getInstance().giftDesaturationQueueLength) {
            this.pop();
         }
      }
   }

   public int get(ItemStack stack) {
      ResourceLocation id = BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_());
      return (int)this.values.stream().filter(v -> v.equals(id)).count();
   }

   public void readFromNbt(ListTag nbt) {
      this.values = NbtHelper.toList(nbt, v -> new ResourceLocation(v.m_7916_()));
   }

   public ListTag toNbt() {
      return NbtHelper.fromList(this.values, v -> StringTag.m_129297_(v.toString()));
   }

   public void pop() {
      if (!this.values.isEmpty()) {
         this.values.remove(0);
      }
   }
}
