package yesman.epicfight.api.event;

public interface CancelableEvent {
   boolean hasCanceled();

   void cancel();
}
