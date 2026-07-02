package forge.net.mca.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.TagsMCA;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;

public interface BlocksMCA {
   DeferredRegister<Block> BLOCKS = DeferredRegister.create("mca", Registries.f_256747_);
   RegistrySupplier<Block> ROSE_GOLD_BLOCK = register("rose_gold_block", () -> new Block(Properties.m_60926_(Blocks.f_50074_)));
   RegistrySupplier<Block> JEWELER_WORKBENCH = register("jeweler_workbench", () -> new JewelerWorkbench(Properties.m_60926_(Blocks.f_50011_).m_60955_()));
   RegistrySupplier<Block> INFERNAL_FLAME = register("infernal_flame", () -> new InfernalFlameBlock(Properties.m_60926_(Blocks.f_50084_)));
   RegistrySupplier<Block> GRAVELLING_HEADSTONE = register(
      "gravelling_headstone",
      () -> new TombstoneBlock(
         Properties.m_60926_(Blocks.f_50069_).m_60955_(), 100, 50, new Vec3(0.0, -25.0, 40.0), -90.0F, true, TombstoneBlock.GRAVELLING_SHAPE
      )
   );
   RegistrySupplier<Block> UPRIGHT_HEADSTONE = register(
      "upright_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50069_).m_60955_(), 70, 30, new Vec3(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE)
   );
   RegistrySupplier<Block> SLANTED_HEADSTONE = register(
      "slanted_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50069_).m_60955_(), 90, 15, new Vec3(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE)
   );
   RegistrySupplier<Block> CROSS_HEADSTONE = register(
      "cross_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50069_).m_60955_(), 80, 15, new Vec3(0.0, -13.0, 15.0), -45.0F, true, TombstoneBlock.CROSS_SHAPE)
   );
   RegistrySupplier<Block> WALL_HEADSTONE = register(
      "wall_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50069_).m_60955_(), 100, 15, new Vec3(0.0, -25.0, 40.0), 0.0F, false, TombstoneBlock.WALL_SHAPE)
   );
   RegistrySupplier<Block> COBBLESTONE_UPRIGHT_HEADSTONE = register(
      "cobblestone_upright_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50652_).m_60955_(), 70, 30, new Vec3(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE)
   );
   RegistrySupplier<Block> COBBLESTONE_SLANTED_HEADSTONE = register(
      "cobblestone_slanted_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50652_).m_60955_(), 90, 15, new Vec3(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE)
   );
   RegistrySupplier<Block> WOODEN_UPRIGHT_HEADSTONE = register(
      "wooden_upright_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50011_).m_60955_(), 70, 30, new Vec3(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE)
   );
   RegistrySupplier<Block> WOODEN_SLANTED_HEADSTONE = register(
      "wooden_slanted_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_50011_).m_60955_(), 90, 15, new Vec3(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE)
   );
   RegistrySupplier<Block> GOLDEN_UPRIGHT_HEADSTONE = register(
      "golden_upright_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_152550_).m_60955_(), 70, 30, new Vec3(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE)
   );
   RegistrySupplier<Block> GOLDEN_SLANTED_HEADSTONE = register(
      "golden_slanted_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_152550_).m_60955_(), 90, 15, new Vec3(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE)
   );
   RegistrySupplier<Block> DEEPSLATE_UPRIGHT_HEADSTONE = register(
      "deepslate_upright_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_152550_).m_60955_(), 70, 30, new Vec3(0.0, -30.0, -8.0), 0.0F, true, TombstoneBlock.UPRIGHT_SHAPE)
   );
   RegistrySupplier<Block> DEEPSLATE_SLANTED_HEADSTONE = register(
      "deepslate_slanted_headstone",
      () -> new TombstoneBlock(Properties.m_60926_(Blocks.f_152550_).m_60955_(), 90, 15, new Vec3(0.0, -12.0, 22.0), -72.5F, true, TombstoneBlock.SLANTED_SHAPE)
   );

   static void bootstrap() {
      BLOCKS.register();
      TagsMCA.Blocks.bootstrap();
      BlockEntityTypesMCA.bootstrap();
   }

   static <T extends Block> RegistrySupplier<T> register(String name, Supplier<T> block) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return BLOCKS.register(id, block);
   }
}
