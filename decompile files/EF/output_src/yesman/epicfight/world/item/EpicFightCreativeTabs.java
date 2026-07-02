package yesman.epicfight.world.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_, "epicfight");
   public static final RegistryObject<CreativeModeTab> ITEMS = TABS.register(
      "items",
      () -> CreativeModeTab.builder()
         .m_257941_(Component.m_237115_("itemGroup.epicfight.items"))
         .m_257737_(() -> new ItemStack((ItemLike)EpicFightItems.SKILLBOOK.get()))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.f_256731_})
         .withBackgroundLocation(EpicFightMod.identifier("textures/gui/container/epicfight_creative_tab.png"))
         .m_257809_()
         .m_257501_((params, output) -> EpicFightItems.ITEMS.getEntries().forEach(item -> {
            if (item != EpicFightItems.UCHIGATANA_SHEATH && item != EpicFightItems.SKILLBOOK) {
               output.m_246326_((ItemLike)item.get());
            }
         }))
         .m_257652_()
   );
}
