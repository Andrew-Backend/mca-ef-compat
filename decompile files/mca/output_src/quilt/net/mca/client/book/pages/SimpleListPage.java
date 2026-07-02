package quilt.net.mca.client.book.pages;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import quilt.net.mca.client.gui.ExtendedBookScreen;

public class SimpleListPage extends ListPage {
   public SimpleListPage(List<class_2561> text) {
      super(text);
   }

   @Override
   int getEntriesPerPage() {
      return 14;
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      int y = 20;

      for (int i = this.page * this.getEntriesPerPage(); i < Math.min(this.text.size(), (this.page + 1) * this.getEntriesPerPage()); i++) {
         context.method_51439(screen.getTextRenderer(), this.text.get(i), (screen.field_22789 - 192) / 2 + 36, y, -16777216, screen.getBook().hasTextShadow());
         y += 10;
      }
   }
}
