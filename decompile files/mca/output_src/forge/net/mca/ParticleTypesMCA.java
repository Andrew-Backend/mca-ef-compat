package forge.net.mca;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.mixin.MixinDefaultParticleType;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public interface ParticleTypesMCA {
   DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create("mca", Registries.f_256890_);
   RegistrySupplier<SimpleParticleType> POS_INTERACTION = register("pos_interaction", () -> MixinDefaultParticleType.init(false));
   RegistrySupplier<SimpleParticleType> NEG_INTERACTION = register("neg_interaction", () -> MixinDefaultParticleType.init(false));

   static void bootstrap() {
      PARTICLE_TYPES.register();
   }

   static <T extends ParticleType<?>> RegistrySupplier<T> register(String name, Supplier<T> type) {
      return PARTICLE_TYPES.register(new ResourceLocation("mca", name), type);
   }
}
