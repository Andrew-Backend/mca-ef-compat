package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPPlayAnimationAndSetTarget extends SPAnimatorControl {
   protected int targetId;

   public SPPlayAnimationAndSetTarget(AnimatorControlPacket.Action action, int animationId, int entityId, float modifyTime, boolean paused, int targetId) {
      super(action, animationId, entityId, modifyTime, paused);
      this.targetId = targetId;
   }

   public SPPlayAnimationAndSetTarget(
      AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float modifyTime, LivingEntityPatch<?> entitypatch
   ) {
      super(action, animation, modifyTime, entitypatch);
      this.targetId = entitypatch.getTarget().m_19879_();
   }

   @Override
   public void onArrive() {
      super.onArrive();
      Minecraft mc = Minecraft.m_91087_();
      Entity entity = mc.f_91074_.m_9236_().m_6815_(this.entityId);
      Entity target = mc.f_91074_.m_9236_().m_6815_(this.targetId);
      if (entity instanceof Mob entityliving && target instanceof LivingEntity) {
         entityliving.m_6710_((LivingEntity)target);
      }
   }

   public static SPPlayAnimationAndSetTarget fromBytes(FriendlyByteBuf buf) {
      return new SPPlayAnimationAndSetTarget(
         (AnimatorControlPacket.Action)buf.m_130066_(AnimatorControlPacket.Action.class),
         buf.readInt(),
         buf.readInt(),
         buf.readFloat(),
         buf.readBoolean(),
         buf.readInt()
      );
   }

   public static void toBytes(SPPlayAnimationAndSetTarget msg, FriendlyByteBuf buf) {
      buf.m_130068_(msg.action);
      buf.writeInt(msg.animationId);
      buf.writeInt(msg.entityId);
      buf.writeFloat(msg.transitionTimeModifier);
      buf.writeBoolean(msg.pause);
      buf.writeInt(msg.targetId);
   }

   public static void handle(SPPlayAnimationAndSetTarget msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> msg.onArrive());
      ctx.get().setPacketHandled(true);
   }
}
