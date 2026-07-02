package yesman.epicfight.network.server;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class SPEntityPairingPacket {
   private final int entityId;
   private final EntityPairingPacketType type;
   private final FriendlyByteBuf buffer;

   public SPEntityPairingPacket() {
      this.entityId = 0;
      this.type = null;
      this.buffer = new FriendlyByteBuf(Unpooled.buffer());
   }

   public SPEntityPairingPacket(int entityId, EntityPairingPacketType eventType) {
      this.entityId = entityId;
      this.type = eventType;
      this.buffer = new FriendlyByteBuf(Unpooled.buffer());
   }

   public EntityPairingPacketType getPairingPacketType() {
      return this.type;
   }

   public FriendlyByteBuf getBuffer() {
      return this.buffer;
   }

   public static SPEntityPairingPacket fromBytes(FriendlyByteBuf buf) {
      SPEntityPairingPacket msg = new SPEntityPairingPacket(buf.readInt(), EntityPairingPacketType.ENUM_MANAGER.getOrThrow(buf.readInt()));

      while (buf.isReadable()) {
         msg.buffer.writeByte(buf.readByte());
      }

      return msg;
   }

   public static void toBytes(SPEntityPairingPacket msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.entityId);
      buf.writeInt(msg.type.universalOrdinal());

      while (msg.buffer.isReadable()) {
         buf.writeByte(msg.buffer.readByte());
      }
   }

   public static void handle(SPEntityPairingPacket msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Minecraft mc = Minecraft.m_91087_();
         Entity entity = mc.f_91074_.m_9236_().m_6815_(msg.entityId);
         if (entity != null) {
            EntityPatch<?> entitypatch = (EntityPatch<?>)entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
            if (entitypatch != null) {
               entitypatch.fireEntityPairingEvent(msg);
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
