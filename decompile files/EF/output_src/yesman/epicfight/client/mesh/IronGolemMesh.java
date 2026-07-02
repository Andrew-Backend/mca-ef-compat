package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class IronGolemMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart chest = this.getOrLogException(this.parts, "chest");
   public final SkinnedMesh.SkinnedMeshPart core = this.getOrLogException(this.parts, "core");
   public final SkinnedMesh.SkinnedMeshPart leftArm = this.getOrLogException(this.parts, "leftArm");
   public final SkinnedMesh.SkinnedMeshPart rightArm = this.getOrLogException(this.parts, "rightArm");
   public final SkinnedMesh.SkinnedMeshPart leftLeg = this.getOrLogException(this.parts, "leftLeg");
   public final SkinnedMesh.SkinnedMeshPart rightLeg = this.getOrLogException(this.parts, "rightLeg");

   public IronGolemMesh(
      Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties
   ) {
      super(arrayMap, parts, parent, properties);
   }
}
