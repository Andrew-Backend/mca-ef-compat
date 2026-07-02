package yesman.epicfight.epicskins.exception;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HttpResponseException extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private int statusCode;
   private String responseBody;

   public HttpResponseException(String message, int statusCode, String responseBody) {
      super(message);
      this.statusCode = statusCode;
      this.responseBody = responseBody;
   }

   public int getHttpStatusCode() {
      return this.statusCode;
   }

   public String getResponseBody() {
      return this.responseBody;
   }
}
