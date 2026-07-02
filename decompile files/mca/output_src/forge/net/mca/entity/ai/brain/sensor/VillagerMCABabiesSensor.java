package forge.net.mca.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import forge.net.mca.entity.EntitiesMCA;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class VillagerMCABabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> m_7163_() {
      return ImmutableSet.of(MemoryModuleType.f_26366_);
   }

   protected void m_5578_(ServerLevel world, LivingEntity entity) {
      entity.m_6274_().m_21879_(MemoryModuleType.f_26366_, this.getVisibleVillagerBabies(entity));
   }

   private List<LivingEntity> getVisibleVillagerBabies(LivingEntity entities) {
      return this.getVisibleMobs(entities).m_186128_(this::isVillagerBaby).toList();
   }

   private boolean isVillagerBaby(LivingEntity entity) {
      return (entity.m_6095_() == EntitiesMCA.FEMALE_VILLAGER.get() || entity.m_6095_() == EntitiesMCA.MALE_VILLAGER.get()) && entity.m_6162_();
   }

   private NearestVisibleLivingEntities getVisibleMobs(LivingEntity entity) {
      return (NearestVisibleLivingEntities)entity.m_6274_().m_257414_(MemoryModuleType.f_148205_).orElseGet(NearestVisibleLivingEntities::m_186106_);
   }
}
