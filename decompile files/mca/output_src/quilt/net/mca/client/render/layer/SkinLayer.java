package quilt.net.mca.client.render.layer;

import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_572;
import quilt.net.mca.MCA;
import quilt.net.mca.client.model.CommonVillagerModel;
import quilt.net.mca.client.resources.ColorPalette;
import quilt.net.mca.entity.ai.Genetics;
import quilt.net.mca.entity.ai.Traits;

public class SkinLayer<T extends class_1309, M extends class_572<T>> extends VillagerLayer<T, M> {
   public SkinLayer(class_3883<T, M> renderer, M model) {
      super(renderer, model);
   }

   @Override
   public class_2960 getSkin(T villager) {
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
