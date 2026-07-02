package yesman.epicfight.compat.kubejs.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

public class CustomPassiveSkill extends CustomSkill {
   public CustomPassiveSkill(CustomSkill.CustomSkillBuilder builder) {
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

   @Info(
      "Creates a custom passive skill.\nThis builder type is basically just a preset for a passive skill.\nIdeally, you should not override `drawOnGui()`, as that is pre-set for passive skills. Otherwise, it wouldn't be necessary to use this builder type.\n"
   )
   public static class CustomPassiveSkillBuilder extends CustomSkill.CustomSkillBuilder {
      public CustomPassiveSkillBuilder(ResourceLocation id) {
         super(id);
      }

      @Override
      public Skill createObject() {
         return new CustomPassiveSkill(this);
      }
   }
}
