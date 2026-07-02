package yesman.epicfight.skill;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.IForgeRegistry.BakeCallback;
import net.minecraftforge.registries.IForgeRegistry.ClearCallback;
import net.minecraftforge.registries.IForgeRegistry.CreateCallback;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.api.utils.datastruct.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;

public class SkillDataKey<T> {
   private static final HashMultimap<Class<?>, SkillDataKey<?>> SKILL_DATA_KEYS = HashMultimap.create();
   private static final ResourceLocation CLASS_TO_DATA_KEYS = EpicFightMod.identifier("classtodatakeys");
   private static final ResourceLocation DATA_KEY_TO_ID = EpicFightMod.identifier("datakeytoid");
   private final PacketBufferCodec<T> packetCodec;
   private final T defaultValue;
   private final boolean syncronizeTrackingPlayers;

   public static SkillDataKey.SkillDataKeyCallbacks getRegistryCallback() {
      return SkillDataKey.SkillDataKeyCallbacks.INSTANCE;
   }

   public static <T> SkillDataKey<T> createSkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, Class<?>... skillClass) {
      return createSkillDataKey(packetCodec, defaultValue, false, skillClass);
   }

   public static <T> SkillDataKey<T> createSkillDataKey(
      PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass
   ) {
      SkillDataKey<T> key = new SkillDataKey<>(packetCodec, defaultValue, syncronizeTrackingPlayers);

      for (Class<?> cls : skillClass) {
         SKILL_DATA_KEYS.put(cls, key);
      }

      return key;
   }

   public static IdMapper<SkillDataKey<?>> getIdMap() {
      return (IdMapper<SkillDataKey<?>>)SkillDataKeys.REGISTRY.get().getSlaveMap(DATA_KEY_TO_ID, IdMapper.class);
   }

   public static Map<Class<?>, Set<SkillDataKey<?>>> getSkillDataKeyMap() {
      return (Map<Class<?>, Set<SkillDataKey<?>>>)SkillDataKeys.REGISTRY.get().getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
   }

   public SkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers) {
      this.packetCodec = packetCodec;
      this.defaultValue = defaultValue;
      this.syncronizeTrackingPlayers = syncronizeTrackingPlayers;
   }

   public T readFromBuffer(FriendlyByteBuf buffer) {
      return this.packetCodec.decode(buffer);
   }

   public void writeToBuffer(FriendlyByteBuf buffer, T value) {
      this.packetCodec.encode(value, buffer);
   }

   public T defaultValue() {
      return this.defaultValue;
   }

   public int getId() {
      return getIdMap().m_7447_(this);
   }

   public boolean syncronizeToTrackingPlayers() {
      return this.syncronizeTrackingPlayers;
   }

   private static class SkillDataKeyCallbacks implements BakeCallback<SkillDataKey<?>>, CreateCallback<SkillDataKey<?>>, ClearCallback<SkillDataKey<?>> {
      static final SkillDataKey.SkillDataKeyCallbacks INSTANCE = new SkillDataKey.SkillDataKeyCallbacks();

      public void onBake(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
         ClearableIdMapper<SkillDataKey<?>> skillDataKeyMap = (ClearableIdMapper<SkillDataKey<?>>)owner.getSlaveMap(
            SkillDataKey.DATA_KEY_TO_ID, ClearableIdMapper.class
         );
         owner.forEach(skillDataKeyMap::m_122667_);
         Map<Class<?>, Set<SkillDataKey<?>>> skillDataKeys = (Map<Class<?>, Set<SkillDataKey<?>>>)owner.getSlaveMap(SkillDataKey.CLASS_TO_DATA_KEYS, Map.class);
         SkillManager.getSkillRegistry()
            .forEach(
               skill -> {
                  Class<?> skillClass = skill.getClass();
                  Set<SkillDataKey<?>> dataKeySet = Sets.newHashSet();
                  skillDataKeys.put(skillClass, dataKeySet);

                  do {
                     if (SkillDataKey.SKILL_DATA_KEYS.containsKey(skillClass)) {
                        dataKeySet.addAll(SkillDataKey.SKILL_DATA_KEYS.get(skillClass));
                     }

                     skillClass = skillClass.getSuperclass();
                  } while (Skill.class.isAssignableFrom(skillClass));

                  if (!dataKeySet.isEmpty()) {
                     EpicFightMod.LOGGER
                        .info("Data keys " + dataKeySet.stream().map(SkillDataKeys.REGISTRY.get()::getKey).toList() + " for " + skill.getRegistryName());
                  }
               }
            );
      }

      public void onCreate(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
         owner.setSlaveMap(SkillDataKey.CLASS_TO_DATA_KEYS, Maps.newHashMap());
         owner.setSlaveMap(SkillDataKey.DATA_KEY_TO_ID, new ClearableIdMapper(owner.getKeys().size()));
      }

      public void onClear(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
         ((Map)owner.getSlaveMap(SkillDataKey.CLASS_TO_DATA_KEYS, Map.class)).clear();
         ((ClearableIdMapper)owner.getSlaveMap(SkillDataKey.DATA_KEY_TO_ID, ClearableIdMapper.class)).clear();
      }
   }
}
