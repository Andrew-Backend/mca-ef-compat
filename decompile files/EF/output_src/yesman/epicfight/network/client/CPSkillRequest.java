package yesman.epicfight.network.client;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPSkillRequest {
   private final SkillSlot skillSlot;
   private final CPSkillRequest.WorkType workType;
   private final FriendlyByteBuf buffer;

   public CPSkillRequest(SkillSlot skillSlot) {
      this(skillSlot, CPSkillRequest.WorkType.CAST);
   }

   public CPSkillRequest(SkillSlot skillSlot, CPSkillRequest.WorkType active) {
      this.skillSlot = skillSlot;
      this.workType = active;
      this.buffer = new FriendlyByteBuf(Unpooled.buffer());
   }

   public CPSkillRequest(SkillSlot skillSlot, CPSkillRequest.WorkType active, FriendlyByteBuf pb) {
      this.skillSlot = skillSlot;
      this.workType = active;
      this.buffer = new FriendlyByteBuf(Unpooled.buffer());
      if (pb != null) {
         this.buffer.writeBytes(pb);
      }
   }

   public FriendlyByteBuf getBuffer() {
      return this.buffer;
   }

   public static CPSkillRequest fromBytes(FriendlyByteBuf buf) {
      CPSkillRequest msg = new CPSkillRequest(
         SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()), (CPSkillRequest.WorkType)buf.m_130066_(CPSkillRequest.WorkType.class)
      );

      while (buf.isReadable()) {
         msg.buffer.writeByte(buf.readByte());
      }

      return msg;
   }

   public static void toBytes(CPSkillRequest msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.skillSlot.universalOrdinal());
      buf.m_130068_(msg.workType);

      while (msg.buffer.isReadable()) {
         buf.writeByte(msg.buffer.readByte());
      }
   }

   public static void handle(CPSkillRequest msg, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(() -> EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
            SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
            switch (msg.workType) {
               case CAST:
                  skillContainer.requestCasting(playerpatch, msg.getBuffer());
                  break;
               case CANCEL:
                  skillContainer.requestCancel(playerpatch, msg.getBuffer());
                  break;
               case HOLD_START:
                  skillContainer.requestHold(playerpatch, msg.getBuffer());
            }
         }));
      ctx.get().setPacketHandled(true);
   }

   public enum WorkType {
      CAST,
      CANCEL,
      HOLD_START;
   }
}
