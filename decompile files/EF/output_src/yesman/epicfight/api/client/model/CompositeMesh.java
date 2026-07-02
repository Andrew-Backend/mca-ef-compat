package yesman.epicfight.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public class CompositeMesh implements Mesh, SoftBodyTranslatable {
   private final StaticMesh<?> staticMesh;
   private final SoftBodyTranslatable softBodyMesh;

   public CompositeMesh(StaticMesh<?> staticMesh, SoftBodyTranslatable softBodyMesh) {
      this.staticMesh = staticMesh;
      this.softBodyMesh = softBodyMesh;
   }

   @Override
   public void initialize() {
      this.staticMesh.initialize();
   }

   @Override
   public void draw(
      PoseStack poseStack, VertexConsumer bufferBuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay
   ) {
      this.staticMesh.draw(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
      this.softBodyMesh.getOriginalMesh().draw(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
   }

   @Override
   public void drawPosed(
      PoseStack poseStack,
      VertexConsumer bufferBuilder,
      Mesh.DrawingFunction drawingFunction,
      int packedLight,
      float r,
      float g,
      float b,
      float a,
      int overlay,
      Armature armature,
      OpenMatrix4f[] poses
   ) {
      this.staticMesh.drawPosed(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay, armature, poses);
      this.softBodyMesh.getOriginalMesh().drawPosed(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay, armature, poses);
   }

   @Override
   public boolean canStartSoftBodySimulation() {
      return this.softBodyMesh.canStartSoftBodySimulation();
   }

   public ClothSimulator.ClothObject createSimulationData(
      @Nullable SoftBodyTranslatable provider, ClothSimulatable simOwner, ClothSimulator.ClothObjectBuilder simBuilder
   ) {
      return this.softBodyMesh.createSimulationData(this, simOwner, simBuilder);
   }

   @Nullable
   public StaticMesh<?> getStaticMesh() {
      return this.staticMesh;
   }

   @Override
   public StaticMesh<?> getOriginalMesh() {
      return (StaticMesh<?>)this.softBodyMesh;
   }

   @Override
   public void putSoftBodySimulationInfo(Map<String, SoftBodyTranslatable.ClothSimulationInfo> sofyBodySimulationInfo) {
      this.softBodyMesh.putSoftBodySimulationInfo(sofyBodySimulationInfo);
   }

   @Override
   public Map<String, SoftBodyTranslatable.ClothSimulationInfo> getSoftBodySimulationInfo() {
      return this.softBodyMesh.getSoftBodySimulationInfo();
   }
}
