package quilt.net.mca.mixin;

import net.minecraft.class_1266;
import net.minecraft.class_1315;
import net.minecraft.class_1646;
import net.minecraft.class_2487;
import net.minecraft.class_3730;
import net.minecraft.class_5425;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.ducks.IVillagerEntity;

@Mixin(class_1646.class)
abstract class MixinVillagerEntity implements IVillagerEntity {
   @Nullable
   private transient class_3730 reason;

   @Override
   public class_3730 getSpawnReason() {
      return this.reason == null ? class_3730.field_16459 : this.reason;
   }

   @Inject(method = "method_5943", at = @At("HEAD"))
   private void onInitialize(
      class_5425 world,
      class_1266 difficulty,
      class_3730 spawnReason,
      @Nullable class_1315 entityData,
      @Nullable class_2487 entityNbt,
      CallbackInfoReturnable<class_1315> info
   ) {
      this.reason = spawnReason;
   }
}
