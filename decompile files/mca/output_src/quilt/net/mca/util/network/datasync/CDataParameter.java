package quilt.net.mca.util.network.datasync;

import java.util.function.BiFunction;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2941;
import net.minecraft.class_2945;

public class CDataParameter<T> implements CParameter<T, T> {
   private final String id;
   private final T defaultValue;
   private final class_2941<T> valueType;
   private final BiFunction<class_2487, String, T> load;
   private final CDataParameter.TriConsumer<class_2487, String, ? super T> save;

   protected CDataParameter(
      String id,
      class_2941<T> valueType,
      T defaultValue,
      BiFunction<class_2487, String, T> load,
      CDataParameter.TriConsumer<class_2487, String, ? super T> save
   ) {
      this.id = id;
      this.defaultValue = defaultValue;
      this.valueType = valueType;
      this.load = load;
      this.save = save;
   }

   @Override
   public T getDefault() {
      return this.defaultValue;
   }

   @Override
   public T get(class_2940<T> param, class_2945 tracker) {
      return (T)tracker.method_12789(param);
   }

   @Override
   public void set(class_2940<T> param, class_2945 tracker, T v) {
      tracker.method_12778(param, v);
   }

   @Override
   public T load(class_2487 nbt) {
      return this.load.apply(nbt, this.id);
   }

   @Override
   public void save(class_2487 nbt, T value) {
      this.save.accept(nbt, this.id, value);
   }

   @Override
   public class_2940<T> createParam(Class<? extends class_1297> type) {
      return class_2945.method_12791(type, this.valueType);
   }

   public interface TriConsumer<A, B, C> {
      void accept(A var1, B var2, C var3);
   }
}
