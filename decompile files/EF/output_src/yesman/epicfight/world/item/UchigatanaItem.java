package yesman.epicfight.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.main.EpicFightMod;

public class UchigatanaItem extends WeaponItem {
   public UchigatanaItem(Properties build) {
      super(EpicFightItemTier.UCHIGATANA, 0, -2.0F, build);
   }

   public boolean m_6832_(ItemStack toRepair, ItemStack repair) {
      return toRepair.m_41720_() == Items.f_42025_;
   }

   public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.m_237113_(""));
      tooltip.add(Component.m_237115_(EpicFightMod.format("item.%s.uchigatana.tooltip")));
   }

   public float m_8102_(ItemStack itemstack, BlockState blockstate) {
      if (blockstate.m_60713_(Blocks.f_50033_)) {
         return 15.0F;
      } else {
         return blockstate.m_204336_(BlockTags.f_278398_) ? 1.5F : 1.0F;
      }
   }

   @Override
   public boolean m_8096_(BlockState blockstate) {
      return blockstate.m_60713_(Blocks.f_50033_);
   }
}
