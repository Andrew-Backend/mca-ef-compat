package yesman.epicfight.world.entity.ai.behavior;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class BackUpIfTooCloseStopInaction {
   public static OneShot<Mob> create(int tooCloseDistance, float strafeSpeed) {
      OneShot<Mob> parent = BackUpIfTooClose.m_257698_(tooCloseDistance, strafeSpeed);
      return BehaviorBuilder.m_257845_(mob -> {
         MobPatch<?> mobpatch = EpicFightCapabilities.getEntityPatch(mob, MobPatch.class);
         boolean inaction = mobpatch != null ? mobpatch.getEntityState().inaction() : false;
         if (inaction) {
            mob.m_21566_().m_24988_(0.0F, 0.0F);
         }

         return !inaction;
      }, parent);
   }
}
