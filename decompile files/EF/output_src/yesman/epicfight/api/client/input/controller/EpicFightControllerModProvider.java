package yesman.epicfight.api.client.input.controller;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;
import yesman.epicfight.main.EpicFightMod;

@Experimental
public final class EpicFightControllerModProvider {
   @Nullable
   @org.jetbrains.annotations.Nullable
   private static IEpicFightControllerMod instance = null;
   private static boolean overriddenByOtherMod;

   private EpicFightControllerModProvider() {
   }

   public static void set(@NotNull String registrantModId, @NotNull IEpicFightControllerMod modInstance) {
      if (overriddenByOtherMod) {
         EpicFightMod.LOGGER
            .warn(
               "Mod '{}' is overriding the Epic Fight controller implementation, which was already set by another mod. Only the last registered implementation will be used. This may occur if multiple controller mods are installed.",
               registrantModId
            );
      }

      instance = modInstance;
      if (registrantModId.equals("epicfight")) {
         EpicFightMod.LOGGER.info("Epic Fight detected and registered supported controller mod: '{}'.", modInstance.getModName());
      } else {
         overriddenByOtherMod = true;
      }
   }

   @org.jetbrains.annotations.Nullable
   public static IEpicFightControllerMod get() {
      return instance;
   }
}
