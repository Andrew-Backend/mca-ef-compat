package forge.net.mca.entity.ai.brain.sensor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.monster.Creeper;

public class ExplodingCreeperSensor extends NearestVisibleLivingEntitySensor {
   protected boolean m_142628_(LivingEntity entity, LivingEntity target) {
      return target instanceof Creeper && ((Creeper)target).m_32311_();
   }

   protected MemoryModuleType<LivingEntity> m_142149_() {
      return MemoryModuleType.f_26323_;
   }
}
