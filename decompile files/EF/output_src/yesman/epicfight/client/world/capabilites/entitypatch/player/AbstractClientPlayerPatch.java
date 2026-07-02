package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import org.joml.Vector4f;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.physics.PhysicsSimulator;
import yesman.epicfight.api.physics.SimulationTypes;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.online.EpicSkins;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class AbstractClientPlayerPatch<T extends AbstractClientPlayer> extends PlayerPatch<T> implements ClothSimulatable {
   private Item prevHeldItem;
   private Item prevHeldItemOffHand;
   protected EpicSkins epicSkinsInformation;
   private final ClothSimulator clothSimulator = new ClothSimulator();
   public float modelYRotO2;
   public double xPosO2;
   public double yPosO2;
   public double zPosO2;
   public double xCloakO2;
   public double yCloakO2;
   public double zCloakO2;

   public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
      super.onJoinWorld(entity, event);
      this.prevHeldItem = Items.f_41852_;
      this.prevHeldItemOffHand = Items.f_41852_;
      EpicSkins.initEpicSkins(this);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (!this.state.updateLivingMotion() && considerInaction) {
         this.currentLivingMotion = LivingMotions.INACTION;
      } else if (this.original.m_21255_() || this.original.m_21209_()) {
         this.currentLivingMotion = LivingMotions.FLY;
      } else if (this.original.m_20202_() != null) {
         if (this.original.m_20202_() instanceof PlayerRideableJumping) {
            this.currentLivingMotion = LivingMotions.MOUNT;
         } else {
            this.currentLivingMotion = LivingMotions.SIT;
         }
      } else if (this.original.m_6067_()) {
         this.currentLivingMotion = LivingMotions.SWIM;
      } else if (this.original.m_5803_()) {
         this.currentLivingMotion = LivingMotions.SLEEP;
      } else if (!this.original.m_20096_() && this.original.m_6147_()) {
         this.currentLivingMotion = LivingMotions.CLIMB;
      } else if (!this.original.m_150110_().f_35935_) {
         ClientAnimator animator = this.getClientAnimator();
         if (this.original.m_5842_() && this.original.m_20186_() - this.yo < -0.005) {
            this.currentLivingMotion = LivingMotions.FLOAT;
         } else if (this.original.m_20186_() - this.yo < -0.4F || this.isAirborneState()) {
            this.currentLivingMotion = LivingMotions.FALL;
         } else if (this.isMoving()) {
            if (this.original.m_6047_()) {
               this.currentLivingMotion = LivingMotions.SNEAK;
            } else if (this.original.m_20142_()) {
               this.currentLivingMotion = LivingMotions.RUN;
            } else {
               this.currentLivingMotion = LivingMotions.WALK;
            }

            animator.baseLayer.animationPlayer.setReversed(this.dz < 0.0);
         } else {
            animator.baseLayer.animationPlayer.setReversed(false);
            if (this.original.m_6047_()) {
               this.currentLivingMotion = LivingMotions.KNEEL;
            } else {
               this.currentLivingMotion = LivingMotions.IDLE;
            }
         }
      } else if (this.isMoving()) {
         this.currentLivingMotion = LivingMotions.CREATIVE_FLY;
      } else {
         this.currentLivingMotion = LivingMotions.CREATIVE_IDLE;
      }

      UpdatePlayerMotionEvent.BaseLayer baseLayerEvent = new UpdatePlayerMotionEvent.BaseLayer(
         this, this.currentLivingMotion, !this.state.updateLivingMotion() && considerInaction
      );
      this.eventListeners.triggerEvents(PlayerEventListener.EventType.UPDATE_BASE_LIVING_MOTION_EVENT, baseLayerEvent);
      MinecraftForge.EVENT_BUS.post(baseLayerEvent);
      this.currentLivingMotion = baseLayerEvent.getMotion();
      if (!this.state.updateLivingMotion() && considerInaction) {
         this.currentCompositeMotion = LivingMotions.NONE;
      } else {
         CapabilityItem mainhandItemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
         CapabilityItem offhandItemCap = this.getHoldingItemCapability(InteractionHand.OFF_HAND);
         LivingMotion customLivingMotion = mainhandItemCap.getLivingMotion(this, InteractionHand.MAIN_HAND);
         if (customLivingMotion == null) {
            customLivingMotion = offhandItemCap.getLivingMotion(this, InteractionHand.OFF_HAND);
         }

         if (customLivingMotion != null) {
            this.currentCompositeMotion = customLivingMotion;
         } else if (this.original.m_6117_()) {
            UseAnim useAnim = this.original.m_21211_().m_41780_();
            if (useAnim == UseAnim.BLOCK) {
               this.currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
            } else if (useAnim == UseAnim.CROSSBOW) {
               this.currentCompositeMotion = LivingMotions.RELOAD;
            } else if (useAnim == UseAnim.DRINK) {
               this.currentCompositeMotion = LivingMotions.DRINK;
            } else if (useAnim == UseAnim.EAT) {
               this.currentCompositeMotion = LivingMotions.EAT;
            } else if (useAnim == UseAnim.SPYGLASS) {
               this.currentCompositeMotion = LivingMotions.SPECTATE;
            } else {
               this.currentCompositeMotion = this.currentLivingMotion;
            }
         } else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getRealAnimation().get().isReboundAnimation()) {
            this.currentCompositeMotion = LivingMotions.SHOT;
         } else if (this.original.f_20911_ && this.original.m_21257_().isEmpty()) {
            this.currentCompositeMotion = LivingMotions.DIGGING;
         } else {
            this.currentCompositeMotion = this.currentLivingMotion;
         }

         UpdatePlayerMotionEvent.CompositeLayer compositeLayerEvent = new UpdatePlayerMotionEvent.CompositeLayer(this, this.currentCompositeMotion);
         this.eventListeners.triggerEvents(PlayerEventListener.EventType.UPDATE_COMPOSITE_LIVING_MOTION_EVENT, compositeLayerEvent);
         MinecraftForge.EVENT_BUS.post(compositeLayerEvent);
         this.currentCompositeMotion = compositeLayerEvent.getMotion();
      }
   }

   @Override
   public void onOldPosUpdate() {
      this.modelYRotO2 = this.modelYRotO;
      this.xPosO2 = (float)this.original.f_19790_;
      this.yPosO2 = (float)this.original.f_19791_;
      this.zPosO2 = (float)this.original.f_19792_;
   }

   @Override
   protected void clientTick(LivingTickEvent event) {
      this.xCloakO2 = this.original.f_36102_;
      this.yCloakO2 = this.original.f_36103_;
      this.zCloakO2 = this.original.f_36104_;
      super.clientTick(event);
      if (!this.getEntityState().updateLivingMotion()) {
         this.original.f_20883_ = this.original.f_20885_;
      }

      boolean isMainHandChanged = this.prevHeldItem != this.original.m_150109_().m_36056_().m_41720_();
      boolean isOffHandChanged = this.prevHeldItemOffHand != ((ItemStack)this.original.m_150109_().f_35976_.get(0)).m_41720_();
      if (isMainHandChanged || isOffHandChanged) {
         this.updateHeldItem(this.getHoldingItemCapability(InteractionHand.MAIN_HAND), this.getHoldingItemCapability(InteractionHand.OFF_HAND));
         if (isMainHandChanged) {
            this.prevHeldItem = this.original.m_150109_().m_36056_().m_41720_();
         }

         if (isOffHandChanged) {
            this.prevHeldItemOffHand = ((ItemStack)this.original.m_150109_().f_35976_.get(0)).m_41720_();
         }
      }

      if (this.original.f_20919_ == 1) {
         this.getClientAnimator().playDeathAnimation();
      }

      this.clothSimulator.tick(this);
   }

   protected boolean isMoving() {
      return Math.abs(this.dx) > 0.01F || Math.abs(this.dz) > 0.01F;
   }

   public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
      this.cancelItemUse();
      this.getClientAnimator()
         .iterAllLayers(
            layer -> {
               if (!layer.isOff()) {
                  layer.animationPlayer
                     .getRealAnimation()
                     .get()
                     .getProperty(AnimationProperty.StaticAnimationProperty.ON_ITEM_CHANGE_EVENT)
                     .ifPresent(
                        event -> {
                           event.params(mainHandCap, offHandCap);
                           event.execute(
                              this,
                              layer.animationPlayer.getRealAnimation(),
                              layer.animationPlayer.getPrevElapsedTime(),
                              layer.animationPlayer.getElapsedTime()
                           );
                        }
                     );
               }
            }
         );
   }

   @Override
   public void entityPairing(SPEntityPairingPacket packet) {
      super.entityPairing(packet);
      if (packet.getPairingPacketType().is(EntityPairingPacketTypes.class)) {
         switch ((EntityPairingPacketTypes)packet.getPairingPacketType().toEnum(EntityPairingPacketTypes.class)) {
            case TECHNICIAN_ACTIVATED:
               this.original
                  .m_9236_()
                  .m_7106_(
                     (ParticleOptions)EpicFightParticles.WHITE_AFTERIMAGE.get(),
                     this.original.m_20185_(),
                     this.original.m_20186_(),
                     this.original.m_20189_(),
                     Double.longBitsToDouble(this.original.m_19879_()),
                     0.0,
                     0.0
                  );
               break;
            case ADRENALINE_ACTIVATED:
               if (this.original.m_7578_()) {
                  Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_119755_((SoundEvent)EpicFightSounds.ADRENALINE.get(), 1.0F, 1.0F));
               } else {
                  this.original.m_216990_((SoundEvent)EpicFightSounds.ADRENALINE.get());
               }

               this.original
                  .m_9236_()
                  .m_7106_(
                     (ParticleOptions)EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(),
                     this.original.m_20185_(),
                     this.original.m_20186_(),
                     this.original.m_20189_(),
                     Double.longBitsToDouble(this.original.m_19879_()),
                     0.0,
                     0.0
                  );
               break;
            case EMERGENCY_ESCAPE_ACTIVATED:
               float yRot = packet.getBuffer().readFloat();
               this.original
                  .m_9236_()
                  .m_7106_(
                     (ParticleOptions)EpicFightParticles.AIR_BURST.get(),
                     this.original.m_20185_(),
                     this.original.m_20186_() + this.original.m_20206_() * 0.5F,
                     this.original.m_20189_(),
                     90.0,
                     yRot,
                     0.0
                  );
               this.entityDecorations
                  .addColorModifier(EntityDecorations.EMERGENCY_ESCAPE_TRANSPARENCY_MODIFIER, new EntityDecorations.RenderAttributeModifier<Vector4f>() {
                     private int tickCount;

                     public void modifyValue(Vector4f val, float partialTick) {
                        val.w = (float)Math.pow((this.tickCount + partialTick) / 6.0, 2.0) - 0.4F;
                     }

                     @Override
                     public boolean shouldRemove() {
                        return this.tickCount > 6;
                     }

                     @Override
                     public void tick() {
                        this.tickCount++;
                     }
                  });
         }
      }
   }

   @Override
   public boolean overrideRender() {
      RenderEpicFightPlayerEvent renderepicfightplayerevent = new RenderEpicFightPlayerEvent(this, !ClientConfig.enableOriginalModel || this.isEpicFightMode());
      MinecraftForge.EVENT_BUS.post(renderepicfightplayerevent);
      return renderepicfightplayerevent.getShouldRender();
   }

   @Override
   public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
      return false;
   }

   @Override
   public void poseTick(DynamicAnimation animation, Pose pose, float elapsedTime, float partialTick) {
      if (pose.hasTransform("Head") && this.armature.hasJoint("Head") && animation.doesHeadRotFollowEntityHead()) {
         float headRelativeRot = Mth.m_14189_(
            partialTick, Mth.m_14177_(this.modelYRotO - this.original.f_20886_), Mth.m_14177_(this.modelYRot - this.original.f_20885_)
         );
         OpenMatrix4f headTransform = this.armature.getBoundTransformFor(pose, this.armature.searchJointByName("Head"));
         OpenMatrix4f toOriginalRotation = headTransform.removeScale().removeTranslation().invert();
         Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
         Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);
         OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(headRelativeRot, yAxis)
            .rotateDeg(-Mth.m_14189_(partialTick, this.original.f_19860_, this.original.m_146909_()), xAxis);
         pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
      }
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTick) {
      if (this.original.m_21209_()) {
         OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, partialTick, 0.9375F, 0.9375F, 0.9375F);
         float yRot = MathUtils.lerpBetween(this.original.f_19859_, this.original.m_146908_(), partialTick);
         float xRot = MathUtils.lerpBetween(this.original.f_19860_, this.original.m_146909_(), partialTick);
         mat.rotateDeg(-yRot, Vec3f.Y_AXIS)
            .rotateDeg(-xRot, Vec3f.X_AXIS)
            .rotateDeg((this.original.f_19797_ + partialTick) * -55.0F, Vec3f.Z_AXIS)
            .translate(0.0F, -0.39F, 0.0F);
         return mat;
      }

      if (this.original.m_21255_()) {
         OpenMatrix4f mat = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, partialTick, 0.9375F, 0.9375F, 0.9375F);
         float f1 = this.original.m_21256_() + partialTick;
         float f2 = Mth.m_14036_(f1 * f1 / 100.0F, 0.0F, 1.0F);
         mat.rotateDeg(-Mth.m_14189_(partialTick, this.original.f_20884_, this.original.f_20883_), Vec3f.Y_AXIS)
            .rotateDeg(f2 * -this.original.m_146909_(), Vec3f.X_AXIS);
         Vec3 vec3d = this.original.m_20252_(partialTick);
         Vec3 vec3d1 = this.original.m_272267_(partialTick);
         double d0 = vec3d1.m_165925_();
         double d1 = vec3d.m_165925_();
         if (d0 > 0.0 && d1 > 0.0) {
            double d2 = (vec3d1.f_82479_ * vec3d.f_82479_ + vec3d1.f_82481_ * vec3d.f_82481_) / (Math.sqrt(d0) * Math.sqrt(d1));
            double d3 = vec3d1.f_82479_ * vec3d.f_82481_ - vec3d1.f_82481_ * vec3d.f_82479_;
            mat.rotate((float)(-(Math.signum(d3) * Math.acos(d2))), Vec3f.Z_AXIS);
         }

         return mat;
      } else if (this.original.m_5803_()) {
         BlockState blockstate = this.original.m_146900_();
         float yRot = 0.0F;
         if (blockstate.isBed(this.original.m_9236_(), (BlockPos)this.original.m_21257_().orElse(null), (LivingEntity)this.original)
            && blockstate.m_61138_(BlockStateProperties.f_61374_)) {
            switch ((Direction)blockstate.m_61143_(BlockStateProperties.f_61374_)) {
               case EAST:
                  yRot = 90.0F;
                  break;
               case WEST:
                  yRot = -90.0F;
                  break;
               case SOUTH:
                  yRot = 180.0F;
            }
         }

         return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRot, yRot, 0.0F, 0.9375F, 0.9375F, 0.9375F);
      } else {
         float xRotO = 0.0F;
         float xRot = 0.0F;
         float yRotO;
         float yRot;
         if (this.original.m_20202_() instanceof LivingEntity ridingEntity) {
            yRotO = ridingEntity.f_20884_;
            yRot = ridingEntity.f_20883_;
         } else {
            yRotO = this.modelYRotO;
            yRot = this.modelYRot;
         }

         if (!this.getEntityState().inaction() && this.original.m_20089_() == net.minecraft.world.entity.Pose.SWIMMING) {
            float f = this.original.m_20998_(partialTick);
            float f3 = this.original.m_20069_() ? this.original.m_146909_() : 0.0F;
            float f4 = Mth.m_14179_(f, 0.0F, f3);
            xRotO = f4;
            xRot = f4;
         }

         return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, xRotO, xRot, yRotO, yRot, partialTick, 0.9375F, 0.9375F, 0.9375F);
      }
   }

   public void setEpicSkinsInformation(EpicSkins epicSkinsInformation) {
      this.epicSkinsInformation = epicSkinsInformation;
   }

   public EpicSkins getEpicSkinsInformation() {
      return this.epicSkinsInformation;
   }

   public boolean isEpicSkinsLoaded() {
      return this.epicSkinsInformation != null;
   }

   @Override
   public EntitySnapshot<?> captureEntitySnapshot() {
      return EntitySnapshot.capturePlayer(this);
   }

   @Override
   public <SIM extends PhysicsSimulator<?, ?, ?, ?, ?>> Optional<SIM> getSimulator(SimulationTypes<?, ?, ?, ?, ?, SIM> simulationType) {
      return (Optional<SIM>)(simulationType == SimulationTypes.CLOTH ? Optional.of(this.clothSimulator) : Optional.empty());
   }

   @Override
   public ClothSimulator getClothSimulator() {
      return this.clothSimulator;
   }

   @Override
   public Vec3 getAccurateCloakLocation(float partialFrame) {
      if (partialFrame < 0.0F) {
         partialFrame = 1.0F - partialFrame;
         double x = Mth.m_14139_(partialFrame, this.xCloakO2, this.original.f_36102_) - Mth.m_14139_(partialFrame, this.xPosO2, this.original.f_19854_);
         double y = Mth.m_14139_(partialFrame, this.yCloakO2, this.original.f_36103_) - Mth.m_14139_(partialFrame, this.yPosO2, this.original.f_19855_);
         double z = Mth.m_14139_(partialFrame, this.zCloakO2, this.original.f_36104_) - Mth.m_14139_(partialFrame, this.zPosO2, this.original.f_19856_);
         return new Vec3(x, y, z);
      } else {
         double x = Mth.m_14139_(partialFrame, this.original.f_36102_, this.original.f_36105_)
            - Mth.m_14139_(partialFrame, this.original.f_19854_, this.original.m_20185_());
         double y = Mth.m_14139_(partialFrame, this.original.f_36103_, this.original.f_36106_)
            - Mth.m_14139_(partialFrame, this.original.f_19855_, this.original.m_20186_());
         double z = Mth.m_14139_(partialFrame, this.original.f_36104_, this.original.f_36075_)
            - Mth.m_14139_(partialFrame, this.original.f_19856_, this.original.m_20189_());
         return new Vec3(x, y, z);
      }
   }

   @Override
   public Vec3 getAccuratePartialLocation(float partialFrame) {
      if (partialFrame < 0.0F) {
         double x = Mth.m_14139_(++partialFrame, this.xPosO2, this.original.f_19790_);
         double y = Mth.m_14139_(partialFrame, this.yPosO2, this.original.f_19791_);
         double z = Mth.m_14139_(partialFrame, this.zPosO2, this.original.f_19792_);
         return new Vec3(x, y, z);
      } else {
         double x = Mth.m_14139_(partialFrame, this.original.f_19790_, this.original.m_20185_());
         double y = Mth.m_14139_(partialFrame, this.original.f_19791_, this.original.m_20186_());
         double z = Mth.m_14139_(partialFrame, this.original.f_19792_, this.original.m_20189_());
         return new Vec3(x, y, z);
      }
   }

   @Override
   public Vec3 getObjectVelocity() {
      return new Vec3(
         this.original.m_20185_() - this.original.f_19790_,
         this.original.m_20186_() - this.original.f_19791_,
         this.original.m_20189_() - this.original.f_19792_
      );
   }

   @Override
   public float getAccurateYRot(float partialFrame) {
      return partialFrame < 0.0F
         ? Mth.m_14189_(++partialFrame, this.modelYRotO2, this.getYRotO())
         : Mth.m_14189_(partialFrame, this.getYRotO(), this.getYRot());
   }

   @Override
   public float getYRotDelta(float partialFrame) {
      if (partialFrame < 0.0F) {
         partialFrame++;
         return Mth.m_14189_(partialFrame, this.modelYRotO2, this.getYRotO()) - this.modelYRotO2;
      } else {
         return Mth.m_14189_(partialFrame, this.getYRotO(), this.getYRot()) - this.getYRotO();
      }
   }

   @Override
   public boolean invalid() {
      return this.original.m_213877_();
   }

   @Override
   public float getScale() {
      return 0.9375F;
   }

   @Override
   public Animator getSimulatableAnimator() {
      return this.animator;
   }

   @Override
   public float getGravity() {
      return this.getOriginal().m_5842_() ? 0.98F : 9.8F;
   }
}
