package yesman.epicfight.api.animation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;

public class AnimationClip {
   public static final AnimationClip EMPTY_CLIP = new AnimationClip();
   protected Map<String, TransformSheet> jointTransforms = new HashMap<>();
   protected float clipTime;
   protected float[] bakedTimes;

   public void addJointTransform(String jointName, TransformSheet sheet) {
      this.jointTransforms.put(jointName, sheet);
      this.bakedTimes = null;
   }

   public boolean hasJointTransform(String jointName) {
      return this.jointTransforms.containsKey(jointName);
   }

   public void bakeKeyframes() {
      Set<Float> timestamps = new HashSet<>();
      this.jointTransforms.values().forEach(transformSheet -> transformSheet.forEach((i, keyframe) -> timestamps.add(keyframe.time())));
      float[] bakedTimestamps = new float[timestamps.size()];
      MutableInt mi = new MutableInt(0);
      timestamps.stream().sorted().toList().forEach(f -> bakedTimestamps[mi.getAndAdd(1)] = f);
      Map<String, TransformSheet> bakedJointTransforms = new HashMap<>();
      this.jointTransforms.forEach((jointName, transformSheet) -> bakedJointTransforms.put(jointName, transformSheet.createInterpolated(bakedTimestamps)));
      this.jointTransforms = bakedJointTransforms;
      this.bakedTimes = bakedTimestamps;
   }

   public void setBaked() {
      TransformSheet transformSheet = this.jointTransforms.get("Root");
      if (transformSheet != null) {
         this.bakedTimes = new float[transformSheet.getKeyframes().length];

         for (int i = 0; i < transformSheet.getKeyframes().length; i++) {
            this.bakedTimes[i] = transformSheet.getKeyframes()[i].time();
         }
      }
   }

   public TransformSheet getJointTransform(String jointName) {
      return this.jointTransforms.get(jointName);
   }

   public final Pose getPoseInTime(float time) {
      Pose pose = new Pose();
      if (time < 0.0F) {
         time += this.clipTime;
      }

      if (this.bakedTimes != null && this.bakedTimes.length > 0) {
         int begin = 0;
         int end = this.bakedTimes.length - 1;

         while (end - begin > 1) {
            int i = begin + (end - begin) / 2;
            if (this.bakedTimes[i] <= time && this.bakedTimes[i + 1] > time) {
               begin = i;
               end = i + 1;
               break;
            }

            if (this.bakedTimes[i] > time) {
               end = i;
            } else if (this.bakedTimes[i + 1] <= time) {
               begin = i;
            }
         }

         float delta = Mth.m_14036_((time - this.bakedTimes[begin]) / (this.bakedTimes[end] - this.bakedTimes[begin]), 0.0F, 1.0F);
         TransformSheet.InterpolationInfo iInfo = new TransformSheet.InterpolationInfo(begin, end, delta);

         for (String jointName : this.jointTransforms.keySet()) {
            pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(iInfo));
         }
      } else {
         for (String jointName : this.jointTransforms.keySet()) {
            pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(time));
         }
      }

      return pose;
   }

   public Map<String, TransformSheet> getJointTransforms() {
      return Collections.unmodifiableMap(this.jointTransforms);
   }

   public void reset() {
      this.jointTransforms.clear();
      this.bakedTimes = null;
   }

   public void setClipTime(float clipTime) {
      this.clipTime = clipTime;
   }

   public float getClipTime() {
      return this.clipTime;
   }
}
