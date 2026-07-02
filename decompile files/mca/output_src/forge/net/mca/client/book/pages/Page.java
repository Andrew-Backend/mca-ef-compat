package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Page {
   public abstract void render(ExtendedBookScreen var1, GuiGraphics var2, int var3, int var4, float var5);

   public void open(boolean back) {
   }

   public boolean previousPage() {
      return true;
   }

   public boolean nextPage() {
      return true;
   }
}
