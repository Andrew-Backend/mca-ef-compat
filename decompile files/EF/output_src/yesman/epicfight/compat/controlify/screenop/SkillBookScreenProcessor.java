package yesman.epicfight.compat.controlify.screenop;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import yesman.epicfight.client.gui.screen.SkillBookScreen;

public class SkillBookScreenProcessor extends ScreenProcessor<SkillBookScreen> {
   private static final InputBindingSupplier LEARN_SKILL = ControlifyBindings.GUI_PRESS;

   public SkillBookScreenProcessor(SkillBookScreen screen) {
      super(screen);
   }

   protected void handleButtons(ControllerEntity controller) {
      if (LEARN_SKILL.on(controller).guiPressed().get()) {
         ((SkillBookScreen)this.screen).getLearnButton().m_5691_();
         playClackSound();
      }

      super.handleButtons(controller);
   }

   protected void setInitialFocus() {
   }

   protected void handleComponentNavigation(ControllerEntity controller) {
   }

   public void onWidgetRebuild() {
      super.onWidgetRebuild();
      ButtonGuideApi.addGuideToButton(((SkillBookScreen)this.screen).getLearnButton(), LEARN_SKILL, ButtonGuidePredicate.always());
   }
}
