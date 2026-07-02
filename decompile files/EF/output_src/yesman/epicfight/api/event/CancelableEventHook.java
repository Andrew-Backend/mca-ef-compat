package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.ContextAwareEventSubscription;
import yesman.epicfight.api.event.subscriptions.DefaultEventSubscription;

public class CancelableEventHook<T extends Event & CancelableEvent> extends EventHook<T> {
   @Override
   public boolean post(T event) {
      EventContext eventContext = event.getEventContext();

      for (EventListener<T> subscriber : this.subscriptions.values()) {
         eventContext.subscriptionStart(subscriber.name());
         if (subscriber.subscription() instanceof DefaultEventSubscription<T> passiveSubscription) {
            if (!event.hasCanceled()) {
               passiveSubscription.fire(event);
               eventContext.onCalled();
            }
         } else if (subscriber.subscription() instanceof ContextAwareEventSubscription<T> contextAwareSubscription) {
            contextAwareSubscription.fire(event, eventContext);
            eventContext.onCalled();
         }
      }

      eventContext.subscriptionEnd();
      return event.hasCanceled();
   }

   public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription) {
      this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), 0);
   }

   public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, int priority) {
      this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), priority);
   }

   public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name) {
      this.registerContextAwareEvent(subscription, name, 0);
   }

   public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name, int priority) {
      this.subscriptions.put(priority, new EventListener<>(name, subscription));
   }

   public static <T extends Event & CancelableEvent> CancelableEventHook<T> createCancelableEventHook() {
      return new CancelableEventHook<>();
   }
}
