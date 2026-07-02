package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class HumanoidMesh extends SkinnedMesh {
   public final SkinnedMesh.SkinnedMeshPart head = this.getOrLogException(this.parts, "head");
   public final SkinnedMesh.SkinnedMeshPart torso = this.getOrLogException(this.parts, "torso");
   public final SkinnedMesh.SkinnedMeshPart leftArm = this.getOrLogException(this.parts, "leftArm");
   public final SkinnedMesh.SkinnedMeshPart rightArm = this.getOrLogException(this.parts, "rightArm");
   public final SkinnedMesh.SkinnedMeshPart leftLeg = this.getOrLogException(this.parts, "leftLeg");
   public final SkinnedMesh.SkinnedMeshPart rightLeg = this.getOrLogException(this.parts, "rightLeg");
   public final SkinnedMesh.SkinnedMeshPart hat = this.getOrLogException(this.parts, "hat");
   public final SkinnedMesh.SkinnedMeshPart jacket = this.getOrLogException(this.parts, "jacket");
   public final SkinnedMesh.SkinnedMeshPart leftSleeve = this.getOrLogException(this.parts, "leftSleeve");
   public final SkinnedMesh.SkinnedMeshPart rightSleeve = this.getOrLogException(this.parts, "rightSleeve");
   public final SkinnedMesh.SkinnedMeshPart leftPants = this.getOrLogException(this.parts, "leftPants");
   public final SkinnedMesh.SkinnedMeshPart rightPants = this.getOrLogException(this.parts, "rightPants");

   public HumanoidMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, Mesh.RenderProperties properties) {
      super(arrayMap, parts, parent, properties);
   }

   public AssetAccessor<? extends SkinnedMesh> getHumanoidArmorModel(EquipmentSlot slot) {
      switch (slot) {
         case HEAD:
            return Meshes.HELMET;
         case CHEST:
            return Meshes.CHESTPLATE;
         case LEGS:
            return Meshes.LEGGINS;
         case FEET:
            return Meshes.BOOTS;
         default:
            return null;
      }
   }
}
