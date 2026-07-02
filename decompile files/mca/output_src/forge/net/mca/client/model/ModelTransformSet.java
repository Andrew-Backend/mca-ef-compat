package forge.net.mca.client.model;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

public interface ModelTransformSet {
   ModelTransformSet.Transformer get(String var1);

   default ModelTransformSet interpolate(ModelTransformSet to, float delta) {
      if (delta <= 0.0F) {
         return this;
      }

      if (delta >= 1.0F) {
         return to;
      }

      ModelTransformSet.Transformer[] components = new ModelTransformSet.Transformer[2];
      ModelTransformSet.Transformer combined = (part, op, scale) -> {
         components[0].applyTo(part, ModelTransformSet.Op.LERP, delta);
         components[1].applyTo(part, ModelTransformSet.Op.LERP, 1.0F - delta);
      };
      return key -> {
         components[0] = (ModelTransformSet.Transformer)Preconditions.checkNotNull(
            this.get(key), "Cannot interpolate because the source set was missing key `" + key + "`"
         );
         components[1] = (ModelTransformSet.Transformer)Preconditions.checkNotNull(
            to.get(key), "Cannot interpolate because the target set was missing key `" + key + "`"
         );
         return combined;
      };
   }

   class Builder {
      private static final float TO_RADIANS = (float) (Math.PI / 180.0);
      private final Map<String, ModelTransformSet.Transformer> transforms = new HashMap<>();

      public ModelTransformSet.Builder with(String key, float x, float y, float z, float pitch, float yaw, float roll) {
         return this.with(key, x, y, z, pitch, yaw, roll, ModelTransformSet.Op.SET, ModelTransformSet.Op.SET);
      }

      public ModelTransformSet.Builder with(String key, float x, float y, float z, float pitch, float yaw, float roll, ModelTransformSet.Op pivot) {
         return this.with(key, x, y, z, pitch, yaw, roll, pivot, ModelTransformSet.Op.SET);
      }

      public ModelTransformSet.Builder rotate(String key, float pitch, float yaw, float roll) {
         return this.rotate(key, pitch, yaw, roll, ModelTransformSet.Op.SET);
      }

      public ModelTransformSet.Builder rotate(String key, float pitch, float yaw, float roll, ModelTransformSet.Op op) {
         return this.with(key, 0.0F, 0.0F, 0.0F, pitch, yaw, roll, ModelTransformSet.Op.KEEP, op);
      }

      public ModelTransformSet.Builder with(
         String key, float x, float y, float z, float pitch, float yaw, float roll, ModelTransformSet.Op pivot, ModelTransformSet.Op rotate
      ) {
         PartPose transform = createTransform(x, y, z, pitch, yaw, roll);
         this.transforms.put(key, (part, op, delta) -> {
            part.f_104200_ = op.apply(delta, part.f_104200_, pivot.apply(delta, part.f_104200_, transform.f_171405_));
            part.f_104201_ = op.apply(delta, part.f_104201_, pivot.apply(delta, part.f_104201_, transform.f_171406_));
            part.f_104202_ = op.apply(delta, part.f_104202_, pivot.apply(delta, part.f_104202_, transform.f_171407_));
            part.f_104203_ = op.apply(delta, part.f_104203_, rotate.apply(delta, part.f_104203_, transform.f_171408_));
            part.f_104204_ = op.apply(delta, part.f_104204_, rotate.apply(delta, part.f_104204_, transform.f_171409_));
            part.f_104205_ = op.apply(delta, part.f_104205_, rotate.apply(delta, part.f_104205_, transform.f_171410_));
         });
         return this;
      }

      public static PartPose createTransform(float x, float y, float z, float pitch, float yaw, float roll) {
         return PartPose.m_171423_(x, y, z, pitch * (float) (Math.PI / 180.0), yaw * (float) (Math.PI / 180.0), roll * (float) (Math.PI / 180.0));
      }

      public ModelTransformSet build() {
         return new HashMap<>(this.transforms)::get;
      }
   }

   interface Op {
      ModelTransformSet.Op KEEP = (delta, a, b) -> a;
      ModelTransformSet.Op SET = (delta, a, b) -> b;
      ModelTransformSet.Op ADD = (delta, a, b) -> a + b;
      ModelTransformSet.Op LERP = Mth::m_14179_;

      float apply(float var1, float var2, float var3);
   }

   interface Transformer {
      default void applyTo(ModelPart part) {
         this.applyTo(part, ModelTransformSet.Op.SET, 1.0F);
      }

      void applyTo(ModelPart var1, ModelTransformSet.Op var2, float var3);
   }
}
