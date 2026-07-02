package yesman.epicfight.api.animation.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.entity.eventlistener.AttackEndEvent;
import yesman.epicfight.world.entity.eventlistener.AttackPhaseEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class AttackAnimation extends ActionAnimation {
   public static final AnimationVariables.SharedAnimationVariableKey<List<Entity>> ATTACK_TRIED_ENTITIES = AnimationVariables.shared(
      animator -> Lists.newArrayList(), false
   );
   public static final AnimationVariables.SharedAnimationVariableKey<List<LivingEntity>> ACTUALLY_HIT_ENTITIES = AnimationVariables.shared(
      animator -> Lists.newArrayList(), false
   );
   public final AttackAnimation.Phase[] phases;

   public AttackAnimation(
      float transitionTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, accessor, armature, new AttackAnimation.Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, colliderJoint, collider));
   }

   public AttackAnimation(
      float transitionTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      InteractionHand hand,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      this(
         transitionTime,
         accessor,
         armature,
         new AttackAnimation.Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, hand, colliderJoint, collider)
      );
   }

   public AttackAnimation(
      float transitionTime,
      AnimationManager.AnimationAccessor<? extends AttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature,
      AttackAnimation.Phase... phases
   ) {
      super(transitionTime, accessor, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_TARGET_DISTANCE);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_DISTANCE);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.MODEL_COORD);
      this.addProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.ATTACK_TARGET_LOCATION);
      this.addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.MOB_ATTACK_TARGET_LOOK);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
      this.phases = phases;
      this.stateSpectrumBlueprint.clear();

      for (AttackAnimation.Phase phase : phases) {
         if (!phase.noStateBind) {
            this.bindPhaseState(phase);
         }
      }
   }

   public AttackAnimation(
      float convertTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      InteractionHand hand,
      @Nullable Collider collider,
      Joint colliderJoint,
      String path,
      AssetAccessor<? extends Armature> armature
   ) {
      this(convertTime, path, armature, new AttackAnimation.Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, hand, colliderJoint, collider));
   }

   public AttackAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
      super(convertTime, 0.0F, path, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_TARGET_DISTANCE);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_DISTANCE);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.MODEL_COORD);
      this.addProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.ATTACK_TARGET_LOCATION);
      this.addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.MOB_ATTACK_TARGET_LOOK);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
      this.phases = phases;
      this.stateSpectrumBlueprint.clear();

      for (AttackAnimation.Phase phase : phases) {
         if (!phase.noStateBind) {
            this.bindPhaseState(phase);
         }
      }
   }

   protected void bindPhaseState(AttackAnimation.Phase phase) {
      float preDelay = phase.preDelay;
      this.stateSpectrumBlueprint
         .newTimePair(phase.start, preDelay)
         .addState(EntityState.PHASE_LEVEL, 1)
         .newTimePair(phase.start, phase.contact)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .newTimePair(phase.start, phase.recovery)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .newTimePair(phase.start, phase.end)
         .addState(EntityState.INACTION, true)
         .newTimePair(phase.antic, phase.end)
         .addState(EntityState.TURNING_LOCKED, true)
         .newTimePair(preDelay, phase.contact)
         .addState(EntityState.ATTACKING, true)
         .addState(EntityState.PHASE_LEVEL, 2)
         .newTimePair(phase.contact, phase.end)
         .addState(EntityState.PHASE_LEVEL, 3);
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      super.begin(entitypatch);
      entitypatch.setLastAttackSuccess(false);
   }

   @Override
   public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
      super.linkTick(entitypatch, linkAnimation);
      if (!entitypatch.isLogicalClient()) {
         this.attackTick(entitypatch, linkAnimation);
      }
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      if (!entitypatch.isLogicalClient()) {
         this.attackTick(entitypatch, this.getAccessor());
      }
   }

   @Override
   public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
      super.end(entitypatch, nextAnimation, isEnd);
      if (entitypatch instanceof ServerPlayerPatch playerpatch) {
         if (isEnd) {
            playerpatch.getEventListener()
               .triggerEvents(PlayerEventListener.EventType.ATTACK_ANIMATION_END_EVENT, new AttackEndEvent(playerpatch, this.getAccessor()));
         }

         AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor());
         float elapsedTime = player.getElapsedTime();
         EntityState state = this.getState(entitypatch, elapsedTime);
         if (!isEnd && state.attacking()) {
            playerpatch.getEventListener()
               .triggerEvents(
                  PlayerEventListener.EventType.ATTACK_PHASE_END_EVENT,
                  new AttackPhaseEndEvent(playerpatch, this.getAccessor(), this.getPhaseByTime(elapsedTime), this.getPhaseOrderByTime(elapsedTime))
               );
         }
      }

      if (entitypatch instanceof HumanoidMobPatch<?> mobpatch && entitypatch.isLogicalClient()) {
         Mob entity = (Mob)mobpatch.getOriginal();
         if (entity.m_5448_() != null && !entity.m_5448_().m_6084_()) {
            entity.m_6710_(null);
         }
      }
   }

   protected void attackTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
      AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor());
      float prevElapsedTime = player.getPrevElapsedTime();
      float elapsedTime = player.getElapsedTime();
      EntityState prevState = animation.get().getState(entitypatch, prevElapsedTime);
      EntityState state = animation.get().getState(entitypatch, elapsedTime);
      AttackAnimation.Phase phase = this.getPhaseByTime(animation.get().isLinkAnimation() ? 0.0F : elapsedTime);
      if (prevState.attacking() || state.attacking() || prevState.getLevel() <= 2 && state.getLevel() > 2) {
         if (!prevState.attacking()
            || phase != this.getPhaseByTime(prevElapsedTime) && (state.attacking() || prevState.getLevel() <= 2 && state.getLevel() > 2)) {
            entitypatch.onStrike(this, phase.hand);
            entitypatch.playSound(this.getSwingSound(entitypatch, phase), 0.0F, 0.0F);
            entitypatch.removeHurtEntities();
         }

         this.hurtCollidingEntities(entitypatch, prevElapsedTime, elapsedTime, prevState, state, phase);
         if ((!state.attacking() || elapsedTime >= this.getTotalTime()) && entitypatch instanceof ServerPlayerPatch playerpatch) {
            playerpatch.getEventListener()
               .triggerEvents(
                  PlayerEventListener.EventType.ATTACK_PHASE_END_EVENT,
                  new AttackPhaseEndEvent(playerpatch, this.getAccessor(), phase, this.getPhaseOrderByTime(elapsedTime))
               );
         }
      }
   }

   protected void hurtCollidingEntities(
      LivingEntityPatch<?> entitypatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, AttackAnimation.Phase phase
   ) {
      LivingEntity entity = entitypatch.getOriginal();
      float prevPoseTime = prevState.attacking() ? prevElapsedTime : phase.preDelay;
      float poseTime = state.attacking() ? elapsedTime : phase.contact;
      List<Entity> list = this.getPhaseByTime(elapsedTime)
         .getCollidingEntities(entitypatch, this, prevPoseTime, poseTime, this.getPlaySpeed(entitypatch, this));
      if (!list.isEmpty()) {
         HitEntityList hitEntities = new HitEntityList(
            entitypatch, list, phase.getProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY).orElse(HitEntityList.Priority.DISTANCE)
         );
         int maxStrikes = this.getMaxStrikes(entitypatch, phase);

         while (entitypatch.getCurrentlyActuallyHitEntities().size() < maxStrikes && hitEntities.next()) {
            Entity target = hitEntities.getEntity();
            LivingEntity trueEntity = this.getTrueEntity(target);
            if (trueEntity != null
               && trueEntity.m_6084_()
               && !entitypatch.getCurrentlyAttackTriedEntities().contains(trueEntity)
               && !entitypatch.isTargetInvulnerable(target)
               && (target instanceof LivingEntity || target instanceof PartEntity)) {
               AABB aabb = target.m_20191_();
               if (MathUtils.canBeSeen(
                  target,
                  entity,
                  target.m_20182_().m_82554_(entity.m_146892_()) + aabb.m_82399_().m_82554_(new Vec3(aabb.f_82291_, aabb.f_82292_, aabb.f_82293_))
               )) {
                  EpicFightDamageSource damagesource = this.getEpicFightDamageSource(entitypatch, target, phase);
                  int prevInvulTime = target.f_19802_;
                  target.f_19802_ = 0;
                  AttackResult attackResult = entitypatch.attack(damagesource, target, phase.hand);
                  target.f_19802_ = prevInvulTime;
                  if (attackResult.resultType.dealtDamage()) {
                     SoundEvent hitSound = this.getHitSound(entitypatch, phase);
                     if (hitSound != null) {
                        target.m_9236_()
                           .m_6263_(
                              null, target.m_20185_(), target.m_20186_(), target.m_20189_(), this.getHitSound(entitypatch, phase), target.m_5720_(), 1.0F, 1.0F
                           );
                     }

                     this.spawnHitParticle((ServerLevel)target.m_9236_(), entitypatch, target, phase);
                  }

                  entitypatch.getCurrentlyAttackTriedEntities().add(trueEntity);
                  if (attackResult.resultType.shouldCount()) {
                     entitypatch.getCurrentlyActuallyHitEntities().add(trueEntity);
                  }
               }
            }
         }
      }
   }

   public LivingEntity getTrueEntity(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         return livingEntity;
      } else {
         return entity instanceof PartEntity<?> partEntity && partEntity.getParent() instanceof LivingEntity livingEntity ? livingEntity : null;
      }
   }

   protected int getMaxStrikes(LivingEntityPatch<?> entitypatch, AttackAnimation.Phase phase) {
      return phase.getProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER)
         .map(valueModifier -> (int)ValueModifier.calculator().attach(valueModifier).getResult(entitypatch.getMaxStrikes(phase.hand)))
         .orElse(entitypatch.getMaxStrikes(phase.hand));
   }

   protected SoundEvent getSwingSound(LivingEntityPatch<?> entitypatch, AttackAnimation.Phase phase) {
      return phase.getProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND).orElse(entitypatch.getSwingSound(phase.hand));
   }

   protected SoundEvent getHitSound(LivingEntityPatch<?> entitypatch, AttackAnimation.Phase phase) {
      return phase.getProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND).orElse(entitypatch.getWeaponHitSound(phase.hand));
   }

   public EpicFightDamageSource getEpicFightDamageSource(LivingEntityPatch<?> entitypatch, Entity target, AttackAnimation.Phase phase) {
      return this.getEpicFightDamageSource(entitypatch.getDamageSource(this.getAccessor(), phase.hand), entitypatch, target, phase);
   }

   public EpicFightDamageSource getEpicFightDamageSource(
      DamageSource originalSource, LivingEntityPatch<?> entitypatch, Entity target, AttackAnimation.Phase phase
   ) {
      if (phase == null) {
         phase = this.getPhaseByTime(entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor()).getElapsedTime());
      }

      EpicFightDamageSource epicfightSource;
      if (originalSource instanceof EpicFightDamageSource epicfightDamageSource) {
         epicfightSource = epicfightDamageSource;
      } else {
         epicfightSource = EpicFightDamageSources.fromVanillaDamageSource(originalSource).setAnimation(this.getAccessor());
      }

      phase.getProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER).ifPresent(opt -> epicfightSource.attachDamageModifier(opt));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER).ifPresent(opt -> epicfightSource.attachArmorNegationModifier(opt));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER).ifPresent(opt -> epicfightSource.attachImpactModifier(opt));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE).ifPresent(opt -> epicfightSource.setStunType(opt));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG).ifPresent(opt -> opt.forEach(epicfightSource::addRuntimeTag));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.EXTRA_DAMAGE).ifPresent(opt -> opt.forEach(epicfightSource::addExtraDamage));
      phase.getProperty(AnimationProperty.AttackPhaseProperty.SOURCE_LOCATION_PROVIDER)
         .ifPresentOrElse(
            opt -> epicfightSource.setInitialPosition(opt.apply(entitypatch)), () -> epicfightSource.setInitialPosition(entitypatch.getOriginal().m_20182_())
         );
      return epicfightSource;
   }

   protected void spawnHitParticle(ServerLevel world, LivingEntityPatch<?> attacker, Entity hit, AttackAnimation.Phase phase) {
      Optional<RegistryObject<HitParticleType>> particleOptional = phase.getProperty(AnimationProperty.AttackPhaseProperty.PARTICLE);
      HitParticleType particle = particleOptional.isPresent() ? (HitParticleType)particleOptional.get().get() : attacker.getWeaponHitParticle(phase.hand);
      particle.spawnParticleWithArgument(world, null, null, hit, attacker.getOriginal());
   }

   @Override
   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      if (entitypatch instanceof PlayerPatch<?> playerpatch) {
         AttackAnimation.Phase phase = this.getPhaseByTime(playerpatch.<Animator>getAnimator().getPlayerFor(this.getAccessor()).getElapsedTime());
         float speedFactor = this.getProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR).orElse(1.0F);
         Optional<Float> property = this.getProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED);
         float correctedSpeed = property.<Float>map(value -> playerpatch.getAttackSpeed(phase.hand) / value)
            .orElse(this.getTotalTime() * playerpatch.getAttackSpeed(phase.hand));
         correctedSpeed = Math.round(correctedSpeed * 1000.0F) / 1000.0F;
         return 1.0F + (correctedSpeed - 1.0F) * speedFactor;
      } else {
         return 1.0F;
      }
   }

   public <V, A extends AttackAnimation> A addProperty(AnimationProperty.AttackPhaseProperty<V> propertyType, V value) {
      return this.addProperty(propertyType, value, 0);
   }

   public <V, A extends AttackAnimation> A addProperty(AnimationProperty.AttackPhaseProperty<V> propertyType, V value, int index) {
      this.phases[index].addProperty(propertyType, value);
      return (A)this;
   }

   public <A extends AttackAnimation> A removeProperty(AnimationProperty.AttackPhaseProperty<?> propertyType) {
      return this.removeProperty(propertyType, 0);
   }

   public <A extends AttackAnimation> A removeProperty(AnimationProperty.AttackPhaseProperty<?> propertyType, int index) {
      this.phases[index].removeProperty(propertyType);
      return (A)this;
   }

   public AttackAnimation.Phase getPhaseByTime(float elapsedTime) {
      AttackAnimation.Phase currentPhase = null;

      for (AttackAnimation.Phase phase : this.phases) {
         currentPhase = phase;
         if (phase.end > elapsedTime) {
            break;
         }
      }

      return currentPhase;
   }

   public int getPhaseOrderByTime(float elapsedTime) {
      int i = 0;

      for (AttackAnimation.Phase phase : this.phases) {
         if (phase.end > elapsedTime) {
            break;
         }

         i++;
      }

      return i;
   }

   @Override
   public Object getModifiedLinkState(EntityState.StateFactor<?> factor, Object val, LivingEntityPatch<?> entitypatch, float elapsedTime) {
      return factor == EntityState.ATTACKING && elapsedTime < this.getPlaySpeed(entitypatch, this) * 0.05F ? false : val;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, float playbackTime, float partialTicks) {
      AnimationPlayer animPlayer = entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor());
      float prevElapsedTime = animPlayer.getPrevElapsedTime();
      float elapsedTime = animPlayer.getElapsedTime();
      AttackAnimation.Phase phase = this.getPhaseByTime(playbackTime);

      for (Pair<Joint, Collider> colliderInfo : phase.colliders) {
         Collider collider = (Collider)colliderInfo.getSecond();
         if (collider == null) {
            collider = entitypatch.getColliderMatching(phase.hand);
         }

         collider.draw(
            poseStack,
            buffer,
            entitypatch,
            this,
            (Joint)colliderInfo.getFirst(),
            prevElapsedTime,
            elapsedTime,
            partialTicks,
            this.getPlaySpeed(entitypatch, this)
         );
      }
   }

   public static class JointColliderPair extends Pair<Joint, Collider> {
      public JointColliderPair(Joint first, Collider second) {
         super(first, second);
      }

      public static AttackAnimation.JointColliderPair of(Joint joint, Collider collider) {
         return new AttackAnimation.JointColliderPair(joint, collider);
      }
   }

   public static class Phase {
      private final Map<AnimationProperty.AttackPhaseProperty<?>, Object> properties = Maps.newHashMap();
      public final float start;
      public final float antic;
      public final float preDelay;
      public final float contact;
      public final float recovery;
      public final float end;
      public final InteractionHand hand;
      public AttackAnimation.JointColliderPair[] colliders;
      public final boolean noStateBind;

      public Phase(float start, float antic, float contact, float recovery, float end, Joint joint, Collider collider) {
         this(start, antic, contact, recovery, end, InteractionHand.MAIN_HAND, joint, collider);
      }

      public Phase(float start, float antic, float contact, float recovery, float end, InteractionHand hand, Joint joint, Collider collider) {
         this(start, antic, antic, contact, recovery, end, hand, joint, collider);
      }

      public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, Joint joint, Collider collider) {
         this(start, antic, preDelay, contact, recovery, end, InteractionHand.MAIN_HAND, joint, collider);
      }

      public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, InteractionHand hand, Joint joint, Collider collider) {
         this(start, antic, preDelay, contact, recovery, end, false, hand, joint, collider);
      }

      public Phase(InteractionHand hand, Joint joint, Collider collider) {
         this(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, true, hand, joint, collider);
      }

      public Phase(
         float start,
         float antic,
         float preDelay,
         float contact,
         float recovery,
         float end,
         boolean noStateBind,
         InteractionHand hand,
         Joint joint,
         Collider collider
      ) {
         this(start, antic, preDelay, contact, recovery, end, noStateBind, hand, AttackAnimation.JointColliderPair.of(joint, collider));
      }

      public Phase(
         float start,
         float antic,
         float preDelay,
         float contact,
         float recovery,
         float end,
         InteractionHand hand,
         AttackAnimation.JointColliderPair... colliders
      ) {
         this(start, antic, preDelay, contact, recovery, end, false, hand, colliders);
      }

      public Phase(
         float start,
         float antic,
         float preDelay,
         float contact,
         float recovery,
         float end,
         boolean noStateBind,
         InteractionHand hand,
         AttackAnimation.JointColliderPair... colliders
      ) {
         if (start > end) {
            throw new IllegalArgumentException("Phase create exception: Start time is bigger than end time");
         }

         this.start = start;
         this.antic = antic;
         this.preDelay = preDelay;
         this.contact = contact;
         this.recovery = recovery;
         this.end = end;
         this.colliders = colliders;
         this.hand = hand;
         this.noStateBind = noStateBind;
      }

      public <V> AttackAnimation.Phase addProperty(AnimationProperty.AttackPhaseProperty<V> propertyType, V value) {
         this.properties.put(propertyType, value);
         return this;
      }

      public AttackAnimation.Phase removeProperty(AnimationProperty.AttackPhaseProperty<?> propertyType) {
         this.properties.remove(propertyType);
         return this;
      }

      public void addProperties(Set<Entry<AnimationProperty.AttackPhaseProperty<?>, Object>> set) {
         for (Entry<AnimationProperty.AttackPhaseProperty<?>, Object> entry : set) {
            this.properties.put(entry.getKey(), entry.getValue());
         }
      }

      public <V> Optional<V> getProperty(AnimationProperty.AttackPhaseProperty<V> propertyType) {
         return Optional.ofNullable((V)this.properties.get(propertyType));
      }

      public List<Entity> getCollidingEntities(
         LivingEntityPatch<?> entitypatch, AttackAnimation animation, float prevElapsedTime, float elapsedTime, float attackSpeed
      ) {
         Set<Entity> entities = Sets.newHashSet();

         for (Pair<Joint, Collider> colliderInfo : this.colliders) {
            Collider collider = (Collider)colliderInfo.getSecond();
            if (collider == null) {
               collider = entitypatch.getColliderMatching(this.hand);
            }

            entities.addAll(
               collider.updateAndSelectCollideEntity(entitypatch, animation, prevElapsedTime, elapsedTime, (Joint)colliderInfo.getFirst(), attackSpeed)
            );
         }

         return new ArrayList<>(entities);
      }

      public AttackAnimation.JointColliderPair[] getColliders() {
         return this.colliders;
      }

      public InteractionHand getHand() {
         return this.hand;
      }
   }
}
