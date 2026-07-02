package yesman.epicfight.world.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class DaggerItem extends WeaponItem {
   public DaggerItem(Properties build, Tier materialIn) {
      super(materialIn, 1, -1.6F, build);
   }
}
