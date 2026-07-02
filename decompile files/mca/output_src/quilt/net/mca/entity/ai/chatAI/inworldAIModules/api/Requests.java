package quilt.net.mca.entity.ai.chatAI.inworldAIModules.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;

public class Requests {
   public static Optional<String> makeRequest(String urlString, String body) {
      return makeRequest(urlString, body, "");
   }

   public static Optional<String> makeRequest(String urlString, String body, String sessionIDAuth) {
      String responseString = "No response";

      try {
         URL url = new URL(urlString);
         HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/json");
         con.setRequestProperty("authorization", "Basic " + Config.getInstance().inworldAIToken);
         if (!sessionIDAuth.isEmpty()) {
            con.setRequestProperty("Grpc-Metadata-session-id", sessionIDAuth);
         }

         con.setDoOutput(true);

         try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(body.getBytes(StandardCharsets.UTF_8));
            wr.flush();
         }

         InputStream var12 = con.getInputStream();
         responseString = new String(var12.readAllBytes(), StandardCharsets.UTF_8);
         return Optional.of(responseString);
      } catch (IOException e) {
         MCA.LOGGER.error("InworldAI: Sending %s to %s".formatted(body, urlString));
         MCA.LOGGER.error("InworldAI: Received %s".formatted(responseString));
         MCA.LOGGER.error(e);
         return Optional.empty();
      }
   }

   public record OpenSessionRequest(String name, Requests.OpenSessionRequest.EndUserConfig user) {
      public record EndUserConfig(String endUserId, String givenName, String gender, String role, Long age) {
      }
   }

   public record SendTextRequest(String text) {
   }

   public record SendTriggerRequest(TriggerEvent triggerEvent, String endUserId) {
   }
}
