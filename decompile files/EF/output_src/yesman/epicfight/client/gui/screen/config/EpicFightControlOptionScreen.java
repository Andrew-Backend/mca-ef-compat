package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.gui.widgets.RewindableButton;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightControlOptionScreen extends EpicFightOptionSubScreen {
   private EpicFightOptionList optionsList;

   public EpicFightControlOptionScreen(Screen parentScreen) {
      super(parentScreen, Component.m_237115_(EpicFightMod.format("gui.%s.control_options")));
   }

   @Override
   protected void m_7856_() {
      super.m_7856_();
      this.optionsList = new EpicFightOptionList(this.f_96541_, this.f_96543_, this.f_96544_, 32, this.f_96544_ - 32, 25);
      int buttonHeight = -32;
      Button longPressCounterButton = new RewindableButton(
         this.f_96543_ / 2 - 165,
         this.f_96544_ / 4 + buttonHeight,
         160,
         20,
         Component.m_237110_(EpicFightMod.format("gui.%s.long_press_counter"), new Object[]{ItemStack.f_41584_.format(ClientConfig.longPressCounter)}),
         button -> {
            ClientConfig.longPressCounter = MathUtils.wrapClamp(++ClientConfig.longPressCounter, 1, 10);
            button.m_93666_(
               Component.m_237110_(EpicFightMod.format("gui.%s.long_press_counter"), new Object[]{ItemStack.f_41584_.format(ClientConfig.longPressCounter)})
            );
         },
         button -> {
            ClientConfig.longPressCounter = MathUtils.wrapClamp(--ClientConfig.longPressCounter, 1, 10);
            button.m_93666_(
               Component.m_237110_(EpicFightMod.format("gui.%s.long_press_counter"), new Object[]{ItemStack.f_41584_.format(ClientConfig.longPressCounter)})
            );
         }
      );
      longPressCounterButton.m_257544_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.long_press_counter.tooltip"))));
      Button cameraAutoSwitchButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.autoSwitchCamera ? "on" : "off"))), button -> {
               ClientConfig.autoSwitchCamera = !ClientConfig.autoSwitchCamera;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.autoSwitchCamera ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.camera_auto_switch.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(longPressCounterButton, cameraAutoSwitchButton);
      buttonHeight += 24;
      Button resolveKeyConflictsButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.key_conflict_resolve_scope." + ClientConfig.keyConflictResolveScope.m_7912_())), button -> {
               ClientConfig.keyConflictResolveScope = ClientConfig.keyConflictResolveScope.nextEnum();
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.key_conflict_resolve_scope." + ClientConfig.keyConflictResolveScope.m_7912_())));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.key_conflict_resolve_scope.tooltip"))))
         .m_253136_();
      Button cameraPerspectiveToggleMode = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode." + ClientConfig.cameraPerspectiveToggleMode.m_7912_())),
            button -> {
               ClientConfig.cameraPerspectiveToggleMode = ClientConfig.cameraPerspectiveToggleMode.nextEnum();
               button.m_93666_(
                  Component.m_237115_(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode." + ClientConfig.cameraPerspectiveToggleMode.m_7912_()))
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(resolveKeyConflictsButton, cameraPerspectiveToggleMode);
      buttonHeight += 24;
      Button enableLockOnQuickShiftButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.lock_on_quick_shift." + (ClientConfig.lockOnQuickShift ? "on" : "off"))), button -> {
               ClientConfig.lockOnQuickShift = !ClientConfig.lockOnQuickShift;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.lock_on_quick_shift." + (ClientConfig.lockOnQuickShift ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.lock_on_quick_shift.tooltip"))))
         .m_253136_();
      Button lockOnRangeButton = new RewindableButton(
         this.f_96543_ / 2 + 5,
         this.f_96544_ / 4 + buttonHeight,
         160,
         20,
         Component.m_237110_(EpicFightMod.format("gui.%s.lock_on_range"), new Object[]{ItemStack.f_41584_.format(ClientConfig.lockOnRange)}),
         button -> {
            ClientConfig.lockOnRange = MathUtils.wrapClamp(++ClientConfig.lockOnRange, 5, 25);
            button.m_93666_(Component.m_237110_(EpicFightMod.format("gui.%s.lock_on_range"), new Object[]{ItemStack.f_41584_.format(ClientConfig.lockOnRange)}));
         },
         button -> {
            ClientConfig.lockOnRange = MathUtils.wrapClamp(--ClientConfig.lockOnRange, 5, 25);
            button.m_93666_(Component.m_237110_(EpicFightMod.format("gui.%s.lock_on_range"), new Object[]{ItemStack.f_41584_.format(ClientConfig.lockOnRange)}));
         }
      );
      lockOnRangeButton.m_257544_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.lock_on_range.tooltip"))));
      this.optionsList.addSmall(enableLockOnQuickShiftButton, lockOnRangeButton);
      buttonHeight += 24;
      Button itemPreferenceButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.item_preferences")), button -> this.f_96541_.m_91152_(new ItemsPreferenceScreen(this))
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.item_preferences.tooltip"))))
         .m_253136_();
      Button preferenceWorkButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.preference_work." + ClientConfig.preferenceWork.m_7912_())), button -> {
               ClientConfig.preferenceWork = ClientConfig.preferenceWork.nextEnum();
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.preference_work." + ClientConfig.preferenceWork.m_7912_())));
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.preference_work.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(itemPreferenceButton, preferenceWorkButton);
      buttonHeight += 24;
      this.m_7787_(this.optionsList);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      ClientEngine.getInstance().renderEngine.versionNotifier.render(guiGraphics, false);
      this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
   }
}
