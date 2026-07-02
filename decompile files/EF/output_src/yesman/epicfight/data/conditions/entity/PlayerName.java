package yesman.epicfight.data.conditions.entity;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class PlayerName extends Condition.EntityPatchCondition {
   private String name;

   public PlayerName read(CompoundTag tag) {
      this.name = this.assertTag("name", "string", tag, StringTag.class, CompoundTag::m_128461_);
      return this;
   }

   @Override
   public CompoundTag serializePredicate() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("name", this.name);
      return tag;
   }

   public boolean predicate(LivingEntityPatch<?> target) {
      return target instanceof PlayerPatch<?> playerpatch ? playerpatch.getOriginal().m_7755_().getString().equals(this.name) : false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Condition.ParameterEditor> getAcceptingParameters(Screen screen) {
      ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("name"), null, null);
      return List.of(Condition.ParameterEditor.of(name -> StringTag.m_129297_(name.toString()), tag -> ParseUtil.nullOrToString(tag, Tag::m_7916_), editbox));
   }
}
