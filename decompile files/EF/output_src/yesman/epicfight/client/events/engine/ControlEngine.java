package yesman.epicfight.client.events.engine;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class ControlEngine {
   private final Set<Object> packets = Sets.newHashSet();
   private final Minecraft minecraft;
   private LocalPlayer player;
   private LocalPlayerPatch playerPatch;
   private int weaponInnatePressCounter = 0;
   private int sneakPressCounter = 0;
   private int moverPressCounter = 0;
   private int tickSinceLastJump = 0;
   private int lastHotbarLockedTime;
   private boolean weaponInnatePressToggle = false;
   private boolean sneakPressToggle = false;
   private boolean moverPressToggle = false;
   private boolean attackLightPressToggle = false;
   private boolean hotbarLocked;
   private boolean holdingFinished;
   private int reserveCounter;
   @Deprecated
   private KeyMapping reservedKey;
   private SkillSlot reservedOrHoldingSkillSlot;
   @Deprecated
   private KeyMapping currentHoldingKey;
   public Options options;

   public ControlEngine() {
      ControlEngine.Events.controlEngine = this;
      this.minecraft = Minecraft.m_91087_();
      this.options = this.minecraft.f_91066_;
   }

   public void setPlayerPatch(LocalPlayerPatch playerPatch) {
      this.weaponInnatePressCounter = 0;
      this.weaponInnatePressToggle = false;
      this.sneakPressCounter = 0;
      this.sneakPressToggle = false;
      this.attackLightPressToggle = false;
      this.player = playerPatch.getOriginal();
      this.playerPatch = playerPatch;
   }

   public LocalPlayerPatch getPlayerPatch() {
      return this.playerPatch;
   }

   public boolean canPlayerMove(EntityState playerState) {
      return !playerState.movementLocked() || this.player.m_245714_() != null;
   }

   public boolean canPlayerRotate(EntityState playerState) {
      return !playerState.turningLocked() || this.player.m_245714_() != null;
   }

   public void handleEpicFightKeyMappings() {
      if (this.playerPatch != null) {
         InputManager.triggerOnPress(EpicFightInputAction.OPEN_SKILL_SCREEN, this::openSkillEditor);
         InputManager.triggerOnPress(EpicFightInputAction.OPEN_CONFIG_SCREEN, this::openConfig);
         InputManager.triggerOnPress(EpicFightInputAction.SWITCH_VANILLA_MODEL_DEBUGGING, this::switchVanillaModelDebugging);
         InputManager.triggerOnPress(EpicFightInputAction.ATTACK, () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.ATTACK, this::maybeAttack));
         InputManager.triggerOnPress(EpicFightInputAction.DODGE, () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.DODGE, this::maybeDodge));
         if (InputManager.isActionActive(EpicFightInputAction.GUARD)) {
            this.maybeGuard();
         }

         InputManager.triggerOnPress(
            EpicFightInputAction.WEAPON_INNATE_SKILL,
            () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.WEAPON_INNATE_SKILL, this::handleSeparateWeaponInnateSkill)
         );
         InputManager.triggerOnPress(
            EpicFightInputAction.MOBILITY, () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.MOBILITY, this::maybePerformMoverSkill)
         );
         InputManager.triggerOnPress(EpicFightInputAction.SWITCH_MODE, this::switchMode);
         InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON, this::toggleLockOnState);
         InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON_SHIFT_LEFT, this::searchNewTargetFromLeft);
         InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON_SHIFT_RIGHT, this::searchNewTargetFromRight);
         if (shouldDisableSwapHandItems()) {
            consumeSwapOffhandKeyClicks();
         }

         if (this.playerPatch.isEpicFightMode() && !Minecraft.m_91087_().m_91104_()) {
            if (this.player.f_19797_ - this.lastHotbarLockedTime > 20 && this.hotbarLocked) {
               this.unlockHotkeys();
            }

            if (this.weaponInnatePressToggle) {
               if (!InputManager.isActionActive(EpicFightInputAction.WEAPON_INNATE_SKILL)) {
                  this.attackLightPressToggle = true;
                  this.weaponInnatePressToggle = false;
                  this.weaponInnatePressCounter = 0;
               } else if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.WEAPON_INNATE_SKILL, EpicFightInputAction.ATTACK)) {
                  if (this.weaponInnatePressCounter > ClientConfig.longPressCounter) {
                     if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
                        if (!this.player.m_5833_()) {
                           this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightInputAction.WEAPON_INNATE_SKILL);
                        }
                     } else {
                        this.lockHotkeys();
                     }

                     this.weaponInnatePressToggle = false;
                     this.weaponInnatePressCounter = 0;
                  } else {
                     this.weaponInnatePressCounter++;
                  }
               }
            }

            if (this.attackLightPressToggle) {
               SkillSlot slot = SkillSlots.BASIC_ATTACK;
               SkillCastEvent skillCastEvent = this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this);
               if (skillCastEvent.isExecutable()) {
                  this.player.m_36334_();
                  this.releaseAllServedKeys();
               } else if (!this.player.m_5833_()) {
                  this.reserveKey(slot, EpicFightInputAction.ATTACK);
               }

               this.lockHotkeys();
               this.attackLightPressToggle = false;
               this.weaponInnatePressToggle = false;
               this.weaponInnatePressCounter = 0;
            }

            if (this.sneakPressToggle) {
               if (!InputManager.isActionActive(MinecraftInputAction.SNEAK)) {
                  SkillSlot skillSlot = this.playerPatch.getEntityState().knockDown() ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
                  SkillContainer skill = this.playerPatch.getSkill(skillSlot);
                  if (skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
                     this.reserveKey(skillSlot, MinecraftInputAction.SNEAK);
                  }

                  this.sneakPressToggle = false;
                  this.sneakPressCounter = 0;
               } else if (this.sneakPressCounter > ClientConfig.longPressCounter) {
                  this.sneakPressToggle = false;
                  this.sneakPressCounter = 0;
               } else {
                  this.sneakPressCounter++;
               }
            }

            if (this.currentHoldingKey != null) {
               SkillContainer container = this.playerPatch.getSkill(this.reservedOrHoldingSkillSlot);
               if (!container.isEmpty()) {
                  if (container.getSkill() instanceof HoldableSkill) {
                     if (!this.isCurrentHoldingActionActive()) {
                        this.holdingFinished = true;
                     }

                     if (container.getSkill() instanceof ChargeableSkill chargingSkill) {
                        if (this.holdingFinished) {
                           if (this.playerPatch.getSkillChargingTicks() > chargingSkill.getMinChargingTicks()) {
                              container.sendCastRequest(this.playerPatch, this);
                              this.releaseAllServedKeys();
                           }
                        } else if (this.playerPatch.getSkillChargingTicks() >= chargingSkill.getAllowedMaxChargingTicks()) {
                           this.releaseAllServedKeys();
                        }
                     } else if (this.holdingFinished) {
                        this.playerPatch.resetHolding();
                        container.getSkill().cancelOnClient(container, container.getSkill().gatherArguments(container, this));
                        container.sendCancelRequest(this.playerPatch, this);
                        this.releaseAllServedKeys();
                     }
                  } else {
                     this.releaseAllServedKeys();
                  }
               }
            }

            if (this.reservedKey != null) {
               if (this.reserveCounter > 0) {
                  SkillContainer skill = this.playerPatch.getSkill(this.reservedOrHoldingSkillSlot);
                  this.reserveCounter--;
                  if (skill.getSkill() != null && skill.sendCastRequest(this.playerPatch, this).isExecutable()) {
                     this.releaseAllServedKeys();
                     this.lockHotkeys();
                  }
               } else {
                  this.releaseAllServedKeys();
               }
            }

            if (this.isSwitchOrDropBlocked()) {
               disableHotbarSlotPresses();
               consumeDropKeyClicks();
            }

            if (this.minecraft.f_91073_ != null
               && EpicFightCameraAPI.getInstance().isTPSMode()
               && InputConstants.m_84830_(Minecraft.m_91087_().m_91268_().m_85439_(), 341)) {
               if (InputConstants.m_84830_(Minecraft.m_91087_().m_91268_().m_85439_(), 263)) {
                  ClientConfig.cameraHorizontalLocation = Math.min(10, ClientConfig.cameraHorizontalLocation + 1);
               }

               if (InputConstants.m_84830_(Minecraft.m_91087_().m_91268_().m_85439_(), 262)) {
                  ClientConfig.cameraHorizontalLocation = Math.max(-10, ClientConfig.cameraHorizontalLocation - 1);
               }

               if (InputConstants.m_84830_(Minecraft.m_91087_().m_91268_().m_85439_(), 265)) {
                  ClientConfig.cameraVerticalLocation = Math.min(5, ClientConfig.cameraVerticalLocation + 1);
               }

               if (InputConstants.m_84830_(Minecraft.m_91087_().m_91268_().m_85439_(), 264)) {
                  ClientConfig.cameraVerticalLocation = Math.max(-2, ClientConfig.cameraVerticalLocation - 1);
               }
            }
         }
      }
   }

   private void openSkillEditor() {
      CapabilitySkill capabilitySkill = this.playerPatch.getSkillCapability();
      if (capabilitySkill != null) {
         this.minecraft.m_91152_(new SkillEditScreen(this.player, capabilitySkill));
      }
   }

   private void openConfig() {
      this.minecraft.m_91152_(new IngameConfigurationScreen(null));
   }

   private void switchVanillaModelDebugging() {
      boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
      this.minecraft.f_91068_.m_90913_(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off", new Object[0]);
   }

   private void maybeAttack() {
      if (this.playerPatch.isEpicFightMode() && !this.isCurrentHoldingAction(EpicFightInputAction.ATTACK)) {
         MinecraftInputAction vanillaAttack = MinecraftInputAction.ATTACK_DESTROY;
         EpicFightInputAction epicFightAttack = EpicFightInputAction.ATTACK;
         boolean shouldPlayAttackAnimation = this.playerPatch.canPlayAttackAnimation();
         if (vanillaAttack.keyMapping().getKey() == epicFightAttack.keyMapping().getKey() && Minecraft.m_91087_().f_91077_ != null && shouldPlayAttackAnimation
            )
          {
            consumeVanillaAttackKeyClicks();
         }

         if (shouldPlayAttackAnimation) {
            if (!InputManager.isBoundToSamePhysicalInput(epicFightAttack, EpicFightInputAction.WEAPON_INNATE_SKILL)) {
               SkillSlot slot = SkillSlots.BASIC_ATTACK;
               SkillCastEvent skillCastEvent = this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this);
               if (skillCastEvent.isExecutable()) {
                  this.player.m_36334_();
                  this.attackLightPressToggle = false;
                  this.releaseAllServedKeys();
               } else if (!this.player.m_5833_()) {
                  this.reserveKey(slot, epicFightAttack);
               }

               this.lockHotkeys();
               this.attackLightPressToggle = false;
               this.weaponInnatePressToggle = false;
               this.weaponInnatePressCounter = 0;
            } else if (!this.weaponInnatePressToggle) {
               this.weaponInnatePressToggle = true;
            }
         }
      }
   }

   private void maybeDodge() {
      if (this.playerPatch.isEpicFightMode() && !this.isCurrentHoldingAction(EpicFightInputAction.DODGE)) {
         if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.DODGE, MinecraftInputAction.SNEAK)) {
            if (this.player.m_20202_() == null && !this.sneakPressToggle) {
               this.sneakPressToggle = true;
            }
         } else {
            SkillSlot skillCategory = this.playerPatch.getEntityState().knockDown() ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
            SkillContainer skill = this.playerPatch.getSkill(skillCategory);
            if (!skill.isEmpty() && skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
               this.reserveKey(SkillSlots.DODGE, EpicFightInputAction.DODGE);
            }
         }
      }
   }

   private void maybeGuard() {
      if (this.playerPatch.isEpicFightMode() && !this.isCurrentHoldingAction(EpicFightInputAction.GUARD)) {
         boolean shouldCancelGuard = false;
         if (this.playerPatch.isHoldingAny()) {
            shouldCancelGuard = true;
         } else if (ShieldItem.class.isAssignableFrom(this.player.m_21205_().m_41720_().getClass())
            || ShieldItem.class.isAssignableFrom(this.player.m_21206_().m_41720_().getClass())) {
            shouldCancelGuard = true;
         }

         if (!shouldCancelGuard) {
            SkillCastEvent skillCastEvent = this.playerPatch.getSkill(SkillSlots.GUARD).sendCastRequest(this.playerPatch, this);
            if (skillCastEvent.shouldReserveKey()) {
               if (!this.player.m_5833_()) {
                  this.reserveKey(SkillSlots.GUARD, EpicFightInputAction.GUARD);
               }
            } else {
               this.lockHotkeys();
            }
         }
      }
   }

   private void handleSeparateWeaponInnateSkill() {
      if (this.playerPatch.isEpicFightMode() && !this.isCurrentHoldingAction(EpicFightInputAction.WEAPON_INNATE_SKILL)) {
         if (!InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.ATTACK, EpicFightInputAction.WEAPON_INNATE_SKILL)) {
            if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
               if (!this.player.m_5833_()) {
                  this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightInputAction.WEAPON_INNATE_SKILL);
               }
            } else {
               this.lockHotkeys();
            }
         }
      }
   }

   private void maybePerformMoverSkill() {
      if (this.playerPatch.isEpicFightMode() && !this.playerPatch.isHoldingAny()) {
         if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.MOBILITY, MinecraftInputAction.JUMP)) {
            SkillContainer skillContainer = this.playerPatch.getSkill(SkillSlots.MOVER);
            if (!skillContainer.isEmpty()) {
               SkillCastEvent event = new SkillCastEvent(this.playerPatch, skillContainer, skillContainer.getSkill().gatherArguments(skillContainer, this));
               if (skillContainer.canUse(this.playerPatch, event) && this.player.m_20202_() == null && !this.moverPressToggle) {
                  this.moverPressToggle = true;
               }
            }
         } else {
            SkillContainer skill = this.playerPatch.getSkill(SkillSlots.MOVER);
            skill.sendCastRequest(this.playerPatch, this);
         }
      }
   }

   private void switchMode() {
      boolean canSwitch = EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(this.playerPatch.getOriginal().m_9236_());
      if (!canSwitch) {
         this.minecraft.f_91065_.m_93076_().m_93785_(Component.m_237115_("epicfight.messages.mode_switching_disabled").m_130940_(ChatFormatting.RED));
      } else {
         this.playerPatch.toggleMode();
      }
   }

   private void toggleLockOnState() {
      EpicFightCameraAPI.getInstance().toggleLockOn();
   }

   private void searchNewTargetFromLeft() {
      EpicFightCameraAPI.getInstance().setNextLockOnTarget(1, true, true);
   }

   private void searchNewTargetFromRight() {
      EpicFightCameraAPI.getInstance().setNextLockOnTarget(-1, true, true);
   }

   private void inputTick(Input input) {
      PlayerInputState inputState = InputManager.getInputState(input);
      if (this.moverPressToggle) {
         if (!InputManager.isActionActive(MinecraftInputAction.JUMP)) {
            this.moverPressToggle = false;
            this.moverPressCounter = 0;
            if (this.player.m_20096_()) {
               this.player.f_20954_ = 0;
               inputState = inputState.withJumping(true);
               InputManager.setInputState(inputState);
            }
         } else if (this.moverPressCounter > ClientConfig.longPressCounter) {
            SkillContainer skill = this.playerPatch.getSkill(SkillSlots.MOVER);
            skill.sendCastRequest(this.playerPatch, this);
            this.moverPressToggle = false;
            this.moverPressCounter = 0;
         } else {
            this.player.f_20954_ = 2;
            this.moverPressCounter++;
         }
      }

      if (!this.canPlayerMove(this.playerPatch.getEntityState())) {
         inputState = inputState.copyWith(0.0F, 0.0F, false, false, false, false, false, false);
         InputManager.setInputState(inputState);
         this.player.f_108583_ = -1;
         this.player.m_6858_(false);
      }

      if (this.player.m_6084_()) {
         this.playerPatch
            .getEventListener()
            .triggerEvents(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, new MovementInputEvent(this.playerPatch, inputState));
      }

      if (this.tickSinceLastJump > 0) {
         this.tickSinceLastJump--;
      }
   }

   @Deprecated(forRemoval = true)
   private void reserveKey(SkillSlot slot, KeyMapping keyMapping) {
      this.reservedKey = keyMapping;
      this.reservedOrHoldingSkillSlot = slot;
      this.reserveCounter = 8;
   }

   private void reserveKey(SkillSlot slot, InputAction action) {
      this.reserveKey(slot, action.keyMapping());
   }

   public void releaseAllServedKeys() {
      this.holdingFinished = true;
      this.currentHoldingKey = null;
      this.reservedOrHoldingSkillSlot = null;
      this.reserveCounter = -1;
      this.reservedKey = null;
   }

   public void setHoldingKey(SkillSlot chargingSkillSlot, KeyMapping keyMapping) {
      this.holdingFinished = false;
      this.currentHoldingKey = keyMapping;
      this.reservedOrHoldingSkillSlot = chargingSkillSlot;
      this.reserveCounter = -1;
      this.reservedKey = null;
   }

   public void lockHotkeys() {
      this.hotbarLocked = true;
      this.lastHotbarLockedTime = this.player.f_19797_;
      disableHotbarSlotPresses();
   }

   public void unlockHotkeys() {
      this.hotbarLocked = false;
   }

   public void addPacketToSend(Object packet) {
      this.packets.add(packet);
   }

   @Deprecated(forRemoval = true)
   public static boolean isKeyDown(KeyMapping key) {
      if (key.getKey().m_84868_() == Type.KEYSYM) {
         return key.m_90857_() || GLFW.glfwGetKey(Minecraft.m_91087_().m_91268_().m_85439_(), key.getKey().m_84873_()) > 0;
      } else {
         return key.getKey().m_84868_() != Type.MOUSE
            ? false
            : key.m_90857_() || GLFW.glfwGetMouseButton(Minecraft.m_91087_().m_91268_().m_85439_(), key.getKey().m_84873_()) > 0;
      }
   }

   @Deprecated(forRemoval = true)
   private static boolean isKeyPressed(KeyMapping key, boolean eventCheck) {
      boolean consumes = key.m_90859_();
      if (consumes && eventCheck) {
         int mouseButton = Type.MOUSE == key.getKey().m_84868_() ? key.getKey().m_84873_() : -1;
         InteractionKeyMappingTriggered inputEvent = ForgeHooksClient.onClickInput(mouseButton, key, InteractionHand.MAIN_HAND);
         if (inputEvent.isCanceled()) {
            return false;
         }
      }

      return consumes;
   }

   @Deprecated(forRemoval = false)
   public static void makeUnpressed(KeyMapping keyMapping) {
      while (keyMapping.m_90859_()) {
      }

      keyMapping.m_7249_(false);
   }

   @Deprecated(forRemoval = true)
   public static void setKeyBind(KeyMapping key, boolean setter) {
      key.m_7249_(setter);
   }

   public static void setSprintingKeyStateNotDown() {
      MinecraftInputAction.SPRINT.keyMapping().m_7249_(false);
   }

   @Internal
   public static boolean shouldDisableVanillaAttack() {
      LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
      return playerPatch == null ? false : playerPatch.isEpicFightMode() && playerPatch.canPlayAttackAnimation();
   }

   @Internal
   public static boolean shouldDisableSwapHandItems() {
      LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
      return playerPatch == null
         ? false
         : playerPatch.getEntityState().inaction() || !playerPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).canBePlacedOffhand();
   }

   private static void consumeVanillaAttackKeyClicks() {
      makeUnpressed(MinecraftInputAction.ATTACK_DESTROY.keyMapping());
   }

   private static void consumeSwapOffhandKeyClicks() {
      makeUnpressed(MinecraftInputAction.SWAP_OFF_HAND.keyMapping());
   }

   private static void disableHotbarSlotPresses() {
      KeyMapping[] hotbarSlots = Minecraft.m_91087_().f_91066_.f_92056_;

      for (int i = 0; i < 9; i++) {
         KeyMapping hotbarSlot = hotbarSlots[i];
         makeUnpressed(hotbarSlot);
      }
   }

   private static void consumeDropKeyClicks() {
      makeUnpressed(MinecraftInputAction.DROP.keyMapping());
   }

   @Nullable
   private static InputAction mapKeyMappingToAction(@NotNull KeyMapping keyMapping) {
      return InputAction.fromKeyMapping(keyMapping);
   }

   private boolean isCurrentHoldingAction(@NotNull InputAction other) {
      if (this.currentHoldingKey == null) {
         return false;
      }

      InputAction currentHoldingAction = mapKeyMappingToAction(this.currentHoldingKey);
      return currentHoldingAction == null ? this.currentHoldingKey == other.keyMapping() : other == currentHoldingAction;
   }

   private boolean isCurrentHoldingActionActive() {
      if (this.currentHoldingKey == null) {
         return false;
      }

      InputAction currentHoldingAction = mapKeyMappingToAction(this.currentHoldingKey);
      return currentHoldingAction == null ? isKeyDown(this.currentHoldingKey) : InputManager.isActionActive(currentHoldingAction);
   }

   @Internal
   public static boolean isHotbarCyclingDisabled() {
      Minecraft minecraft = Minecraft.m_91087_();
      LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
      return minecraft.f_91074_ != null && localPlayerPatch != null && !localPlayerPatch.getEntityState().canSwitchHoldingItem() && minecraft.f_91080_ == null;
   }

   public boolean isSwitchOrDropBlocked() {
      return !this.playerPatch.getEntityState().canSwitchHoldingItem() || this.hotbarLocked;
   }

   public boolean moverToggling() {
      return this.moverPressToggle;
   }

   public boolean sneakToggling() {
      return this.sneakPressToggle;
   }

   public boolean attackToggling() {
      return this.attackLightPressToggle;
   }

   public boolean weaponInnateToggling() {
      return this.weaponInnatePressToggle;
   }

   @EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT)
   public static class Events {
      static ControlEngine controlEngine;

      @SubscribeEvent
      public static void livingJumpEvent(LivingJumpEvent event) {
         if (event.getEntity() == controlEngine.player) {
            controlEngine.tickSinceLastJump = 5;
         }
      }

      @SubscribeEvent
      public static void mouseScrollEvent(MouseScrollingEvent event) {
         if (ControlEngine.isHotbarCyclingDisabled()) {
            event.setCanceled(true);
         }
      }

      @SubscribeEvent
      public static void moveInputEvent(MovementInputUpdateEvent event) {
         if (controlEngine.playerPatch != null) {
            controlEngine.inputTick(event.getInput());
         }
      }

      @SubscribeEvent
      public static void clientTickEndEvent(ClientTickEvent event) {
         if (controlEngine.minecraft.f_91074_ != null) {
            if (event.phase == Phase.END) {
               for (Object packet : controlEngine.packets) {
                  EpicFightNetworkManager.sendToServer(packet);
               }

               controlEngine.packets.clear();
            }
         }
      }

      @SubscribeEvent
      public static void interactionEvent(InteractionKeyMappingTriggered event) {
         if (controlEngine.minecraft.f_91074_ != null && controlEngine.minecraft.f_91077_ != null) {
            InputAction triggeredAction = ControlEngine.mapKeyMappingToAction(event.getKeyMapping());
            if (triggeredAction != null) {
               if (triggeredAction == MinecraftInputAction.ATTACK_DESTROY
                  && InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.ATTACK, MinecraftInputAction.ATTACK_DESTROY)
                  && controlEngine.minecraft.f_91077_.m_6662_() == net.minecraft.world.phys.HitResult.Type.BLOCK
                  && ClientConfig.combatPreferredItems.contains(controlEngine.player.m_21205_().m_41720_())) {
                  BlockPos bp = ((BlockHitResult)controlEngine.minecraft.f_91077_).m_82425_();
                  BlockState bs = controlEngine.minecraft.f_91073_.m_8055_(bp);
                  if (!controlEngine.player.m_21205_().m_41720_().m_6777_(bs, controlEngine.player.m_9236_(), bp, controlEngine.player)
                     || controlEngine.player.m_21205_().m_41691_(bs) <= 1.0F) {
                     event.setSwingHand(false);
                     event.setCanceled(true);
                  }
               }

               LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(controlEngine.minecraft.f_91074_, LocalPlayerPatch.class);
               if (playerpatch != null) {
                  if (playerpatch.isVanillaMode()
                     && triggeredAction == MinecraftInputAction.ATTACK_DESTROY
                     && !EpicFightGameRules.ALLOW_VANILLA_MELEE.getRuleValue(playerpatch.getOriginal().m_9236_())
                     && controlEngine.minecraft.f_91077_ instanceof EntityHitResult entityHitResult
                     && (entityHitResult.m_82443_() instanceof LivingEntity || entityHitResult.m_82443_() instanceof PartEntity)) {
                     event.setSwingHand(false);
                     event.setCanceled(true);
                  }

                  if (triggeredAction == MinecraftInputAction.USE
                     && InputManager.isBoundToSamePhysicalInput(MinecraftInputAction.USE, EpicFightInputAction.GUARD)) {
                     MutableBoolean canGuard = new MutableBoolean(false);
                     MutableBoolean vanillaMode = new MutableBoolean(false);
                     SkillContainer skillcontainer = playerpatch.getSkill(SkillSlots.GUARD);
                     if (playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.VANILLA) {
                        vanillaMode.setTrue();
                     }

                     if (skillcontainer.getSkill() != null && skillcontainer.getSkill().canExecute(skillcontainer)) {
                        canGuard.setValue(true);
                     }

                     if (!vanillaMode.getValue()) {
                        if (controlEngine.minecraft.f_91077_.m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS) {
                           if (canGuard.booleanValue() && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
                              event.setSwingHand(false);
                              event.setCanceled(true);
                           }
                        } else if (canGuard.booleanValue()) {
                           InteractionResult interactionResult = switch (controlEngine.minecraft.f_91077_.m_6662_()) {
                              case ENTITY -> ((EntityHitResult)controlEngine.minecraft.f_91077_)
                                 .m_82443_()
                                 .m_6096_(controlEngine.minecraft.f_91074_, event.getHand());
                              case BLOCK -> {
                                 BlockHitResult blockHitResult = (BlockHitResult)controlEngine.minecraft.f_91077_;
                                 BlockPos blockpos = blockHitResult.m_82425_();
                                 BlockState blockstate = controlEngine.minecraft.f_91073_.m_8055_(blockpos);
                                 FakeLevel fakeLevelForSimulation = FakeLevel.getFakeLevel(controlEngine.minecraft.f_91073_);
                                 FakeLevel.FakeClientPlayer fakePlayerForSimulation = FakeLevel.getFakePlayer(controlEngine.minecraft.f_91074_.m_36316_());
                                 yield blockstate.m_60664_(fakeLevelForSimulation, fakePlayerForSimulation, event.getHand(), blockHitResult);
                              }
                              default -> throw new IllegalArgumentException();
                           };
                           if (interactionResult != InteractionResult.PASS && ClientConfig.keyConflictResolveScope.cancelInteraction()) {
                              event.setSwingHand(false);
                              event.setCanceled(true);
                           } else if (interactionResult == InteractionResult.PASS && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
                              event.setSwingHand(false);
                              event.setCanceled(true);
                           }
                        }
                     }
                  }

                  if (EpicFightCameraAPI.getInstance().isTPSMode() && !event.isCanceled() && event.shouldSwingHand()) {
                     EpicFightCameraAPI.getInstance().alignPlayerLookToCrosshair(false, true, false);
                  }
               }
            }
         }
      }
   }
}
