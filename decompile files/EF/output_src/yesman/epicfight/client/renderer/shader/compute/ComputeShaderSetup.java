package yesman.epicfight.client.renderer.shader.compute;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.IArrayBufferProxy;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public abstract class ComputeShaderSetup {
   protected static final int WORK_GROUP_SIZE = 128;
   public static final OpenMatrix4f[] TOTAL_POSES = OpenMatrix4f.allocateMatrixArray(1000);
   public static final OpenMatrix4f[] TOTAL_NORMALS = OpenMatrix4f.allocateMatrixArray(1000);
   protected static final IArrayBufferProxy POSE_BO = ComputeShaderProvider.createDynamicBuffer(TOTAL_POSES, 16, OpenMatrix4f::store);
   protected final StaticSSBO<ComputeShaderSetup.VertexObj> vObjBO;
   protected final StaticSSBO<ComputeShaderSetup.WeightInfo> jointBO;
   protected final StaticSSBO<ComputeShaderSetup.ElemInfo> elementsBO;
   protected final OutputSSBO outVertexAttrBO;
   protected final IArrayBufferProxy hiddenFlagsBO;
   protected final Integer[] hiddenFlags;
   protected final int arrayObjectId;
   protected final int vcount;

   public ComputeShaderSetup(SkinnedMesh skinnedMesh, int outBufferSize) {
      Map<VertexBuilder, Integer> vertexBuilderMap = new HashMap<>();
      List<ComputeShaderSetup.ElemInfo> elements = new ArrayList<>();
      this.arrayObjectId = GlStateManager._glGenVertexArrays();
      int currentBoundVao = GlStateManager._getInteger(34229);
      int currentBoundVbo = GlStateManager._getInteger(34966);
      GlStateManager._glBindVertexArray(this.arrayObjectId);
      List<Float> uvList = Lists.newArrayList();
      this.hiddenFlags = new Integer[(skinnedMesh.getAllParts().size() + 31) / 32];
      this.hiddenFlagsBO = ComputeShaderProvider.createDynamicBuffer(this.hiddenFlags, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));
      MutableInt partIdx = new MutableInt(0);
      skinnedMesh.getAllParts()
         .forEach(
            skinnedMeshPart -> {
               skinnedMeshPart.initVBO(
                  new ComputeShaderSetup.PartBuffer(skinnedMeshPart.getVertices(), vertexBuilderMap, skinnedMesh.uvs(), uvList, elements, partIdx.intValue())
               );
               partIdx.add(1);
            }
         );
      ComputeShaderSetup.VertexObj[] vertexObjs = new ComputeShaderSetup.VertexObj[vertexBuilderMap.size()];
      List<ComputeShaderSetup.WeightInfo> jointList = new ArrayList<>();
      vertexBuilderMap.forEach(
         (vb, idx) -> {
            int startPos = jointList.size();

            for (int i = 0; i < skinnedMesh.affectingJointCounts()[vb.position]; i++) {
               int jointIndex = skinnedMesh.affectingJointIndices()[vb.position][i];
               int weightIndex = skinnedMesh.affectingWeightIndices()[vb.position][i];
               float weight = skinnedMesh.weights()[weightIndex];
               jointList.add(new ComputeShaderSetup.WeightInfo(jointIndex, weight));
            }

            vertexObjs[idx] = new ComputeShaderSetup.VertexObj(
               skinnedMesh.positions()[vb.position * 3],
               skinnedMesh.positions()[vb.position * 3 + 1],
               skinnedMesh.positions()[vb.position * 3 + 2],
               skinnedMesh.normals()[vb.normal * 3],
               skinnedMesh.normals()[vb.normal * 3 + 1],
               skinnedMesh.normals()[vb.normal * 3 + 2],
               skinnedMesh.uvs()[vb.uv * 2],
               skinnedMesh.uvs()[vb.uv * 2 + 1],
               startPos,
               startPos + skinnedMesh.affectingJointCounts()[vb.position]
            );
         }
      );
      this.initAttachmentSSBO(elements, uvList);
      this.vcount = elements.size();
      this.elementsBO = new StaticSSBO<>(elements, 2, ComputeShaderSetup.ElemInfo::store);
      this.vObjBO = new StaticSSBO<>(Lists.newArrayList(vertexObjs), 10, ComputeShaderSetup.VertexObj::store);
      this.jointBO = new StaticSSBO<>(jointList, 2, ComputeShaderSetup.WeightInfo::store);
      this.outVertexAttrBO = new OutputSSBO((short)outBufferSize, elements.size(), DynamicSSBO.DataMode.STREAM);
      GlStateManager._glBindVertexArray(currentBoundVao);
      GlStateManager._glBindBuffer(34962, currentBoundVbo);
   }

   protected void initAttachmentSSBO(List<ComputeShaderSetup.ElemInfo> elements, List<Float> uvList) {
   }

   public static void setShaderDefaultUniforms(Matrix4f frustumMatrix, ShaderInstance shader, Mode mode, Window window) {
      for (int i = 0; i < 12; i++) {
         int j = RenderSystem.getShaderTexture(i);
         shader.m_173350_("Sampler" + i, j);
      }

      if (shader.f_173308_ != null) {
         shader.f_173308_.m_5679_(frustumMatrix);
      }

      if (shader.f_173309_ != null) {
         shader.f_173309_.m_5679_(RenderSystem.getProjectionMatrix());
      }

      if (shader.f_200956_ != null) {
         shader.f_200956_.m_200759_(RenderSystem.getInverseViewRotationMatrix());
      }

      if (shader.f_173312_ != null) {
         shader.f_173312_.m_5941_(RenderSystem.getShaderColor());
      }

      if (shader.f_267422_ != null) {
         shader.f_267422_.m_5985_(RenderSystem.getShaderGlintAlpha());
      }

      if (shader.f_173315_ != null) {
         shader.f_173315_.m_5985_(RenderSystem.getShaderFogStart());
      }

      if (shader.f_173316_ != null) {
         shader.f_173316_.m_5985_(RenderSystem.getShaderFogEnd());
      }

      if (shader.f_173317_ != null) {
         shader.f_173317_.m_5941_(RenderSystem.getShaderFogColor());
      }

      if (shader.f_202432_ != null) {
         shader.f_202432_.m_142617_(RenderSystem.getShaderFogShape().m_202324_());
      }

      if (shader.f_173310_ != null) {
         shader.f_173310_.m_5679_(RenderSystem.getTextureMatrix());
      }

      if (shader.f_173319_ != null) {
         shader.f_173319_.m_5985_(RenderSystem.getShaderGameTime());
      }

      if (shader.f_173311_ != null) {
         shader.f_173311_.m_7971_(window.m_85441_(), window.m_85442_());
      }

      if (shader.f_173318_ != null && (mode == Mode.LINES || mode == Mode.LINE_STRIP)) {
         shader.f_173318_.m_5985_(RenderSystem.getShaderLineWidth());
      }

      RenderSystem.setupShaderLights(shader);
   }

   static void clearBufferState(VertexFormat vertexFormat) {
      vertexFormat.m_86024_();
   }

   public abstract void bindBufferFormat(VertexFormat var1);

   public abstract void applyComputeShader(PoseStack var1, float var2, float var3, float var4, float var5, int var6, int var7, int var8);

   public abstract void drawWithShader(
      SkinnedMesh var1,
      PoseStack var2,
      MultiBufferSource var3,
      RenderType var4,
      int var5,
      float var6,
      float var7,
      float var8,
      float var9,
      int var10,
      @Nullable Armature var11,
      OpenMatrix4f[] var12
   );

   public abstract int vaoId();

   public abstract int vertexCount();

   public void destroyBuffers() {
      this.vObjBO.close();
      this.jointBO.close();
      this.elementsBO.close();
      this.hiddenFlagsBO.close();
      RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
   }

   protected void draw(
      PoseStack poseStack, RenderType renderType, Matrix4f frustumMatrix, float r, float g, float b, float a, int overlay, int packedLight, int joints
   ) {
      renderType.m_110185_();
      Mode mode = renderType.m_173186_();
      ShaderInstance shader = RenderSystem.getShader();
      VertexFormat format = shader.m_173364_();
      this.bindBufferFormat(format);
      setShaderDefaultUniforms(frustumMatrix, shader, mode, Minecraft.m_91087_().m_91268_());
      shader.m_173363_();
      this.applyComputeShader(poseStack, r, g, b, a, overlay, packedLight, joints);
      GL20C.glUseProgram(RenderSystem.getShader().m_108943_());
      GL11C.glDrawArrays(Mode.TRIANGLES.f_166946_, 0, this.vcount);
      RenderSystem.getShader().m_173362_();
      renderType.m_110188_();
      format.m_86024_();
   }

   public interface BufferUploadable {
      void store(FloatBuffer var1);
   }

   public record ElemInfo(int poolId, int partId) implements ComputeShaderSetup.BufferUploadable {
      @Override
      public void store(FloatBuffer buffer) {
         buffer.put(Float.intBitsToFloat(this.poolId));
         buffer.put(Float.intBitsToFloat(this.partId));
      }
   }

   public interface MeshPartBuffer {
      int vboId();

      int partIdx();
   }

   public static class PartBuffer implements ComputeShaderSetup.MeshPartBuffer {
      private final int partIdx;

      public PartBuffer(
         List<VertexBuilder> vertexBuilders,
         Map<VertexBuilder, Integer> vertexBuilderMap,
         float[] uvs,
         List<Float> uvList,
         List<ComputeShaderSetup.ElemInfo> elements,
         int partIdx
      ) {
         this.partIdx = partIdx;

         for (VertexBuilder vb : vertexBuilders) {
            if (!vertexBuilderMap.containsKey(vb)) {
               int next = vertexBuilderMap.size();
               vertexBuilderMap.put(vb, next);
               uvList.add(uvs[vb.uv * 2]);
               uvList.add(uvs[vb.uv * 2 + 1]);
            }

            int vertexPoolIndex = vertexBuilderMap.get(vb);
            elements.add(new ComputeShaderSetup.ElemInfo(vertexPoolIndex, partIdx));
         }
      }

      @Override
      public int vboId() {
         return -1;
      }

      @Override
      public int partIdx() {
         return this.partIdx;
      }
   }

   public record VertexObj(float px, float py, float pz, float nx, float ny, float nz, float u, float v, int jts, int jte)
      implements ComputeShaderSetup.BufferUploadable {
      @Override
      public void store(FloatBuffer floatBuffer) {
         floatBuffer.put(this.px);
         floatBuffer.put(this.py);
         floatBuffer.put(this.pz);
         floatBuffer.put(this.nx);
         floatBuffer.put(this.ny);
         floatBuffer.put(this.nz);
         floatBuffer.put(this.u);
         floatBuffer.put(this.v);
         floatBuffer.put(Float.intBitsToFloat(this.jts));
         floatBuffer.put(Float.intBitsToFloat(this.jte));
      }
   }

   public record WeightInfo(int jtId, float weight) implements ComputeShaderSetup.BufferUploadable {
      @Override
      public void store(FloatBuffer buffer) {
         buffer.put(Float.intBitsToFloat(this.jtId));
         buffer.put(this.weight);
      }
   }
}
