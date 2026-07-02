package yesman.epicfight.client.renderer.shader.compute;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class VanillaComputeShaderSetup extends ComputeShaderSetup {
   public VanillaComputeShaderSetup(SkinnedMesh skinnedMesh) {
      super(skinnedMesh, 12);
   }

   @Override
   public void bindBufferFormat(VertexFormat vertexFormat) {
      ImmutableList<VertexFormatElement> elems = vertexFormat.m_86023_();
      GL15C.glBindBuffer(34962, this.outVertexAttrBO.glSSBO);

      for (int i = 0; i < elems.size(); i++) {
         VertexFormatElement elem = (VertexFormatElement)elems.get(i);
         if (elem == DefaultVertexFormat.f_85804_) {
            GL20C.glVertexAttribPointer(i, 3, 5126, false, 48, 0L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_166849_) {
            GL20C.glVertexAttribPointer(i, 2, 5126, false, 48, 28L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85805_) {
            GL20C.glVertexAttribPointer(i, 4, 5126, true, 48, 12L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85809_) {
            GL20C.glVertexAttribPointer(i, 3, 5120, true, 48, 36L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85807_) {
            GL30C.glVertexAttribIPointer(i, 2, 5123, 48, 40L);
            GL20C.glEnableVertexAttribArray(i);
         } else if (elem == DefaultVertexFormat.f_85808_) {
            GL30C.glVertexAttribIPointer(i, 2, 5123, 48, 44L);
            GL20C.glEnableVertexAttribArray(i);
         }
      }

      GL15C.glBindBuffer(34962, 0);
   }

   @Override
   public void applyComputeShader(PoseStack poseStack, float r, float g, float b, float a, int overlay, int light, int jointCount) {
      ComputeProgram shader = ComputeShaderProvider.meshComputeVanilla;
      shader.useProgram();
      shader.getUniform("colorIn").uploadVec4(r, g, b, a);
      shader.getUniform("uv1In").uploadUnsignedInt(overlay);
      shader.getUniform("uv2In").uploadUnsignedInt(light);
      shader.getUniform("part_offset").uploadUnsignedInt(jointCount);
      shader.getUniform("model_view_matrix").uploadMatrix4f(poseStack.m_85850_().m_252922_());
      shader.getUniform("normal_matrix").uploadMatrix3f(poseStack.m_85850_().m_252943_());
      ComputeShaderSetup.POSE_BO.bindBufferBase(0);
      this.elementsBO.bindBufferBase(1);
      this.vObjBO.bindBufferBase(2);
      this.jointBO.bindBufferBase(3);
      this.hiddenFlagsBO.bindBufferBase(4);
      this.outVertexAttrBO.bindBufferBase(5);
      int workGroupCount = (this.vcount + 128 - 1) / 128;
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
      this.draw(poseStack, renderType, RenderSystem.getModelViewMatrix(), r, g, b, a, overlay, packedLight, poses.length);
      if (buffers instanceof OutlineBufferSource outlineBufferSource) {
         renderType.m_7280_()
            .ifPresent(
               outlineRendertype -> this.draw(
                  poseStack,
                  outlineRendertype,
                  RenderSystem.getModelViewMatrix(),
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
   public int vertexCount() {
      return this.vcount;
   }
}
