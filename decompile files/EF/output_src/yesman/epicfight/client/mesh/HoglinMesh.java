package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class HoglinMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart body = this.getOrLogException(this.parts, "body");
   public final SkinnedMesh.SkinnedMeshPart leftFrontLeg = this.getOrLogException(this.parts, "leftFrontLeg");
   public final SkinnedMesh.SkinnedMeshPart rightFrontLeg = this.getOrLogException(this.parts, "rightFrontLeg");
   public final SkinnedMesh.SkinnedMeshPart leftBackLeg = this.getOrLogException(this.parts, "leftBackLeg");
   public final SkinnedMesh.SkinnedMeshPart rightBackLeg = this.getOrLogException(this.parts, "rightBackLeg");

   public HoglinMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
