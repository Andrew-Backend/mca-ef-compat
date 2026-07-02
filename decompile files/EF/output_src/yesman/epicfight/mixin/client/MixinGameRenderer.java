package yesman.epicfight.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.config.ClientConfig;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
   @Shadow
   private Camera f_109054_;
   @Shadow
   private Minecraft f_109059_;

   @Shadow
   protected abstract void m_109087_(float var1);

   @Redirect(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"),
      method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"
   )
   private void epicfight$renderLevel(GameRenderer gameRenderer, float partialTick) {
      if (EpicFightCameraAPI.getInstance().isTPSMode()) {
         this.pickInTPSPerspective(partialTick);
      } else {
         this.m_109087_(partialTick);
      }
   }

   @Unique
   private void pickInTPSPerspective(float partialTick) {
      Entity entity = this.f_109059_.m_91288_();
      if (entity != null && this.f_109059_.f_91073_ != null) {
         this.f_109059_.f_91077_ = EpicFightCameraAPI.getInstance().getCrosshairHitResult();
         this.f_109059_.f_91076_ = EpicFightCameraAPI.getInstance().getFocusingEntity();
         if (this.f_109059_.f_91077_ != null) {
            double d0 = this.f_109059_.f_91072_.m_105286_();
            double entityReach = this.f_109059_.f_91074_.getEntityReach();
            double distanceLimit = Math.max(d0, entityReach) + ClientConfig.cameraZoom * 0.5;
            Vec3 hitPos = this.f_109059_.f_91077_.m_82450_();
            if (hitPos.m_82557_(this.f_109054_.m_90583_()) > distanceLimit * distanceLimit) {
               Vec3 cameraPos = this.f_109054_.m_90583_();
               this.f_109059_.f_91077_ = BlockHitResult.m_82426_(
                  hitPos, Direction.m_122366_(cameraPos.f_82479_, cameraPos.f_82480_, cameraPos.f_82481_), BlockPos.m_274446_(hitPos)
               );
            }
         }
      }
   }
}
