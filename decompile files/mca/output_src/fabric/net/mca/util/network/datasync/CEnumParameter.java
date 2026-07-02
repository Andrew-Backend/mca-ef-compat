package fabric.net.mca.util.network.datasync;

import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import org.jetbrains.annotations.Nullable;

public class CEnumParameter<T extends Enum<T>> implements CParameter<T, Integer> {
   private final String id;
   @Nullable
   private final T defaultValue;
   private final T[] values;

   public CEnumParameter(String id, Class<T> type, @Nullable T dv) {
      this.id = id;
      this.defaultValue = dv;
      this.values = type.getEnumConstants();
   }

   public Integer getDefault() {
      return this.defaultValue == null ? -1 : this.defaultValue.ordinal();
   }

   public T get(class_2940<Integer> param, class_2945 tracker) {
      return this.fromIndex((Integer)tracker.method_12789(param));
   }

   public void set(class_2940<Integer> param, class_2945 tracker, @Nullable T v) {
      tracker.method_12778(param, v == null ? -1 : v.ordinal());
   }

   public T load(class_2487 nbt) {
      return nbt.method_10573(this.id, 99) ? this.fromIndex(nbt.method_10550(this.id)) : this.defaultValue;
   }

   public void save(class_2487 nbt, T value) {
      if (value != null) {
         nbt.method_10569(this.id, value.ordinal());
      }
   }

   private T fromIndex(int index) {
      return index >= 0 && index < this.values.length ? this.values[index] : this.defaultValue;
   }

   @Override
   public class_2940<Integer> createParam(Class<? extends class_1297> type) {
      return class_2945.method_12791(type, class_2943.field_13327);
   }
}
