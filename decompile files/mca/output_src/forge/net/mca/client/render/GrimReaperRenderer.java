package forge.net.mca.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCA;
import forge.net.mca.client.model.GrimReaperEntityModel;
import forge.net.mca.entity.GrimReaperEntity;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class GrimReaperRenderer extends HumanoidMobRenderer<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
   private static final ResourceLocation TEXTURE = MCA.locate("textures/entity/grimreaper.png");

   public GrimReaperRenderer(Context ctx) {
      super(ctx, new GrimReaperEntityModel(LayerDefinition.m_171565_(GrimReaperEntityModel.getModelData(CubeDeformation.f_171458_), 64, 64).m_171564_()), 0.5F);
   }

   protected void scale(GrimReaperEntity reaper, PoseStack matrices, float tickDelta) {
      matrices.m_85841_(1.3F, 1.3F, 1.3F);
   }

   public ResourceLocation getTexture(GrimReaperEntity reaper) {
      return TEXTURE;
   }
}
