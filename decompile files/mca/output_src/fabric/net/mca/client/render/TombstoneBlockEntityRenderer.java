package fabric.net.mca.client.render;

import fabric.net.mca.block.TombstoneBlock;
import fabric.net.mca.util.localization.FlowingText;
import java.util.List;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5481;
import net.minecraft.class_7833;
import net.minecraft.class_827;
import net.minecraft.class_5614.class_5615;

public class TombstoneBlockEntityRenderer implements class_827<TombstoneBlock.Data> {
   private final class_327 text;

   public TombstoneBlockEntityRenderer(class_5615 context) {
      this.text = context.method_32143();
   }

   public int method_33893() {
      return 32;
   }

   public void render(TombstoneBlock.Data entity, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light, int overlay) {
      if (entity.hasEntity()) {
         class_2680 state = entity.method_11010();
         matrices.method_22903();
         matrices.method_22904(0.5, 0.5, 0.5);
         class_2350 facing = ((class_2350)state.method_11654(class_2741.field_12481)).method_10153();
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-facing.method_10144()));
         matrices.method_46416(0.0F, 0.0F, 0.0F);
         matrices.method_22905(0.010416667F, 0.010416667F, 0.010416667F);
         matrices.method_22907(class_7833.field_40718.rotationDegrees(180.0F));
         TombstoneBlock block = (TombstoneBlock)state.method_26204();
         matrices.method_22907(class_7833.field_40714.rotationDegrees(block.getRotation()));
         class_243 offset = block.getNameplateOffset();
         matrices.method_22904(offset.method_10216(), offset.method_10214(), offset.method_10215());
         int maxLineWidth = block.getLineWidth();
         float y = this.drawText(
            this.text, this.text.method_1728(class_2561.method_43471("block.mca.tombstone.header"), maxLineWidth), 0.0F, matrices, vertexConsumers, light
         );
         y += 5.0F;
         FlowingText name = entity.getOrCreateEntityName(n -> FlowingText.Factory.wrapLines(this.text, n, maxLineWidth, block.getMaxNameHeight()));
         matrices.method_22903();
         matrices.method_22905(name.scale(), name.scale(), name.scale());
         y = this.drawText(this.text, name.lines(), y / name.scale(), matrices, vertexConsumers, light) * name.scale();
         matrices.method_22909();
         y += 5.0F;
         this.drawText(
            this.text,
            this.text.method_1728(class_2561.method_43471("block.mca.tombstone.footer." + entity.getGender().binary().getDataName()), maxLineWidth),
            y,
            matrices,
            vertexConsumers,
            light
         );
         matrices.method_22909();
      }
   }

   private float drawText(class_327 text, List<class_5481> lines, float y, class_4587 matrices, class_4597 vertexConsumers, int light) {
      for (class_5481 line : lines) {
         float x = -text.method_30880(line) / 2.0F;
         text.method_37296(line, x, y, 16777215, 0, matrices.method_23760().method_23761(), vertexConsumers, light);
         y += 10.0F;
      }

      return y;
   }
}
