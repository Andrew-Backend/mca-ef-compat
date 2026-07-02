package forge.net.mca.resources.data;

import com.google.common.base.Strings;
import forge.net.mca.resources.PoolUtil;
import java.util.Locale;
import net.minecraft.util.RandomSource;

public final class NameSet {
   public static final NameSet DEFAULT = new NameSet(" ", new String[]{"unknown"}, new String[]{"names"});
   private final String separator;
   private final String[] first;
   private final String[] second;

   public String separator() {
      return this.separator;
   }

   public String[] first() {
      return this.first;
   }

   public String[] second() {
      return this.second;
   }

   public NameSet(String separator, String[] first, String[] second) {
      this.separator = separator;
      this.first = first;
      this.second = second;
   }

   public String toName(RandomSource rng) {
      String first = PoolUtil.pickOne(this.first(), null, rng);
      String second = PoolUtil.pickOne(this.second(), null, rng);
      return Strings.isNullOrEmpty(this.separator()) ? toTitleCase(first + second) : toTitleCase(first) + this.separator() + toTitleCase(second);
   }

   static String toTitleCase(String s) {
      return s.substring(0, 1).toUpperCase(Locale.ENGLISH) + s.substring(1);
   }
}
