package fabric.net.mca.util.recipes;

import fabric.net.mca.entity.CribWoodType;
import fabric.net.mca.item.CribItem;
import fabric.net.mca.item.ItemsMCA;
import java.util.function.Consumer;
import net.minecraft.class_1767;
import net.minecraft.class_1935;
import net.minecraft.class_2246;
import net.minecraft.class_2444;
import net.minecraft.class_2447;
import net.minecraft.class_7800;

public class CribRecipeProvider {
   public static void generate(Consumer<class_2444> consumer) {
      for (CribWoodType wood : CribWoodType.values()) {
         for (class_1767 color : class_1767.values()) {
            class_2447.method_10436(class_7800.field_40635, (class_1935)ItemsMCA.CRIBS.stream().filter(c -> {
                  CribItem crib = (CribItem)c.get();
                  return crib.getColor() == color && crib.getWood() == wood;
               }).findFirst().get().get(), 1)
               .method_10434('F', fenceFromWoodType(wood))
               .method_10434('P', plankFromWoodType(wood))
               .method_10434('C', carpetFromColor(color))
               .method_10439("F F")
               .method_10439("FCF")
               .method_10439("PPP")
               .method_10431(consumer);
         }
      }
   }

   private static class_1935 plankFromWoodType(CribWoodType woodType) {
      switch (woodType) {
         case SPRUCE:
            return class_2246.field_9975;
         case ACACIA:
            return class_2246.field_10218;
         case BIRCH:
            return class_2246.field_10148;
         case CHERRY:
            return class_2246.field_42751;
         case CRIMSON:
            return class_2246.field_22126;
         case DARK_OAK:
            return class_2246.field_10075;
         case JUNGLE:
            return class_2246.field_10334;
         case MANGROVE:
            return class_2246.field_37577;
         case WARPED:
            return class_2246.field_22127;
         default:
            return class_2246.field_10161;
      }
   }

   private static class_1935 fenceFromWoodType(CribWoodType woodType) {
      switch (woodType) {
         case SPRUCE:
            return class_2246.field_10020;
         case ACACIA:
            return class_2246.field_10144;
         case BIRCH:
            return class_2246.field_10299;
         case CHERRY:
            return class_2246.field_42747;
         case CRIMSON:
            return class_2246.field_22132;
         case DARK_OAK:
            return class_2246.field_10132;
         case JUNGLE:
            return class_2246.field_10319;
         case MANGROVE:
            return class_2246.field_37565;
         case WARPED:
            return class_2246.field_22133;
         default:
            return class_2246.field_10620;
      }
   }

   private static class_1935 carpetFromColor(class_1767 color) {
      switch (color) {
         case field_7952:
            return class_2246.field_10466;
         case field_7946:
            return class_2246.field_9977;
         case field_7958:
            return class_2246.field_10482;
         case field_7951:
            return class_2246.field_10290;
         case field_7947:
            return class_2246.field_10512;
         case field_7961:
            return class_2246.field_10040;
         case field_7954:
            return class_2246.field_10393;
         case field_7944:
            return class_2246.field_10591;
         case field_7967:
            return class_2246.field_10209;
         case field_7955:
            return class_2246.field_10433;
         case field_7945:
            return class_2246.field_10510;
         case field_7966:
            return class_2246.field_10043;
         case field_7957:
            return class_2246.field_10473;
         case field_7942:
            return class_2246.field_10338;
         case field_7963:
            return class_2246.field_10106;
         default:
            return class_2246.field_10536;
      }
   }
}
