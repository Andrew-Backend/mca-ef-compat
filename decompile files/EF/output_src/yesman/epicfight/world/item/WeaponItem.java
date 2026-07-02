package yesman.epicfight.world.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WeaponItem extends SwordItem {
   public WeaponItem(Tier tier, int damageIn, float speedIn, Properties builder) {
      super(tier, damageIn, speedIn, builder);
   }

   public boolean m_8096_(BlockState blockIn) {
      return false;
   }
}
