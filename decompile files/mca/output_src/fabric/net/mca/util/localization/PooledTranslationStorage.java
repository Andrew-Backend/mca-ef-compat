package fabric.net.mca.util.localization;

import fabric.net.mca.resources.PoolUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.class_3545;
import net.minecraft.class_5819;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PooledTranslationStorage {
   private static final Pattern TRAILING_NUMBERS_PATTERN = Pattern.compile("/[0-9]+$");
   private static final Predicate<String> TRAILING_NUMBERS_PREDICATE = TRAILING_NUMBERS_PATTERN.asPredicate();
   private final Map<String, List<class_3545<String, String>>> multiTranslations = new HashMap<>();
   private final class_5819 rand = class_5819.method_43047();

   public PooledTranslationStorage(Map<String, String> translations) {
      translations.forEach(this::addTranslation);
   }

   private void addTranslation(String key, String value) {
      if (TRAILING_NUMBERS_PREDICATE.test(key)) {
         this.multiTranslations.computeIfAbsent(TRAILING_NUMBERS_PATTERN.matcher(key).replaceAll(""), k -> new ArrayList<>()).add(new class_3545(key, value));
      }
   }

   @NotNull
   private List<class_3545<String, String>> getOptions(String key) {
      return this.multiTranslations.getOrDefault(key, Collections.emptyList());
   }

   @Nullable
   public class_3545<String, String> get(String key) {
      List<class_3545<String, String>> options = this.getOptions(key);
      if (!options.isEmpty()) {
         class_3545<String, String> pair = PoolUtil.pickOne(options, new class_3545(key, key), this.rand);
         pair.method_34965(TemplateSet.INSTANCE.replace((String)pair.method_15441()));
         return pair;
      } else {
         return null;
      }
   }

   public boolean contains(String key) {
      return !this.getOptions(key).isEmpty();
   }
}
