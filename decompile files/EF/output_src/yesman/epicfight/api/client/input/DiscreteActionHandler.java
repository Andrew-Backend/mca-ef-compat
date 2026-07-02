package yesman.epicfight.api.client.input;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DiscreteActionHandler {
   void onAction(@NotNull DiscreteActionHandler.Context var1);

   record Context(boolean triggeredByController) {
   }
}
