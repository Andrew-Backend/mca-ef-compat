package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.render.Blit;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector2f;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.compat.controlify.screenop.SkillBookScreenProcessor;
import yesman.epicfight.compat.controlify.screenop.SkillEditScreenProcessor;
import yesman.epicfight.main.EpicFightMod;

@Internal
public class EpicFightControlifyEntrypoint implements ControlifyEntrypoint {
   private static InputBindingSupplier attack;
   private static InputBindingSupplier mobility;
   private static InputBindingSupplier guard;
   private static InputBindingSupplier dodge;
   private static InputBindingSupplier switchMode;
   private static InputBindingSupplier weaponInnateSkill;
   private static InputBindingSupplier weaponInnateSkillTooltip;
   private static InputBindingSupplier openSkillEditorScreen;
   private static InputBindingSupplier openConfigScreen;
   private static InputBindingSupplier switchVanillaModeDebugging;
   private static InputBindingSupplier lockOn;
   private static InputBindingSupplier lockOnShiftLeft;
   private static InputBindingSupplier lockOnShiftRight;
   private static InputBindingSupplier lockOnShiftFreely;

   public void onControllersDiscovered(ControlifyApi controlify) {
   }

   public void onControlifyInit(InitContext context) {
      registerModIntegration();
   }

   public void onControlifyPreInit(PreInitContext context) {
      ControlifyBindApi registrar = ControlifyBindApi.get();
      registerCustomRadialIcons();
      EpicFightControlifyBindContexts.EpicFight.register(registrar);
      registerInputBindings(registrar);
      registerEvents();
      registerGuides();
      registerScreenProcessors();
   }

   private static void registerCustomRadialIcons() {
      for (EpicFightControlifyEntrypoint.EpicFightRadialIcons icon : EpicFightControlifyEntrypoint.EpicFightRadialIcons.values()) {
         ResourceLocation location = icon.getId();
         RadialIcons.registerIcon(location, (graphics, x, y, tickDelta) -> {
            graphics.m_280168_().m_85836_();
            graphics.m_280168_().m_252880_(x, y, 0.0F);
            graphics.m_280168_().m_85841_(0.5F, 0.5F, 1.0F);
            Blit.blitTex(graphics, location, 0, 0, 0, 0, 32, 32, 32, 32);
            graphics.m_280168_().m_85849_();
         });
      }
   }

   private static void registerInputBindings(ControlifyBindApi registrar) {
      for (EpicFightInputAction action : EpicFightInputAction.values()) {
         registerInputBinding(registrar, action);
      }
   }

   @NotNull
   private static InputBindingSupplier registerInputBinding(@NotNull ControlifyBindApi registrar, @NotNull EpicFightInputAction action) {
      Component combatCategory = Component.m_237115_("key.epicfight.combat");
      Component guiCategory = Component.m_237115_("key.epicfight.gui");
      Component cameraCategory = Component.m_237115_("key.epicfight.camera");
      Component systemCategory = Component.m_237115_("key.epicfight.system");

      return switch (action) {
         case ATTACK -> attack = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(combatCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
         case DODGE -> dodge = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(combatCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
         case GUARD -> guard = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(combatCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
         case LOCK_ON -> lockOn = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(cameraCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
         case LOCK_ON_SHIFT_LEFT -> lockOnShiftLeft = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(cameraCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.LOCK_ON})
         );
         case LOCK_ON_SHIFT_RIGHT -> lockOnShiftRight = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(cameraCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.LOCK_ON})
         );
         case LOCK_ON_SHIFT_FREELY -> lockOnShiftFreely = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(cameraCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.LOCK_ON})
         );
         case SWITCH_MODE -> switchMode = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(systemCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.IN_GAME})
               .radialCandidate(EpicFightControlifyEntrypoint.EpicFightRadialIcons.UCHIGATANA.getId())
         );
         case WEAPON_INNATE_SKILL -> weaponInnateSkill = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(combatCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
         case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(guiCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.ANY_SCREEN})
         );
         case OPEN_SKILL_SCREEN -> openSkillEditorScreen = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(guiCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.IN_GAME})
               .radialCandidate(EpicFightControlifyEntrypoint.EpicFightRadialIcons.SKILL_BOOK.getId())
         );
         case OPEN_CONFIG_SCREEN -> openConfigScreen = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(guiCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.IN_GAME})
               .radialCandidate(RadialIcons.getItem(Items.f_42451_))
         );
         case SWITCH_VANILLA_MODEL_DEBUGGING -> switchVanillaModeDebugging = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(systemCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.IN_GAME})
         );
         case MOBILITY -> mobility = registrar.registerBinding(
            builder -> applyCommonBindingProperties(action, builder)
               .category(combatCategory)
               .allowedContexts(new BindContext[]{EpicFightControlifyBindContexts.EpicFight.COMBAT_MODE})
         );
      };
   }

   @NotNull
   private static InputBindingBuilder applyCommonBindingProperties(@NotNull EpicFightInputAction action, @NotNull InputBindingBuilder builder) {
      EpicFightControlifyEntrypoint.TranslationKeys translationKeys = EpicFightControlifyEntrypoint.TranslationKeys.fromAction(action);
      KeyMapping keyMappingToIgnore = action.keyMapping();
      return builder.id(getBindingId(action))
         .name(translationKeys.getNameComponent())
         .description(translationKeys.getDescriptionComponent())
         .addKeyCorrelation(keyMappingToIgnore);
   }

   @NotNull
   private static ResourceLocation getBindingId(@NotNull EpicFightInputAction action) {
      String path = switch (action) {
         case ATTACK -> "attack";
         case DODGE -> "dodge";
         case GUARD -> "guard";
         case LOCK_ON -> "lock_on";
         case LOCK_ON_SHIFT_LEFT -> "lock_on_shift_left";
         case LOCK_ON_SHIFT_RIGHT -> "lock_on_shift_right";
         case LOCK_ON_SHIFT_FREELY -> "lock_on_shift_freely";
         case SWITCH_MODE -> "switch_mode";
         case WEAPON_INNATE_SKILL -> "weapon_innate_skill";
         case WEAPON_INNATE_SKILL_TOOLTIP -> "weapon_innate_skill_tooltip";
         case OPEN_SKILL_SCREEN -> "open_skill_editor_screen";
         case OPEN_CONFIG_SCREEN -> "open_config_screen";
         case SWITCH_VANILLA_MODEL_DEBUGGING -> "switch_vanilla_mode_debugging";
         case MOBILITY -> "mobility";
      };
      return EpicFightMod.identifier(path);
   }

   private static void registerModIntegration() {
      EpicFightControllerModProvider.set("epicfight", new EpicFightControlifyControllerMod());
   }

   private static void registerEvents() {
      ControlifyEvents.LOOK_INPUT_MODIFIER.register(event -> {
         double multiplier = 10.0;
         Vector2f lookInput = event.lookInput();
         double dy = lookInput.x * 10.0;
         double dx = lookInput.y * 10.0;
         if (EpicFightCameraAPI.getInstance().turnCamera(dy, dx)) {
            lookInput.zero();
         }
      });
   }

   private static void registerGuides() {
   }

   @NotNull
   public static InputBinding getControlifyBinding(@NotNull EpicFightInputAction action) {
      InputBindingSupplier bindingSupplier = switch (action) {
         case ATTACK -> attack;
         case DODGE -> dodge;
         case GUARD -> guard;
         case LOCK_ON -> lockOn;
         case LOCK_ON_SHIFT_LEFT -> lockOnShiftLeft;
         case LOCK_ON_SHIFT_RIGHT -> lockOnShiftRight;
         case LOCK_ON_SHIFT_FREELY -> lockOnShiftFreely;
         case SWITCH_MODE -> switchMode;
         case WEAPON_INNATE_SKILL -> weaponInnateSkill;
         case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip;
         case OPEN_SKILL_SCREEN -> openSkillEditorScreen;
         case OPEN_CONFIG_SCREEN -> openConfigScreen;
         case SWITCH_VANILLA_MODEL_DEBUGGING -> switchVanillaModeDebugging;
         case MOBILITY -> mobility;
      };
      InputBinding binding = bindingSupplier.onOrNull(requireControllerEntity());
      return Objects.requireNonNull(binding, "The binding for the action " + action.name() + " is not yet registered.");
   }

   @NotNull
   public static InputBinding getControlifyBinding(@NotNull MinecraftInputAction action) {
      InputBindingSupplier bindingSupplier = switch (action) {
         case ATTACK_DESTROY -> ControlifyBindings.ATTACK;
         case MOVE_FORWARD -> ControlifyBindings.WALK_FORWARD;
         case MOVE_BACKWARD -> ControlifyBindings.WALK_BACKWARD;
         case MOVE_LEFT -> ControlifyBindings.WALK_LEFT;
         case MOVE_RIGHT -> ControlifyBindings.WALK_RIGHT;
         case SPRINT -> ControlifyBindings.SPRINT;
         case SNEAK -> ControlifyBindings.SNEAK;
         case USE -> ControlifyBindings.USE;
         case SWAP_OFF_HAND -> ControlifyBindings.SWAP_HANDS;
         case DROP -> ControlifyBindings.DROP_INGAME;
         case TOGGLE_PERSPECTIVE -> ControlifyBindings.CHANGE_PERSPECTIVE;
         case JUMP -> ControlifyBindings.JUMP;
      };
      InputBinding binding = bindingSupplier.onOrNull(requireControllerEntity());
      return Objects.requireNonNull(binding, "The binding for the action " + action.name() + " is not yet registered.");
   }

   @NotNull
   public static ControlifyApi getApi() {
      return ControlifyApi.get();
   }

   @NotNull
   public static ControllerEntity requireControllerEntity() {
      Optional<ControllerEntity> optionalControllerEntity = getApi().getCurrentController();
      if (optionalControllerEntity.isEmpty()) {
         String message = String.format(
            "The method IEpicFightControllerMod#getInputState must not be called when the input mode is not %s", InputMode.CONTROLLER.name()
         );
         EpicFightMod.LOGGER.error(message);
         throw new IllegalStateException(message);
      } else {
         return optionalControllerEntity.get();
      }
   }

   private static void registerScreenProcessors() {
      ScreenProcessorProvider.registerProvider(SkillEditScreen.class, SkillEditScreenProcessor::new);
      ScreenProcessorProvider.registerProvider(SkillBookScreen.class, SkillBookScreenProcessor::new);
   }

   private enum EpicFightRadialIcons {
      UCHIGATANA(EpicFightMod.identifier("textures/item/uchigatana_gui.png")),
      SKILL_BOOK(EpicFightMod.identifier("textures/item/skillbook.png"));

      @NotNull
      private final ResourceLocation id;

      EpicFightRadialIcons(@NotNull ResourceLocation id) {
         this.id = id;
      }

      @NotNull
      public ResourceLocation getId() {
         return this.id;
      }
   }

   private record TranslationKeys(@NotNull String name, @NotNull String description) {
      @NotNull
      private Component getNameComponent() {
         return Component.m_237115_(this.name());
      }

      @NotNull
      private Component getDescriptionComponent() {
         return Component.m_237115_(this.description());
      }

      @NotNull
      private static EpicFightControlifyEntrypoint.TranslationKeys fromAction(@NotNull EpicFightInputAction action) {
         return switch (action) {
            case ATTACK -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.attack", "key.epicfight.attack.description");
            case DODGE -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.dodge", "key.epicfight.dodge.description");
            case GUARD -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.guard", "key.epicfight.guard.description");
            case LOCK_ON -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.lock_on", "key.epicfight.lock_on.description");
            case LOCK_ON_SHIFT_LEFT -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.lock_on_shift_left", "key.epicfight.lock_on_shift_left.description"
            );
            case LOCK_ON_SHIFT_RIGHT -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.lock_on_shift_right", "key.epicfight.lock_on_shift_right.description"
            );
            case LOCK_ON_SHIFT_FREELY -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.lock_on_shift_freely", "key.epicfight.lock_on_shift_freely.description"
            );
            case SWITCH_MODE -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.switch_mode", "key.epicfight.switch_mode.description");
            case WEAPON_INNATE_SKILL -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.weapon_innate_skill", "key.epicfight.weapon_innate_skill.description"
            );
            case WEAPON_INNATE_SKILL_TOOLTIP -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.show_tooltip", "key.epicfight.show_tooltip.description"
            );
            case OPEN_SKILL_SCREEN -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.skill_gui", "key.epicfight.skill_gui.description");
            case OPEN_CONFIG_SCREEN -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.config", "key.epicfight.config.description");
            case SWITCH_VANILLA_MODEL_DEBUGGING -> new EpicFightControlifyEntrypoint.TranslationKeys(
               "key.epicfight.switch_vanilla_model_debug", "key.epicfight.switch_vanilla_model_debug.description"
            );
            case MOBILITY -> new EpicFightControlifyEntrypoint.TranslationKeys("key.epicfight.mover_skill", "key.epicfight.mover_skill.description");
         };
      }
   }
}
