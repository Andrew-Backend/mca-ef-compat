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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
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
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.LayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PresetRenderer
   extends PatchedEntityRenderer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityRenderer<LivingEntity>, SkinnedMesh>
   implements LayerRenderer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>> {
   private final LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> presetRenderer;
   protected final Map<Class<?>, PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, ? extends RenderLayer<LivingEntity, EntityModel<LivingEntity>>>> patchedLayers = Maps.newHashMap();
   protected final List<PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, ? extends RenderLayer<LivingEntity, EntityModel<LivingEntity>>>> customLayers = Lists.newArrayList();
   protected final AssetAccessor<SkinnedMesh> mesh;

   public PresetRenderer(
      Context context, EntityType<?> entityType, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer, AssetAccessor<SkinnedMesh> mesh
   ) {
      this.presetRenderer = renderer;
      this.mesh = mesh;
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
            } catch (IOException var19) {
            }
         }
      }

      LayerUtil.addLayer(this, entityType, layers);
   }

   @Override
   public void render(
      LivingEntity entity,
      LivingEntityPatch<LivingEntity> entitypatch,
      EntityRenderer<LivingEntity> renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      super.render(entity, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);
      Minecraft mc = Minecraft.m_91087_();
      MixinLivingEntityRenderer livingEntityRendererAccessor = (MixinLivingEntityRenderer)this.presetRenderer;
      boolean isVisible = livingEntityRendererAccessor.invokeIsBodyVisible(entity);
      boolean isVisibleToPlayer = !isVisible && !entity.m_20177_(mc.f_91074_);
      boolean isGlowing = mc.m_91314_(entity);
      RenderType renderType = livingEntityRendererAccessor.invokeGetRenderType(entity, isVisible, isVisibleToPlayer, isGlowing);
      Armature armature = entitypatch.getArmature();
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      if (renderType != null) {
         this.prepareVanillaModel(entity, this.presetRenderer.m_7200_(), this.presetRenderer, partialTicks);
         SkinnedMesh mesh = this.getMeshProvider(entitypatch).get();
         this.prepareModel(mesh, entity, entitypatch, this.presetRenderer);
         PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);
         if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
            mesh.draw(
               poseStack,
               buffer,
               renderType,
               packedLight,
               1.0F,
               1.0F,
               1.0F,
               isVisibleToPlayer ? 0.15F : 1.0F,
               this.getOverlayCoord(entity, entitypatch, partialTicks),
               armature,
               armature.getPoseMatrices()
            );
         }
      }

      if (!entity.m_5833_()) {
         this.renderLayer(this.presetRenderer, entitypatch, entity, armature.getPoseMatrices(), buffer, poseStack, packedLight, partialTicks);
      }

      if (renderType != null && Minecraft.m_91087_().m_91290_().m_114377_()) {
         entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
      }

      poseStack.m_85849_();
   }

   public float getVanillaRendererBob(LivingEntity entity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer, float partialTicks) {
      return entity.f_19797_ + partialTicks;
   }

   protected void prepareVanillaModel(
      LivingEntity entityIn, EntityModel<LivingEntity> model, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer, float partialTicks
   ) {
      boolean shouldSit = entityIn.m_20159_() && entityIn.m_20202_() != null && entityIn.m_20202_().shouldRiderSit();
      model.f_102609_ = shouldSit;
      model.f_102610_ = entityIn.m_6162_();
      float f = Mth.m_14189_(partialTicks, entityIn.f_20884_, entityIn.f_20883_);
      float f1 = Mth.m_14189_(partialTicks, entityIn.f_20886_, entityIn.f_20885_);
      float f2 = f1 - f;
      if (shouldSit && entityIn.m_20202_() instanceof LivingEntity livingentity) {
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

      float f6 = Mth.m_14179_(partialTicks, entityIn.f_19860_, entityIn.m_146909_());
      if (LivingEntityRenderer.m_194453_(entityIn)) {
         f6 *= -1.0F;
         f2 *= -1.0F;
      }

      float f7 = this.getVanillaRendererBob(entityIn, renderer, partialTicks);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (!shouldSit && entityIn.m_6084_()) {
         f8 = entityIn.f_267362_.m_267711_(partialTicks);
         f5 = entityIn.f_267362_.m_267756_() - entityIn.f_267362_.m_267731_() * (1.0F - partialTicks);
         if (entityIn.m_6162_()) {
            f5 *= 3.0F;
         }

         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      model.m_6839_(entityIn, f5, f8, partialTicks);
      model.m_6973_(entityIn, f5, f8, f7, f2, f6);
   }

   protected void prepareModel(
      SkinnedMesh mesh,
      LivingEntity entity,
      LivingEntityPatch<LivingEntity> entitypatch,
      LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer
   ) {
      mesh.initialize();
   }

   protected void renderLayer(
      LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer,
      LivingEntityPatch<LivingEntity> entitypatch,
      LivingEntity entity,
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
      float bob = this.getVanillaRendererBob(entity, renderer, partialTicks);

      for (RenderLayer<LivingEntity, EntityModel<LivingEntity>> layer : renderer.f_115291_) {
         Class<?> layerClass = layer.getClass();
         if (layerClass.isAnonymousClass()) {
            layerClass = layerClass.getSuperclass();
         }

         if (this.patchedLayers.containsKey(layerClass)) {
            this.patchedLayers.get(layerClass).renderLayer(entity, entitypatch, layer, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
         }
      }

      for (PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, ? extends RenderLayer<LivingEntity, EntityModel<LivingEntity>>> patchedLayer : this.customLayers) {
         patchedLayer.renderLayer(entity, entitypatch, null, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
      }
   }

   protected int getOverlayCoord(LivingEntity entity, LivingEntityPatch<LivingEntity> entitypatch, float partialTicks) {
      return OverlayTexture.m_118093_(0, OverlayTexture.m_118096_(entity.f_20916_ > 5));
   }

   @Override
   public void addPatchedLayer(
      Class<?> originalLayerClass,
      PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, ? extends RenderLayer<LivingEntity, EntityModel<LivingEntity>>> patchedLayer
   ) {
      this.patchedLayers.putIfAbsent(originalLayerClass, patchedLayer);
   }

   @Override
   public void addCustomLayer(
      PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, ? extends RenderLayer<LivingEntity, EntityModel<LivingEntity>>> patchedLayer
   ) {
      this.customLayers.add(patchedLayer);
   }

   @Override
   public AssetAccessor<SkinnedMesh> getDefaultMesh() {
      return this.mesh;
   }
}
