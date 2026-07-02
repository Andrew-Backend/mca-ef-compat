package yesman.epicfight.network.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.api.animation.AnimationManager;

public class CPCheckAnimationRegistryMatches {
   public final int animationCount;
   public final String[] registryNames;

   public CPCheckAnimationRegistryMatches() {
      this.animationCount = 0;
      this.registryNames = new String[0];
   }

   public CPCheckAnimationRegistryMatches(int animationCount, String[] registryNames) {
      this.animationCount = animationCount;
      this.registryNames = registryNames;
   }

   public static CPCheckAnimationRegistryMatches fromBytes(FriendlyByteBuf buf) {
      int animationCount = buf.readInt();
      String[] registryNames = new String[animationCount];

      for (int i = 0; i < animationCount; i++) {
         registryNames[i] = buf.m_130277_();
      }

      return new CPCheckAnimationRegistryMatches(animationCount, registryNames);
   }

   public static void toBytes(CPCheckAnimationRegistryMatches msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.animationCount);

      for (String registryName : msg.registryNames) {
         buf.m_130070_(registryName);
      }
   }

   public static void handle(CPCheckAnimationRegistryMatches msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> AnimationManager.getInstance().validateClientAnimationRegistry(msg, ctx.get().getSender().f_8906_));
      ctx.get().setPacketHandled(true);
   }
}
