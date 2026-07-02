package forge.net.mca.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum EquipmentSet {
   NAKED(Items.f_41852_, Items.f_41852_, Items.f_41852_, Items.f_41852_, Items.f_41852_, Items.f_41852_),
   GUARD_0(Items.f_42383_, Items.f_41852_, Items.f_41852_, Items.f_42469_, Items.f_42462_, Items.f_42463_),
   GUARD_0_LEFT(Items.f_41852_, Items.f_42383_, Items.f_41852_, Items.f_42469_, Items.f_42462_, Items.f_42463_),
   GUARD_1(Items.f_42383_, Items.f_42740_, Items.f_42468_, Items.f_42469_, Items.f_42462_, Items.f_42471_),
   GUARD_2(Items.f_42388_, Items.f_42740_, Items.f_42468_, Items.f_42469_, Items.f_42470_, Items.f_42471_),
   ARCHER_0(Items.f_42411_, Items.f_41852_, Items.f_41852_, Items.f_42408_, Items.f_41852_, Items.f_41852_),
   ARCHER_0_LEFT(Items.f_41852_, Items.f_42411_, Items.f_41852_, Items.f_42408_, Items.f_41852_, Items.f_41852_),
   ARCHER_1(Items.f_42411_, Items.f_41852_, Items.f_41852_, Items.f_42469_, Items.f_42462_, Items.f_42463_),
   ARCHER_1_LEFT(Items.f_41852_, Items.f_42411_, Items.f_41852_, Items.f_42469_, Items.f_42462_, Items.f_42463_),
   ARCHER_2(Items.f_42411_, Items.f_41852_, Items.f_41852_, Items.f_42473_, Items.f_42462_, Items.f_42471_),
   ARCHER_2_LEFT(Items.f_41852_, Items.f_42411_, Items.f_41852_, Items.f_42473_, Items.f_42462_, Items.f_42471_),
   ELITE(Items.f_42393_, Items.f_42393_, Items.f_42472_, Items.f_42481_, Items.f_42478_, Items.f_42483_),
   ROYAL(Items.f_42713_, Items.f_42391_, Items.f_42476_, Items.f_42477_, Items.f_42478_, Items.f_42479_);

   final Item mainHand;
   final Item getOffHand;
   final Item head;
   final Item chest;
   final Item legs;
   final Item feet;

   EquipmentSet(Item mainHand, Item offHand, Item head, Item chest, Item legs, Item feet) {
      this.mainHand = mainHand;
      this.getOffHand = offHand;
      this.head = head;
      this.chest = chest;
      this.legs = legs;
      this.feet = feet;
   }

   public Item getMainHand() {
      return this.mainHand;
   }

   public Item getGetOffHand() {
      return this.getOffHand;
   }

   public Item getHead() {
      return this.head;
   }

   public Item getChest() {
      return this.chest;
   }

   public Item getLegs() {
      return this.legs;
   }

   public Item getFeet() {
      return this.feet;
   }
}
