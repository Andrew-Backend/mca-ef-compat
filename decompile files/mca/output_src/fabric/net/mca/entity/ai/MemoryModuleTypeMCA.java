package fabric.net.mca.entity.ai;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import fabric.net.mca.mixin.MixinMemoryModuleType;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_4140;
import net.minecraft.class_7924;

public interface MemoryModuleTypeMCA {
   DeferredRegister<class_4140<?>> MEMORY_MODULES = DeferredRegister.create("mca", class_7924.field_41206);
   RegistrySupplier<class_4140<class_1657>> PLAYER_FOLLOWING = register("player_following_memory", Optional.empty());
   RegistrySupplier<class_4140<Boolean>> STAYING = register("staying_memory", Optional.of(Codec.BOOL));
   RegistrySupplier<class_4140<class_1309>> NEAREST_GUARD_ENEMY = register("nearest_guard_enemy", Optional.empty());
   RegistrySupplier<class_4140<Boolean>> WEARS_ARMOR = register("wears_armor", Optional.of(Codec.BOOL));
   RegistrySupplier<class_4140<Integer>> SMALL_BOUNTY = register("small_bounty", Optional.of(Codec.INT));
   RegistrySupplier<class_4140<class_1309>> HIT_BY_PLAYER = register("hit_by_player", Optional.empty());
   RegistrySupplier<class_4140<Long>> LAST_GRIEVE = register("last_grieve", Optional.of(Codec.LONG));
   RegistrySupplier<class_4140<Boolean>> FORCED_HOME = register("forced_home", Optional.of(Codec.BOOL));

   static void bootstrap() {
      MEMORY_MODULES.register();
   }

   static <U> RegistrySupplier<class_4140<U>> register(String name, Optional<Codec<U>> codec) {
      class_2960 id = new class_2960("mca", name);
      return MEMORY_MODULES.register(id, () -> MixinMemoryModuleType.init(codec));
   }
}
