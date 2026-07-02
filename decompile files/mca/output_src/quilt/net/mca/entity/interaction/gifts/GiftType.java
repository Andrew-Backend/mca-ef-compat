package quilt.net.mca.entity.interaction.gifts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.resources.data.analysis.IntAnalysis;

public class GiftType {
   static final List<GiftType> REGISTRY = new ArrayList<>();
   private final class_2960 id;
   private int priority;
   private final List<GiftPredicate> conditions;
   private final Map<class_1792, Integer> items;
   private final Map<class_6862<class_1792>, Integer> tags;
   private int fail;
   private int good;
   private int better;
   private final Map<Response, String> responses;

   public static GiftType fromJson(class_2960 id, JsonObject json) {
      List<GiftPredicate> conditions = new ArrayList<>();
      class_3518.method_15292(json, "conditions", new JsonArray())
         .forEach(element -> conditions.add(GiftPredicate.fromJson(class_3518.method_15295(element, "condition"))));
      HashMap<class_1792, Integer> items = new HashMap<>();
      HashMap<class_6862<class_1792>, Integer> tags = new HashMap<>();
      class_3518.method_15296(json, "items").entrySet().forEach(element -> {
         String string = (String)element.getKey();
         Integer satisfaction = ((JsonElement)element.getValue()).getAsInt();
         if (string.charAt(0) == '#') {
            class_2960 identifier = new class_2960(string.substring(1));
            class_6862<class_1792> tag = class_6862.method_40092(class_7924.field_41197, identifier);
            if (tag != null) {
               tags.put(tag, satisfaction);
            } else if (identifier.method_12836().equals("mca")) {
               throw new JsonSyntaxException("Unknown item tag '" + identifier + "'");
            }
         } else {
            class_2960 identifier = new class_2960(string);
            Optional<class_1792> item = class_7923.field_41178.method_17966(identifier);
            if (item.isPresent()) {
               items.put(item.get(), satisfaction);
            } else if (identifier.method_12836().equals("mca")) {
               throw new JsonSyntaxException("Unknown item '" + identifier + "'");
            }
         }
      });
      int priority = class_3518.method_15282(json, "priority", 0);
      JsonObject thresholds = class_3518.method_15281(json, "thresholds", new JsonObject());
      int fail = class_3518.method_15282(thresholds, "fail", 0);
      int good = class_3518.method_15282(thresholds, "good", 10);
      int better = class_3518.method_15282(thresholds, "better", 20);
      JsonObject responsesJson = class_3518.method_15281(json, "responses", new JsonObject());
      Map<Response, String> responses = Stream.of(Response.values())
         .collect(
            Collectors.toMap(
               Function.identity(),
               response -> class_3518.method_15253(responsesJson, response.name().toLowerCase(Locale.ENGLISH), response.getDefaultDialogue())
            )
         );
      return new GiftType(id, priority, conditions, items, tags, fail, good, better, responses);
   }

   public static Stream<GiftType> allMatching(class_1799 stack) {
      return REGISTRY.stream().filter(type -> type.matches(stack));
   }

   public static Optional<GiftType> bestMatching(VillagerEntityMCA recipient, class_1799 stack, class_3222 player) {
      int max = allMatching(stack).mapToInt(a -> a.priority).max().orElse(0);
      Optional<GiftType> worst = allMatching(stack)
         .filter(a -> a.priority == max)
         .filter(a -> a.getResponse(a.getSatisfactionFor(recipient, stack, player).getTotal()) == Response.FAIL)
         .max(Comparator.comparingDouble(a -> a.getSatisfactionFor(recipient, stack, player).getTotal().intValue()));
      return worst.isPresent()
         ? worst
         : allMatching(stack)
            .filter(a -> a.priority == max)
            .max(Comparator.comparingDouble(a -> a.getSatisfactionFor(recipient, stack, player).getTotal().intValue()));
   }

   public static Optional<GiftType> getGiftType(class_2960 id) {
      return REGISTRY.stream().filter(p -> p.id.equals(id)).findFirst();
   }

   private static Map<Response, String> getDefaultDialogues() {
      return Arrays.stream(Response.values()).collect(Collectors.toMap(r -> (Response)r, Response::getDefaultDialogue));
   }

   public GiftType(class_1792 item, int satisfaction, class_2960 extendFrom) {
      this(item, satisfaction, getDefaultDialogues());
      Optional<GiftType> type = getGiftType(extendFrom);
      type.ifPresent(this::extendFrom);
   }

   public GiftType(class_1792 item, int satisfaction, Map<Response, String> responses) {
      this(
         class_7923.field_41178.method_10221(item),
         0,
         new LinkedList<>(),
         Collections.singletonMap(item, satisfaction),
         Collections.emptyMap(),
         0,
         10,
         20,
         responses
      );
   }

   public GiftType(
      class_2960 id,
      int priority,
      List<GiftPredicate> conditions,
      Map<class_1792, Integer> items,
      Map<class_6862<class_1792>, Integer> tags,
      int fail,
      int good,
      int better,
      Map<Response, String> responses
   ) {
      this.id = id;
      this.priority = priority;
      this.conditions = conditions;
      this.items = items;
      this.tags = tags;
      this.fail = fail;
      this.good = good;
      this.better = better;
      this.responses = responses;
   }

   public class_2960 getId() {
      return this.id;
   }

   public List<GiftPredicate> getConditions() {
      return this.conditions;
   }

   public Map<Response, String> getResponses() {
      return this.responses;
   }

   public boolean matches(class_1799 stack) {
      return this.items.keySet().stream().anyMatch(i -> i == stack.method_7909()) || this.tags.keySet().stream().anyMatch(stack::method_31573);
   }

   public IntAnalysis getSatisfactionFor(VillagerEntityMCA recipient, class_1799 stack, class_3222 player) {
      IntAnalysis analysis = new IntAnalysis();
      Optional<Integer> value = this.items.entrySet().stream().filter(i -> i.getKey() == stack.method_7909()).findFirst().map(Entry::getValue);
      int base = value.orElseGet(() -> this.tags.entrySet().stream().filter(i -> stack.method_31573(i.getKey())).findFirst().map(Entry::getValue).orElse(0));
      analysis.add("base", base);

      for (GiftPredicate c : this.conditions) {
         int val = c.getSatisfactionFor(recipient, stack, player);
         if (c.test(recipient, stack, player) > 0.0F) {
            analysis.add(c.getConditionKeys().get(0), val);
         }
      }

      return analysis;
   }

   public Response getResponse(int satisfaction) {
      return satisfaction <= this.fail
         ? Response.FAIL
         : (satisfaction <= this.good ? Response.GOOD : (satisfaction <= this.better ? Response.BETTER : Response.BEST));
   }

   public String getDialogueFor(Response response) {
      return this.responses.get(response);
   }

   public void extendFrom(GiftType extendingType) {
      this.conditions.addAll(extendingType.getConditions());
      this.responses.clear();
      this.responses.putAll(extendingType.getResponses());
      this.priority = extendingType.priority;
      this.fail = extendingType.fail;
      this.good = extendingType.good;
      this.better = extendingType.better;
   }
}
