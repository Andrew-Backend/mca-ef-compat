package yesman.epicfight.api.animation.types.procedural;

import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EnderDragonAttackAnimation extends AttackAnimation {
   public EnderDragonAttackAnimation(
      float convertTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends EnderDragonAttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD);
      this.addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null);
   }

   @Override
   public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
      super.putOnPlayer(animationPlayer, entitypatch);
      if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
         Vec3 entitypos = ikSimulatable.toEntity().m_20182_();
         OpenMatrix4f toWorld = OpenMatrix4f.mul(
            OpenMatrix4f.createTranslation((float)entitypos.f_82479_, (float)entitypos.f_82480_, (float)entitypos.f_82481_),
            ikSimulatable.getModelMatrix(1.0F),
            null
         );
         this.getProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION)
            .ifPresent(
               ikDefinitions -> {
                  for (InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKInfo : ikDefinitions) {
                     TransformSheet tipAnim = bakedIKInfo.terminalBoneTransform().getFirstFrame();
                     Keyframe[] keyframes = tipAnim.getKeyframes();
                     JointTransform firstposeTransform = keyframes[0].transform();
                     firstposeTransform.translation().multiply(-1.0F, 1.0F, -1.0F);
                     if (bakedIKInfo.clipAnimation() && !bakedIKInfo.touchingGround()[0]) {
                        firstposeTransform.translation().set(OpenMatrix4f.transform3v(toWorld, firstposeTransform.translation(), null));
                     } else {
                        Vec3f rayResultPosition = this.getRayCastedTipPosition(
                           ikSimulatable, firstposeTransform.translation().add(0.0F, 2.5F, 0.0F), toWorld, 8.0F, bakedIKInfo.rayLeastHeight()
                        );
                        firstposeTransform.translation().set(rayResultPosition);
                     }

                     for (Keyframe keyframe : keyframes) {
                        keyframe.transform().translation().set(firstposeTransform.translation());
                     }

                     ikSimulatable.getIKSimulator()
                        .runUntil(
                           bakedIKInfo.endJoint(),
                           this,
                           InverseKinematicsSimulator.InverseKinematicsBuilder.create(firstposeTransform.translation(), tipAnim, bakedIKInfo),
                           () -> entitypatch.<Animator>getAnimator().getPlayer(this.getAccessor()).isPresent()
                        );
                  }
               }
            );
      }
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      if (entitypatch.isLogicalClient()) {
         entitypatch.getClientAnimator().resetMotion(true);
         entitypatch.getClientAnimator().resetCompositeMotion();
      }
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
         Vec3 entitypos = ikSimulatable.toEntity().m_20182_();
         OpenMatrix4f toWorld = OpenMatrix4f.mul(
            OpenMatrix4f.createTranslation((float)entitypos.f_82479_, (float)entitypos.f_82480_, (float)entitypos.f_82481_),
            ikSimulatable.getModelMatrix(1.0F),
            null
         );
         float elapsedTime = entitypatch.<Animator>getAnimator().getPlayerFor(this.getAccessor()).getElapsedTime();
         this.getProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION)
            .ifPresent(
               ikDefinitions -> {
                  for (InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKInfo : ikDefinitions) {
                     if (ikSimulatable.getIKSimulator().isRunning(bakedIKInfo.endJoint()) && bakedIKInfo.clipAnimation()) {
                        Keyframe[] keyframes = this.getTransfroms().get(bakedIKInfo.endJoint().getName()).getKeyframes();
                        float startTime = keyframes[bakedIKInfo.startFrame()].time();
                        float endTime = keyframes[bakedIKInfo.endFrame() - 1].time();
                        if (startTime <= elapsedTime && elapsedTime < endTime) {
                           InverseKinematicsSimulator.InverseKinematicsObject tipAnim = ikSimulatable.getIKSimulator()
                              .getRunningObject(bakedIKInfo.endJoint())
                              .get();
                           Vec3f clipStart = bakedIKInfo.endPosition().copy().add(0.0F, 2.5F, 0.0F).multiply(-1.0F, 1.0F, -1.0F);
                           Vec3f finalTargetpos = bakedIKInfo.clipAnimation() && !bakedIKInfo.touchingGround()[bakedIKInfo.touchingGround().length - 1]
                              ? OpenMatrix4f.transform3v(toWorld, bakedIKInfo.endPosition().multiply(-1.0F, 1.0F, -1.0F), null)
                              : this.getRayCastedTipPosition(ikSimulatable, clipStart, toWorld, 8.0F, bakedIKInfo.rayLeastHeight());
                           if (tipAnim.isOnWorking()) {
                              tipAnim.newTargetPosition(finalTargetpos);
                           } else {
                              this.startPartAnimation(
                                 bakedIKInfo, tipAnim, this.clipAnimation(bakedIKInfo.terminalBoneTransform(), bakedIKInfo), finalTargetpos
                              );
                           }
                        }
                     }
                  }
               }
            );
      }
   }
}
