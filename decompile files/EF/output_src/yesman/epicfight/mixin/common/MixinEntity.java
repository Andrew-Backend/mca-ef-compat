package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(Entity.class)
public abstract class MixinEntity {
   @Shadow
   private boolean f_19861_;
   @Unique
   private int lastOnGroundTick;

   @Inject(at = @At("HEAD"), method = "setOldPosAndRot()V", cancellable = true)
   private void epicfight_setOldPosAndRot(CallbackInfo callbackInfo) {
      Entity self = (Entity)this;
      EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, EntityPatch.class);
      if (entitypatch != null) {
         entitypatch.onOldPosUpdate();
      }
   }

   @Inject(at = @At("TAIL"), method = "onAddedToWorld()V", cancellable = true, remap = false)
   private void epicfight_onAddedToWorld(CallbackInfo callbackInfo) {
      Entity self = (Entity)this;
      EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, EntityPatch.class);
      if (entitypatch != null) {
         entitypatch.onAddedToWorld();
      }
   }

   @Inject(at = @At("HEAD"), method = "lerpMotion(DDD)V", cancellable = true)
   private void lerpMotion(double pX, double pY, double pZ, CallbackInfo callback) {
      Entity e = (Entity)this;
      EpicFightCapabilities.getUnparameterizedEntityPatch(e, LivingEntityPatch.class)
         .ifPresent(
            entitypatch -> {
               if (entitypatch.<Animator>getAnimator()
                  .getPlayerFor(null)
                  .getRealAnimation()
                  .get()
                  .getProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT)
                  .orElse(false)) {
                  callback.cancel();
               }
            }
         );
   }

   @ModifyVariable(method = "turn(DD)V", at = @At("HEAD"), ordinal = 0)
   private double epicfight$turnParam1(double yRot) {
      Entity e = (Entity)this;
      PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(e, PlayerPatch.class);
      return playerpatch != null ? playerpatch.checkYTurn(yRot) : yRot;
   }

   @ModifyVariable(method = "turn(DD)V", at = @At("HEAD"), ordinal = 1)
   private double epicfight$turnParam2(double xRot) {
      Entity e = (Entity)this;
      PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(e, PlayerPatch.class);
      return playerpatch != null ? playerpatch.checkXTurn(xRot) : xRot;
   }

   @Inject(at = @At("HEAD"), method = "setOnGroundWithKnownMovement(ZLnet/minecraft/world/phys/Vec3;)V")
   public void epicfight$setOnGroundWithKnownMovement(boolean pOnGround, Vec3 pMovement, CallbackInfo callbackInfo) {
      Entity self = (Entity)this;
      if (this.f_19861_) {
         this.lastOnGroundTick = self.f_19797_;
      }

      if (!this.f_19861_ && pOnGround && self.f_19797_ - this.lastOnGroundTick >= 4) {
         EpicFightCapabilities.getParameterizedEntityPatch(self, LivingEntity.class, LivingEntityPatch.class)
            .ifPresent(
               entitypatch -> entitypatch.onFall(
                  new LivingFallEvent((LivingEntity)entitypatch.getOriginal(), ((LivingEntity)entitypatch.getOriginal()).f_19789_, 1.0F)
               )
            );
      }
   }
}
