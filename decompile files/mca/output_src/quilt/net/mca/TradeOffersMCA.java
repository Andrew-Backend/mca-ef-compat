package quilt.net.mca;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1914;
import net.minecraft.class_1935;
import net.minecraft.class_2248;
import net.minecraft.class_3852;
import net.minecraft.class_3853;
import net.minecraft.class_5819;
import net.minecraft.class_3853.class_1652;
import quilt.net.mca.item.ItemsMCA;

public class TradeOffersMCA {
   public static Map<class_3852, Int2ObjectMap<class_1652[]>> createTradeMap() {
      return ImmutableMap.of(
         (class_3852)ProfessionsMCA.ADVENTURER.get(),
         new Int2ObjectOpenHashMap(
            ImmutableMap.of(
               1,
               new class_1652[]{
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8777, 1, 1, 16, 1),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_18138, 3, 1, 4, 10),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8175, 4, 1, 3, 5),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8578, 5, 1, 2, 20),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8477, 10, 1, 8, 20),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8560, 10, 1, 3, 30),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8463, 5, 1, 8, 30),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8807, 15, 1, 1, 30),
                  new TradeOffersMCA.SellItemFactory(class_1802.field_8367, 32, 1, 3, 50),
                  new TradeOffersMCA.BuyForOneEmeraldFactory(class_1802.field_8229, 10, 10, 30)
               },
               2,
               new class_1652[0],
               3,
               new class_1652[0],
               4,
               new class_1652[0],
               5,
               new class_1652[0]
            )
         ),
         (class_3852)ProfessionsMCA.CULTIST.get(),
         new Int2ObjectOpenHashMap(
            ImmutableMap.of(
               1,
               new class_1652[]{
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.SIRBEN_BABY_BOY.get(), 5, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.SIRBEN_BABY_GIRL.get(), 5, 1, 1, 1),
                  new TradeOffersMCA.BuyForOneEmeraldFactory((class_1935)ItemsMCA.BABY_BOY.get(), 1, 1, 1),
                  new TradeOffersMCA.BuyForOneEmeraldFactory((class_1935)ItemsMCA.BABY_GIRL.get(), 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_0.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_0.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_1.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_1.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_2.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_CULT_2.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_DEATH.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_INFECTION.get(), 1, 1, 1, 1),
                  new TradeOffersMCA.SellItemFactory((class_1792)ItemsMCA.BOOK_SUPPORTERS.get(), 1, 1, 1, 1)
               },
               2,
               new class_1652[0],
               3,
               new class_1652[0],
               4,
               new class_1652[0],
               5,
               new class_1652[0]
            )
         )
      );
   }

   public static void bootstrap() {
      class_3853.field_17067.putAll(createTradeMap());
   }

   static class BuyForOneEmeraldFactory implements class_1652 {
      private final class_1792 buy;
      private final int price;
      private final int maxUses;
      private final int experience;
      private final float multiplier;

      public BuyForOneEmeraldFactory(class_1935 item, int price, int maxUses, int experience) {
         this.buy = item.method_8389();
         this.price = price;
         this.maxUses = maxUses;
         this.experience = experience;
         this.multiplier = 0.05F;
      }

      public class_1914 method_7246(class_1297 entity, class_5819 random) {
         class_1799 itemStack = new class_1799(this.buy, this.price);
         return new class_1914(itemStack, new class_1799(class_1802.field_8687), this.maxUses, this.experience, this.multiplier);
      }
   }

   static class SellItemFactory implements class_1652 {
      private final class_1799 sell;
      private final int price;
      private final int count;
      private final int maxUses;
      private final int experience;
      private final float multiplier;

      public SellItemFactory(class_2248 block, int price, int count, int maxUses, int experience) {
         this(new class_1799(block), price, count, maxUses, experience);
      }

      public SellItemFactory(class_1792 item, int price, int count, int maxUses, int experience) {
         this(new class_1799(item), price, count, maxUses, experience);
      }

      public SellItemFactory(class_1799 stack, int price, int count, int maxUses, int experience) {
         this(stack, price, count, maxUses, experience, 0.05F);
      }

      public SellItemFactory(class_1799 stack, int price, int count, int maxUses, int experience, float multiplier) {
         this.sell = stack;
         this.price = price;
         this.count = count;
         this.maxUses = maxUses;
         this.experience = experience;
         this.multiplier = multiplier;
      }

      public class_1914 method_7246(class_1297 entity, class_5819 random) {
         return new class_1914(
            new class_1799(class_1802.field_8687, this.price),
            new class_1799(this.sell.method_7909(), this.count),
            this.maxUses,
            this.experience,
            this.multiplier
         );
      }
   }
}
