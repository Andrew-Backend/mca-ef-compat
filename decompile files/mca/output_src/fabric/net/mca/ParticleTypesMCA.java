package fabric.net.mca;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import fabric.net.mca.mixin.MixinDefaultParticleType;
import java.util.function.Supplier;
import net.minecraft.class_2396;
import net.minecraft.class_2400;
import net.minecraft.class_2960;
import net.minecraft.class_7924;

public interface ParticleTypesMCA {
   DeferredRegister<class_2396<?>> PARTICLE_TYPES = DeferredRegister.create("mca", class_7924.field_41210);
   RegistrySupplier<class_2400> POS_INTERACTION = register("pos_interaction", () -> MixinDefaultParticleType.init(false));
   RegistrySupplier<class_2400> NEG_INTERACTION = register("neg_interaction", () -> MixinDefaultParticleType.init(false));

   static void bootstrap() {
      PARTICLE_TYPES.register();
   }

   static <T extends class_2396<?>> RegistrySupplier<T> register(String name, Supplier<T> type) {
      return PARTICLE_TYPES.register(new class_2960("mca", name), type);
   }
}
