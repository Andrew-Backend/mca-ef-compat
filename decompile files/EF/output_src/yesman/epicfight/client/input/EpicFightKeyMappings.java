package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT, bus = Bus.MOD)
public class EpicFightKeyMappings {
   public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP = new KeyMapping(
      "key.epicfight.show_tooltip", KeyConflictContext.GUI, Type.KEYSYM, 340, "key.epicfight.gui"
   );
   public static final KeyMapping SKILL_EDIT = new KeyMapping("key.epicfight.skill_gui", KeyConflictContext.IN_GAME, Type.KEYSYM, 75, "key.epicfight.gui");
   public static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping("key.epicfight.config", KeyConflictContext.IN_GAME, Type.KEYSYM, -1, "key.epicfight.gui");
   public static final KeyMapping DODGE = new CombatKeyMapping("key.epicfight.dodge", 342, "key.epicfight.combat");
   public static final KeyMapping GUARD = new CombatKeyMapping("key.epicfight.guard", Type.MOUSE, 1, "key.epicfight.combat");
   public static final KeyMapping ATTACK = new CombatKeyMapping("key.epicfight.attack", Type.MOUSE, 0, "key.epicfight.combat");
   public static final KeyMapping WEAPON_INNATE_SKILL = new CombatKeyMapping("key.epicfight.weapon_innate_skill", Type.MOUSE, 0, "key.epicfight.combat");
   public static final KeyMapping MOVER_SKILL = new CombatKeyMapping("key.epicfight.mover_skill", 32, "key.epicfight.combat");
   public static final KeyMapping SWITCH_MODE = new KeyMapping("key.epicfight.switch_mode", KeyConflictContext.IN_GAME, Type.KEYSYM, 82, "key.epicfight.combat");
   public static final KeyMapping LOCK_ON = new KeyMapping("key.epicfight.lock_on", KeyConflictContext.IN_GAME, Type.KEYSYM, 71, "key.epicfight.camera");
   public static final KeyMapping LOCK_ON_SHIFT_LEFT = new KeyMapping(
      "key.epicfight.lock_on_shift_left", KeyConflictContext.IN_GAME, Type.KEYSYM, 263, "key.epicfight.camera"
   );
   public static final KeyMapping LOCK_ON_SHIFT_RIGHT = new KeyMapping(
      "key.epicfight.lock_on_shift_right", KeyConflictContext.IN_GAME, Type.KEYSYM, 262, "key.epicfight.camera"
   );
   public static final KeyMapping LOCK_ON_SHIFT_FREELY = new KeyMapping(
      "key.epicfight.lock_on_shift_freely", KeyConflictContext.IN_GAME, Type.MOUSE, 2, "key.epicfight.camera"
   );
   public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING = new KeyMapping(
      "key.epicfight.switch_vanilla_model_debug", KeyConflictContext.IN_GAME, Type.KEYSYM, -1, "key.epicfight.system"
   );

   @SubscribeEvent
   public static void registerKeys(RegisterKeyMappingsEvent event) {
      event.register(WEAPON_INNATE_SKILL_TOOLTIP);
      event.register(SWITCH_MODE);
      event.register(DODGE);
      event.register(GUARD);
      event.register(ATTACK);
      event.register(WEAPON_INNATE_SKILL);
      event.register(MOVER_SKILL);
      event.register(SKILL_EDIT);
      event.register(LOCK_ON);
      event.register(LOCK_ON_SHIFT_LEFT);
      event.register(LOCK_ON_SHIFT_RIGHT);
      event.register(LOCK_ON_SHIFT_FREELY);
      event.register(OPEN_CONFIG_SCREEN);
      event.register(SWITCH_VANILLA_MODEL_DEBUGGING);
   }
}
