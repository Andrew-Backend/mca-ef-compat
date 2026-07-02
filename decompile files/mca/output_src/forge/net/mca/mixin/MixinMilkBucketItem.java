package forge.net.mca.mixin;

import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MixinMilkBucketItem {
   @Inject(method = "m_5922_", at = @At("RETURN"))
   public void onFinishedUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
      VillagerLike<?> villagerLike = world.f_46443_ ? CommonVillagerModel.getVillager(user) : VillagerLike.toVillager(user);
      if (villagerLike != null && villagerLike.getTraits().hasTrait(Traits.LACTOSE_INTOLERANCE)) {
         user.m_7292_(new MobEffectInstance(MobEffects.f_19614_, 100, 0));
      }
   }
}
