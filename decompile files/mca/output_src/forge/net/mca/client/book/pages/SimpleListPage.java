package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SimpleListPage extends ListPage {
   public SimpleListPage(List<Component> text) {
      super(text);
   }

   @Override
   int getEntriesPerPage() {
      return 14;
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      int y = 20;

      for (int i = this.page * this.getEntriesPerPage(); i < Math.min(this.text.size(), (this.page + 1) * this.getEntriesPerPage()); i++) {
         context.m_280614_(screen.getTextRenderer(), this.text.get(i), (screen.f_96543_ - 192) / 2 + 36, y, -16777216, screen.getBook().hasTextShadow());
         y += 10;
      }
   }
}
