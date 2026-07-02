package fabric.net.mca.entity.ai;

import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_1297;
import net.minecraft.class_5250;

public class ConversationManager {
   private final VillagerEntityMCA entity;
   private final Queue<ConversationManager.Message> pendingMessages = new ConcurrentLinkedQueue<>();

   public ConversationManager(VillagerEntityMCA entity) {
      this.entity = entity;
   }

   public void addMessage(class_1297 receiver, class_5250 message) {
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
      private final class_1297 receiver;
      VillagerEntityMCA entity;
      public final int validUntil;
      public static final int TIME_VALID = 6000;
      private boolean delivered = false;

      private Message(class_1297 receiver) {
         this.receiver = receiver;
         this.validUntil = receiver.field_6012 + 6000;
      }

      public class_1297 getReceiver() {
         return this.receiver;
      }

      public void deliver() {
         this.delivered = true;
      }

      public boolean stillValid() {
         return !this.delivered && !this.receiver.method_31481() && this.receiver.field_6012 < this.validUntil;
      }
   }

   public static class PhraseText extends ConversationManager.Message {
      private final String text;

      public PhraseText(class_1297 receiver, String text) {
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
      private final class_5250 text;

      public TextMessage(class_1297 receiver, class_5250 text) {
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
