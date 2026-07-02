package yesman.epicfight.main;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.registries.RegistryObject;

public record EpicFightExtensions(RegistryObject<CreativeModeTab> skillBookCreativeTab) implements IExtensionPoint<EpicFightExtensions> {
}
