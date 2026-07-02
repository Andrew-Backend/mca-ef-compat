package forge.net.mca.mixin.client;

import forge.net.mca.Config;
import forge.net.mca.MCAClient;
import forge.net.mca.entity.VillagerLike;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class MixinPlayerEntityClient extends LivingEntity {
   protected MixinPlayerEntityClient(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Inject(method = "m_6431_(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F", at = @At("RETURN"), cancellable = true)
   public void mca$getActiveEyeHeight(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
      if (Config.getInstance().scaleEyeHeightWithPlayerHeight && !this.m_217003_(Pose.SLEEPING)) {
         MCAClient.getPlayerData(this.m_20148_())
            .filter(data -> data.getPlayerModel() != VillagerLike.PlayerModel.VANILLA)
            .ifPresent(data -> cir.setReturnValue(Math.min(this.m_20206_() - 0.0625F, (Float)cir.getReturnValue() * data.getRawScaleFactor())));
      }
   }
}
