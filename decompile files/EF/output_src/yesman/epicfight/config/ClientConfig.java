package yesman.epicfight.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.utils.CirculatableEnum;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.ScreenCalculations;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientConfig {
   private static final Builder BUILDER = new Builder();
   public static final BooleanValue SHOW_TARGET_INDICATOR = BUILDER.define("ingame.show_target_indicator", () -> true);
   public static final EnumValue<ClientConfig.HealthBarVisibility> HEALTH_BAR_VISIBILITY = BUILDER.defineEnum(
      "ingame.health_bar_show_option", ClientConfig.HealthBarVisibility.HURT
   );
   public static final BooleanValue SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP = BUILDER.define("ingame.show_epicfight_attributes", () -> true);
   public static final DoubleValue TARGET_OUTLINE_COLOR = BUILDER.defineInRange("ingame.target_outline_color", 0.0, 0.0, 1.0);
   public static final EnumValue<ClientConfig.BlockGuideOptions> MINE_BLOCK_GUIDE_OPTION = BUILDER.defineEnum(
      "ingame.mine_block_guide_option", ClientConfig.BlockGuideOptions.CROSSHAIR
   );
   public static final BooleanValue ENABLE_TARGET_ENTITY_GUIDE = BUILDER.define("ingame.enable_target_entity_guide", () -> true);
   public static final BooleanValue BLOOD_EFFECTS = BUILDER.define("ingame.blood_effects", () -> true);
   public static final BooleanValue GROUND_SLAMS = BUILDER.define("ingame.ground_slams", () -> true);
   public static final IntValue MAX_STUCK_PROJECTILES = BUILDER.defineInRange("ingame.max_hit_projectiles", 30, 0, 30);
   public static final BooleanValue ENABLE_ANIMATED_FIRST_PERSON_MODEL = BUILDER.define("ingame.first_person_model", () -> true);
   public static final BooleanValue ENABLE_PLAYER_VANILLA_MODEL = BUILDER.define("ingame.enable_player_vanilla_model", () -> true);
   public static final BooleanValue ENABLE_COSMETICS = BUILDER.define("ingame.enable_cosmetics", () -> true);
   public static final BooleanValue ACTIVATE_COMPUTE_SHADER = BUILDER.define("ingame.use_compute_shader", () -> false);
   public static final BooleanValue ENABLE_POV_ACTION = BUILDER.define("ingame.enable_pov_action", () -> true);
   public static final ConfigValue<ClientConfig.TPSType> CAMERA_MODE = BUILDER.defineEnum("ingame.camera.camera_mode", ClientConfig.TPSType.WHEN_AIMING);
   public static final IntValue CAMERA_HORIZONTAL_LOCATION = BUILDER.defineInRange("ingame.camera.horizontal_location", -5, -10, 10);
   public static final IntValue CAMERA_VERTICAL_LOCATION = BUILDER.defineInRange("ingame.camera.vertical_location", 0, -2, 5);
   public static final IntValue CAMERA_ZOOM = BUILDER.defineInRange("ingame.camera.zoom", 3, 6, 10);
   public static final IntValue LOCK_ON_RANGE = BUILDER.defineInRange("ingame.camera.lock_on_range", 20, 5, 25);
   public static final IntValue LONG_PRESS_COUNTER = BUILDER.defineInRange("ingame.long_press_count", 2, 1, 10);
   public static final BooleanValue AUTO_SWITCH_CAMERA = BUILDER.define("ingame.camera_auto_switch", () -> false);
   public static final BooleanValue LOCK_ON_QUICK_SHIFT = BUILDER.define("ingame.camera.lock_on_quick_shift", () -> true);
   public static final EnumValue<ClientConfig.KeyConflictResolveScope> KEY_CONFLICT_RESOLVE_SCOPE = BUILDER.defineEnum(
      "ingame.key_conflict_resolve_scope", ClientConfig.KeyConflictResolveScope.INTERACTION
   );
   public static final EnumValue<ClientConfig.PreferenceWork> PREFERENCE_WORK = BUILDER.defineEnum(
      "ingame.preference_work", ClientConfig.PreferenceWork.ADAPTIVE
   );
   public static final EnumValue<ClientConfig.CameraPerspectiveToggleMode> CAMERA_PERSPECTIVE_TOGGLE_MODE = BUILDER.comment(
         "Defines how the camera toggles perspectives.\n\n    1. Vanilla (Default)\n       Uses Minecraft's default behavior.\n       Cycles through all available perspectives.\n\n    2. Skip Third-Person Front Perspective\n       Skips only the front view when toggling.\n       Other perspectives remain available.\n"
      )
      .defineEnum("ingame.camera_perspective_toggle_mode", ClientConfig.CameraPerspectiveToggleMode.VANILLA);
   public static final ConfigValue<List<? extends String>> BATTLE_MODE_SWITCHING_ITEMS = BUILDER.defineList(
      "ingame.combat_preferred_items", Lists.newArrayList(), element -> element instanceof String str ? str.contains(":") : false
   );
   public static final ConfigValue<List<? extends String>> MINING_MODE_SWITCHING_ITEMS = BUILDER.defineList(
      "ingame.mining_preferred_items", Lists.newArrayList(), element -> element instanceof String str ? str.contains(":") : false
   );
   public static final ConfigValue<Integer> STAMINA_BAR_X = BUILDER.define("ingame.ui.stamina_bar_x", 120);
   public static final ConfigValue<Integer> STAMINA_BAR_Y = BUILDER.define("ingame.ui.stamina_bar_y", 10);
   public static final EnumValue<ScreenCalculations.HorizontalBasis> STAMINA_BAR_BASE_X = BUILDER.defineEnum(
      "ingame.ui.stamina_bar_x_base", ScreenCalculations.HorizontalBasis.RIGHT
   );
   public static final EnumValue<ScreenCalculations.VerticalBasis> STAMINA_BAR_BASE_Y = BUILDER.defineEnum(
      "ingame.ui.stamina_bar_y_base", ScreenCalculations.VerticalBasis.BOTTOM
   );
   public static final ConfigValue<Integer> WEAPON_INNATE_X = BUILDER.define("ingame.ui.weapon_innate_x", 42);
   public static final ConfigValue<Integer> WEAPON_INNATE_Y = BUILDER.define("ingame.ui.weapon_innate_y", 48);
   public static final EnumValue<ScreenCalculations.HorizontalBasis> WEAPON_INNATE_BASE_X = BUILDER.defineEnum(
      "ingame.ui.weapon_innate_x_base", ScreenCalculations.HorizontalBasis.RIGHT
   );
   public static final EnumValue<ScreenCalculations.VerticalBasis> WEAPON_INNATE_BASE_Y = BUILDER.defineEnum(
      "ingame.ui.weapon_innate_y_base", ScreenCalculations.VerticalBasis.BOTTOM
   );
   public static final ConfigValue<Integer> PASSIVE_X = BUILDER.define("ingame.ui.passives_x", 70);
   public static final ConfigValue<Integer> PASSIVE_Y = BUILDER.define("ingame.ui.passives_y", 36);
   public static final EnumValue<ScreenCalculations.HorizontalBasis> PASSIVE_BASE_X = BUILDER.defineEnum(
      "ingame.ui.passives_x_base", ScreenCalculations.HorizontalBasis.RIGHT
   );
   public static final EnumValue<ScreenCalculations.VerticalBasis> PASSIVE_BASE_Y = BUILDER.defineEnum(
      "ingame.ui.passives_y_base", ScreenCalculations.VerticalBasis.BOTTOM
   );
   public static final EnumValue<ScreenCalculations.AlignDirection> PASSIVE_ALIGN_DIRECTION = BUILDER.defineEnum(
      "ingame.ui.passives_align_direction", ScreenCalculations.AlignDirection.HORIZONTAL
   );
   public static final ConfigValue<Integer> CHARGING_BAR_X = BUILDER.define("ingame.ui.charging_bar_x", -119);
   public static final ConfigValue<Integer> CHARGING_BAR_Y = BUILDER.define("ingame.ui.charging_bar_y", 60);
   public static final EnumValue<ScreenCalculations.HorizontalBasis> CHARGING_BAR_BASE_X = BUILDER.defineEnum(
      "ingame.ui.charging_bar_x_base", ScreenCalculations.HorizontalBasis.CENTER
   );
   public static final EnumValue<ScreenCalculations.VerticalBasis> CHARGING_BAR_BASE_Y = BUILDER.defineEnum(
      "ingame.ui.charging_bar_y_base", ScreenCalculations.VerticalBasis.CENTER
   );
   public static final ConfigValue<String> ACCESS_TOKEN = BUILDER.comment("Login information for epic fight patron server. Do not change these values manually")
      .define("access_token", "");
   public static final ConfigValue<String> REFRESH_TOKNE = BUILDER.define("refresh_token", "");
   public static final EnumValue<AuthenticationHelper.AuthenticationProvider> PROVIDER = BUILDER.defineEnum(
      "provider", AuthenticationHelper.AuthenticationProvider.NULL
   );
   public static final ForgeConfigSpec SPEC = BUILDER.build();
   public static int maxStuckProjectiles;
   public static double targetOutlineColor;
   public static int packedTargetOutlineColor = -1;
   public static boolean bloodEffects;
   public static boolean groundSlams;
   public static boolean showEpicFightAttributesInTooltip;
   public static boolean activateComputeShader;
   public static boolean enableAnimatedFirstPersonModel;
   public static ClientConfig.BlockGuideOptions mineBlockGuideOption;
   public static boolean enableTargetEntityGuide;
   public static boolean enablePovAction;
   public static boolean enableCosmetics;
   public static boolean enableOriginalModel;
   public static int longPressCounter;
   public static boolean autoSwitchCamera;
   public static boolean lockOnQuickShift;
   public static ClientConfig.KeyConflictResolveScope keyConflictResolveScope;
   public static ClientConfig.PreferenceWork preferenceWork;
   public static ClientConfig.CameraPerspectiveToggleMode cameraPerspectiveToggleMode;
   public static Set<Item> combatPreferredItems;
   public static Set<Item> miningPreferredItems;
   @Deprecated
   @Internal
   public static ClientConfig.TPSType cameraMode;
   public static int cameraHorizontalLocation;
   public static int cameraVerticalLocation;
   public static int cameraZoom;
   public static int lockOnRange;
   public static boolean showTargetIndicator;
   public static ClientConfig.HealthBarVisibility healthBarVisibility;
   public static int staminaBarX;
   public static int staminaBarY;
   public static ScreenCalculations.HorizontalBasis staminaBarBaseX;
   public static ScreenCalculations.VerticalBasis staminaBarBaseY;
   public static int weaponInnateX;
   public static int weaponInnateY;
   public static ScreenCalculations.HorizontalBasis weaponInnateBaseX;
   public static ScreenCalculations.VerticalBasis weaponInnateBaseY;
   public static int passiveX;
   public static int passiveY;
   public static ScreenCalculations.HorizontalBasis passiveBaseX;
   public static ScreenCalculations.VerticalBasis passiveBaseY;
   public static ScreenCalculations.AlignDirection passiveAlignDirection;
   public static int chargingBarX;
   public static int chargingBarY;
   public static ScreenCalculations.HorizontalBasis chargingBarBaseX;
   public static ScreenCalculations.VerticalBasis chargingBarBaseY;

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      if (event.getConfig().getType() == Type.CLIENT) {
         maxStuckProjectiles = (Integer)MAX_STUCK_PROJECTILES.get();
         targetOutlineColor = (Double)TARGET_OUTLINE_COLOR.get();
         packedTargetOutlineColor = ColorSlider.rgbColor(targetOutlineColor);
         bloodEffects = (Boolean)BLOOD_EFFECTS.get();
         groundSlams = (Boolean)GROUND_SLAMS.get();
         showEpicFightAttributesInTooltip = (Boolean)SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get();
         activateComputeShader = (Boolean)ACTIVATE_COMPUTE_SHADER.get();
         enableAnimatedFirstPersonModel = (Boolean)ENABLE_ANIMATED_FIRST_PERSON_MODEL.get();
         mineBlockGuideOption = (ClientConfig.BlockGuideOptions)MINE_BLOCK_GUIDE_OPTION.get();
         enableTargetEntityGuide = (Boolean)ENABLE_TARGET_ENTITY_GUIDE.get();
         enablePovAction = (Boolean)ENABLE_POV_ACTION.get();
         enableCosmetics = (Boolean)ENABLE_COSMETICS.get();
         enableOriginalModel = (Boolean)ENABLE_PLAYER_VANILLA_MODEL.get();
         cameraMode = (ClientConfig.TPSType)CAMERA_MODE.get();
         cameraHorizontalLocation = (Integer)CAMERA_HORIZONTAL_LOCATION.get();
         cameraVerticalLocation = (Integer)CAMERA_VERTICAL_LOCATION.get();
         cameraZoom = (Integer)CAMERA_ZOOM.get();
         lockOnRange = (Integer)LOCK_ON_RANGE.get();
         longPressCounter = (Integer)LONG_PRESS_COUNTER.get();
         autoSwitchCamera = (Boolean)AUTO_SWITCH_CAMERA.get();
         lockOnQuickShift = (Boolean)LOCK_ON_QUICK_SHIFT.get();
         keyConflictResolveScope = (ClientConfig.KeyConflictResolveScope)KEY_CONFLICT_RESOLVE_SCOPE.get();
         preferenceWork = (ClientConfig.PreferenceWork)PREFERENCE_WORK.get();
         cameraPerspectiveToggleMode = (ClientConfig.CameraPerspectiveToggleMode)CAMERA_PERSPECTIVE_TOGGLE_MODE.get();
         combatPreferredItems = ((List)BATTLE_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet());
         miningPreferredItems = ((List)MINING_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet());
         if (combatPreferredItems.isEmpty() && miningPreferredItems.isEmpty()) {
            ItemsPreferenceScreen.resetItems();
         }

         showTargetIndicator = (Boolean)SHOW_TARGET_INDICATOR.get();
         healthBarVisibility = (ClientConfig.HealthBarVisibility)HEALTH_BAR_VISIBILITY.get();
         staminaBarX = (Integer)STAMINA_BAR_X.get();
         staminaBarY = (Integer)STAMINA_BAR_Y.get();
         staminaBarBaseX = (ScreenCalculations.HorizontalBasis)STAMINA_BAR_BASE_X.get();
         staminaBarBaseY = (ScreenCalculations.VerticalBasis)STAMINA_BAR_BASE_Y.get();
         weaponInnateX = (Integer)WEAPON_INNATE_X.get();
         weaponInnateY = (Integer)WEAPON_INNATE_Y.get();
         weaponInnateBaseX = (ScreenCalculations.HorizontalBasis)WEAPON_INNATE_BASE_X.get();
         weaponInnateBaseY = (ScreenCalculations.VerticalBasis)WEAPON_INNATE_BASE_Y.get();
         passiveX = (Integer)PASSIVE_X.get();
         passiveY = (Integer)PASSIVE_Y.get();
         passiveBaseX = (ScreenCalculations.HorizontalBasis)PASSIVE_BASE_X.get();
         passiveBaseY = (ScreenCalculations.VerticalBasis)PASSIVE_BASE_Y.get();
         passiveAlignDirection = (ScreenCalculations.AlignDirection)PASSIVE_ALIGN_DIRECTION.get();
         chargingBarX = (Integer)CHARGING_BAR_X.get();
         chargingBarY = (Integer)CHARGING_BAR_Y.get();
         chargingBarBaseX = (ScreenCalculations.HorizontalBasis)CHARGING_BAR_BASE_X.get();
         chargingBarBaseY = (ScreenCalculations.VerticalBasis)CHARGING_BAR_BASE_Y.get();
         if (EpicFightServerConnectionHelper.init(event.getConfig().getFullPath().getParent().toString())) {
            EpicFightMod.LOGGER.info("Epic Fight web server connection helper: supported");

            try {
               Class.forName("yesman.epicfight.epicskins.user.AuthenticationHelperImpl");
            } catch (Exception e) {
               EpicFightMod.LOGGER.info("Epic Fight web server status: Failed at initializing Authentication provider: " + e);
            }
         } else {
            EpicFightMod.LOGGER.info("Epic Fight web server connection helper: unsupported");
         }

         if (EpicFightServerConnectionHelper.supported() && ClientEngine.getInstance().getAuthHelper().valid()) {
            ClientEngine.getInstance().getAuthHelper().initialize(ACCESS_TOKEN, REFRESH_TOKNE, PROVIDER);
         }
      }
   }

   public static List<Runnable> getUnsaved() {
      List<Runnable> saveWorks = new ArrayList<>();
      if (maxStuckProjectiles != (Integer)MAX_STUCK_PROJECTILES.get()) {
         saveWorks.add(() -> MAX_STUCK_PROJECTILES.set(maxStuckProjectiles));
      }

      if (targetOutlineColor != (Double)TARGET_OUTLINE_COLOR.get()) {
         saveWorks.add(() -> {
            TARGET_OUTLINE_COLOR.set(targetOutlineColor);
            packedTargetOutlineColor = ColorSlider.rgbColor(targetOutlineColor);
         });
      }

      if (bloodEffects != (Boolean)BLOOD_EFFECTS.get()) {
         saveWorks.add(() -> BLOOD_EFFECTS.set(bloodEffects));
      }

      if (groundSlams != (Boolean)GROUND_SLAMS.get()) {
         saveWorks.add(() -> GROUND_SLAMS.set(groundSlams));
      }

      if (showEpicFightAttributesInTooltip != (Boolean)SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get()) {
         saveWorks.add(() -> SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.set(showEpicFightAttributesInTooltip));
      }

      if (activateComputeShader != (Boolean)ACTIVATE_COMPUTE_SHADER.get()) {
         saveWorks.add(() -> ACTIVATE_COMPUTE_SHADER.set(activateComputeShader));
      }

      if (enableAnimatedFirstPersonModel != (Boolean)ENABLE_ANIMATED_FIRST_PERSON_MODEL.get()) {
         saveWorks.add(() -> ENABLE_ANIMATED_FIRST_PERSON_MODEL.set(enableAnimatedFirstPersonModel));
      }

      if (mineBlockGuideOption != MINE_BLOCK_GUIDE_OPTION.get()) {
         saveWorks.add(() -> MINE_BLOCK_GUIDE_OPTION.set(mineBlockGuideOption));
      }

      if (enableTargetEntityGuide != (Boolean)ENABLE_TARGET_ENTITY_GUIDE.get()) {
         saveWorks.add(() -> ENABLE_TARGET_ENTITY_GUIDE.set(enableTargetEntityGuide));
      }

      if (enablePovAction != (Boolean)ENABLE_POV_ACTION.get()) {
         saveWorks.add(() -> ENABLE_POV_ACTION.set(enablePovAction));
      }

      if (enableCosmetics != (Boolean)ENABLE_COSMETICS.get()) {
         saveWorks.add(() -> ENABLE_COSMETICS.set(enableCosmetics));
      }

      if (enableOriginalModel != (Boolean)ENABLE_PLAYER_VANILLA_MODEL.get()) {
         saveWorks.add(() -> ENABLE_PLAYER_VANILLA_MODEL.set(enableOriginalModel));
      }

      if (cameraMode != CAMERA_MODE.get()) {
         saveWorks.add(() -> CAMERA_MODE.set(cameraMode));
      }

      if (cameraHorizontalLocation != (Integer)CAMERA_HORIZONTAL_LOCATION.get()) {
         saveWorks.add(() -> CAMERA_HORIZONTAL_LOCATION.set(cameraHorizontalLocation));
      }

      if (cameraVerticalLocation != (Integer)CAMERA_VERTICAL_LOCATION.get()) {
         saveWorks.add(() -> CAMERA_VERTICAL_LOCATION.set(cameraVerticalLocation));
      }

      if (cameraZoom != (Integer)CAMERA_ZOOM.get()) {
         saveWorks.add(() -> CAMERA_ZOOM.set(cameraZoom));
      }

      if (lockOnRange != (Integer)LOCK_ON_RANGE.get()) {
         saveWorks.add(() -> LOCK_ON_RANGE.set(lockOnRange));
      }

      if (longPressCounter != (Integer)LONG_PRESS_COUNTER.get()) {
         saveWorks.add(() -> LONG_PRESS_COUNTER.set(longPressCounter));
      }

      if (autoSwitchCamera != (Boolean)AUTO_SWITCH_CAMERA.get()) {
         saveWorks.add(() -> AUTO_SWITCH_CAMERA.set(autoSwitchCamera));
      }

      if (lockOnQuickShift != (Boolean)LOCK_ON_QUICK_SHIFT.get()) {
         saveWorks.add(() -> LOCK_ON_QUICK_SHIFT.set(lockOnQuickShift));
      }

      if (keyConflictResolveScope != KEY_CONFLICT_RESOLVE_SCOPE.get()) {
         saveWorks.add(() -> KEY_CONFLICT_RESOLVE_SCOPE.set(keyConflictResolveScope));
      }

      if (preferenceWork != PREFERENCE_WORK.get()) {
         saveWorks.add(() -> PREFERENCE_WORK.set(preferenceWork));
      }

      if (!combatPreferredItems.equals(
         ((List)BATTLE_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet())
      )) {
         saveWorks.add(
            () -> BATTLE_MODE_SWITCHING_ITEMS.set(
               combatPreferredItems.stream().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList())
            )
         );
      }

      if (!miningPreferredItems.equals(
         ((List)MINING_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet())
      )) {
         saveWorks.add(
            () -> MINING_MODE_SWITCHING_ITEMS.set(
               miningPreferredItems.stream().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList())
            )
         );
      }

      if (showTargetIndicator != (Boolean)SHOW_TARGET_INDICATOR.get()) {
         saveWorks.add(() -> SHOW_TARGET_INDICATOR.set(showTargetIndicator));
      }

      if (healthBarVisibility != HEALTH_BAR_VISIBILITY.get()) {
         saveWorks.add(() -> HEALTH_BAR_VISIBILITY.set(healthBarVisibility));
      }

      if (staminaBarX != (Integer)STAMINA_BAR_X.get()) {
         saveWorks.add(() -> STAMINA_BAR_X.set(staminaBarX));
      }

      if (staminaBarY != (Integer)STAMINA_BAR_Y.get()) {
         saveWorks.add(() -> STAMINA_BAR_Y.set(staminaBarY));
      }

      if (staminaBarBaseX != STAMINA_BAR_BASE_X.get()) {
         saveWorks.add(() -> STAMINA_BAR_BASE_X.set(staminaBarBaseX));
      }

      if (staminaBarBaseY != STAMINA_BAR_BASE_Y.get()) {
         saveWorks.add(() -> STAMINA_BAR_BASE_Y.set(staminaBarBaseY));
      }

      if (weaponInnateX != (Integer)WEAPON_INNATE_X.get()) {
         saveWorks.add(() -> WEAPON_INNATE_X.set(weaponInnateX));
      }

      if (weaponInnateX != (Integer)WEAPON_INNATE_Y.get()) {
         saveWorks.add(() -> WEAPON_INNATE_Y.set(weaponInnateY));
      }

      if (weaponInnateBaseX != WEAPON_INNATE_BASE_X.get()) {
         saveWorks.add(() -> WEAPON_INNATE_BASE_X.set(weaponInnateBaseX));
      }

      if (weaponInnateBaseY != WEAPON_INNATE_BASE_Y.get()) {
         saveWorks.add(() -> WEAPON_INNATE_BASE_Y.set(weaponInnateBaseY));
      }

      if (passiveX != (Integer)PASSIVE_X.get()) {
         saveWorks.add(() -> PASSIVE_X.set(passiveX));
      }

      if (passiveY != (Integer)PASSIVE_Y.get()) {
         saveWorks.add(() -> PASSIVE_Y.set(passiveY));
      }

      if (passiveBaseX != PASSIVE_BASE_X.get()) {
         saveWorks.add(() -> PASSIVE_BASE_X.set(passiveBaseX));
      }

      if (passiveBaseY != PASSIVE_BASE_Y.get()) {
         saveWorks.add(() -> PASSIVE_BASE_Y.set(passiveBaseY));
      }

      if (passiveAlignDirection != PASSIVE_ALIGN_DIRECTION.get()) {
         saveWorks.add(() -> PASSIVE_ALIGN_DIRECTION.set(passiveAlignDirection));
      }

      if (chargingBarX != (Integer)CHARGING_BAR_X.get()) {
         saveWorks.add(() -> CHARGING_BAR_X.set(chargingBarX));
      }

      if (chargingBarY != (Integer)CHARGING_BAR_Y.get()) {
         saveWorks.add(() -> CHARGING_BAR_Y.set(chargingBarY));
      }

      if (chargingBarBaseX != CHARGING_BAR_BASE_X.get()) {
         saveWorks.add(() -> CHARGING_BAR_BASE_X.set(chargingBarBaseX));
      }

      if (chargingBarBaseY != CHARGING_BAR_BASE_Y.get()) {
         saveWorks.add(() -> CHARGING_BAR_BASE_Y.set(chargingBarBaseY));
      }

      return saveWorks;
   }

   public static void saveChanges() {
      if (maxStuckProjectiles != (Integer)MAX_STUCK_PROJECTILES.get()) {
         MAX_STUCK_PROJECTILES.set(maxStuckProjectiles);
      }

      if (targetOutlineColor != (Double)TARGET_OUTLINE_COLOR.get()) {
         TARGET_OUTLINE_COLOR.set(targetOutlineColor);
         packedTargetOutlineColor = ColorSlider.rgbColor(targetOutlineColor);
      }

      if (bloodEffects != (Boolean)BLOOD_EFFECTS.get()) {
         BLOOD_EFFECTS.set(bloodEffects);
      }

      if (groundSlams != (Boolean)GROUND_SLAMS.get()) {
         GROUND_SLAMS.set(groundSlams);
      }

      if (showEpicFightAttributesInTooltip != (Boolean)SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get()) {
         SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.set(showEpicFightAttributesInTooltip);
      }

      if (activateComputeShader != (Boolean)ACTIVATE_COMPUTE_SHADER.get()) {
         ACTIVATE_COMPUTE_SHADER.set(activateComputeShader);
      }

      if (enableAnimatedFirstPersonModel != (Boolean)ENABLE_ANIMATED_FIRST_PERSON_MODEL.get()) {
         ENABLE_ANIMATED_FIRST_PERSON_MODEL.set(enableAnimatedFirstPersonModel);
      }

      if (mineBlockGuideOption != MINE_BLOCK_GUIDE_OPTION.get()) {
         MINE_BLOCK_GUIDE_OPTION.set(mineBlockGuideOption);
      }

      if (enableTargetEntityGuide != (Boolean)ENABLE_TARGET_ENTITY_GUIDE.get()) {
         ENABLE_TARGET_ENTITY_GUIDE.set(enableTargetEntityGuide);
      }

      if (enablePovAction != (Boolean)ENABLE_POV_ACTION.get()) {
         ENABLE_POV_ACTION.set(enablePovAction);
      }

      if (enableCosmetics != (Boolean)ENABLE_COSMETICS.get()) {
         ENABLE_COSMETICS.set(enableCosmetics);
      }

      if (enableOriginalModel != (Boolean)ENABLE_PLAYER_VANILLA_MODEL.get()) {
         ENABLE_PLAYER_VANILLA_MODEL.set(enableOriginalModel);
      }

      if (cameraMode != CAMERA_MODE.get()) {
         CAMERA_MODE.set(cameraMode);
      }

      if (cameraHorizontalLocation != (Integer)CAMERA_HORIZONTAL_LOCATION.get()) {
         CAMERA_HORIZONTAL_LOCATION.set(cameraHorizontalLocation);
      }

      if (cameraVerticalLocation != (Integer)CAMERA_VERTICAL_LOCATION.get()) {
         CAMERA_VERTICAL_LOCATION.set(cameraVerticalLocation);
      }

      if (cameraZoom != (Integer)CAMERA_ZOOM.get()) {
         CAMERA_ZOOM.set(cameraZoom);
      }

      if (lockOnRange != (Integer)LOCK_ON_RANGE.get()) {
         LOCK_ON_RANGE.set(lockOnRange);
      }

      if (longPressCounter != (Integer)LONG_PRESS_COUNTER.get()) {
         LONG_PRESS_COUNTER.set(longPressCounter);
      }

      if (autoSwitchCamera != (Boolean)AUTO_SWITCH_CAMERA.get()) {
         AUTO_SWITCH_CAMERA.set(autoSwitchCamera);
      }

      if (lockOnQuickShift != (Boolean)LOCK_ON_QUICK_SHIFT.get()) {
         LOCK_ON_QUICK_SHIFT.set(lockOnQuickShift);
      }

      if (keyConflictResolveScope != KEY_CONFLICT_RESOLVE_SCOPE.get()) {
         KEY_CONFLICT_RESOLVE_SCOPE.set(keyConflictResolveScope);
      }

      if (preferenceWork != PREFERENCE_WORK.get()) {
         PREFERENCE_WORK.set(preferenceWork);
      }

      if (cameraPerspectiveToggleMode != CAMERA_PERSPECTIVE_TOGGLE_MODE.get()) {
         CAMERA_PERSPECTIVE_TOGGLE_MODE.set(cameraPerspectiveToggleMode);
         CAMERA_PERSPECTIVE_TOGGLE_MODE.save();
      }

      if (!combatPreferredItems.equals(
         ((List)BATTLE_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet())
      )) {
         BATTLE_MODE_SWITCHING_ITEMS.set(combatPreferredItems.stream().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList()));
      }

      if (!miningPreferredItems.equals(
         ((List)MINING_MODE_SWITCHING_ITEMS.get())
            .stream()
            .map(itemName -> (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
            .collect(Collectors.toSet())
      )) {
         MINING_MODE_SWITCHING_ITEMS.set(miningPreferredItems.stream().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList()));
      }

      if (showTargetIndicator != (Boolean)SHOW_TARGET_INDICATOR.get()) {
         SHOW_TARGET_INDICATOR.set(showTargetIndicator);
      }

      if (healthBarVisibility != HEALTH_BAR_VISIBILITY.get()) {
         HEALTH_BAR_VISIBILITY.set(healthBarVisibility);
      }

      if (staminaBarX != (Integer)STAMINA_BAR_X.get()) {
         STAMINA_BAR_X.set(staminaBarX);
      }

      if (staminaBarY != (Integer)STAMINA_BAR_Y.get()) {
         STAMINA_BAR_Y.set(staminaBarY);
      }

      if (staminaBarBaseX != STAMINA_BAR_BASE_X.get()) {
         STAMINA_BAR_BASE_X.set(staminaBarBaseX);
      }

      if (staminaBarBaseY != STAMINA_BAR_BASE_Y.get()) {
         STAMINA_BAR_BASE_Y.set(staminaBarBaseY);
      }

      if (weaponInnateX != (Integer)WEAPON_INNATE_X.get()) {
         WEAPON_INNATE_X.set(weaponInnateX);
      }

      if (weaponInnateX != (Integer)WEAPON_INNATE_Y.get()) {
         WEAPON_INNATE_Y.set(weaponInnateY);
      }

      if (weaponInnateBaseX != WEAPON_INNATE_BASE_X.get()) {
         WEAPON_INNATE_BASE_X.set(weaponInnateBaseX);
      }

      if (weaponInnateBaseY != WEAPON_INNATE_BASE_Y.get()) {
         WEAPON_INNATE_BASE_Y.set(weaponInnateBaseY);
      }

      if (passiveX != (Integer)PASSIVE_X.get()) {
         PASSIVE_X.set(passiveX);
      }

      if (passiveY != (Integer)PASSIVE_Y.get()) {
         PASSIVE_Y.set(passiveY);
      }

      if (passiveBaseX != PASSIVE_BASE_X.get()) {
         PASSIVE_BASE_X.set(passiveBaseX);
      }

      if (passiveBaseY != PASSIVE_BASE_Y.get()) {
         PASSIVE_BASE_Y.set(passiveBaseY);
      }

      if (passiveAlignDirection != PASSIVE_ALIGN_DIRECTION.get()) {
         PASSIVE_ALIGN_DIRECTION.set(passiveAlignDirection);
      }

      if (chargingBarX != (Integer)CHARGING_BAR_X.get()) {
         CHARGING_BAR_X.set(chargingBarX);
      }

      if (chargingBarY != (Integer)CHARGING_BAR_Y.get()) {
         CHARGING_BAR_Y.set(chargingBarY);
      }

      if (chargingBarBaseX != CHARGING_BAR_BASE_X.get()) {
         CHARGING_BAR_BASE_X.set(chargingBarBaseX);
      }

      if (chargingBarBaseY != CHARGING_BAR_BASE_Y.get()) {
         CHARGING_BAR_BASE_Y.set(chargingBarBaseY);
      }
   }

   public static Vec2i getStaminaPosition(int width, int height) {
      int posX = staminaBarBaseX.positionGetter.apply(width, staminaBarX);
      int posY = staminaBarBaseY.positionGetter.apply(height, staminaBarY);
      return new Vec2i(posX, posY);
   }

   public static Vec2i getWeaponInnatePosition(int width, int height) {
      int posX = weaponInnateBaseX.positionGetter.apply(width, weaponInnateX);
      int posY = weaponInnateBaseY.positionGetter.apply(height, weaponInnateY);
      return new Vec2i(posX, posY);
   }

   public static Vec2i getChargingBarPosition(int width, int height) {
      int posX = chargingBarBaseX.positionGetter.apply(width, chargingBarX);
      int posY = chargingBarBaseY.positionGetter.apply(height, chargingBarY);
      return new Vec2i(posX, posY);
   }

   public static ClientConfig.TPSType getCameraMode() {
      if (cameraMode == null) {
         Exception noConfigValueException = new IllegalStateException("TPS Type is null");
         EpicFightMod.LOGGER.warn("Epic Fight Config error: TPS Type is null", noConfigValueException);
         noConfigValueException.printStackTrace();
         return ClientConfig.TPSType.WHEN_AIMING;
      } else {
         return cameraMode;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum BlockGuideOptions implements CirculatableEnum<ClientConfig.BlockGuideOptions>, StringRepresentable {
      NONE(false, false),
      CROSSHAIR(true, false),
      HIGHLIGHT(false, true),
      CROSSHAIR_AND_HIGHLIGHT(true, true);

      boolean showCrosshair;
      boolean showBlockHighlight;

      BlockGuideOptions(boolean showCrosshair, boolean showBlockHighlight) {
         this.showCrosshair = showCrosshair;
         this.showBlockHighlight = showBlockHighlight;
      }

      public boolean switchCrosshair() {
         return this.showCrosshair;
      }

      public boolean showBlockHighlight() {
         return this.showBlockHighlight;
      }

      public ClientConfig.BlockGuideOptions nextEnum() {
         return values()[(this.ordinal() + 1) % 4];
      }

      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }
   }

   public enum CameraPerspectiveToggleMode implements CirculatableEnum<ClientConfig.CameraPerspectiveToggleMode>, StringRepresentable {
      VANILLA,
      SKIP_THIRD_PERSON_FRONT;

      public ClientConfig.CameraPerspectiveToggleMode nextEnum() {
         ClientConfig.CameraPerspectiveToggleMode[] values = values();
         return values[(this.ordinal() + 1) % values.length];
      }

      @NotNull
      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum HealthBarVisibility implements CirculatableEnum<ClientConfig.HealthBarVisibility>, StringRepresentable {
      NONE,
      HURT,
      TARGET,
      TARGET_AND_HURT;

      public ClientConfig.HealthBarVisibility nextEnum() {
         return values()[(this.ordinal() + 1) % 4];
      }

      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum KeyConflictResolveScope implements CirculatableEnum<ClientConfig.KeyConflictResolveScope>, StringRepresentable {
      NONE(false, false),
      INTERACTION(true, false),
      ITEM_USE(false, true),
      INTERACTION_AND_ITEMUSE(true, true);

      boolean cancelInteraction;
      boolean cancelItemUse;

      KeyConflictResolveScope(boolean cancelBlockInteraction, boolean cancelItemInteraction) {
         this.cancelInteraction = cancelBlockInteraction;
         this.cancelItemUse = cancelItemInteraction;
      }

      public boolean cancelInteraction() {
         return this.cancelInteraction;
      }

      public boolean cancelItemUse() {
         return this.cancelItemUse;
      }

      public ClientConfig.KeyConflictResolveScope nextEnum() {
         return values()[(this.ordinal() + 1) % 4];
      }

      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum PreferenceWork implements CirculatableEnum<ClientConfig.PreferenceWork>, StringRepresentable {
      ADAPTIVE(true),
      SWITCH_MODE(false);

      boolean checkHitResult;

      PreferenceWork(boolean checkHitResult) {
         this.checkHitResult = checkHitResult;
      }

      public boolean checkHitResult() {
         return this.checkHitResult;
      }

      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }

      public ClientConfig.PreferenceWork nextEnum() {
         return values()[(this.ordinal() + 1) % 2];
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum TPSType implements CirculatableEnum<ClientConfig.TPSType>, StringRepresentable {
      ALWAYS_BACK(false, null),
      WHEN_AIMING(true, EpicFightCameraAPI::isZooming),
      ALWAYS(true, cameraApi -> true);

      boolean hasTPSTransition;
      Predicate<EpicFightCameraAPI> checker;

      TPSType(boolean hasTPSTransition, Predicate<EpicFightCameraAPI> checker) {
         this.hasTPSTransition = hasTPSTransition;
         this.checker = checker;
      }

      public boolean shouldSwitch(EpicFightCameraAPI cameraApi) {
         return this.hasTPSTransition ? this.checker.test(cameraApi) : false;
      }

      public boolean hasTPSTransition() {
         return this.hasTPSTransition;
      }

      public ClientConfig.TPSType nextEnum() {
         return values()[(this.ordinal() + 1) % 3];
      }

      public String m_7912_() {
         return ParseUtil.toLowerCase(this.name());
      }
   }
}
