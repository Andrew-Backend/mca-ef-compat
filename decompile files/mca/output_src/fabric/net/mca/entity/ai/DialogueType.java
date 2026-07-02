package fabric.net.mca.entity.ai;

import fabric.net.mca.MCAClient;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.Personality;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.class_2477;
import net.minecraft.class_5819;

public enum DialogueType {
   ADULT(null),
   ADULTP(ADULT),
   UNASSIGNED(ADULT),
   BABY(UNASSIGNED),
   CHILD(ADULT),
   CHILDP(CHILD),
   TODDLER(CHILD),
   TODDLERP(CHILDP),
   SPOUSE(ADULT),
   TEEN(ADULT),
   TEENP(TEEN),
   ENGAGED(ADULT);

   public final DialogueType fallback;
   private static final class_5819 random = class_5819.method_43047();
   private static final DialogueType[] VALUES = values();
   public static final Map<String, DialogueType> MAP = Arrays.stream(VALUES).collect(Collectors.toMap(Enum::name, Function.identity()));

   DialogueType(DialogueType fallback) {
      this.fallback = fallback;
   }

   public DialogueType toChild() {
      return switch (this) {
         case ADULT -> ADULTP;
         default -> UNASSIGNED;
         case CHILD -> CHILDP;
         case TODDLER -> TODDLERP;
         case TEEN -> TEENP;
      };
   }

   public static DialogueType fromAge(AgeState state) {
      for (DialogueType t : values()) {
         if (t.name().equals(state.name())) {
            return t;
         }
      }

      return UNASSIGNED;
   }

   public static DialogueType byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : UNASSIGNED;
   }

   private static Optional<String> getPrefixedPhrase(DialogueType type, String prefix, String key) {
      for (DialogueType t = type; t != null; t = t.fallback) {
         String s = prefix + "." + t.name().toLowerCase(Locale.ENGLISH) + "." + key;
         if (class_2477.method_10517().method_4678(s)) {
            return Optional.of(s);
         }
      }

      String s = prefix + "." + key;
      return class_2477.method_10517().method_4678(s) ? Optional.of(s) : Optional.empty();
   }

   public static String applyFallback(String key) {
      if (!key.contains("#")) {
         return key;
      }

      Map<String, String> flags = new HashMap<>();

      for (String s : key.split("\\.")) {
         if (s.startsWith("#")) {
            flags.put(s.substring(1, 2), s.substring(2));
            key = key.replace(s + ".", "");
         }
      }

      DialogueType type = null;
      if (flags.containsKey("T")) {
         type = MAP.get(flags.get("T"));
      }

      if (type == null) {
         return key;
      }

      if (flags.containsKey("P") && random.method_43056()) {
         Optional<String> p = getPrefixedPhrase(type, flags.get("P"), key);
         if (p.isPresent()) {
            return p.get();
         }
      }

      if (flags.containsKey("E") && MCAClient.useExpandedPersonalityTranslations()) {
         String personality = Personality.valueOf(flags.get("E")).name().toLowerCase(Locale.ROOT);
         Optional<String> p = getPrefixedPhrase(type, personality, key);
         if (p.isPresent()) {
            return p.get();
         }
      }

      if (flags.containsKey("G")) {
         Optional<String> p = getPrefixedPhrase(type, flags.get("G"), key);
         if (p.isPresent()) {
            return p.get();
         }
      }

      for (DialogueType t = type; t != null; t = t.fallback) {
         String s = t.name().toLowerCase(Locale.ENGLISH) + "." + key;
         if (class_2477.method_10517().method_4678(s)) {
            return s;
         }
      }

      return key;
   }
}
