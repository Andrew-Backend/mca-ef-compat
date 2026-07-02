package forge.net.mca.client.render.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.MCA;
import forge.net.mca.MCAClient;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.client.model.PlayerEntityExtendedModel;
import forge.net.mca.client.model.VillagerEntityModelMCA;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class VillagerLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
   private static final float[] DEFAULT_COLOR = new float[]{1.0F, 1.0F, 1.0F};
   private static final Map<String, ResourceLocation> TEXTURE_CACHE = Maps.newHashMap();
   private static final Map<ResourceLocation, Boolean> TEXTURE_EXIST_CACHE = Maps.newHashMap();
   public final M model;

   public VillagerLayer(RenderLayerParent<T, M> renderer, M model) {
      super(renderer);
      this.model = model;
   }

   @Nullable
   public ResourceLocation getSkin(T villager) {
      return null;
   }

   @Nullable
   protected ResourceLocation getOverlay(T villager) {
      return null;
   }

   public float[] getColor(T villager, float tickDelta) {
      return DEFAULT_COLOR;
   }

   protected boolean isTranslucent() {
      return false;
   }

   public void render(
      PoseStack transform,
      MultiBufferSource provider,
      int light,
      T villager,
      float limbAngle,
      float limbDistance,
      float tickDelta,
      float animationProgress,
      float headYaw,
      float headPitch
   ) {
      Minecraft client = Minecraft.m_91087_();
      boolean visible = !villager.m_20145_();
      boolean glowing = client.m_91314_(villager);
      if (!CommonVillagerModel.getVillager(villager).hasCustomSkin()) {
         if (!(villager instanceof Player) || MCAClient.useVillagerRenderer(villager.m_20148_())) {
            if (this.model instanceof VillagerEntityModelMCA layer) {
               layer.copyVisibility((M)this.m_117386_());
            }

            if (this.model instanceof PlayerEntityExtendedModel layer) {
               layer.copyVisibility((M)this.m_117386_());
            }

            ((HumanoidModel)this.m_117386_()).m_102872_(this.model);
            this.renderFinal(transform, provider, light, villager, tickDelta, visible, glowing);
         }
      }
   }

   public void renderFinal(PoseStack transform, MultiBufferSource provider, int light, T villager, float tickDelta, boolean visible, boolean glowing) {
      int tint = LivingEntityRenderer.m_115338_(villager, 0.0F);
      ResourceLocation skin = this.getSkin(villager);
      if (this.canUse(skin)) {
         float[] color = this.getColor(villager, tickDelta);
         this.renderModel(transform, provider, light, this.model, color[0], color[1], color[2], skin, tint, visible, glowing);
      }

      ResourceLocation overlay = this.getOverlay(villager);
      if (!Objects.equals(skin, overlay) && this.canUse(overlay)) {
         this.renderModel(transform, provider, light, this.model, 1.0F, 1.0F, 1.0F, overlay, tint, visible, glowing);
      }
   }

   @Nullable
   protected RenderType getRenderLayer(ResourceLocation texture, boolean showBody, boolean translucent, boolean showOutline) {
      if (translucent) {
         return RenderType.m_110467_(texture);
      } else if (showBody) {
         return this.model.m_103119_(texture);
      } else {
         return showOutline ? RenderType.m_110491_(texture) : null;
      }
   }

   private void renderModel(
      PoseStack transform,
      MultiBufferSource provider,
      int light,
      M model,
      float r,
      float g,
      float b,
      ResourceLocation texture,
      int overlay,
      boolean visible,
      boolean glowing
   ) {
      RenderType layer = this.getRenderLayer(texture, visible, this.isTranslucent(), glowing);
      if (layer != null) {
         VertexConsumer buffer = provider.m_6299_(layer);
         model.m_7695_(transform, buffer, light, overlay, r, g, b, 1.0F);
      }
   }

   public final boolean canUse(ResourceLocation texture) {
      return TEXTURE_EXIST_CACHE.computeIfAbsent(
         texture,
         s -> texture != null && texture.m_135827_().equals("immersive_library")
            ? true
            : texture != null && Minecraft.m_91087_().m_91098_().m_213713_(texture).isPresent()
      );
   }

   @Nullable
   protected final ResourceLocation cached(String name, Function<String, ResourceLocation> supplier) {
      return TEXTURE_CACHE.computeIfAbsent(name, s -> {
         try {
            return supplier.apply(s);
         } catch (ResourceLocationException ignored) {
            return null;
         }
      });
   }

   static {
      TEXTURE_EXIST_CACHE.put(MCA.locate("temp"), true);
   }
}
