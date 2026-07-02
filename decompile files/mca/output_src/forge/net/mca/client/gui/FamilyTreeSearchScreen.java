package forge.net.mca.client.gui;

import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.c2s.FamilyTreeUUIDLookup;
import forge.net.mca.util.compat.ButtonWidget;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FamilyTreeSearchScreen extends Screen {
   static final int DATA_WIDTH = 120;
   private List<FamilyTreeSearchScreen.Entry> list = new LinkedList<>();
   private ButtonWidget buttonPage;
   private int pageNumber;
   private FamilyTreeSearchScreen.Entry selectedVillager;
   private int mouseX;
   private int mouseY;

   public FamilyTreeSearchScreen() {
      super(Component.m_237115_("gui.family_tree.title"));
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_7856_() {
      EditBox field = (EditBox)this.m_142416_(
         new EditBox(this.f_96547_, this.f_96543_ / 2 - 60, this.f_96544_ / 2 - 80, 120, 18, Component.m_237115_("structure_block.structure_name"))
      );
      field.m_94199_(32);
      field.m_94151_(this::searchVillager);
      field.m_93692_(true);
      this.m_7522_(field);
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 44, this.f_96544_ / 2 + 82, 88, 20, Component.m_237115_("gui.done"), sender -> this.m_7379_()));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 24 - 20, this.f_96544_ / 2 + 60, 20, 20, Component.m_237113_("<"), b -> {
         if (this.pageNumber > 0) {
            this.pageNumber--;
         }
      }));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 24, this.f_96544_ / 2 + 60, 20, 20, Component.m_237113_(">"), b -> {
         if (this.pageNumber < Math.ceil(this.list.size() / 9.0) - 1.0) {
            this.pageNumber++;
         }
      }));
      this.buttonPage = (ButtonWidget)this.m_142416_(
         new ButtonWidget(this.f_96543_ / 2 - 24, this.f_96544_ / 2 + 60, 48, 20, Component.m_237113_("0/0)"), b -> {})
      );
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      assert this.f_96541_ != null;
      this.mouseX = (int)(this.f_96541_.f_91067_.m_91589_() * this.f_96543_ / this.f_96541_.m_91268_().m_85441_());
      this.mouseY = (int)(this.f_96541_.f_91067_.m_91594_() * this.f_96544_ / this.f_96541_.m_91268_().m_85442_());
      context.m_280509_(this.f_96543_ / 2 - 60 - 10, this.f_96544_ / 2 - 110, this.f_96543_ / 2 + 60 + 10, this.f_96544_ / 2 + 110, 1711276032);
      this.m_280273_(context);
      this.renderVillagers(context);
      context.m_280653_(this.f_96547_, Component.m_237115_("gui.title.family_tree"), this.f_96543_ / 2, this.f_96544_ / 2 - 100, 16777215);
      super.m_88315_(context, mouseX, mouseY, delta);
   }

   private void renderVillagers(GuiGraphics context) {
      int maxPages = (int)Math.ceil(this.list.size() / 9.0);
      this.buttonPage.m_93666_(Component.m_237113_(this.pageNumber + 1 + "/" + maxPages));
      this.selectedVillager = null;

      for (int i = 0; i < 9; i++) {
         int index = i + this.pageNumber * 9;
         if (index >= this.list.size()) {
            break;
         }

         int y = this.f_96544_ / 2 - 52 + i * 12;
         boolean hover = this.isMouseWithin(this.f_96543_ / 2 - 50, y - 1, 100, 12);
         FamilyTreeSearchScreen.Entry entry = this.list.get(index);
         Component text;
         if (MCA.isBlankString(entry.mother) && MCA.isBlankString(entry.father)) {
            text = Component.m_237115_("gui.family_tree.child_of_0");
         } else if (MCA.isBlankString(entry.mother)) {
            text = Component.m_237110_("gui.family_tree.child_of_1", new Object[]{entry.father});
         } else if (MCA.isBlankString(entry.father)) {
            text = Component.m_237110_("gui.family_tree.child_of_1", new Object[]{entry.mother});
         } else {
            text = Component.m_237110_("gui.family_tree.child_of_2", new Object[]{entry.father, entry.mother});
         }

         context.m_280653_(this.f_96547_, text, this.f_96543_ / 2, y, hover ? -2631804 : -1);
         if (hover) {
            this.selectedVillager = entry;
         }
      }
   }

   private void searchVillager(String v) {
      if (!MCA.isBlankString(v)) {
         NetworkHandler.sendToServer(new FamilyTreeUUIDLookup(v));
      }
   }

   public void setList(List<FamilyTreeSearchScreen.Entry> list) {
      this.list = list;
   }

   protected boolean isMouseWithin(int x, int y, int w, int h) {
      return this.mouseX >= x && this.mouseX < x + w && this.mouseY >= y && this.mouseY < y + h;
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.selectedVillager != null) {
         this.selectVillager(this.selectedVillager.name, this.selectedVillager.uuid);
      }

      return super.m_6375_(mouseX, mouseY, button);
   }

   void selectVillager(String name, UUID villager) {
      assert this.f_96541_ != null;
      this.f_96541_.m_91152_(new FamilyTreeScreen(villager));
   }

   public record Entry(UUID uuid, String name, String father, String mother) implements Serializable {
   }
}
