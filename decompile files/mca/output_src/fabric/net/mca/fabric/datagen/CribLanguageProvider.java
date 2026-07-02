package fabric.net.mca.fabric.datagen;

import fabric.net.mca.entity.CribWoodType;
import fabric.net.mca.item.CribItem;
import fabric.net.mca.item.ItemsMCA;
import java.util.Locale;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.class_1767;

public class CribLanguageProvider extends FabricLanguageProvider {
   protected CribLanguageProvider(FabricDataOutput dataOutput) {
      super(dataOutput);
   }

   public void generateTranslations(TranslationBuilder translationBuilder) {
      for (CribWoodType wood : CribWoodType.values()) {
         for (class_1767 color : class_1767.values()) {
            CribItem item = (CribItem)ItemsMCA.CRIBS.stream().filter(c -> {
               CribItem crib = (CribItem)c.get();
               return crib.getColor() == color && crib.getWood() == wood;
            }).findFirst().get().get();
            String colorName = "";

            for (String s : item.getColor().method_7792().split("_")) {
               colorName = colorName + s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1) + " ";
            }

            String woodName = "";

            for (String s : item.getWood().toString().toLowerCase(Locale.ROOT).split("_")) {
               woodName = woodName + s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1) + " ";
            }

            translationBuilder.add(item, colorName + woodName + "Crib");
         }
      }
   }
}
