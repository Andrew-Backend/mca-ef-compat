package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class SodiumFakeBlockRenderer implements FakeBlockRenderer {
   private static final Direction[] DIRECTIONS = Direction.values();

   @Override
   public void render(Camera camera, PoseStack poseStack, MultiBufferSource buffers, Level level, BlockPos bp, float r, float g, float b, float a) {
      BlockState bs = level.m_8055_(bp);
      if (bs.m_60799_() == RenderShape.MODEL) {
         RandomSource randomsource = RandomSource.m_216327_();
         long seed = bs.m_60726_(bp);
         randomsource.m_188584_(seed);
         Vec3 offset = bs.m_271730_() ? bs.m_60824_(level, bp) : Vec3.f_82478_;
         VertexConsumer buffer = buffers.m_6299_(EpicFightRenderTypes.blockHighlight());
         BlockRenderDispatcher blockrenderdispatcher = Minecraft.m_91087_().m_91289_();
         BakedModel model = blockrenderdispatcher.m_110910_(bs);
         MutableBlockPos mutablepos = bp.m_122032_();
         int originX = bp.m_123341_() & 15;
         int originY = bp.m_123342_() & 15;
         int originZ = bp.m_123343_() & 15;

         for (Direction d : DIRECTIONS) {
            List<BakedQuad> culledFaces = model.getQuads(bs, d, randomsource, ModelData.EMPTY, null);
            mutablepos.m_122159_(bp, d);
            if (Block.m_152444_(bs, level, bp, d, mutablepos)) {
               this.renderPreviewBlocks(buffer, level, culledFaces, originX, originY, originZ, offset, r, g, b, a);
            }
         }

         this.renderPreviewBlocks(buffer, level, model.getQuads(bs, null, randomsource, ModelData.EMPTY, null), originX, originY, originZ, offset, r, g, b, a);
         RenderSystem.getModelViewStack().m_85836_();
         RenderSystem.getModelViewStack().m_252931_(poseStack.m_85850_().m_252922_());
         RenderSystem.applyModelViewMatrix();
         Uniform uniform = GameRenderer.m_172649_().f_173320_;
         Vec3 camPos = camera.m_90583_();
         int cameraBpX = (int)camPos.f_82479_;
         int cameraBpY = (int)camPos.f_82480_;
         int cameraBpZ = (int)camPos.f_82481_;
         uniform.m_5889_(
            (bp.m_123341_() >> 4 << 4) - cameraBpX - fractional(camPos.f_82479_),
            (bp.m_123342_() >> 4 << 4) - cameraBpY - fractional(camPos.f_82480_),
            (bp.m_123343_() >> 4 << 4) - cameraBpZ - fractional(camPos.f_82481_)
         );
         if (buffers instanceof FullyBufferedMultiBufferSource irisBuffer) {
            irisBuffer.m_109911_();
         } else if (buffers instanceof BufferSource vanillaBuffer) {
            vanillaBuffer.m_173043_();
         }

         uniform.m_5889_(0.0F, 0.0F, 0.0F);
         RenderSystem.getModelViewStack().m_85849_();
         RenderSystem.applyModelViewMatrix();
      }
   }

   private void renderPreviewBlocks(
      VertexConsumer consumer,
      BlockAndTintGetter level,
      List<BakedQuad> quads,
      int originX,
      int originY,
      int originZ,
      Vec3 offset,
      float r,
      float g,
      float b,
      float a
   ) {
      for (BakedQuad bakedquad : quads) {
         float f = level.m_7717_(bakedquad.m_111306_(), bakedquad.m_111307_());
         putBulkDataWithoutPose(
            consumer,
            bakedquad,
            originX,
            originY,
            originZ,
            offset,
            new float[]{f, f, f, f},
            r,
            g,
            b,
            a,
            new int[]{16777215, 16777215, 16777215, 16777215},
            OverlayTexture.f_118083_,
            false
         );
      }
   }

   private static void putBulkDataWithoutPose(
      VertexConsumer vertexConsumer,
      BakedQuad pQuad,
      int originX,
      int originY,
      int originZ,
      Vec3 offset,
      float[] pColorMuls,
      float pRed,
      float pGreen,
      float pBlue,
      float alpha,
      int[] pCombinedLights,
      int pCombinedOverlay,
      boolean pMulColor
   ) {
      float[] afloat = new float[]{pColorMuls[0], pColorMuls[1], pColorMuls[2], pColorMuls[3]};
      int[] aint1 = pQuad.m_111303_();
      Vec3i vec3i = pQuad.m_111306_().m_122436_();
      Vector3f vector3f = new Vector3f(vec3i.m_123341_(), vec3i.m_123342_(), vec3i.m_123343_());
      int j = pQuad.m_111303_().length / 8;
      BakedQuadView bakedQuadView = (BakedQuadView)pQuad;
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.f_85811_.m_86020_());
         IntBuffer intbuffer = bytebuffer.asIntBuffer();

         for (int k = 0; k < j; k++) {
            intbuffer.clear();
            intbuffer.put(aint1, k * 8, 8);
            float outX = decodePosition(encodePosition(originX + bakedQuadView.getX(k) + (float)offset.m_7096_()));
            float outY = decodePosition(encodePosition(originY + bakedQuadView.getY(k) + (float)offset.m_7098_()));
            float outZ = decodePosition(encodePosition(originZ + bakedQuadView.getZ(k) + (float)offset.m_7094_()));
            float f3;
            float f4;
            float f5;
            if (pMulColor) {
               float f6 = (bytebuffer.get(12) & 0xFF) / 255.0F;
               float f7 = (bytebuffer.get(13) & 0xFF) / 255.0F;
               float f8 = (bytebuffer.get(14) & 0xFF) / 255.0F;
               f3 = f6 * afloat[k] * pRed;
               f4 = f7 * afloat[k] * pGreen;
               f5 = f8 * afloat[k] * pBlue;
            } else {
               f3 = afloat[k] * pRed;
               f4 = afloat[k] * pGreen;
               f5 = afloat[k] * pBlue;
            }

            int l = vertexConsumer.applyBakedLighting(pCombinedLights[k], bytebuffer);
            float f9 = bytebuffer.getFloat(16);
            float f10 = bytebuffer.getFloat(20);
            vertexConsumer.applyBakedNormals(vector3f, bytebuffer, new Matrix3f());
            float vertexAlpha = pMulColor ? alpha * (bytebuffer.get(15) & 0xFF) / 255.0F : alpha;
            vertexConsumer.m_5954_(outX, outY, outZ, f3, f4, f5, vertexAlpha, f9, f10, pCombinedOverlay, l, vector3f.x(), vector3f.y(), vector3f.z());
         }
      } catch (Throwable var35) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var34) {
               var35.addSuppressed(var34);
            }
         }

         throw var35;
      }

      if (memorystack != null) {
         memorystack.close();
      }
   }

   private static float fractional(double value) {
      float fullPrecision = (float)(value - (int)value);
      float modifier = Math.copySign(128.0F, fullPrecision);
      return fullPrecision + modifier - modifier;
   }

   static short encodePosition(float v) {
      return (short)((8.0F + v) * 2048.0F);
   }

   static float decodePosition(short raw) {
      return (raw & 65535) * 4.8828125E-4F - 8.0F;
   }
}
