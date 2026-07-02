package yesman.epicfight.epicskins.client.widget;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.epicskins.user.Cosmetic;

@OnlyIn(Dist.CLIENT)
public class CapePopupBox extends PopupBox<Cosmetic> {
   final Runnable onPress;

   public CapePopupBox(
      Screen owner,
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      Component title,
      Function<Cosmetic, String> displayStringMapper,
      Runnable onPress,
      Consumer<Pair<String, Cosmetic>> responder
   ) {
      super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, displayStringMapper, responder);
      this.onPress = onPress;
   }

   @Override
   public void m_5716_(double x, double y) {
      if (this.clickedPopupButton(x, y)) {
         this.onPress.run();
      }
   }
}
