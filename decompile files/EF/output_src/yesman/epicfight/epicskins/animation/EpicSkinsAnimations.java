package yesman.epicfight.epicskins.animation;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD)
public class EpicSkinsAnimations {
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_IDLE1;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_IDLE2;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_STANDING;

   @SubscribeEvent
   public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
      event.newBuilder("epicskins", EpicSkinsAnimations::build);
   }

   public static void build(AnimationManager.AnimationBuilder builder) {
      BIPED_IDLE1 = builder.nextAccessor("biped/skinscreen_idle1", accessor -> new StaticAnimation(false, accessor, Armatures.BIPED));
      BIPED_IDLE2 = builder.nextAccessor("biped/skinscreen_idle2", accessor -> new StaticAnimation(false, accessor, Armatures.BIPED));
      BIPED_STANDING = builder.nextAccessor("biped/skinscreen_stand", accessor -> new StaticAnimation(false, accessor, Armatures.BIPED));
   }
}
