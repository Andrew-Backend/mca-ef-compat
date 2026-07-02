package yesman.epicfight.client.gui.datapack.widgets;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class SubScreenOpenButton extends ResizableButton {
   protected final Supplier<Screen> subScreenProvider;

   public SubScreenOpenButton(SubScreenOpenButton.Builder builder) {
      super(builder);
      this.subScreenProvider = builder.subScreenProvider;
   }

   public void m_5691_() {
      Minecraft.m_91087_().m_91152_(this.subScreenProvider.get());
   }

   public static SubScreenOpenButton.Builder builder() {
      return new SubScreenOpenButton.Builder();
   }

   public static class Builder extends ResizableButton.Builder {
      Supplier<Screen> subScreenProvider;

      public Builder() {
         super(CommonComponents.f_238772_, null);
      }

      public SubScreenOpenButton.Builder subScreen(Supplier<Screen> subScreenProvider) {
         this.subScreenProvider = subScreenProvider;
         return this;
      }

      public SubScreenOpenButton build() {
         return new SubScreenOpenButton(this);
      }
   }
}
