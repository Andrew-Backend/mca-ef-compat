package forge.net.mca.entity.ai;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.mixin.MixinMemoryModuleType;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public interface MemoryModuleTypeMCA {
   DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create("mca", Registries.f_257023_);
   RegistrySupplier<MemoryModuleType<Player>> PLAYER_FOLLOWING = register("player_following_memory", Optional.empty());
   RegistrySupplier<MemoryModuleType<Boolean>> STAYING = register("staying_memory", Optional.of(Codec.BOOL));
   RegistrySupplier<MemoryModuleType<LivingEntity>> NEAREST_GUARD_ENEMY = register("nearest_guard_enemy", Optional.empty());
   RegistrySupplier<MemoryModuleType<Boolean>> WEARS_ARMOR = register("wears_armor", Optional.of(Codec.BOOL));
   RegistrySupplier<MemoryModuleType<Integer>> SMALL_BOUNTY = register("small_bounty", Optional.of(Codec.INT));
   RegistrySupplier<MemoryModuleType<LivingEntity>> HIT_BY_PLAYER = register("hit_by_player", Optional.empty());
   RegistrySupplier<MemoryModuleType<Long>> LAST_GRIEVE = register("last_grieve", Optional.of(Codec.LONG));
   RegistrySupplier<MemoryModuleType<Boolean>> FORCED_HOME = register("forced_home", Optional.of(Codec.BOOL));

   static void bootstrap() {
      MEMORY_MODULES.register();
   }

   static <U> RegistrySupplier<MemoryModuleType<U>> register(String name, Optional<Codec<U>> codec) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return MEMORY_MODULES.register(id, () -> MixinMemoryModuleType.init(codec));
   }
}
