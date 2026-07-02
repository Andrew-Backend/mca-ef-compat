package forge.net.mca.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import forge.net.mca.block.TombstoneBlock;
import forge.net.mca.util.localization.FlowingText;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class TombstoneBlockEntityRenderer implements BlockEntityRenderer<TombstoneBlock.Data> {
   private final Font text;

   public TombstoneBlockEntityRenderer(Context context) {
      this.text = context.m_173586_();
   }

   public int m_142163_() {
      return 32;
   }

   public void render(TombstoneBlock.Data entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
      if (entity.hasEntity()) {
         BlockState state = entity.m_58900_();
         matrices.m_85836_();
         matrices.m_85837_(0.5, 0.5, 0.5);
         Direction facing = ((Direction)state.m_61143_(BlockStateProperties.f_61374_)).m_122424_();
         matrices.m_252781_(Axis.f_252436_.m_252977_(-facing.m_122435_()));
         matrices.m_252880_(0.0F, 0.0F, 0.0F);
         matrices.m_85841_(0.010416667F, 0.010416667F, 0.010416667F);
         matrices.m_252781_(Axis.f_252403_.m_252977_(180.0F));
         TombstoneBlock block = (TombstoneBlock)state.m_60734_();
         matrices.m_252781_(Axis.f_252529_.m_252977_(block.getRotation()));
         Vec3 offset = block.getNameplateOffset();
         matrices.m_85837_(offset.m_7096_(), offset.m_7098_(), offset.m_7094_());
         int maxLineWidth = block.getLineWidth();
         float y = this.drawText(
            this.text, this.text.m_92923_(Component.m_237115_("block.mca.tombstone.header"), maxLineWidth), 0.0F, matrices, vertexConsumers, light
         );
         y += 5.0F;
         FlowingText name = entity.getOrCreateEntityName(n -> FlowingText.Factory.wrapLines(this.text, n, maxLineWidth, block.getMaxNameHeight()));
         matrices.m_85836_();
         matrices.m_85841_(name.scale(), name.scale(), name.scale());
         y = this.drawText(this.text, name.lines(), y / name.scale(), matrices, vertexConsumers, light) * name.scale();
         matrices.m_85849_();
         y += 5.0F;
         this.drawText(
            this.text,
            this.text.m_92923_(Component.m_237115_("block.mca.tombstone.footer." + entity.getGender().binary().getDataName()), maxLineWidth),
            y,
            matrices,
            vertexConsumers,
            light
         );
         matrices.m_85849_();
      }
   }

   private float drawText(Font text, List<FormattedCharSequence> lines, float y, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
      for (FormattedCharSequence line : lines) {
         float x = -text.m_92724_(line) / 2.0F;
         text.m_168645_(line, x, y, 16777215, 0, matrices.m_85850_().m_252922_(), vertexConsumers, light);
         y += 10.0F;
      }

      return y;
   }
}
