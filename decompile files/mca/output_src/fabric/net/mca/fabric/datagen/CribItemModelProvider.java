package fabric.net.mca.fabric.datagen;

import fabric.net.mca.MCA;
import fabric.net.mca.entity.CribWoodType;
import fabric.net.mca.item.CribItem;
import fabric.net.mca.item.ItemsMCA;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.class_1767;
import net.minecraft.class_1792;
import net.minecraft.class_2960;
import net.minecraft.class_4910;
import net.minecraft.class_4915;
import net.minecraft.class_4941;
import net.minecraft.class_4942;
import net.minecraft.class_4944;
import net.minecraft.class_4945;

public class CribItemModelProvider extends FabricModelProvider {
   public CribItemModelProvider(FabricDataOutput output) {
      super(output);
   }

   public void generateBlockStateModels(class_4910 blockStateModelGenerator) {
   }

   public void generateItemModels(class_4915 itemModelGenerator) {
      for (CribWoodType wood : CribWoodType.values()) {
         for (class_1767 color : class_1767.values()) {
            class_1792 item = (class_1792)ItemsMCA.CRIBS.stream().filter(c -> {
               CribItem crib = (CribItem)c.get();
               return crib.getColor() == color && crib.getWood() == wood;
            }).findFirst().orElse(ItemsMCA.CRIBS.get(0)).get();
            class_4942 cribModel = new class_4942(
               Optional.of(new class_2960("minecraft", "item/generated")), Optional.empty(), new class_4945[]{class_4945.field_23006, class_4945.field_42089}
            );
            cribModel.method_25852(
               class_4941.method_25840(item),
               class_4944.method_48529(
                  MCA.locate("item/crib/beds/" + color.method_7792()), MCA.locate("item/crib/frames/" + wood.toString().toLowerCase(Locale.ROOT))
               ),
               itemModelGenerator.field_22844
            );
         }
      }
   }
}
