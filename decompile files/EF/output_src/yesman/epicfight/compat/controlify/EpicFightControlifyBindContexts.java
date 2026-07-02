package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.bindings.BindContext;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightControlifyBindContexts {
   public static final BindContext IN_GAME = BindContext.IN_GAME;
   public static final BindContext ANY_SCREEN = BindContext.ANY_SCREEN;

   private EpicFightControlifyBindContexts() {
   }

   public static boolean isInGame(@NotNull Minecraft mc) {
      return mc.f_91080_ == null && mc.f_91073_ != null && mc.f_91074_ != null;
   }

   public static final class EpicFight {
      public static final BindContext COMBAT_MODE = new BindContext(EpicFightMod.identifier("epicfight_combat"), mc -> {
         boolean isInGame = EpicFightControlifyBindContexts.isInGame(mc);
         return isInGame && ClientEngine.getInstance().isEpicFightMode();
      });
      public static final BindContext LOCK_ON = new BindContext(EpicFightMod.identifier("epicfight_lock_on"), mc -> {
         boolean isInGame = EpicFightControlifyBindContexts.isInGame(mc);
         return isInGame && EpicFightCameraAPI.getInstance().isLockingOnTarget();
      });

      private EpicFight() {
      }

      public static void register(@NotNull ControlifyBindApi registrar) {
         registrar.registerBindContext(COMBAT_MODE);
         registrar.registerBindContext(LOCK_ON);
      }
   }
}
