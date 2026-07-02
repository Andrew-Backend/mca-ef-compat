package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.event.EventContext;

@FunctionalInterface
public interface ContextAwareEventSubscription<T extends Event & CancelableEvent> extends EventSubscription<T> {
   void fire(T var1, EventContext var2);
}
