package yesman.epicfight.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;

@JeiPlugin
public class JEIEpicFightPlugin implements IModPlugin {
   public ResourceLocation getPluginUid() {
      return EpicFightMod.identifier("jei_plugin");
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      super.registerCategories(registration);
   }

   public void registerRecipes(IRecipeRegistration registration) {
      super.registerRecipes(registration);
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      super.registerGuiHandlers(registration);
   }
}
