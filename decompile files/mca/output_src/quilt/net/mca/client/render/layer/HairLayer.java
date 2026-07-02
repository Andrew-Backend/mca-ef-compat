package quilt.net.mca.client.render.layer;

import net.minecraft.class_1309;
import net.minecraft.class_1472;
import net.minecraft.class_1767;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import quilt.net.mca.client.gui.immersive_library.SkinCache;
import quilt.net.mca.client.model.CommonVillagerModel;
import quilt.net.mca.client.resources.ColorPalette;
import quilt.net.mca.entity.ai.Genetics;
import quilt.net.mca.entity.ai.Traits;

public class HairLayer<T extends class_1309, M extends class_572<T>> extends VillagerLayer<T, M> {
   public HairLayer(class_3883<T, M> renderer, M model) {
      super(renderer, model);
   }

   @Override
   public void render(
      class_4587 transform,
      class_4597 provider,
      int light,
      T villager,
      float limbAngle,
      float limbDistance,
      float tickDelta,
      float animationProgress,
      float headYaw,
      float headPitch
   ) {
      this.model.method_2805(true);
      this.model.field_3397.field_3665 = false;
      this.model.field_3392.field_3665 = false;
      super.render(transform, provider, light, villager, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
   }

   @Override
   public class_2960 getSkin(T villager) {
      String identifier = CommonVillagerModel.getVillager(villager).getHair();
      return identifier.startsWith("immersive_library:")
         ? SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)))
         : this.cached(identifier, class_2960::new);
   }

   @Override
   protected class_2960 getOverlay(T villager) {
      return this.cached(CommonVillagerModel.getVillager(villager).getHair().replace(".png", "_overlay.png"), class_2960::new);
   }

   private float[] getRainbow(class_1309 entity, float tickDelta) {
      int n = Math.abs(entity.field_6012) / 25 + entity.method_5628();
      int o = class_1767.values().length;
      int p = n % o;
      int q = (n + 1) % o;
      float r = (Math.abs(entity.field_6012) % 25 + tickDelta) / 25.0F;
      float[] fs = class_1472.method_6634(class_1767.method_7791(p));
      float[] gs = class_1472.method_6634(class_1767.method_7791(q));
      return new float[]{fs[0] * (1.0F - r) + gs[0] * r, fs[1] * (1.0F - r) + gs[1] * r, fs[2] * (1.0F - r) + gs[2] * r};
   }

   @Override
   public float[] getColor(T villager, float tickDelta) {
      if (CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.RAINBOW)) {
         return this.getRainbow(villager, tickDelta);
      }

      float[] hairDye = CommonVillagerModel.getVillager(villager).getHairDye();
      if (hairDye[0] > 0.0F) {
         return hairDye;
      }

      float albinism = CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.ALBINISM) ? 0.1F : 1.0F;
      return ColorPalette.HAIR
         .getColor(
            CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.EUMELANIN) * albinism,
            CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.PHEOMELANIN) * albinism,
            0.0F
         );
   }
}
