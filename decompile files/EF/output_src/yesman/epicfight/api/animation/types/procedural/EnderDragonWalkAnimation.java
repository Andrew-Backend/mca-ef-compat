package yesman.epicfight.api.animation.types.procedural;

import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EnderDragonWalkAnimation extends StaticAnimation {
   public EnderDragonWalkAnimation(
      float convertTime, AnimationManager.AnimationAccessor<? extends EnderDragonWalkAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(convertTime, true, accessor, armature);
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
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
         if (entitypatch.<Animator>getAnimator().getPlayerFor(null).getAnimation().get() != this) {
            return;
         }

         Vec3 entitypos = ikSimulatable.toEntity().m_20182_();
         OpenMatrix4f toWorld = OpenMatrix4f.mul(
            OpenMatrix4f.createTranslation((float)entitypos.f_82479_, (float)entitypos.f_82480_, (float)entitypos.f_82481_),
            ikSimulatable.getModelMatrix(1.0F),
            null
         );

         for (InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKInfo : this.getProperty(
               AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION
            )
            .orElse(null)) {
            if (ikSimulatable.getIKSimulator().isRunning(bakedIKInfo.endJoint())) {
               InverseKinematicsSimulator.InverseKinematicsObject ikObject = ikSimulatable.getIKSimulator().getRunningObject(bakedIKInfo.endJoint()).get();
               Vec3f clipStart = bakedIKInfo.endPosition().copy().add(0.0F, 2.5F, 0.0F).multiply(-1.0F, 1.0F, -1.0F);
               Vec3f finalTargetpos = this.getRayCastedTipPosition(ikSimulatable, clipStart, toWorld, 8.0F, bakedIKInfo.rayLeastHeight());
               if (ikObject.isOnWorking()) {
                  ikObject.newTargetPosition(finalTargetpos);
               } else {
                  InverseKinematicsSimulator.InverseKinematicsObject opponentIKObject = ikSimulatable.getIKSimulator()
                     .getRunningObject(bakedIKInfo.opponentJoint())
                     .get();
                  if (opponentIKObject != null && !opponentIKObject.isOnWorking()) {
                     Vec3f footpos = ikObject.getTipPosition(1.0F);
                     if (footpos.distanceSqr(finalTargetpos) > 15.0) {
                        this.startPartAnimation(bakedIKInfo, ikObject, this.clipAnimation(bakedIKInfo.terminalBoneTransform(), bakedIKInfo), finalTargetpos);
                     }
                  }
               }
            }
         }
      }
   }
}
