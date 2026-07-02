package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.ClientEngine;

public class CombatKeyMapping extends KeyMapping {
   public CombatKeyMapping(String description, int code, String category) {
      this(description, Type.KEYSYM, code, category);
   }

   public CombatKeyMapping(String description, Type type, int code, String category) {
      super(description, KeyConflictContext.IN_GAME, type, code, category);
   }

   public boolean isActiveAndMatches(@NotNull Key keyCode) {
      return super.isActiveAndMatches(keyCode) && ClientEngine.getInstance().isEpicFightMode();
   }
}
