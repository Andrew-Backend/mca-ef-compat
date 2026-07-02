package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.api.utils.LevelUtil;

public record SPFracture(Vec3 location, double radius, boolean noSound, boolean noParticle) {
   public SPFracture() {
      this(Vec3.f_82478_, 0.0);
   }

   public SPFracture(Vec3 location, double radius) {
      this(location, radius, false, false);
   }

   public static SPFracture fromBytes(FriendlyByteBuf buf) {
      return new SPFracture(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readDouble(), buf.readBoolean(), buf.readBoolean());
   }

   public static void toBytes(SPFracture msg, FriendlyByteBuf buf) {
      buf.writeDouble(msg.location.f_82479_);
      buf.writeDouble(msg.location.f_82480_);
      buf.writeDouble(msg.location.f_82481_);
      buf.writeDouble(msg.radius);
      buf.writeBoolean(msg.noSound);
      buf.writeBoolean(msg.noParticle);
   }

   public static void handle(SPFracture msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> LevelUtil.getInstance().handlePacket(msg));
      ctx.get().setPacketHandled(true);
   }
}
