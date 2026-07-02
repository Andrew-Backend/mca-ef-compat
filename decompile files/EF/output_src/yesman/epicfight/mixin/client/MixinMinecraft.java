package yesman.epicfight.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.config.ClientConfig;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
   @Shadow
   @Final
   private GameRenderer f_91063_;
   @Shadow
   @Final
   private Options f_91066_;
   @Shadow
   private LocalPlayer f_91074_;
   @Shadow
   private ClientLevel f_91073_;
   @Shadow
   private HitResult f_91077_;
   @Shadow
   private MultiPlayerGameMode f_91072_;

   @Shadow
   public Entity m_91288_() {
      throw new AbstractMethodError();
   }

   @Inject(at = @At("HEAD"), method = "handleKeybinds()V", cancellable = true)
   private void epicfight$handleKeybindsHEAD(CallbackInfo callbackInfo) {
      ClientEngine.getInstance().controlEngine.handleEpicFightKeyMappings();
   }

   @Inject(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V", shift = Shift.AFTER),
      method = "handleKeybinds()V"
   )
   private void epicfight$handleKeybindsINVOKE(CallbackInfo callbackInfo) {
      EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
      if (cameraApi.isTPSMode()) {
         cameraApi.setCameraRotations(this.f_91074_.m_146909_(), this.f_91074_.m_146908_(), true);
      }

      if (this.f_91066_.m_92176_() != CameraType.THIRD_PERSON_BACK && cameraApi.isLockingOnTarget()) {
         cameraApi.setLockOn(false);
      }
   }

   @Inject(at = @At("HEAD"), method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
   private void epicfight$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (EpicFightCameraAPI.getInstance().shouldHighlightTarget(entity)) {
         callbackInfo.setReturnValue(true);
         callbackInfo.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "startAttack", cancellable = true)
   private void onStartVanillaAttack(CallbackInfoReturnable<Boolean> cir) {
      if (ControlEngine.shouldDisableVanillaAttack()) {
         cir.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "continueAttack", cancellable = true)
   private void onContinueVanillaAttack(boolean leftClick, CallbackInfo ci) {
      if (ControlEngine.shouldDisableVanillaAttack()) {
         ci.cancel();
      }
   }

   @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick()V")
   private void epicfight$tick(GameRenderer gameRenderer, float partialTick) {
      if (EpicFightCameraAPI.getInstance().isTPSMode()) {
         this.pickInTPSPerspectiveMode(partialTick);
      } else {
         this.f_91063_.m_109087_(partialTick);
      }
   }

   @Unique
   private void pickInTPSPerspectiveMode(float partialTick) {
      Entity entity = this.m_91288_();
      if (entity != null && this.f_91073_ != null) {
         this.f_91077_ = EpicFightCameraAPI.getInstance().getCrosshairHitResult();
         if (this.f_91077_ != null) {
            double d0 = this.f_91072_.m_105286_();
            double entityReach = this.f_91074_.getEntityReach();
            double distanceLimit = Math.max(d0, entityReach) + ClientConfig.cameraZoom * 0.5;
            Vec3 hitPos = this.f_91077_.m_82450_();
            if (hitPos.m_82557_(this.f_91063_.m_109153_().m_90583_()) > distanceLimit * distanceLimit) {
               Vec3 cameraPos = this.f_91063_.m_109153_().m_90583_();
               this.f_91077_ = BlockHitResult.m_82426_(
                  hitPos, Direction.m_122366_(cameraPos.f_82479_, cameraPos.f_82480_, cameraPos.f_82481_), BlockPos.m_274446_(hitPos)
               );
            }
         }
      }
   }
}
