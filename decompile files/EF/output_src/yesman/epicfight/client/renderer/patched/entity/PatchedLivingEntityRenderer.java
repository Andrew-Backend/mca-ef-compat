package yesman.epicfight.client.renderer.patched.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector4f;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.LayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.renderer.patched.layer.RenderOriginalModelLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedLivingEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>, R extends LivingEntityRenderer<E, M>, AM extends SkinnedMesh>
   extends PatchedEntityRenderer<E, T, R, AM>
   implements LayerRenderer<E, T, M> {
   protected final Map<Class<?>, PatchedLayer<E, T, M, ? extends RenderLayer<E, M>>> patchedLayers = Maps.newHashMap();
   protected final List<PatchedLayer<E, T, M, ? extends RenderLayer<E, M>>> customLayers = Lists.newArrayList();

   public PatchedLivingEntityRenderer(Context context, EntityType<?> entityType) {
      ResourceLocation type = EntityType.m_20613_(entityType);
      FileToIdConverter filetoidconverter = FileToIdConverter.m_246568_("animated_layers/" + type.m_135815_());
      List<Pair<ResourceLocation, JsonElement>> layers = Lists.newArrayList();

      for (Entry<ResourceLocation, Resource> entry : filetoidconverter.m_247457_(context.m_174026_()).entrySet()) {
         Reader reader = null;

         try {
            reader = entry.getValue().m_215508_();
            JsonElement jsonelement = (JsonElement)GsonHelper.m_13776_(new GsonBuilder().create(), reader, JsonElement.class);
            layers.add(Pair.of(entry.getKey(), jsonelement));
         } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
            EpicFightMod.LOGGER.error("Failed to parse layer file {} for {}", entry.getKey(), type);
            jsonparseexception.printStackTrace();
         } finally {
            try {
               if (reader != null) {
                  reader.close();
               }
            } catch (IOException var17) {
            }
         }
      }

      LayerUtil.addLayer(this, entityType, layers);
   }

   public PatchedLivingEntityRenderer<E, T, M, R, AM> initLayerLast(Context context, EntityType<?> entityType) {
      List<RenderLayer<E, M>> vanillaLayers = null;
      if (entityType == EntityType.f_20532_) {
         if (context.m_174022_().f_114363_.get("default") instanceof LivingEntityRenderer livingentityrenderer) {
            vanillaLayers = livingentityrenderer.f_115291_;
         }
      } else if (context.m_174022_().f_114362_.get(entityType) instanceof LivingEntityRenderer livingentityrenderer) {
         vanillaLayers = livingentityrenderer.f_115291_;
      }

      if (vanillaLayers != null) {
         for (RenderLayer<E, M> layer : vanillaLayers) {
            Class<?> layerClass = layer.getClass();
            if (layerClass.isAnonymousClass()) {
               layerClass = layer.getClass().getSuperclass();
            }

            if (!this.patchedLayers.containsKey(layerClass)) {
               this.addPatchedLayer(
                  layerClass,
                  new RenderOriginalModelLayer<>("Root", new Vec3f(0.0F, this.getDefaultLayerHeightCorrection(), 0.0F), new Vec3f(0.0F, 0.0F, 0.0F))
               );
            }
         }
      }

      return this;
   }

   public void render(E entity, T entitypatch, R renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
      super.render(entity, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);
      Minecraft mc = Minecraft.m_91087_();
      MixinLivingEntityRenderer livingEntityRendererAccessor = (MixinLivingEntityRenderer)renderer;
      boolean isVisible = livingEntityRendererAccessor.invokeIsBodyVisible(entity);
      boolean isVisibleToPlayer = !isVisible && !entity.m_20177_(mc.f_91074_);
      boolean isGlowing = mc.m_91314_(entity);
      RenderType renderType = livingEntityRendererAccessor.invokeGetRenderType(entity, isVisible, isVisibleToPlayer, isGlowing);
      Armature armature = entitypatch.getArmature();
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      this.prepareVanillaModel(entity, (M)renderer.m_7200_(), renderer, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      if (renderType != null) {
         AM mesh = this.getMeshProvider(entitypatch).get();
         this.prepareModel(mesh, entity, entitypatch, renderer);
         PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);
         if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
            Vector4f color = new Vector4f(1.0F, 1.0F, 1.0F, isVisibleToPlayer ? 0.15F : 1.0F);
            entitypatch.getEntityDecorations().modifyColor(color, partialTicks);
            int blockLight = (packedLight & 240) >> 4;
            int skyLight = (packedLight & 15728640) >> 20;
            Vec2i lightUv = new Vec2i(blockLight, skyLight);
            entitypatch.getEntityDecorations().modifyLight(lightUv, partialTicks);
            int modifiedLight = LightTexture.m_109885_(lightUv.x, lightUv.y);
            mesh.draw(
               poseStack,
               buffer,
               renderType,
               modifiedLight,
               color.x(),
               color.y(),
               color.z(),
               color.w(),
               this.getOverlayCoord(entity, entitypatch, partialTicks),
               armature,
               armature.getPoseMatrices()
            );
            entitypatch.getEntityDecorations()
               .listDecorationOverlays()
               .forEach(
                  decorationOverlay -> {
                     if (!decorationOverlay.shouldRemove() && decorationOverlay.shouldRender()) {
                        Vector4f overlayColor = decorationOverlay.color(partialTicks);
                        mesh.draw(
                           poseStack,
                           buffer,
                           decorationOverlay.getRenderType(),
                           modifiedLight,
                           overlayColor.x(),
                           overlayColor.y(),
                           overlayColor.z(),
                           overlayColor.w(),
                           OverlayTexture.f_118083_,
                           armature,
                           armature.getPoseMatrices()
                        );
                     }
                  }
               );
         }
      }

      if (!entity.m_5833_()) {
         this.renderLayer(renderer, entitypatch, entity, armature.getPoseMatrices(), buffer, poseStack, packedLight, partialTicks);
      }

      if (renderType != null && Minecraft.m_91087_().m_91290_().m_114377_()) {
         entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
      }

      poseStack.m_85849_();
   }

   protected void prepareVanillaModel(E entity, M model, LivingEntityRenderer<E, M> renderer, float partialTicks) {
      boolean shouldSit = entity.m_20159_() && entity.m_20202_() != null && entity.m_20202_().shouldRiderSit();
      model.f_102609_ = shouldSit;
      model.f_102610_ = entity.m_6162_();
      float f = Mth.m_14189_(partialTicks, entity.f_20884_, entity.f_20883_);
      float f1 = Mth.m_14189_(partialTicks, entity.f_20886_, entity.f_20885_);
      float f2 = f1 - f;
      if (shouldSit && entity.m_20202_() instanceof LivingEntity livingentity) {
         f = Mth.m_14189_(partialTicks, livingentity.f_20884_, livingentity.f_20883_);
         f2 = f1 - f;
         float f3 = Mth.m_14177_(f2);
         if (f3 < -85.0F) {
            f3 = -85.0F;
         }

         if (f3 >= 85.0F) {
            f3 = 85.0F;
         }

         f = f1 - f3;
         if (f3 * f3 > 2500.0F) {
            f += f3 * 0.2F;
         }

         f2 = f1 - f;
      }

      float f6 = Mth.m_14179_(partialTicks, entity.f_19860_, entity.m_146909_());
      if (LivingEntityRenderer.m_194453_(entity)) {
         f6 *= -1.0F;
         f2 *= -1.0F;
      }

      float f7 = ((MixinLivingEntityRenderer)renderer).invokeGetBob(entity, partialTicks);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (!shouldSit && entity.m_6084_()) {
         f8 = entity.f_267362_.m_267711_(partialTicks);
         f5 = entity.f_267362_.m_267756_() - entity.f_267362_.m_267731_() * (1.0F - partialTicks);
         if (entity.m_6162_()) {
            f5 *= 3.0F;
         }

         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      model.m_6839_(entity, f5, f8, partialTicks);
      model.m_6973_(entity, f5, f8, f7, f2, f6);
   }

   protected void prepareModel(AM mesh, E entity, T entitypatch, R renderer) {
      mesh.initialize();
   }

   protected void renderLayer(
      LivingEntityRenderer<E, M> renderer,
      T entitypatch,
      E entity,
      OpenMatrix4f[] poses,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      float f = MathUtils.lerpBetween(entity.f_20884_, entity.f_20883_, partialTicks);
      float f1 = MathUtils.lerpBetween(entity.f_20886_, entity.f_20885_, partialTicks);
      float f2 = f1 - f;
      float f7 = entity.m_5686_(partialTicks);
      float bob = ((MixinLivingEntityRenderer)renderer).invokeGetBob(entity, partialTicks);

      for (RenderLayer<E, M> layer : renderer.f_115291_) {
         Class<?> layerClass = layer.getClass();
         if (layerClass.isAnonymousClass()) {
            layerClass = layerClass.getSuperclass();
         }

         if (this.patchedLayers.containsKey(layerClass)) {
            this.patchedLayers.get(layerClass).renderLayer(entity, entitypatch, layer, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
         }
      }

      for (PatchedLayer<E, T, M, ? extends RenderLayer<E, M>> patchedLayer : this.customLayers) {
         patchedLayer.renderLayer(entity, entitypatch, null, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
      }
   }

   protected int getOverlayCoord(E entity, T entitypatch, float partialTicks) {
      int initU = 0;
      int initV = OverlayTexture.m_118096_(entity.f_20916_ > 0 || entity.f_20919_ > 0);
      Vec2i coord = new Vec2i(initU, initV);
      entitypatch.getEntityDecorations().modifyOverlay(coord, partialTicks);
      return OverlayTexture.m_118093_(coord.x, coord.y);
   }

   @Override
   public void mulPoseStack(PoseStack poseStack, Armature armature, E entity, T entitypatch, float partialTicks) {
      super.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      if (entity.m_6047_()) {
         poseStack.m_85837_(0.0, 0.15, 0.0);
      }
   }

   @Override
   public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends RenderLayer<E, M>> patchedLayer) {
      this.patchedLayers.putIfAbsent(originalLayerClass, patchedLayer);
   }

   public void addPatchedLayerAlways(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends RenderLayer<E, M>> patchedLayer) {
      this.patchedLayers.put(originalLayerClass, patchedLayer);
   }

   @Override
   public void addCustomLayer(PatchedLayer<E, T, M, ? extends RenderLayer<E, M>> patchedLayer) {
      this.customLayers.add(patchedLayer);
   }

   protected float getDefaultLayerHeightCorrection() {
      return 1.15F;
   }
}
