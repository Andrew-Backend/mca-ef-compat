package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class RewindableButton extends Button {
   protected final OnPress onRewindPress;

   public RewindableButton(int x, int y, int width, int height, Component title, OnPress pressedAction, OnPress rewindPressedAction) {
      super(x, y, width, height, title, pressedAction, Button.f_252438_);
      this.onRewindPress = rewindPressedAction;
   }

   protected boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }

   public void onClick(double mouseX, double mouseY, int button) {
      if (button == 0) {
         super.m_5716_(mouseX, mouseY);
      } else {
         this.onRewindPress.m_93750_(this);
      }
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.f_93623_ && this.f_93624_) {
         if (this.m_7972_(button)) {
            boolean flag = this.m_93680_(mouseX, mouseY);
            if (flag) {
               this.m_7435_(Minecraft.m_91087_().m_91106_());
               this.onClick(mouseX, mouseY, button);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
