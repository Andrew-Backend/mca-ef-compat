package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class VexMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart torso = this.getOrLogException(this.parts, "torso");
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart tail = this.getOrLogException(this.parts, "tail");
   public final SkinnedMesh.SkinnedMeshPart leftArm = this.getOrLogException(this.parts, "leftArm");
   public final SkinnedMesh.SkinnedMeshPart rightArm = this.getOrLogException(this.parts, "rightArm");
   public final SkinnedMesh.SkinnedMeshPart leftWing = this.getOrLogException(this.parts, "leftWing");
   public final SkinnedMesh.SkinnedMeshPart rightWing = this.getOrLogException(this.parts, "rightWing");

   public VexMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
