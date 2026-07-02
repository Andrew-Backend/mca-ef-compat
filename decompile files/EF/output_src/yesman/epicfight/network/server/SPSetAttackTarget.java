package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPSetAttackTarget {
   private final int entityId;
   private int targetEntityId;

   public SPSetAttackTarget() {
      this.entityId = 0;
   }

   public SPSetAttackTarget(int entityId, int targetEntityId) {
      this.entityId = entityId;
      this.targetEntityId = targetEntityId;
   }

   public static SPSetAttackTarget fromBytes(FriendlyByteBuf buf) {
      return new SPSetAttackTarget(buf.readInt(), buf.readInt());
   }

   public static void toBytes(SPSetAttackTarget msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.entityId);
      buf.writeInt(msg.targetEntityId);
   }

   public static void handle(SPSetAttackTarget msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Minecraft minecraft = Minecraft.m_91087_();
         Entity entity = minecraft.f_91073_.m_6815_(msg.entityId);
         Entity targetEntity = minecraft.f_91073_.m_6815_(msg.targetEntityId);
         if (entity != null && entity instanceof Mob) {
            if (targetEntity != null && targetEntity instanceof LivingEntity) {
               ((Mob)entity).m_6710_((LivingEntity)targetEntity);
            } else {
               ((Mob)entity).m_6710_(null);
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
