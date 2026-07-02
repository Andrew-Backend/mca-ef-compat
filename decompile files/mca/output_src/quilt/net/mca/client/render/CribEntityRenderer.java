package quilt.net.mca.client.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1767;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import net.minecraft.class_897;
import net.minecraft.class_918;
import net.minecraft.class_5617.class_5618;
import quilt.net.mca.MCA;
import quilt.net.mca.client.model.CribEntityModel;
import quilt.net.mca.entity.CribEntity;
import quilt.net.mca.entity.CribWoodType;

public class CribEntityRenderer extends class_897<CribEntity> {
   private final int TEXTURE_WIDTH = 88;
   private final int TEXTURE_HEIGHT = 60;
   private final Map<String, class_2960> REGISTERED_TEXTURES = new HashMap<>();
   protected CribEntityModel<CribEntity> model;
   private final class_918 itemRenderer;

   public CribEntityRenderer(class_5618 ctx) {
      super(ctx);
      this.itemRenderer = ctx.method_32168();
      this.model = new CribEntityModel<>(class_5607.method_32110(CribEntityModel.getModelData(class_5605.field_27715), 88, 60).method_32109());
      this.field_4673 = 0.75F;

      for (CribWoodType woodType : CribWoodType.values()) {
         for (class_1767 color : class_1767.values()) {
            try {
               this.REGISTERED_TEXTURES.put(this.getTextureID(woodType, color), this.generateMultiTexture(woodType, color));
            } catch (IOException e) {
               MCA.LOGGER.warn("And error occurred while loading dynamic crib texture! Skipping...\n{}", e.getMessage());
            }
         }
      }
   }

   public void render(CribEntity cribEntity, float f, float g, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i) {
      class_2960 texture = this.REGISTERED_TEXTURES.get(this.getTextureID(cribEntity));
      matrixStack.method_22903();
      matrixStack.method_22904(0.0, 0.375, 0.0);
      matrixStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F - f));
      matrixStack.method_22905(-1.0F, -1.0F, 1.0F);
      matrixStack.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
      this.model.setAngles(cribEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
      class_4588 vertexConsumer = vertexConsumerProvider.getBuffer(this.model.method_23500(texture));
      this.model.method_2828(matrixStack, vertexConsumer, i, class_4608.field_21444, 1.0F, 1.0F, 1.0F, 1.0F);
      class_1799 babyItem = cribEntity.getBabyItem();
      if (!babyItem.equals(class_1799.field_8037)) {
         matrixStack.method_46416(0.0F, 0.05F, 0.0F);
         matrixStack.method_22907(class_7833.field_40714.rotationDegrees(-90.0F));
         matrixStack.method_22907(class_7833.field_40718.rotationDegrees(180.0F));
         matrixStack.method_22905(0.75F, 0.75F, 0.75F);
         this.itemRenderer
            .method_23178(
               babyItem,
               class_811.field_4319,
               i,
               class_4608.field_21444,
               matrixStack,
               vertexConsumerProvider,
               cribEntity.method_37908(),
               cribEntity.method_5628()
            );
      }

      matrixStack.method_22909();
      super.method_3936(cribEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   private String getTextureID(CribEntity cribEntity) {
      return this.getTextureID(cribEntity.getWoodType(), cribEntity.getColor());
   }

   private String getTextureID(CribWoodType wood, class_1767 color) {
      return wood.toString().toLowerCase(Locale.ROOT) + "-" + color.method_7792();
   }

   private class_2960 generateMultiTexture(CribWoodType wood, class_1767 color) throws IOException {
      ClassLoader loader = MCA.class.getClassLoader();
      InputStream frameStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/frames/" + wood.toString().toLowerCase(Locale.ROOT) + ".png");
      if (frameStream == null) {
         frameStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/frames/oak.png");
      }

      assert frameStream != null;
      BufferedImage frame = ImageIO.read(frameStream);
      InputStream bedStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/beds/" + color.method_7792() + ".png");
      if (bedStream == null) {
         bedStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/beds/white.png");
      }

      assert bedStream != null;
      BufferedImage bed = ImageIO.read(bedStream);
      BufferedImage combined = new BufferedImage(88, 60, 2);
      Graphics g = combined.getGraphics();
      g.drawImage(frame, 0, 0, null);
      g.drawImage(bed, 0, 0, null);
      g.dispose();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(combined, "png", baos);
      byte[] bytes = baos.toByteArray();
      class_1043 dynTex = new class_1043(class_1011.method_49277(bytes));
      return class_310.method_1551().method_1531().method_4617("mca", dynTex);
   }

   public class_2960 getTexture(CribEntity crib) {
      return this.REGISTERED_TEXTURES.get(this.getTextureID(crib));
   }
}
