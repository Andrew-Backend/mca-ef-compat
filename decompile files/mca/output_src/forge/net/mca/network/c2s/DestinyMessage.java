package forge.net.mca.network.c2s;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.util.WorldUtils;
import forge.net.mca.util.compat.ExtendedFuzzyPositions;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class DestinyMessage implements Message {
   private static final long serialVersionUID = -782119062565197963L;
   private final String location;
   private final boolean isClosing;

   public DestinyMessage(String location, boolean isClosing) {
      this.location = location;
      this.isClosing = isClosing;
   }

   public DestinyMessage(String location) {
      this(location, false);
   }

   public DestinyMessage(boolean isClosing) {
      this(null, isClosing);
   }

   @Override
   public void receive(ServerPlayer player) {
      if (this.isClosing) {
         player.m_21195_(MobEffects.f_19609_);
         player.m_21195_(MobEffects.f_19616_);
      }

      if (Config.getInstance().allowDestinyTeleportation && this.location != null) {
         MCA.executorService
            .execute(
               () -> {
                  if (this.location.charAt(0) == '#') {
                     String tagId = this.location.substring(1);
                     WorldUtils.getClosestStructurePosition(
                           player.m_284548_(), player.m_20183_(), TagKey.m_203882_(Registries.f_256944_, new ResourceLocation(tagId)), 128
                        )
                        .ifPresent(pos -> this.handleBlockPos(player, pos));
                  } else {
                     WorldUtils.getClosestStructurePosition(player.m_284548_(), player.m_20183_(), new ResourceLocation(this.location), 128)
                        .ifPresent(pos -> this.handleBlockPos(player, pos));
                  }
               }
            );
      }
   }

   private void handleBlockPos(ServerPlayer player, BlockPos pos) {
      player.m_9236_().m_46745_(pos);
      if (this.location.equals("minecraft:ancient_city")) {
         pos = new BlockPos(pos.m_123341_(), -50, pos.m_123343_());
      } else {
         pos = player.m_9236_().m_5452_(Types.WORLD_SURFACE, pos);
      }

      pos = RandomPos.m_148545_(pos, player.m_9236_().m_141928_(), p -> player.m_9236_().m_8055_(p).m_60828_(player.m_9236_(), p));
      pos = ExtendedFuzzyPositions.downWhile(pos, 1, p -> !player.m_9236_().m_8055_(p.m_7495_()).m_60838_(player.m_9236_(), p));
      ChunkPos chunkPos = new ChunkPos(pos);
      player.m_284548_().m_7726_().m_8387_(TicketType.f_9448_, chunkPos, 1, player.m_19879_());
      player.f_8906_.m_9780_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), player.m_146908_(), player.m_146909_(), EnumSet.noneOf(RelativeMovement.class));
      player.m_9158_(player.m_9236_().m_46472_(), pos, 0.0F, true, false);
      if (player.m_9236_().m_7654_() != null && player.m_9236_().m_7654_().m_7779_(player.m_36316_())) {
         player.m_284548_().m_8733_(pos, 0.0F);
      }
   }
}
