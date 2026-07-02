package quilt.net.mca.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1569;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3532;
import net.minecraft.class_4140;
import net.minecraft.class_4146;
import net.minecraft.class_6670;
import net.minecraft.class_7923;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.MemoryModuleTypeMCA;
import quilt.net.mca.util.RegistryHelper;

public class GuardEnemiesSensor extends class_4146<class_1309> {
   public Set<class_4140<?>> method_19099() {
      return ImmutableSet.of(class_4140.field_18441, class_4140.field_18442, (class_4140)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get());
   }

   protected void method_19101(class_3218 world, class_1309 entity) {
      super.method_19101(world, entity);
      entity.method_18868().method_18879((class_4140)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get(), this.getNearestHostile(entity));
   }

   private Optional<class_1309> getNearestHostile(class_1309 entity) {
      return this.getVisibleMobs(entity)
         .flatMap(
            list -> list.method_38980(e -> isGuardEnemy(e, entity))
               .filter(e -> e.method_5858(entity) <= 2304.0)
               .min((a, b) -> this.compareEntities(entity, a, b))
         );
   }

   private Optional<class_6670> getVisibleMobs(class_1309 entity) {
      return entity.method_18868().method_46873(class_4140.field_18442);
   }

   private int compareEntities(class_1309 entity, class_1309 hostile1, class_1309 hostile2) {
      int i = getPriority(hostile2, entity) - getPriority(hostile1, entity);
      return i == 0 ? this.compareDistances(entity, hostile1, hostile2) : i;
   }

   private int compareDistances(class_1309 entity, class_1309 hostile1, class_1309 hostile2) {
      return class_3532.method_15357(hostile1.method_5858(entity) - hostile2.method_5858(entity));
   }

   public static boolean isGuardEnemy(class_1309 entity, class_1309 guard) {
      return getPriority(entity, guard) >= 0;
   }

   private static int getPriority(class_1309 entity, class_1309 guard) {
      if (entity instanceof VillagerEntityMCA villager) {
         return villager.isHostile() ? 10 : -1;
      } else if (guard != null && entity instanceof class_1308 && ((class_1308)entity).method_5968() == guard) {
         return 9;
      } else {
         class_2960 id = class_7923.field_41177.method_10221(entity.method_5864());
         if (Config.getInstance().guardsTargetEntities.containsKey(id.toString())) {
            return Config.getInstance().guardsTargetEntities.get(id.toString());
         } else {
            Optional<Integer> tagPriority = getTagPriority(entity.method_5864());
            if (tagPriority.isPresent()) {
               return tagPriority.get();
            } else {
               return Config.getInstance().guardsTargetMonsters && entity instanceof class_1569 ? 3 : -1;
            }
         }
      }
   }

   private static Optional<Integer> getTagPriority(class_1299<?> type) {
      for (Entry<String, Integer> entry : Config.getInstance().guardsTargetEntities.entrySet()) {
         String key = entry.getKey();
         if (key.startsWith("#")) {
            class_2960 id = class_2960.method_12829(key.substring(1));
            if (id != null && RegistryHelper.isObjectInTag(class_7923.field_41177, id, type)) {
               return Optional.of(entry.getValue());
            }
         }
      }

      return Optional.empty();
   }

   protected int method_43081() {
      return 48;
   }

   protected int method_43082() {
      return 48;
   }
}
