package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class WitherMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart centerHead = this.getOrLogException(this.parts, "centerHead");
   public final SkinnedMesh.SkinnedMeshPart leftHead = this.getOrLogException(this.parts, "leftHead");
   public final SkinnedMesh.SkinnedMeshPart rightHead = this.getOrLogException(this.parts, "rightHead");
   public final SkinnedMesh.SkinnedMeshPart ribcage = this.getOrLogException(this.parts, "ribcage");
   public final SkinnedMesh.SkinnedMeshPart tail = this.getOrLogException(this.parts, "tail");

   public WitherMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
