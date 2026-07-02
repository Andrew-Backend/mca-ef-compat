package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.config.ClientConfig;

public class EpicFightOptionSubScreen extends Screen {
   protected final Screen lastScreen;

   public EpicFightOptionSubScreen(Screen parentScreen, Component title) {
      super(title);
      this.lastScreen = parentScreen;
   }

   protected void m_7856_() {
      this.m_142416_(Button.m_253074_(CommonComponents.f_130655_, button -> {
         ClientConfig.saveChanges();
         this.m_7379_();
      }).m_252987_(this.f_96543_ / 2 - 100, this.f_96544_ - 28, 200, 20).m_253136_());
   }

   public void m_7861_() {
      ClientConfig.saveChanges();
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.lastScreen);
   }

   protected void basicListRender(GuiGraphics guiGraphics, EpicFightOptionList optionList, int mouseX, int mouseY, float partialTicks) {
      this.m_280273_(guiGraphics);
      optionList.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 20, 16777215);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }
}
