package fabric.net.mca.client.gui;

import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.network.c2s.CallToPlayerMessage;
import fabric.net.mca.network.c2s.GetFamilyRequest;
import fabric.net.mca.util.compat.ButtonWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.class_1299;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_490;
import org.jetbrains.annotations.NotNull;

public class WhistleScreen extends class_437 {
   private List<String> keys = new ArrayList<>();
   private class_2487 villagerData = new class_2487();
   private VillagerEntityMCA dummy;
   private ButtonWidget selectionLeftButton;
   private ButtonWidget selectionRightButton;
   private ButtonWidget villagerNameButton;
   private ButtonWidget callButton;
   private int loadingAnimationTicks;
   private int selectedIndex;

   public WhistleScreen() {
      super(class_2561.method_43471("gui.whistle.title"));
   }

   public void method_25393() {
      super.method_25393();
      if (this.loadingAnimationTicks != -1) {
         this.loadingAnimationTicks++;
      }

      if (this.loadingAnimationTicks >= 20) {
         this.loadingAnimationTicks = 0;
      }
   }

   public void method_25426() {
      NetworkHandler.sendToServer(new GetFamilyRequest());
      this.selectionLeftButton = (ButtonWidget)this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 123, this.field_22790 / 2 + 65, 20, 20, class_2561.method_43470("<<"), b -> {
            if (this.selectedIndex == 0) {
               this.selectedIndex = this.keys.size() - 1;
            } else {
               this.selectedIndex--;
            }

            this.setVillagerData(this.selectedIndex);
         })
      );
      this.selectionRightButton = (ButtonWidget)this.method_37063(
         new ButtonWidget(this.field_22789 / 2 + 103, this.field_22790 / 2 + 65, 20, 20, class_2561.method_43470(">>"), b -> {
            if (this.selectedIndex == this.keys.size() - 1) {
               this.selectedIndex = 0;
            } else {
               this.selectedIndex++;
            }

            this.setVillagerData(this.selectedIndex);
         })
      );
      this.villagerNameButton = (ButtonWidget)this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 100, this.field_22790 / 2 + 65, 200, 20, class_2561.method_43470(""), b -> {})
      );
      this.callButton = (ButtonWidget)this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 100, this.field_22790 / 2 + 90, 60, 20, class_2561.method_43471("gui.button.call"), b -> {
            NetworkHandler.sendToServer(new CallToPlayerMessage(UUID.fromString(this.keys.get(this.selectedIndex))));
            Objects.requireNonNull(this.field_22787).method_1507(null);
         })
      );
      this.method_37063(
         new ButtonWidget(
            this.field_22789 / 2 + 40,
            this.field_22790 / 2 + 90,
            60,
            20,
            class_2561.method_43471("gui.button.exit"),
            b -> Objects.requireNonNull(this.field_22787).method_1507(null)
         )
      );
      this.toggleButtons(false);
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25394(class_332 context, int sizeX, int sizeY, float offset) {
      this.method_25420(context);
      context.method_27534(this.field_22793, class_2561.method_43471("gui.whistle.title"), this.field_22789 / 2, this.field_22790 / 2 - 100, 16777215);
      if (this.loadingAnimationTicks != -1) {
         String loadingMsg = new String(new char[this.loadingAnimationTicks / 5 % 4]).replace("\u0000", ".");
         context.method_27535(
            this.field_22793,
            class_2561.method_43471("gui.loading").method_10852(class_2561.method_43470(loadingMsg)),
            this.field_22789 / 2 - 20,
            this.field_22790 / 2 - 10,
            16777215
         );
      } else if (this.keys.size() == 0) {
         context.method_27534(this.field_22793, class_2561.method_43471("gui.whistle.noFamily"), this.field_22789 / 2, this.field_22790 / 2 + 50, 16777215);
      } else {
         context.method_25300(this.field_22793, this.selectedIndex + 1 + " / " + this.keys.size(), this.field_22789 / 2, this.field_22790 / 2 + 50, 16777215);
      }

      this.drawDummy(context);
      super.method_25394(context, sizeX, sizeY, offset);
   }

   private void drawDummy(class_332 context) {
      int posX = this.field_22789 / 2;
      int posY = this.field_22790 / 2 + 45;
      if (this.dummy != null) {
         class_490.method_2486(context, posX, posY, 60, 0.0F, 0.0F, this.dummy);
      }
   }

   public void setVillagerData(@NotNull class_2487 data) {
      this.villagerData = data;
      this.keys = new ArrayList<>(data.method_10541());
      this.loadingAnimationTicks = -1;
      this.selectedIndex = 0;
      this.setVillagerData(0);
   }

   private void setVillagerData(int index) {
      if (this.keys.size() > 0) {
         class_2487 firstData = this.villagerData.method_10562(this.keys.get(index));
         this.dummy = (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(class_310.method_1551().field_1687);
         this.dummy.method_5749(firstData);
         this.villagerNameButton.method_25355(this.dummy.method_5476());
         this.toggleButtons(true);
      } else {
         this.toggleButtons(false);
      }
   }

   private void toggleButtons(boolean enabled) {
      this.selectionLeftButton.field_22763 = enabled;
      this.selectionRightButton.field_22763 = enabled;
      this.callButton.field_22763 = enabled;
   }
}
