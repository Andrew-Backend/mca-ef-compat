package yesman.epicfight.api.client.animation.property;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class JointMask {
   public static final JointMask.BindModifier KEEP_CHILD_LOCROT = (entitypatch, baseLayerPose, result, livingMotion, wholeEntry, priority, joint, poses) -> {
      Pose currentPose = (Pose)poses.get(priority).getSecond();
      JointTransform lowestTransform = baseLayerPose.orElseEmpty(joint.getName());
      JointTransform currentTransform = currentPose.orElseEmpty(joint.getName());
      result.orElseEmpty(joint.getName()).translation().y = lowestTransform.translation().y;
      OpenMatrix4f lowestMatrix = lowestTransform.toMatrix();
      OpenMatrix4f currentMatrix = currentTransform.toMatrix();
      OpenMatrix4f currentToLowest = OpenMatrix4f.mul(OpenMatrix4f.invert(currentMatrix, null), lowestMatrix, null);

      for (Joint subJoint : joint.getSubJoints()) {
         if (wholeEntry.isMasked(livingMotion, subJoint.getName())) {
            OpenMatrix4f lowestLocalTransform = OpenMatrix4f.mul(joint.getLocalTransform(), lowestMatrix, null);
            OpenMatrix4f currentLocalTransform = OpenMatrix4f.mul(joint.getLocalTransform(), currentMatrix, null);
            OpenMatrix4f childTransform = OpenMatrix4f.mul(subJoint.getLocalTransform(), result.orElseEmpty(subJoint.getName()).toMatrix(), null);
            OpenMatrix4f lowestFinal = OpenMatrix4f.mul(lowestLocalTransform, childTransform, null);
            OpenMatrix4f currentFinal = OpenMatrix4f.mul(currentLocalTransform, childTransform, null);
            Vec3f vec = new Vec3f((currentFinal.m30 - lowestFinal.m30) * 0.5F, currentFinal.m31 - lowestFinal.m31, currentFinal.m32 - lowestFinal.m32);
            JointTransform jt = result.orElseEmpty(subJoint.getName());
            jt.parent(JointTransform.translation(vec), OpenMatrix4f::mul);
            jt.jointLocal(JointTransform.fromMatrixWithoutScale(currentToLowest), OpenMatrix4f::mul);
         }
      }
   };
   private final String jointName;
   private final JointMask.BindModifier bindModifier;

   public static JointMask of(String jointName, JointMask.BindModifier bindModifier) {
      return new JointMask(jointName, bindModifier);
   }

   public static JointMask of(String jointName) {
      return new JointMask(jointName, null);
   }

   private JointMask(String jointName, JointMask.BindModifier bindModifier) {
      this.jointName = jointName;
      this.bindModifier = bindModifier;
   }

   @FunctionalInterface
   public interface BindModifier {
      void modify(
         LivingEntityPatch<?> var1,
         Pose var2,
         Pose var3,
         LivingMotion var4,
         JointMaskEntry var5,
         Layer.Priority var6,
         Joint var7,
         Map<Layer.Priority, Pair<AssetAccessor<? extends DynamicAnimation>, Pose>> var8
      );
   }

   public static class JointMaskSet {
      final Map<String, JointMask.BindModifier> masks = Maps.newHashMap();

      public boolean contains(String name) {
         return this.masks.containsKey(name);
      }

      public JointMask.BindModifier getBindModifier(String jointName) {
         return this.masks.get(jointName);
      }

      public static JointMask.JointMaskSet of(JointMask... masks) {
         JointMask.JointMaskSet jointMaskSet = new JointMask.JointMaskSet();

         for (JointMask jointMask : masks) {
            jointMaskSet.masks.put(jointMask.jointName, jointMask.bindModifier);
         }

         return jointMaskSet;
      }

      public static JointMask.JointMaskSet of(Set<JointMask> jointMasks) {
         JointMask.JointMaskSet jointMaskSet = new JointMask.JointMaskSet();

         for (JointMask jointMask : jointMasks) {
            jointMaskSet.masks.put(jointMask.jointName, jointMask.bindModifier);
         }

         return jointMaskSet;
      }
   }
}
