package forge.net.mca.mixin;

import forge.net.mca.Config;
import java.util.Arrays;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementRewards.class)
public abstract class MixinAdvancementRewards {
   @Unique
   private static final ThreadLocal<ResourceLocation[]> mca$savedLoot = new ThreadLocal<>();

   @Inject(method = "m_9989_", at = @At("HEAD"))
   private void mca$filterBooksStart(ServerPlayer player, CallbackInfo ci) {
      if (!Config.getInstance().giveAdvancementBooks) {
         ResourceLocation[] loot = ((MixinAdvancementRewardsAccessor)this).getLoot();
         if (loot != null && Arrays.stream(loot).anyMatch(id -> id.toString().startsWith("mca:books/"))) {
            mca$savedLoot.set(loot);
            ResourceLocation[] filtered = Arrays.stream(loot).filter(id -> !id.toString().startsWith("mca:books/")).toArray(ResourceLocation[]::new);
            ((MixinAdvancementRewardsAccessor)this).setLoot(filtered);
         }
      }
   }

   @Inject(method = "m_9989_", at = @At("RETURN"))
   private void mca$filterBooksEnd(ServerPlayer player, CallbackInfo ci) {
      ResourceLocation[] saved = mca$savedLoot.get();
      if (saved != null) {
         mca$savedLoot.remove();
         ((MixinAdvancementRewardsAccessor)this).setLoot(saved);
      }
   }
}
