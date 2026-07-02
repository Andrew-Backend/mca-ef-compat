package yesman.epicfight.api.event;

import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class Event {
   private final EventContext eventContext = new EventContext();

   public boolean hasCanceled() {
      return this instanceof CancelableEvent ? this.eventContext.isCanceled() : false;
   }

   public void cancel() {
      if (!(this instanceof CancelableEvent)) {
         throw new IllegalStateException("Unable to cancel a non cancelable event");
      }

      this.eventContext.onCanceled();
   }

   @Internal
   public EventContext getEventContext() {
      return this.eventContext;
   }
}
