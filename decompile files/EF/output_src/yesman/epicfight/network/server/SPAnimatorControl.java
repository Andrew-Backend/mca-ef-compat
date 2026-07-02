package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPAnimatorControl extends AnimatorControlPacket {
   protected int entityId;
   protected AnimatorControlPacket.Layer layer = AnimatorControlPacket.Layer.ANIMATION;
   protected AnimatorControlPacket.Priority priority = AnimatorControlPacket.Priority.ANIMATION;

   public SPAnimatorControl(
      AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch<?> entitypatch
   ) {
      this(action, animation.get().getId(), entitypatch.getOriginal().m_19879_(), transitionTimeModifier, false);
   }

   public SPAnimatorControl(
      AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause
   ) {
      this(action, animation.get().getId(), entityId, transitionTimeModifier, pause);
   }

   public SPAnimatorControl(AnimatorControlPacket.Action action, int animationId, int entityId, float transitionTimeModifier, boolean pause) {
      super(action, animationId, transitionTimeModifier, pause);
      this.entityId = entityId;
   }

   public SPAnimatorControl(
      AnimatorControlPacket.Action action,
      AssetAccessor<? extends StaticAnimation> animation,
      float transitionTimeModifier,
      LivingEntityPatch<?> entitypatch,
      AnimatorControlPacket.Layer layer,
      AnimatorControlPacket.Priority priority
   ) {
      this(action, animation.get().getId(), entitypatch.getOriginal().m_19879_(), transitionTimeModifier, false);
      this.layer = layer;
      this.priority = priority;
   }

   public SPAnimatorControl(
      AnimatorControlPacket.Action action,
      int animationId,
      int entityId,
      float transitionTimeModifier,
      boolean pause,
      AnimatorControlPacket.Layer layer,
      AnimatorControlPacket.Priority priority
   ) {
      super(action, animationId, transitionTimeModifier, pause);
      this.entityId = entityId;
      this.layer = layer;
      this.priority = priority;
   }

   public <T extends SPAnimatorControl> void onArrive() {
      EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.m_91087_().f_91073_.m_6815_(this.entityId), LivingEntityPatch.class)
         .ifPresent(
            entitypatch -> {
               if (this.action == AnimatorControlPacket.Action.PLAY_CLIENT
                  && this.layer != AnimatorControlPacket.Layer.ANIMATION
                  && this.priority != AnimatorControlPacket.Priority.ANIMATION) {
                  entitypatch.getClientAnimator()
                     .playAnimationAt(AnimationManager.byId(this.animationId), this.transitionTimeModifier, this.layer, this.priority);
               } else {
                  this.process((LivingEntityPatch<?>)entitypatch);
               }
            }
         );
   }

   public static SPAnimatorControl fromBytes(FriendlyByteBuf buf) {
      return new SPAnimatorControl(
         (AnimatorControlPacket.Action)buf.m_130066_(AnimatorControlPacket.Action.class),
         buf.readInt(),
         buf.readInt(),
         buf.readFloat(),
         buf.readBoolean(),
         (AnimatorControlPacket.Layer)buf.m_130066_(AnimatorControlPacket.Layer.class),
         (AnimatorControlPacket.Priority)buf.m_130066_(AnimatorControlPacket.Priority.class)
      );
   }

   public static void toBytes(SPAnimatorControl msg, FriendlyByteBuf buf) {
      buf.m_130068_(msg.action);
      buf.writeInt(msg.animationId);
      buf.writeInt(msg.entityId);
      buf.writeFloat(msg.transitionTimeModifier);
      buf.writeBoolean(msg.pause);
      buf.m_130068_(msg.layer);
      buf.m_130068_(msg.priority);
   }

   public static void handle(SPAnimatorControl msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> msg.onArrive());
      ctx.get().setPacketHandled(true);
   }
}
