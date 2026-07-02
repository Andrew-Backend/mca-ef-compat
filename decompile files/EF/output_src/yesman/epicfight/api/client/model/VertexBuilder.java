package yesman.epicfight.api.client.model;

import com.google.common.collect.Lists;
import java.util.List;

public class VertexBuilder {
   public final int position;
   public final int uv;
   public final int normal;

   public static List<VertexBuilder> create(int[] drawingIndices) {
      List<VertexBuilder> vertexIndicators = Lists.newArrayList();

      for (int i = 0; i < drawingIndices.length / 3; i++) {
         int k = i * 3;
         int position = drawingIndices[k];
         int uv = drawingIndices[k + 1];
         int normal = drawingIndices[k + 2];
         VertexBuilder vi = new VertexBuilder(position, uv, normal);
         vertexIndicators.add(vi);
      }

      return vertexIndicators;
   }

   public VertexBuilder(int position, int uv, int normal) {
      this.position = position;
      this.uv = uv;
      this.normal = normal;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof VertexBuilder vb) ? false : this.position == vb.position && this.uv == vb.uv && this.normal == vb.normal;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + this.position;
      result = 31 * result + this.uv;
      return 31 * result + this.normal;
   }
}
