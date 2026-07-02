package yesman.epicfight.data.conditions.entity;

import com.ibm.icu.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class PlayerSkillActivated extends Condition.EntityPatchCondition {
   private Skill skill;

   public PlayerSkillActivated read(CompoundTag tag) {
      String skillName = this.assertTag("skill", "string", tag, StringTag.class, CompoundTag::m_128461_);
      if ((this.skill = SkillManager.getSkill(skillName)) == null) {
         throw new NoSuchElementException(
            MessageFormat.format("{} condition error: Skill named {} does not exist", new Object[]{this.getClass().getSimpleName(), skillName})
         );
      } else {
         return this;
      }
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("skill", this.skill.getRegistryName().toString());
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      if (target instanceof PlayerPatch<?> playerpatch) {
         Optional<SkillContainer> skill = playerpatch.getSkillContainerFor(this.skill);
         return skill.isEmpty() ? false : skill.get().isActivated();
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      AbstractWidget popupBox = new PopupBox.RegistryPopupBox(
         screen, screen.getMinecraft().f_91062_, 0, 0, 0, 0, null, null, Component.m_237113_("skill"), SkillManager.getSkillRegistry(), null
      );
      return List.of(
         Condition.ParameterEditor.of(
            skill -> StringTag.m_129297_(skill.toString()), tag -> SkillManager.getSkill(ParseUtil.nullOrToString(tag, Tag::m_7916_)), popupBox
         )
      );
   }
}
