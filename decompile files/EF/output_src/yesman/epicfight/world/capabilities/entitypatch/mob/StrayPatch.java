package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.world.item.EpicFightItems;

public class StrayPatch<T extends AbstractSkeleton> extends SkeletonPatch<T> {
   public void onJoinWorld(T entityIn, EntityJoinLevelEvent event) {
      super.onJoinWorld(entityIn, event);
      this.original.m_8061_(EquipmentSlot.HEAD, new ItemStack((ItemLike)EpicFightItems.STRAY_HAT.get()));
      this.original.m_8061_(EquipmentSlot.CHEST, new ItemStack((ItemLike)EpicFightItems.STRAY_ROBE.get()));
      this.original.m_8061_(EquipmentSlot.LEGS, new ItemStack((ItemLike)EpicFightItems.STRAY_PANTS.get()));
   }
}
