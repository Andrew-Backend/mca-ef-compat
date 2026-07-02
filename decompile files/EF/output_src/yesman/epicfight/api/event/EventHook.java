package yesman.epicfight.api.event;

import java.util.TreeMap;
import yesman.epicfight.api.event.subscriptions.DefaultEventSubscription;

public class EventHook<T extends Event> {
   final TreeMap<Integer, EventListener<T>> subscriptions = new TreeMap<>((i1, i2) -> Integer.compare(i2, i1));

   public boolean post(T eventInstance) {
      EventContext eventContext = new EventContext();

      for (EventListener<T> subscriber : this.subscriptions.values()) {
         eventContext.subscriptionStart(subscriber.name());
         if (subscriber.subscription() instanceof DefaultEventSubscription<T> passiveSubscription) {
            passiveSubscription.fire(eventInstance);
            eventContext.onCalled();
         }
      }

      eventContext.subscriptionEnd();
      return false;
   }

   public void registerEvent(DefaultEventSubscription<T> subscription) {
      this.registerEvent(subscription, getDefaultSubscriberName(), 0);
   }

   public void registerEvent(DefaultEventSubscription<T> subscription, int priority) {
      this.registerEvent(subscription, getDefaultSubscriberName(), priority);
   }

   public void registerEvent(DefaultEventSubscription<T> subscription, String name) {
      this.registerEvent(subscription, name, 0);
   }

   public void registerEvent(DefaultEventSubscription<T> subscription, String name, int priority) {
      this.subscriptions.put(priority, new EventListener<>(name, subscription));
   }

   protected static String getDefaultSubscriberName() {
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      StackTraceElement caller = stackTraceElements[2];
      return caller.getClassName();
   }

   public static <T extends Event> EventHook<T> createEventHook() {
      return new EventHook<>();
   }
}
