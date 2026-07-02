package forge.net.mca.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCA;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FaceLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends VillagerLayer<T, M> {
   private static final int FACE_COUNT = 22;
   private final String variant;

   public FaceLayer(RenderLayerParent<T, M> renderer, M model, String variant) {
      super(renderer, model);
      this.variant = variant;
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
      this.model.m_8009_(false);
      this.model.f_102808_.f_104207_ = true;
      super.render(transform, provider, light, villager, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
   }

   @Override
   protected boolean isTranslucent() {
      return true;
   }

   @Override
   public ResourceLocation getSkin(T villager) {
      int index = (int)Math.min(21.0F, Math.max(0.0F, CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.FACE) * 22.0F));
      int time = villager.f_19797_ / 2 + (int)(CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536.0F);
      boolean blink = time % 50 == 1 || time % 57 == 1 || villager.m_5803_() || villager.m_21224_();
      boolean hasHeterochromia = this.variant.equals("normal") && CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.HETEROCHROMIA);
      String gender = CommonVillagerModel.getVillager(villager).getGenetics().getGender().getDataName();
      String blinkTexture = blink ? "_blink" : (hasHeterochromia ? "_hetero" : "");
      return this.cached("skins/face/" + this.variant + "/" + gender + "/" + index + blinkTexture + ".png", MCA::locate);
   }
}
