package forge.net.mca.client.model;

import forge.net.mca.entity.VillagerLike;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class ZombieVillagerEntityModelMCA<T extends LivingEntity & VillagerLike<T>> extends VillagerEntityModelMCA<T> {
   public ZombieVillagerEntityModelMCA(ModelPart tree) {
      super(tree);
   }

   @Override
   public void m_6973_(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      super.m_6973_(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      AnimationUtils.m_102102_(this.f_102812_, this.f_102811_, false, this.f_102608_, animationProgress);
      this.leftArmwear.m_104315_(this.f_102812_);
      this.rightArmwear.m_104315_(this.f_102811_);
   }
}
