package forge.net.mca.server.world.data;

import forge.net.mca.Config;
import forge.net.mca.util.MaxSizeHashMap;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

public class VillagerTrackerManager extends SavedData {
   private static final int MAP_SIZE = 16384;
   private final Map<UUID, GlobalPos> entries = new MaxSizeHashMap<>(16384);

   public static VillagerTrackerManager get(ServerLevel world) {
      return WorldUtils.loadData(world.m_7654_().m_129783_(), VillagerTrackerManager::new, VillagerTrackerManager::new, "mca_villager_tracker");
   }

   VillagerTrackerManager(ServerLevel world) {
   }

   VillagerTrackerManager(CompoundTag nbt) {
      this.entries.putAll(NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> NbtHelper.decodeGlobalPos(element)));
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      return NbtHelper.fromMap(nbt, this.entries, UUID::toString, NbtHelper::encodeGlobalPosition);
   }

   public void remove(UUID id) {
      this.entries.remove(id);
      this.m_77762_();
   }

   public static void update(Entity entity) {
      if (Config.getInstance().trackVillagerPosition && entity.m_9236_() instanceof ServerLevel serverWorld) {
         get(serverWorld).set(entity);
      }
   }

   public void set(Entity entity) {
      this.entries.put(entity.m_20148_(), GlobalPos.m_122643_(entity.m_9236_().m_46472_(), entity.m_20183_()));
   }

   public GlobalPos get(UUID id) {
      return this.entries.get(id);
   }
}
