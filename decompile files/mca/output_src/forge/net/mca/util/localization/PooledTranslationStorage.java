package forge.net.mca.util.localization;

import forge.net.mca.resources.PoolUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PooledTranslationStorage {
   private static final Pattern TRAILING_NUMBERS_PATTERN = Pattern.compile("/[0-9]+$");
   private static final Predicate<String> TRAILING_NUMBERS_PREDICATE = TRAILING_NUMBERS_PATTERN.asPredicate();
   private final Map<String, List<Tuple<String, String>>> multiTranslations = new HashMap<>();
   private final RandomSource rand = RandomSource.m_216327_();

   public PooledTranslationStorage(Map<String, String> translations) {
      translations.forEach(this::addTranslation);
   }

   private void addTranslation(String key, String value) {
      if (TRAILING_NUMBERS_PREDICATE.test(key)) {
         this.multiTranslations.computeIfAbsent(TRAILING_NUMBERS_PATTERN.matcher(key).replaceAll(""), k -> new ArrayList<>()).add(new Tuple(key, value));
      }
   }

   @NotNull
   private List<Tuple<String, String>> getOptions(String key) {
      return this.multiTranslations.getOrDefault(key, Collections.emptyList());
   }

   @Nullable
   public Tuple<String, String> get(String key) {
      List<Tuple<String, String>> options = this.getOptions(key);
      if (!options.isEmpty()) {
         Tuple<String, String> pair = PoolUtil.pickOne(options, new Tuple(key, key), this.rand);
         pair.m_145025_(TemplateSet.INSTANCE.replace((String)pair.m_14419_()));
         return pair;
      } else {
         return null;
      }
   }

   public boolean contains(String key) {
      return !this.getOptions(key).isEmpty();
   }
}
