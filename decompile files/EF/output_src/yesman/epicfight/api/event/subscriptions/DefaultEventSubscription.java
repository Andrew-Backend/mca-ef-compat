package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.Event;

@FunctionalInterface
public interface DefaultEventSubscription<T extends Event> extends EventSubscription<T> {
   void fire(T var1);
}
