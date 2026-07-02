package quilt.net.mca.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.class_3300;
import net.minecraft.class_5819;
import quilt.net.mca.MCA;

public class API {
   static final class_5819 rng = class_5819.method_43047();
   static API.Data instance = new API.Data();

   public static VillageComponents getVillagePool() {
      return instance.villageComponents;
   }

   public static String getRandomWord(String from) {
      return instance.pickWord(from);
   }

   public static String getRandomSentence(String from, int wordCount) {
      List<String> words = new LinkedList<>();

      for (int i = 0; i < wordCount; i++) {
         words.add(getRandomWord(from));
      }

      return String.join(" ", words);
   }

   public static String getRandomSentence(String from, String source) {
      int wordCount = source.split(" ").length;
      String sentence = getRandomSentence(from, wordCount);
      char last = source.charAt(source.length() - 1);
      if (last == '!' || last == '?' || last == '.') {
         sentence = sentence + last;
      }

      return sentence;
   }

   public static class_5819 getRng() {
      return rng;
   }

   static class Data {
      final VillageComponents villageComponents = new VillageComponents(API.rng);
      private final Map<String, List<String>> words = new HashMap<>();

      void init(class_3300 manager) {
         try {
            this.villageComponents.load();
            this.words.put("zombie", Arrays.asList(Resources.read("api/names/zombie_words.json", String[].class)));
            this.words.put("baby", Arrays.asList(Resources.read("api/names/baby_words.json", String[].class)));
         } catch (Resources.BrokenResourceException e) {
            MCA.LOGGER.error("Could not load MCA resources", e);
         }
      }

      public String pickWord(String from) {
         return PoolUtil.pickOne(this.words.get(from), "?", API.rng);
      }
   }
}
