package forge.net.mca.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntityTypesMCA {
   DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create("mca", Registries.f_256922_);
   RegistrySupplier<BlockEntityType<TombstoneBlock.Data>> TOMBSTONE = register(
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

   static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(
      String name, BiFunction<BlockPos, BlockState, T> factory, List<RegistrySupplier<Block>> suppliers
   ) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return BLOCK_ENTITY_TYPES.register(
         id,
         () -> Builder.m_155273_(factory::apply, suppliers.stream().map(Supplier::get).toArray(Block[]::new))
            .m_58966_(Util.m_137456_(References.f_16781_, id.toString()))
      );
   }
}
