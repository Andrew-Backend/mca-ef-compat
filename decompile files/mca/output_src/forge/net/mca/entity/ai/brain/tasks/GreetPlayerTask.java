package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.ConversationManager;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.Relationship;
import forge.net.mca.server.world.data.PlayerSaveData;
import forge.net.mca.server.world.data.Village;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class GreetPlayerTask extends Behavior<VillagerEntityMCA> {
   private static final int MAX_COOLDOWN = 100;
   private int cooldown = 0;

   public GreetPlayerTask() {
      super(ImmutableMap.of(), 0);
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA entity) {
      this.cooldown--;
      return this.cooldown < 0 && entity.m_6274_().m_257414_(MemoryModuleType.f_26372_).isEmpty();
   }

   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      this.cooldown = 100;
      getPlayer(villager).ifPresent(player -> {
         Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
         int day = (int)(villager.m_9236_().m_46468_() / 24000L);
         memories.setLastSeen(day);
         String phrase = memories.getHearts() < 0 ? "welcomeFoe" : "welcome";
         villager.conversationManager.addMessage(new ConversationManager.PhraseText(player, phrase));
      });
   }

   private static Optional<? extends Player> getPlayer(VillagerEntityMCA villager) {
      return ((ServerLevel)villager.m_9236_()).m_6907_().stream().filter(p -> isWithinSeeRange(villager, p)).filter(p -> shouldGreet(villager, p)).findFirst();
   }

   private static boolean shouldGreet(VillagerEntityMCA villager, ServerPlayer player) {
      Optional<Integer> id = PlayerSaveData.get(player).getLastSeenVillageId();
      Optional<Village> village = villager.getResidency().getHomeVillage();
      if (id.isPresent() && village.isPresent() && id.get() == village.get().getId()) {
         Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
         int day = (int)(villager.m_9236_().m_46468_() / 24000L);
         if (!Relationship.IS_MARRIED.test(villager, player)
            && !Relationship.IS_RELATIVE.test(villager, player)
            && Math.abs(memories.getHearts()) < Config.getInstance().greetHeartsThreshold) {
            memories.setLastSeen(day);
         } else {
            long diff = day - memories.getLastSeen();
            if (diff > Config.getInstance().greetAfterDays && memories.getLastSeen() > 0L) {
               return true;
            }

            if (diff > 0L) {
               memories.setLastSeen(day);
            }
         }
      }

      return false;
   }

   private static boolean isWithinSeeRange(VillagerEntityMCA villager, Player player) {
      return villager.m_20183_().m_123314_(player.m_20183_(), 32.0);
   }
}
