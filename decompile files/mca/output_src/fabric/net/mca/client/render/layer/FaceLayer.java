package fabric.net.mca.client.render.layer;

import fabric.net.mca.MCA;
import fabric.net.mca.client.model.CommonVillagerModel;
import fabric.net.mca.entity.ai.Genetics;
import fabric.net.mca.entity.ai.Traits;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;

public class FaceLayer<T extends class_1309, M extends class_572<T>> extends VillagerLayer<T, M> {
   private static final int FACE_COUNT = 22;
   private final String variant;

   public FaceLayer(class_3883<T, M> renderer, M model, String variant) {
      super(renderer, model);
      this.variant = variant;
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
      this.model.method_2805(false);
      this.model.field_3398.field_3665 = true;
      super.render(transform, provider, light, villager, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
   }

   @Override
   protected boolean isTranslucent() {
      return true;
   }

   @Override
   public class_2960 getSkin(T villager) {
      int index = (int)Math.min(21.0F, Math.max(0.0F, CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.FACE) * 22.0F));
      int time = villager.field_6012 / 2 + (int)(CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536.0F);
      boolean blink = time % 50 == 1 || time % 57 == 1 || villager.method_6113() || villager.method_29504();
      boolean hasHeterochromia = this.variant.equals("normal") && CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.HETEROCHROMIA);
      String gender = CommonVillagerModel.getVillager(villager).getGenetics().getGender().getDataName();
      String blinkTexture = blink ? "_blink" : (hasHeterochromia ? "_hetero" : "");
      return this.cached("skins/face/" + this.variant + "/" + gender + "/" + index + blinkTexture + ".png", MCA::locate);
   }
}
