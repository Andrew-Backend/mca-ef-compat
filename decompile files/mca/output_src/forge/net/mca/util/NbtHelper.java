package forge.net.mca.util;

import com.mojang.datafixers.util.Pair;
import forge.net.mca.MCA;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public interface NbtHelper {
   static <T extends Tag> T computeIfAbsent(CompoundTag nbt, String key, int type, Supplier<T> factory) {
      if (!nbt.m_128425_(key, type)) {
         nbt.m_128365_(key, factory.get());
      }

      return (T)nbt.m_128423_(key);
   }

   static CompoundTag copyTo(CompoundTag from, CompoundTag to) {
      from.m_128431_().forEach(key -> to.m_128365_(key, from.m_128423_(key)));
      return to;
   }

   static <V> List<V> toList(Tag nbt, Function<Tag, V> valueMapper) {
      return toStream(nbt, valueMapper).collect(Collectors.toList());
   }

   static <V> Stream<V> toStream(Tag nbt, Function<Tag, V> valueMapper) {
      return ((ListTag)nbt).stream().map(valueMapper);
   }

   static <K, V> Map<K, V> toMap(CompoundTag nbt, Function<String, K> keyMapper, Function<Tag, V> valueMapper) {
      return toMap(nbt, keyMapper, (k, e) -> valueMapper.apply(e));
   }

   static <K, V> Map<K, V> toMap(CompoundTag nbt, Function<String, K> keyMapper, BiFunction<K, Tag, V> valueMapper) {
      return nbt.m_128431_().stream().map(e -> {
         K k = keyMapper.apply(e);
         if (k == null) {
            return null;
         } else {
            V v = valueMapper.apply(k, nbt.m_128423_(e));
            if (v == null) {
               return null;
            } else {
               return k == null ? null : new Pair(k, v);
            }
         }
      }).filter(Objects::nonNull).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   static <V> ListTag fromList(Iterable<V> list, Function<V, Tag> valueMapper) {
      ListTag output = new ListTag();
      list.forEach(item -> output.add(valueMapper.apply((V)item)));
      return output;
   }

   static <K, V> CompoundTag fromMap(CompoundTag output, Map<K, V> map, Function<K, String> keyMapper, Function<V, Tag> valueMapper) {
      map.forEach((key, value) -> output.m_128365_(keyMapper.apply((K)key), valueMapper.apply((V)value)));
      return output;
   }

   static Tag encodeGlobalPosition(GlobalPos v) {
      return (Tag)GlobalPos.f_122633_.encodeStart(NbtOps.f_128958_, v).resultOrPartial(MCA.LOGGER::error).orElseThrow();
   }

   static GlobalPos decodeGlobalPos(Tag element) {
      return (GlobalPos)GlobalPos.f_122633_.parse(NbtOps.f_128958_, element).resultOrPartial(MCA.LOGGER::error).orElse(null);
   }
}
