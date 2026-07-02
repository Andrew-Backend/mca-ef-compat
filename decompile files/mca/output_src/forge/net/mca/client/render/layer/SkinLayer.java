package forge.net.mca.client.render.layer;

import forge.net.mca.MCA;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.client.resources.ColorPalette;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SkinLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends VillagerLayer<T, M> {
   public SkinLayer(RenderLayerParent<T, M> renderer, M model) {
      super(renderer, model);
   }

   @Override
   public ResourceLocation getSkin(T villager) {
      Genetics genetics = CommonVillagerModel.getVillager(villager).getGenetics();
      int skin = (int)Math.min(4.0F, Math.max(0.0F, genetics.getGene(Genetics.SKIN) * 5.0F));
      return this.cached("skins/skin/" + genetics.getGender().getDataName() + "/" + skin + ".png", MCA::locate);
   }

   @Override
   public float[] getColor(T villager, float tickDelta) {
      float albinism = CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.ALBINISM) ? 0.1F : 1.0F;
      return ColorPalette.SKIN
         .getColor(
            CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.MELANIN) * albinism,
            CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * albinism,
            CommonVillagerModel.getVillager(villager).getInfectionProgress()
         );
   }
}
