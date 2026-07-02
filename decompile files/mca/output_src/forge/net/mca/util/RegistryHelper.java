package forge.net.mca.util;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

public class RegistryHelper {
   public static <T> Optional<TagKey<T>> tryGetTagKey(Registry<T> registry, ResourceLocation id) {
      return registry.m_203613_().filter(tagKey -> tagKey.f_203868_().equals(id)).findFirst();
   }

   public static <T> Optional<? extends HolderSet<T>> getEntries(TagKey<T> tagKey) {
      return getRegistryOf(tagKey).m_203431_(tagKey);
   }

   public static <T> Optional<Holder<T>> tryGetEntry(Registry<T> registry, T object) {
      return registry.m_7854_(object).map(registry::m_246971_);
   }

   public static <T> boolean isObjectInTag(Registry<T> registry, ResourceLocation tagId, T object) {
      return tryGetTagKey(registry, tagId).map(tagKey -> isObjectInTag(registry, (TagKey<T>)tagKey, object)).orElse(false);
   }

   public static <T> boolean isObjectInTag(Registry<T> registry, TagKey<T> tag, T object) {
      Optional<Holder<T>> entry = tryGetEntry(registry, object);
      return entry.<Boolean>map(tRegistryEntry -> tRegistryEntry.m_203656_(tag)).orElse(false);
   }

   public static <T> boolean isTagEmpty(TagKey<T> tag) {
      return getEntries(tag).<Integer>map(HolderSet::m_203632_).orElse(0) == 0;
   }

   public static <T> Registry<T> getRegistryOf(@NotNull TagKey<T> key) {
      return (Registry<T>)BuiltInRegistries.f_257047_.m_7745_(key.f_203867_().m_135782_());
   }
}
