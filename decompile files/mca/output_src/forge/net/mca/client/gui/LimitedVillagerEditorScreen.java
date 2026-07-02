package forge.net.mca.client.gui;

import forge.net.mca.entity.VillagerLike;
import forge.net.mca.util.localization.FlowingText;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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
      return this.villagerData.m_128451_("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal();
   }

   @Override
   protected boolean shouldPrintPlayerHint() {
      return false;
   }

   @Override
   protected void setPage(String page) {
      this.page = page;
      if (page.equals("general")) {
         int y = this.f_96544_ / 2 - 40;
         this.drawName(this.f_96543_ / 2, y);
         y += 24;
         if (this.villagerUUID.equals(this.playerUUID)) {
            this.addModelSelectionWidgets(this.f_96543_ / 2, y);
         }
      }
   }

   @Override
   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.m_88315_(context, mouseX, mouseY, delta);
      int y = this.f_96544_ / 2 + 20;

      for (Component text : FlowingText.wrap(Component.m_237115_("gui.villager_editor.customization_hint"), 175)) {
         context.m_280653_(this.f_96547_, text, this.f_96543_ / 2 + 87, y, -1);
         y += 10;
      }
   }
}
