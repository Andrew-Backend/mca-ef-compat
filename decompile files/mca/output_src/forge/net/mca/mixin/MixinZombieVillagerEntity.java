package forge.net.mca.mixin;

import forge.net.mca.ducks.IVillagerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
abstract class MixinZombieVillagerEntity implements IVillagerEntity {
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

   @ModifyVariable(method = "m_34375_", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private VillagerData setVillagerData(VillagerData villagerData) {
      VillagerProfession profession = villagerData.m_35571_();
      if (profession.toString().startsWith("mca.")) {
         villagerData = villagerData.m_35565_(VillagerProfession.f_35585_);
      }

      return villagerData;
   }
}
