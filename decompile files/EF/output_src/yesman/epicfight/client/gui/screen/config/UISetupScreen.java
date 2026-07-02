package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.ScreenCalculations;
import yesman.epicfight.client.gui.widgets.UIComponent;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.config.OptionHandler;
import yesman.epicfight.main.EpicFightMod;

public class UISetupScreen extends Screen {
   protected final Screen parentScreen;
   private UIComponent draggingButton;

   public UISetupScreen(Screen parentScreen) {
      super(Component.m_237113_(EpicFightMod.format("%s.gui.configuration.ui_setup")));
      this.parentScreen = parentScreen;
   }

   public void m_7856_() {
      this.f_169369_.clear();
      int weaponInnateX = ClientConfig.weaponInnateBaseX.positionGetter.apply(this.f_96543_, ClientConfig.weaponInnateX);
      int weaponInnateY = ClientConfig.weaponInnateBaseY.positionGetter.apply(this.f_96544_, ClientConfig.weaponInnateY);
      OptionHandler<Integer> weaponInnateXHandler = OptionHandler.of(ClientConfig.weaponInnateX, val -> ClientConfig.weaponInnateX = val);
      OptionHandler<Integer> weaponInnateYHandler = OptionHandler.of(ClientConfig.weaponInnateY, val -> ClientConfig.weaponInnateY = val);
      OptionHandler<ScreenCalculations.HorizontalBasis> weaponInnateBaseXHandler = OptionHandler.of(
         ClientConfig.weaponInnateBaseX, val -> ClientConfig.weaponInnateBaseX = val
      );
      OptionHandler<ScreenCalculations.VerticalBasis> weaponInnateBaseYHandler = OptionHandler.of(
         ClientConfig.weaponInnateBaseY, val -> ClientConfig.weaponInnateBaseY = val
      );
      this.m_142416_(
         new UIComponent(
            weaponInnateX,
            weaponInnateY,
            weaponInnateXHandler,
            weaponInnateYHandler,
            weaponInnateBaseXHandler,
            weaponInnateBaseYHandler,
            32,
            32,
            0,
            0,
            1,
            1,
            1,
            1,
            0,
            163,
            184,
            this,
            EpicFightMod.identifier("textures/gui/skills/weapon_innate/sweeping_edge.png")
         )
      );
      int staminaX = ClientConfig.staminaBarBaseX.positionGetter.apply(this.f_96543_, ClientConfig.staminaBarX);
      int staminaY = ClientConfig.staminaBarBaseY.positionGetter.apply(this.f_96544_, ClientConfig.staminaBarY);
      OptionHandler<Integer> staminaBarXHandler = OptionHandler.of(ClientConfig.staminaBarX, val -> ClientConfig.staminaBarX = val);
      OptionHandler<Integer> staminaBarYHandler = OptionHandler.of(ClientConfig.staminaBarY, val -> ClientConfig.staminaBarY = val);
      OptionHandler<ScreenCalculations.HorizontalBasis> staminaBarBaseXHandler = OptionHandler.of(
         ClientConfig.staminaBarBaseX, val -> ClientConfig.staminaBarBaseX = val
      );
      OptionHandler<ScreenCalculations.VerticalBasis> staminaBarBaseYHandler = OptionHandler.of(
         ClientConfig.staminaBarBaseY, val -> ClientConfig.staminaBarBaseY = val
      );
      this.m_142416_(
         new UIComponent(
            staminaX,
            staminaY,
            staminaBarXHandler,
            staminaBarYHandler,
            staminaBarBaseXHandler,
            staminaBarBaseYHandler,
            118,
            4,
            2,
            38,
            237,
            9,
            256,
            256,
            255,
            128,
            64,
            this,
            EpicFightMod.identifier("textures/gui/battle_icons.png")
         )
      );
      int chargingBarX = ClientConfig.chargingBarBaseX.positionGetter.apply(this.f_96543_, ClientConfig.chargingBarX);
      int chargingBarY = ClientConfig.chargingBarBaseY.positionGetter.apply(this.f_96544_, ClientConfig.chargingBarY);
      OptionHandler<Integer> chargingBarXHandler = OptionHandler.of(ClientConfig.chargingBarX, val -> ClientConfig.chargingBarX = val);
      OptionHandler<Integer> chargingBarYHandler = OptionHandler.of(ClientConfig.chargingBarY, val -> ClientConfig.chargingBarY = val);
      OptionHandler<ScreenCalculations.HorizontalBasis> chargingBarBaseXHandler = OptionHandler.of(
         ClientConfig.chargingBarBaseX, val -> ClientConfig.chargingBarBaseX = val
      );
      OptionHandler<ScreenCalculations.VerticalBasis> chargingBarBaseYHandler = OptionHandler.of(
         ClientConfig.chargingBarBaseY, val -> ClientConfig.chargingBarBaseY = val
      );
      this.m_142416_(
         new UIComponent(
            chargingBarX,
            chargingBarY,
            chargingBarXHandler,
            chargingBarYHandler,
            chargingBarBaseXHandler,
            chargingBarBaseYHandler,
            238,
            13,
            1,
            71,
            237,
            13,
            256,
            256,
            255,
            255,
            255,
            this,
            EpicFightMod.identifier("textures/gui/battle_icons.png")
         )
      );
      int passiveX = ClientConfig.passiveBaseX.positionGetter.apply(this.f_96543_, ClientConfig.passiveX);
      int passiveY = ClientConfig.passiveBaseY.positionGetter.apply(this.f_96544_, ClientConfig.passiveY);
      OptionHandler<Integer> passiveXHandler = OptionHandler.of(ClientConfig.passiveX, val -> ClientConfig.passiveX = val);
      OptionHandler<Integer> passiveYHandler = OptionHandler.of(ClientConfig.passiveY, val -> ClientConfig.passiveY = val);
      OptionHandler<ScreenCalculations.HorizontalBasis> passiveBaseXHandler = OptionHandler.of(
         ClientConfig.passiveBaseX, val -> ClientConfig.passiveBaseX = val
      );
      OptionHandler<ScreenCalculations.VerticalBasis> passiveBaseYHandler = OptionHandler.of(ClientConfig.passiveBaseY, val -> ClientConfig.passiveBaseY = val);
      OptionHandler<ScreenCalculations.AlignDirection> passiveAlignDirectionHandler = OptionHandler.of(
         ClientConfig.passiveAlignDirection, val -> ClientConfig.passiveAlignDirection = val
      );
      this.m_142416_(
         new UIComponent.PassiveUIComponent(
            passiveX,
            passiveY,
            passiveXHandler,
            passiveYHandler,
            passiveBaseXHandler,
            passiveBaseYHandler,
            passiveAlignDirectionHandler,
            24,
            24,
            0,
            0,
            1,
            1,
            1,
            1,
            255,
            255,
            255,
            this,
            EpicFightMod.identifier("textures/gui/skills/guard/guard.png"),
            EpicFightMod.identifier("textures/gui/skills/passive/berserker.png")
         )
      );
      this.m_142416_(Button.m_253074_(Component.m_237113_("⟳"), button -> {
         ClientConfig.weaponInnateX = (Integer)ClientConfig.WEAPON_INNATE_X.getDefault();
         ClientConfig.weaponInnateY = (Integer)ClientConfig.WEAPON_INNATE_Y.getDefault();
         ClientConfig.weaponInnateBaseX = (ScreenCalculations.HorizontalBasis)ClientConfig.WEAPON_INNATE_BASE_X.getDefault();
         ClientConfig.weaponInnateBaseY = (ScreenCalculations.VerticalBasis)ClientConfig.WEAPON_INNATE_BASE_Y.getDefault();
         ClientConfig.staminaBarX = (Integer)ClientConfig.STAMINA_BAR_X.getDefault();
         ClientConfig.staminaBarY = (Integer)ClientConfig.STAMINA_BAR_Y.getDefault();
         ClientConfig.staminaBarBaseX = (ScreenCalculations.HorizontalBasis)ClientConfig.STAMINA_BAR_BASE_X.getDefault();
         ClientConfig.staminaBarBaseY = (ScreenCalculations.VerticalBasis)ClientConfig.STAMINA_BAR_BASE_Y.getDefault();
         ClientConfig.chargingBarX = (Integer)ClientConfig.CHARGING_BAR_X.getDefault();
         ClientConfig.chargingBarY = (Integer)ClientConfig.CHARGING_BAR_Y.getDefault();
         ClientConfig.chargingBarBaseX = (ScreenCalculations.HorizontalBasis)ClientConfig.CHARGING_BAR_BASE_X.getDefault();
         ClientConfig.chargingBarBaseY = (ScreenCalculations.VerticalBasis)ClientConfig.CHARGING_BAR_BASE_Y.getDefault();
         ClientConfig.passiveX = (Integer)ClientConfig.PASSIVE_X.getDefault();
         ClientConfig.passiveY = (Integer)ClientConfig.PASSIVE_Y.getDefault();
         ClientConfig.passiveBaseX = (ScreenCalculations.HorizontalBasis)ClientConfig.PASSIVE_BASE_X.getDefault();
         ClientConfig.passiveBaseY = (ScreenCalculations.VerticalBasis)ClientConfig.PASSIVE_BASE_Y.getDefault();
         ClientConfig.passiveAlignDirection = (ScreenCalculations.AlignDirection)ClientConfig.PASSIVE_ALIGN_DIRECTION.getDefault();
         this.m_7856_();
      }).m_252987_(this.f_96543_ - 14, 0, 14, 14).m_253136_());
   }

   public boolean m_6375_(double x, double y, int pressType) {
      for (GuiEventListener guieventlistener : this.m_6702_()) {
         if (guieventlistener instanceof UIComponent uiComponent && uiComponent.popupScreen.isOpen() && uiComponent.popupScreen.m_6375_(x, y, pressType)) {
            this.m_7522_(guieventlistener);
            if (pressType == 0) {
               this.m_7897_(true);
            }

            return true;
         }

         if (guieventlistener.m_6375_(x, y, pressType)) {
            this.m_7522_(guieventlistener);
            if (pressType == 0) {
               this.m_7897_(true);
            }

            return true;
         }
      }

      return false;
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.f_96541_.f_91073_ == null) {
         this.m_280039_(guiGraphics);
      } else {
         this.m_280273_(guiGraphics);
      }

      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public void beginToDrag(UIComponent button) {
      this.draggingButton = button;
   }

   public void endDragging() {
      this.draggingButton = null;
   }

   public boolean isDraggingComponent(UIComponent button) {
      return this.draggingButton == button;
   }
}
