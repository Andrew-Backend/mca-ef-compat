package forge.net.mca.util;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.Nullable;

public interface InventoryUtils {
   static Stream<ItemStack> stream(Container inventory) {
      return IntStream.range(0, inventory.m_6643_()).mapToObj(inventory::m_8020_);
   }

   static int getFirstSlotContainingItem(Container inv, Predicate<ItemStack> predicate) {
      for (int i = 0; i < inv.m_6643_(); i++) {
         ItemStack stack = inv.m_8020_(i);
         if (predicate.test(stack)) {
            return i;
         }
      }

      return -1;
   }

   static boolean contains(Container inv, Class<?> clazz) {
      for (int i = 0; i < inv.m_6643_(); i++) {
         ItemStack stack = inv.m_8020_(i);
         Item item = stack.m_41720_();
         if (item.getClass() == clazz) {
            return true;
         }
      }

      return false;
   }

   static ItemStack getBestItemOfType(Container inv, @Nullable Class<?> type) {
      return type == null ? ItemStack.f_41583_ : inv.m_8020_(getBestItemOfTypeSlot(inv, type));
   }

   static int getBestItemOfTypeSlot(Container inv, Class<?> type) {
      int highestMaxDamage = 0;
      int best = -1;

      for (int i = 0; i < inv.m_6643_(); i++) {
         ItemStack stackInInventory = inv.m_8020_(i);
         String itemClassName = stackInInventory.m_41720_().getClass().getName();
         if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.m_41776_()) {
            highestMaxDamage = stackInInventory.m_41776_();
            best = i;
         }
      }

      return best;
   }

   static Optional<ItemStack> getBestArmor(Container inv, EquipmentSlot slot) {
      return stream(inv)
         .filter(s -> s.m_41720_() instanceof ArmorItem)
         .filter(s -> ((ArmorItem)s.m_41720_()).m_40402_() == slot)
         .max(Comparator.comparingDouble(s -> ((ArmorItem)s.m_41720_()).m_40404_()));
   }

   static Optional<ItemStack> getBestSword(Container inv) {
      return stream(inv).filter(s -> s.m_41720_() instanceof SwordItem).max(Comparator.comparingDouble(s -> ((SwordItem)s.m_41720_()).m_43299_()));
   }

   static Optional<ItemStack> getBestRanged(Container inv) {
      return stream(inv).filter(s -> s.m_41720_() instanceof ProjectileWeaponItem).max(Comparator.comparingDouble(s -> s.m_41720_().m_41462_()));
   }

   static void dropAllItems(Entity entity, Container inv) {
      for (int i = 0; i < inv.m_6643_(); i++) {
         ItemStack stack = inv.m_8020_(i);
         entity.m_5552_(stack, 1.0F);
      }

      inv.m_6211_();
   }

   static void load(Container inv, ListTag tagList) {
      for (int i = 0; i < inv.m_6643_(); i++) {
         inv.m_6836_(i, ItemStack.f_41583_);
      }

      for (int i = 0; i < tagList.size(); i++) {
         CompoundTag nbt = tagList.m_128728_(i);
         int slot = nbt.m_128445_("Slot") & 255;
         if (slot < inv.m_6643_()) {
            inv.m_6836_(slot, ItemStack.m_41712_(nbt));
         }
      }
   }

   static ListTag save(Container inv) {
      ListTag tagList = new ListTag();

      for (int i = 0; i < inv.m_6643_(); i++) {
         ItemStack itemstack = inv.m_8020_(i);
         if (itemstack != ItemStack.f_41583_) {
            CompoundTag nbt = new CompoundTag();
            nbt.m_128344_("Slot", (byte)i);
            itemstack.m_41751_(nbt);
            tagList.add(nbt);
         }
      }

      return tagList;
   }

   static void saveToNBT(SimpleContainer inv, CompoundTag nbt) {
      nbt.m_128365_("Inventory", inv.m_7927_());
   }

   static void readFromNBT(SimpleContainer inv, CompoundTag nbt) {
      inv.m_7797_(nbt.m_128437_("Inventory", 10));
   }
}
