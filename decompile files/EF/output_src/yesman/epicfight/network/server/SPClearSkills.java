package yesman.epicfight.network.server;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public record SPClearSkills(int entityId) {
   public static SPClearSkills fromBytes(FriendlyByteBuf buf) {
      return new SPClearSkills(buf.readInt());
   }

   public static void toBytes(SPClearSkills msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.entityId());
   }

   public static void handle(SPClearSkills msg, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               Entity entity = Minecraft.m_91087_().f_91073_.m_6815_(msg.entityId());
               EpicFightCapabilities.getPlayerPatchAsOptional(entity)
                  .ifPresent(playerpatch -> playerpatch.getSkillCapability().clearContainersAndLearnedSkills(playerpatch.getOriginal().m_7578_()));
            }
         );
      ctx.get().setPacketHandled(true);
   }
}
