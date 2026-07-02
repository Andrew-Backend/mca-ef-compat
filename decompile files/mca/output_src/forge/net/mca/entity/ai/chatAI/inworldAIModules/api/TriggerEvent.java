package forge.net.mca.entity.ai.chatAI.inworldAIModules.api;

public record TriggerEvent(String trigger, TriggerEvent.Parameter[] parameters) {
   public record Parameter(String name, String value) {
   }
}
