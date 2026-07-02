package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(RangedCrossbowAttackGoal.class)
public class MixinRangedCrossbowAttackGoal {
   @Redirect(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
      ),
      method = "tick()V"
   )
   private void epicfight$tick(RangedAttackMob self, LivingEntity target, float velocity) {
      self.m_6504_(target, velocity);
      EpicFightCapabilities.getUnparameterizedEntityPatch((Entity)self, LivingEntityPatch.class).ifPresent(entitypatch -> entitypatch.playShootingAnimation());
   }
}
