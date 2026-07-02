package fabric.net.mca.client.gui.immersive_library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.client.gui.immersive_library.responses.ErrorResponse;
import fabric.net.mca.client.gui.immersive_library.responses.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;

public class Api {
   private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create();

   public static Response request(Api.HttpMethod httpMethod, Class<? extends Response> expectedAnswer, String url) {
      return request(httpMethod, expectedAnswer, url, null, null);
   }

   public static Response request(Api.HttpMethod httpMethod, Class<? extends Response> expectedAnswer, String url, Map<String, String> queryParams) {
      return request(httpMethod, expectedAnswer, url, queryParams, null);
   }

   public static Response request(
      Api.HttpMethod httpMethod, Class<? extends Response> expectedAnswer, String url, Map<String, String> queryParams, Map<String, String> body
   ) {
      try {
         String fullUrl = Config.getInstance().immersiveLibraryUrl + (url.contains("v2") ? "/" : "/v1/") + url;
         if (queryParams != null) {
            fullUrl = queryParams.keySet()
               .stream()
               .map(key -> key + "=" + URLEncoder.encode(queryParams.get(key), StandardCharsets.UTF_8))
               .collect(Collectors.joining("&", fullUrl + "?", ""));
         }

         HttpURLConnection con = (HttpURLConnection)new URL(fullUrl).openConnection();
         con.setRequestMethod(httpMethod.name());
         con.setRequestProperty("Content-Type", "application/json");
         con.setRequestProperty("Accept-Encoding", "gzip");
         con.setRequestProperty("Accept", "application/json");
         if (Auth.hasToken()) {
            con.setRequestProperty("Authorization", "Bearer " + Auth.getToken());
         }

         if (body != null && !body.isEmpty()) {
            con.setDoOutput(true);
            Gson gson = new Gson();
            String jsonBody = gson.toJson(body);
            con.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));
         }

         if (con.getErrorStream() != null) {
            int responseCode = con.getResponseCode();
            String error = IOUtils.toString(con.getErrorStream(), StandardCharsets.UTF_8);
            JsonObject object = (JsonObject)Api.gson.fromJson(error, JsonObject.class);
            return new ErrorResponse(responseCode, object.get("message").getAsString());
         }

         String response;
         if ("gzip".equals(con.getContentEncoding())) {
            response = IOUtils.toString(new GZIPInputStream(con.getInputStream()), StandardCharsets.UTF_8);
         } else {
            response = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
         }

         return (Response)Api.gson.fromJson(response, expectedAnswer);
      } catch (IOException e) {
         MCA.LOGGER.warn(e);
         return new ErrorResponse(-1, e.toString());
      } catch (Exception e) {
         MCA.LOGGER.error(e);
         return new ErrorResponse(-1, e.toString());
      }
   }

   public enum HttpMethod {
      POST,
      GET,
      DELETE,
      PUT;
   }
}
