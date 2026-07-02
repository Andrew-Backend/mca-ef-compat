package yesman.epicfight.compat;

import com.github.exopandora.shouldersurfing.api.callback.ICameraCouplingCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public class ShoulderSurfingCompat implements IShoulderSurfingPlugin {
   public void register(IShoulderSurfingRegistrar registrar) {
      this.disableEpicFightCamera();
      this.registerShoulderSurfingEvents(registrar);
      this.registerEpicFightEvents();
   }

   private void disableEpicFightCamera() {
      EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.ShoulderSurfing);
   }

   private void registerShoulderSurfingEvents(IShoulderSurfingRegistrar registrar) {
      registrar.registerCameraCouplingCallback(new ShoulderSurfingCompat.CameraCouplingOnAttack());
      registrar.registerCameraCouplingCallback(new ShoulderSurfingCompat.CameraCouplingOnChargingSkill());
   }

   private void registerEpicFightEvents() {
      EpicFightClientHooks.Camera.BUILD_TRANSFORM_PRE.registerEvent(ShoulderSurfingCompat::buildCameraTransform);
      EpicFightClientHooks.Camera.LOCK_ON_TICK.registerEvent(ShoulderSurfingCompat::lockOnTick);
   }

   private static void buildCameraTransform(BuildCameraTransform.Pre event) {
      IShoulderSurfing shoulderSurfing = ShoulderSurfing.getInstance();
      if (shoulderSurfing.isShoulderSurfing()) {
         if (event.getCameraApi().isLockingOnTarget()) {
            syncLockOnRotations(event, shoulderSurfing);
         }

         event.cancel();
      }
   }

   private static void syncLockOnRotations(BuildCameraTransform.Pre event, IShoulderSurfing shoulderSurfing) {
      float camXRot = Mth.m_14189_(event.getPartialTick(), event.getCameraApi().getCameraXRotO(), event.getCameraApi().getCameraXRot());
      float camYRot = Mth.m_14189_(event.getPartialTick(), event.getCameraApi().getCameraYRotO(), event.getCameraApi().getCameraYRot());
      shoulderSurfing.getCamera().setXRot(camXRot);
      shoulderSurfing.getCamera().setYRot(camYRot);
   }

   private static void lockOnTick(LockOnEvent.Tick event) {
      IShoulderSurfing instance = ShoulderSurfing.getInstance();
      if (instance.isShoulderSurfing()) {
         LocalPlayer localPlayer = event.getCameraApi().getMinecraft().f_91074_;
         assert localPlayer != null;
         double toTargetDistanceSqr = localPlayer.m_20182_().m_82557_(event.getLockOnTarget().m_20182_());
         Vec3 lockStart = MathUtils.lerpVector(
            localPlayer.m_146892_(),
            event.getCameraApi().getMinecraft().f_91063_.m_109153_().m_90583_(),
            (float)Mth.m_144851_(toTargetDistanceSqr, 1.0, 18.0, 0.2F, 1.0)
         );
         Vec3 lockEnd = MathUtils.lerpVector(
            event.getLockOnTarget().m_146892_(), event.getLockOnTarget().m_20191_().m_82399_(), (float)Mth.m_144851_(toTargetDistanceSqr, 0.0, 18.0, 0.5, 1.0)
         );
         float clamp = 30.0F;
         Vec3 toTarget = lockEnd.m_82546_(lockStart);
         float xRot = (float)MathUtils.getXRotOfVector(toTarget);
         float yRot = (float)MathUtils.getYRotOfVector(toTarget);
         CameraType cameraType = event.getCameraApi().getMinecraft().f_91066_.m_92176_();
         if (!cameraType.m_90612_()) {
            xRot = Mth.m_14036_(xRot, -30.0F, 30.0F);
         }

         float xLerp = Mth.m_14036_(Mth.m_14177_(xRot - instance.getCamera().getXRot()) * 0.4F, -30.0F, 30.0F);
         float yLerp = Mth.m_14036_(Mth.m_14177_(yRot - instance.getCamera().getYRot()) * 0.4F, -30.0F, 30.0F);
         Vec3 playerToTarget = lockEnd.m_82546_(localPlayer.m_146892_());
         event.getCameraApi().setCameraRotations(instance.getCamera().getXRot() + xLerp, instance.getCamera().getYRot() + yLerp, false);
         event.setXRot((float)MathUtils.getXRotOfVector(playerToTarget));
         event.setYRot((float)MathUtils.getYRotOfVector(playerToTarget));
      }
   }

   private static class CameraCouplingOnAttack implements ICameraCouplingCallback {
      public boolean isForcingCameraCoupling(Minecraft minecraft) {
         return InputManager.isActionActive(EpicFightInputAction.ATTACK) || InputManager.isActionActive(MinecraftInputAction.ATTACK_DESTROY);
      }
   }

   private static class CameraCouplingOnChargingSkill implements ICameraCouplingCallback {
      public boolean isForcingCameraCoupling(Minecraft minecraft) {
         LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
         return localPlayerPatch == null ? false : localPlayerPatch.isHoldingAny();
      }
   }
}
