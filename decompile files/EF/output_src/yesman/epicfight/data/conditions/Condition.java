package yesman.epicfight.data.conditions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface Condition<T> {
   default Condition<T> read(JsonElement json) throws CommandSyntaxException {
      return this.read(TagParser.m_129359_(json.toString()));
   }

   Condition<T> read(CompoundTag var1) throws IllegalArgumentException;

   CompoundTag serializePredicate();

   boolean predicate(T var1);

   default <O> O assertTag(String key, String tagFormatMessage, CompoundTag compound, Class<? extends Tag> tagType, BiFunction<CompoundTag, String, O> getter) throws IllegalArgumentException {
      if (!compound.m_128441_(key)) {
         throw new IllegalArgumentException(MessageFormat.format("{0} condition error: {1} not specified!", this.getClass().getSimpleName(), key));
      } else {
         Tag tag = compound.m_128423_(key);
         if (!tagType.isAssignableFrom(tag.getClass())) {
            throw new IllegalArgumentException(
               MessageFormat.format("{0} condition error: the {1} value must be a {2} format", this.getClass().getSimpleName(), key, tagFormatMessage)
            );
         } else {
            return getter.apply(compound, key);
         }
      }
   }

   default <E extends Enum<E>> E assertEnumTag(String key, Class<E> enumCls, CompoundTag compound) throws IllegalArgumentException {
      if (!compound.m_128441_(key)) {
         throw new IllegalArgumentException(MessageFormat.format("{0} condition error: {1} not specified!", this.getClass().getSimpleName(), key));
      }

      String enumString = this.<String>assertTag(key, "string", compound, StringTag.class, CompoundTag::m_128461_).toUpperCase(Locale.ROOT);

      try {
         return Enum.valueOf(enumCls, enumString);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(
            MessageFormat.format("{0} condition error: invalid enum for {1}: {2}", this.getClass().getSimpleName(), key, enumString)
         );
      }
   }

   default <E extends ExtendableEnum> E assertExtendableEnumTag(String key, ExtendableEnumManager<E> extendableEnumManager, CompoundTag compound) throws IllegalArgumentException, NoSuchElementException {
      if (!compound.m_128441_(key)) {
         throw new IllegalArgumentException(MessageFormat.format("{0} condition error: {1} not specified!", this.getClass().getSimpleName(), key));
      }

      String enumString = this.<String>assertTag(key, "string", compound, StringTag.class, CompoundTag::m_128461_).toLowerCase(Locale.ROOT);

      try {
         return extendableEnumManager.getOrThrow(enumString);
      } catch (NoSuchElementException ex) {
         throw new NoSuchElementException(MessageFormat.format("{0} condition error: {1}", this.getClass().getSimpleName(), ex.getMessage()));
      }
   }

   @OnlyIn(Dist.CLIENT)
   List<Condition.ParameterEditor> getAcceptingParameters(Screen var1);

   abstract class EntityCondition implements Condition<Entity> {
   }

   abstract class EntityPatchCondition implements Condition<LivingEntityPatch<?>> {
   }

   abstract class ItemStackCondition implements Condition<ItemStack> {
   }

   @OnlyIn(Dist.CLIENT)
   class ParameterEditor {
      public final Function<Object, Tag> toTag;
      public final Function<Tag, Object> fromTag;
      public final AbstractWidget editWidget;

      public static Condition.ParameterEditor of(Function<Object, Tag> toTag, Function<Tag, Object> fromTag, AbstractWidget editWidget) {
         return new Condition.ParameterEditor(toTag, fromTag, editWidget);
      }

      private ParameterEditor(Function<Object, Tag> toTag, Function<Tag, Object> fromTag, AbstractWidget editWidget) {
         this.toTag = toTag;
         this.fromTag = fromTag;
         this.editWidget = editWidget;
      }
   }
}
