package yesman.epicfight.api.event;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class EventContext {
   private List<String> calledBy = new ArrayList<>();
   private List<String> canceledBy = new ArrayList<>();
   @Nullable
   private String currentSubscriber;

   public boolean isCanceled() {
      return !this.canceledBy.isEmpty();
   }

   public boolean isCanceledBy(String name) {
      return this.canceledBy.contains(name);
   }

   public boolean hasCalledBy(String name) {
      return this.calledBy.contains(name);
   }

   @Internal
   public void onCalled() {
      this.calledBy.add(this.currentSubscriber);
   }

   @Internal
   public void onCanceled() {
      this.canceledBy.add(this.currentSubscriber);
   }

   @Internal
   public void subscriptionStart(String name) {
      this.currentSubscriber = name;
   }

   @Internal
   public void subscriptionEnd() {
      this.currentSubscriber = null;
   }
}
