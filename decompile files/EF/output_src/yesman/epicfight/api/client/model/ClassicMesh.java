package yesman.epicfight.api.client.model;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.main.EpicFightMod;

public class ClassicMesh extends StaticMesh<ClassicMesh.ClassicMeshPart> {
   public ClassicMesh(
      Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> partBuilders, ClassicMesh parent, Mesh.RenderProperties properties
   ) {
      super(arrayMap, partBuilders, parent, properties);
   }

   @Override
   protected Map<String, ClassicMesh.ClassicMeshPart> createModelPart(Map<MeshPartDefinition, List<VertexBuilder>> partBuilders) {
      Map<String, ClassicMesh.ClassicMeshPart> parts = Maps.newHashMap();
      partBuilders.forEach(
         (partDefinition, vertexBuilder) -> parts.put(
            partDefinition.partName(),
            new ClassicMesh.ClassicMeshPart(
               (List<VertexBuilder>)vertexBuilder, partDefinition.renderProperties(), partDefinition.getModelPartAnimationProvider()
            )
         )
      );
      return parts;
   }

   protected ClassicMesh.ClassicMeshPart getOrLogException(Map<String, ClassicMesh.ClassicMeshPart> parts, String name) {
      if (!parts.containsKey(name)) {
         EpicFightMod.LOGGER.debug("Can not find the mesh part named " + name + " in " + this.getClass().getCanonicalName());
         return null;
      } else {
         return parts.get(name);
      }
   }

   @Override
   public void draw(
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      Mesh.DrawingFunction drawingFunction,
      int packedLight,
      float r,
      float g,
      float b,
      float a,
      int overlay
   ) {
      for (ClassicMesh.ClassicMeshPart part : this.parts.values()) {
         part.draw(poseStack, vertexConsumer, drawingFunction, packedLight, r, g, b, a, overlay);
      }
   }

   @Override
   public void drawPosed(
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
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
      this.draw(poseStack, vertexConsumer, drawingFunction, packedLight, r, g, b, a, overlay);
   }

   public class ClassicMeshPart extends MeshPart {
      protected static final Vector4f POSITION = new Vector4f();
      protected static final Vector3f NORMAL = new Vector3f();

      public ClassicMeshPart(
         List<VertexBuilder> verticies, @Nullable Mesh.RenderProperties renderProperties, @Nullable Supplier<OpenMatrix4f> vanillaPartTracer
      ) {
         super(verticies, renderProperties, vanillaPartTracer);
      }

      @Override
      public void draw(
         PoseStack poseStack,
         VertexConsumer bufferbuilder,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a,
         int overlay
      ) {
         if (!this.isHidden()) {
            Vector4f color = this.getColor(r, g, b, a);
            poseStack.m_85836_();
            OpenMatrix4f transform = this.getVanillaPartTransform();
            if (transform != null) {
               poseStack.m_252931_(OpenMatrix4f.exportToMojangMatrix(transform));
            }

            Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
            Matrix3f matrix3f = poseStack.m_85850_().m_252943_();

            for (VertexBuilder vi : this.getVertices()) {
               ClassicMesh.this.getVertexPosition(vi.position, POSITION);
               ClassicMesh.this.getVertexNormal(vi.normal, NORMAL);
               POSITION.mul(matrix4f);
               NORMAL.mul(matrix3f);
               drawingFunction.draw(
                  bufferbuilder,
                  POSITION.x(),
                  POSITION.y(),
                  POSITION.z(),
                  NORMAL.x(),
                  NORMAL.y(),
                  NORMAL.z(),
                  packedLight,
                  color.x,
                  color.y,
                  color.z,
                  color.w,
                  ClassicMesh.this.uvs[vi.uv * 2],
                  ClassicMesh.this.uvs[vi.uv * 2 + 1],
                  overlay
               );
            }

            poseStack.m_85849_();
         }
      }
   }
}
