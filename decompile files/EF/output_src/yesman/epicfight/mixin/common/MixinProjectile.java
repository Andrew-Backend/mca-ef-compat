package yesman.epicfight.mixin.common;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface MixinProjectile {
   @Invoker("onHitEntity")
   void invoke_onHitEntity(EntityHitResult var1);
}
