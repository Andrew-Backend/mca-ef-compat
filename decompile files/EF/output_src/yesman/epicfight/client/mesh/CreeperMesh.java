package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class CreeperMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart torso = this.getOrLogException(this.parts, "torso");
   public final SkinnedMesh.SkinnedMeshPart legRF = this.getOrLogException(this.parts, "legRF");
   public final SkinnedMesh.SkinnedMeshPart legLF = this.getOrLogException(this.parts, "legLF");
   public final SkinnedMesh.SkinnedMeshPart legRB = this.getOrLogException(this.parts, "legRB");
   public final SkinnedMesh.SkinnedMeshPart legLB = this.getOrLogException(this.parts, "legLB");

   public CreeperMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
