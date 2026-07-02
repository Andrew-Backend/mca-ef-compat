package fabric.net.mca.server.world.data;

import fabric.net.mca.Config;
import fabric.net.mca.util.MaxSizeHashMap;
import fabric.net.mca.util.NbtHelper;
import fabric.net.mca.util.WorldUtils;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_18;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import net.minecraft.class_4208;

public class VillagerTrackerManager extends class_18 {
   private static final int MAP_SIZE = 16384;
   private final Map<UUID, class_4208> entries = new MaxSizeHashMap<>(16384);

   public static VillagerTrackerManager get(class_3218 world) {
      return WorldUtils.loadData(world.method_8503().method_30002(), VillagerTrackerManager::new, VillagerTrackerManager::new, "mca_villager_tracker");
   }

   VillagerTrackerManager(class_3218 world) {
   }

   VillagerTrackerManager(class_2487 nbt) {
      this.entries.putAll(NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> NbtHelper.decodeGlobalPos(element)));
   }

   public class_2487 method_75(class_2487 nbt) {
      return NbtHelper.fromMap(nbt, this.entries, UUID::toString, NbtHelper::encodeGlobalPosition);
   }

   public void remove(UUID id) {
      this.entries.remove(id);
      this.method_80();
   }

   public static void update(class_1297 entity) {
      if (Config.getInstance().trackVillagerPosition && entity.method_37908() instanceof class_3218 serverWorld) {
         get(serverWorld).set(entity);
      }
   }

   public void set(class_1297 entity) {
      this.entries.put(entity.method_5667(), class_4208.method_19443(entity.method_37908().method_27983(), entity.method_24515()));
   }

   public class_4208 get(UUID id) {
      return this.entries.get(id);
   }
}
