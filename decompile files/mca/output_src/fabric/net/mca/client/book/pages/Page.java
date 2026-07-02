package fabric.net.mca.client.book.pages;

import fabric.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.class_332;

public abstract class Page {
   public abstract void render(ExtendedBookScreen var1, class_332 var2, int var3, int var4, float var5);

   public void open(boolean back) {
   }

   public boolean previousPage() {
      return true;
   }

   public boolean nextPage() {
      return true;
   }
}
