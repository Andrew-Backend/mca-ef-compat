package quilt.net.mca.client.book.pages;

import net.minecraft.class_332;
import quilt.net.mca.client.gui.ExtendedBookScreen;

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
