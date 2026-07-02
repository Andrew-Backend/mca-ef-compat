package fabric.net.mca.mixin;

import fabric.net.mca.ducks.IVillagerEntity;
import net.minecraft.class_1266;
import net.minecraft.class_1315;
import net.minecraft.class_1641;
import net.minecraft.class_2487;
import net.minecraft.class_3730;
import net.minecraft.class_3850;
import net.minecraft.class_3852;
import net.minecraft.class_5425;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1641.class)
abstract class MixinZombieVillagerEntity implements IVillagerEntity {
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

   @ModifyVariable(method = "method_7195", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private class_3850 setVillagerData(class_3850 villagerData) {
      class_3852 profession = villagerData.method_16924();
      if (profession.toString().startsWith("mca.")) {
         villagerData = villagerData.method_16921(class_3852.field_17051);
      }

      return villagerData;
   }
}
