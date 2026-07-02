package fabric.net.mca.client.model;

import fabric.net.mca.entity.VillagerLike;
import net.minecraft.class_1309;
import net.minecraft.class_4896;
import net.minecraft.class_630;

public class ZombieVillagerEntityModelMCA<T extends class_1309 & VillagerLike<T>> extends VillagerEntityModelMCA<T> {
   public ZombieVillagerEntityModelMCA(class_630 tree) {
      super(tree);
   }

   @Override
   public void method_17087(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      super.method_17087(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      class_4896.method_29352(this.field_27433, this.field_3401, false, this.field_3447, animationProgress);
      this.leftArmwear.method_17138(this.field_27433);
      this.rightArmwear.method_17138(this.field_3401);
   }
}
