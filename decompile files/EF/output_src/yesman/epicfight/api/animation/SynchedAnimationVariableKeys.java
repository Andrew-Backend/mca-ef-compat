package yesman.epicfight.api.animation;

import java.util.function.Supplier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.main.EpicFightMod;

public class SynchedAnimationVariableKeys {
   private static final Supplier<RegistryBuilder<SynchedAnimationVariableKey<?>>> BUILDER = () -> new RegistryBuilder()
      .addCallback(SynchedAnimationVariableKey.getRegistryCallback());
   public static final DeferredRegister<SynchedAnimationVariableKey<?>> SYNCHED_ANIMATION_VARIABLE_KEYS = DeferredRegister.create(
      EpicFightMod.identifier("synched_animation_variable_keys"), "epicfight"
   );
   public static final Supplier<IForgeRegistry<SynchedAnimationVariableKey<?>>> REGISTRY = SYNCHED_ANIMATION_VARIABLE_KEYS.makeRegistry(BUILDER);
   public static final RegistryObject<SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<Vec3>> DESTINATION = SYNCHED_ANIMATION_VARIABLE_KEYS.register(
      "destination",
      () -> SynchedAnimationVariableKey.independent(animator -> animator.getEntityPatch().getOriginal().m_20182_(), true, PacketBufferCodec.VEC3)
   );
   public static final RegistryObject<SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<Integer>> TARGET_ENTITY = SYNCHED_ANIMATION_VARIABLE_KEYS.register(
      "target_entity", () -> SynchedAnimationVariableKey.independent(animator -> (Integer)null, true, PacketBufferCodec.INTEGER)
   );
   public static final RegistryObject<SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<Integer>> CHARGING_TICKS = SYNCHED_ANIMATION_VARIABLE_KEYS.register(
      "animation_playing_speed", () -> SynchedAnimationVariableKey.independent(animator -> 0, true, PacketBufferCodec.INTEGER)
   );
}
