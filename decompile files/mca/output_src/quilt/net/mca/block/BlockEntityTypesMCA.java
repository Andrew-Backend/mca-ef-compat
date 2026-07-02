package quilt.net.mca.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.class_1208;
import net.minecraft.class_156;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_7924;
import net.minecraft.class_2591.class_2592;

public interface BlockEntityTypesMCA {
   DeferredRegister<class_2591<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create("mca", class_7924.field_41255);
   RegistrySupplier<class_2591<TombstoneBlock.Data>> TOMBSTONE = register(
      "tombstone",
      TombstoneBlock.Data::new,
      List.of(
         BlocksMCA.GRAVELLING_HEADSTONE,
         BlocksMCA.UPRIGHT_HEADSTONE,
         BlocksMCA.SLANTED_HEADSTONE,
         BlocksMCA.CROSS_HEADSTONE,
         BlocksMCA.WALL_HEADSTONE,
         BlocksMCA.COBBLESTONE_UPRIGHT_HEADSTONE,
         BlocksMCA.COBBLESTONE_SLANTED_HEADSTONE,
         BlocksMCA.WOODEN_UPRIGHT_HEADSTONE,
         BlocksMCA.WOODEN_SLANTED_HEADSTONE,
         BlocksMCA.GOLDEN_UPRIGHT_HEADSTONE,
         BlocksMCA.GOLDEN_SLANTED_HEADSTONE,
         BlocksMCA.DEEPSLATE_UPRIGHT_HEADSTONE,
         BlocksMCA.DEEPSLATE_SLANTED_HEADSTONE
      )
   );

   static void bootstrap() {
      BLOCK_ENTITY_TYPES.register();
   }

   static <T extends class_2586> RegistrySupplier<class_2591<T>> register(
      String name, BiFunction<class_2338, class_2680, T> factory, List<RegistrySupplier<class_2248>> suppliers
   ) {
      class_2960 id = new class_2960("mca", name);
      return BLOCK_ENTITY_TYPES.register(
         id,
         () -> class_2592.method_20528(factory::apply, suppliers.stream().map(Supplier::get).toArray(class_2248[]::new))
            .method_11034(class_156.method_29187(class_1208.field_5727, id.toString()))
      );
   }
}
