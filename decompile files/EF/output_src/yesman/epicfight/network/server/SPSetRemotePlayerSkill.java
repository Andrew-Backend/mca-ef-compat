package yesman.epicfight.network.server;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public record SPSetRemotePlayerSkill(int entityId, SkillSlot slot, @Nullable Skill skill) {
   public static SPSetRemotePlayerSkill fromBytes(FriendlyByteBuf buf) {
      return new SPSetRemotePlayerSkill(buf.readInt(), SkillSlot.ENUM_MANAGER.get(buf.readInt()), buf.isReadable() ? (Skill)buf.readRegistryId() : null);
   }

   public static void toBytes(SPSetRemotePlayerSkill msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.entityId());
      buf.writeInt(msg.slot().universalOrdinal());
      if (msg.skill() != null) {
         buf.writeRegistryId(RegistryManager.ACTIVE.getRegistry(SkillManager.SKILL_REGISTRY_KEY), msg.skill);
      }
   }

   public static void handle(SPSetRemotePlayerSkill msg, Supplier<Context> ctx) {
      ctx.get()
         .enqueueWork(
            () -> {
               Entity entity = Minecraft.m_91087_().f_91073_.m_6815_(msg.entityId());
               EpicFightCapabilities.getUnparameterizedEntityPatch(entity, AbstractClientPlayerPatch.class)
                  .ifPresent(playerpatch -> playerpatch.getSkill(msg.slot()).setSkillRemote(msg.skill()));
            }
         );
      ctx.get().setPacketHandled(true);
   }
}
