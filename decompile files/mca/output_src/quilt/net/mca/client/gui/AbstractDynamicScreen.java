package quilt.net.mca.client.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import quilt.net.mca.client.resources.Icon;
import quilt.net.mca.entity.interaction.Constraint;

public abstract class AbstractDynamicScreen extends class_437 {
   protected static final float iconScale = 1.5F;
   private String activeScreen = "main";
   private int mouseX;
   private int mouseY;
   private Set<Constraint> constraints = new HashSet<>();

   protected AbstractDynamicScreen(class_2561 title) {
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

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }

   protected abstract void buttonPressed(Button var1);

   protected void disableButton(String id) {
      this.method_25396().forEach(b -> {
         if (b instanceof AbstractDynamicScreen.ButtonEx && ((AbstractDynamicScreen.ButtonEx)b).getApiButton().identifier().equals(id)) {
            ((AbstractDynamicScreen.ButtonEx)b).field_22763 = false;
         }
      });
   }

   protected void enableAllButtons() {
      this.method_25396().forEach(b -> {
         if (b instanceof class_339) {
            ((class_339)b).field_22763 = true;
         }
      });
   }

   protected void disableAllButtons() {
      this.method_25396().forEach(b -> {
         if (b instanceof class_339) {
            if (b instanceof AbstractDynamicScreen.ButtonEx) {
               if (!((AbstractDynamicScreen.ButtonEx)b).getApiButton().identifier().equals("gui.button.backarrow")) {
                  ((class_339)b).field_22763 = false;
               }
            } else {
               ((class_339)b).field_22763 = false;
            }
         }
      });
   }

   public void setLayout(String guiKey) {
      this.activeScreen = guiKey;
      this.method_37067();
      MCAScreens.getInstance().getScreen(guiKey).ifPresent(buttons -> {
         for (Button b : buttons) {
            this.method_37063(new AbstractDynamicScreen.ButtonEx(b, this));
         }
      });
   }

   protected void drawIcon(class_332 context, class_2960 texture, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.method_25302(texture, (int)(icon.x() / 1.5F), (int)(icon.y() / 1.5F), icon.u(), icon.v(), 16, 16);
   }

   protected void drawHoveringIconText(class_332 context, class_2561 text, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.method_51438(this.field_22793, text, icon.x() + 16, icon.y() + 20);
   }

   protected void drawHoveringIconText(class_332 context, List<class_2561> text, String key) {
      Icon icon = MCAScreens.getInstance().getIcon(key);
      context.method_51434(this.field_22793, text, icon.x() + 16, icon.y() + 20);
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

   private static class ButtonEx extends class_4185 {
      private final Button apiButton;

      public ButtonEx(Button apiButton, AbstractDynamicScreen screen) {
         super(
            (int)(screen.field_22789 * AbstractDynamicScreen.Alignment.alignments.get(apiButton.align()).h + apiButton.x()),
            (int)(screen.field_22790 * AbstractDynamicScreen.Alignment.alignments.get(apiButton.align()).v + apiButton.y()),
            apiButton.width(),
            apiButton.height(),
            class_2561.method_43471(apiButton.identifier()),
            a -> screen.buttonPressed(apiButton),
            field_40754
         );
         this.apiButton = apiButton;
         if (!apiButton.isValidForConstraint(screen.getConstraints())) {
            if (apiButton.hideOnFail()) {
               this.field_22764 = false;
            }

            this.field_22763 = false;
         }
      }

      public Button getApiButton() {
         return this.apiButton;
      }
   }
}
