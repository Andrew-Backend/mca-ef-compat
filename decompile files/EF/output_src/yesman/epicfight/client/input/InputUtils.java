package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;

@Internal
public final class InputUtils {
   private InputUtils() {
   }

   public static void runKeyboardMouseEvent(@NotNull EpicFightInputAction action, @NotNull Runnable handler) {
      KeyMapping keyMapping = action.keyMapping();
      Key key = keyMapping.getKey();
      boolean isMouse = Type.MOUSE == key.m_84868_();
      int mouseButton = isMouse ? key.m_84873_() : -1;
      if (checkInteractionKeyUsable(mouseButton, keyMapping)) {
         handler.run();
      }
   }

   public static void sneakingTick(boolean isSneaking, float sneakingSpeedMultiplier) {
      LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
      if (localPlayer != null) {
         sneakingTick(localPlayer, isSneaking, sneakingSpeedMultiplier);
      }
   }

   public static boolean checkInteractionKeyUsable(int mouseButton, KeyMapping keyMapping) {
      Options option = Minecraft.m_91087_().f_91066_;
      if (keyMapping != option.f_92096_ && keyMapping != option.f_92095_ && keyMapping != option.f_92097_) {
         return true;
      }

      InteractionKeyMappingTriggered inputEvent = ForgeHooksClient.onClickInput(mouseButton, keyMapping, InteractionHand.MAIN_HAND);
      return !inputEvent.isCanceled();
   }

   public static void sneakingTick(@NotNull LocalPlayer player, boolean isSneaking, float sneakingSpeedMultiplier) {
      player.f_108618_.m_214106_(isSneaking, sneakingSpeedMultiplier);
   }
}
