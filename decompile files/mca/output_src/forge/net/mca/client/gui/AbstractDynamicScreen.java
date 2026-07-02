package forge.net.mca.client.gui;

import forge.net.mca.client.resources.Icon;
import forge.net.mca.entity.interaction.Constraint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractDynamicScreen extends Screen {
   protected static final float iconScale = 1.5F;
   private String activeScreen = "main";
   private int mouseX;
   private int mouseY;
   private Set<Constraint> constraints = new HashSet<>();

   protected AbstractDynamicScreen(Component title) {
      super(title);
   }

   public String getActiveScreen() {
      return this.activeScreen;
   }

   public Set<Constraint> getConstraints() {
      return this.constraints;
   }

   public void setConstraints(Set<Constraint> constraints) {
      this.constraints = constraints;
      this.setLayout(this.activeScreen);
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.m_88315_(context, mouseX, mouseY, delta);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   protected abstract void buttonPressed(Button var1);

   protected void disableButton(String id) {
      this.m_6702_().forEach(b -> {
         if (b instanceof AbstractDynamicScreen.ButtonEx && ((AbstractDynamicScreen.ButtonEx)b).getApiButton().identifier().equals(id)) {
            ((AbstractDynamicScreen.ButtonEx)b).f_93623_ = false;
         }
      });
   }

   protected void enableAllButtons() {
      this.m_6702_().forEach(b -> {
         if (b instanceof AbstractWidget) {
            ((AbstractWidget)b).f_93623_ = true;
         }
      });
   }

   protected void disableAllButtons() {
      this.m_6702_().forEach(b -> {
         if (b instanceof AbstractWidget) {
            if (b instanceof AbstractDynamicScreen.ButtonEx) {
               if (!((AbstractDynamicScreen.ButtonEx)b).getApiButton().identifier().equals("gui.button.backarrow")) {
                  ((AbstractWidget)b).f_93623_ = false;
               }
            } else {
               ((AbstractWidget)b).f_93623_ = false;
            }
         }
      });
   }

   public void setLayout(String guiKey) {
      this.activeScreen = guiKey;
      this.m_169413_();
      MCAScreens.getInstance().getScreen(guiKey).ifPresent(buttons -> {
         for (Button b : buttons) {
            this.m_142416_(new AbstractDynamicScreen.ButtonEx(b, this));
         }
      });
   }

   protected void drawIcon(GuiGraphics context, ResourceLocation texture, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.m_280218_(texture, (int)(icon.x() / 1.5F), (int)(icon.y() / 1.5F), icon.u(), icon.v(), 16, 16);
   }

   protected void drawHoveringIconText(GuiGraphics context, Component text, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.m_280557_(this.f_96547_, text, icon.x() + 16, icon.y() + 20);
   }

   protected void drawHoveringIconText(GuiGraphics context, List<Component> text, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.m_280666_(this.f_96547_, text, icon.x() + 16, icon.y() + 20);
   }

   protected boolean hoveringOverIcon(String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      return this.hoveringOver(icon.x(), icon.y(), 24, 24);
   }

   protected boolean hoveringOver(int x, int y, int w, int h) {
      return this.mouseX > x && this.mouseX < x + w && this.mouseY > y && this.mouseY < y + h;
   }

   private enum Alignment {
      TOP_LEFT(0.0F, 0.0F),
      TOP(0.5F, 0.0F),
      TOP_RIGHT(1.0F, 0.0F),
      RIGHT(1.0F, 0.5F),
      BOTTOM_RIGHT(1.0F, 1.0F),
      BOTTOM(0.5F, 1.0F),
      BOTTOM_LEFT(0.0F, 1.0F),
      LEFT(0.0F, 0.5F),
      CENTER(0.5F, 0.5F);

      final float h;
      final float v;
      static final Map<String, AbstractDynamicScreen.Alignment> alignments = new HashMap<>();

      Alignment(float h, float v) {
         this.h = h;
         this.v = v;
      }

      static {
         for (AbstractDynamicScreen.Alignment a : values()) {
            alignments.put(a.name().toLowerCase(Locale.ENGLISH), a);
         }
      }
   }

   private static class ButtonEx extends net.minecraft.client.gui.components.Button {
      private final Button apiButton;

      public ButtonEx(Button apiButton, AbstractDynamicScreen screen) {
         super(
            (int)(screen.f_96543_ * AbstractDynamicScreen.Alignment.alignments.get(apiButton.align()).h + apiButton.x()),
            (int)(screen.f_96544_ * AbstractDynamicScreen.Alignment.alignments.get(apiButton.align()).v + apiButton.y()),
            apiButton.width(),
            apiButton.height(),
            Component.m_237115_(apiButton.identifier()),
            a -> screen.buttonPressed(apiButton),
            f_252438_
         );
         this.apiButton = apiButton;
         if (!apiButton.isValidForConstraint(screen.getConstraints())) {
            if (apiButton.hideOnFail()) {
               this.f_93624_ = false;
            }

            this.f_93623_ = false;
         }
      }

      public Button getApiButton() {
         return this.apiButton;
      }
   }
}
