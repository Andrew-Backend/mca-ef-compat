package fabric.net.mca.mixin;

import fabric.net.mca.client.model.CommonVillagerModel;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.Traits;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1805;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1805.class)
public class MixinMilkBucketItem {
   @Inject(method = "method_7861", at = @At("RETURN"))
   public void onFinishedUsing(class_1799 stack, class_1937 world, class_1309 user, CallbackInfoReturnable<class_1799> cir) {
      VillagerLike<?> villagerLike = world.field_9236 ? CommonVillagerModel.getVillager(user) : VillagerLike.toVillager(user);
      if (villagerLike != null && villagerLike.getTraits().hasTrait(Traits.LACTOSE_INTOLERANCE)) {
         user.method_6092(new class_1293(class_1294.field_5899, 100, 0));
      }
   }
}
