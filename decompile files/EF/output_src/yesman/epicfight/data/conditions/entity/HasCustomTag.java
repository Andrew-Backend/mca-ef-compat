package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;

public class HasCustomTag extends Condition.EntityCondition {
   private final Set<String> allowedTags;

   public HasCustomTag(ListTag allowedTags) {
      this.allowedTags = allowedTags.stream().<String>map(Tag::m_7916_).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public Condition<Entity> read(CompoundTag tag) {
      return null;
   }

   @Override
   public CompoundTag serializePredicate() {
      return null;
   }

   public boolean predicate(Entity target) {
      for (String tag : this.allowedTags) {
         if (target.m_19880_().contains(tag)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("tag"), null, null);
      return List.of(Condition.ParameterEditor.of(value -> StringTag.m_129297_(value.toString()), tag -> ParseUtil.nullOrToString(tag, Tag::m_7916_), editbox));
   }
}
