package yesman.epicfight.data.conditions.entity;

import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetInPov extends Condition.EntityPatchCondition {
   protected double min;
   protected double max;

   public TargetInPov() {
   }

   public TargetInPov(double min, double max) {
      this.min = min;
      this.max = max;
   }

   public TargetInPov read(CompoundTag tag) {
      this.min = this.<Double>assertTag("min", "decimal", tag, NumericTag.class, CompoundTag::m_128459_);
      this.max = this.<Double>assertTag("max", "decimal", tag, NumericTag.class, CompoundTag::m_128459_);
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128347_("min", this.min);
      tag.m_128347_("max", this.max);
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> entitypatch) {
      double degree = entitypatch.getAngleTo(entitypatch.getTarget());
      return this.min < degree && degree < this.max;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox minEditBox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("min"), null, null);
      ResizableEditBox maxEditBox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("max"), null, null);
      minEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
      maxEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
      Function<Object, Tag> doubleParser = value -> DoubleTag.m_128500_(Double.valueOf(value.toString()));
      Function<Tag, Object> doubleGetter = tag -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::m_7916_));
      return List.of(Condition.ParameterEditor.of(doubleParser, doubleGetter, minEditBox), Condition.ParameterEditor.of(doubleParser, doubleGetter, maxEditBox));
   }

   public static class TargetInPovHorizontal extends TargetInPov {
      public TargetInPovHorizontal() {
      }

      public TargetInPovHorizontal(double min, double max) {
         super(min, max);
      }

      @Override
      public boolean predicate(LivingEntityPatch<?> entitypatch) {
         double degree = entitypatch.getAngleToHorizontal(entitypatch.getTarget());
         return this.min < degree && degree < this.max;
      }
   }
}
