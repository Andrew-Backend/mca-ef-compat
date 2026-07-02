package yesman.epicfight.data.conditions.entity;

import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class HealthPoint extends Condition.EntityPatchCondition {
   private float health;
   private HealthPoint.Comparator comparator;

   public HealthPoint() {
      this.health = 0.0F;
   }

   public HealthPoint(float health, HealthPoint.Comparator comparator) {
      this.health = health;
      this.comparator = comparator;
   }

   public HealthPoint read(CompoundTag tag) {
      this.health = this.<Float>assertTag("health", "decimal", tag, NumericTag.class, CompoundTag::m_128457_);
      this.comparator = this.assertEnumTag("comparator", HealthPoint.Comparator.class, tag);
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("comparator", this.comparator.toString().toLowerCase(Locale.ROOT));
      tag.m_128350_("health", this.health);
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      switch (this.comparator) {
         case LESS_ABSOLUTE:
            return this.health > target.getOriginal().m_21223_();
         case GREATER_ABSOLUTE:
            return this.health < target.getOriginal().m_21223_();
         case LESS_RATIO:
            return this.health > target.getOriginal().m_21223_() / target.getOriginal().m_21233_();
         case GREATER_RATIO:
            return this.health < target.getOriginal().m_21223_() / target.getOriginal().m_21233_();
         default:
            return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("health"), null, null);
      AbstractWidget comboBox = new ComboBox<>(
         screen,
         screen.getMinecraft().f_91062_,
         0,
         0,
         0,
         0,
         null,
         null,
         4,
         Component.m_237113_("comparator"),
         List.of(HealthPoint.Comparator.values()),
         ParseUtil::snakeToSpacedCamel,
         null
      );
      editbox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
      return List.of(
         Condition.ParameterEditor.of(
            value -> ParseUtil.parseOrGet(value.toString(), v -> FloatTag.m_128566_(Float.parseFloat(value.toString())), StringTag.m_129297_("")),
            tag -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::m_7916_)),
            editbox
         ),
         Condition.ParameterEditor.of(
            value -> StringTag.m_129297_(value.toString().toLowerCase(Locale.ROOT)),
            tag -> ParseUtil.enumValueOfOrNull(HealthPoint.Comparator.class, ParseUtil.nullOrToString(tag, Tag::m_7916_)),
            comboBox
         )
      );
   }

   public enum Comparator {
      GREATER_ABSOLUTE,
      LESS_ABSOLUTE,
      GREATER_RATIO,
      LESS_RATIO;
   }
}
