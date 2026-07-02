package fabric.net.mca.mixin;

import fabric.net.mca.Config;
import java.util.Arrays;
import net.minecraft.class_170;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_170.class)
public abstract class MixinAdvancementRewards {
   @Unique
   private static final ThreadLocal<class_2960[]> mca$savedLoot = new ThreadLocal<>();

   @Inject(method = "method_748", at = @At("HEAD"))
   private void mca$filterBooksStart(class_3222 player, CallbackInfo ci) {
      if (!Config.getInstance().giveAdvancementBooks) {
         class_2960[] loot = ((MixinAdvancementRewardsAccessor)this).getLoot();
         if (loot != null && Arrays.stream(loot).anyMatch(id -> id.toString().startsWith("mca:books/"))) {
            mca$savedLoot.set(loot);
            class_2960[] filtered = Arrays.stream(loot).filter(id -> !id.toString().startsWith("mca:books/")).toArray(class_2960[]::new);
            ((MixinAdvancementRewardsAccessor)this).setLoot(filtered);
         }
      }
   }

   @Inject(method = "method_748", at = @At("RETURN"))
   private void mca$filterBooksEnd(class_3222 player, CallbackInfo ci) {
      class_2960[] saved = mca$savedLoot.get();
      if (saved != null) {
         mca$savedLoot.remove();
         ((MixinAdvancementRewardsAccessor)this).setLoot(saved);
      }
   }
}
