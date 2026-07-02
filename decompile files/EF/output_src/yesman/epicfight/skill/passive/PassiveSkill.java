package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;

public abstract class PassiveSkill extends Skill {
   public static SkillBuilder<PassiveSkill> createPassiveBuilder() {
      return new SkillBuilder().setCategory(SkillCategories.PASSIVE).setResource(Skill.Resource.NONE);
   }

   public PassiveSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
      guiGraphics.drawString(gui.getFont(), remainTime, x + 12.0F - 4 * remainTime.length(), y + 6.0F, 16777215, true);
      poseStack.m_85849_();
   }
}
