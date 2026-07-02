package forge.net.mca.util.network.datasync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NbtCompoundDefaultGetters {
   public static int getInt(CompoundTag nbt, String key, int def) {
      try {
         if (nbt.m_128425_(key, 99)) {
            return nbt.m_128451_(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static float getFloat(CompoundTag nbt, String key, float def) {
      try {
         if (nbt.m_128425_(key, 99)) {
            return nbt.m_128457_(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static String getString(CompoundTag nbt, String key, String def) {
      try {
         if (nbt.m_128425_(key, 8)) {
            return nbt.m_128461_(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static CompoundTag getCompound(CompoundTag nbt, String key, CompoundTag def) {
      try {
         if (nbt.m_128425_(key, 10)) {
            return nbt.m_128469_(key);
         }
      } catch (ClassCastException var4) {
      }

      return def.m_6426_();
   }

   public static ItemStack getItemStack(CompoundTag nbt, String key, ItemStack def) {
      try {
         if (nbt.m_128425_(key, 10)) {
            return ItemStack.m_41712_(nbt.m_128469_(key));
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }
}
