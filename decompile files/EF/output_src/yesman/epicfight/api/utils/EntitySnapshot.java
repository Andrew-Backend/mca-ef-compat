package yesman.epicfight.api.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.physics.SimulationTypes;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.PatchedCapeLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class EntitySnapshot<T extends LivingEntityPatch<?>> {
   private static final InteractionHand[] HANDS = InteractionHand.values();
   protected final T entitypatch;
   protected final EntitySnapshot.RenderableFigure entityFigure;
   protected final OpenMatrix4f[] poseMatrices;
   protected final OpenMatrix4f modelMatrix;
   protected final Vec3 position;
   protected final List<EntitySnapshot.RenderableFigure> armorMeshes;
   protected final List<Pair<InteractionHand, ItemStack>> handItems;
   protected final float yRot;
   protected final float heightHalf;

   public static EntitySnapshot<LivingEntityPatch<?>> captureLivingEntity(LivingEntityPatch<?> entitypatch) {
      return ClientEngine.getInstance().renderEngine.hasRendererFor(entitypatch.getOriginal()) ? new EntitySnapshot<>(entitypatch) : null;
   }

   public static EntitySnapshot.PlayerSnapshot capturePlayer(AbstractClientPlayerPatch<?> playerpatch) {
      return ClientEngine.getInstance().renderEngine.hasRendererFor(playerpatch.getOriginal()) ? new EntitySnapshot.PlayerSnapshot(playerpatch) : null;
   }

   public EntitySnapshot(T entitypatch) {
      LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> vanillarenderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)Minecraft.m_91087_()
         .m_91290_()
         .m_114382_(entitypatch.getOriginal());
      PatchedEntityRenderer patchedrenderer = ClientEngine.getInstance().renderEngine.getEntityRenderer(entitypatch.getOriginal());
      AssetAccessor<SkinnedMesh> meshAccessor = patchedrenderer.getMeshProvider(entitypatch);
      ResourceLocation textureLocation = vanillarenderer.m_5478_(entitypatch.getOriginal());
      if (textureLocation == null) {
         EpicFightMod.logAndStacktraceIfDevSide(
            Logger::warn,
            "No texture for " + entitypatch.getOriginal(),
            NullPointerException::new,
            "No texture is provided by vanilla renderer " + vanillarenderer.getClass().getSimpleName()
         );
      }

      if (meshAccessor == null || meshAccessor.isEmpty()) {
         EpicFightMod.logAndStacktraceIfDevSide(
            Logger::warn,
            "No mesh for " + entitypatch.getOriginal(),
            NullPointerException::new,
            "No mesh is provided by patched renderer " + patchedrenderer.getClass().getSimpleName()
         );
      }

      this.entityFigure = new EntitySnapshot.RenderableFigure(meshAccessor.get(), textureLocation);
      Pose pose = entitypatch.<Animator>getAnimator().getPose(1.0F);
      patchedrenderer.setJointTransforms(entitypatch, entitypatch.getArmature(), pose, 1.0F);
      this.poseMatrices = entitypatch.getArmature().getPoseAsTransformMatrix(pose, false);
      this.modelMatrix = entitypatch.getModelMatrix(1.0F);
      Builder<EntitySnapshot.RenderableFigure> builder = ImmutableList.builder();
      entitypatch.getOriginal().m_6168_().forEach(itemstackx -> {
         if (itemstackx.m_41720_() instanceof ArmorItem) {
            EquipmentSlot armorSlot = itemstackx.getEquipmentSlot();
            SkinnedMesh armor = WearableItemLayer.getCachedModel(itemstackx.m_41720_());
            ResourceLocation texture = WearableItemLayer.getArmorResource(entitypatch.getOriginal(), itemstackx, armorSlot, null);
            if (armor != null) {
               builder.add(new EntitySnapshot.RenderableFigure(armor, texture));
            }
         }
      });
      this.armorMeshes = builder.build();
      Builder<Pair<InteractionHand, ItemStack>> builder$2 = ImmutableList.builder();

      for (InteractionHand hand : HANDS) {
         ItemStack itemstack = entitypatch.getAdvancedHoldingItemStack(hand);
         if (!itemstack.m_41619_()) {
            builder$2.add(Pair.of(hand, itemstack));
         }
      }

      this.handItems = builder$2.build();
      this.position = entitypatch.getOriginal().m_20182_();
      this.yRot = Mth.m_14177_(entitypatch.getYRot());
      this.heightHalf = entitypatch.getOriginal().m_20206_() * 0.5F;
      this.entitypatch = entitypatch;
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffers,
      RenderType rendertype,
      Mesh.DrawingFunction drawingFunction,
      int packedLight,
      float r,
      float g,
      float b,
      float a
   ) {
      if (this.entityFigure.mesh != null && this.entityFigure.texture != null) {
         this.entityFigure.mesh.initialize();
         this.entityFigure
            .mesh
            .draw(
               poseStack,
               buffers,
               rendertype,
               drawingFunction,
               packedLight,
               r,
               g,
               b,
               a,
               OverlayTexture.f_118083_,
               this.entitypatch.getArmature(),
               this.poseMatrices
            );

         for (EntitySnapshot.RenderableFigure armorFigures : this.armorMeshes) {
            armorFigures.mesh.initialize();
            armorFigures.mesh
               .draw(
                  poseStack,
                  buffers,
                  rendertype,
                  drawingFunction,
                  packedLight,
                  r,
                  g,
                  b,
                  a,
                  OverlayTexture.f_118083_,
                  this.entitypatch.getArmature(),
                  this.poseMatrices
               );
         }
      }
   }

   public void renderTextured(
      PoseStack poseStack,
      MultiBufferSource buffers,
      Function<ResourceLocation, RenderType> rendertypeFunction,
      Mesh.DrawingFunction drawingFunction,
      int packedLight,
      float r,
      float g,
      float b,
      float a
   ) {
      if (this.entityFigure.mesh != null && this.entityFigure.texture != null) {
         this.entityFigure.mesh.initialize();
         this.entityFigure
            .mesh
            .draw(
               poseStack,
               buffers,
               rendertypeFunction.apply(this.entityFigure.texture),
               drawingFunction,
               packedLight,
               r,
               g,
               b,
               a,
               OverlayTexture.f_118083_,
               this.entitypatch.getArmature(),
               this.poseMatrices
            );

         for (EntitySnapshot.RenderableFigure armorFigures : this.armorMeshes) {
            armorFigures.mesh.initialize();
            armorFigures.mesh
               .draw(
                  poseStack,
                  buffers,
                  rendertypeFunction.apply(armorFigures.texture),
                  drawingFunction,
                  packedLight,
                  r,
                  g,
                  b,
                  a,
                  OverlayTexture.f_118083_,
                  this.entitypatch.getArmature(),
                  this.poseMatrices
               );
         }
      }
   }

   public void renderItems(
      PoseStack poseStack, MultiBufferSource buffers, RenderType rendertype, Mesh.DrawingFunction drawingFunction, int packedLight, float alpha
   ) {
      for (Pair<InteractionHand, ItemStack> items : this.handItems) {
         ItemStack itemstack = (ItemStack)items.getSecond();
         if (ClientEngine.getInstance().renderEngine.getItemRenderer(itemstack).appearedInAfterimage()) {
            poseStack.m_85836_();
            BakedModel bakedmodel = Minecraft.m_91087_()
               .m_91291_()
               .m_174264_(
                  itemstack,
                  this.entitypatch.getOriginal().m_9236_(),
                  this.entitypatch.getOriginal(),
                  this.entitypatch.getOriginal().m_19879_() + ItemDisplayContext.THIRD_PERSON_RIGHT_HAND.ordinal()
               );
            if (!bakedmodel.m_7521_()) {
               MathUtils.mulStack(
                  poseStack,
                  ClientEngine.getInstance()
                     .renderEngine
                     .getItemRenderer(itemstack)
                     .getCorrectionMatrix(this.entitypatch, (InteractionHand)items.getFirst(), this.poseMatrices)
               );
               bakedmodel = ForgeHooksClient.handleCameraTransforms(poseStack, bakedmodel, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false);
               poseStack.m_252880_(-0.5F, -0.5F, -0.5F);

               for (BakedModel model : bakedmodel.getRenderPasses(itemstack, true)) {
                  renderModelLists(model, itemstack, packedLight, OverlayTexture.f_118083_, alpha, poseStack, buffers.m_6299_(rendertype), drawingFunction);
               }
            }

            poseStack.m_85849_();
         }
      }
   }

   public OpenMatrix4f[] poseMatrices() {
      return this.poseMatrices;
   }

   public OpenMatrix4f getModelMatrix() {
      return this.modelMatrix;
   }

   public float getYRot() {
      return this.yRot;
   }

   public float getHeightHalf() {
      return this.heightHalf;
   }

   public Vec3 getPosition() {
      return this.position;
   }

   public static void renderModelLists(
      BakedModel pModel,
      ItemStack pStack,
      int pCombinedLight,
      int pCombinedOverlay,
      float alpha,
      PoseStack pPoseStack,
      VertexConsumer pBuffer,
      Mesh.DrawingFunction drawingFunction
   ) {
      RandomSource randomsource = RandomSource.m_216327_();

      for (Direction direction : Direction.values()) {
         randomsource.m_188584_(42L);
         renderQuadList(
            pPoseStack, pBuffer, pModel.m_213637_((BlockState)null, direction, randomsource), pStack, pCombinedLight, pCombinedOverlay, alpha, drawingFunction
         );
      }

      randomsource.m_188584_(42L);
      renderQuadList(
         pPoseStack,
         pBuffer,
         pModel.m_213637_((BlockState)null, (Direction)null, randomsource),
         pStack,
         pCombinedLight,
         pCombinedOverlay,
         alpha,
         drawingFunction
      );
   }

   public static void renderQuadList(
      PoseStack pPoseStack,
      VertexConsumer pBuffer,
      List<BakedQuad> pQuads,
      ItemStack pItemStack,
      int pCombinedLight,
      int pCombinedOverlay,
      float alpha,
      Mesh.DrawingFunction drawingFunction
   ) {
      boolean flag = !pItemStack.m_41619_();
      com.mojang.blaze3d.vertex.PoseStack.Pose posestack$pose = pPoseStack.m_85850_();

      for (BakedQuad bakedquad : pQuads) {
         int i = -1;
         if (flag && bakedquad.m_111304_()) {
            i = Minecraft.m_91087_().getItemColors().m_92676_(pItemStack, bakedquad.m_111305_());
         }

         float f = (i >> 16 & 0xFF) / 255.0F;
         float f1 = (i >> 8 & 0xFF) / 255.0F;
         float f2 = (i & 0xFF) / 255.0F;
         drawingFunction.putBulkData(posestack$pose, bakedquad, pBuffer, f, f1, f2, alpha, pCombinedLight, pCombinedOverlay, true);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class PlayerSnapshot extends EntitySnapshot<AbstractClientPlayerPatch<?>> {
      protected final Matrix4f localMatrix;
      protected final OpenMatrix4f[] unboundPoseMatrices;
      protected EntitySnapshot.RenderableFigure capeFigure;

      public PlayerSnapshot(AbstractClientPlayerPatch<?> entitypatch) {
         super(entitypatch);
         PatchedLivingEntityRenderer patchedrenderer = (PatchedLivingEntityRenderer)ClientEngine.getInstance()
            .renderEngine
            .getEntityRenderer(entitypatch.getOriginal());
         PoseStack poseStack = new PoseStack();
         patchedrenderer.mulPoseStack(poseStack, entitypatch.getArmature(), (LivingEntity)entitypatch.getOriginal(), (T)entitypatch, 1.0F);
         this.localMatrix = poseStack.m_85850_().m_252922_();
         this.unboundPoseMatrices = new OpenMatrix4f[entitypatch.getArmature().getPoseMatrices().length];

         for (int i = 0; i < entitypatch.getArmature().getPoseMatrices().length; i++) {
            this.unboundPoseMatrices[i] = new OpenMatrix4f(entitypatch.getArmature().getPoseMatrices()[i]);
         }

         if (entitypatch.getOriginal().m_36170_(PlayerModelPart.CAPE) && entitypatch.getOriginal().m_108561_() != null) {
            entitypatch.getSimulator(SimulationTypes.CLOTH)
               .ifPresent(
                  clohtSimulator -> clohtSimulator.getRunningObject(ClothSimulator.PLAYER_CLOAK)
                     .ifPresent(
                        clothObj -> {
                           ClothSimulator.ClothObject capturedClothObj = clothObj.captureMyself();
                           Function<Float, OpenMatrix4f> partialColliderTransformProvider = partialFrame -> {
                              Vec3 pos = entitypatch.getOriginal().m_20318_(partialFrame);
                              float yRotLerp = Mth.m_14189_(partialFrame, entitypatch.getYRotO(), entitypatch.getYRot());
                              return OpenMatrix4f.createTranslation((float)pos.f_82479_, (float)pos.f_82480_, (float)pos.f_82481_)
                                 .rotateDeg(180.0F - yRotLerp, Vec3f.Y_AXIS);
                           };
                           capturedClothObj.tick(entitypatch, partialColliderTransformProvider, 1.0F, entitypatch.getArmature(), this.unboundPoseMatrices);
                           this.capeFigure = new EntitySnapshot.RenderableFigure(capturedClothObj, entitypatch.getOriginal().m_108561_());
                        }
                     )
               );
         }
      }

      @Override
      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         RenderType rendertype,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a
      ) {
         if (this.capeFigure != null) {
            PatchedCapeLayer.renderSimulatingCape(
               poseStack,
               buffers,
               rendertype,
               drawingFunction,
               (ClothSimulator.ClothObject)this.capeFigure.mesh,
               this.position.f_82479_,
               this.position.f_82480_,
               this.position.f_82481_,
               r,
               g,
               b,
               a,
               this.entitypatch,
               this.unboundPoseMatrices,
               packedLight,
               this.localMatrix,
               this.yRot
            );
         }

         super.render(poseStack, buffers, rendertype, drawingFunction, packedLight, r, g, b, a);
      }

      @Override
      public void renderTextured(
         PoseStack poseStack,
         MultiBufferSource buffers,
         Function<ResourceLocation, RenderType> rendertypeFunction,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a
      ) {
         super.renderTextured(poseStack, buffers, rendertypeFunction, drawingFunction, packedLight, r, g, b, a);
         if (this.capeFigure != null) {
            PatchedCapeLayer.renderSimulatingCape(
               poseStack,
               buffers,
               rendertypeFunction.apply(this.capeFigure.texture),
               drawingFunction,
               (ClothSimulator.ClothObject)this.capeFigure.mesh,
               this.position.f_82479_,
               this.position.f_82480_,
               this.position.f_82481_,
               r,
               g,
               b,
               a,
               this.entitypatch,
               this.unboundPoseMatrices,
               packedLight,
               this.localMatrix,
               this.yRot
            );
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public record RenderableFigure(Mesh mesh, ResourceLocation texture) {
   }
}
