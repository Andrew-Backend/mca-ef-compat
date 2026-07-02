package fabric.net.mca.util;

import com.mojang.datafixers.util.Pair;
import fabric.net.mca.MCA;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_4208;

public interface NbtHelper {
   static <T extends class_2520> T computeIfAbsent(class_2487 nbt, String key, int type, Supplier<T> factory) {
      if (!nbt.method_10573(key, type)) {
         nbt.method_10566(key, factory.get());
      }

      return (T)nbt.method_10580(key);
   }

   static class_2487 copyTo(class_2487 from, class_2487 to) {
      from.method_10541().forEach(key -> to.method_10566(key, from.method_10580(key)));
      return to;
   }

   static <V> List<V> toList(class_2520 nbt, Function<class_2520, V> valueMapper) {
      return toStream(nbt, valueMapper).collect(Collectors.toList());
   }

   static <V> Stream<V> toStream(class_2520 nbt, Function<class_2520, V> valueMapper) {
      return ((class_2499)nbt).stream().map(valueMapper);
   }

   static <K, V> Map<K, V> toMap(class_2487 nbt, Function<String, K> keyMapper, Function<class_2520, V> valueMapper) {
      return toMap(nbt, keyMapper, (k, e) -> valueMapper.apply(e));
   }

   static <K, V> Map<K, V> toMap(class_2487 nbt, Function<String, K> keyMapper, BiFunction<K, class_2520, V> valueMapper) {
      return nbt.method_10541().stream().map(e -> {
         K k = keyMapper.apply(e);
         if (k == null) {
            return null;
         } else {
            V v = valueMapper.apply(k, nbt.method_10580(e));
            if (v == null) {
               return null;
            } else {
               return k == null ? null : new Pair(k, v);
            }
         }
      }).filter(Objects::nonNull).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   static <V> class_2499 fromList(Iterable<V> list, Function<V, class_2520> valueMapper) {
      class_2499 output = new class_2499();
      list.forEach(item -> output.add(valueMapper.apply((V)item)));
      return output;
   }

   static <K, V> class_2487 fromMap(class_2487 output, Map<K, V> map, Function<K, String> keyMapper, Function<V, class_2520> valueMapper) {
      map.forEach((key, value) -> output.method_10566(keyMapper.apply((K)key), valueMapper.apply((V)value)));
      return output;
   }

   static class_2520 encodeGlobalPosition(class_4208 v) {
      return (class_2520)class_4208.field_25066.encodeStart(class_2509.field_11560, v).resultOrPartial(MCA.LOGGER::error).orElseThrow();
   }

   static class_4208 decodeGlobalPos(class_2520 element) {
      return (class_4208)class_4208.field_25066.parse(class_2509.field_11560, element).resultOrPartial(MCA.LOGGER::error).orElse(null);
   }
}
