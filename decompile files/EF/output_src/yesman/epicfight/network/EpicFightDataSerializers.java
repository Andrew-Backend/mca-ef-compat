package yesman.epicfight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class EpicFightDataSerializers {
   public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER = DeferredRegister.create(Keys.ENTITY_DATA_SERIALIZERS, "epicfight");
   public static final RegistryObject<EntityDataSerializer<Vec3>> VEC3 = ENTITY_DATA_SERIALIZER.register(
      "vector_3_double", () -> new EntityDataSerializer<Vec3>() {
         public void write(FriendlyByteBuf buffer, Vec3 vec3) {
            buffer.writeDouble(vec3.f_82479_);
            buffer.writeDouble(vec3.f_82480_);
            buffer.writeDouble(vec3.f_82481_);
         }

         public Vec3 read(FriendlyByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
         }

         public Vec3 copy(Vec3 vec3) {
            return vec3;
         }
      }
   );
}
