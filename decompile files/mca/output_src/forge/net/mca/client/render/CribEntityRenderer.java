package forge.net.mca.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import forge.net.mca.MCA;
import forge.net.mca.client.model.CribEntityModel;
import forge.net.mca.entity.CribEntity;
import forge.net.mca.entity.CribWoodType;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CribEntityRenderer extends EntityRenderer<CribEntity> {
   private final int TEXTURE_WIDTH = 88;
   private final int TEXTURE_HEIGHT = 60;
   private final Map<String, ResourceLocation> REGISTERED_TEXTURES = new HashMap<>();
   protected CribEntityModel<CribEntity> model;
   private final ItemRenderer itemRenderer;

   public CribEntityRenderer(Context ctx) {
      super(ctx);
      this.itemRenderer = ctx.m_174025_();
      this.model = new CribEntityModel<>(LayerDefinition.m_171565_(CribEntityModel.getModelData(CubeDeformation.f_171458_), 88, 60).m_171564_());
      this.f_114477_ = 0.75F;

      for (CribWoodType woodType : CribWoodType.values()) {
         for (DyeColor color : DyeColor.values()) {
            try {
               this.REGISTERED_TEXTURES.put(this.getTextureID(woodType, color), this.generateMultiTexture(woodType, color));
            } catch (IOException e) {
               MCA.LOGGER.warn("And error occurred while loading dynamic crib texture! Skipping...\n{}", e.getMessage());
            }
         }
      }
   }

   public void render(CribEntity cribEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
      ResourceLocation texture = this.REGISTERED_TEXTURES.get(this.getTextureID(cribEntity));
      matrixStack.m_85836_();
      matrixStack.m_85837_(0.0, 0.375, 0.0);
      matrixStack.m_252781_(Axis.f_252436_.m_252977_(180.0F - f));
      matrixStack.m_85841_(-1.0F, -1.0F, 1.0F);
      matrixStack.m_252781_(Axis.f_252436_.m_252977_(90.0F));
      this.model.setAngles(cribEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexConsumer = vertexConsumerProvider.m_6299_(this.model.m_103119_(texture));
      this.model.m_7695_(matrixStack, vertexConsumer, i, OverlayTexture.f_118083_, 1.0F, 1.0F, 1.0F, 1.0F);
      ItemStack babyItem = cribEntity.getBabyItem();
      if (!babyItem.equals(ItemStack.f_41583_)) {
         matrixStack.m_252880_(0.0F, 0.05F, 0.0F);
         matrixStack.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
         matrixStack.m_252781_(Axis.f_252403_.m_252977_(180.0F));
         matrixStack.m_85841_(0.75F, 0.75F, 0.75F);
         this.itemRenderer
            .m_269128_(
               babyItem,
               ItemDisplayContext.FIXED,
               i,
               OverlayTexture.f_118083_,
               matrixStack,
               vertexConsumerProvider,
               cribEntity.m_9236_(),
               cribEntity.m_19879_()
            );
      }

      matrixStack.m_85849_();
      super.m_7392_(cribEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   private String getTextureID(CribEntity cribEntity) {
      return this.getTextureID(cribEntity.getWoodType(), cribEntity.getColor());
   }

   private String getTextureID(CribWoodType wood, DyeColor color) {
      return wood.toString().toLowerCase(Locale.ROOT) + "-" + color.m_41065_();
   }

   private ResourceLocation generateMultiTexture(CribWoodType wood, DyeColor color) throws IOException {
      ClassLoader loader = MCA.class.getClassLoader();
      InputStream frameStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/frames/" + wood.toString().toLowerCase(Locale.ROOT) + ".png");
      if (frameStream == null) {
         frameStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/frames/oak.png");
      }

      assert frameStream != null;
      BufferedImage frame = ImageIO.read(frameStream);
      InputStream bedStream = loader.getResourceAsStream("assets/mca/textures/entity/crib/beds/" + color.m_41065_() + ".png");
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
      DynamicTexture dynTex = new DynamicTexture(NativeImage.m_271751_(bytes));
      return Minecraft.m_91087_().m_91097_().m_118490_("mca", dynTex);
   }

   public ResourceLocation getTexture(CribEntity crib) {
      return this.REGISTERED_TEXTURES.get(this.getTextureID(crib));
   }
}
