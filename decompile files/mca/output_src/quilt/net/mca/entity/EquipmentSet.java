package quilt.net.mca.entity;

import net.minecraft.class_1792;
import net.minecraft.class_1802;

public enum EquipmentSet {
   NAKED(class_1802.field_8162, class_1802.field_8162, class_1802.field_8162, class_1802.field_8162, class_1802.field_8162, class_1802.field_8162),
   GUARD_0(class_1802.field_8371, class_1802.field_8162, class_1802.field_8162, class_1802.field_8523, class_1802.field_8570, class_1802.field_8370),
   GUARD_0_LEFT(class_1802.field_8162, class_1802.field_8371, class_1802.field_8162, class_1802.field_8523, class_1802.field_8570, class_1802.field_8370),
   GUARD_1(class_1802.field_8371, class_1802.field_8255, class_1802.field_8743, class_1802.field_8523, class_1802.field_8570, class_1802.field_8660),
   GUARD_2(class_1802.field_8802, class_1802.field_8255, class_1802.field_8743, class_1802.field_8523, class_1802.field_8396, class_1802.field_8660),
   ARCHER_0(class_1802.field_8102, class_1802.field_8162, class_1802.field_8162, class_1802.field_8577, class_1802.field_8162, class_1802.field_8162),
   ARCHER_0_LEFT(class_1802.field_8162, class_1802.field_8102, class_1802.field_8162, class_1802.field_8577, class_1802.field_8162, class_1802.field_8162),
   ARCHER_1(class_1802.field_8102, class_1802.field_8162, class_1802.field_8162, class_1802.field_8523, class_1802.field_8570, class_1802.field_8370),
   ARCHER_1_LEFT(class_1802.field_8162, class_1802.field_8102, class_1802.field_8162, class_1802.field_8523, class_1802.field_8570, class_1802.field_8370),
   ARCHER_2(class_1802.field_8102, class_1802.field_8162, class_1802.field_8162, class_1802.field_8058, class_1802.field_8570, class_1802.field_8660),
   ARCHER_2_LEFT(class_1802.field_8162, class_1802.field_8102, class_1802.field_8162, class_1802.field_8058, class_1802.field_8570, class_1802.field_8660),
   ELITE(class_1802.field_22022, class_1802.field_22022, class_1802.field_8805, class_1802.field_22028, class_1802.field_8416, class_1802.field_22030),
   ROYAL(class_1802.field_8547, class_1802.field_8556, class_1802.field_8862, class_1802.field_8678, class_1802.field_8416, class_1802.field_8753);

   final class_1792 mainHand;
   final class_1792 getOffHand;
   final class_1792 head;
   final class_1792 chest;
   final class_1792 legs;
   final class_1792 feet;

   EquipmentSet(class_1792 mainHand, class_1792 offHand, class_1792 head, class_1792 chest, class_1792 legs, class_1792 feet) {
      this.mainHand = mainHand;
      this.getOffHand = offHand;
      this.head = head;
      this.chest = chest;
      this.legs = legs;
      this.feet = feet;
   }

   public class_1792 getMainHand() {
      return this.mainHand;
   }

   public class_1792 getGetOffHand() {
      return this.getOffHand;
   }

   public class_1792 getHead() {
      return this.head;
   }

   public class_1792 getChest() {
      return this.chest;
   }

   public class_1792 getLegs() {
      return this.legs;
   }

   public class_1792 getFeet() {
      return this.feet;
   }
}
