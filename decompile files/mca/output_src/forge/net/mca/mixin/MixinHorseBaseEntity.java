package forge.net.mca.mixin;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
abstract class MixinHorseBaseEntity extends Animal implements ContainerListener, PlayerRideableJumping, Saddleable {
   @Shadow
   @Nullable
   public abstract LivingEntity m_6688_();

   MixinHorseBaseEntity() {
      super(null, null);
   }

   @Inject(method = "m_6107_()Z", at = @At("HEAD"), cancellable = true)
   private void onIsImmobile(CallbackInfoReturnable<Boolean> info) {
      if (this.m_6688_() instanceof VillagerEntityMCA) {
         info.setReturnValue(false);
      }
   }
}
