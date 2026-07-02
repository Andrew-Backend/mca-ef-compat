package yesman.epicfight.main;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;

@OnlyIn(Dist.CLIENT)
public interface AuthenticationHelper {
   boolean valid();

   void initialize(ConfigValue<String> var1, ConfigValue<String> var2, EnumValue<AuthenticationHelper.AuthenticationProvider> var3);

   default Screen getAvatarEditorScreen(Screen parentScreen) {
      return null;
   }

   AuthenticationHelper.Status status();

   @OnlyIn(Dist.CLIENT)
   enum AuthenticationProvider {
      NULL("null"),
      DISCORD("discord"),
      PATREON("patreon");

      String signature;

      AuthenticationProvider(String signature) {
         this.signature = signature;
      }

      @Override
      public String toString() {
         return this.signature;
      }
   }

   @OnlyIn(Dist.CLIENT)
   enum Status {
      UNAUTHENTICATED,
      AUTHENTICATED,
      OFFLINE_MODE;
   }
}
