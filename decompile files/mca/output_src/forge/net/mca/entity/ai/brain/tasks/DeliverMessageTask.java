package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.ConversationManager;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class DeliverMessageTask extends Behavior<VillagerEntityMCA> {
   private static final int TALKING_TIME_MIN = 100;
   private static final int TALKING_TIME_MAX = 500;
   private static final long MIN_TIME_BETWEEN_SOUND = 6000L;
   private ConversationManager.Message message = null;
   private Entity receiver = null;
   private int talked;
   private long lastInteraction = Long.MIN_VALUE;
   private Vec3 lastInteractionPos;

   public DeliverMessageTask() {
      super(
         ImmutableMap.of(
            MemoryModuleType.f_26370_,
            MemoryStatus.REGISTERED,
            MemoryModuleType.f_26371_,
            MemoryStatus.REGISTERED,
            MemoryModuleType.f_26374_,
            MemoryStatus.REGISTERED
         ),
         600
      );
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      if (villager.m_6274_().m_257414_(MemoryModuleType.f_26372_).isPresent()) {
         return false;
      }

      Optional<ConversationManager.Message> optionalMessage = getMessage(villager);
      optionalMessage.ifPresent(m -> {
         this.message = m;
         this.receiver = this.message.getReceiver();
      });
      return optionalMessage.isPresent() && isWithinSeeRange(villager, this.receiver);
   }

   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      this.talked = 0;
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.message != null
         && this.talked < this.getMaxTalkingTime()
         && villager.m_6274_().m_257414_(MemoryModuleType.f_26372_).isEmpty()
         && !villager.getVillagerBrain().isPanicking()
         && !villager.m_5803_();
   }

   private int getMaxTalkingTime() {
      if (this.lastInteractionPos != null) {
         Vec3 pos = this.receiver.m_20182_();
         if (this.lastInteractionPos.m_82509_(pos, 1.0)) {
            return 500;
         }
      }

      return 100;
   }

   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      if (this.receiver instanceof LivingEntity e) {
         villager.m_6274_().m_21879_(MemoryModuleType.f_26374_, e);
         BehaviorUtils.m_22595_(villager, e);
      }

      BehaviorUtils.m_22590_(villager, this.receiver, 0.5F, 2);
      if (this.message.getReceiver() == this.receiver) {
         if (this.message.stillValid() && isWithinRange(villager, this.receiver)) {
            if (time - this.lastInteraction > 6000L) {
               villager.playWelcomeSound();
            }

            this.lastInteraction = time;
            this.lastInteractionPos = this.receiver.m_20182_();
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

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      this.message = null;
      this.receiver = null;
      villager.m_6274_().m_21936_(MemoryModuleType.f_26374_);
      villager.m_6274_().m_21936_(MemoryModuleType.f_26370_);
      villager.m_6274_().m_21936_(MemoryModuleType.f_26371_);
   }

   private static Optional<ConversationManager.Message> getMessage(VillagerEntityMCA villager) {
      return villager.conversationManager.getCurrentMessage();
   }

   private static boolean isWithinRange(VillagerEntityMCA villager, Entity player) {
      return villager.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get()).isPresent()
         ? true
         : villager.m_20183_().m_123314_(player.m_20183_(), 3.0);
   }

   private static boolean isWithinSeeRange(VillagerEntityMCA villager, Entity player) {
      return villager.m_20183_().m_123314_(player.m_20183_(), 64.0);
   }
}
