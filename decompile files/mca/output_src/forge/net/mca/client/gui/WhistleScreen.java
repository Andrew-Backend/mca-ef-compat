package forge.net.mca.client.gui;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.network.c2s.CallToPlayerMessage;
import forge.net.mca.network.c2s.GetFamilyRequest;
import forge.net.mca.util.compat.ButtonWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class WhistleScreen extends Screen {
   private List<String> keys = new ArrayList<>();
   private CompoundTag villagerData = new CompoundTag();
   private VillagerEntityMCA dummy;
   private ButtonWidget selectionLeftButton;
   private ButtonWidget selectionRightButton;
   private ButtonWidget villagerNameButton;
   private ButtonWidget callButton;
   private int loadingAnimationTicks;
   private int selectedIndex;

   public WhistleScreen() {
      super(Component.m_237115_("gui.whistle.title"));
   }

   public void m_86600_() {
      super.m_86600_();
      if (this.loadingAnimationTicks != -1) {
         this.loadingAnimationTicks++;
      }

      if (this.loadingAnimationTicks >= 20) {
         this.loadingAnimationTicks = 0;
      }
   }

   public void m_7856_() {
      NetworkHandler.sendToServer(new GetFamilyRequest());
      this.selectionLeftButton = (ButtonWidget)this.m_142416_(
         new ButtonWidget(this.f_96543_ / 2 - 123, this.f_96544_ / 2 + 65, 20, 20, Component.m_237113_("<<"), b -> {
            if (this.selectedIndex == 0) {
               this.selectedIndex = this.keys.size() - 1;
            } else {
               this.selectedIndex--;
            }

            this.setVillagerData(this.selectedIndex);
         })
      );
      this.selectionRightButton = (ButtonWidget)this.m_142416_(
         new ButtonWidget(this.f_96543_ / 2 + 103, this.f_96544_ / 2 + 65, 20, 20, Component.m_237113_(">>"), b -> {
            if (this.selectedIndex == this.keys.size() - 1) {
               this.selectedIndex = 0;
            } else {
               this.selectedIndex++;
            }

            this.setVillagerData(this.selectedIndex);
         })
      );
      this.villagerNameButton = (ButtonWidget)this.m_142416_(
         new ButtonWidget(this.f_96543_ / 2 - 100, this.f_96544_ / 2 + 65, 200, 20, Component.m_237113_(""), b -> {})
      );
      this.callButton = (ButtonWidget)this.m_142416_(
         new ButtonWidget(this.f_96543_ / 2 - 100, this.f_96544_ / 2 + 90, 60, 20, Component.m_237115_("gui.button.call"), b -> {
            NetworkHandler.sendToServer(new CallToPlayerMessage(UUID.fromString(this.keys.get(this.selectedIndex))));
            Objects.requireNonNull(this.f_96541_).m_91152_(null);
         })
      );
      this.m_142416_(
         new ButtonWidget(
            this.f_96543_ / 2 + 40,
            this.f_96544_ / 2 + 90,
            60,
            20,
            Component.m_237115_("gui.button.exit"),
            b -> Objects.requireNonNull(this.f_96541_).m_91152_(null)
         )
      );
      this.toggleButtons(false);
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_88315_(GuiGraphics context, int sizeX, int sizeY, float offset) {
      this.m_280273_(context);
      context.m_280653_(this.f_96547_, Component.m_237115_("gui.whistle.title"), this.f_96543_ / 2, this.f_96544_ / 2 - 100, 16777215);
      if (this.loadingAnimationTicks != -1) {
         String loadingMsg = new String(new char[this.loadingAnimationTicks / 5 % 4]).replace("\u0000", ".");
         context.m_280430_(
            this.f_96547_,
            Component.m_237115_("gui.loading").m_7220_(Component.m_237113_(loadingMsg)),
            this.f_96543_ / 2 - 20,
            this.f_96544_ / 2 - 10,
            16777215
         );
      } else if (this.keys.size() == 0) {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.whistle.noFamily"), this.f_96543_ / 2, this.f_96544_ / 2 + 50, 16777215);
      } else {
         context.m_280137_(this.f_96547_, this.selectedIndex + 1 + " / " + this.keys.size(), this.f_96543_ / 2, this.f_96544_ / 2 + 50, 16777215);
      }

      this.drawDummy(context);
      super.m_88315_(context, sizeX, sizeY, offset);
   }

   private void drawDummy(GuiGraphics context) {
      int posX = this.f_96543_ / 2;
      int posY = this.f_96544_ / 2 + 45;
      if (this.dummy != null) {
         InventoryScreen.m_274545_(context, posX, posY, 60, 0.0F, 0.0F, this.dummy);
      }
   }

   public void setVillagerData(@NotNull CompoundTag data) {
      this.villagerData = data;
      this.keys = new ArrayList<>(data.m_128431_());
      this.loadingAnimationTicks = -1;
      this.selectedIndex = 0;
      this.setVillagerData(0);
   }

   private void setVillagerData(int index) {
      if (this.keys.size() > 0) {
         CompoundTag firstData = this.villagerData.m_128469_(this.keys.get(index));
         this.dummy = (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(Minecraft.m_91087_().f_91073_);
         this.dummy.m_7378_(firstData);
         this.villagerNameButton.m_93666_(this.dummy.m_5446_());
         this.toggleButtons(true);
      } else {
         this.toggleButtons(false);
      }
   }

   private void toggleButtons(boolean enabled) {
      this.selectionLeftButton.f_93623_ = enabled;
      this.selectionRightButton.f_93623_ = enabled;
      this.callButton.f_93623_ = enabled;
   }
}
