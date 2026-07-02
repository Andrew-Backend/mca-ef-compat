package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class DragonMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart neck = this.getOrLogException(this.parts, "neck");
   public final SkinnedMesh.SkinnedMeshPart torso = this.getOrLogException(this.parts, "torso");
   public final SkinnedMesh.SkinnedMeshPart leftLegFront = this.getOrLogException(this.parts, "leftLegFront");
   public final SkinnedMesh.SkinnedMeshPart rightLegFront = this.getOrLogException(this.parts, "rightLegFront");
   public final SkinnedMesh.SkinnedMeshPart leftLegBack = this.getOrLogException(this.parts, "leftLegBack");
   public final SkinnedMesh.SkinnedMeshPart rightLegBack = this.getOrLogException(this.parts, "rightLegBack");
   public final SkinnedMesh.SkinnedMeshPart leftWing = this.getOrLogException(this.parts, "leftWing");
   public final SkinnedMesh.SkinnedMeshPart rightWing = this.getOrLogException(this.parts, "rightWing");
   public final SkinnedMesh.SkinnedMeshPart tail = this.getOrLogException(this.parts, "tail");

   public DragonMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }
}
