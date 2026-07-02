package yesman.epicfight.api.utils.math;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;

public class AnimationTransformEntry {
   private static final String[] BINDING_PRIORITY = new String[]{
      "parent", "joint_local_transform", "animation_transform", "front_result", "overwrite_rotation"
   };
   private final Map<String, Pair<OpenMatrix4f, MatrixOperation>> matrices = Maps.newHashMap();

   public void put(String entryPosition, OpenMatrix4f matrix) {
      this.put(entryPosition, matrix, OpenMatrix4f::mul);
   }

   public void put(String entryPosition, OpenMatrix4f matrix, MatrixOperation operation) {
      if (this.matrices.containsKey(entryPosition)) {
         Pair<OpenMatrix4f, MatrixOperation> appliedTransform = this.matrices.get(entryPosition);
         OpenMatrix4f result = ((MatrixOperation)appliedTransform.getSecond()).mul((OpenMatrix4f)appliedTransform.getFirst(), matrix, null);
         this.matrices.put(entryPosition, Pair.of(result, operation));
      } else {
         this.matrices.put(entryPosition, Pair.of(new OpenMatrix4f(matrix), operation));
      }
   }

   public OpenMatrix4f getResult() {
      OpenMatrix4f result = new OpenMatrix4f();

      for (String entryName : BINDING_PRIORITY) {
         if (this.matrices.containsKey(entryName)) {
            Pair<OpenMatrix4f, MatrixOperation> pair = this.matrices.get(entryName);
            ((MatrixOperation)pair.getSecond()).mul(result, (OpenMatrix4f)pair.getFirst(), result);
         }
      }

      return result;
   }
}
