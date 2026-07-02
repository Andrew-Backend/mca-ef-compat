package quilt.net.mca.client.gui;

import java.util.UUID;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.util.localization.FlowingText;

public class LimitedVillagerEditorScreen extends VillagerEditorScreen {
   public LimitedVillagerEditorScreen(UUID villagerUUID, UUID playerUUID) {
      super(villagerUUID, playerUUID);
   }

   @Override
   protected boolean shouldShowPageSelection() {
      return false;
   }

   @Override
   protected boolean shouldUsePlayerModel() {
      return this.villagerData.method_10550("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal();
   }

   @Override
   protected boolean shouldPrintPlayerHint() {
      return false;
   }

   @Override
   protected void setPage(String page) {
      this.page = page;
      if (page.equals("general")) {
         int y = this.field_22790 / 2 - 40;
         this.drawName(this.field_22789 / 2, y);
         y += 24;
         if (this.villagerUUID.equals(this.playerUUID)) {
            this.addModelSelectionWidgets(this.field_22789 / 2, y);
         }
      }
   }

   @Override
   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      int y = this.field_22790 / 2 + 20;

      for (class_2561 text : FlowingText.wrap(class_2561.method_43471("gui.villager_editor.customization_hint"), 175)) {
         context.method_27534(this.field_22793, text, this.field_22789 / 2 + 87, y, -1);
         y += 10;
      }
   }
}
