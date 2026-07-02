package forge.net.mca.util.recipes;

import forge.net.mca.entity.CribWoodType;
import forge.net.mca.item.CribItem;
import forge.net.mca.item.ItemsMCA;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class CribRecipeProvider {
   public static void generate(Consumer<FinishedRecipe> consumer) {
      for (CribWoodType wood : CribWoodType.values()) {
         for (DyeColor color : DyeColor.values()) {
            ShapedRecipeBuilder.m_246608_(RecipeCategory.DECORATIONS, (ItemLike)ItemsMCA.CRIBS.stream().filter(c -> {
                  CribItem crib = (CribItem)c.get();
                  return crib.getColor() == color && crib.getWood() == wood;
               }).findFirst().get().get(), 1)
               .m_126127_('F', fenceFromWoodType(wood))
               .m_126127_('P', plankFromWoodType(wood))
               .m_126127_('C', carpetFromColor(color))
               .m_126130_("F F")
               .m_126130_("FCF")
               .m_126130_("PPP")
               .m_176498_(consumer);
         }
      }
   }

   private static ItemLike plankFromWoodType(CribWoodType woodType) {
      switch (woodType) {
         case SPRUCE:
            return Blocks.f_50741_;
         case ACACIA:
            return Blocks.f_50744_;
         case BIRCH:
            return Blocks.f_50742_;
         case CHERRY:
            return Blocks.f_271304_;
         case CRIMSON:
            return Blocks.f_50655_;
         case DARK_OAK:
            return Blocks.f_50745_;
         case JUNGLE:
            return Blocks.f_50743_;
         case MANGROVE:
            return Blocks.f_220865_;
         case WARPED:
            return Blocks.f_50656_;
         default:
            return Blocks.f_50705_;
      }
   }

   private static ItemLike fenceFromWoodType(CribWoodType woodType) {
      switch (woodType) {
         case SPRUCE:
            return Blocks.f_50479_;
         case ACACIA:
            return Blocks.f_50482_;
         case BIRCH:
            return Blocks.f_50480_;
         case CHERRY:
            return Blocks.f_271219_;
         case CRIMSON:
            return Blocks.f_50661_;
         case DARK_OAK:
            return Blocks.f_50483_;
         case JUNGLE:
            return Blocks.f_50481_;
         case MANGROVE:
            return Blocks.f_220852_;
         case WARPED:
            return Blocks.f_50662_;
         default:
            return Blocks.f_50132_;
      }
   }

   private static ItemLike carpetFromColor(DyeColor color) {
      switch (color) {
         case WHITE:
            return Blocks.f_50336_;
         case ORANGE:
            return Blocks.f_50337_;
         case MAGENTA:
            return Blocks.f_50338_;
         case LIGHT_BLUE:
            return Blocks.f_50339_;
         case YELLOW:
            return Blocks.f_50340_;
         case LIME:
            return Blocks.f_50341_;
         case PINK:
            return Blocks.f_50342_;
         case GRAY:
            return Blocks.f_50343_;
         case LIGHT_GRAY:
            return Blocks.f_50344_;
         case CYAN:
            return Blocks.f_50345_;
         case PURPLE:
            return Blocks.f_50346_;
         case BLUE:
            return Blocks.f_50347_;
         case BROWN:
            return Blocks.f_50348_;
         case GREEN:
            return Blocks.f_50349_;
         case BLACK:
            return Blocks.f_50351_;
         default:
            return Blocks.f_50350_;
      }
   }
}
