package yesman.epicfight.api.animation.types;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSyncPlayerAnimationPosition;
import yesman.epicfight.network.server.SPSyncAnimationPosition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ActionAnimation extends MainFrameAnimation {
   public static final AnimationVariables.SharedAnimationVariableKey<TransformSheet> ACTION_ANIMATION_COORD = AnimationVariables.shared(
      animator -> new TransformSheet(), false
   );
   public static final AnimationVariables.IndependentAnimationVariableKey<Vec3> BEGINNING_LOCATION = AnimationVariables.independent(
      animator -> animator.getEntityPatch().getOriginal().m_20182_(), true
   );
   public static final AnimationVariables.IndependentAnimationVariableKey<Float> INITIAL_LOOK_VEC_DOT = AnimationVariables.independent(animator -> 1.0F, true);

   public ActionAnimation(
      float transitionTime, AnimationManager.AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, Float.MAX_VALUE, accessor, armature);
   }

   public ActionAnimation(
      float transitionTime, float postDelay, AnimationManager.AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, accessor, armature);
      this.stateSpectrumBlueprint
         .clear()
         .newTimePair(0.0F, postDelay)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .addState(EntityState.TURNING_LOCKED, true)
         .newTimePair(0.0F, Float.MAX_VALUE)
         .addState(EntityState.INACTION, true);
      this.addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
   }

   public ActionAnimation(float transitionTime, float postDelay, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, path, armature);
      this.stateSpectrumBlueprint
         .clear()
         .newTimePair(0.0F, postDelay)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .addState(EntityState.TURNING_LOCKED, true)
         .newTimePair(0.0F, Float.MAX_VALUE)
         .addState(EntityState.INACTION, true);
      this.addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
   }

   @Override
   public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
      if (entitypatch.shouldMoveOnCurrentSide(this)) {
         MoveCoordFunctions.MoveCoordSetter moveCoordSetter = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN)
            .orElse(MoveCoordFunctions.RAW_COORD);
         moveCoordSetter.set(this, entitypatch, entitypatch.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD));
      }

      super.putOnPlayer(animationPlayer, entitypatch);
   }

   protected void initCoordVariables(LivingEntityPatch<?> entitypatch) {
      Vec3 start = entitypatch.getOriginal().m_20182_();
      if (entitypatch.getTarget() != null) {
         Vec3 targetTracePosition = entitypatch.getTarget().m_20182_();
         Vec3 toDestWorld = targetTracePosition.m_82546_(start);
         float dot = Mth.m_14036_((float)toDestWorld.m_82541_().m_82526_(MathUtils.getVectorForRotation(0.0F, entitypatch.getYRot())), 0.0F, 1.0F);
         entitypatch.<Animator>getAnimator().getVariables().put(INITIAL_LOOK_VEC_DOT, this.getAccessor(), dot);
      }

      entitypatch.<Animator>getAnimator().getVariables().put(BEGINNING_LOCATION, this.getAccessor(), start);
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      entitypatch.cancelItemUse();
      super.begin(entitypatch);
      if (entitypatch.shouldMoveOnCurrentSide(this)) {
         entitypatch.beginAction(this);
         this.initCoordVariables(entitypatch);
         if (this.getProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT).orElse(false)) {
            entitypatch.getOriginal().m_20334_(0.0, entitypatch.getOriginal().m_20184_().f_82480_, 0.0);
            entitypatch.getOriginal().f_20900_ = 0.0F;
            entitypatch.getOriginal().f_20901_ = 0.0F;
            entitypatch.getOriginal().f_20902_ = 0.0F;
         }
      }
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      if (this.getProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT).orElse(false)) {
         double gravity = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false)
            ? 0.0
            : entitypatch.getOriginal().m_20184_().f_82480_;
         entitypatch.getOriginal().m_20334_(0.0, gravity, 0.0);
      }

      this.move(entitypatch, this.getAccessor());
   }

   @Override
   public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
      if (this.getProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT).orElse(false)) {
         double gravity = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false)
            ? 0.0
            : entitypatch.getOriginal().m_20184_().f_82480_;
         entitypatch.getOriginal().m_20334_(0.0, gravity, 0.0);
      }

      this.move(entitypatch, linkAnimation);
   }

   protected void move(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
      if (this.validateMovement(entitypatch, animation)) {
         float elapsedTime = entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor()).getElapsedTime();
         if (this.getState(EntityState.INACTION, entitypatch, elapsedTime)) {
            LivingEntity livingentity = entitypatch.getOriginal();
            Vec3 vec3 = this.getCoordVector(entitypatch, animation);
            livingentity.m_6478_(MoverType.SELF, vec3);
            if (entitypatch.isLogicalClient()) {
               EpicFightNetworkManager.sendToServer(
                  new CPSyncPlayerAnimationPosition(livingentity.m_19879_(), elapsedTime, livingentity.m_20182_(), animation.get().isLinkAnimation() ? 2 : 1)
               );
            } else {
               EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
                  new SPSyncAnimationPosition(livingentity.m_19879_(), elapsedTime, livingentity.m_20182_(), animation.get().isLinkAnimation() ? 2 : 1),
                  livingentity
               );
            }
         }
      }
   }

   protected boolean validateMovement(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
      if (!entitypatch.shouldMoveOnCurrentSide(this)) {
         return false;
      } else if (animation.get().isLinkAnimation()) {
         return !this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK).orElse(true) ? false : this.shouldMove(0.0F);
      } else {
         return this.shouldMove(entitypatch.<Animator>getAnimator().getPlayerFor(animation).getElapsedTime());
      }
   }

   protected boolean shouldMove(float currentTime) {
      if (this.properties.containsKey(AnimationProperty.ActionAnimationProperty.MOVE_TIME)) {
         TimePairList moveTimes = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_TIME).get();
         return moveTimes.isTimeInPairs(currentTime);
      } else {
         return true;
      }
   }

   @Override
   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      if (this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isEmpty()) {
         this.correctRootJoint(animation, pose, entitypatch, time, partialTicks);
      }

      super.modifyPose(animation, pose, entitypatch, time, partialTicks);
   }

   public void correctRootJoint(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      JointTransform jt = pose.orElseEmpty("Root");
      Vec3f jointPosition = jt.translation();
      OpenMatrix4f toRootTransformApplied = entitypatch.getArmature().searchJointByName("Root").getLocalTransform().removeTranslation();
      OpenMatrix4f toOrigin = OpenMatrix4f.invert(toRootTransformApplied, null);
      Vec3f worldPosition = OpenMatrix4f.transform3v(toRootTransformApplied, jointPosition, null);
      worldPosition.x = 0.0F;
      worldPosition.y = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false) && worldPosition.y > 0.0F
         ? 0.0F
         : worldPosition.y;
      worldPosition.z = 0.0F;
      OpenMatrix4f.transform3v(toOrigin, worldPosition, worldPosition);
      jointPosition.x = worldPosition.x;
      jointPosition.y = worldPosition.y;
      jointPosition.z = worldPosition.z;
   }

   @Override
   public void setLinkAnimation(
      AssetAccessor<? extends DynamicAnimation> fromAnimation,
      Pose startPose,
      boolean isOnSameLayer,
      float transitionTimeModifier,
      LivingEntityPatch<?> entitypatch,
      LinkAnimation dest
   ) {
      dest.resetNextStartTime();
      float playTime = this.getPlaySpeed(entitypatch, dest);
      AnimationProperty.PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation()
         .get()
         .getProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER)
         .orElse(null);
      if (playSpeedModifier != null) {
         playTime = playSpeedModifier.modify(this, entitypatch, playTime, 0.0F, playTime);
      }

      playTime = Math.abs(playTime) * 0.05F;
      float linkTime = transitionTimeModifier > 0.0F ? transitionTimeModifier + this.transitionTime : this.transitionTime;
      float totalTime = playTime * (int)Math.ceil(linkTime / playTime);
      float nextStartTime = Math.max(0.0F, -transitionTimeModifier);
      nextStartTime += totalTime - linkTime;
      dest.setNextStartTime(nextStartTime);
      dest.getAnimationClip().reset();
      dest.setTotalTime(totalTime);
      dest.setConnectedAnimations(fromAnimation, this.getAccessor());
      Pose nextStartPose = this.getPoseByTime(entitypatch, nextStartTime, 1.0F);
      if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
         this.correctRawZCoord(entitypatch, nextStartPose, nextStartTime);
      }

      Map<String, JointTransform> data1 = startPose.getJointTransformData();
      Map<String, JointTransform> data2 = nextStartPose.getJointTransformData();
      Set<String> joint1 = new HashSet<>(isOnSameLayer ? data1.keySet() : Set.of());
      Set<String> joint2 = new HashSet<>(data2.keySet());
      if (entitypatch.isLogicalClient()) {
         JointMaskEntry entry = fromAnimation.get().getJointMaskEntry(entitypatch, false).orElse(null);
         JointMaskEntry entry2 = this.getJointMaskEntry(entitypatch, true).orElse(null);
         if (entry != null && entitypatch.isLogicalClient()) {
            joint1.removeIf(
               jointNamex -> entry.isMasked(
                  fromAnimation.get().getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER
                     ? entitypatch.getClientAnimator().currentMotion()
                     : entitypatch.getClientAnimator().currentCompositeMotion(),
                  jointNamex
               )
            );
         }

         if (entry2 != null && entitypatch.isLogicalClient()) {
            joint2.removeIf(
               jointNamex -> entry2.isMasked(
                  this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER
                     ? entitypatch.getCurrentLivingMotion()
                     : entitypatch.currentCompositeMotion,
                  jointNamex
               )
            );
         }
      }

      joint1.addAll(joint2);
      if (linkTime != totalTime) {
         Pose pose = this.getPoseByTime(entitypatch, 0.0F, 0.0F);
         Map<String, JointTransform> poseData = pose.getJointTransformData();
         if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
            this.correctRawZCoord(entitypatch, pose, 0.0F);
         }

         for (String jointName : joint1) {
            Keyframe[] keyframes = new Keyframe[]{
               new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty())),
               new Keyframe(linkTime, poseData.getOrDefault(jointName, JointTransform.empty())),
               new Keyframe(totalTime, data2.getOrDefault(jointName, JointTransform.empty()))
            };
            TransformSheet sheet = new TransformSheet(keyframes);
            dest.getAnimationClip().addJointTransform(jointName, sheet);
         }
      } else {
         for (String jointName : joint1) {
            Keyframe[] keyframes = new Keyframe[]{
               new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty())),
               new Keyframe(totalTime, data2.getOrDefault(jointName, JointTransform.empty()))
            };
            TransformSheet sheet = new TransformSheet(keyframes);
            dest.getAnimationClip().addJointTransform(jointName, sheet);
         }
      }

      dest.loadCoord(null);
      this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).ifPresent(coord -> {
         Keyframe[] keyframesx = new Keyframe[]{new Keyframe(0.0F, JointTransform.empty()), new Keyframe(totalTime, coord.getKeyframes()[0].transform())};
         TransformSheet sheetx = new TransformSheet(keyframesx);
         dest.loadCoord(sheetx);
      });
      if (entitypatch.shouldMoveOnCurrentSide(this)) {
         MoveCoordFunctions.MoveCoordSetter moveCoordSetter = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN)
            .orElse(MoveCoordFunctions.RAW_COORD);
         moveCoordSetter.set(dest, entitypatch, entitypatch.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD));
      }
   }

   public void correctRawZCoord(LivingEntityPatch<?> entitypatch, Pose pose, float poseTime) {
      JointTransform jt = pose.orElseEmpty("Root");
      if (this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isEmpty()) {
         TransformSheet coordTransform = this.getTransfroms().get("Root");
         jt.translation().add(0.0F, 0.0F, coordTransform.getInterpolatedTranslation(poseTime).z);
      }
   }

   public Vec3 getExpectedMovement(LivingEntityPatch<?> entitypatch, float elapseTime) {
      this.initCoordVariables(entitypatch);
      TransformSheet coordTransform = new TransformSheet();
      MoveCoordFunctions.MoveCoordSetter moveCoordSetter = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN)
         .orElse(MoveCoordFunctions.RAW_COORD);
      moveCoordSetter.set(this, entitypatch, coordTransform);
      MoveCoordFunctions.MoveCoordGetter moveGetter = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_GET)
         .orElse(MoveCoordFunctions.MODEL_COORD);
      Vec3f move = moveGetter.get(this, entitypatch, coordTransform, 0.0F, elapseTime);
      return move.toDoubleVector();
   }

   protected Vec3 getCoordVector(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
      AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(animation);
      TimePairList coordUpdateTime = this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME).orElse(null);
      boolean inUpdateTime = coordUpdateTime == null || coordUpdateTime.isTimeInPairs(player.getElapsedTime());
      boolean getRawCoord = this.getProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE).orElse(!inUpdateTime);
      TransformSheet transformSheet = entitypatch.<Animator>getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD);
      MoveCoordFunctions.MoveCoordSetter moveCoordsetter = getRawCoord
         ? MoveCoordFunctions.RAW_COORD
         : this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK).orElse(null);
      if (moveCoordsetter != null) {
         moveCoordsetter.set(animation.get(), entitypatch, transformSheet);
      }

      boolean hasNoGravity = entitypatch.getOriginal().m_20068_();
      boolean moveVertical = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL)
         .orElse(this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isPresent());
      MoveCoordFunctions.MoveCoordGetter moveGetter = getRawCoord
         ? MoveCoordFunctions.MODEL_COORD
         : this.getProperty(AnimationProperty.ActionAnimationProperty.COORD_GET).orElse(MoveCoordFunctions.MODEL_COORD);
      Vec3f move = moveGetter.get(animation.get(), entitypatch, transformSheet, player.getPrevElapsedTime(), player.getElapsedTime());
      LivingEntity livingentity = entitypatch.getOriginal();
      Vec3 motion = livingentity.m_20184_();
      this.getProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME).ifPresentOrElse(noGravityTime -> {
         if (noGravityTime.isTimeInPairs(animation.get().isLinkAnimation() ? 0.0F : player.getElapsedTime())) {
            livingentity.m_20334_(motion.f_82479_, 0.0, motion.f_82481_);
         } else {
            move.y = 0.0F;
         }
      }, () -> {
         if (moveVertical && move.y > 0.0F && !hasNoGravity) {
            double gravity = livingentity.m_21051_((Attribute)ForgeMod.ENTITY_GRAVITY.get()).m_22135_();
            livingentity.m_20334_(motion.f_82479_, motion.f_82480_ < 0.0 ? motion.f_82480_ + gravity : 0.0, motion.f_82481_);
         }
      });
      if (!moveVertical) {
         move.y = 0.0F;
      }

      if (inUpdateTime) {
         this.getProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER).ifPresent(entityYRotProvider -> {
            float yRot = entityYRotProvider.get(animation.get(), entitypatch);
            entitypatch.setYRot(yRot);
         });
      }

      return move.toDoubleVector();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
      return playerpatch.isLogicalClient();
   }
}
