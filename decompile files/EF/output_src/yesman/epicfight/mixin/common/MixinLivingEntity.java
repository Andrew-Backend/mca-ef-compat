package yesman.epicfight.mixin.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributeSupplier;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
   @Shadow
   protected abstract void m_6472_(DamageSource var1, float var2);

   @Inject(at = @At("TAIL"), method = "<clinit>")
   private static void epicfight$staticInitialize(CallbackInfo callbackInfo) {
      LivingEntityPatch.initLivingEntityDataAccessor();
   }

   @Inject(at = @At("TAIL"), method = "defineSynchedData()V", cancellable = true)
   protected void epicfight$defineSynchedData(CallbackInfo info) {
      LivingEntityPatch.createSyncedEntityData((LivingEntity)this);
   }

   @Inject(at = @At("TAIL"), method = "blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V", cancellable = true)
   private void epicfight$blockUsingShield(LivingEntity p_21200_, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      LivingEntityPatch<?> opponentEntitypatch = EpicFightCapabilities.getEntityPatch(p_21200_, LivingEntityPatch.class);
      LivingEntityPatch<?> selfEntitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
      if (opponentEntitypatch != null) {
         opponentEntitypatch.setLastAttackResult(AttackResult.blocked(0.0F));
         if (selfEntitypatch != null && opponentEntitypatch.getEpicFightDamageSource() != null) {
            opponentEntitypatch.onAttackBlocked(opponentEntitypatch.getEpicFightDamageSource(), selfEntitypatch);
         }
      }
   }

   @Inject(at = @At("RETURN"), method = "hurt", cancellable = true)
   private void epicfight$hurt(DamageSource damagesource, float amount, CallbackInfoReturnable<Boolean> info) {
      LivingEntity self = (LivingEntity)this;
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.m_7639_(), LivingEntityPatch.class);
      if (entitypatch != null && (Boolean)info.getReturnValue()) {
         entitypatch.setLastAttackEntity(self);
      }
   }

   @Inject(at = @At("HEAD"), method = "push(Lnet/minecraft/world/entity/Entity;)V", cancellable = true)
   private void epicfight$push(Entity p_20293_, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
      if (entitypatch != null && !entitypatch.canPush(p_20293_)) {
         info.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", cancellable = true)
   private void epicfight$getDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> info) {
      if (source instanceof EpicFightDamageSource epicFightDamageSource && !source.m_269533_(DamageTypeTags.f_268490_)) {
         this.m_6472_(source, amount);
         float armorNegationAmount = amount * Math.min(epicFightDamageSource.calculateArmorNegation() * 0.01F, 1.0F);
         float amountElse = amount - armorNegationAmount;
         LivingEntity self = (LivingEntity)this;
         amountElse = CombatRules.m_19272_(amountElse, self.m_21230_(), (float)self.m_21133_(Attributes.f_22285_));
         info.setReturnValue(armorNegationAmount + amountElse);
         info.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
   private void epicfight$readAdditionalSaveData(CompoundTag compTag, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
      if (entitypatch != null) {
         entitypatch.initAttributesFromCompound(compTag);
      }
   }

   @Inject(at = @At("HEAD"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
   private void epicfight$addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
      if (entitypatch != null) {
         entitypatch.saveData(compoundTag);
      }
   }

   @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", cancellable = true)
   private void epicfight$constructor(EntityType<?> entityType, Level level, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      EpicFightCapabilities.getUnparameterizedEntityPatch(self, HurtableEntityPatch.class)
         .ifPresent(entitypatch -> self.m_21204_().f_22141_ = new EpicFightAttributeSupplier(self.m_21204_().f_22141_));
   }

   @Inject(at = @At("TAIL"), method = "setAbsorptionAmount(F)V", cancellable = true)
   private void epicfight$setAbsorptionAmount(float absorptionAmount, CallbackInfo info) {
      LivingEntity self = (LivingEntity)this;
      if (!self.m_9236_().m_5776_()) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAbsorption(self.m_19879_(), absorptionAmount), self);
      }
   }

   @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"), method = "jumpFromGround()V")
   private float epicfight$jumpFromGround(LivingEntity livingEntity) {
      if (livingEntity instanceof Player player && player.m_7578_()) {
         EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
         return cameraApi.isTPSMode() ? cameraApi.getCameraYRot() : livingEntity.m_146908_();
      } else {
         return livingEntity.m_146908_();
      }
   }

   @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F", ordinal = 0), method = "tick()V")
   private float epicfight$tick(LivingEntity livingEntity) {
      return livingEntity instanceof Player player && player.m_7578_() ? EpicFightCameraAPI.getInstance().getYRotForHead(player) : livingEntity.m_146908_();
   }

   @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"), method = "tickHeadTurn(FF)F")
   protected float epicfight$tickHeadTurn(LivingEntity livingEntity) {
      return livingEntity instanceof Player player && player.m_7578_() ? EpicFightCameraAPI.getInstance().getYRotForHead(player) : livingEntity.m_146908_();
   }
}
