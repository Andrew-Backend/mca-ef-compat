package yesman.epicfight.client.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.main.EpicFightMod;

public class VersionNotifier {
   private final Minecraft minecraft;
   private final boolean visible;
   private int count = 0;

   public VersionNotifier(Minecraft minecraft) {
      this.minecraft = minecraft;
      this.visible = ModList.get().getModFileById("epicfight").versionString().matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+");
   }

   public void init() {
      this.count = 0;
   }

   public void render(GuiGraphics guiGraphics, boolean inWorld) {
      if (this.visible) {
         if (this.count <= 600 || !inWorld) {
            this.count++;
            Window sr = this.minecraft.m_91268_();
            int width = sr.m_85445_();
            if (inWorld) {
               String l1 = Component.m_237115_(EpicFightMod.format("%s.messages.test_version_warning_line1")).getString();
               String l2 = Component.m_237110_(
                     EpicFightMod.format("%s.messages.test_version_warning_line2"), new Object[]{ModList.get().getModFileById("epicfight").versionString()}
                  )
                  .getString();
               guiGraphics.m_280488_(this.minecraft.f_91062_, l1, width - this.minecraft.f_91062_.m_92895_(l1) - 2, 8, 16777215);
               guiGraphics.m_280488_(this.minecraft.f_91062_, l2, width - this.minecraft.f_91062_.m_92895_(l2) - 2, 20, 16777215);
            } else {
               String l1 = Component.m_237110_(
                     EpicFightMod.format("%s.messages.version_notifier"), new Object[]{ModList.get().getModFileById("epicfight").versionString()}
                  )
                  .getString();
               guiGraphics.m_280488_(this.minecraft.f_91062_, l1, width - this.minecraft.f_91062_.m_92895_(l1) - 2, 8, 16777215);
            }
         }
      }
   }
}
