package forge.net.mca.client.render;

import forge.net.mca.client.model.VillagerEntityModelMCA;
import forge.net.mca.client.render.layer.ClothingLayer;
import forge.net.mca.client.render.layer.FaceLayer;
import forge.net.mca.client.render.layer.HairLayer;
import forge.net.mca.client.render.layer.SkinLayer;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class VillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<VillagerEntityMCA> {
   public VillagerEntityMCARenderer(Context ctx) {
      super(ctx, createModel(VillagerEntityModelMCA.bodyData(CubeDeformation.f_171458_)).hideWears());
      this.m_115326_(new SkinLayer(this, (VillagerEntityModelMCA)this.f_115290_));
      this.m_115326_(new FaceLayer(this, createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.01F))).hideWears(), "normal"));
      this.m_115326_(new ClothingLayer(this, createModel(VillagerEntityModelMCA.bodyData(new CubeDeformation(0.0625F))), "normal"));
      this.m_115326_(new HairLayer(this, createModel(VillagerEntityModelMCA.hairData(new CubeDeformation(0.125F)))));
   }

   private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(MeshDefinition data) {
      return new VillagerEntityModelMCA(LayerDefinition.m_171565_(data, 64, 64).m_171564_());
   }
}
