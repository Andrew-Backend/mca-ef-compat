package quilt.net.mca.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_7924;
import net.minecraft.class_4970.class_2251;
import quilt.net.mca.TagsMCA;

public interface BlocksMCA {
   DeferredRegister<class_2248> BLOCKS = DeferredRegister.create("mca", class_7924.field_41254);
   RegistrySupplier<class_2248> ROSE_GOLD_BLOCK = register("rose_gold_block", () -> new class_2248(class_2251.method_9630(class_2246.field_10205)));
   RegistrySupplier<class_2248> JEWELER_WORKBENCH = register(
      "jeweler_workbench", () -> new JewelerWorkbench(class_2251.method_9630(class_2246.field_10126).method_22488())
   );
   RegistrySupplier<class_2248> INFERNAL_FLAME = register("infernal_flame", () -> new InfernalFlameBlock(class_2251.method_9630(class_2246.field_22089)));
   RegistrySupplier<class_2248> GRAVELLING_HEADSTONE = register(
      "gravelling_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10340).method_22488(), 100, 50, new class_243(0.0, -25.0, 40.0), -90.0F, true, TombstoneBlock.GRAVELLING_SHAPE
      )
   );
   RegistrySupplier<class_2248> UPRIGHT_HEADSTONE = register(
      "upright_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10340).method_22488(), 70, 30, new class_243(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE
      )
   );
   RegistrySupplier<class_2248> SLANTED_HEADSTONE = register(
      "slanted_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10340).method_22488(), 90, 15, new class_243(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE
      )
   );
   RegistrySupplier<class_2248> CROSS_HEADSTONE = register(
      "cross_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10340).method_22488(), 80, 15, new class_243(0.0, -13.0, 15.0), -45.0F, true, TombstoneBlock.CROSS_SHAPE
      )
   );
   RegistrySupplier<class_2248> WALL_HEADSTONE = register(
      "wall_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10340).method_22488(), 100, 15, new class_243(0.0, -25.0, 40.0), 0.0F, false, TombstoneBlock.WALL_SHAPE
      )
   );
   RegistrySupplier<class_2248> COBBLESTONE_UPRIGHT_HEADSTONE = register(
      "cobblestone_upright_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10445).method_22488(), 70, 30, new class_243(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE
      )
   );
   RegistrySupplier<class_2248> COBBLESTONE_SLANTED_HEADSTONE = register(
      "cobblestone_slanted_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10445).method_22488(), 90, 15, new class_243(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE
      )
   );
   RegistrySupplier<class_2248> WOODEN_UPRIGHT_HEADSTONE = register(
      "wooden_upright_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10126).method_22488(), 70, 30, new class_243(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE
      )
   );
   RegistrySupplier<class_2248> WOODEN_SLANTED_HEADSTONE = register(
      "wooden_slanted_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_10126).method_22488(), 90, 15, new class_243(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE
      )
   );
   RegistrySupplier<class_2248> GOLDEN_UPRIGHT_HEADSTONE = register(
      "golden_upright_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_28888).method_22488(), 70, 30, new class_243(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE
      )
   );
   RegistrySupplier<class_2248> GOLDEN_SLANTED_HEADSTONE = register(
      "golden_slanted_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_28888).method_22488(), 90, 15, new class_243(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE
      )
   );
   RegistrySupplier<class_2248> DEEPSLATE_UPRIGHT_HEADSTONE = register(
      "deepslate_upright_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_28888).method_22488(), 70, 30, new class_243(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE
      )
   );
   RegistrySupplier<class_2248> DEEPSLATE_SLANTED_HEADSTONE = register(
      "deepslate_slanted_headstone",
      () -> new TombstoneBlock(
         class_2251.method_9630(class_2246.field_28888).method_22488(), 90, 15, new class_243(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE
      )
   );

   static void bootstrap() {
      BLOCKS.register();
      TagsMCA.Blocks.bootstrap();
      BlockEntityTypesMCA.bootstrap();
   }

   static <T extends class_2248> RegistrySupplier<T> register(String name, Supplier<T> block) {
      class_2960 id = new class_2960("mca", name);
      return BLOCKS.register(id, block);
   }
}
