package fabric.net.mca.util.network.datasync;

import net.minecraft.class_1799;
import net.minecraft.class_2487;

public class NbtCompoundDefaultGetters {
   public static int getInt(class_2487 nbt, String key, int def) {
      try {
         if (nbt.method_10573(key, 99)) {
            return nbt.method_10550(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static float getFloat(class_2487 nbt, String key, float def) {
      try {
         if (nbt.method_10573(key, 99)) {
            return nbt.method_10583(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static String getString(class_2487 nbt, String key, String def) {
      try {
         if (nbt.method_10573(key, 8)) {
            return nbt.method_10558(key);
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }

   public static class_2487 getCompound(class_2487 nbt, String key, class_2487 def) {
      try {
         if (nbt.method_10573(key, 10)) {
            return nbt.method_10562(key);
         }
      } catch (ClassCastException var4) {
      }

      return def.method_10553();
   }

   public static class_1799 getItemStack(class_2487 nbt, String key, class_1799 def) {
      try {
         if (nbt.method_10573(key, 10)) {
            return class_1799.method_7915(nbt.method_10562(key));
         }
      } catch (ClassCastException var4) {
      }

      return def;
   }
}
