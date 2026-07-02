package forge.net.mca.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.MCAClient;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.client.model.PlayerEntityExtendedModel;
import forge.net.mca.client.model.VillagerEntityModelMCA;
import forge.net.mca.client.render.layer.ClothingLayer;
import forge.net.mca.client.render.layer.FaceLayer;
import forge.net.mca.client.render.layer.HairLayer;
import forge.net.mca.client.render.layer.SkinLayer;
import forge.net.mca.client.render.layer.VillagerLayer;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   @Unique
   private PlayerModel<AbstractClientPlayer> mca$villagerModel;
   @Unique
   private PlayerModel<AbstractClientPlayer> mca$vanillaModel;
   @Unique
   SkinLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> mca$skinLayer;
   @Unique
   ClothingLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> mca$clothingLayer;

   @Shadow
   protected abstract void m_117818_(AbstractClientPlayer var1);

   public MixinPlayerEntityRenderer(Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
      super(ctx, model, shadowRadius);
   }

   @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V", at = @At("TAIL"))
   private void init(Context ctx, boolean slim, CallbackInfo ci) {
      if (MCAClient.isPlayerRendererAllowed()) {
         this.mca$villagerModel = mca$createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.0F), slim));
         this.mca$vanillaModel = (PlayerModel<AbstractClientPlayer>)this.f_115290_;
         this.mca$skinLayer = new SkinLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.0F))));
         this.m_115326_(this.mca$skinLayer);
         this.m_115326_(new FaceLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.01F))), "normal"));
         this.mca$clothingLayer = new ClothingLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.0625F))), "normal");
         this.m_115326_(this.mca$clothingLayer);
         this.m_115326_(new HairLayer(this, mca$createModel(VillagerEntityModelMCA.hairData(new CubeDeformation(0.125F)))));
      }
   }

   @Unique
   private static PlayerEntityExtendedModel<AbstractClientPlayer> mca$createModel(MeshDefinition data) {
      return new PlayerEntityExtendedModel(LayerDefinition.m_171565_(data, 64, 64).m_171564_());
   }

   @Inject(method = "m_7546_(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("TAIL"), cancellable = true)
   private void injectScale(AbstractClientPlayer player, PoseStack matrices, float f, CallbackInfo ci) {
      if (MCAClient.useGeneticsRenderer(player.m_20148_())) {
         float height = CommonVillagerModel.getVillager(player).getRawScaleFactor();
         float width = CommonVillagerModel.getVillager(player).getHorizontalScaleFactor();
         matrices.m_85841_(width, height, width);
         if (CommonVillagerModel.getVillager(player).getAgeState() == AgeState.BABY && !player.m_20159_()) {
            matrices.m_252880_(0.0F, 0.6F, 0.0F);
         }

         ci.cancel();
         this.f_115290_ = this.mca$villagerModel;
      } else if (MCAClient.isPlayerRendererAllowed()) {
         this.f_115290_ = this.mca$vanillaModel;
      }
   }

   @Inject(
      method = "m_117770_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void injectRenderRightArm(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo ci) {
      if (MCAClient.renderArms(player.m_20148_(), "right_arm")) {
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$skinLayer.model.f_102811_, this.mca$skinLayer.model.f_103375_, this.mca$skinLayer
         );
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$clothingLayer.model.f_102811_, this.mca$clothingLayer.model.f_103375_, this.mca$clothingLayer
         );
         ci.cancel();
      }
   }

   @Inject(
      method = "m_117813_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void injectRenderLeftArm(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer player, CallbackInfo ci) {
      if (MCAClient.renderArms(player.m_20148_(), "left_arm")) {
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$skinLayer.model.f_102812_, this.mca$skinLayer.model.f_103374_, this.mca$skinLayer
         );
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$clothingLayer.model.f_102812_, this.mca$clothingLayer.model.f_103374_, this.mca$clothingLayer
         );
         ci.cancel();
      }
   }

   @Unique
   private void mca$renderCustomArm(
      PoseStack matrices,
      MultiBufferSource vertexConsumers,
      int light,
      AbstractClientPlayer player,
      ModelPart arm,
      ModelPart sleeve,
      VillagerLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layer
   ) {
      PlayerEntityExtendedModel<AbstractClientPlayer> model = (PlayerEntityExtendedModel<AbstractClientPlayer>)layer.model;
      this.m_117818_(player);
      model.f_102608_ = 0.0F;
      model.f_102817_ = false;
      model.f_102818_ = 0.0F;
      model.m_6973_(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      model.applyVillagerDimensions(CommonVillagerModel.getVillager(player), player.m_6047_());
      ResourceLocation skin = layer.getSkin(player);
      if (layer.canUse(skin)) {
         VertexConsumer buffer = vertexConsumers.m_6299_(RenderType.m_110458_(skin));
         float[] color = layer.getColor(player, 0.0F);
         arm.f_104203_ = 0.0F;
         arm.m_104306_(matrices, buffer, light, OverlayTexture.f_118083_, color[0], color[1], color[2], 1.0F);
         sleeve.f_104203_ = 0.0F;
         sleeve.m_104306_(matrices, buffer, light, OverlayTexture.f_118083_, color[0], color[1], color[2], 1.0F);
      }
   }
}
