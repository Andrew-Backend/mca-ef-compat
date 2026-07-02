package yesman.epicfight.network.client;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPModifySkillData;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public record CPModifySkillData(SkillDataKey<?> dataKey, SkillSlot slot, Object value) {
   public static CPModifySkillData fromBytes(FriendlyByteBuf buf) {
      SkillDataKey<?> dataKey = (SkillDataKey<?>)buf.readRegistryId();
      SkillSlot slot = SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt());
      Object value = dataKey.readFromBuffer(buf);
      return new CPModifySkillData(dataKey, slot, value);
   }

   public static void toBytes(CPModifySkillData msg, FriendlyByteBuf buf) {
      buf.writeRegistryId(SkillDataKeys.REGISTRY.get(), msg.dataKey);
      buf.writeInt(msg.slot.universalOrdinal());
      ((SkillDataKey<Object>)msg.dataKey).writeToBuffer(buf, msg.value);
   }

   public static void handle(CPModifySkillData msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof ServerPlayerPatch playerpatch) {
            SkillDataManager dataManager = playerpatch.getSkill(msg.slot).getDataManager();
            dataManager.setDataRawtype(msg.dataKey, msg.value);
            if (msg.dataKey.syncronizeToTrackingPlayers()) {
               SPModifySkillData syncToOtherClientsPacket = new SPModifySkillData(msg.dataKey, msg.slot, msg.value, playerpatch.getOriginal().m_19879_());
               EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(syncToOtherClientsPacket, playerpatch.getOriginal());
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
