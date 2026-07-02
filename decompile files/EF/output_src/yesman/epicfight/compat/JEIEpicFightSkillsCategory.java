package yesman.epicfight.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import yesman.epicfight.skill.SkillCategories;

public class JEIEpicFightSkillsCategory implements IRecipeCategory<SkillCategories> {
   public RecipeType<SkillCategories> getRecipeType() {
      return null;
   }

   public Component getTitle() {
      return null;
   }

   public IDrawable getBackground() {
      return null;
   }

   public IDrawable getIcon() {
      return null;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, SkillCategories recipe, IFocusGroup focuses) {
   }
}
