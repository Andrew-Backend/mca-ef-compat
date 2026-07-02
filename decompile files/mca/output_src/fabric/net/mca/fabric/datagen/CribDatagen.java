package fabric.net.mca.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;

public class CribDatagen implements DataGeneratorEntrypoint {
   public void onInitializeDataGenerator(FabricDataGenerator generator) {
      Pack pack = generator.createPack();
      pack.addProvider(CribRecipeProvider::new);
      pack.addProvider(CribLanguageProvider::new);
      pack.addProvider(CribItemModelProvider::new);
   }
}
