package yesman.epicfight.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfForm;
import de.teamlapen.werewolves.client.model.WerewolfEarsModel;
import de.teamlapen.werewolves.client.render.layer.HumanWerewolfLayer;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.mixin.teamlapen.MixinHumanWerewolfLayer;

public class WerewolvesCompat implements ICompatModule {
   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
      eventBus.addListener(event -> {
         WerewolfForm form = WerewolfPlayer.get(event.getPlayerPatch().getOriginal()).getForm();
         if (form == WerewolfForm.SURVIVALIST || form == WerewolfForm.BEAST) {
            event.setCanceled(true);
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onModEventBusClient(IEventBus eventBus) {
      eventBus.addListener(event -> {
         if (event.get(EntityType.f_20532_) instanceof PPlayerRenderer playerrenderer) {
            playerrenderer.addPatchedLayerAlways(HumanWerewolfLayer.class, new WerewolvesCompat.EpicFightHumanWerewolfLayer());
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
      eventBus.addListener(event -> {
         WerewolfForm form = WerewolfPlayer.get(event.getPlayerPatch().getOriginal()).getForm();
         if (form == WerewolfForm.SURVIVALIST || form == WerewolfForm.BEAST) {
            event.setShouldRender(false);
         }
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class EpicFightHumanWerewolfLayer<A extends HumanoidModel<AbstractClientPlayer>>
      extends PatchedLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, HumanWerewolfLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>, A>> {
      private SkinnedMesh mesh;
      private SkinnedMesh slimMesh;

      protected void renderLayer(
         AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch,
         AbstractClientPlayer entityliving,
         HumanWerewolfLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>, A> vanillaLayer,
         PoseStack poseStack,
         MultiBufferSource buffer,
         int packedLight,
         OpenMatrix4f[] poses,
         float bob,
         float yRot,
         float xRot,
         float partialTicks
      ) {
         MixinHumanWerewolfLayer<AbstractClientPlayer, A> accessor = (MixinHumanWerewolfLayer<AbstractClientPlayer, A>)vanillaLayer;
         String modelType = entityliving.m_108564_();
         A vanillaModel = (A)accessor.getModel();
         if (vanillaModel instanceof WerewolfEarsModel werewolfEars) {
            werewolfEars.f_102808_.m_171322_(werewolfEars.f_102808_.m_233566_());
            werewolfEars.f_102809_.m_171322_(werewolfEars.f_102809_.m_233566_());
            werewolfEars.f_102810_.m_171322_(werewolfEars.f_102810_.m_233566_());
            werewolfEars.f_102812_.m_171322_(werewolfEars.f_102812_.m_233566_());
            werewolfEars.f_102811_.m_171322_(werewolfEars.f_102811_.m_233566_());
            werewolfEars.f_102814_.m_171322_(werewolfEars.f_102814_.m_233566_());
            werewolfEars.f_102813_.m_171322_(werewolfEars.f_102813_.m_233566_());
         }

         SkinnedMesh mesh;
         if ("default".equals(modelType)) {
            if (this.mesh == null) {
               this.mesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(vanillaModel);
            }

            mesh = this.mesh;
         } else {
            if (this.slimMesh == null) {
               this.slimMesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(vanillaModel);
            }

            mesh = this.slimMesh;
         }

         Helper.asIWerewolf(entityliving)
            .filter(werewolf -> werewolf.getForm() == WerewolfForm.HUMAN)
            .ifPresent(
               werewolf -> {
                  ResourceLocation texture = accessor.getTextures().get(werewolf.getSkinType() % accessor.getTextures().size());
                  RenderType rendertype = EpicFightRenderTypes.getTriangulated(RenderType.m_110458_(texture));
                  mesh.draw(
                     poseStack,
                     buffer,
                     rendertype,
                     packedLight,
                     1.0F,
                     1.0F,
                     1.0F,
                     1.0F,
                     LivingEntityRenderer.m_115338_(entityliving, 0.0F),
                     entitypatch.getArmature(),
                     poses
                  );
               }
            );
      }
   }
}
