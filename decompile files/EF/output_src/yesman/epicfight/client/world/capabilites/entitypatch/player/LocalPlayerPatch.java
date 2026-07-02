package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.Clone;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimatorControl;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class LocalPlayerPatch extends AbstractClientPlayerPatch<LocalPlayer> {
   private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
   private Minecraft minecraft;
   private float staminaO;
   private int prevChargingAmount;
   private AnimationSubFileReader.PovSettings povSettings;
   private LocalPlayerPatch.FirstPersonLayer firstPersonLayer = new LocalPlayerPatch.FirstPersonLayer();

   public void onConstructed(LocalPlayer entity) {
      super.onConstructed(entity);
      this.minecraft = Minecraft.m_91087_();
   }

   public void onJoinWorld(LocalPlayer player, EntityJoinLevelEvent event) {
      super.onJoinWorld(player, event);
      this.eventListeners
         .addEventListener(
            PlayerEventListener.EventType.ACTION_EVENT_CLIENT, ACTION_EVENT_UUID, playerEvent -> ClientEngine.getInstance().controlEngine.unlockHotkeys()
         );
   }

   public void onRespawnLocalPlayer(Clone event) {
      this.onJoinWorld(event.getNewPlayer(), new EntityJoinLevelEvent(event.getNewPlayer(), event.getNewPlayer().m_9236_()));
   }

   @Override
   public void tick(LivingTickEvent event) {
      this.staminaO = this.getStamina();
      if (this.isHoldingAny() && this.getHoldingSkill() instanceof ChargeableSkill) {
         this.prevChargingAmount = this.getChargingAmount();
      } else {
         this.prevChargingAmount = 0;
      }

      super.tick(event);
   }

   @Override
   public void clientTick(LivingTickEvent event) {
      this.staminaO = this.getStamina();
      super.clientTick(event);
      AssetAccessor<? extends StaticAnimation> currentPlaying = this.firstPersonLayer.animationPlayer.getRealAnimation();
      boolean noPovAnimation = this.getClientAnimator()
         .iterVisibleLayersUntilFalse(
            layer -> {
               if (layer.isOff()) {
                  return true;
               }

               Optional<DirectStaticAnimation> optPovAnimation = layer.animationPlayer
                  .getRealAnimation()
                  .get()
                  .getProperty(ClientAnimationProperties.POV_ANIMATION);
               Optional<AnimationSubFileReader.PovSettings> optPovSettings = layer.animationPlayer
                  .getRealAnimation()
                  .get()
                  .getProperty(ClientAnimationProperties.POV_SETTINGS);
               optPovAnimation.ifPresent(povAnimation -> {
                  if (!povAnimation.equals(currentPlaying.get())) {
                     this.firstPersonLayer.playAnimation(povAnimation, layer.animationPlayer.getRealAnimation(), this, 0.0F);
                     this.povSettings = optPovSettings.get();
                  }
               });
               return !optPovAnimation.isPresent();
            }
         );
      if (noPovAnimation && !currentPlaying.equals(Animations.EMPTY_ANIMATION)) {
         this.firstPersonLayer.off();
      }

      this.firstPersonLayer.update(this);
      if (this.firstPersonLayer.animationPlayer.getAnimation().equals(Animations.EMPTY_ANIMATION)) {
         this.povSettings = null;
      }
   }

   @Override
   public boolean overrideRender() {
      return this.original.m_7306_(this.minecraft.f_91074_) && this.minecraft.f_91066_.m_92176_().m_90612_() && !ClientConfig.enableAnimatedFirstPersonModel
         ? false
         : super.overrideRender();
   }

   @Override
   public LivingEntity getTarget() {
      return EpicFightCameraAPI.getInstance().getFocusingEntity();
   }

   @Override
   public void toVanillaMode(boolean synchronize) {
      if (this.playerMode != PlayerPatch.PlayerMode.VANILLA) {
         ClientEngine.getInstance().renderEngine.downSlideSkillUI();
         if (ClientConfig.autoSwitchCamera) {
            this.minecraft.f_91066_.m_92157_(CameraType.FIRST_PERSON);
         }

         if (synchronize) {
            EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerPatch.PlayerMode.VANILLA));
         }
      }

      super.toVanillaMode(synchronize);
   }

   @Override
   public void toEpicFightMode(boolean synchronize) {
      if (this.playerMode != PlayerPatch.PlayerMode.EPICFIGHT) {
         ClientEngine.getInstance().renderEngine.upSlideSkillUI();
         if (ClientConfig.autoSwitchCamera) {
            this.minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
         }

         if (synchronize) {
            EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerPatch.PlayerMode.EPICFIGHT));
         }
      }

      super.toEpicFightMode(synchronize);
   }

   @Override
   public boolean isFirstPerson() {
      return this.minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
   }

   @Override
   public boolean shouldBlockMoving() {
      return InputManager.isActionActive(MinecraftInputAction.MOVE_BACKWARD) || InputManager.isActionActive(MinecraftInputAction.SNEAK);
   }

   @Override
   public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
      return !this.isLogicalClient() ? false : actionAnimation.shouldPlayerMove(this);
   }

   public float getStaminaO() {
      return this.staminaO;
   }

   public int getPrevChargingAmount() {
      return this.prevChargingAmount;
   }

   public LocalPlayerPatch.FirstPersonLayer getFirstPersonLayer() {
      return this.firstPersonLayer;
   }

   public AnimationSubFileReader.PovSettings getPovSettings() {
      return this.povSettings;
   }

   public boolean hasCameraAnimation() {
      return this.povSettings != null && this.povSettings.cameraTransform() != null;
   }

   @Override
   public void setStamina(float value) {
      EpicFightNetworkManager.sendToServer(new CPSetStamina(value, true));
   }

   @Override
   public void setModelYRot(float amount, boolean sendPacket) {
      super.setModelYRot(amount, sendPacket);
      if (sendPacket) {
         EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot(amount));
      }
   }

   public float getModelYRot() {
      return this.modelYRot;
   }

   public void setModelYRotInGui(float rotDeg) {
      this.useModelYRot = true;
      this.modelYRot = rotDeg;
   }

   public void disableModelYRotInGui(float originalDeg) {
      this.useModelYRot = false;
      this.modelYRot = originalDeg;
   }

   @Override
   public void disableModelYRot(boolean sendPacket) {
      super.disableModelYRot(sendPacket);
      if (sendPacket) {
         EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot());
      }
   }

   @Override
   public double checkXTurn(double xRot) {
      if (xRot == 0.0) {
         return xRot;
      }

      if (ClientConfig.enablePovAction && this.minecraft.f_91066_.m_92176_().m_90612_() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
         AnimationSubFileReader.PovSettings.ViewLimit viewLimit = this.getPovSettings().viewLimit();
         if (viewLimit != null) {
            float xRotDest = this.original.m_146909_() + (float)xRot * 0.15F;
            if (xRotDest <= viewLimit.xRotMin() || xRotDest >= viewLimit.xRotMax()) {
               return 0.0;
            }
         }
      }

      return xRot;
   }

   @Override
   public double checkYTurn(double yRot) {
      if (yRot == 0.0) {
         return yRot;
      }

      if (ClientConfig.enablePovAction && this.minecraft.f_91066_.m_92176_().m_90612_() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
         AnimationSubFileReader.PovSettings.ViewLimit viewLimit = this.getPovSettings().viewLimit();
         if (viewLimit != null) {
            float yCamera = Mth.m_14177_(this.original.m_146908_());
            float yBody = MathUtils.findNearestRotation(yCamera, this.getYRot());
            float yRotDest = yCamera + (float)yRot * 0.15F;
            float yRotClamped = Mth.m_14036_(yRotDest, yBody + viewLimit.yRotMin(), yBody + viewLimit.yRotMax());
            if (yRotDest != yRotClamped) {
               return 0.0;
            }
         }
      }

      return yRot;
   }

   @Override
   public void beginAction(ActionAnimation animation) {
      EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
      if (cameraApi.isTPSMode()) {
         if (cameraApi.getFocusingEntity() != null && animation instanceof AttackAnimation) {
            cameraApi.alignPlayerLookToCrosshair(false, true, true);
         } else {
            cameraApi.alignPlayerLookToCameraRotation(false, true, true);
         }
      }

      if (!this.useModelYRot || animation.getProperty(AnimationProperty.ActionAnimationProperty.SYNC_CAMERA).orElse(false)) {
         this.modelYRot = this.original.m_146908_();
      }

      if (cameraApi.getFocusingEntity() != null && cameraApi.isLockingOnTarget() && !cameraApi.getFocusingEntity().m_213877_()) {
         Vec3 playerPosition = this.original.m_20182_();
         Vec3 targetPosition = cameraApi.getFocusingEntity().m_20182_();
         Vec3 toTarget = targetPosition.m_82546_(playerPosition);
         this.original.m_146922_((float)MathUtils.getYRotOfVector(toTarget));
      }
   }

   @Override
   public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation) {
      this.animator.reserveAnimation(animation);
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, false, false, false));
   }

   @Override
   public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation) {
      this.animator.playAnimationInstantly(animation);
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, false, false, false));
   }

   @Override
   public void playShootingAnimation() {
      this.animator.playShootingAnimation();
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, 0.0F, false, true, false));
   }

   @Override
   public void stopPlaying(AssetAccessor<? extends StaticAnimation> animation) {
      this.animator.stopPlaying(animation);
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.STOP, animation, -1.0F, false, false, false));
   }

   @Override
   public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, false, true));
   }

   @Override
   public void playAnimationInClientSide(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
      this.animator.playAnimation(animation, transitionTimeModifier);
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, true, false));
   }

   @Override
   public void pauseAnimator(AnimatorControlPacket.Action action, boolean pause) {
      super.pauseAnimator(action, pause);
      EpicFightNetworkManager.sendToServer(new CPAnimatorControl(action, -1, 0.0F, pause, false, false));
   }

   @Override
   public void openSkillBook(ItemStack itemstack, InteractionHand hand) {
      if (itemstack.m_41782_() && itemstack.m_41783_().m_128441_("skill")) {
         Minecraft.m_91087_().m_91152_(new SkillBookScreen((Player)this.original, itemstack, hand));
      }
   }

   @Override
   public void resetHolding() {
      if (this.holdingSkill != null) {
         ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
      }

      super.resetHolding();
   }

   @Override
   public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
      super.updateHeldItem(mainHandCap, offHandCap);
      if (!ClientConfig.preferenceWork.checkHitResult()) {
         if (ClientConfig.combatPreferredItems.contains(this.original.m_21205_().m_41720_())) {
            this.toEpicFightMode(true);
         } else if (ClientConfig.miningPreferredItems.contains(this.original.m_21205_().m_41720_())) {
            this.toVanillaMode(true);
         }
      }
   }

   public boolean canPlayAttackAnimation() {
      if (this.isVanillaMode()) {
         return false;
      }

      EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
      HitResult hitResult = EpicFightCameraAPI.getInstance().isTPSMode()
            && cameraApi.getCrosshairHitResult() != null
            && cameraApi.getCrosshairHitResult().m_82450_().m_82557_(this.original.m_146892_()) < this.original.getBlockReach() * this.original.getBlockReach()
         ? cameraApi.getCrosshairHitResult()
         : this.minecraft.f_91077_;
      if (hitResult == null) {
         return true;
      }

      EntityHitResult entityHitResult = RenderEngine.asEntityHitResult(hitResult);
      if (entityHitResult != null) {
         Entity hitEntity = entityHitResult.m_82443_();
         if (!(hitEntity instanceof LivingEntity) && !(hitEntity instanceof PartEntity)) {
            return false;
         }
      }

      if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
         return true;
      }

      if (ClientConfig.preferenceWork.checkHitResult()) {
         if (!ClientConfig.combatPreferredItems.contains(this.original.m_21205_().m_41720_())) {
            return RenderEngine.hitResultNotEquals(this.minecraft.f_91077_, Type.BLOCK);
         } else {
            BlockHitResult blockHitResult = RenderEngine.asBlockHitResult(this.minecraft.f_91077_);
            if (blockHitResult != null && this.minecraft.f_91073_ != null) {
               BlockPos bp = blockHitResult.m_82425_();
               BlockState bs = this.minecraft.f_91073_.m_8055_(bp);
               return !this.original.m_21205_().m_41720_().m_6777_(bs, this.original.m_9236_(), bp, (Player)this.original)
                  || !this.original.m_21205_().m_41735_(bs);
            } else {
               return true;
            }
         }
      } else {
         return this.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT;
      }
   }

   @Deprecated(forRemoval = true)
   public boolean isTargetLockedOn() {
      return EpicFightCameraAPI.getInstance().isLockingOnTarget();
   }

   @Deprecated(forRemoval = true)
   public void setLockOn(boolean targetLockedOn) {
      EpicFightCameraAPI.getInstance().setLockOn(targetLockedOn);
   }

   @Deprecated(forRemoval = true)
   public void toggleLockOn() {
      this.setLockOn(!EpicFightCameraAPI.getInstance().isLockingOnTarget());
   }

   public class FirstPersonLayer extends Layer {
      private TransformSheet linkCameraTransform = new TransformSheet(
         List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty()))
      );

      public FirstPersonLayer() {
         super(null);
      }

      public void playAnimation(
         AssetAccessor<? extends StaticAnimation> nextFirstPersonAnimation,
         AssetAccessor<? extends StaticAnimation> originalAnimation,
         LivingEntityPatch<?> entitypatch,
         float transitionTimeModifier
      ) {
         Optional<AnimationSubFileReader.PovSettings> povSettings = originalAnimation.get().getProperty(ClientAnimationProperties.POV_SETTINGS);
         boolean hasPrevCameraAnimation = LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null;
         boolean hasNextCameraAnimation = povSettings.isPresent() && povSettings.get().cameraTransform() != null;
         if (hasPrevCameraAnimation || hasNextCameraAnimation) {
            if (hasPrevCameraAnimation) {
               this.linkCameraTransform.getKeyframes()[0]
                  .transform()
                  .copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
            } else {
               this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(JointTransform.empty());
            }

            if (hasNextCameraAnimation) {
               this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(povSettings.get().cameraTransform().getKeyframes()[0].transform());
            } else {
               this.linkCameraTransform.getKeyframes()[1].transform().clearTransform();
            }

            this.linkCameraTransform.getKeyframes()[1].setTime(nextFirstPersonAnimation.get().getTransitionTime());
         }

         super.playAnimation(nextFirstPersonAnimation, entitypatch, transitionTimeModifier);
      }

      public void off() {
         if (LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null) {
            this.linkCameraTransform.getKeyframes()[0]
               .transform()
               .copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
            this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(JointTransform.empty());
            this.linkCameraTransform.getKeyframes()[1].setTime(0.15F);
         }

         super.off(LocalPlayerPatch.this);
      }

      @Override
      protected Pose getCurrentPose(LivingEntityPatch<?> entitypatch) {
         return this.animationPlayer.isEmpty() ? super.getCurrentPose(entitypatch) : this.animationPlayer.getCurrentPose(entitypatch, 0.0F);
      }

      public TransformSheet getLinkCameraTransform() {
         return this.linkCameraTransform;
      }
   }
}
