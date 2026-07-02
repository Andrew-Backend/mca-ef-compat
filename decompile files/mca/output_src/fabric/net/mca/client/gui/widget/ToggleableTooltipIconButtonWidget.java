package fabric.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import fabric.net.mca.client.gui.InteractScreen;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_757;
import net.minecraft.class_4185.class_4241;

public class ToggleableTooltipIconButtonWidget extends ToggleableTooltipButtonWidget {
   private final int u;
   private final int v;

   public ToggleableTooltipIconButtonWidget(int x, int y, int u, int v, boolean toggle, class_5250 tooltip, class_4241 onPress) {
      super(x, y, 16, 16, toggle, class_2561.method_43470(""), tooltip, onPress);
      this.u = u;
      this.v = v;
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      this.drawIcon(context);
   }

   private void drawIcon(class_332 context) {
      RenderSystem.setShader(class_757::method_34542);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.field_22765);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      int offset = this.toggle ? 0 : 16;
      context.method_25302(InteractScreen.ICON_TEXTURES, this.method_46426(), this.method_46427(), this.u, this.v + offset, this.field_22758, this.field_22759);
   }
}
