package fabric.net.mca.util;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_18;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2794;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_3194;
import net.minecraft.class_3195;
import net.minecraft.class_3215;
import net.minecraft.class_3218;
import net.minecraft.class_3730;
import net.minecraft.class_5425;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_6885;
import net.minecraft.class_7924;
import net.minecraft.class_6880.class_6883;
import net.minecraft.class_6885.class_6886;
import net.minecraft.class_6885.class_6888;

public interface WorldUtils {
   static List<class_1297> getCloseEntities(class_1937 world, class_1297 e, double range) {
      class_243 pos = e.method_19538();
      return world.method_8335(e, new class_238(pos, pos).method_1014(range));
   }

   static <T extends class_1297> List<T> getCloseEntities(class_1937 world, class_1297 e, double range, Class<T> c) {
      return getCloseEntities(world, e.method_19538(), range, c);
   }

   static <T extends class_1297> List<T> getCloseEntities(class_1937 world, class_243 pos, double range, Class<T> c) {
      return world.method_18467(c, new class_238(pos, pos).method_1014(range));
   }

   static <T extends class_18> T loadData(class_3218 world, Function<class_2487, T> loader, Function<class_3218, T> factory, String dataId) {
      return (T)world.method_17983().method_17924(loader, () -> factory.apply(world), dataId);
   }

   static void spawnEntity(class_1937 world, class_1308 entity, class_3730 reason) {
      entity.method_5943((class_5425)world, world.method_8404(entity.method_24515()), reason, null, null);
      world.method_8649(entity);
   }

   static Optional<class_2338> getClosestStructurePosition(class_3218 world, class_2338 center, class_2960 structure, int radius) {
      class_2378<class_3195> registry = world.method_30349().method_30530(class_7924.field_41246);
      class_3195 feature = (class_3195)registry.method_10223(structure);
      Optional<class_6883<class_3195>> entry = registry.method_40265(registry.method_10206(feature));
      if (entry.isPresent()) {
         class_6886<class_3195> of = class_6885.method_40246(new class_6880[]{(class_6880)entry.get()});
         Pair<class_2338, class_6880<class_3195>> pair = world.method_14178().method_12129().method_12103(world, of, center, radius, false);
         return pair == null ? Optional.empty() : Optional.ofNullable((class_2338)pair.getFirst());
      } else {
         return Optional.empty();
      }
   }

   static Optional<class_2338> getClosestStructurePosition(class_3218 world, class_2338 center, class_6862<class_3195> tag, int radius) {
      class_2378<class_3195> registry = world.method_30349().method_30530(class_7924.field_41246);
      Optional<class_6888<class_3195>> entryList = registry.method_40266(tag);
      if (entryList.isPresent()) {
         class_2794 chunkGenerator = world.method_14178().method_12129();
         Pair<class_2338, class_6880<class_3195>> pair = chunkGenerator.method_12103(world, (class_6885)entryList.get(), center, radius, false);
         return pair == null ? Optional.empty() : Optional.ofNullable((class_2338)pair.getFirst());
      } else {
         return Optional.empty();
      }
   }

   static boolean isChunkLoaded(class_1937 world, class_2382 pos) {
      return world instanceof class_3218 serverWorld ? isChunkLoaded(serverWorld, pos) : false;
   }

   static boolean isChunkLoaded(class_3218 world, class_2382 pos) {
      return isChunkLoaded(world, new class_2338(pos));
   }

   static boolean isChunkLoaded(class_3218 world, class_2338 pos) {
      class_1923 chunkPos = new class_1923(pos);
      class_2818 worldChunk = world.method_14178().method_21730(chunkPos.field_9181, chunkPos.field_9180);
      return worldChunk == null ? false : worldChunk.method_12225() == class_3194.field_13877 && world.method_37116(chunkPos.method_8324());
   }

   static boolean isAreaLoaded(class_3218 world, class_1923 pos, int radius) {
      class_3215 chunkManager = world.method_14178();

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            if (!chunkManager.method_12123(pos.field_9181 + x, pos.field_9180 + z)) {
               return false;
            }
         }
      }

      return true;
   }
}
