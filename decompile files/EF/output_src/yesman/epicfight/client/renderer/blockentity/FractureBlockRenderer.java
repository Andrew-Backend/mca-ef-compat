package yesman.epicfight.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.level.block.entity.FractureBlockEntity;

public class FractureBlockRenderer implements BlockEntityRenderer<FractureBlockEntity> {
   private final BlockRenderDispatcher blockRenderDispatcher;

   public FractureBlockRenderer(Context context) {
      this.blockRenderDispatcher = context.m_173584_();
   }

   public boolean shouldRender(FractureBlockEntity p_173568_, Vec3 p_173569_) {
      return Vec3.m_82512_(p_173568_.m_58899_()).m_82509_(p_173569_, this.m_142163_());
   }

   public void render(
      FractureBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int lightColor, int overlayColor
   ) {
      float turnBackTime = 5.0F;
      float lerpAmount = Mth.m_14036_(
         partialTicks * (1.0F / turnBackTime) + (turnBackTime - (blockEntity.getMaxLifeTime() - blockEntity.getLifeTime())) * (1.0F / turnBackTime), 0.0F, 1.0F
      );
      Vector3f translate = blockEntity.getMaxLifeTime() > blockEntity.getLifeTime() + turnBackTime
         ? blockEntity.getTranslate()
         : MathUtils.lerpMojangVector(blockEntity.getTranslate(), new Vector3f(), lerpAmount);
      Quaternionf rotate = blockEntity.getMaxLifeTime() > blockEntity.getLifeTime() + turnBackTime
         ? blockEntity.getRotation()
         : MathUtils.lerpQuaternion(blockEntity.getRotation(), new Quaternionf(), lerpAmount);
      double BOUNCE_MAX_HEIGHT = blockEntity.getBouncing();
      double TIME = Math.max(BOUNCE_MAX_HEIGHT * 8.0, 8.0);
      double EXTENDER = 1.0 / Math.pow(TIME * 0.5, 2.0);
      double MOVE_GRAPH = Math.sqrt(BOUNCE_MAX_HEIGHT / EXTENDER);
      double bouncingAnimation = Math.max(-EXTENDER * Math.pow(blockEntity.getLifeTime() + partialTicks - MOVE_GRAPH, 2.0) + BOUNCE_MAX_HEIGHT, 0.0);
      poseStack.m_85836_();
      poseStack.m_85837_(0.5, 0.5, 0.5);
      poseStack.m_252781_(rotate);
      poseStack.m_85837_(translate.x(), translate.y() + bouncingAnimation, translate.z());
      poseStack.m_85837_(-0.5, -0.5, -0.5);
      this.blockRenderDispatcher
         .renderBreakingTexture(
            blockEntity.getOriginalBlockState(),
            blockEntity.m_58899_().m_7494_(),
            blockEntity.m_58904_(),
            poseStack,
            multiBufferSource.m_6299_(RenderType.m_110463_()),
            ModelData.EMPTY
         );
      poseStack.m_85849_();
   }
}
