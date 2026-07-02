package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class EndermanMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart headTop = this.getOrLogException(this.parts, "headTop");
   public final SkinnedMesh.SkinnedMeshPart headBottom = this.getOrLogException(this.parts, "headBottom");
   public final SkinnedMesh.SkinnedMeshPart torso = this.getOrLogException(this.parts, "torso");
   public final SkinnedMesh.SkinnedMeshPart leftArm = this.getOrLogException(this.parts, "leftArm");
   public final SkinnedMesh.SkinnedMeshPart rightArm = this.getOrLogException(this.parts, "rightArm");
   public final SkinnedMesh.SkinnedMeshPart leftLeg = this.getOrLogException(this.parts, "leftLeg");
   public final SkinnedMesh.SkinnedMeshPart rightLeg = this.getOrLogException(this.parts, "rightLeg");

   public EndermanMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
