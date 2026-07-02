package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class VanillaFakeBlockRenderer implements FakeBlockRenderer {
   private static final Direction[] DIRECTIONS = Direction.values();

   @Override
   public void render(Camera camera, PoseStack poseStack, MultiBufferSource buffers, Level level, BlockPos bp, float r, float g, float b, float a) {
      BlockState bs = level.m_8055_(bp);
      if (bs.m_60799_() == RenderShape.MODEL) {
         RandomSource randomsource = RandomSource.m_216327_();
         long seed = bs.m_60726_(bp);
         randomsource.m_188584_(seed);
         PoseStack poseStack2 = new PoseStack();
         Vec3 vec = bs.m_60824_(level, bp);
         poseStack2.m_85836_();
         poseStack2.m_252880_(bp.m_123341_() & 15, bp.m_123342_() & 15, bp.m_123343_() & 15);
         poseStack2.m_85837_(vec.f_82479_, vec.f_82480_, vec.f_82481_);
         Vec3 camPos = camera.m_90583_();
         VertexConsumer buffer = buffers.m_6299_(EpicFightRenderTypes.blockHighlight());
         BlockRenderDispatcher blockrenderdispatcher = Minecraft.m_91087_().m_91289_();
         BakedModel model = blockrenderdispatcher.m_110910_(bs);
         MutableBlockPos mutablepos = bp.m_122032_();

         for (Direction d : DIRECTIONS) {
            List<BakedQuad> culledFaces = model.getQuads(bs, d, randomsource, ModelData.EMPTY, null);
            mutablepos.m_122159_(bp, d);
            if (Block.m_152444_(bs, level, bp, d, mutablepos)) {
               this.renderPreviewBlocks(poseStack2, buffer, level, culledFaces, r, g, b, a);
            }
         }

         this.renderPreviewBlocks(poseStack2, buffer, level, model.getQuads(bs, null, randomsource, ModelData.EMPTY, null), r, g, b, a);
         poseStack2.m_85849_();
         RenderSystem.getModelViewStack().m_85836_();
         RenderSystem.getModelViewStack().m_252931_(poseStack.m_85850_().m_252922_());
         RenderSystem.applyModelViewMatrix();
         Uniform uniform = GameRenderer.m_172649_().f_173320_;
         uniform.m_5889_(
            (float)((bp.m_123341_() >> 4 << 4) - camPos.m_7096_()),
            (float)((bp.m_123342_() >> 4 << 4) - camPos.m_7098_()),
            (float)((bp.m_123343_() >> 4 << 4) - camPos.m_7094_())
         );
         if (buffers instanceof BufferSource vanillaBuffer) {
            vanillaBuffer.m_173043_();
         }

         uniform.m_5889_(0.0F, 0.0F, 0.0F);
         poseStack2.m_85849_();
         RenderSystem.getModelViewStack().m_85849_();
         RenderSystem.applyModelViewMatrix();
      }
   }

   private void renderPreviewBlocks(
      PoseStack poseStack, VertexConsumer consumer, BlockAndTintGetter level, List<BakedQuad> quads, float r, float g, float b, float a
   ) {
      for (BakedQuad bakedquad : quads) {
         float f = level.m_7717_(bakedquad.m_111306_(), bakedquad.m_111307_());
         consumer.putBulkData(
            poseStack.m_85850_(),
            bakedquad,
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
}
