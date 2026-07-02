package fabric.net.mca.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
   private final int maxSize;

   public LimitedLinkedHashMap(int maxSize) {
      super(maxSize);
      this.maxSize = maxSize;
   }

   @Override
   protected boolean removeEldestEntry(Entry<K, V> eldest) {
      return this.size() > this.maxSize;
   }
}
