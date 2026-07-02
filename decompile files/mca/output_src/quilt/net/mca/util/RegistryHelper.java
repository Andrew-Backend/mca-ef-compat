package quilt.net.mca.util;

import java.util.Optional;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_6885;
import net.minecraft.class_7923;
import org.jetbrains.annotations.NotNull;

public class RegistryHelper {
   public static <T> Optional<class_6862<T>> tryGetTagKey(class_2378<T> registry, class_2960 id) {
      return registry.method_40273().filter(tagKey -> tagKey.comp_327().equals(id)).findFirst();
   }

   public static <T> Optional<? extends class_6885<T>> getEntries(class_6862<T> tagKey) {
      return getRegistryOf(tagKey).method_40266(tagKey);
   }

   public static <T> Optional<class_6880<T>> tryGetEntry(class_2378<T> registry, T object) {
      return registry.method_29113(object).map(registry::method_40290);
   }

   public static <T> boolean isObjectInTag(class_2378<T> registry, class_2960 tagId, T object) {
      return tryGetTagKey(registry, tagId).map(tagKey -> isObjectInTag(registry, (class_6862<T>)tagKey, object)).orElse(false);
   }

   public static <T> boolean isObjectInTag(class_2378<T> registry, class_6862<T> tag, T object) {
      Optional<class_6880<T>> entry = tryGetEntry(registry, object);
      return entry.<Boolean>map(tRegistryEntry -> tRegistryEntry.method_40220(tag)).orElse(false);
   }

   public static <T> boolean isTagEmpty(class_6862<T> tag) {
      return getEntries(tag).<Integer>map(class_6885::method_40247).orElse(0) == 0;
   }

   public static <T> class_2378<T> getRegistryOf(@NotNull class_6862<T> key) {
      return (class_2378<T>)class_7923.field_41167.method_10223(key.comp_326().method_29177());
   }
}
