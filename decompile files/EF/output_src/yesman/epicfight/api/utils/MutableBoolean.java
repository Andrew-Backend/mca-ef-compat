package yesman.epicfight.api.utils;

import org.apache.commons.lang3.BooleanUtils;

public class MutableBoolean implements Comparable<MutableBoolean> {
   boolean val;

   public MutableBoolean() {
      this(false);
   }

   public MutableBoolean(boolean init) {
      this.val = init;
   }

   public boolean value() {
      return this.val;
   }

   public void set(boolean val) {
      this.val = val;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableBoolean ? this.val == ((MutableBoolean)obj).val : false;
   }

   @Override
   public int hashCode() {
      return this.val ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
   }

   public int compareTo(MutableBoolean other) {
      return BooleanUtils.compare(this.val, other.val);
   }
}
