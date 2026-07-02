package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPMoveAndPlayAnimation extends SPPlayAnimationAndSetTarget {
   protected double posX;
   protected double posY;
   protected double posZ;
   protected float yRot;

   public SPMoveAndPlayAnimation(
      AnimatorControlPacket.Action action,
      int animation,
      int entityId,
      float modifyTime,
      boolean pause,
      int targetId,
      double posX,
      double posY,
      double posZ,
      float yRot
   ) {
      super(action, animation, entityId, modifyTime, pause, targetId);
      this.posX = posX;
      this.posY = posY;
      this.posZ = posZ;
      this.yRot = yRot;
   }

   public SPMoveAndPlayAnimation(
      AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float modifyTime, LivingEntityPatch<?> entitypatch
   ) {
      super(action, animation, modifyTime, entitypatch);
      Vec3 position = entitypatch.getOriginal().m_20182_();
      this.posX = position.f_82479_;
      this.posY = position.f_82480_;
      this.posZ = position.f_82481_;
      this.yRot = entitypatch.getOriginal().f_19859_;
   }

   @Override
   public void onArrive() {
      super.onArrive();
      Minecraft mc = Minecraft.m_91087_();
      Entity entity = mc.f_91074_.m_9236_().m_6815_(this.entityId);
      entity.m_6034_(this.posX, this.posY, this.posZ);
      entity.m_146922_(this.yRot);
      entity.f_19854_ = entity.m_20185_();
      entity.f_19855_ = entity.m_20186_();
      entity.f_19856_ = entity.m_20189_();
      entity.f_19790_ = entity.m_20185_();
      entity.f_19791_ = entity.m_20186_();
      entity.f_19792_ = entity.m_20189_();
      entity.f_19859_ = this.yRot;
   }

   public static SPMoveAndPlayAnimation fromBytes(FriendlyByteBuf buf) {
      return new SPMoveAndPlayAnimation(
         (AnimatorControlPacket.Action)buf.m_130066_(AnimatorControlPacket.Action.class),
         buf.readInt(),
         buf.readInt(),
         buf.readFloat(),
         buf.readBoolean(),
         buf.readInt(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readFloat()
      );
   }

   public static void toBytes(SPMoveAndPlayAnimation msg, FriendlyByteBuf buf) {
      buf.m_130068_(msg.action);
      buf.writeInt(msg.animationId);
      buf.writeInt(msg.entityId);
      buf.writeFloat(msg.transitionTimeModifier);
      buf.writeBoolean(msg.pause);
      buf.writeInt(msg.targetId);
      buf.writeDouble(msg.posX);
      buf.writeDouble(msg.posY);
      buf.writeDouble(msg.posZ);
      buf.writeFloat(msg.yRot);
   }

   public static void handler(SPMoveAndPlayAnimation msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> msg.onArrive());
      ctx.get().setPacketHandled(true);
   }
}
