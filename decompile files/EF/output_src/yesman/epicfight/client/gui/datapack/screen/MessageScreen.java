package yesman.epicfight.client.gui.datapack.screen;

import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import yesman.epicfight.client.gui.datapack.widgets.DataBindingComponent;

public class MessageScreen<T> extends Screen {
   protected final OnPress onOkPress;
   protected final OnPress onCancelPress;
   protected final Screen parentScreen;
   protected final Component message;
   protected final Consumer<T> onOkPressWithInput;
   protected final DataBindingComponent<T, ?> inputWidget;
   protected int messageBoxWidth;
   protected int messageBoxHeight;
   protected Component okButtomTitle;
   protected Component cancelButtomTitle;
   protected int layerFarPlane = 100;

   public MessageScreen(String title, String message, Screen parentScreen, OnPress onOkPres, int width, int height) {
      this(title, Component.m_237113_(message), parentScreen, onOkPres, null, width, height);
   }

   public MessageScreen(String title, Component message, Screen parentScreen, OnPress onOkPres, int width, int height) {
      this(title, message, parentScreen, onOkPres, null, width, height);
   }

   public MessageScreen(String title, String message, Screen parentScreen, OnPress onOkPress, @Nullable OnPress onCancelPress, int width, int height) {
      this(title, Component.m_237113_(message), parentScreen, onOkPress, onCancelPress, width, height);
   }

   public MessageScreen(String title, Component message, Screen parentScreen, OnPress onOkPress, @Nullable OnPress onCancelPress, int width, int height) {
      super(Component.m_237113_(title));
      this.onOkPress = onOkPress;
      this.onCancelPress = onCancelPress;
      this.parentScreen = parentScreen;
      this.message = message;
      this.messageBoxWidth = width;
      this.messageBoxHeight = height;
      this.onOkPressWithInput = null;
      this.inputWidget = null;
      this.f_96541_ = parentScreen.getMinecraft();
   }

   public MessageScreen(
      String title,
      String message,
      Screen parentScreen,
      Consumer<T> onOkPressWithInput,
      @Nullable OnPress onCancelPress,
      DataBindingComponent<T, ?> inputWidget,
      int width,
      int height
   ) {
      super(Component.m_237113_(title));
      this.onOkPress = null;
      this.onCancelPress = onCancelPress;
      this.parentScreen = parentScreen;
      this.message = Component.m_237113_(message);
      this.messageBoxWidth = width;
      this.messageBoxHeight = height;
      this.onOkPressWithInput = onOkPressWithInput;
      this.inputWidget = inputWidget;
      this.f_96541_ = parentScreen.getMinecraft();
   }

   public MessageScreen<T> autoCalculateHeight() {
      int titleLength = this.f_96541_.f_91062_.m_92923_(this.f_96539_, this.messageBoxWidth - 16).size();
      int messageLength = this.f_96541_.f_91062_.m_92923_(this.message, this.messageBoxWidth - 16).size();
      this.messageBoxHeight = 40 + messageLength * 15 + titleLength * 15;
      return this;
   }

   public MessageScreen<T> withOkTitle(Component okButtomTitle) {
      this.okButtomTitle = okButtomTitle;
      return this;
   }

   public MessageScreen<T> withCancelTitle(Component cancelButtomTitle) {
      this.cancelButtomTitle = cancelButtomTitle;
      return this;
   }

   protected void m_7856_() {
      this.parentScreen.m_6575_(this.f_96541_, this.f_96543_, this.f_96544_);
      int height = this.messageBoxHeight / 2;
      int stringWidthOk = this.f_96541_.f_91062_.m_92852_(this.okButtomTitle != null ? this.okButtomTitle : CommonComponents.f_286989_) + 10;
      int stringWidthCancel = this.f_96541_.f_91062_.m_92852_(this.cancelButtomTitle != null ? this.cancelButtomTitle : CommonComponents.f_130656_) + 10;
      int buttonWidht = Math.max(Math.max(stringWidthOk, stringWidthCancel), 55);
      if (this.onOkPress != null || this.onOkPressWithInput != null) {
         this.m_142416_(
            Button.m_253074_(this.okButtomTitle != null ? this.okButtomTitle : CommonComponents.f_286989_, button -> {
                  if (this.onOkPress != null) {
                     this.onOkPress.m_93750_(button);
                  } else {
                     this.onOkPressWithInput.accept(this.inputWidget._getValue());
                  }
               })
               .m_252987_(
                  this.f_96543_ / 2 - (buttonWidht + 1), this.f_96544_ / 2 + height - 20, buttonWidht + (this.onCancelPress == null ? buttonWidht + 2 : 0), 16
               )
               .m_253136_()
         );
      }

      if (this.onCancelPress != null) {
         this.m_142416_(
            Button.m_253074_(this.cancelButtomTitle != null ? this.cancelButtomTitle : CommonComponents.f_130656_, this.onCancelPress)
               .m_252987_(this.f_96543_ / 2 + 1, this.f_96544_ / 2 + height - 20, buttonWidht, 16)
               .m_253136_()
         );
      }

      if (this.inputWidget != null) {
         this.m_142416_(this.inputWidget);
      }
   }

   public void m_86600_() {
      this.parentScreen.m_86600_();
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.parentScreen.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, this.layerFarPlane);
      guiGraphics.m_280024_(0, 0, this.f_96543_, this.f_96544_, -1072689136, -804253680);
      int width = this.messageBoxWidth / 2;
      int height = this.messageBoxHeight / 2;
      List<FormattedCharSequence> messageLines = this.f_96541_.f_91062_.m_92923_(this.message, width * 2 - 16);
      int messageWidth = this.f_96541_.f_91062_.m_92852_(this.f_96539_);
      guiGraphics.m_280509_(this.f_96543_ / 2 - width, this.f_96544_ / 2 - height, this.f_96543_ / 2 + width, this.f_96544_ / 2 + height, -6250336);
      guiGraphics.m_280509_(
         this.f_96543_ / 2 - width + 1, this.f_96544_ / 2 - height + 1, this.f_96543_ / 2 + width - 1, this.f_96544_ / 2 + height - 1, -16777215
      );
      int y = this.f_96544_ / 2 - height + 10;
      if (!StringUtil.isNullOrEmpty(this.f_96539_.getString())) {
         guiGraphics.m_280430_(this.f_96547_, this.f_96539_, this.f_96543_ / 2 - messageWidth / 2, y, 16777215);
         y += 20;
      }

      for (FormattedCharSequence charSequence : messageLines) {
         guiGraphics.m_280648_(this.f_96547_, charSequence, this.f_96543_ / 2 - width + 8, y, 16777215);
         y += 15;
      }

      if (this.inputWidget != null) {
         this.inputWidget._setWidth((width - 20) * 2);
         this.inputWidget._setX(this.f_96543_ / 2 - width + 20);
         this.inputWidget._setY(y + 8);
      }

      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.m_280168_().m_85849_();
   }

   public MessageScreen<T> setLayerFarPlane(int i) {
      this.layerFarPlane = i;
      return this;
   }
}
