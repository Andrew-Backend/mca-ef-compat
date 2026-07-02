package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.datapack.screen.DatapackEditScreen;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public class IngameConfigurationScreen extends Screen {
   protected final Screen parentScreen;

   public IngameConfigurationScreen(Screen screen) {
      super(Component.m_237115_(EpicFightMod.format("gui.%s.configurations")));
      this.parentScreen = screen;
   }

   protected void m_7856_() {
      this.m_142416_(
         Button.m_253074_(
               Component.m_237115_(EpicFightMod.format("gui.%s.button.graphics")),
               button -> Minecraft.m_91087_().m_91152_(new EpicFightGraphicOptionScreen(this))
            )
            .m_252794_(this.f_96543_ / 2 - 165, 42)
            .m_253046_(160, 20)
            .m_253136_()
      );
      this.m_142416_(
         Button.m_253074_(
               Component.m_237115_(EpicFightMod.format("gui.%s.button.controls")),
               button -> Minecraft.m_91087_().m_91152_(new EpicFightControlOptionScreen(this))
            )
            .m_252794_(this.f_96543_ / 2 + 5, 42)
            .m_253046_(160, 20)
            .m_253136_()
      );
      this.m_142416_(
         Button.m_253074_(
               Component.m_237115_(EpicFightMod.format("gui.%s.button.datapack_edit")), button -> Minecraft.m_91087_().m_91152_(new DatapackEditScreen(this))
            )
            .m_252794_(this.f_96543_ / 2 - 165, 68)
            .m_253046_(160, 20)
            .m_253136_()
      );
      Tooltip unsupportedReason = null;
      if (EpicFightSharedConstants.IS_DEV_ENV) {
         unsupportedReason = Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.tooltip.dev_environment")));
      } else if (!EpicFightServerConnectionHelper.supported()) {
         unsupportedReason = Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.tooltip.communication_module_error")));
      } else if (ClientEngine.getInstance().getAuthHelper().status() == AuthenticationHelper.Status.OFFLINE_MODE) {
         unsupportedReason = Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.tooltip.offline_mode")));
      }

      Button skinConfigScreen = Button.m_253074_(
            Component.m_237115_("gui.epicskins.button.skin_configuration"),
            button -> {
               if (Minecraft.m_91087_().f_91073_ == null) {
                  Minecraft.m_91087_().m_91152_(ClientEngine.getInstance().getAuthHelper().getAvatarEditorScreen(this));
               } else {
                  Minecraft.m_91087_()
                     .m_91152_(
                        new MessageScreen(
                              "Warning", "You may not open avatar screen while in the world", this, button2 -> Minecraft.m_91087_().m_91152_(this), 300, 70
                           )
                           .autoCalculateHeight()
                     );
               }
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, 68)
         .m_253046_(160, 20)
         .m_257505_(unsupportedReason)
         .m_253136_();
      skinConfigScreen.f_93623_ = unsupportedReason == null;
      this.m_142416_(skinConfigScreen);
      this.m_142416_(
         Button.m_253074_(CommonComponents.f_130655_, button -> this.f_96541_.m_91152_(this.parentScreen))
            .m_252987_(this.f_96543_ / 2 - 100, this.f_96544_ - 40, 200, 20)
            .m_253136_()
      );
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
      guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 15, 16777215);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }
}
