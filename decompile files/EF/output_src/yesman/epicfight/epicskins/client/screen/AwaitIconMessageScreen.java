package yesman.epicfight.epicskins.client.screen;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;

@OnlyIn(Dist.CLIENT)
public class AwaitIconMessageScreen extends MessageScreen<Object> {
   int tickCount;

   public AwaitIconMessageScreen(String title, String message, Screen parentScreen, int width, int height) {
      super(title, message, parentScreen, null, width, height);
   }

   @Override
   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 100.0F);
      guiGraphics.m_280168_().m_252880_(this.f_96543_ / 2, this.f_96544_ / 2 + 8, 100.0F);

      for (int i = 0; i < 12; i++) {
         int mod = (i * 21 + this.tickCount) % 256;
         int color = mod << 16 | mod << 8 | mod << 0 | -33554432;
         guiGraphics.m_280168_().m_85836_();
         guiGraphics.m_280168_().m_252781_(Axis.f_252403_.m_252977_(-30.0F * i));
         guiGraphics.m_280168_().m_252880_(-1.0F, 0.0F, 0.0F);
         guiGraphics.m_280509_(0, 11, 2, 18, color);
         guiGraphics.m_280168_().m_85849_();
      }

      guiGraphics.m_280168_().m_85849_();
      this.tickCount += 3;
   }
}
