package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public interface Faction extends ExtendableEnum {
   ExtendableEnumManager<Faction> ENUM_MANAGER = new ExtendableEnumManager<>("faction");

   ResourceLocation healthBarTexture();

   int healthBarIndex();

   int damageColor();
}
