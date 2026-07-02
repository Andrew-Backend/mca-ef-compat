package forge.net.mca.mixin;

import forge.net.mca.ducks.IVillagerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
abstract class MixinVillagerEntity implements IVillagerEntity {
   @Nullable
   private transient MobSpawnType reason;

   @Override
   public MobSpawnType getSpawnReason() {
      return this.reason == null ? MobSpawnType.NATURAL : this.reason;
   }

   @Inject(method = "m_6518_", at = @At("HEAD"))
   private void onInitialize(
      ServerLevelAccessor world,
      DifficultyInstance difficulty,
      MobSpawnType spawnReason,
      @Nullable SpawnGroupData entityData,
      @Nullable CompoundTag entityNbt,
      CallbackInfoReturnable<SpawnGroupData> info
   ) {
      this.reason = spawnReason;
   }
}
