package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class SPPotion {
   private final MobEffectInstance effectInstance;
   private final SPPotion.Action action;
   private final int entityId;

   public SPPotion() {
      this.effectInstance = null;
      this.entityId = 0;
      this.action = SPPotion.Action.REMOVE;
   }

   public SPPotion(MobEffectInstance effect, SPPotion.Action action, int entityId) {
      this.effectInstance = effect;
      this.entityId = entityId;
      this.action = action;
   }

   public static SPPotion fromBytes(FriendlyByteBuf buf) {
      MobEffect effect = MobEffect.m_19453_(buf.readInt());
      MobEffectInstance effectInstance = new MobEffectInstance(effect, 0, buf.readInt());
      int entityId = buf.readInt();
      SPPotion.Action action = (SPPotion.Action)buf.m_130066_(SPPotion.Action.class);
      return new SPPotion(effectInstance, action, entityId);
   }

   public static void toBytes(SPPotion msg, FriendlyByteBuf buf) {
      buf.writeInt(MobEffect.m_19459_(msg.effectInstance.m_19544_()));
      buf.writeInt(msg.effectInstance.m_19564_());
      buf.writeInt(msg.entityId);
      buf.m_130068_(msg.action);
   }

   public static void handle(SPPotion msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         Minecraft mc = Minecraft.m_91087_();
         Entity entity = mc.f_91073_.m_6815_(msg.entityId);
         if (entity != null && entity instanceof LivingEntity livEntity) {
            switch (msg.action) {
               case ACTIVATE:
                  livEntity.m_7292_(msg.effectInstance);
                  break;
               case REMOVE:
                  livEntity.m_21195_(msg.effectInstance.m_19544_());
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }

   public enum Action {
      ACTIVATE,
      REMOVE;
   }
}
