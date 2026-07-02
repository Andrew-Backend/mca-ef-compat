package fabric.net.mca.client.gui;

import fabric.net.mca.MCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.FamilyTreeUUIDLookup;
import fabric.net.mca.util.compat.ButtonWidget;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public class FamilyTreeSearchScreen extends class_437 {
   static final int DATA_WIDTH = 120;
   private List<FamilyTreeSearchScreen.Entry> list = new LinkedList<>();
   private ButtonWidget buttonPage;
   private int pageNumber;
   private FamilyTreeSearchScreen.Entry selectedVillager;
   private int mouseX;
   private int mouseY;

   public FamilyTreeSearchScreen() {
      super(class_2561.method_43471("gui.family_tree.title"));
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25426() {
      class_342 field = (class_342)this.method_37063(
         new class_342(
            this.field_22793, this.field_22789 / 2 - 60, this.field_22790 / 2 - 80, 120, 18, class_2561.method_43471("structure_block.structure_name")
         )
      );
      field.method_1880(32);
      field.method_1863(this::searchVillager);
      field.method_25365(true);
      this.method_25395(field);
      this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 44, this.field_22790 / 2 + 82, 88, 20, class_2561.method_43471("gui.done"), sender -> this.method_25419())
      );
      this.method_37063(new ButtonWidget(this.field_22789 / 2 - 24 - 20, this.field_22790 / 2 + 60, 20, 20, class_2561.method_43470("<"), b -> {
         if (this.pageNumber > 0) {
            this.pageNumber--;
         }
      }));
      this.method_37063(new ButtonWidget(this.field_22789 / 2 + 24, this.field_22790 / 2 + 60, 20, 20, class_2561.method_43470(">"), b -> {
         if (this.pageNumber < Math.ceil(this.list.size() / 9.0) - 1.0) {
            this.pageNumber++;
         }
      }));
      this.buttonPage = (ButtonWidget)this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 24, this.field_22790 / 2 + 60, 48, 20, class_2561.method_43470("0/0)"), b -> {})
      );
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      assert this.field_22787 != null;
      this.mouseX = (int)(this.field_22787.field_1729.method_1603() * this.field_22789 / this.field_22787.method_22683().method_4489());
      this.mouseY = (int)(this.field_22787.field_1729.method_1604() * this.field_22790 / this.field_22787.method_22683().method_4506());
      context.method_25294(this.field_22789 / 2 - 60 - 10, this.field_22790 / 2 - 110, this.field_22789 / 2 + 60 + 10, this.field_22790 / 2 + 110, 1711276032);
      this.method_25420(context);
      this.renderVillagers(context);
      context.method_27534(this.field_22793, class_2561.method_43471("gui.title.family_tree"), this.field_22789 / 2, this.field_22790 / 2 - 100, 16777215);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void renderVillagers(class_332 context) {
      int maxPages = (int)Math.ceil(this.list.size() / 9.0);
      this.buttonPage.method_25355(class_2561.method_43470(this.pageNumber + 1 + "/" + maxPages));
      this.selectedVillager = null;

      for (int i = 0; i < 9; i++) {
         int index = i + this.pageNumber * 9;
         if (index >= this.list.size()) {
            break;
         }

         int y = this.field_22790 / 2 - 52 + i * 12;
         boolean hover = this.isMouseWithin(this.field_22789 / 2 - 50, y - 1, 100, 12);
         FamilyTreeSearchScreen.Entry entry = this.list.get(index);
         class_2561 text;
         if (MCA.isBlankString(entry.mother) && MCA.isBlankString(entry.father)) {
            text = class_2561.method_43471("gui.family_tree.child_of_0");
         } else if (MCA.isBlankString(entry.mother)) {
            text = class_2561.method_43469("gui.family_tree.child_of_1", new Object[]{entry.father});
         } else if (MCA.isBlankString(entry.father)) {
            text = class_2561.method_43469("gui.family_tree.child_of_1", new Object[]{entry.mother});
         } else {
            text = class_2561.method_43469("gui.family_tree.child_of_2", new Object[]{entry.father, entry.mother});
         }

         context.method_27534(this.field_22793, text, this.field_22789 / 2, y, hover ? -2631804 : -1);
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

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.selectedVillager != null) {
         this.selectVillager(this.selectedVillager.name, this.selectedVillager.uuid);
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   void selectVillager(String name, UUID villager) {
      assert this.field_22787 != null;
      this.field_22787.method_1507(new FamilyTreeScreen(villager));
   }

   public record Entry(UUID uuid, String name, String father, String mother) implements Serializable {
   }
}
