package forge.net.mca.client.gui;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.item.BabyItem;
import forge.net.mca.network.c2s.BabyNameRequest;
import forge.net.mca.network.c2s.BabyNamingVillagerMessage;
import forge.net.mca.util.compat.ButtonWidget;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NameBabyScreen extends Screen {
   private final ItemStack baby;
   private final Player player;
   private EditBox babyNameTextField;

   public NameBabyScreen(Player player, ItemStack baby) {
      super(Component.m_237115_("gui.nameBaby.title"));
      this.baby = baby;
      this.player = player;
   }

   public void m_86600_() {
      super.m_86600_();
      this.babyNameTextField.m_94120_();
   }

   public void m_7856_() {
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 40, this.f_96544_ / 2 + 20, 80, 20, Component.m_237115_("gui.button.done"), b -> {
         NetworkHandler.sendToServer(new BabyNamingVillagerMessage(this.player.m_150109_().f_35977_, this.babyNameTextField.m_94155_().trim()));
         Objects.requireNonNull(this.f_96541_).m_91152_(null);
      }));
      this.m_142416_(
         new ButtonWidget(
            this.f_96543_ / 2 + 105,
            this.f_96544_ / 2 - 20,
            60,
            20,
            Component.m_237115_("gui.button.random"),
            b -> NetworkHandler.sendToServer(new BabyNameRequest(((BabyItem)this.baby.m_41720_()).getGender()))
         )
      );
      this.babyNameTextField = new EditBox(
         this.f_96547_, this.f_96543_ / 2 - 100, this.f_96544_ / 2 - 20, 200, 20, Component.m_237115_("structure_block.structure_name")
      );
      this.babyNameTextField.m_94199_(32);
      this.m_264313_(this.babyNameTextField);
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_88315_(GuiGraphics context, int w, int h, float scale) {
      this.m_280273_(context);
      this.m_7522_(this.babyNameTextField);
      context.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 70, 16777215);
      this.babyNameTextField.m_88315_(context, this.f_96543_ / 2 - 100, this.f_96544_ / 2 - 20, scale);
      super.m_88315_(context, w, h, scale);
   }

   public void setBabyName(String name) {
      this.babyNameTextField.m_94144_(name);
   }
}
