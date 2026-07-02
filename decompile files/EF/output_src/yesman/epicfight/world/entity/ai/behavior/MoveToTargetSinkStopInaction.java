package yesman.epicfight.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class MoveToTargetSinkStopInaction extends MoveToTargetSink {
   protected boolean m_6737_(ServerLevel level, Mob mob, long gameTime) {
      if (super.m_6737_(level, mob, gameTime)) {
         MobPatch<?> mobpatch = EpicFightCapabilities.getEntityPatch(mob, MobPatch.class);
         if (mobpatch != null) {
            return !mobpatch.getEntityState().inaction();
         }
      }

      return false;
   }
}
