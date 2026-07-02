package fabric.net.mca.entity.ai.chatAI.inworldAIModules;

import com.google.gson.Gson;
import fabric.net.mca.MCA;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.Requests;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.Session;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;
import fabric.net.mca.server.world.data.PlayerSaveData;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_3222;

public class SessionModule {
   private static final String INWORLD_BASE_URL = "https://api.inworld.ai/v1/";
   private static final long SESSION_MAX_VALID_TIME = 1680000L;
   private static final int STATUS_UPDATE_FREQUENCY = 20;
   private final String characterResourceName;
   private final String workspaceID;
   private final Gson gson;
   private final Map<UUID, SessionModule.OpenSession> openSessionMap = new ConcurrentHashMap<>();

   public SessionModule(String characterResourceName) {
      this.characterResourceName = characterResourceName;
      this.workspaceID = characterResourceName.split("/")[1];
      this.gson = new Gson();
   }

   public Optional<Interaction> getResponse(class_3222 player, String msg, TriggerEvent statusUpdate) {
      long currentTime = System.currentTimeMillis();
      if (!this.openSessionIfNeeded(player, currentTime)) {
         return Optional.empty();
      }

      SessionModule.OpenSession openSession = this.openSessionMap.get(player.method_5667());
      if (openSession.getInteractionCount() % 20 == 0) {
         this.sendTriggerRequest(openSession.getSession(), statusUpdate, player.method_5667().toString());
      }

      Optional<Interaction> interactionOptional = this.sendTextRequest(openSession.getSession(), msg);
      if (interactionOptional.isPresent()) {
         openSession.updateSession(currentTime);
      }

      return interactionOptional;
   }

   private Optional<Session> openSessionRequest(String playerId, String playerName, String playerGender) {
      Requests.OpenSessionRequest.EndUserConfig config = new Requests.OpenSessionRequest.EndUserConfig(playerId, playerName, playerGender, null, null);
      Requests.OpenSessionRequest request = new Requests.OpenSessionRequest(this.characterResourceName, config);
      String requestBody = this.gson.toJson(request);
      String endpoint = "https://api.inworld.ai/v1/" + this.characterResourceName + ":openSession";
      Optional<String> response = Requests.makeRequest(endpoint, requestBody);
      if (response.isEmpty()) {
         return Optional.empty();
      }

      Session session = (Session)this.gson.fromJson(response.get(), Session.class);
      return Optional.of(session);
   }

   private Optional<Interaction> sendTextRequest(Session session, String message) {
      Requests.SendTextRequest request = new Requests.SendTextRequest(message);
      String requestBody = this.gson.toJson(request);
      String endpoint = "https://api.inworld.ai/v1/" + this.getSessionCharacterResourceName(session) + ":sendText";
      Optional<String> response = Requests.makeRequest(endpoint, requestBody, session.name());
      if (response.isEmpty()) {
         return Optional.empty();
      }

      Interaction data = (Interaction)this.gson.fromJson(response.get(), Interaction.class);
      return Optional.of(data);
   }

   private Optional<Interaction> sendTriggerRequest(Session session, TriggerEvent event, String endUserId) {
      Requests.SendTriggerRequest request = new Requests.SendTriggerRequest(event, endUserId);
      String requestBody = this.gson.toJson(request);
      String endpoint = "https://api.inworld.ai/v1/" + this.getSessionCharacterResourceName(session) + ":sendTrigger";
      Optional<String> response = Requests.makeRequest(endpoint, requestBody, session.name());
      if (response.isEmpty()) {
         return Optional.empty();
      }

      Interaction data = (Interaction)this.gson.fromJson(response.get(), Interaction.class);
      return Optional.of(data);
   }

   private String getSessionCharacterResourceName(Session session) {
      return "workspaces/%s/sessions/%s/sessionCharacters/%s".formatted(this.workspaceID, session.name(), session.sessionCharacters()[0].character());
   }

   private boolean openSessionIfNeeded(class_3222 player, long currentTime) {
      SessionModule.OpenSession openSession = this.openSessionMap.getOrDefault(player.method_5667(), new SessionModule.OpenSession(null, 0L));
      if (openSession.getSession() == null || currentTime - openSession.getLastInteractionMillis() > 1680000L) {
         Optional<Session> sessionOptional = this.openSessionRequest(
            player.method_5667().toString(), player.method_5477().getString(), PlayerSaveData.get(player).getGender().getDataName()
         );
         if (sessionOptional.isEmpty()) {
            MCA.LOGGER.error("Failed to open Inworld session. Consult logs");
            return false;
         }

         SessionModule.OpenSession newSession = new SessionModule.OpenSession(sessionOptional.get(), currentTime);
         this.openSessionMap.put(player.method_5667(), newSession);
      }

      return true;
   }

   private static class OpenSession {
      private final Session session;
      private long lastInteractionMillis;
      private int interactionCount;

      public OpenSession(Session session, long lastInteractionMillis) {
         this.session = session;
         this.lastInteractionMillis = lastInteractionMillis;
         this.interactionCount = 0;
      }

      public Session getSession() {
         return this.session;
      }

      public int getInteractionCount() {
         return this.interactionCount;
      }

      public long getLastInteractionMillis() {
         return this.lastInteractionMillis;
      }

      public void updateSession(long lastInteractionMillis) {
         this.lastInteractionMillis = lastInteractionMillis;
         this.interactionCount++;
      }
   }
}
