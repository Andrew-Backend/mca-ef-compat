package fabric.net.mca.client.gui;

import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.item.BabyItem;
import fabric.net.mca.network.c2s.BabyNameRequest;
import fabric.net.mca.network.c2s.BabyNamingVillagerMessage;
import fabric.net.mca.util.compat.ButtonWidget;
import java.util.Objects;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public class NameBabyScreen extends class_437 {
   private final class_1799 baby;
   private final class_1657 player;
   private class_342 babyNameTextField;

   public NameBabyScreen(class_1657 player, class_1799 baby) {
      super(class_2561.method_43471("gui.nameBaby.title"));
      this.baby = baby;
      this.player = player;
   }

   public void method_25393() {
      super.method_25393();
      this.babyNameTextField.method_1865();
   }

   public void method_25426() {
      this.method_37063(new ButtonWidget(this.field_22789 / 2 - 40, this.field_22790 / 2 + 20, 80, 20, class_2561.method_43471("gui.button.done"), b -> {
         NetworkHandler.sendToServer(new BabyNamingVillagerMessage(this.player.method_31548().field_7545, this.babyNameTextField.method_1882().trim()));
         Objects.requireNonNull(this.field_22787).method_1507(null);
      }));
      this.method_37063(
         new ButtonWidget(
            this.field_22789 / 2 + 105,
            this.field_22790 / 2 - 20,
            60,
            20,
            class_2561.method_43471("gui.button.random"),
            b -> NetworkHandler.sendToServer(new BabyNameRequest(((BabyItem)this.baby.method_7909()).getGender()))
         )
      );
      this.babyNameTextField = new class_342(
         this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 2 - 20, 200, 20, class_2561.method_43471("structure_block.structure_name")
      );
      this.babyNameTextField.method_1880(32);
      this.method_48265(this.babyNameTextField);
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25394(class_332 context, int w, int h, float scale) {
      this.method_25420(context);
      this.method_25395(this.babyNameTextField);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 70, 16777215);
      this.babyNameTextField.method_25394(context, this.field_22789 / 2 - 100, this.field_22790 / 2 - 20, scale);
      super.method_25394(context, w, h, scale);
   }

   public void setBabyName(String name) {
      this.babyNameTextField.method_1852(name);
   }
}
