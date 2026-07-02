package yesman.epicfight.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerConfig {
   private static final Builder BUILDER = new Builder();
   public static final BooleanValue ALLOW_CUSTOM_ANIMATIONS = BUILDER.define("allow_custom_animations", false);
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   public static boolean allowCustomAnimations;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      if (event.getConfig().getType() == Type.SERVER) {
         allowCustomAnimations = (Boolean)ALLOW_CUSTOM_ANIMATIONS.get();
      }
   }
}
