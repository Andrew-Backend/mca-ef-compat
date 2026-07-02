package quilt.net.mca.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import quilt.net.mca.entity.interaction.InteractionPredicate;

public class InteractionPredicateTypeAdapter implements JsonDeserializer<InteractionPredicate> {
   public static final InteractionPredicateTypeAdapter INSTANCE = new InteractionPredicateTypeAdapter();

   public InteractionPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return InteractionPredicate.fromJson(json.getAsJsonObject());
   }
}
