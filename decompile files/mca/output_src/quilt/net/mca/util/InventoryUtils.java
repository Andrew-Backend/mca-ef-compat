package quilt.net.mca.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.class_1263;
import net.minecraft.class_1277;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1738;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_1829;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import org.jetbrains.annotations.Nullable;

public interface InventoryUtils {
   static Stream<class_1799> stream(class_1263 inventory) {
      return IntStream.range(0, inventory.method_5439()).mapToObj(inventory::method_5438);
   }

   static int getFirstSlotContainingItem(class_1263 inv, Predicate<class_1799> predicate) {
      for (int i = 0; i < inv.method_5439(); i++) {
         class_1799 stack = inv.method_5438(i);
         if (predicate.test(stack)) {
            return i;
         }
      }

      return -1;
   }

   static boolean contains(class_1263 inv, Class<?> clazz) {
      for (int i = 0; i < inv.method_5439(); i++) {
         class_1799 stack = inv.method_5438(i);
         class_1792 item = stack.method_7909();
         if (item.getClass() == clazz) {
            return true;
         }
      }

      return false;
   }

   static class_1799 getBestItemOfType(class_1263 inv, @Nullable Class<?> type) {
      return type == null ? class_1799.field_8037 : inv.method_5438(getBestItemOfTypeSlot(inv, type));
   }

   static int getBestItemOfTypeSlot(class_1263 inv, Class<?> type) {
      int highestMaxDamage = 0;
      int best = -1;

      for (int i = 0; i < inv.method_5439(); i++) {
         class_1799 stackInInventory = inv.method_5438(i);
         String itemClassName = stackInInventory.method_7909().getClass().getName();
         if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.method_7936()) {
            highestMaxDamage = stackInInventory.method_7936();
            best = i;
         }
      }

      return best;
   }

   static Optional<class_1799> getBestArmor(class_1263 inv, class_1304 slot) {
      return stream(inv)
         .filter(s -> s.method_7909() instanceof class_1738)
         .filter(s -> ((class_1738)s.method_7909()).method_7685() == slot)
         .max(Comparator.comparingDouble(s -> ((class_1738)s.method_7909()).method_7687()));
   }

   static Optional<class_1799> getBestSword(class_1263 inv) {
      return stream(inv).filter(s -> s.method_7909() instanceof class_1829).max(Comparator.comparingDouble(s -> ((class_1829)s.method_7909()).method_8020()));
   }

   static Optional<class_1799> getBestRanged(class_1263 inv) {
      return stream(inv).filter(s -> s.method_7909() instanceof class_1811).max(Comparator.comparingDouble(s -> s.method_7909().method_7841()));
   }

   static void dropAllItems(class_1297 entity, class_1263 inv) {
      for (int i = 0; i < inv.method_5439(); i++) {
         class_1799 stack = inv.method_5438(i);
         entity.method_5699(stack, 1.0F);
      }

      inv.method_5448();
   }

   static void load(class_1263 inv, class_2499 tagList) {
      for (int i = 0; i < inv.method_5439(); i++) {
         inv.method_5447(i, class_1799.field_8037);
      }

      for (int i = 0; i < tagList.size(); i++) {
         class_2487 nbt = tagList.method_10602(i);
         int slot = nbt.method_10571("Slot") & 255;
         if (slot < inv.method_5439()) {
            inv.method_5447(slot, class_1799.method_7915(nbt));
         }
      }
   }

   static class_2499 save(class_1263 inv) {
      class_2499 tagList = new class_2499();

      for (int i = 0; i < inv.method_5439(); i++) {
         class_1799 itemstack = inv.method_5438(i);
         if (itemstack != class_1799.field_8037) {
            class_2487 nbt = new class_2487();
            nbt.method_10567("Slot", (byte)i);
            itemstack.method_7980(nbt);
            tagList.add(nbt);
         }
      }

      return tagList;
   }

   static void saveToNBT(class_1277 inv, class_2487 nbt) {
      nbt.method_10566("Inventory", inv.method_7660());
   }

   static void readFromNBT(class_1277 inv, class_2487 nbt) {
      inv.method_7659(nbt.method_10554("Inventory", 10));
   }
}
