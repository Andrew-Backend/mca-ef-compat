package yesman.epicfight.api.client.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.entity.PartEntity;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Matrix4f;
import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.api.client.event.types.ActivateTPSCamera;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.CoupleTPSCamera;
import yesman.epicfight.api.client.event.types.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public final class EpicFightCameraAPI {
   private static final EpicFightCameraAPI INSTANCE = new EpicFightCameraAPI();
   private static final int MAX_ZOOM_TICK = 8;
   private final Minecraft minecraft;
   private final Set<UseAnim> tpsItemAnimations = Set.of(UseAnim.BLOCK, UseAnim.BOW, UseAnim.SPEAR, UseAnim.CROSSBOW);
   private boolean zoomingIn;
   private int zoomTick = 0;
   private int zoomOutDelay = 0;
   private float cameraXRotO;
   private float cameraXRot;
   private float cameraYRotO;
   private float cameraYRot;
   private boolean couplingYRot;
   private float fpvXRotO;
   private float fpvXRot;
   private float fpvYRotO;
   private float fpvYRot;
   private int fpvLerpTick = -1;
   private int maxFpvLerpTick;
   @Nullable
   private HitResult crosshairHitResult;
   @Nullable
   private LivingEntity focusingEntity;
   private boolean lockingOnTarget;
   private double accumulatedX;
   private int quickShiftDelay;

   public static EpicFightCameraAPI getInstance() {
      return INSTANCE;
   }

   private EpicFightCameraAPI() {
      this.minecraft = Minecraft.m_91087_();
   }

   public boolean isTPSMode() {
      if (this.minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK && ClientConfig.getCameraMode().shouldSwitch(this)) {
         ActivateTPSCamera event = new ActivateTPSCamera(this);
         EpicFightClientHooks.Camera.ACTIVATE_TPS_CAMERA.post(event);
         return !event.hasCanceled();
      } else {
         return false;
      }
   }

   public boolean isFirstPerson() {
      return this.minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
   }

   public boolean isZooming() {
      return this.zoomTick > 0;
   }

   public int zoomCount() {
      return this.zoomTick;
   }

   public void zoomIn() {
      if (!this.zoomingIn) {
         this.zoomingIn = true;
         this.zoomTick = this.zoomTick == 0 ? 1 : this.zoomTick;
      }
   }

   public void zoomOut(int zoomOutTicks) {
      if (this.zoomingIn) {
         this.zoomingIn = false;
         this.zoomOutDelay = zoomOutTicks;
      }
   }

   public float getCameraXRotO() {
      return this.cameraXRotO;
   }

   public float getCameraYRotO() {
      return this.cameraYRotO;
   }

   public float getCameraXRot() {
      return this.cameraXRot;
   }

   public float getCameraYRot() {
      return this.cameraYRot;
   }

   public void setCameraXRot(float xRot) {
      this.cameraXRot = xRot;
   }

   public void setCameraYRot(float yRot) {
      this.cameraYRot = yRot;
   }

   public void setCameraRotations(float xRot, float yRot, boolean syncOld) {
      this.cameraXRot = xRot;
      this.cameraYRot = yRot;
      if (syncOld) {
         this.cameraXRotO = this.cameraXRot;
         this.cameraYRotO = this.cameraYRot;
      }
   }

   public void setCouplingState(boolean flag) {
      if (this.isTPSMode()) {
         this.couplingYRot = flag;
      } else {
         this.couplingYRot = true;
      }
   }

   public boolean getCouplingState() {
      return this.couplingYRot;
   }

   public float getForwardXRot() {
      return this.isTPSMode() ? this.cameraXRot : this.minecraft.f_91074_.m_146909_();
   }

   public float getForwardYRot() {
      return this.isTPSMode() ? this.cameraYRot : this.minecraft.f_91074_.m_146908_();
   }

   public void fixFpvRotation(float xRot, float yRot, int lerpTick) {
      if (this.minecraft.f_91074_ != null) {
         this.fpvXRotO = Mth.m_14177_(this.minecraft.f_91074_.m_146909_());
         this.fpvXRot = Mth.m_14177_(xRot);
         this.fpvYRotO = Mth.m_14177_(this.minecraft.f_91074_.m_146908_());
         this.fpvYRot = Mth.m_14177_(yRot);
         this.fpvLerpTick = lerpTick;
         this.maxFpvLerpTick = lerpTick;
      }
   }

   public float getLerpedFpvXRot(float partialTick) {
      float delta = (float)this.fpvLerpTick / this.maxFpvLerpTick + (1.0F - partialTick) * 0.2F;
      return Mth.m_14189_(delta, this.fpvXRot, this.fpvXRotO);
   }

   public float getLerpedFpvYRot(float partialTick) {
      float delta = (float)this.fpvLerpTick / this.maxFpvLerpTick + (1.0F - partialTick) * 0.2F;
      return Mth.m_14189_(delta, this.fpvYRot, this.fpvYRotO);
   }

   public boolean isLerpingFpv() {
      return this.fpvLerpTick > -1;
   }

   @Nullable
   public HitResult getCrosshairHitResult() {
      return this.crosshairHitResult;
   }

   public LivingEntity getFocusingEntity() {
      return this.focusingEntity;
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public void setLockOn(boolean flag) {
      if (this.lockingOnTarget != flag) {
         boolean newlyFoundFocusingEntity = false;
         if (flag && this.focusingEntity == null) {
            newlyFoundFocusingEntity = this.setNextLockOnTarget(0, false, false);
         }

         if (!flag || this.focusingEntity != null) {
            boolean eventCanceled;
            if (flag) {
               LockOnEvent.Start lockOnEvent = new LockOnEvent.Start(this, this.focusingEntity);
               EpicFightClientHooks.Camera.LOCK_ON_START.post(lockOnEvent);
               eventCanceled = lockOnEvent.hasCanceled();
               if (eventCanceled && newlyFoundFocusingEntity) {
                  this.setFocusingEntity(null);
               }
            } else {
               LockOnEvent.Release lockOnEvent = new LockOnEvent.Release(this, this.focusingEntity);
               EpicFightClientHooks.Camera.LOCK_ON_RELEASED.post(lockOnEvent);
               eventCanceled = lockOnEvent.hasCanceled();
            }

            if (!eventCanceled) {
               this.lockingOnTarget = flag;
               if (flag && newlyFoundFocusingEntity) {
                  this.sendTargeting(this.focusingEntity);
               }

               if (!this.isTPSMode()) {
                  if (!flag) {
                     this.minecraft.f_91074_.m_146926_(this.cameraXRot);
                  } else {
                     this.setCameraRotations(this.minecraft.f_91074_.m_146909_(), this.minecraft.f_91074_.m_146908_(), true);
                  }
               }
            }
         }
      }
   }

   public void toggleLockOn() {
      this.setLockOn(!this.lockingOnTarget);
   }

   public boolean isLockingOnTarget() {
      return this.lockingOnTarget;
   }

   public int getFocusingEntityPickRange() {
      return this.minecraft.f_91074_ == null ? 0 : ClientConfig.lockOnRange;
   }

   public boolean setNextLockOnTarget(int direction) {
      return this.setNextLockOnTarget(direction, false, true);
   }

   public boolean setNextLockOnTarget(int direction, boolean necessarilyLockingOn, boolean sendChange) {
      if (!this.lockingOnTarget && necessarilyLockingOn) {
         return false;
      }

      List<Entity> entitiesInLevel = new ArrayList<>();
      this.minecraft.f_91073_.m_104735_().forEach(entitiesInLevel::add);
      Vec3 cameraLocation = this.minecraft.f_91063_.m_109153_().m_90583_();
      Matrix4f compactProjection = this.getCompactProjectionMatrix();
      double lockOnRange = this.getFocusingEntityPickRange();
      Optional<Pair<LivingEntity, Float>> next = entitiesInLevel.stream()
         .filter(
            entity -> this.predicateFocusableEntity(entity)
               && !entity.m_7306_(this.focusingEntity)
               && MathUtils.canBeSeen(entity, this.minecraft.f_91074_, lockOnRange)
               && (
                  this.minecraft
                        .m_91290_()
                        .m_114397_(entity, this.minecraft.f_91060_.getFrustum(), cameraLocation.m_7096_(), cameraLocation.m_7098_(), cameraLocation.m_7094_())
                     || entity.m_20367_(this.minecraft.f_91074_)
               )
               && entity.m_20280_(this.minecraft.f_91074_) < lockOnRange * lockOnRange
         )
         .map(
            entity -> Pair.of(
               entity, MathUtils.worldToScreenCoord(compactProjection, this.minecraft.f_91063_.m_109153_(), entity.m_20191_().m_82399_()).f_82470_
            )
         )
         .filter(
            pair -> (Float)pair.getSecond() >= -1.0F
               && (Float)pair.getSecond() <= 1.0F
               && (direction == 0 || MathUtils.getSign(((Float)pair.getSecond()).floatValue()) == MathUtils.getSign(direction))
         )
         .min((p1, p2) -> Float.compare(Math.abs((Float)p1.getSecond()), Math.abs((Float)p2.getSecond())));
      next.ifPresent(pair -> {
         this.setFocusingEntity((LivingEntity)pair.getFirst());
         if (sendChange) {
            this.sendTargeting(this.focusingEntity);
         }
      });
      return next.isPresent();
   }

   private Matrix4f getCompactProjectionMatrix() {
      PoseStack posestack = new PoseStack();
      double fov = this.minecraft.f_91063_.m_109141_(this.minecraft.f_91063_.m_109153_(), 1.0F, true);
      posestack.m_252931_(this.minecraft.f_91063_.m_253088_(fov));
      return posestack.m_85850_().m_252922_();
   }

   public void alignPlayerLookToCameraRotation(boolean noInterpolation, boolean syncBodyRot, boolean syncToServer) {
      if (this.minecraft.f_91074_ != null) {
         this.minecraft.f_91074_.m_146926_(this.cameraXRot);
         this.minecraft.f_91074_.m_146922_(this.cameraYRot);
         this.minecraft.f_91074_.m_5616_(this.cameraYRot);
         if (syncBodyRot) {
            this.minecraft.f_91074_.m_5618_(this.cameraYRot);
         }

         if (noInterpolation) {
            this.minecraft.f_91074_.f_19860_ = this.cameraXRot;
            this.minecraft.f_91074_.f_19859_ = this.cameraYRot;
            this.minecraft.f_91074_.f_20886_ = this.cameraYRot;
            if (syncBodyRot) {
               this.minecraft.f_91074_.f_20884_ = this.cameraYRot;
            }
         }

         if (syncToServer) {
            this.minecraft.f_91074_.f_108617_.m_104955_(new Rot(this.cameraYRot, this.cameraXRot, this.minecraft.f_91074_.m_20096_()));
         }
      }
   }

   public void alignPlayerLookToCrosshair(boolean noInterpolation, boolean syncBodyRot, boolean syncToServer) {
      if (this.minecraft.f_91074_ != null) {
         if (this.crosshairHitResult == null) {
            this.alignPlayerLookToCameraRotation(noInterpolation, syncBodyRot, syncToServer);
         } else {
            Vec3 fromEyeToDest = this.crosshairHitResult.m_82450_().m_82546_(this.minecraft.f_91074_.m_146892_());
            float xRot = (float)MathUtils.getXRotOfVector(fromEyeToDest);
            float yRot = (float)MathUtils.getYRotOfVector(fromEyeToDest);
            this.minecraft.f_91074_.m_146926_(xRot);
            this.minecraft.f_91074_.m_146922_(yRot);
            this.minecraft.f_91074_.m_5616_(yRot);
            if (syncBodyRot) {
               this.minecraft.f_91074_.m_5618_(yRot);
            }

            if (noInterpolation) {
               this.minecraft.f_91074_.f_19860_ = xRot;
               this.minecraft.f_91074_.f_19859_ = yRot;
               this.minecraft.f_91074_.f_20886_ = yRot;
               if (syncBodyRot) {
                  this.minecraft.f_91074_.f_20884_ = yRot;
               }
            }

            if (syncToServer) {
               this.minecraft.f_91074_.f_108617_.m_104955_(new Rot(yRot, xRot, this.minecraft.f_91074_.m_20096_()));
            }
         }
      }
   }

   public Vec3 getRelativeMove(Vec3 relative, float magnitude) {
      return Entity.m_20015_(relative, magnitude, this.isTPSMode() && !this.lockingOnTarget ? this.cameraYRot : this.minecraft.f_91074_.m_146908_());
   }

   public boolean shouldHighlightTarget(@NotNull Entity entity) {
      if (ClientConfig.enableTargetEntityGuide && this.minecraft.f_91074_ != null && entity.m_7306_(this.focusingEntity)) {
         if (!EpicFightCapabilities.getUnparameterizedEntityPatch(entity, EntityPatch.class)
            .map(entitypatch -> entitypatch.isOutlineVisible(this.minecraft.f_91074_))
            .orElse(false)) {
            return false;
         }

         if (this.lockingOnTarget) {
            return true;
         }

         if (ClientConfig.combatPreferredItems.contains(this.minecraft.f_91074_.m_21205_().m_41720_())) {
            BlockHitResult blockHitResult = RenderEngine.asBlockHitResult(this.minecraft.f_91077_);
            if (blockHitResult == null) {
               return true;
            }

            BlockPos bp = blockHitResult.m_82425_();
            BlockState bs = this.minecraft.f_91073_.m_8055_(bp);
            return !this.minecraft.f_91074_.m_21205_().m_41720_().m_6777_(bs, this.minecraft.f_91074_.m_9236_(), bp, this.minecraft.f_91074_)
               || !this.minecraft.f_91074_.m_21205_().m_41735_(bs);
         } else {
            return !RenderEngine.hitResultEquals(this.minecraft.f_91077_, Type.BLOCK);
         }
      } else {
         return false;
      }
   }

   @Internal
   public boolean turnCamera(double dy, double dx) {
      MutableBoolean cancel = new MutableBoolean(false);
      EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.f_91074_, LocalPlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               cancel.setValue(this.isTPSMode() || this.lockingOnTarget);
               if (cancel.booleanValue()) {
                  float modifier = this.lockingOnTarget && !InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)
                     ? (ClientConfig.lockOnQuickShift ? 0.005F : 0.0F)
                     : 0.15F;
                  this.setCameraRotations(Mth.m_14036_(this.cameraXRot + (float)dx * modifier, -90.0F, 90.0F), this.cameraYRot + (float)dy * modifier, false);
                  if (ClientConfig.lockOnQuickShift && this.quickShiftDelay <= 0) {
                     this.accumulatedX += -dy * 0.15F;
                     if (Math.abs(this.accumulatedX) > 20.0 && this.lockingOnTarget) {
                        this.setNextLockOnTarget(Mth.m_14205_(this.accumulatedX), true, true);
                        this.accumulatedX = 0.0;
                        this.quickShiftDelay = 4;
                     }
                  }

                  this.accumulatedX *= 0.98;
               }
            }
         );
      return cancel.booleanValue();
   }

   @Internal
   public void preClientTick() {
      this.cameraXRotO = this.cameraXRot;
      this.cameraYRotO = this.cameraYRot;
      if (this.zoomTick > 0) {
         if (this.zoomOutDelay > 0) {
            this.zoomOutDelay--;
         } else {
            this.zoomTick = this.zoomTick + (this.zoomingIn ? 1 : -1);
            this.zoomTick = Math.min(8, this.zoomTick);
         }
      }

      if (this.quickShiftDelay > 0) {
         this.quickShiftDelay--;
      }
   }

   @Internal
   public void postClientTick() {
      if (!this.minecraft.m_91104_() && this.minecraft.f_91074_ != null) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.f_91074_, LocalPlayerPatch.class)
            .ifPresent(
               playerpatchx -> {
                  CapabilityItem mainhandItemCap = playerpatchx.getAdvancedHoldingItemCapability(InteractionHand.MAIN_HAND);
                  CapabilityItem offhandItemCap = playerpatchx.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
                  CapabilityItem.ZoomInType rangeWeaponZoomInType = !mainhandItemCap.isEmpty()
                        && mainhandItemCap.getZoomInType() != CapabilityItem.ZoomInType.NONE
                     ? mainhandItemCap.getZoomInType()
                     : offhandItemCap.getZoomInType();
                  switch (rangeWeaponZoomInType) {
                     case ALWAYS:
                        this.zoomIn();
                        break;
                     case USE_TICK:
                        if (playerpatchx.getOriginal().m_21212_() > 0) {
                           this.zoomIn();
                        } else {
                           this.zoomOut(8);
                        }
                        break;
                     case AIMING:
                        if (playerpatchx.getClientAnimator().isAiming()) {
                           this.zoomIn();
                        } else {
                           this.zoomOut(8);
                        }
                     case CUSTOM:
                        break;
                     default:
                        this.zoomOut(1);
                  }
               }
            );
         double pickRange = ((Integer)this.minecraft.f_91066_.m_231984_().m_231551_()).intValue() * 16.0;
         Camera mainCamera = this.minecraft.f_91063_.m_109153_();
         Vec3 cameraPos = mainCamera.m_90583_();
         Vec3 lookVec = new Vec3(mainCamera.m_253058_());
         Vec3 rayEed = cameraPos.m_82520_(lookVec.f_82479_ * pickRange, lookVec.f_82480_ * pickRange, lookVec.f_82481_ * pickRange);
         LocalPlayer localPlayer = this.minecraft.f_91074_;
         this.crosshairHitResult = localPlayer.m_9236_().m_45547_(new ClipContext(cameraPos, rayEed, Block.OUTLINE, Fluid.NONE, localPlayer));
         int focusingRange = this.getFocusingEntityPickRange();
         double entityPickRange = Math.min(this.crosshairHitResult.m_82450_().m_82557_(cameraPos), focusingRange * focusingRange);
         AABB aabb = localPlayer.m_20191_()
            .m_82383_(cameraPos.m_82546_(localPlayer.m_20299_(1.0F)))
            .m_82369_(lookVec.m_82490_(entityPickRange))
            .m_82377_(1.0, 1.0, 1.0);
         EntityHitResult entityHitResult = ProjectileUtil.m_37287_(
            localPlayer, cameraPos, rayEed, aabb, entity -> !entity.m_5833_() && entity.m_6087_(), entityPickRange
         );
         if (entityHitResult != null) {
            this.crosshairHitResult = entityHitResult;
            if (!entityHitResult.m_82443_().m_7306_(this.focusingEntity)) {
               if (entityHitResult.m_82443_() instanceof LivingEntity livingentity) {
                  if (!(entityHitResult.m_82443_() instanceof ArmorStand)
                     && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
                     this.setFocusingEntity(livingentity);
                  }
               } else if (entityHitResult.m_82443_() instanceof PartEntity<?> partEntity) {
                  if (partEntity.getParent() instanceof LivingEntity parentLivingEntity
                     && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
                     this.setFocusingEntity(parentLivingEntity);
                  }
               } else {
                  this.setLockOn(false);
                  this.setFocusingEntity(null);
               }

               if (this.focusingEntity != null) {
                  this.sendTargeting(this.focusingEntity);
               }
            }
         }

         boolean tpsMode = this.isTPSMode();
         if (tpsMode) {
            Vec3 view = new Vec3(mainCamera.m_253058_());
            if (view.m_82526_(this.crosshairHitResult.m_82450_().m_82546_(localPlayer.m_146892_()).m_82541_()) < -0.1) {
               this.crosshairHitResult = BlockHitResult.m_82426_(
                  cameraPos.m_82520_(lookVec.f_82479_ * 50.0, lookVec.f_82480_ * 50.0, lookVec.f_82481_ * 50.0), Direction.UP, BlockPos.f_121853_
               );
               if (!this.lockingOnTarget && this.focusingEntity != null) {
                  this.setFocusingEntity(null);
                  this.sendTargeting(null);
               }
            }

            if (this.focusingEntity != null) {
               double dot = view.m_82526_(this.focusingEntity.m_146892_().m_82546_(localPlayer.m_146892_()));
               if (dot < -0.1 && !this.lockingOnTarget) {
                  this.setFocusingEntity(null);
                  this.sendTargeting(null);
               }
            }
         }

         if (this.focusingEntity != null) {
            if (this.lockingOnTarget && !this.focusingEntity.m_6084_()) {
               boolean releaseLockOn = !ClientConfig.lockOnQuickShift || !this.setNextLockOnTarget(0, true, true);
               if (releaseLockOn) {
                  this.setLockOn(false);
               }
            } else {
               double distance = this.minecraft.f_91074_.m_20238_(this.focusingEntity.m_20182_());
               double maxLockOnDistance = focusingRange * focusingRange;
               if (this.focusingEntity.m_20177_(localPlayer)
                  || distance > maxLockOnDistance
                  || !MathUtils.canBeSeen(this.focusingEntity, this.minecraft.f_91074_, maxLockOnDistance)
                  || !this.lockingOnTarget
                     && this.focusingEntity.m_20182_().m_82546_(mainCamera.m_90583_()).m_82541_().m_82526_(new Vec3(mainCamera.m_253058_()))
                        < Mth.m_14085_(0.8, 0.96, Mth.m_14112_(Mth.m_14008_(distance, 9.0, 64.0), 9.0, 64.0))) {
                  if (this.lockingOnTarget) {
                     this.setLockOn(false);
                  }

                  this.setFocusingEntity(null);
                  this.sendTargeting(null);
               }
            }
         }

         if (this.isFirstPerson() && this.isLerpingFpv()) {
            this.fpvLerpTick--;
            if (!this.isLerpingFpv()) {
               this.minecraft.f_91074_.m_146926_(this.fpvXRot);
               this.minecraft.f_91074_.m_146922_(this.fpvYRot);
            }
         } else if (!this.isTPSMode() && !this.lockingOnTarget) {
            this.cameraXRot = this.minecraft.f_91074_.m_146909_();
            this.cameraYRot = this.minecraft.f_91074_.m_146908_();
         } else {
            LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(localPlayer, LocalPlayerPatch.class);
            float clamp = 30.0F;
            float desiredXRot = 0.0F;
            float desiredYRot = 0.0F;
            if (this.focusingEntity != null
               && this.lockingOnTarget
               && !this.isLerpingFpv()
               && !InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
               Vec3 lockEnd;
               Vec3 lockStart;
               if (tpsMode) {
                  double toTargetDistanceSqr = localPlayer.m_20182_().m_82557_(this.focusingEntity.m_20182_());
                  lockStart = MathUtils.lerpVector(localPlayer.m_146892_(), cameraPos, (float)Mth.m_144851_(toTargetDistanceSqr, 1.0, 18.0, 0.2F, 1.0));
                  lockEnd = MathUtils.lerpVector(
                     this.focusingEntity.m_146892_(), this.focusingEntity.m_20191_().m_82399_(), (float)Mth.m_144851_(toTargetDistanceSqr, 0.0, 18.0, 0.5, 1.0)
                  );
               } else {
                  lockStart = localPlayer.m_146892_();
                  lockEnd = this.focusingEntity.m_146892_();
               }

               Vec3 toTarget = lockEnd.m_82546_(lockStart);
               float xRot = (float)MathUtils.getXRotOfVector(toTarget);
               float yRot = (float)MathUtils.getYRotOfVector(toTarget);
               CameraType cameraType = this.minecraft.f_91066_.m_92176_();
               if (!cameraType.m_90612_()) {
                  xRot = Mth.m_14036_(xRot, -clamp, clamp);
               }

               xRot += !cameraType.m_90612_() && !tpsMode ? 30.0F + xRot * 0.5F : 0.0F;
               float xLerp = Mth.m_14036_(Mth.m_14177_(xRot - this.cameraXRot) * 0.4F, -clamp, clamp);
               float yLerp = Mth.m_14036_(Mth.m_14177_(yRot - this.cameraYRot) * 0.4F, -clamp, clamp);
               Vec3 playerToTarget = lockEnd.m_82546_(localPlayer.m_146892_());
               this.setCameraRotations(this.cameraXRot + xLerp, this.cameraYRot + yLerp, false);
               desiredXRot = (float)MathUtils.getXRotOfVector(playerToTarget);
               desiredYRot = (float)MathUtils.getYRotOfVector(playerToTarget);
            } else if (this.lockingOnTarget && InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
               desiredXRot = this.cameraXRot;
               desiredYRot = this.cameraYRot;
            } else if (tpsMode) {
               CoupleTPSCamera coupleCameraEvent = this.predicateCouplingPlayer();
               boolean shouldCoupling = coupleCameraEvent.shouldCoupleCamera();
               if (!(Mth.m_14154_(Mth.m_14177_(this.cameraYRot - localPlayer.f_20883_)) <= 51.0F) && !shouldCoupling) {
                  desiredXRot = 0.0F;
                  desiredYRot = localPlayer.f_20883_;
                  clamp = 15.0F;
               } else if (coupleCameraEvent.isOnlyMoving()) {
                  Vec2 movemoventPulse = localPlayer.f_108618_.m_108575_();
                  desiredYRot = this.cameraYRot + (float)MathUtils.getYRotOfVector(new Vec3(movemoventPulse.f_82470_, 0.0, movemoventPulse.f_82471_));
                  desiredXRot = desiredYRot == this.cameraYRot ? this.cameraXRot : 0.0F;
               } else {
                  Vec3 toHitResult;
                  if (this.lockingOnTarget) {
                     toHitResult = this.focusingEntity.m_146892_();
                  } else if (this.crosshairHitResult.m_6662_() == Type.MISS) {
                     double delta = Mth.m_14036_(localPlayer.m_146909_(), -30.0F, 0.0F) / -30.0F;
                     double lookVecScale = Mth.m_14085_(30.0, 75.0, delta);
                     toHitResult = cameraPos.m_82549_(lookVec.m_82490_(lookVecScale));
                  } else {
                     toHitResult = this.crosshairHitResult.m_82450_();
                  }

                  toHitResult = toHitResult.m_82546_(localPlayer.m_146892_());
                  desiredXRot = (float)MathUtils.getXRotOfVector(toHitResult);
                  desiredYRot = shouldCoupling
                     ? (Math.abs(this.cameraXRot) > 80.0F ? this.cameraYRot : (float)MathUtils.getYRotOfVector(toHitResult))
                     : this.cameraYRot;
               }
            }

            if (this.focusingEntity != null && this.lockingOnTarget) {
               LockOnEvent.Tick lockOnEventTick = new LockOnEvent.Tick(this, this.focusingEntity, desiredXRot, desiredYRot);
               EpicFightClientHooks.Camera.LOCK_ON_TICK.post(lockOnEventTick);
               desiredXRot = lockOnEventTick.getModifiedXRot();
               desiredYRot = lockOnEventTick.getModifiedYRot();
            }

            if ((playerpatch == null || !playerpatch.getEntityState().turningLocked() || playerpatch.getEntityState().lockonRotate())
               && (tpsMode || this.minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK && this.lockingOnTarget)) {
               float xDelta = Mth.m_14036_(Mth.m_14177_(desiredXRot - localPlayer.m_146909_()), -clamp, clamp);
               float yDelta = Mth.m_14036_(Mth.m_14177_(desiredYRot - localPlayer.m_146908_()), -clamp, clamp);
               localPlayer.m_146926_(localPlayer.m_146909_() + xDelta);
               localPlayer.m_146922_(localPlayer.m_146908_() + yDelta);
            }
         }
      }
   }

   @Internal
   public BuildCameraTransform.Pre setupCamera(Camera camera, float partialTick) {
      BuildCameraTransform.Pre event = new BuildCameraTransform.Pre(this, camera, partialTick);
      if (!camera.m_90592_().m_7306_(this.minecraft.f_91074_)) {
         event.cancel();
         return event;
      }

      EpicFightClientHooks.Camera.BUILD_TRANSFORM_PRE.post(event);
      if (event.hasCanceled()) {
         return event;
      }

      if (this.isTPSMode()) {
         float partialZoomTick = this.zoomTick == 0 ? 0.0F : Math.min(this.zoomTick + (this.zoomingIn ? partialTick : -partialTick), 7.0F);
         float delta = ClientConfig.getCameraMode() == ClientConfig.TPSType.WHEN_AIMING ? partialZoomTick / 7.0F : 1.0F;
         float xRot = Mth.m_14189_(delta, this.minecraft.f_91074_.m_146909_(), Mth.m_14189_(partialTick, this.cameraXRotO, this.cameraXRot));
         float yRot = Mth.m_14189_(delta, this.minecraft.f_91074_.m_146908_(), Mth.m_14189_(partialTick, this.cameraYRotO, this.cameraYRot));
         camera.m_90572_(yRot, xRot);
         Vec3 playerPos = new Vec3(
            Mth.m_14139_(partialTick, camera.m_90592_().f_19854_, camera.m_90592_().m_20185_()),
            Mth.m_14139_(partialTick, camera.m_90592_().f_19855_, camera.m_90592_().m_20186_()) + Mth.m_14139_(partialTick, camera.f_90563_, camera.f_90562_),
            Mth.m_14139_(partialTick, camera.m_90592_().f_19856_, camera.m_90592_().m_20189_())
         );
         Vec3f relocation = new Vec3f(ClientConfig.cameraHorizontalLocation * 0.2F, ClientConfig.cameraVerticalLocation * 0.2F, 0.0F);
         OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-yRot, Vec3f.Y_AXIS), relocation, relocation);
         double cameraZoom = ClientConfig.cameraZoom * 0.5 - partialZoomTick * 0.1;
         double hitDistance = 1.0;

         for (int i = 0; i < 8; i++) {
            float f = (i & 1) * 2 - 1;
            float f1 = (i >> 1 & 1) * 2 - 1;
            float f2 = (i >> 2 & 1) * 2 - 1;
            f *= 0.1F;
            f1 *= 0.1F;
            f2 *= 0.1F;
            Vec3 vec3 = playerPos.m_82520_(f, f1, f2);
            Vec3 vec31 = new Vec3(
               playerPos.f_82479_ + (relocation.x - camera.m_253058_().x() * cameraZoom) + f,
               playerPos.f_82480_ + (relocation.y - camera.m_253058_().y() * cameraZoom) + f1,
               playerPos.f_82481_ + (relocation.z - camera.m_253058_().z() * cameraZoom) + f2
            );
            double length = vec3.m_82554_(vec31);
            HitResult hitresult = this.minecraft.f_91073_.m_45547_(new ClipContext(vec3, vec31, Block.VISUAL, Fluid.NONE, camera.m_90592_()));
            if (hitresult.m_6662_() != Type.MISS) {
               double d0 = hitresult.m_82450_().m_82554_(playerPos) / length;
               if (d0 < hitDistance) {
                  hitDistance = d0;
               }
            }
         }

         double nearestX = playerPos.f_82479_ + (relocation.x - camera.m_253058_().x() * cameraZoom) * hitDistance;
         double nearestY = playerPos.f_82480_ + (relocation.y - camera.m_253058_().y() * cameraZoom) * hitDistance;
         double nearestZ = playerPos.f_82481_ + (relocation.z - camera.m_253058_().z() * cameraZoom) * hitDistance;
         if (Float.compare(1.0F, delta) == 0) {
            camera.m_90584_(nearestX, nearestY, nearestZ);
         } else {
            camera.m_90572_(this.minecraft.f_91074_.m_5675_(partialTick), this.minecraft.f_91074_.m_5686_(partialTick));
            camera.m_90584_(
               Mth.m_14139_(partialTick, this.minecraft.f_91074_.f_19854_, this.minecraft.f_91074_.m_20185_()),
               Mth.m_14139_(partialTick, this.minecraft.f_91074_.f_19855_, this.minecraft.f_91074_.m_20186_())
                  + Mth.m_14179_(partialTick, camera.f_90563_, camera.f_90562_),
               Mth.m_14139_(partialTick, this.minecraft.f_91074_.f_19856_, this.minecraft.f_91074_.m_20189_())
            );
            camera.m_90568_(-camera.m_90566_(4.0), 0.0, 0.0);
            camera.m_90572_(yRot, xRot);
            camera.m_90584_(
               camera.m_90583_().m_7096_() + (nearestX - camera.m_90583_().m_7096_()) * delta,
               camera.m_90583_().m_7098_() + (nearestY - camera.m_90583_().m_7098_()) * delta,
               camera.m_90583_().m_7094_() + (nearestZ - camera.m_90583_().m_7094_()) * delta
            );
         }

         event.setVanillaCameraSetupCanceled(true);
         this.fireCameraBuildPost(camera, partialTick);
         return event;
      } else {
         if (this.lockingOnTarget && this.focusingEntity != null) {
            if (this.minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK) {
               float xRot = Mth.m_14189_(partialTick, this.cameraXRotO, this.cameraXRot);
               float yRot = Mth.m_14189_(partialTick, this.cameraYRotO, this.cameraYRot);
               camera.m_90572_(yRot, xRot);
               camera.m_90584_(
                  Mth.m_14139_(partialTick, camera.m_90592_().f_19854_, camera.m_90592_().m_20185_()),
                  Mth.m_14139_(partialTick, camera.m_90592_().f_19855_, camera.m_90592_().m_20186_())
                     + Mth.m_14179_(partialTick, camera.f_90563_, camera.f_90562_),
                  Mth.m_14139_(partialTick, camera.m_90592_().f_19856_, camera.m_90592_().m_20189_())
               );
               if (camera.m_90594_()) {
                  camera.m_90568_(-camera.m_90566_(4.0), 0.0, 0.0);
               } else if (camera.m_90592_() instanceof LivingEntity livingEntity && livingEntity.m_5803_()) {
                  Direction direction = ((LivingEntity)camera.m_90592_()).m_21259_();
                  camera.m_90572_(direction != null ? direction.m_122435_() - 180.0F : 0.0F, 0.0F);
                  camera.m_90568_(0.0, 0.3, 0.0);
               }

               event.setVanillaCameraSetupCanceled(true);
               this.fireCameraBuildPost(camera, partialTick);
               return event;
            }

            if (this.minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
               if (!InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
                  camera.m_90592_().m_146926_(Mth.m_14189_(partialTick, this.cameraXRotO, this.cameraXRot));
                  camera.m_90592_().m_146922_(Mth.m_14189_(partialTick, this.cameraYRotO, this.cameraYRot));
               } else {
                  this.cameraXRot = camera.m_90592_().m_146909_();
                  this.cameraYRot = camera.m_90592_().m_146908_();
               }
            }
         }

         return event;
      }
   }

   @Internal
   public void fireCameraBuildPost(Camera camera, float partialTick) {
      EpicFightClientHooks.Camera.BUILD_TRANSFORM_POST.post(new BuildCameraTransform.Post(this, camera, partialTick));
   }

   @Internal
   public float getYRotForHead(Player player) {
      if (!player.m_7578_()) {
         throw new IllegalArgumentException("Must pass a LocalPlayer to getYRotForHead(Player)");
      } else if (!this.isTPSMode()) {
         return player.m_146908_();
      } else {
         CoupleTPSCamera coupleCamera = this.predicateCouplingPlayer();
         if (coupleCamera.shouldCoupleCamera()) {
            return coupleCamera.isOnlyMoving() ? player.m_146908_() : this.cameraYRot;
         } else {
            return Mth.m_14154_(Mth.m_14177_(this.cameraYRot - player.f_20883_)) <= 51.0F ? this.cameraYRot : player.m_146908_();
         }
      }
   }

   @Internal
   public void onItemUseEvent(Player player, PlayerPatch<?> playerpatch, ItemStack itemstack, InteractionHand hand) {
      if (this.isTPSMode()) {
         EpicFightClientHooks.Camera.ITEM_USED_WHEN_DECOUPLED.post(new ItemUsedInDecoupledCamera(this, player, playerpatch, itemstack, hand));
      }
   }

   private void setFocusingEntity(LivingEntity entity) {
      if (this.predicateFocusableEntity(entity)) {
         this.focusingEntity = entity;
      } else {
         this.focusingEntity = null;
      }
   }

   private boolean predicateFocusableEntity(Entity entity) {
      return entity instanceof LivingEntity livingEntity
         && !entity.m_5833_()
         && entity.m_6087_()
         && entity.m_6084_()
         && !entity.m_7306_(this.minecraft.f_91074_)
         && this.minecraft.f_91074_.m_6779_(livingEntity)
         && entity.m_6095_() != EntityType.f_20494_;
   }

   private CoupleTPSCamera predicateCouplingPlayer() {
      LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(this.minecraft.f_91074_, LocalPlayerPatch.class);
      CoupleTPSCamera coupleTPSCameraEvent = new CoupleTPSCamera(
         this,
         InputManager.getInputState(this.minecraft.f_91074_.f_108618_).getMoveVector().m_165912_() > 0.0F,
         InputManager.isActionActive(MinecraftInputAction.ATTACK_DESTROY),
         this.minecraft.f_91074_.m_6117_() && this.tpsItemAnimations.contains(this.minecraft.f_91074_.m_21211_().m_41780_()),
         this.isZooming(),
         playerpatch == null || playerpatch.isHoldingAny(),
         this.couplingYRot
      );
      EpicFightClientHooks.Camera.COUPLE_CAMERA.post(coupleTPSCameraEvent);
      return coupleTPSCameraEvent;
   }

   private void sendTargeting(@Nullable LivingEntity target) {
      EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(target == null ? -1 : target.m_19879_()));
   }
}
