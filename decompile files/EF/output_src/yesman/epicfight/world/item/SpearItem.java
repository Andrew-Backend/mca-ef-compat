package yesman.epicfight.world.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class SpearItem extends WeaponItem {
   public SpearItem(Properties build, Tier materialIn) {
      super(materialIn, 3, -2.8F, build);
   }
}
