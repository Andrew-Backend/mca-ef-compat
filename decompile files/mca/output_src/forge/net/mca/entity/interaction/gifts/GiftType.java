package forge.net.mca.entity.interaction.gifts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.resources.data.analysis.IntAnalysis;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GiftType {
   static final List<GiftType> REGISTRY = new ArrayList<>();
   private final ResourceLocation id;
   private int priority;
   private final List<GiftPredicate> conditions;
   private final Map<Item, Integer> items;
   private final Map<TagKey<Item>, Integer> tags;
   private int fail;
   private int good;
   private int better;
   private final Map<Response, String> responses;

   public static GiftType fromJson(ResourceLocation id, JsonObject json) {
      List<GiftPredicate> conditions = new ArrayList<>();
      GsonHelper.m_13832_(json, "conditions", new JsonArray())
         .forEach(element -> conditions.add(GiftPredicate.fromJson(GsonHelper.m_13918_(element, "condition"))));
      HashMap<Item, Integer> items = new HashMap<>();
      HashMap<TagKey<Item>, Integer> tags = new HashMap<>();
      GsonHelper.m_13930_(json, "items").entrySet().forEach(element -> {
         String string = (String)element.getKey();
         Integer satisfaction = ((JsonElement)element.getValue()).getAsInt();
         if (string.charAt(0) == '#') {
            ResourceLocation identifier = new ResourceLocation(string.substring(1));
            TagKey<Item> tag = TagKey.m_203882_(Registries.f_256913_, identifier);
            if (tag != null) {
               tags.put(tag, satisfaction);
            } else if (identifier.m_135827_().equals("mca")) {
               throw new JsonSyntaxException("Unknown item tag '" + identifier + "'");
            }
         } else {
            ResourceLocation identifier = new ResourceLocation(string);
            Optional<Item> item = BuiltInRegistries.f_257033_.m_6612_(identifier);
            if (item.isPresent()) {
               items.put(item.get(), satisfaction);
            } else if (identifier.m_135827_().equals("mca")) {
               throw new JsonSyntaxException("Unknown item '" + identifier + "'");
            }
         }
      });
      int priority = GsonHelper.m_13824_(json, "priority", 0);
      JsonObject thresholds = GsonHelper.m_13841_(json, "thresholds", new JsonObject());
      int fail = GsonHelper.m_13824_(thresholds, "fail", 0);
      int good = GsonHelper.m_13824_(thresholds, "good", 10);
      int better = GsonHelper.m_13824_(thresholds, "better", 20);
      JsonObject responsesJson = GsonHelper.m_13841_(json, "responses", new JsonObject());
      Map<Response, String> responses = Stream.of(Response.values())
         .collect(
            Collectors.toMap(
               Function.identity(), response -> GsonHelper.m_13851_(responsesJson, response.name().toLowerCase(Locale.ENGLISH), response.getDefaultDialogue())
            )
         );
      return new GiftType(id, priority, conditions, items, tags, fail, good, better, responses);
   }

   public static Stream<GiftType> allMatching(ItemStack stack) {
      return REGISTRY.stream().filter(type -> type.matches(stack));
   }

   public static Optional<GiftType> bestMatching(VillagerEntityMCA recipient, ItemStack stack, ServerPlayer player) {
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

   public static Optional<GiftType> getGiftType(ResourceLocation id) {
      return REGISTRY.stream().filter(p -> p.id.equals(id)).findFirst();
   }

   private static Map<Response, String> getDefaultDialogues() {
      return Arrays.stream(Response.values()).collect(Collectors.toMap(r -> (Response)r, Response::getDefaultDialogue));
   }

   public GiftType(Item item, int satisfaction, ResourceLocation extendFrom) {
      this(item, satisfaction, getDefaultDialogues());
      Optional<GiftType> type = getGiftType(extendFrom);
      type.ifPresent(this::extendFrom);
   }

   public GiftType(Item item, int satisfaction, Map<Response, String> responses) {
      this(
         BuiltInRegistries.f_257033_.m_7981_(item),
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
      ResourceLocation id,
      int priority,
      List<GiftPredicate> conditions,
      Map<Item, Integer> items,
      Map<TagKey<Item>, Integer> tags,
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

   public ResourceLocation getId() {
      return this.id;
   }

   public List<GiftPredicate> getConditions() {
      return this.conditions;
   }

   public Map<Response, String> getResponses() {
      return this.responses;
   }

   public boolean matches(ItemStack stack) {
      return this.items.keySet().stream().anyMatch(i -> i == stack.m_41720_()) || this.tags.keySet().stream().anyMatch(stack::m_204117_);
   }

   public IntAnalysis getSatisfactionFor(VillagerEntityMCA recipient, ItemStack stack, ServerPlayer player) {
      IntAnalysis analysis = new IntAnalysis();
      Optional<Integer> value = this.items.entrySet().stream().filter(i -> i.getKey() == stack.m_41720_()).findFirst().map(Entry::getValue);
      int base = value.orElseGet(() -> this.tags.entrySet().stream().filter(i -> stack.m_204117_(i.getKey())).findFirst().map(Entry::getValue).orElse(0));
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
