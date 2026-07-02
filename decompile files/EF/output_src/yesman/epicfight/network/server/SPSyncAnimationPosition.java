package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.network.common.SyncAnimationPositionPacket;

public class SPSyncAnimationPosition extends SyncAnimationPositionPacket {
   public SPSyncAnimationPosition(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
      super(entityId, elapsedTime, position, lerpSteps);
   }

   public static SPSyncAnimationPosition fromBytes(FriendlyByteBuf buf) {
      return new SPSyncAnimationPosition(buf.readInt(), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt());
   }

   public static void toBytes(SPSyncAnimationPosition msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.entityId);
      buf.writeFloat(msg.elapsedTime);
      buf.writeDouble(msg.position.f_82479_);
      buf.writeDouble(msg.position.f_82480_);
      buf.writeDouble(msg.position.f_82481_);
      buf.writeInt(msg.lerpSteps);
   }

   public static void handle(SPSyncAnimationPosition msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Entity entity = Minecraft.m_91087_().f_91073_.m_6815_(msg.entityId);
         if (entity != null && entity instanceof LivingEntity livingentity) {
            livingentity.f_20904_ = msg.position.f_82479_;
            livingentity.f_20905_ = msg.position.f_82480_;
            livingentity.f_20906_ = msg.position.f_82481_;
            livingentity.f_20903_ = msg.lerpSteps;
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
