package yesman.epicfight.compat.controlify.screenop;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import yesman.epicfight.client.gui.screen.SkillEditScreen;

public class SkillEditScreenProcessor extends ScreenProcessor<SkillEditScreen> {
   private static final InputBindingSupplier OPEN_SKILL_INFO = ControlifyBindings.GUI_ABSTRACT_ACTION_1;

   public SkillEditScreenProcessor(SkillEditScreen screen) {
      super(screen);
   }

   protected void handleButtons(ControllerEntity controller) {
      super.handleButtons(controller);
      if (((SkillEditScreen)this.screen).m_7222_() instanceof SkillEditScreen.EquipSkillButton equipSkillButton
         && OPEN_SKILL_INFO.on(controller).guiPressed().get()) {
         equipSkillButton.openSkillInfoScreen();
      }
   }

   protected void setInitialFocus() {
   }

   public void onWidgetRebuild() {
      super.onWidgetRebuild();
      this.setInputTypeWorkaround();
   }

   private void setInputTypeWorkaround() {
      Minecraft.m_91087_().m_264033_(InputType.KEYBOARD_ARROW);
   }
}
