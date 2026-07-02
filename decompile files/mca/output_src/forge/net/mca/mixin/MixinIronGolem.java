package forge.net.mca.mixin;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IronGolem.class)
public abstract class MixinIronGolem extends LivingEntity {
   protected MixinIronGolem(EntityType<? extends LivingEntity> type, Level world) {
      super(type, world);
   }

   public boolean m_6779_(LivingEntity target) {
      return target instanceof VillagerEntityMCA ? false : super.m_6779_(target);
   }
}
