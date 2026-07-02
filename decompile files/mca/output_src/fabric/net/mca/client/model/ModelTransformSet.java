package fabric.net.mca.client.model;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_3532;
import net.minecraft.class_5603;
import net.minecraft.class_630;

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
         class_5603 transform = createTransform(x, y, z, pitch, yaw, roll);
         this.transforms.put(key, (part, op, delta) -> {
            part.field_3657 = op.apply(delta, part.field_3657, pivot.apply(delta, part.field_3657, transform.field_27702));
            part.field_3656 = op.apply(delta, part.field_3656, pivot.apply(delta, part.field_3656, transform.field_27703));
            part.field_3655 = op.apply(delta, part.field_3655, pivot.apply(delta, part.field_3655, transform.field_27704));
            part.field_3654 = op.apply(delta, part.field_3654, rotate.apply(delta, part.field_3654, transform.field_27705));
            part.field_3675 = op.apply(delta, part.field_3675, rotate.apply(delta, part.field_3675, transform.field_27706));
            part.field_3674 = op.apply(delta, part.field_3674, rotate.apply(delta, part.field_3674, transform.field_27707));
         });
         return this;
      }

      public static class_5603 createTransform(float x, float y, float z, float pitch, float yaw, float roll) {
         return class_5603.method_32091(x, y, z, pitch * (float) (Math.PI / 180.0), yaw * (float) (Math.PI / 180.0), roll * (float) (Math.PI / 180.0));
      }

      public ModelTransformSet build() {
         return new HashMap<>(this.transforms)::get;
      }
   }

   interface Op {
      ModelTransformSet.Op KEEP = (delta, a, b) -> a;
      ModelTransformSet.Op SET = (delta, a, b) -> b;
      ModelTransformSet.Op ADD = (delta, a, b) -> a + b;
      ModelTransformSet.Op LERP = class_3532::method_16439;

      float apply(float var1, float var2, float var3);
   }

   interface Transformer {
      default void applyTo(class_630 part) {
         this.applyTo(part, ModelTransformSet.Op.SET, 1.0F);
      }

      void applyTo(class_630 var1, ModelTransformSet.Op var2, float var3);
   }
}
