package yesman.epicfight.api.animation;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import yesman.epicfight.api.utils.math.AnimationTransformEntry;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.MatrixOperation;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;

public class JointTransform {
   public static final String ANIMATION_TRANSFORM = "animation_transform";
   public static final String JOINT_LOCAL_TRANSFORM = "joint_local_transform";
   public static final String PARENT = "parent";
   public static final String RESULT1 = "front_result";
   public static final String RESULT2 = "overwrite_rotation";
   private final Map<String, JointTransform.TransformEntry> entries = Maps.newHashMap();
   private final Vec3f translation;
   private final Vec3f scale;
   private final Quaternionf rotation;

   public JointTransform(Vec3f translation, Quaternionf rotation, Vec3f scale) {
      this.translation = translation;
      this.rotation = rotation;
      this.scale = scale;
   }

   public Vec3f translation() {
      return this.translation;
   }

   public Quaternionf rotation() {
      return this.rotation;
   }

   public Vec3f scale() {
      return this.scale;
   }

   public void clearTransform() {
      this.translation.set(0.0F, 0.0F, 0.0F);
      this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
      this.scale.set(1.0F, 1.0F, 1.0F);
   }

   public JointTransform copy() {
      return empty().copyFrom(this);
   }

   public JointTransform copyFrom(JointTransform jt) {
      Vec3f newV = jt.translation();
      Quaternionf newQ = jt.rotation();
      Vec3f newS = jt.scale;
      this.translation.set(newV);
      this.rotation.set(newQ);
      this.scale.set(newS);
      this.entries.putAll(jt.entries);
      return this;
   }

   public void jointLocal(JointTransform transform, MatrixOperation multiplyFunction) {
      this.entries.put("joint_local_transform", new JointTransform.TransformEntry(multiplyFunction, this.mergeIfExist("joint_local_transform", transform)));
   }

   public void parent(JointTransform transform, MatrixOperation multiplyFunction) {
      this.entries.put("parent", new JointTransform.TransformEntry(multiplyFunction, this.mergeIfExist("parent", transform)));
   }

   public void animationTransform(JointTransform transform, MatrixOperation multiplyFunction) {
      this.entries.put("animation_transform", new JointTransform.TransformEntry(multiplyFunction, this.mergeIfExist("animation_transform", transform)));
   }

   public void frontResult(JointTransform transform, MatrixOperation multiplyFunction) {
      this.entries.put("front_result", new JointTransform.TransformEntry(multiplyFunction, this.mergeIfExist("front_result", transform)));
   }

   public void overwriteRotation(JointTransform transform) {
      this.entries.put("overwrite_rotation", new JointTransform.TransformEntry(OpenMatrix4f::mul, this.mergeIfExist("overwrite_rotation", transform)));
   }

   public JointTransform mergeIfExist(String entryName, JointTransform transform) {
      if (this.entries.containsKey(entryName)) {
         JointTransform.TransformEntry transformEntry = this.entries.get(entryName);
         return mul(transform, transformEntry.transform, transformEntry.multiplyFunction);
      } else {
         return transform;
      }
   }

   public OpenMatrix4f getAnimationBoundMatrix(Joint joint, OpenMatrix4f parentTransform) {
      AnimationTransformEntry animationTransformEntry = new AnimationTransformEntry();

      for (Entry<String, JointTransform.TransformEntry> entry : this.entries.entrySet()) {
         animationTransformEntry.put(entry.getKey(), entry.getValue().transform.toMatrix(), entry.getValue().multiplyFunction);
      }

      animationTransformEntry.put("animation_transform", this.toMatrix(), OpenMatrix4f::mul);
      animationTransformEntry.put("joint_local_transform", joint.getLocalTransform());
      animationTransformEntry.put("parent", parentTransform);
      return animationTransformEntry.getResult();
   }

   public OpenMatrix4f toMatrix() {
      return new OpenMatrix4f().translate(this.translation).mulBack(OpenMatrix4f.fromQuaternion(this.rotation)).scale(this.scale);
   }

   @Override
   public String toString() {
      return String.format("translation:%s, rotation:%s, scale:%s %d entries ", this.translation, this.rotation, this.scale, this.entries.size());
   }

   public static JointTransform interpolateTransform(JointTransform prev, JointTransform next, float progression, JointTransform dest) {
      if (dest == null) {
         dest = empty();
      }

      MathUtils.lerpVector(prev.translation, next.translation, progression, dest.translation);
      MathUtils.lerpQuaternion(prev.rotation, next.rotation, progression, dest.rotation);
      MathUtils.lerpVector(prev.scale, next.scale, progression, dest.scale);
      return dest;
   }

   public static JointTransform interpolate(JointTransform prev, JointTransform next, float progression) {
      return interpolate(prev, next, progression, null);
   }

   public static JointTransform interpolate(JointTransform prev, JointTransform next, float progression, JointTransform dest) {
      if (dest == null) {
         dest = empty();
      }

      if (prev != null && next != null) {
         progression = Mth.m_14036_(progression, 0.0F, 1.0F);
         interpolateTransform(prev, next, progression, dest);
         dest.entries.clear();

         for (Entry<String, JointTransform.TransformEntry> entry : prev.entries.entrySet()) {
            JointTransform transform = next.entries.containsKey(entry.getKey()) ? next.entries.get(entry.getKey()).transform : empty();
            dest.entries
               .put(
                  entry.getKey(),
                  new JointTransform.TransformEntry(
                     entry.getValue().multiplyFunction, interpolateTransform(entry.getValue().transform, transform, progression, null)
                  )
               );
         }

         for (Entry<String, JointTransform.TransformEntry> entry : next.entries.entrySet()) {
            if (!dest.entries.containsKey(entry.getKey())) {
               dest.entries
                  .put(
                     entry.getKey(),
                     new JointTransform.TransformEntry(
                        entry.getValue().multiplyFunction, interpolateTransform(empty(), entry.getValue().transform, progression, null)
                     )
                  );
            }
         }

         return dest;
      } else {
         dest.clearTransform();
         return dest;
      }
   }

   public static JointTransform fromMatrixWithoutScale(OpenMatrix4f matrix) {
      return new JointTransform(matrix.toTranslationVector(), matrix.toQuaternion(), new Vec3f(1.0F, 1.0F, 1.0F));
   }

   public static JointTransform translation(Vec3f vec) {
      return translationRotation(vec, new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F));
   }

   public static JointTransform rotation(Quaternionf quat) {
      return translationRotation(new Vec3f(0.0F, 0.0F, 0.0F), quat);
   }

   public static JointTransform scale(Vec3f vec) {
      return new JointTransform(new Vec3f(0.0F, 0.0F, 0.0F), new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F), vec);
   }

   public static JointTransform fromMatrix(OpenMatrix4f matrix) {
      return new JointTransform(matrix.toTranslationVector(), matrix.toQuaternion(), matrix.toScaleVector());
   }

   public static JointTransform translationRotation(Vec3f vec, Quaternionf quat) {
      return new JointTransform(vec, quat, new Vec3f(1.0F, 1.0F, 1.0F));
   }

   public static JointTransform mul(JointTransform left, JointTransform right, MatrixOperation operation) {
      return fromMatrix(operation.mul(left.toMatrix(), right.toMatrix(), null));
   }

   public static JointTransform fromPrimitives(
      float locX, float locY, float locZ, float quatX, float quatY, float quatZ, float quatW, float scaX, float scaY, float scaZ
   ) {
      return new JointTransform(new Vec3f(locX, locY, locZ), new Quaternionf(quatX, quatY, quatZ, quatW), new Vec3f(scaX, scaY, scaZ));
   }

   public static JointTransform empty() {
      return new JointTransform(new Vec3f(0.0F, 0.0F, 0.0F), new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F), new Vec3f(1.0F, 1.0F, 1.0F));
   }

   public static class TransformEntry {
      public final MatrixOperation multiplyFunction;
      public final JointTransform transform;

      public TransformEntry(MatrixOperation multiplyFunction, JointTransform transform) {
         this.multiplyFunction = multiplyFunction;
         this.transform = transform;
      }
   }
}
