package yesman.epicfight.client.renderer.shader.compute.iris;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.ComputeShaderSetup;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class IrisComputeShaderSetup extends ComputeShaderSetup {
   protected StaticSSBO<Float> midUVBO;
   private static final Matrix4f IDENTITY4X4 = new Matrix4f();
   private static final Matrix3f IDENTITY3X3 = new Matrix3f();

   public IrisComputeShaderSetup(SkinnedMesh skinnedMesh) {
      super(skinnedMesh, 15);
   }

   @Override
   protected void initAttachmentSSBO(List<ComputeShaderSetup.ElemInfo> elements, List<Float> uvList) {
      List<Float> midUVList = Lists.newArrayList();
      float[] midUVs = new float[elements.size() / 3 * 2];

      for (int i = 0; i < elements.size(); i++) {
         int vertPoolIdx = elements.get(i).poolId();
         float u = uvList.get(vertPoolIdx * 2);
         float v = uvList.get(vertPoolIdx * 2 + 1);
         int faceIdx = i / 3;
         if (i % 3 == 0) {
            midUVs[faceIdx * 2] = u / 3.0F;
            midUVs[faceIdx * 2 + 1] = v / 3.0F;
         } else {
            midUVs[faceIdx * 2] = midUVs[faceIdx * 2] + u / 3.0F;
            midUVs[faceIdx * 2 + 1] = midUVs[faceIdx * 2 + 1] + v / 3.0F;
         }
      }

      for (int i = 0; i < elements.size(); i++) {
         int faceIdx = i / 3;
         midUVList.add(midUVs[faceIdx * 2]);
         midUVList.add(midUVs[faceIdx * 2 + 1]);
      }

      this.midUVBO = new StaticSSBO<>(midUVList, 1, (vx, b) -> b.put(vx));
   }

   @Override
   public void bindBufferFormat(VertexFormat vertexFormat) {
      ImmutableList<VertexFormatElement> elems = vertexFormat.m_86023_();
      GL15C.glBindBuffer(34962, this.outVertexAttrBO.glSSBO);
      int midUvPos = -1;

      for (int i = 0; i < elems.size(); i++) {
         VertexFormatElement elem = (VertexFormatElement)elems.get(i);
         if (elem == DefaultVertexFormat.f_85804_) {
            GL20C.glVertexAttribPointer(i, 3, 5126, false, 60, 0L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_166849_) {
            GL20C.glVertexAttribPointer(i, 2, 5126, false, 60, 28L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85805_) {
            GL20C.glVertexAttribPointer(i, 4, 5126, true, 60, 12L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85809_) {
            GL20C.glVertexAttribPointer(i, 3, 5120, true, 60, 36L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85807_) {
            GL30C.glVertexAttribIPointer(i, 2, 5123, 60, 40L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85808_) {
            GL30C.glVertexAttribIPointer(i, 2, 5123, 60, 44L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == IrisVertexFormats.ENTITY_ID_ELEMENT) {
            GL30C.glVertexAttribIPointer(i, 3, 5123, 60, 48L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
            midUvPos = i;
         } else if (elem == IrisVertexFormats.TANGENT_ELEMENT) {
            GL20C.glVertexAttribPointer(i, 4, 5120, false, 60, 56L);
            GL20C.glEnableVertexAttribArray(i);
         }
      }

      if (midUvPos >= 0) {
         GL15C.glBindBuffer(34962, this.midUVBO.glSSBO);
         GL20C.glVertexAttribPointer(midUvPos, 2, 5126, false, 0, 0L);
         GL20C.glEnableVertexAttribArray(midUvPos);
      }

      GL15C.glBindBuffer(34962, 0);
   }

   @Override
   public void applyComputeShader(PoseStack poseStack, float r, float g, float b, float a, int overlay, int light, int jointCount) {
      ComputeProgram shader = ComputeShaderProvider.meshComputeIris;
      shader.useProgram();
      shader.getUniform("colorIn").uploadVec4(r, g, b, a);
      shader.getUniform("uv1In").uploadUnsignedInt(overlay);
      shader.getUniform("uv2In").uploadUnsignedInt(light);
      shader.getUniform("part_offset").uploadUnsignedInt(jointCount);
      shader.getUniform("entity_id_0").uploadUnsignedInt(getEntity() << 16 & -65536 | getBlock() & 65535);
      shader.getUniform("entity_id_1").uploadUnsignedInt(getItem() << 16);
      if (!Iris.getIrisConfig().areShadersEnabled()) {
         shader.getUniform("model_view_matrix").uploadMatrix4f(poseStack.m_85850_().m_252922_());
         shader.getUniform("normal_matrix").uploadMatrix3f(poseStack.m_85850_().m_252943_());
      } else {
         shader.getUniform("model_view_matrix").uploadMatrix4f(IDENTITY4X4);
         shader.getUniform("normal_matrix").uploadMatrix3f(IDENTITY3X3);
      }

      ComputeShaderSetup.POSE_BO.bindBufferBase(0);
      this.elementsBO.bindBufferBase(1);
      this.vObjBO.bindBufferBase(2);
      this.jointBO.bindBufferBase(3);
      this.hiddenFlagsBO.bindBufferBase(4);
      this.outVertexAttrBO.bindBufferBase(5);
      int workGroupCount = (this.vcount / 3 + 128 - 1) / 128;
      shader.dispatch(workGroupCount, 1, 1);
      shader.waitBarriers();
      ComputeShaderSetup.POSE_BO.unbind();
      this.elementsBO.unbind();
      this.vObjBO.unbind();
      this.jointBO.unbind();
      this.hiddenFlagsBO.unbind();
      this.outVertexAttrBO.unbind();
   }

   @Override
   public void drawWithShader(
      SkinnedMesh skinnedMesh,
      PoseStack poseStack,
      MultiBufferSource buffers,
      RenderType renderType,
      int packedLight,
      float r,
      float g,
      float b,
      float a,
      int overlay,
      @Nullable Armature armature,
      OpenMatrix4f[] poses
   ) {
      for (int i = 0; i < poses.length; i++) {
         TOTAL_POSES[i].load(poses[i]);
         if (armature != null) {
            TOTAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
         }
      }

      Arrays.fill(this.hiddenFlags, Integer.valueOf(0));

      for (SkinnedMesh.SkinnedMeshPart part : skinnedMesh.getAllParts()) {
         OpenMatrix4f mat = part.getVanillaPartTransform();
         if (mat == null) {
            mat = OpenMatrix4f.IDENTITY;
         }

         TOTAL_POSES[poses.length + part.getPartVBO().partIdx()].load(mat);
         if (part.isHidden()) {
            int flagPos = part.getPartVBO().partIdx() / 32;
            int flagOffset = part.getPartVBO().partIdx() % 32;
            int flag = this.hiddenFlags[flagPos];
            this.hiddenFlags[flagPos] = flag | (part.isHidden() ? 1 : 0) << flagOffset;
         }
      }

      this.hiddenFlagsBO.updateAll();
      POSE_BO.updateFromTo(0, poses.length + skinnedMesh.getAllParts().size());
      int currentBoundVao = GlStateManager._getInteger(34229);
      int currentBoundVbo = GlStateManager._getInteger(34966);
      GlStateManager._glBindVertexArray(this.arrayObjectId);
      Matrix4f frustumMatrix = Iris.getIrisConfig().areShadersEnabled() ? poseStack.m_85850_().m_252922_() : RenderSystem.getModelViewMatrix();
      this.draw(poseStack, renderType, frustumMatrix, r, g, b, a, overlay, packedLight, poses.length);
      if (buffers instanceof OutlineBufferSource outlineBufferSource) {
         renderType.m_7280_()
            .ifPresent(
               outlineRendertype -> this.draw(
                  poseStack,
                  outlineRendertype,
                  frustumMatrix,
                  outlineBufferSource.f_109922_ / 255.0F,
                  outlineBufferSource.f_109923_ / 255.0F,
                  outlineBufferSource.f_109924_ / 255.0F,
                  outlineBufferSource.f_109925_ / 255.0F,
                  overlay,
                  packedLight,
                  poses.length
               )
            );
      }

      GlStateManager._glBindVertexArray(currentBoundVao);
      GlStateManager._glBindBuffer(34962, currentBoundVbo);
   }

   @Override
   public int vaoId() {
      return this.arrayObjectId;
   }

   @Override
   public void destroyBuffers() {
      this.midUVBO.close();
      super.destroyBuffers();
   }

   @Override
   public int vertexCount() {
      return this.vcount;
   }

   static short getBlock() {
      return (short)CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
   }

   static short getEntity() {
      return (short)CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
   }

   static short getItem() {
      return (short)CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
   }
}
