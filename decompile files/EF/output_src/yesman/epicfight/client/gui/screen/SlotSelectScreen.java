package yesman.epicfight.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;

public class SlotSelectScreen extends Screen {
   private static final int MAX_ROWS = 2;
   private static final int MAX_COLUMNS = 3;
   private static final int STRIDE = 60;
   private final SkillBookScreen parent;
   private final List<SkillContainer> containers;
   private final List<SlotSelectScreen.SlotButton> slotButtons = new ArrayList<>();
   private int maxScroll;
   private int scroll = 0;

   public SlotSelectScreen(Set<SkillContainer> containers, SkillBookScreen parent) {
      super(Component.m_237119_());
      this.parent = parent;
      this.containers = containers.stream().sorted((c1, c2) -> {
         if (c1.getSlotId() > c2.getSlotId()) {
            return 1;
         } else {
            return c1.getSlotId() < c2.getSlotId() ? -1 : 0;
         }
      }).toList();
   }

   protected void m_7856_() {
      this.parent.m_6575_(this.f_96541_, this.f_96543_, this.f_96544_);
      int buttonX = this.f_96543_ / 2 - 84;
      int buttonY = this.f_96544_ / 2 - 40;
      int columns = 0;
      this.maxScroll = Math.max(this.containers.size() / 3 + 1 - 2, 0);
      if (this.maxScroll > 0) {
         this.m_142416_(new SlotSelectScreen.ScrollArrow(this.f_96543_ / 2 - 8, this.f_96544_ / 2 - 98, 16, 16, button -> this.scrollUp(), true));
         this.m_142416_(new SlotSelectScreen.ScrollArrow(this.f_96543_ / 2 - 8, this.f_96544_ / 2 + 82, 16, 16, button -> this.scrollDown(), false));
      }

      this.slotButtons.clear();

      for (SkillContainer container : this.containers) {
         String slotName = container.getSlot().toString().toLowerCase(Locale.ROOT);
         String skillName = container.getSkill() == null ? "Empty" : Component.m_237115_(container.getSkill().getTranslationKey()).getString();
         SlotSelectScreen.SlotButton slotbutton = new SlotSelectScreen.SlotButton(
            buttonX,
            buttonY,
            48,
            48,
            Component.m_237113_(slotName + ": " + skillName),
            container.getSkill() == null ? SkillEditScreen.EMPTY_SKILL_SLOT_ICON : container.getSkill().getSkillTexture(),
            button -> {
               this.parent.acquireSkillTo(container);
               if (this.f_96541_.f_91080_ == this) {
                  this.m_7379_();
               }
            }
         );
         if (!this.parent.consumesItem() && container.onReplaceCooldown()) {
            slotbutton.f_93623_ = false;
            slotbutton.m_257544_(
               Tooltip.m_257550_(
                  Component.m_237113_(slotName + ": " + skillName + "\n")
                     .m_7220_(Component.m_237110_(EpicFightMod.format("gui.%s.container_on_cooldown"), new Object[]{container.getReplaceCooldown() / 20}))
               )
            );
         }

         buttonX += 60;
         if (++columns >= 3) {
            buttonX = this.f_96543_ / 2 - 84;
            buttonY += 60;
            columns = 0;
         }

         this.slotButtons.add(slotbutton);
         this.m_142416_(slotbutton);
      }

      this.scroll = 0;
      this.setScrollVisibilities();
   }

   protected void scrollUp() {
      int nextScroll = Mth.m_14045_(this.scroll - 1, 0, this.maxScroll);
      if (this.scroll != nextScroll) {
         this.scroll = nextScroll;
         this.slotButtons.forEach(button -> button.m_253211_(button.m_252907_() + 60));
         this.setScrollVisibilities();
      }
   }

   protected void scrollDown() {
      int nextScroll = Mth.m_14045_(this.scroll + 1, 0, this.maxScroll);
      if (this.scroll != nextScroll) {
         this.scroll = nextScroll;
         this.slotButtons.forEach(button -> button.m_253211_(button.m_252907_() - 60));
         this.setScrollVisibilities();
      }
   }

   protected void setScrollVisibilities() {
      int i = 0;

      for (SlotSelectScreen.SlotButton slotButton : this.slotButtons) {
         if (i / 3 >= this.scroll && i / 3 < this.scroll + 2) {
            slotButton.f_93624_ = true;
         } else {
            slotButton.f_93624_ = false;
         }

         i++;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      if (delta > 0.0) {
         this.scrollUp();
      } else {
         this.scrollDown();
      }

      return true;
   }

   public void m_7379_() {
      if (this.parent != null) {
         this.f_96541_.m_91152_(this.parent);
      } else {
         super.m_7379_();
      }
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      int boxWidth = 192;
      int boxHeight = 160;
      int left = (this.f_96543_ - boxWidth) / 2;
      int top = (this.f_96544_ - boxHeight) / 2;
      this.parent.render(guiGraphics, mouseX, mouseY, partialTick, true);
      guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 5000.0F);
      guiGraphics.m_280509_(left, top, left + boxWidth, top + boxHeight, -6250336);
      guiGraphics.m_280509_(left + 1, top + 1, left + boxWidth - 1, top + boxHeight - 1, -16777215);
      Component component = Component.m_237115_(EpicFightMod.format("gui.%s.select_slot_tooltip"));
      int lineHeight = 0;

      for (FormattedCharSequence s : this.f_96547_.m_92923_(component, 250)) {
         guiGraphics.m_280649_(this.f_96547_, s, (this.f_96543_ - boxWidth) / 2 + 8, this.f_96544_ / 2 - 70 + lineHeight, -1513240, false);
         lineHeight += 10;
      }

      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
   }

   class ScrollArrow extends Button {
      final boolean up;

      protected ScrollArrow(int x, int y, int width, int height, OnPress onPress, boolean up) {
         super(x, y, width, height, Component.m_237119_(), onPress, Button.f_252438_);
         this.up = up;
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         if (this.up && SlotSelectScreen.this.scroll != 0) {
            guiGraphics.m_280411_(SkillEditScreen.SCROLL_ARROW_UP, this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_, 0.0F, 0.0F, 16, 16, 16, 16);
         } else if (!this.up && SlotSelectScreen.this.scroll != SlotSelectScreen.this.maxScroll) {
            guiGraphics.m_280411_(
               SkillEditScreen.SCROLL_ARROW_DOWN, this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_, 0.0F, 0.0F, 16, 16, 16, 16
            );
         }
      }

      protected boolean m_93680_(double mouseX, double mouseY) {
         return super.m_93680_(mouseX, mouseY)
            && (this.up && SlotSelectScreen.this.scroll != 0 || !this.up && SlotSelectScreen.this.scroll != SlotSelectScreen.this.maxScroll);
      }
   }

   class SlotButton extends Button {
      final ResourceLocation texture;

      protected SlotButton(int x, int y, int width, int height, Component tooltip, ResourceLocation textureLocation, OnPress onPress) {
         super(x, y, width, height, Component.m_237119_(), onPress, Button.f_252438_);
         this.texture = textureLocation;
         this.m_257544_(Tooltip.m_257550_(tooltip));
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         super.m_87963_(guiGraphics, mouseX, mouseY, partialTick);
         RenderSystem.enableBlend();
         if (!this.f_93623_) {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
         }

         guiGraphics.m_280411_(this.texture, this.m_252754_() + 2, this.m_252907_() + 2, this.f_93618_ - 4, this.f_93619_ - 4, 0.0F, 0.0F, 32, 32, 32, 32);
         if (!this.f_93623_) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         RenderSystem.disableBlend();
      }
   }
}
