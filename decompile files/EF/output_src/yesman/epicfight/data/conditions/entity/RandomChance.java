package yesman.epicfight.data.conditions.entity;

import io.netty.util.internal.StringUtil;
import java.util.List;
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
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RandomChance extends Condition.EntityPatchCondition {
   private float chance;

   public RandomChance() {
      this.chance = 0.0F;
   }

   public RandomChance(float chance) {
      this.chance = chance;
   }

   public RandomChance read(CompoundTag tag) {
      this.chance = this.<Float>assertTag("chance", "decimal", tag, NumericTag.class, CompoundTag::m_128457_);
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128350_("chance", this.chance);
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      return target.getOriginal().m_217043_().m_188501_() < this.chance;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("chance"), null, null);
      editbox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
      return List.of(
         Condition.ParameterEditor.of(
            value -> ParseUtil.parseOrGet(value.toString(), v -> FloatTag.m_128566_(Float.parseFloat(value.toString())), StringTag.m_129297_("")),
            tag -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::m_7916_)),
            editbox
         )
      );
   }
}
