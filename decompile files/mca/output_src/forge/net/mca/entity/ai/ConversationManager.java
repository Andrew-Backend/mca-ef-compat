package forge.net.mca.entity.ai;

import forge.net.mca.entity.VillagerEntityMCA;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

public class ConversationManager {
   private final VillagerEntityMCA entity;
   private final Queue<ConversationManager.Message> pendingMessages = new ConcurrentLinkedQueue<>();

   public ConversationManager(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public void addMessage(Entity receiver, MutableComponent message) {
      this.addMessage(new ConversationManager.TextMessage(receiver, message));
   }

   public void addMessage(ConversationManager.Message message) {
      this.pendingMessages.add(message);
      message.entity = this.entity;
   }

   public Optional<ConversationManager.Message> getCurrentMessage() {
      ConversationManager.Message message = this.pendingMessages.peek();
      if (message == null) {
         return Optional.empty();
      }

      if (message.stillValid()) {
         return Optional.of(message);
      }

      this.pendingMessages.remove(message);
      return this.getCurrentMessage();
   }

   public abstract static class Message {
      private final Entity receiver;
      VillagerEntityMCA entity;
      public final int validUntil;
      public static final int TIME_VALID = 6000;
      private boolean delivered = false;

      private Message(Entity receiver) {
         this.receiver = receiver;
         this.validUntil = receiver.f_19797_ + 6000;
      }

      public Entity getReceiver() {
         return this.receiver;
      }

      public void deliver() {
         this.delivered = true;
      }

      public boolean stillValid() {
         return !this.delivered && !this.receiver.m_213877_() && this.receiver.f_19797_ < this.validUntil;
      }
   }

   public static class PhraseText extends ConversationManager.Message {
      private final String text;

      public PhraseText(Entity receiver, String text) {
         super(receiver);
         this.text = text;
      }

      @Override
      public void deliver() {
         this.entity.sendChatToAllAround(this.text);
         super.deliver();
      }
   }

   public static class TextMessage extends ConversationManager.Message {
      private final MutableComponent text;

      public TextMessage(Entity receiver, MutableComponent text) {
         super(receiver);
         this.text = text;
      }

      @Override
      public void deliver() {
         this.entity.sendChatToAllAround(this.text);
         super.deliver();
      }
   }
}
