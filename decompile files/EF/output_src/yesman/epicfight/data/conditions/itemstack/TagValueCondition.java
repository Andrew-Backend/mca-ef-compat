package yesman.epicfight.data.conditions.itemstack;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;

public class TagValueCondition extends Condition.ItemStackCondition {
   private String key;
   private String value;

   public TagValueCondition read(CompoundTag tag) {
      this.key = tag.m_128461_("key");
      this.value = tag.m_128423_("value").m_7916_();
      if (this.key == null) {
         throw new IllegalArgumentException("No key provided!");
      } else if (this.value == null) {
         throw new IllegalArgumentException("No value provided!");
      } else {
         return this;
      }
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("key", this.key);
      tag.m_128359_("value", this.value);
      return tag;
   }

   public boolean predicate(ItemStack itemstack) {
      String[] keys = this.key.split("[.]");
      List<Tag> visitTags = List.of(itemstack.m_41783_());

      for (int i = 0; i < keys.length; i++) {
         visitTags = visitTags(keys[i], visitTags);
      }

      for (Tag tag : visitTags) {
         if (tag.m_7916_().equals(this.value)) {
            return true;
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox keyEditBox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("key"), null, null);
      ResizableEditBox valueEditBox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("value"), null, null);
      Function<Object, Tag> stringParser = value -> StringTag.m_129297_(value.toString());
      Function<Tag, Object> stringGetter = tag -> ParseUtil.nullOrToString(tag, Tag::m_7916_);
      return List.of(
         Condition.ParameterEditor.of(stringParser, stringGetter, keyEditBox), Condition.ParameterEditor.of(stringParser, stringGetter, valueEditBox)
      );
   }

   private static List<Tag> visitTags(String key, List<Tag> compoundTag) {
      Pattern pattern = Pattern.compile("\\[[0-9]*\\]", 8);
      Matcher matcher = pattern.matcher(key);
      List<Tag> childs = Lists.newArrayList();
      if (matcher.find()) {
         String sIndex = matcher.group().replaceAll("[\\[\\]]", "");
         String arrayKey = matcher.replaceAll("");
         if (StringUtil.isNullOrEmpty(sIndex)) {
            for (Tag tag : compoundTag) {
               if (tag instanceof CompoundTag compTag && compTag.m_128441_(arrayKey)) {
                  for (Tag listTagElement : (ListTag)compTag.m_128423_(arrayKey)) {
                     childs.add(listTagElement);
                  }
               }
            }
         } else {
            int index = Integer.valueOf(sIndex);

            for (Tag tag : compoundTag) {
               if (tag instanceof CompoundTag compTag && compTag.m_128441_(arrayKey)) {
                  ListTag listTag = (ListTag)compTag.m_128423_(arrayKey);
                  childs.add(listTag.get(index));
               }
            }
         }
      } else {
         for (Tag tag : compoundTag) {
            if (tag instanceof CompoundTag compTag && compTag.m_128441_(key)) {
               childs.add(compTag.m_128423_(key));
            }
         }
      }

      return childs;
   }
}
