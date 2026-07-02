package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector4f;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.model.armature.types.ToolHolderArmature;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.TargetIndicatorCheckEvent;

public abstract class LivingEntityPatch<T extends LivingEntity> extends HurtableEntityPatch<T> {
   protected static EntityDataAccessor<Float> STUN_SHIELD;
   protected static EntityDataAccessor<Float> MAX_STUN_SHIELD;
   protected static EntityDataAccessor<Integer> EXECUTION_RESISTANCE;
   protected static EntityDataAccessor<Boolean> AIRBORNE;
   public static final double WEIGHT_CORRECTION = 37.037;
   protected Armature armature;
   protected Animator animator;
   protected EntityState state = EntityState.DEFAULT_STATE;
   protected Vec3 lastAttackPosition;
   protected EpicFightDamageSource epicFightDamageSource;
   protected boolean isLastAttackSuccess;
   protected float lastDealDamage;
   protected AttackResult.ResultType lastAttackResultType;
   protected Entity lastTryHurtEntity;
   protected LivingEntity grapplingTarget;
   public LivingMotion currentLivingMotion = LivingMotions.IDLE;
   public LivingMotion currentCompositeMotion = LivingMotions.IDLE;
   protected final Map<InteractionHand, Joint> parentJointOfHands = new HashMap<>();
   protected final EntityDecorations entityDecorations = new EntityDecorations();

   public static void initLivingEntityDataAccessor() {
      STUN_SHIELD = SynchedEntityData.m_135353_(LivingEntity.class, EntityDataSerializers.f_135029_);
      MAX_STUN_SHIELD = SynchedEntityData.m_135353_(LivingEntity.class, EntityDataSerializers.f_135029_);
      EXECUTION_RESISTANCE = SynchedEntityData.m_135353_(LivingEntity.class, EntityDataSerializers.f_135028_);
      AIRBORNE = SynchedEntityData.m_135353_(LivingEntity.class, EntityDataSerializers.f_135035_);
   }

   public static void createSyncedEntityData(LivingEntity livingentity) {
      livingentity.m_20088_().m_135372_(STUN_SHIELD, 0.0F);
      livingentity.m_20088_().m_135372_(MAX_STUN_SHIELD, 0.0F);
      livingentity.m_20088_().m_135372_(EXECUTION_RESISTANCE, 0);
      livingentity.m_20088_().m_135372_(AIRBORNE, false);
   }

   public void onConstructed(T entityIn) {
      super.onConstructed(entityIn);
      this.armature = Armatures.getArmatureFor(this);
      Animator animator = EpicFightSharedConstants.getAnimator(this);
      this.animator = animator;
      this.initAnimator(animator);
      animator.postInit();
   }

   protected void initAnimator(Animator animator) {
      animator.getVariables().putDefaultSharedVariable(AttackAnimation.ATTACK_TRIED_ENTITIES);
      animator.getVariables().putDefaultSharedVariable(AttackAnimation.ACTUALLY_HIT_ENTITIES);
      animator.getVariables().putDefaultSharedVariable(ActionAnimation.ACTION_ANIMATION_COORD);
      if (this.armature instanceof ToolHolderArmature toolArmature) {
         this.setParentJointOfHand(InteractionHand.MAIN_HAND, toolArmature.rightToolJoint());
         this.setParentJointOfHand(InteractionHand.OFF_HAND, toolArmature.leftToolJoint());
      }
   }

   public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
      super.onJoinWorld(entity, event);
      if (entity.m_21172_((Attribute)EpicFightAttributes.WEIGHT.get()) == 0.0) {
         EntityDimensions entityDimensions = entity.m_6972_(Pose.STANDING);
         double weight = entityDimensions.f_20377_ * entityDimensions.f_20378_ * 37.037;
         entity.m_21051_((Attribute)EpicFightAttributes.WEIGHT.get()).m_22100_(weight);
      }
   }

   public abstract void updateMotion(boolean var1);

   public Armature getArmature() {
      return this.armature;
   }

   public void initAttributesFromCompound(CompoundTag compoundTag) {
      if (compoundTag.m_128425_("max_stun_shield", 5)) {
         this.setMaxStunShield(compoundTag.m_128457_("max_stun_shield"));
      }

      if (compoundTag.m_128425_("stun_shield", 5)) {
         this.setStunShield(compoundTag.m_128457_("stun_shield"));
      }
   }

   public void saveData(CompoundTag compoundTag) {
      compoundTag.m_128350_("max_stun_shield", this.getMaxStunShield());
      compoundTag.m_128350_("stun_shield", this.getStunShield());
   }

   @Override
   public void tick(LivingTickEvent event) {
      super.tick(event);
      if (this.original.m_21223_() <= 0.0F) {
         this.original.m_146926_(0.0F);
         AnimationPlayer animPlayer = this.<Animator>getAnimator().getPlayerFor(null);
         if (this.original.f_20919_ >= 19 && !animPlayer.isEmpty() && !animPlayer.isEnd()) {
            this.original.f_20919_--;
         }
      }

      this.animator.tick();
      if (this.isLogicalClient()) {
         this.clientTick(event);
      } else {
         this.serverTick(event);
      }

      if (this.original.f_20919_ == 19) {
         this.aboutToDeath();
      }

      if (!this.getEntityState().inaction() && this.original.f_19861_ && this.isAirborneState()) {
         this.setAirborneState(false);
      }
   }

   protected void clientTick(LivingTickEvent event) {
      this.entityDecorations.tick();
   }

   protected void serverTick(LivingTickEvent event) {
   }

   public void poseTick(DynamicAnimation animation, yesman.epicfight.api.animation.Pose pose, float elapsedTime, float partialTick) {
      if (pose.hasTransform("Head") && this.armature.hasJoint("Head") && animation.doesHeadRotFollowEntityHead()) {
         float headRelativeRot = Mth.m_14189_(
            partialTick, Mth.m_14177_(this.original.f_20884_ - this.original.f_20886_), Mth.m_14177_(this.original.f_20883_ - this.original.f_20885_)
         );
         OpenMatrix4f toOriginalRotation = new OpenMatrix4f(this.armature.getBoundTransformFor(pose, this.armature.searchJointByName("Head")))
            .removeScale()
            .removeTranslation()
            .invert();
         Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
         Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);
         OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(headRelativeRot, yAxis)
            .rotateDeg(-Mth.m_14189_(partialTick, this.original.f_19860_, this.original.m_146909_()), xAxis);
         pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
      }
   }

   public void onFall(LivingFallEvent event) {
      if (!this.getOriginal().m_9236_().m_5776_() && this.isAirborneState()) {
         AssetAccessor<? extends StaticAnimation> fallAnimation = this.<Animator>getAnimator()
            .getLivingAnimation(LivingMotions.LANDING_RECOVERY, this.getHitAnimation(StunType.FALL));
         if (fallAnimation != null) {
            this.playAnimationSynchronized(fallAnimation, 0.0F);
         }
      }

      this.setAirborneState(false);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void entityPairing(SPEntityPairingPacket packet) {
      super.entityPairing(packet);
      if (packet.getPairingPacketType().is(EntityPairingPacketTypes.class)) {
         switch ((EntityPairingPacketTypes)packet.getPairingPacketType().toEnum(EntityPairingPacketTypes.class)) {
            case BONEBREAKER_BEGIN:
               this.entityDecorations.addDecorationOverlay(EntityDecorations.BONEBREAKER_OVERLAY, new EntityDecorations.DecorationOverlay() {
                  static final ResourceLocation TEXTURE = EpicFightMod.identifier("textures/entity/overlay/crack_level1.png");

                  @Override
                  public RenderType getRenderType() {
                     return EpicFightRenderTypes.overlayModel(TEXTURE);
                  }
               });
               break;
            case BONEBREAKER_MAX_STACK:
               this.original
                  .m_9236_()
                  .m_245747_(this.getOriginal().m_20183_(), (SoundEvent)EpicFightSounds.OLD_FALL.get(), SoundSource.MASTER, 50.0F, 1.0F, false);
               this.entityDecorations.addDecorationOverlay(EntityDecorations.BONEBREAKER_OVERLAY, new EntityDecorations.DecorationOverlay() {
                  static final ResourceLocation TEXTURE = EpicFightMod.identifier("textures/entity/overlay/crack_level2.png");

                  @Override
                  public RenderType getRenderType() {
                     return EpicFightRenderTypes.overlayModel(TEXTURE);
                  }
               });
               break;
            case BONEBREAKER_CLEAR:
               this.entityDecorations.removeDecorationOverlay(EntityDecorations.BONEBREAKER_OVERLAY);
               break;
            case STAMINA_PILLAGER_BODY_ASHES:
               this.entityDecorations
                  .addColorModifier(EntityDecorations.STAMINA_PILLAGER_ASHES_COLOR, new EntityDecorations.RenderAttributeModifier<Vector4f>() {
                     public void modifyValue(Vector4f value, float partialTick) {
                        float rotProgression = Mth.m_14036_(1.0F - (LivingEntityPatch.this.original.f_20919_ + partialTick) / 16.0F, 0.0F, 1.0F);
                        float color = Mth.m_144920_(0.28F, 1.0F, rotProgression * rotProgression);
                        value.x = color;
                        value.y = color;
                        value.z = color;
                     }
                  });
               this.entityDecorations
                  .addOverlayCoordModifier(EntityDecorations.STAMINA_PILLAGER_ASHES_OVERLAY, new EntityDecorations.RenderAttributeModifier<Vec2i>() {
                     public void modifyValue(Vec2i value, float partialTick) {
                        value.x = 0;
                        value.y = 10;
                     }
                  });
               this.entityDecorations
                  .addParticleGenerator(
                     EntityDecorations.STAMINA_PILLAGER_ASHES_PARTICLE,
                     new EntityDecorations.ParticleGenerator() {
                        @Override
                        public void generateParticles() {
                           OpenMatrix4f boundRootTransform = LivingEntityPatch.this.armature
                              .getBoundTransformFor(LivingEntityPatch.this.animator.getPose(1.0F), LivingEntityPatch.this.armature.rootJoint);
                           Vec3f boundRootPos = boundRootTransform.toTranslationVector()
                              .add(
                                 (float)((LivingEntity)LivingEntityPatch.this.getOriginal()).m_20185_(),
                                 (float)((LivingEntity)LivingEntityPatch.this.getOriginal()).m_20186_(),
                                 (float)((LivingEntity)LivingEntityPatch.this.getOriginal()).m_20189_()
                              );
                           RandomSource random = LivingEntityPatch.this.original.m_217043_();
                           Vec3 lookVec = LivingEntityPatch.this.original.m_20154_().m_82490_(0.1);

                           for (int i = 0; i < 3; i++) {
                              LivingEntityPatch.this.original
                                 .m_9236_()
                                 .m_7106_(
                                    (ParticleOptions)EpicFightParticles.ASH_DIRECTIONAL.get(),
                                    boundRootPos.x + random.m_188583_() * 0.4F,
                                    boundRootPos.y + random.m_188583_() * 0.6F,
                                    boundRootPos.z + random.m_188583_() * 0.4F,
                                    lookVec.f_82479_,
                                    0.1F,
                                    lookVec.f_82481_
                                 );
                           }
                        }
                     }
                  );
               break;
            case FLASH_WHITE:
               final int durationTick = packet.getBuffer().readInt();
               final int maxOverlay = packet.getBuffer().readInt();
               final int maxBrightness = packet.getBuffer().readInt();
               final boolean disableRed = packet.getBuffer().readBoolean();
               this.entityDecorations.addOverlayCoordModifier(EntityDecorations.FLASH_WHITE_OVERLAY, new EntityDecorations.RenderAttributeModifier<Vec2i>() {
                  private int tickCount;

                  public void modifyValue(Vec2i value, float partialTick) {
                     float f = Mth.m_14031_((this.tickCount + partialTick) / (durationTick + 1.0F) * (float) Math.PI) * maxOverlay;
                     value.x = (int)f;
                     if (disableRed) {
                        value.y = 10;
                     }
                  }

                  @Override
                  public void tick() {
                     this.tickCount++;
                  }

                  @Override
                  public boolean shouldRemove() {
                     return this.tickCount > durationTick;
                  }
               });
               this.entityDecorations.addLightModifier(EntityDecorations.FLASH_WHITE_LIGHT, new EntityDecorations.RenderAttributeModifier<Vec2i>() {
                  private int tickCount;

                  public void modifyValue(Vec2i value, float partialTick) {
                     float f = Mth.m_14031_((this.tickCount + partialTick) / (durationTick + 1.0F) * (float) Math.PI) * maxBrightness;
                     value.x += (int)f;
                  }

                  @Override
                  public void tick() {
                     this.tickCount++;
                  }

                  @Override
                  public boolean shouldRemove() {
                     return this.tickCount > durationTick;
                  }
               });
               break;
            case VENGEANCE_OVERLAY:
               this.entityDecorations.addColorModifier(EntityDecorations.VENGEANCE_OVERLAY, new EntityDecorations.RenderAttributeModifier<Vector4f>() {
                  public void modifyValue(Vector4f value, float partialTick) {
                     value.x = 1.0F;
                     value.y = 0.5F;
                     value.z = 0.5F;
                  }
               });
               break;
            case VENGEANCE_TARGET_CANCEL:
               this.entityDecorations.removeColorModifier(EntityDecorations.VENGEANCE_OVERLAY);
         }
      }
   }

   @Override
   public void onDeath(LivingDeathEvent event) {
      this.<Animator>getAnimator().playDeathAnimation();
      this.currentLivingMotion = LivingMotions.DEATH;
   }

   public void updateEntityState() {
      this.state = this.animator.getEntityState();
   }

   public void updateEntityState(EntityState entityState) {
      this.state = entityState;
   }

   public void cancelItemUse() {
      if (this.original.m_6117_()) {
         this.original.m_5810_();
         ForgeEventFactory.onUseItemStop(this.original, this.original.m_21211_(), this.original.m_21212_());
      }
   }

   public CapabilityItem getHoldingItemCapability(InteractionHand hand) {
      return EpicFightCapabilities.getItemStackCapability(this.original.m_21120_(hand));
   }

   public CapabilityItem getAdvancedHoldingItemCapability(InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND) {
         return this.getHoldingItemCapability(hand);
      } else {
         return this.isOffhandItemValid() ? this.getHoldingItemCapability(hand) : CapabilityItem.EMPTY;
      }
   }

   public ItemStack getAdvancedHoldingItemStack(InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND) {
         return this.original.m_21120_(hand);
      } else {
         return this.isOffhandItemValid() ? this.original.m_21120_(hand) : ItemStack.f_41583_;
      }
   }

   public EpicFightDamageSource getDamageSource(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, InteractionHand hand) {
      return EpicFightDamageSources.mobAttack(this.original)
         .setAnimation(animation)
         .setBaseArmorNegation(this.getArmorNegation(hand))
         .setBaseImpact(this.getImpact(hand))
         .setUsedItem(this.original.m_21120_(hand));
   }

   public AttackResult tryHurt(DamageSource damageSource, float amount) {
      return AttackResult.of(this.getEntityState().attackResult(damageSource), amount);
   }

   public AttackResult tryHarm(Entity target, EpicFightDamageSource damagesource, float amount) {
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
      return entitypatch != null ? entitypatch.tryHurt(damagesource, amount) : AttackResult.success(amount);
   }

   @Nullable
   @Internal
   public EpicFightDamageSource getEpicFightDamageSource() {
      return this.epicFightDamageSource;
   }

   protected void setOffhandDamage(
      InteractionHand hand,
      ItemStack mainhandItemStack,
      ItemStack offhandItemStack,
      boolean offhandValid,
      Collection<AttributeModifier> mainhandAttributes,
      Collection<AttributeModifier> offhandAttributes
   ) {
      if (hand != InteractionHand.MAIN_HAND) {
         this.getOriginal().m_21008_(InteractionHand.MAIN_HAND, offhandValid ? offhandItemStack : ItemStack.f_41583_);
         this.getOriginal().m_21008_(InteractionHand.OFF_HAND, mainhandItemStack);
         AttributeInstance damageAttributeInstance = this.original.m_21051_(Attributes.f_22281_);
         mainhandAttributes.forEach(damageAttributeInstance::m_22130_);
         offhandAttributes.forEach(damageAttributeInstance::m_22118_);
      }
   }

   protected void recoverMainhandDamage(
      InteractionHand hand,
      ItemStack mainhandItemStack,
      ItemStack offhandItemStack,
      Collection<AttributeModifier> mainhandAttributes,
      Collection<AttributeModifier> offhandAttributes
   ) {
      if (hand != InteractionHand.MAIN_HAND) {
         this.getOriginal().m_21008_(InteractionHand.MAIN_HAND, mainhandItemStack);
         this.getOriginal().m_21008_(InteractionHand.OFF_HAND, offhandItemStack);
         AttributeInstance damageAttributeInstance = this.original.m_21051_(Attributes.f_22281_);
         offhandAttributes.forEach(damageAttributeInstance::m_22130_);
         mainhandAttributes.forEach(damageAttributeInstance::m_22118_);
      }
   }

   public void setLastAttackResult(AttackResult attackResult) {
      this.lastAttackResultType = attackResult.resultType;
      this.lastDealDamage = attackResult.damage;
   }

   public void setLastAttackEntity(Entity tryHurtEntity) {
      this.lastTryHurtEntity = tryHurtEntity;
   }

   protected boolean checkLastAttackSuccess(Entity target) {
      boolean success = target.m_7306_(this.lastTryHurtEntity);
      this.lastTryHurtEntity = null;
      if (success && !this.isLastAttackSuccess) {
         this.setLastAttackSuccess(true);
      }

      return success;
   }

   public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
      return this.checkLastAttackSuccess(target) ? new AttackResult(this.lastAttackResultType, this.lastDealDamage) : AttackResult.missed(0.0F);
   }

   public float getModifiedBaseDamage(float baseDamage) {
      return baseDamage;
   }

   public boolean onDrop(LivingDropsEvent event) {
      return false;
   }

   @Override
   public final float getStunShield() {
      return (Float)this.original.m_20088_().m_135370_(STUN_SHIELD);
   }

   @Override
   public final void setStunShield(float value) {
      value = Mth.m_14036_(value, 0.0F, this.getMaxStunShield());
      this.original.m_20088_().m_135381_(STUN_SHIELD, value);
   }

   public float getMaxStunShield() {
      return (Float)this.original.m_20088_().m_135370_(MAX_STUN_SHIELD);
   }

   public void setMaxStunShield(float value) {
      value = Math.max(value, 0.0F);
      this.original.m_20088_().m_135381_(MAX_STUN_SHIELD, value);
   }

   public int getExecutionResistance() {
      return (Integer)this.original.m_20088_().m_135370_(EXECUTION_RESISTANCE);
   }

   public void setExecutionResistance(int value) {
      int maxExecutionResistance = (int)this.original.m_21133_((Attribute)EpicFightAttributes.EXECUTION_RESISTANCE.get());
      value = Math.min(maxExecutionResistance, value);
      this.original.m_20088_().m_135381_(EXECUTION_RESISTANCE, value);
   }

   @Override
   public float getWeight() {
      return (float)this.original.m_21133_((Attribute)EpicFightAttributes.WEIGHT.get());
   }

   public void rotateTo(float degree, float limit, boolean syncPrevRot) {
      LivingEntity entity = this.getOriginal();
      float yRot = Mth.m_14177_(entity.m_146908_());
      float amount = Mth.m_14036_(Mth.m_14177_(degree - yRot), -limit, limit);
      float f1 = yRot + amount;
      if (syncPrevRot) {
         entity.f_19859_ = f1;
         entity.f_20886_ = f1;
         entity.f_20884_ = f1;
      }

      entity.m_146922_(f1);
      entity.f_20885_ = f1;
      entity.f_20883_ = f1;
   }

   public void rotateTo(Entity target, float limit, boolean syncPrevRot) {
      Vec3 playerPosition = this.original.m_20182_();
      Vec3 targetPosition = target.m_20182_();
      float yaw = (float)MathUtils.getYRotOfVector(targetPosition.m_82546_(playerPosition));
      this.rotateTo(yaw, limit, syncPrevRot);
   }

   public float getYRotDeltaTo(Entity target) {
      Vec3 playerPosition = this.getOriginal().m_20182_();
      Vec3 targetPosition = target.m_20182_();
      float yRotToTarget = (float)MathUtils.getYRotOfVector(targetPosition.m_82546_(playerPosition));
      float yRotCurrent = Mth.m_14177_(this.getOriginal().m_146908_());
      return Mth.m_14036_(Mth.m_14177_(yRotToTarget - yRotCurrent), -this.getYRotLimit(), this.getYRotLimit());
   }

   public LivingEntity getTarget() {
      return this.original.m_21214_();
   }

   public float getAttackDirectionPitch() {
      float partialTicks = EpicFightSharedConstants.isPhysicalClient() ? Minecraft.m_91087_().m_91296_() : 1.0F;
      float pitch = -this.getOriginal().m_5686_(partialTicks);
      float correct = pitch > 0.0F ? 0.03333F * (float)Math.pow(pitch, 2.0) : -0.03333F * (float)Math.pow(pitch, 2.0);
      return Mth.m_14036_(correct, -30.0F, 30.0F);
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      float scale = this.original.m_6162_() ? 0.5F : 1.0F;
      float yRotO;
      float yRot;
      if (this.original.m_20202_() instanceof LivingEntity ridingEntity) {
         yRotO = ridingEntity.f_20884_;
         yRot = ridingEntity.f_20883_;
      } else {
         yRotO = this.isLogicalClient() ? this.original.f_20884_ : this.original.m_146908_();
         yRot = this.isLogicalClient() ? this.original.f_20883_ : this.original.m_146908_();
      }

      return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRotO, yRot, partialTicks, scale, scale, scale);
   }

   public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation) {
      if (this.isLogicalClient()) {
         this.animator.reserveAnimation(animation);
      } else {
         this.handleAnimationPacket(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, SPAnimatorControl::new);
      }
   }

   public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation, LivingEntityPatch.ServerAnimationPacketProvider packetProvider) {
      this.handleAnimationPacket(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, packetProvider);
   }

   public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation) {
      if (this.isLogicalClient()) {
         this.animator.playAnimationInstantly(animation);
      } else {
         this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, SPAnimatorControl::new);
      }
   }

   public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation, LivingEntityPatch.ServerAnimationPacketProvider packetProvider) {
      this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, packetProvider);
   }

   public void playAnimation(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
      this.animator.playAnimation(animation, transitionTimeModifier);
   }

   public void stopPlaying(AssetAccessor<? extends StaticAnimation> animation) {
      if (this.isLogicalClient()) {
         this.animator.stopPlaying(animation);
      } else {
         this.handleAnimationPacket(AnimatorControlPacket.Action.STOP, animation, -1.0F, SPAnimatorControl::new);
      }
   }

   public void playAnimation(
      AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch.ServerAnimationPacketProvider packetProvider
   ) {
      this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, packetProvider);
   }

   public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
      if (!this.isLogicalClient()) {
         this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, SPAnimatorControl::new);
      }
   }

   public void playAnimationSynchronized(
      AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch.ServerAnimationPacketProvider packetProvider
   ) {
      this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, packetProvider);
   }

   public void playAnimationInClientSide(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
      if (this.isLogicalClient()) {
         this.animator.playAnimation(animation, transitionTimeModifier);
      } else {
         this.sendToAllPlayersTrackingMe(
            new SPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, this.original.m_19879_(), transitionTimeModifier, false)
         );
      }
   }

   public void playShootingAnimation() {
      if (this.isLogicalClient()) {
         this.animator.playShootingAnimation();
      } else {
         this.sendToAllPlayersTrackingMe(new SPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, this.getOriginal().m_19879_(), 0.0F, false));
      }
   }

   private void handleAnimationPacket(
      AnimatorControlPacket.Action action,
      AssetAccessor<? extends StaticAnimation> animation,
      float transitionTimeModifier,
      LivingEntityPatch.ServerAnimationPacketProvider packetProvider
   ) {
      if (this.isLogicalClient()) {
         throw new IllegalStateException("Cannot send animation play packet in client side.");
      }

      switch (action) {
         case PLAY:
            this.animator.playAnimation(animation, transitionTimeModifier);
            break;
         case PLAY_INSTANTLY:
            this.animator.playAnimationInstantly(animation);
            break;
         case STOP:
            this.animator.stopPlaying(animation);
            break;
         case RESERVE:
            this.animator.reserveAnimation(animation);
            break;
         case SHOT:
            this.animator.playShootingAnimation();
            break;
         default:
            throw new UnsupportedOperationException("Only PLAY, PLAY_INSTANTLY, STOP and RESERVE are allowed");
      }

      this.sendToAllPlayersTrackingMe(packetProvider.get(action, animation, transitionTimeModifier, this));
   }

   public void pauseAnimator(AnimatorControlPacket.Action action, boolean pause) {
      switch (action) {
         case SOFT_PAUSE:
            this.animator.setSoftPause(pause);
            break;
         case HARD_PAUSE:
            this.animator.setHardPause(pause);
            break;
         default:
            throw new UnsupportedOperationException("Only SOFT_PAUSE and HARD_PAUSE are allowed");
      }

      if (!this.isLogicalClient()) {
         this.sendToAllPlayersTrackingMe(new SPAnimatorControl(action, -1, this.original.m_19879_(), 0.0F, pause));
      }
   }

   public void sendToAllPlayersTrackingMe(Object packet) {
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(packet, this.original);
   }

   public void resetSize(EntityDimensions size) {
      EntityDimensions entitysize = this.original.f_19815_;
      EntityDimensions entitysize1 = size;
      this.original.f_19815_ = entitysize1;
      if (entitysize1.f_20377_ < entitysize.f_20377_) {
         double d0 = entitysize1.f_20377_ / 2.0;
         this.original
            .m_20011_(
               new AABB(
                  this.original.m_20185_() - d0,
                  this.original.m_20186_(),
                  this.original.m_20189_() - d0,
                  this.original.m_20185_() + d0,
                  this.original.m_20186_() + entitysize1.f_20378_,
                  this.original.m_20189_() + d0
               )
            );
      } else {
         AABB axisalignedbb = this.original.m_20191_();
         this.original
            .m_20011_(
               new AABB(
                  axisalignedbb.f_82288_,
                  axisalignedbb.f_82289_,
                  axisalignedbb.f_82290_,
                  axisalignedbb.f_82288_ + entitysize1.f_20377_,
                  axisalignedbb.f_82289_ + entitysize1.f_20378_,
                  axisalignedbb.f_82290_ + entitysize1.f_20377_
               )
            );
         if (entitysize1.f_20377_ > entitysize.f_20377_ && !this.original.m_9236_().m_5776_()) {
            float f = entitysize.f_20377_ - entitysize1.f_20377_;
            this.original.m_6478_(MoverType.SELF, new Vec3(f, 0.0, f));
         }
      }
   }

   @Override
   public boolean applyStun(StunType stunType, float stunTime) {
      this.original.f_20900_ = 0.0F;
      this.original.f_20901_ = 0.0F;
      this.original.f_20902_ = 0.0F;
      this.original.m_20334_(0.0, 0.0, 0.0);
      this.cancelKnockback = true;
      AssetAccessor<? extends StaticAnimation> hitAnimation = this.getHitAnimation(stunType);
      if (hitAnimation != null) {
         this.playAnimationSynchronized(hitAnimation, stunType.hasFixedStunTime() ? 0.0F : stunTime);
         return true;
      } else {
         return false;
      }
   }

   public void beginAction(ActionAnimation animation) {
   }

   public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
   }

   public void updateArmor(CapabilityItem fromCap, CapabilityItem toCap, EquipmentSlot slotType) {
      if (this.original.m_21204_().m_22171_((Attribute)EpicFightAttributes.STUN_ARMOR.get())) {
         if (fromCap != null) {
            this.original.m_21204_().m_22161_(fromCap.getAttributeModifiers(slotType, this));
         }

         if (toCap != null) {
            this.original.m_21204_().m_22178_(toCap.getAttributeModifiers(slotType, this));
         }
      }
   }

   public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> blocker) {
   }

   public void onStrike(AttackAnimation animation, InteractionHand hand) {
      this.getAdvancedHoldingItemCapability(hand).onStrike(this, animation);
   }

   public void onMount(boolean isMountOrDismount, Entity ridingEntity) {
   }

   public void notifyGrapplingWarning() {
   }

   public void onDodgeSuccess(DamageSource damageSource, Vec3 location) {
   }

   public void countHurtTime(float damageTaken) {
      this.original.f_20898_ = damageTaken;
      this.original.f_19802_ = 20;
      this.original.f_20917_ = 10;
      this.original.f_20916_ = this.original.f_20917_;
   }

   @Override
   public boolean isStunned() {
      return this.getEntityState().hurt();
   }

   public <A extends Animator> A getAnimator() {
      return (A)this.animator;
   }

   @OnlyIn(Dist.CLIENT)
   public ClientAnimator getClientAnimator() {
      return this.getAnimator();
   }

   public ServerAnimator getServerAnimator() {
      return this.getAnimator();
   }

   public abstract AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType var1);

   public void aboutToDeath() {
   }

   public SoundEvent getWeaponHitSound(InteractionHand hand) {
      return this.getAdvancedHoldingItemCapability(hand).getHitSound();
   }

   public SoundEvent getSwingSound(InteractionHand hand) {
      CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
      return this.entityDecorations.getModifiedSwingSound(itemCap.getSmashingSound(), itemCap);
   }

   public HitParticleType getWeaponHitParticle(InteractionHand hand) {
      return this.getAdvancedHoldingItemCapability(hand).getHitParticle();
   }

   public Collider getColliderMatching(InteractionHand hand) {
      return this.getAdvancedHoldingItemCapability(hand).getWeaponCollider();
   }

   public int getMaxStrikes(InteractionHand hand) {
      return (int)(
         hand == InteractionHand.MAIN_HAND
            ? this.original.m_21133_((Attribute)EpicFightAttributes.MAX_STRIKES.get())
            : (
               this.isOffhandItemValid()
                  ? this.original.m_21133_((Attribute)EpicFightAttributes.OFFHAND_MAX_STRIKES.get())
                  : this.original.m_21051_((Attribute)EpicFightAttributes.MAX_STRIKES.get()).m_22115_()
            )
      );
   }

   public float getArmorNegation(InteractionHand hand) {
      return (float)(
         hand == InteractionHand.MAIN_HAND
            ? this.original.m_21133_((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
            : (
               this.isOffhandItemValid()
                  ? this.original.m_21133_((Attribute)EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())
                  : this.original.m_21051_((Attribute)EpicFightAttributes.ARMOR_NEGATION.get()).m_22115_()
            )
      );
   }

   public float getImpact(InteractionHand hand) {
      int i = 0;
      float impact;
      if (hand == InteractionHand.MAIN_HAND) {
         impact = (float)this.original.m_21133_((Attribute)EpicFightAttributes.IMPACT.get());
         i = this.getOriginal().m_21205_().getEnchantmentLevel(Enchantments.f_44980_);
      } else if (this.isOffhandItemValid()) {
         impact = (float)this.original.m_21133_((Attribute)EpicFightAttributes.OFFHAND_IMPACT.get());
         i = this.getOriginal().m_21206_().getEnchantmentLevel(Enchantments.f_44980_);
      } else {
         impact = (float)this.original.m_21051_((Attribute)EpicFightAttributes.IMPACT.get()).m_22115_();
      }

      return impact * (1.0F + i * 0.12F);
   }

   public float getReach(InteractionHand hand) {
      return this.getAdvancedHoldingItemCapability(hand).getReach();
   }

   public ItemStack getValidItemInHand(InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND) {
         return this.original.m_21120_(hand);
      } else {
         return this.isOffhandItemValid() ? this.original.m_21120_(hand) : ItemStack.f_41583_;
      }
   }

   public boolean isOffhandItemValid() {
      return this.getHoldingItemCapability(InteractionHand.MAIN_HAND).checkOffhandValid(this);
   }

   public Joint getParentJointOfHand(InteractionHand hand) {
      return this.parentJointOfHands.getOrDefault(hand, this.armature.rootJoint);
   }

   public void setParentJointOfHand(InteractionHand hand, Joint joint) {
      this.parentJointOfHands.put(hand, joint);
   }

   public boolean isTargetInvulnerable(Entity target) {
      if (target.m_6087_() && !target.m_5833_()) {
         return this.original.m_20201_() == target.m_20201_() && !target.canRiderInteract()
            ? true
            : this.original.m_7307_(target) && this.original.m_5647_() != null && !this.original.m_5647_().m_6260_();
      } else {
         return true;
      }
   }

   public boolean canPush(Entity entity) {
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
      if (entitypatch != null) {
         EntityState state = entitypatch.getEntityState();
         if (state.inaction()) {
            return false;
         }
      }

      EntityState thisState = this.getEntityState();
      return !thisState.inaction() && !entity.m_7306_(this.grapplingTarget);
   }

   public LivingEntity getGrapplingTarget() {
      return this.grapplingTarget;
   }

   public void setGrapplingTarget(LivingEntity grapplingTarget) {
      this.grapplingTarget = grapplingTarget;
   }

   public Vec3 getLastAttackPosition() {
      return this.lastAttackPosition;
   }

   public void setLastAttackPosition() {
      this.lastAttackPosition = this.original.m_20182_();
   }

   public void setAirborneState(boolean airborne) {
      this.original.m_20088_().m_135381_(AIRBORNE, airborne);
   }

   public boolean isAirborneState() {
      return (Boolean)this.original.m_20088_().m_135370_(AIRBORNE);
   }

   public void setLastAttackSuccess(boolean setter) {
      this.isLastAttackSuccess = setter;
   }

   public boolean isLastAttackSuccess() {
      return this.isLastAttackSuccess;
   }

   public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
      return !this.isLogicalClient();
   }

   public boolean isFirstPerson() {
      return false;
   }

   @Override
   public boolean overrideRender() {
      return true;
   }

   public boolean shouldBlockMoving() {
      return false;
   }

   public float getYRotLimit() {
      return 20.0F;
   }

   public double getXOld() {
      return this.original.f_19790_;
   }

   public double getYOld() {
      return this.original.f_19791_;
   }

   public double getZOld() {
      return this.original.f_19792_;
   }

   public float getYRot() {
      return this.original.m_146908_();
   }

   public float getYRotO() {
      return this.original.f_19859_;
   }

   public void setYRot(float yRot) {
      this.original.m_146922_(yRot);
      if (this.isLogicalClient()) {
         this.original.f_20883_ = yRot;
         this.original.f_20885_ = yRot;
      }
   }

   public void setYRotO(float yRot) {
      this.original.f_19859_ = yRot;
      if (this.isLogicalClient()) {
         this.original.f_20884_ = yRot;
         this.original.f_20886_ = yRot;
      }
   }

   @Override
   public EntityState getEntityState() {
      return this.state;
   }

   public InteractionHand getAttackingHand() {
      Pair<AnimationPlayer, AttackAnimation> layerInfo = this.<Animator>getAnimator().findFor(AttackAnimation.class);
      return layerInfo != null ? ((AttackAnimation)layerInfo.getSecond()).getPhaseByTime(((AnimationPlayer)layerInfo.getFirst()).getElapsedTime()).hand : null;
   }

   public LivingMotion getCurrentLivingMotion() {
      return this.currentLivingMotion;
   }

   public List<Entity> getCurrentlyAttackTriedEntities() {
      return this.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.ATTACK_TRIED_ENTITIES);
   }

   public List<LivingEntity> getCurrentlyActuallyHitEntities() {
      return this.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.ACTUALLY_HIT_ENTITIES);
   }

   public void removeHurtEntities() {
      this.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.ATTACK_TRIED_ENTITIES).clear();
      this.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.ACTUALLY_HIT_ENTITIES).clear();
   }

   public abstract Faction getFaction();

   public EntityDecorations getEntityDecorations() {
      return this.entityDecorations;
   }

   @OnlyIn(Dist.CLIENT)
   public EntitySnapshot<?> captureEntitySnapshot() {
      return EntitySnapshot.captureLivingEntity(this);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean flashTargetIndicator(LocalPlayerPatch playerpatch) {
      TargetIndicatorCheckEvent event = new TargetIndicatorCheckEvent(playerpatch, this);
      playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, event);
      return event.isCanceled();
   }

   @FunctionalInterface
   public interface ServerAnimationPacketProvider {
      SPAnimatorControl get(AnimatorControlPacket.Action var1, AssetAccessor<? extends StaticAnimation> var2, float var3, LivingEntityPatch<?> var4);
   }
}
