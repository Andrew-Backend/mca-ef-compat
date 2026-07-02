package forge.net.mca.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.util.RegistryHelper;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.monster.Enemy;

public class GuardEnemiesSensor extends NearestLivingEntitySensor<LivingEntity> {
   public Set<MemoryModuleType<?>> m_7163_() {
      return ImmutableSet.of(MemoryModuleType.f_148204_, MemoryModuleType.f_148205_, (MemoryModuleType)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get());
   }

   protected void m_5578_(ServerLevel world, LivingEntity entity) {
      super.m_5578_(world, entity);
      entity.m_6274_().m_21886_((MemoryModuleType)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get(), this.getNearestHostile(entity));
   }

   private Optional<LivingEntity> getNearestHostile(LivingEntity entity) {
      return this.getVisibleMobs(entity)
         .flatMap(
            list -> list.m_186128_(e -> isGuardEnemy(e, entity)).filter(e -> e.m_20280_(entity) <= 2304.0).min((a, b) -> this.compareEntities(entity, a, b))
         );
   }

   private Optional<NearestVisibleLivingEntities> getVisibleMobs(LivingEntity entity) {
      return entity.m_6274_().m_257414_(MemoryModuleType.f_148205_);
   }

   private int compareEntities(LivingEntity entity, LivingEntity hostile1, LivingEntity hostile2) {
      int i = getPriority(hostile2, entity) - getPriority(hostile1, entity);
      return i == 0 ? this.compareDistances(entity, hostile1, hostile2) : i;
   }

   private int compareDistances(LivingEntity entity, LivingEntity hostile1, LivingEntity hostile2) {
      return Mth.m_14107_(hostile1.m_20280_(entity) - hostile2.m_20280_(entity));
   }

   public static boolean isGuardEnemy(LivingEntity entity, LivingEntity guard) {
      return getPriority(entity, guard) >= 0;
   }

   private static int getPriority(LivingEntity entity, LivingEntity guard) {
      if (entity instanceof VillagerEntityMCA villager) {
         return villager.isHostile() ? 10 : -1;
      } else if (guard != null && entity instanceof Mob && ((Mob)entity).m_5448_() == guard) {
         return 9;
      } else {
         ResourceLocation id = BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_());
         if (Config.getInstance().guardsTargetEntities.containsKey(id.toString())) {
            return Config.getInstance().guardsTargetEntities.get(id.toString());
         } else {
            Optional<Integer> tagPriority = getTagPriority(entity.m_6095_());
            if (tagPriority.isPresent()) {
               return tagPriority.get();
            } else {
               return Config.getInstance().guardsTargetMonsters && entity instanceof Enemy ? 3 : -1;
            }
         }
      }
   }

   private static Optional<Integer> getTagPriority(EntityType<?> type) {
      for (Entry<String, Integer> entry : Config.getInstance().guardsTargetEntities.entrySet()) {
         String key = entry.getKey();
         if (key.startsWith("#")) {
            ResourceLocation id = ResourceLocation.m_135820_(key.substring(1));
            if (id != null && RegistryHelper.isObjectInTag(BuiltInRegistries.f_256780_, id, type)) {
               return Optional.of(entry.getValue());
            }
         }
      }

      return Optional.empty();
   }

   protected int m_214020_() {
      return 48;
   }

   protected int m_214019_() {
      return 48;
   }
}
