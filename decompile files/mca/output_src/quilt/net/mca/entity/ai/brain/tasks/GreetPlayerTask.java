package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.ConversationManager;
import quilt.net.mca.entity.ai.Memories;
import quilt.net.mca.entity.ai.Relationship;
import quilt.net.mca.server.world.data.PlayerSaveData;
import quilt.net.mca.server.world.data.Village;

public class GreetPlayerTask extends class_4097<VillagerEntityMCA> {
   private static final int MAX_COOLDOWN = 100;
   private int cooldown = 0;

   public GreetPlayerTask() {
      super(ImmutableMap.of(), 0);
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA entity) {
      this.cooldown--;
      return this.cooldown < 0 && entity.method_18868().method_46873(class_4140.field_22355).isEmpty();
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      this.cooldown = 100;
      getPlayer(villager).ifPresent(player -> {
         Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
         int day = (int)(villager.method_37908().method_8532() / 24000L);
         memories.setLastSeen(day);
         String phrase = memories.getHearts() < 0 ? "welcomeFoe" : "welcome";
         villager.conversationManager.addMessage(new ConversationManager.PhraseText(player, phrase));
      });
   }

   private static Optional<? extends class_1657> getPlayer(VillagerEntityMCA villager) {
      return ((class_3218)villager.method_37908())
         .method_18456()
         .stream()
         .filter(p -> isWithinSeeRange(villager, p))
         .filter(p -> shouldGreet(villager, p))
         .findFirst();
   }

   private static boolean shouldGreet(VillagerEntityMCA villager, class_3222 player) {
      Optional<Integer> id = PlayerSaveData.get(player).getLastSeenVillageId();
      Optional<Village> village = villager.getResidency().getHomeVillage();
      if (id.isPresent() && village.isPresent() && id.get() == village.get().getId()) {
         Memories memories = villager.getVillagerBrain().getMemoriesForPlayer(player);
         int day = (int)(villager.method_37908().method_8532() / 24000L);
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

   private static boolean isWithinSeeRange(VillagerEntityMCA villager, class_1657 player) {
      return villager.method_24515().method_19771(player.method_24515(), 32.0);
   }
}
