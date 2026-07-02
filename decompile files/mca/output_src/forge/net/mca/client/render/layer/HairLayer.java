package forge.net.mca.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.client.gui.immersive_library.SkinCache;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.client.resources.ColorPalette;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class HairLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends VillagerLayer<T, M> {
   public HairLayer(RenderLayerParent<T, M> renderer, M model) {
      super(renderer, model);
   }

   @Override
   public void render(
      PoseStack transform,
      MultiBufferSource provider,
      int light,
      T villager,
      float limbAngle,
      float limbDistance,
      float tickDelta,
      float animationProgress,
      float headYaw,
      float headPitch
   ) {
      this.model.m_8009_(true);
      this.model.f_102814_.f_104207_ = false;
      this.model.f_102813_.f_104207_ = false;
      super.render(transform, provider, light, villager, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
   }

   @Override
   public ResourceLocation getSkin(T villager) {
      String identifier = CommonVillagerModel.getVillager(villager).getHair();
      return identifier.startsWith("immersive_library:")
         ? SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)))
         : this.cached(identifier, ResourceLocation::new);
   }

   @Override
   protected ResourceLocation getOverlay(T villager) {
      return this.cached(CommonVillagerModel.getVillager(villager).getHair().replace(".png", "_overlay.png"), ResourceLocation::new);
   }

   private float[] getRainbow(LivingEntity entity, float tickDelta) {
      int n = Math.abs(entity.f_19797_) / 25 + entity.m_19879_();
      int o = DyeColor.values().length;
      int p = n % o;
      int q = (n + 1) % o;
      float r = (Math.abs(entity.f_19797_) % 25 + tickDelta) / 25.0F;
      float[] fs = Sheep.m_29829_(DyeColor.m_41053_(p));
      float[] gs = Sheep.m_29829_(DyeColor.m_41053_(q));
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
