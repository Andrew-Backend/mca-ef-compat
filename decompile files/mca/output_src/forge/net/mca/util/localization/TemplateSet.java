package forge.net.mca.util.localization;

import forge.net.mca.resources.Supporters;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateSet {
   static final TemplateSet INSTANCE = new TemplateSet().with("supporter", Supporters::getRandomSupporter);
   private final Map<String, Supplier<String>> variables = new HashMap<>();

   public TemplateSet with(String name, Supplier<String> valueSupplier) {
      this.variables.put(name, valueSupplier);
      return this;
   }

   public String replace(String input) {
      for (String name : this.variables.keySet()) {
         input = this.replaceAll(name, input);
      }

      return input;
   }

   private String replaceAll(String name, String input) {
      StringBuilder buffer = new StringBuilder();
      Matcher matcher = Pattern.compile("%" + name + "(:[0-9]+)?%", 2).matcher(input);
      Map<String, String> computed = new HashMap<>();

      while (matcher.find()) {
         String found = matcher.group();
         String replacement = computed.get(found.toLowerCase(Locale.ENGLISH));
         if (replacement == null) {
            replacement = this.variables.get(name).get();
            computed.put(found.toLowerCase(Locale.ENGLISH), replacement);
         }

         matcher.appendReplacement(buffer, replacement);
      }

      matcher.appendTail(buffer);
      return buffer.toString();
   }
}
