package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.EventSubscription;

public record EventListener<T extends Event>(String name, EventSubscription<T> subscription) {
}
