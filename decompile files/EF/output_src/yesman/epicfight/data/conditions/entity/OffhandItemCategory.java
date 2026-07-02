package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class OffhandItemCategory extends Condition.EntityPatchCondition {
   private WeaponCategory category;

   public OffhandItemCategory read(CompoundTag tag) {
      this.category = this.assertExtendableEnumTag("category", WeaponCategory.ENUM_MANAGER, tag);
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("category", this.category.toString());
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      return target.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == this.category;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
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
         Component.m_237113_("category"),
         List.copyOf(WeaponCategory.ENUM_MANAGER.universalValues()),
         ParseUtil::snakeToSpacedCamel,
         null
      );
      return List.of(
         Condition.ParameterEditor.of(
            value -> StringTag.m_129297_(value.toString().toLowerCase(Locale.ROOT)),
            tag -> WeaponCategory.ENUM_MANAGER.get(ParseUtil.nullOrToString(tag, Tag::m_7916_)),
            comboBox
         )
      );
   }
}
