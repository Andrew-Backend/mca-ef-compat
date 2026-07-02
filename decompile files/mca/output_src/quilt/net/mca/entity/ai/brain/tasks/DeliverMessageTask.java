package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.ConversationManager;
import quilt.net.mca.entity.ai.MemoryModuleTypeMCA;

public class DeliverMessageTask extends class_4097<VillagerEntityMCA> {
   private static final int TALKING_TIME_MIN = 100;
   private static final int TALKING_TIME_MAX = 500;
   private static final long MIN_TIME_BETWEEN_SOUND = 6000L;
   private ConversationManager.Message message = null;
   private class_1297 receiver = null;
   private int talked;
   private long lastInteraction = Long.MIN_VALUE;
   private class_243 lastInteractionPos;

   public DeliverMessageTask() {
      super(
         ImmutableMap.of(
            class_4140.field_18445, class_4141.field_18458, class_4140.field_18446, class_4141.field_18458, class_4140.field_18447, class_4141.field_18458
         ),
         600
      );
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      if (villager.method_18868().method_46873(class_4140.field_22355).isPresent()) {
         return false;
      }

      Optional<ConversationManager.Message> optionalMessage = getMessage(villager);
      optionalMessage.ifPresent(m -> {
         this.message = m;
         this.receiver = this.message.getReceiver();
      });
      return optionalMessage.isPresent() && isWithinSeeRange(villager, this.receiver);
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      this.talked = 0;
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.message != null
         && this.talked < this.getMaxTalkingTime()
         && villager.method_18868().method_46873(class_4140.field_22355).isEmpty()
         && !villager.getVillagerBrain().isPanicking()
         && !villager.method_6113();
   }

   private int getMaxTalkingTime() {
      if (this.lastInteractionPos != null) {
         class_243 pos = this.receiver.method_19538();
         if (this.lastInteractionPos.method_24802(pos, 1.0)) {
            return 500;
         }
      }

      return 100;
   }

   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      if (this.receiver instanceof class_1309 e) {
         villager.method_18868().method_18878(class_4140.field_18447, e);
         class_4215.method_19554(villager, e);
      }

      class_4215.method_24557(villager, this.receiver, 0.5F, 2);
      if (this.message.getReceiver() == this.receiver) {
         if (this.message.stillValid() && isWithinRange(villager, this.receiver)) {
            if (time - this.lastInteraction > 6000L) {
               villager.playWelcomeSound();
            }

            this.lastInteraction = time;
            this.lastInteractionPos = this.receiver.method_19538();
            this.message.deliver();
         } else if (!this.message.stillValid() && isWithinRange(villager, this.receiver)) {
            this.talked++;
            Optional<ConversationManager.Message> optionalMessage = getMessage(villager);
            optionalMessage.ifPresent(m -> {
               this.message = m;
               if (m.getReceiver() == this.receiver) {
                  this.talked = 0;
               }
            });
         }
      }
   }

   protected void finishRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      this.message = null;
      this.receiver = null;
      villager.method_18868().method_18875(class_4140.field_18447);
      villager.method_18868().method_18875(class_4140.field_18445);
      villager.method_18868().method_18875(class_4140.field_18446);
   }

   private static Optional<ConversationManager.Message> getMessage(VillagerEntityMCA villager) {
      return villager.conversationManager.getCurrentMessage();
   }

   private static boolean isWithinRange(VillagerEntityMCA villager, class_1297 player) {
      return villager.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.STAYING.get()).isPresent()
         ? true
         : villager.method_24515().method_19771(player.method_24515(), 3.0);
   }

   private static boolean isWithinSeeRange(VillagerEntityMCA villager, class_1297 player) {
      return villager.method_24515().method_19771(player.method_24515(), 64.0);
   }
}
