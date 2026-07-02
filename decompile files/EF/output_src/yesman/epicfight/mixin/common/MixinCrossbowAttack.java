package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(CrossbowAttack.class)
public abstract class MixinCrossbowAttack {
   @Redirect(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
      ),
      method = "crossbowAttack(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/LivingEntity;)V"
   )
   private void epicfight$crossbowAttack(RangedAttackMob self, LivingEntity target, float velocity) {
      self.m_6504_(target, velocity);
      EpicFightCapabilities.getUnparameterizedEntityPatch((Entity)self, LivingEntityPatch.class).ifPresent(entitypatch -> entitypatch.playShootingAnimation());
   }
}
