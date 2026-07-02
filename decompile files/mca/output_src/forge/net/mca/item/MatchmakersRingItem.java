package forge.net.mca.item;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.util.WorldUtils;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class MatchmakersRingItem extends Item implements SpecialCaseGift {
   public MatchmakersRingItem(Properties properties) {
      super(properties);
   }

   @Override
   public boolean handle(ServerPlayer player, VillagerEntityMCA villager) {
      if (player.m_21205_().m_41613_() < 2) {
         villager.sendChatMessage(player, "interaction.matchmaker.fail.needtwo");
         return false;
      }

      if (!villager.getRelationships().isMarried() && villager.getAgeState() == AgeState.ADULT) {
         Optional<VillagerEntityMCA> target = WorldUtils.getCloseEntities(villager.m_9236_(), villager, 5.0)
            .stream()
            .filter(v -> v != villager && v instanceof VillagerEntityMCA)
            .map(VillagerEntityMCA.class::cast)
            .filter(v -> !v.m_6162_() && !v.getRelationships().isMarried())
            .filter(v -> !v.getRelationships().getFamilyEntry().isRelative(villager.m_20148_()))
            .filter(villager::canBeAttractedTo)
            .min(Comparator.comparingDouble(villager::m_20270_));
         if (target.isEmpty()) {
            villager.sendChatMessage(player, "interaction.matchmaker.fail.novillagers");
            return false;
         }

         VillagerEntityMCA spouse = target.get();
         villager.getRelationships().marry(spouse);
         spouse.getRelationships().marry(villager);
         player.m_9236_().m_7605_(villager, (byte)12);
         if (!player.m_7500_()) {
            player.m_21205_().m_41774_(1);
         }

         return true;
      } else {
         villager.sendChatMessage(player, "interaction.matchmaker.fail.married");
         return false;
      }
   }
}
