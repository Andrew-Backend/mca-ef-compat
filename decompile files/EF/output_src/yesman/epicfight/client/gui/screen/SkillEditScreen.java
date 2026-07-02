package yesman.epicfight.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class SkillEditScreen extends Screen {
   public static final ResourceLocation EMPTY_SKILL_SLOT_ICON = EpicFightMod.identifier("textures/gui/empty.png");
   public static final ResourceLocation SCROLL_ARROW_UP = EpicFightMod.identifier("textures/gui/scroll_arrow_up.png");
   public static final ResourceLocation SCROLL_ARROW_DOWN = EpicFightMod.identifier("textures/gui/scroll_arrow_down.png");
   private static final ResourceLocation SKILL_EDIT_UI = EpicFightMod.identifier("textures/gui/screen/skill_edit.png");
   private static final MutableComponent NO_SKILLS = Component.m_237115_(EpicFightMod.format("gui.%s.no_skills"));
   private static final int MAX_SKILL_OPTIONS_ROWS = 6;
   private static final int MAX_SLOT_ROWS = 9;
   private static final int STRIDE = 18;
   private final Player player;
   private final CapabilitySkill skills;
   private final Map<SkillSlot, SkillEditScreen.SlotButton> slotButtons = new LinkedHashMap<>();
   private final List<SkillEditScreen.EquipSkillButton> equipSkillButtons = new ArrayList<>();
   private SkillEditScreen.ScrollArrow up;
   private SkillEditScreen.ScrollArrow down;
   private SkillEditScreen.SlotButton selectedSlotButton;
   private int start;
   private int maxScroll;
   private int scroll = 0;

   public SkillEditScreen(Player player, CapabilitySkill skills) {
      super(Component.m_237115_(EpicFightMod.format("gui.%s.skill_edit")));
      this.player = player;
      this.skills = skills;
   }

   public void m_7856_() {
      this.slotButtons.clear();
      this.equipSkillButtons.clear();
      this.up = null;
      this.down = null;
      this.maxScroll = Math.max(
         SkillSlot.ENUM_MANAGER.universalValues().stream().filter(skillSlotx -> skillSlotx.category().learnable()).toList().size() - 9, 0
      );
      if (this.maxScroll > 0) {
         this.up = new SkillEditScreen.ScrollArrow(this.f_96543_ / 2 - 95, this.f_96544_ / 2 - 114, 16, 16, button -> this.scrollUp(), true);
         this.down = new SkillEditScreen.ScrollArrow(this.f_96543_ / 2 - 95, this.f_96544_ / 2 + 98, 16, 16, button -> this.scrollDown(), false);
         this.m_142416_(this.up);
         this.m_142416_(this.down);
      }

      int left = this.f_96543_ / 2 - 96;
      int top = this.f_96544_ / 2 - 82;

      for (SkillSlot skillSlot : SkillSlot.ENUM_MANAGER.universalValues()) {
         if ((this.player.m_7500_() || !this.skills.getSkillContainersFor(skillSlot.category()).isEmpty()) && skillSlot.category().learnable()) {
            SkillContainer skillContainer = this.skills.getSkillContainerFor(skillSlot);
            SkillEditScreen.SlotButton slotButton = new SkillEditScreen.SlotButton(
               left,
               top,
               skillContainer,
               button -> {
                  this.start = 0;

                  for (Button shownButton : this.equipSkillButtons) {
                     this.m_6702_().remove(shownButton);
                  }

                  this.equipSkillButtons.clear();
                  int k = this.f_96543_ / 2 - 69;
                  MutableInt widgetHeight = new MutableInt(this.f_96544_ / 2 - 78);
                  Stream<Skill> learnedSkill = this.player.m_7500_()
                     ? SkillManager.getSkills(skill -> skill.getCategory() == skillSlot.category()).stream()
                     : this.skills.listAcquiredSkills().filter(skill -> skill.getCategory() == skillSlot.category());
                  learnedSkill.forEach(
                     skill -> {
                        this.equipSkillButtons
                           .add(
                              new SkillEditScreen.EquipSkillButton(
                                    k, widgetHeight.intValue(), 147, 24, skill, Component.m_237115_(skill.getTranslationKey()), replaceSkillButton -> {
                                       if (this.isButtonVisible(replaceSkillButton)) {
                                          skillContainer.setSkill(skill);
                                          EpicFightNetworkManager.sendToServer(new CPChangeSkill(skillSlot, -1, skill));
                                          this.skills.addLearnedSkill(skill);
                                          this.m_7379_();
                                       }
                                    }
                                 )
                                 .setActive(this.skills.getSkillContainer(skill) == null)
                           );
                        widgetHeight.add(26);
                     }
                  );

                  for (Button shownButton : this.equipSkillButtons) {
                     this.m_142416_(shownButton);
                  }

                  this.selectedSlotButton = (SkillEditScreen.SlotButton)button;
               },
               Component.m_237115_(SkillSlot.ENUM_MANAGER.toTranslated(skillSlot))
            );
            this.slotButtons.put(skillSlot, slotButton);
            this.m_142416_(slotButton);
            top += 18;
         }

         this.scroll = 0;
         this.setScrollVisibilities();
      }

      if (this.selectedSlotButton != null) {
         this.selectedSlotButton = this.slotButtons.get(this.selectedSlotButton.skillContainer.getSlot());
         this.selectedSlotButton.m_5691_();
      }
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(guiGraphics);
      if (this.canScroll()) {
         int scrollPosition = (int)(140.0F * ((float)this.start / (this.equipSkillButtons.size() - 6)));
         guiGraphics.m_280411_(SKILL_EDIT_UI, this.f_96543_ / 2 + 80, this.f_96544_ / 2 - 80 + scrollPosition, 12, 15, 231.0F, 2.0F, 12, 15, 256, 256);
      }

      int maxShowingButtons = Math.min(this.equipSkillButtons.size(), 6);

      for (int i = this.start; i < maxShowingButtons + this.start; i++) {
         this.equipSkillButtons.get(i).m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      }

      for (SkillEditScreen.SlotButton sb : this.slotButtons.values()) {
         sb.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      }

      if (this.up != null) {
         this.up.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      }

      if (this.down != null) {
         this.down.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      }

      if (this.slotButtons.isEmpty()) {
         int lineHeight = 0;

         for (FormattedCharSequence s : this.f_96547_.m_92923_(NO_SKILLS, 140)) {
            guiGraphics.m_280649_(this.f_96547_, s, this.f_96543_ / 2 - 65, this.f_96544_ / 2 - 72 + lineHeight, 3158064, false);
            lineHeight += 10;
         }
      }
   }

   public void m_280273_(GuiGraphics guiGraphics) {
      super.m_280273_(guiGraphics);
      guiGraphics.m_280218_(SKILL_EDIT_UI, this.f_96543_ / 2 - 104, this.f_96544_ / 2 - 100, 0, 0, 208, 200);
   }

   private boolean canScroll() {
      return this.equipSkillButtons.size() > 6;
   }

   private boolean isButtonVisible(Button button) {
      int buttonOrder = this.equipSkillButtons.indexOf(button);
      return buttonOrder >= this.start && buttonOrder <= this.start + 6;
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int left = this.f_96543_ / 2 - 96;
      int top = this.f_96544_ / 2 - 82;
      if (left <= mouseX && top <= mouseY && left + 18 >= mouseX && top + 162 >= mouseY) {
         if (delta > 0.0) {
            this.scrollUp();
         } else {
            this.scrollDown();
         }

         return true;
      } else {
         if (!this.canScroll()) {
            return false;
         }

         if (delta > 0.0) {
            if (this.start > 0) {
               this.start--;

               for (Button button : this.equipSkillButtons) {
                  button.m_253211_(button.m_252907_() + 26);
               }

               return true;
            }
         } else if (this.start < this.equipSkillButtons.size() - 6) {
            this.start++;

            for (Button button : this.equipSkillButtons) {
               button.m_253211_(button.m_252907_() - 26);
            }

            return true;
         }

         return false;
      }
   }

   public boolean m_7043_() {
      return false;
   }

   protected void scrollUp() {
      int nextScroll = Mth.m_14045_(this.scroll - 1, 0, this.maxScroll);
      if (this.scroll != nextScroll) {
         this.scroll = nextScroll;
         this.slotButtons.values().forEach(button -> button.m_253211_(button.m_252907_() + 18));
         this.setScrollVisibilities();
      }
   }

   protected void scrollDown() {
      int nextScroll = Mth.m_14045_(this.scroll + 1, 0, this.maxScroll);
      if (this.scroll != nextScroll) {
         this.scroll = nextScroll;
         this.slotButtons.values().forEach(button -> button.m_253211_(button.m_252907_() - 18));
         this.setScrollVisibilities();
      }
   }

   protected void setScrollVisibilities() {
      int i = 0;

      for (SkillEditScreen.SlotButton slotButton : this.slotButtons.values()) {
         if (i >= this.scroll && i < this.scroll + 9) {
            slotButton.f_93624_ = true;
         } else {
            slotButton.f_93624_ = false;
         }

         i++;
      }
   }

   public class EquipSkillButton extends Button {
      private static final int SPACING = 26;
      private final Skill skill;

      public EquipSkillButton(int x, int y, int width, int height, Skill skill, Component title, OnPress pressedAction) {
         super(x, y, width, height, title, pressedAction, Button.f_252438_);
         this.skill = skill;
      }

      public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         this.f_93622_ = mouseX >= this.m_252754_()
            && mouseY >= this.m_252907_()
            && mouseX < this.m_252754_() + this.f_93618_
            && mouseY < this.m_252907_() + this.f_93619_;
         int texY = !this.m_198029_() && this.f_93623_ ? 200 : 224;
         guiGraphics.m_280218_(SkillEditScreen.SKILL_EDIT_UI, this.m_252754_(), this.m_252907_(), 0, texY, this.f_93618_, this.f_93619_);
         RenderSystem.enableBlend();
         guiGraphics.m_280411_(this.skill.getSkillTexture(), this.m_252754_() + 5, this.m_252907_() + 4, 16, 16, 0.0F, 0.0F, 128, 128, 128, 128);
         guiGraphics.m_280614_(SkillEditScreen.this.f_96547_, this.m_6035_(), this.m_252754_() + 26, this.m_252907_() + 2, -1, false);
         if (!this.f_93623_) {
            guiGraphics.m_280614_(
               SkillEditScreen.this.f_96547_,
               Component.m_237113_(SkillEditScreen.this.skills.getSkillContainer(this.skill).getSlot().toString().toLowerCase(Locale.ROOT)),
               this.m_252754_() + 26,
               this.m_252907_() + 12,
               16736352,
               false
            );
         }
      }

      public boolean m_6375_(double x, double y, int pressType) {
         if (this.f_93624_ && pressType == 1) {
            boolean flag = this.clickedNoCountActive(x, y);
            if (flag) {
               this.openSkillInfoScreen();
               return true;
            }
         }

         return super.m_6375_(x, y, pressType);
      }

      public void openSkillInfoScreen() {
         this.m_7435_(Minecraft.m_91087_().m_91106_());
         SkillEditScreen.this.f_96541_.m_91152_(new SkillBookScreen(SkillEditScreen.this.player, this.skill, null, SkillEditScreen.this));
      }

      public void m_93692_(boolean focused) {
         super.m_93692_(focused);
         this.maybeScroll();
      }

      private void maybeScroll() {
         List<SkillEditScreen.EquipSkillButton> buttons = SkillEditScreen.this.equipSkillButtons;
         int start = SkillEditScreen.this.start;
         int maxRows = 6;
         int i = buttons.indexOf(this);
         boolean isOutsideVisibleRowsAtBottom = i >= start + 6;
         boolean isOutsideVisibleRowsAtTop = i < start;
         if (isOutsideVisibleRowsAtBottom || isOutsideVisibleRowsAtTop) {
            int nextStart = isOutsideVisibleRowsAtBottom ? Math.max(0, i - 6 + 1) : i;
            int diff = start - nextStart;

            for (Button button : buttons) {
               button.m_253211_(button.m_252907_() + 26 * diff);
            }

            SkillEditScreen.this.start = nextStart;
         }
      }

      protected boolean clickedNoCountActive(double x, double y) {
         return this.f_93624_ && x >= this.m_252754_() && y >= this.m_252907_() && x < this.m_252754_() + this.f_93618_ && y < this.m_252907_() + this.f_93619_;
      }

      public SkillEditScreen.EquipSkillButton setActive(boolean active) {
         this.f_93623_ = active;
         return this;
      }
   }

   class ScrollArrow extends Button {
      final boolean up;

      protected ScrollArrow(int x, int y, int width, int height, OnPress onPress, boolean up) {
         super(x, y, width, height, Component.m_237119_(), onPress, Button.f_252438_);
         this.up = up;
      }

      protected void m_87963_(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         if (this.up && SkillEditScreen.this.scroll != 0) {
            guiGraphics.m_280411_(SkillEditScreen.SCROLL_ARROW_UP, this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_, 0.0F, 0.0F, 16, 16, 16, 16);
         } else if (!this.up && SkillEditScreen.this.scroll != SkillEditScreen.this.maxScroll) {
            guiGraphics.m_280411_(
               SkillEditScreen.SCROLL_ARROW_DOWN, this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_, 0.0F, 0.0F, 16, 16, 16, 16
            );
         }
      }

      protected boolean m_93680_(double mouseX, double mouseY) {
         return super.m_93680_(mouseX, mouseY)
            && (this.up && SkillEditScreen.this.scroll != 0 || !this.up && SkillEditScreen.this.scroll != SkillEditScreen.this.maxScroll);
      }
   }

   class SlotButton extends Button {
      private static final int SIZE = 18;
      private final SkillContainer skillContainer;
      private final Component slotExplanation;

      public SlotButton(int x, int y, SkillContainer skillContainer, OnPress pressedAction, Component tooltipMessage) {
         super(x, y, 18, 18, Component.m_237119_(), pressedAction, Button.f_252438_);
         this.skillContainer = skillContainer;
         this.slotExplanation = tooltipMessage;
         this.m_257544_(Tooltip.m_257550_(this.slotExplanation));
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         this.f_93623_ = !this.skillContainer.onReplaceCooldown();
         int y = (this.m_198029_() || SkillEditScreen.this.selectedSlotButton == this) && !this.skillContainer.onReplaceCooldown() ? 35 : 17;
         guiGraphics.m_280218_(SkillEditScreen.SKILL_EDIT_UI, this.m_252754_(), this.m_252907_(), 237, y, this.f_93618_, this.f_93619_);
         if (!this.skillContainer.isEmpty()) {
            RenderSystem.enableBlend();
            guiGraphics.m_280411_(
               this.skillContainer.getSkill().getSkillTexture(),
               this.m_252754_() + 1,
               this.m_252907_() + 1,
               this.m_5711_() - 2,
               this.m_93694_() - 2,
               0.0F,
               0.0F,
               128,
               128,
               128,
               128
            );
            RenderSystem.disableBlend();
         } else {
            guiGraphics.m_280411_(
               SkillEditScreen.EMPTY_SKILL_SLOT_ICON,
               this.m_252754_() + 1,
               this.m_252907_() + 1,
               this.m_5711_() - 2,
               this.m_93694_() - 2,
               0.0F,
               0.0F,
               128,
               128,
               128,
               128
            );
         }

         if (this.skillContainer.onReplaceCooldown()) {
            int maxCooldown = EpicFightGameRules.SKILL_REPLACE_COOLDOWN.getRuleValue(SkillEditScreen.this.player.m_9236_());
            float lerp = Mth.m_144920_(0.0F, 16.0F, 1.0F - (float)this.skillContainer.getReplaceCooldown() / maxCooldown);
            guiGraphics.m_280509_(this.m_252754_() + 1, this.m_252907_() + 1 + (int)lerp, this.m_252754_() + 17, this.m_252907_() + 17, 2013265920);
            if (this.m_198029_()) {
               this.m_257544_(
                  Tooltip.m_257550_(
                     Component.m_237110_(EpicFightMod.format("gui.%s.container_on_cooldown"), new Object[]{this.skillContainer.getReplaceCooldown() / 20})
                  )
               );
            }
         } else {
            this.m_257544_(Tooltip.m_257550_(this.slotExplanation));
         }
      }

      public void m_93692_(boolean focused) {
         super.m_93692_(focused);
         this.maybeScroll();
      }

      private void maybeScroll() {
         if (SkillEditScreen.this.maxScroll != 0) {
            int scroll = SkillEditScreen.this.scroll;
            int index = SkillEditScreen.this.slotButtons.values().stream().toList().indexOf(this);
            int relativeIndex = index - scroll;
            boolean needsScrollDown = relativeIndex >= 8;
            boolean needsScrollTop = relativeIndex == 0;
            if (needsScrollDown || needsScrollTop) {
               if (needsScrollDown) {
                  SkillEditScreen.this.scrollDown();
               } else {
                  SkillEditScreen.this.scrollUp();
               }
            }
         }
      }
   }
}
