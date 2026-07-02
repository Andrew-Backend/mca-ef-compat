package yesman.epicfight.api.utils;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.Vec3f;

public interface PacketBufferCodec<T> {
   PacketBufferCodec<Boolean> BOOLEAN = new PacketBufferCodec<Boolean>() {
      public void encode(Boolean obj, FriendlyByteBuf buffer) {
         buffer.writeBoolean(obj);
      }

      public Boolean decode(FriendlyByteBuf buffer) {
         return buffer.readBoolean();
      }
   };
   PacketBufferCodec<Integer> INTEGER = new PacketBufferCodec<Integer>() {
      public void encode(Integer obj, FriendlyByteBuf buffer) {
         buffer.writeInt(obj);
      }

      public Integer decode(FriendlyByteBuf buffer) {
         return buffer.readInt();
      }
   };
   PacketBufferCodec<Float> FLOAT = new PacketBufferCodec<Float>() {
      public void encode(Float obj, FriendlyByteBuf buffer) {
         buffer.writeFloat(obj);
      }

      public Float decode(FriendlyByteBuf buffer) {
         return buffer.readFloat();
      }
   };
   PacketBufferCodec<Double> DOUBLE = new PacketBufferCodec<Double>() {
      public void encode(Double obj, FriendlyByteBuf buffer) {
         buffer.writeDouble(obj);
      }

      public Double decode(FriendlyByteBuf buffer) {
         return buffer.readDouble();
      }
   };
   PacketBufferCodec<Vec3> VEC3 = new PacketBufferCodec<Vec3>() {
      public void encode(Vec3 obj, FriendlyByteBuf buffer) {
         buffer.writeDouble(obj.f_82479_);
         buffer.writeDouble(obj.f_82480_);
         buffer.writeDouble(obj.f_82481_);
      }

      public Vec3 decode(FriendlyByteBuf buffer) {
         return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
      }
   };
   PacketBufferCodec<Vec3f> VEC3F = new PacketBufferCodec<Vec3f>() {
      public void encode(Vec3f obj, FriendlyByteBuf buffer) {
         buffer.writeFloat(obj.x);
         buffer.writeFloat(obj.y);
         buffer.writeFloat(obj.z);
      }

      public Vec3f decode(FriendlyByteBuf buffer) {
         return new Vec3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
      }
   };

   void encode(T var1, FriendlyByteBuf var2);

   T decode(FriendlyByteBuf var1);

   static <T> PacketBufferCodec<TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
      return new PacketBufferCodec<TagKey<T>>() {
         public void encode(TagKey<T> tagKey, FriendlyByteBuf buffer) {
            buffer.m_130085_(tagKey.f_203867_().m_135782_());
            buffer.m_130085_(tagKey.f_203868_());
         }

         public TagKey<T> decode(FriendlyByteBuf buffer) {
            ResourceLocation registryx = buffer.m_130281_();
            ResourceLocation tagName = buffer.m_130281_();
            return TagKey.m_203882_(ResourceKey.m_135788_(registryx), tagName);
         }
      };
   }
}
