package forge.net.mca.mixin;

import forge.net.mca.server.world.data.VillageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class MixinFlintAndSteelItem {
   @Inject(method = "m_6225_(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))
   private void mca$onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
      if (((InteractionResult)cir.getReturnValue()).m_19077_() && context.m_43725_() instanceof ServerLevel serverWorld) {
         serverWorld.m_7654_().execute(() -> VillageManager.get(serverWorld).getReaperSpawner().trySpawnReaper(serverWorld, context.m_8083_()));
      }
   }
}
