package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class SpiderMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart middleStomach = this.getOrLogException(this.parts, "middleStomach");
   public final SkinnedMesh.SkinnedMeshPart bottomStomach = this.getOrLogException(this.parts, "bottomStomach");
   public final SkinnedMesh.SkinnedMeshPart leftLeg1 = this.getOrLogException(this.parts, "leftLeg1");
   public final SkinnedMesh.SkinnedMeshPart leftLeg2 = this.getOrLogException(this.parts, "leftLeg2");
   public final SkinnedMesh.SkinnedMeshPart leftLeg3 = this.getOrLogException(this.parts, "leftLeg3");
   public final SkinnedMesh.SkinnedMeshPart leftLeg4 = this.getOrLogException(this.parts, "leftLeg4");
   public final SkinnedMesh.SkinnedMeshPart rightLeg1 = this.getOrLogException(this.parts, "rightLeg1");
   public final SkinnedMesh.SkinnedMeshPart rightLeg2 = this.getOrLogException(this.parts, "rightLeg2");
   public final SkinnedMesh.SkinnedMeshPart rightLeg3 = this.getOrLogException(this.parts, "rightLeg3");
   public final SkinnedMesh.SkinnedMeshPart rightLeg4 = this.getOrLogException(this.parts, "rightLeg4");

   public SpiderMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
