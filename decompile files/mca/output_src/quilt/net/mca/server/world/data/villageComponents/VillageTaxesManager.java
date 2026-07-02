package quilt.net.mca.server.world.data.villageComponents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.class_124;
import net.minecraft.class_1263;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_7923;
import quilt.net.mca.Config;
import quilt.net.mca.resources.Rank;
import quilt.net.mca.resources.Tasks;
import quilt.net.mca.server.world.data.Village;
import quilt.net.mca.util.WorldUtils;

public class VillageTaxesManager {
   private static final int MAX_STORAGE_SIZE = 1024;
   private final Village village;

   public VillageTaxesManager(Village village) {
      this.village = village;
   }

   public void taxes(class_3218 world) {
      double taxes = Config.getInstance().taxesFactor * this.village.getPopulation() * this.village.getTaxes() + world.field_9229.method_43058();
      int moodImpact = 0;
      float r = this.village.getTaxes() + (world.field_9229.method_43057() - 0.5F) * world.field_9229.method_43057();
      class_2561 msg;
      if (this.village.getTaxes() == 0.0F) {
         msg = class_2561.method_43469("gui.village.taxes.no", new Object[]{this.village.getName()}).method_27692(class_124.field_1060);
         moodImpact = 5;
         taxes = 0.0;
      } else if (r < 0.1) {
         msg = class_2561.method_43469("gui.village.taxes.more", new Object[]{this.village.getName()}).method_27692(class_124.field_1060);
         taxes += this.village.getPopulation() * 0.25;
      } else if (r < 0.3) {
         msg = class_2561.method_43469("gui.village.taxes.happy", new Object[]{this.village.getName()}).method_27692(class_124.field_1077);
         moodImpact = 5;
      } else if (r < 0.7) {
         msg = class_2561.method_43469("gui.village.taxes", new Object[]{this.village.getName()});
      } else if (r < 0.8) {
         msg = class_2561.method_43469("gui.village.taxes.sad", new Object[]{this.village.getName()}).method_27692(class_124.field_1065);
         moodImpact = -5;
      } else if (r < 0.9) {
         msg = class_2561.method_43469("gui.village.taxes.angry", new Object[]{this.village.getName()}).method_27692(class_124.field_1061);
         moodImpact = -10;
      } else {
         msg = class_2561.method_43469("gui.village.taxes.riot", new Object[]{this.village.getName()}).method_27692(class_124.field_1079);
         taxes = 0.0;
      }

      world.method_18456().stream().filter(v -> Tasks.getRank(this.village, v).isAtLeast(Rank.MERCHANT)).forEach(player -> player.method_7353(msg, true));
      if (this.village.hasBuilding("library")) {
         taxes *= 1.5;
      }

      while (taxes > 0.0) {
         double finalTaxes = taxes;
         List<String> valids = Config.getInstance()
            .taxesMap
            .entrySet()
            .stream()
            .filter(e -> e.getValue() * world.field_9229.method_43057() < finalTaxes)
            .map(Entry::getKey)
            .toList();
         if (valids.isEmpty()) {
            break;
         }

         String itemName = valids.get(world.field_9229.method_43048(valids.size()));
         class_1792 item = (class_1792)class_7923.field_41178.method_10223(new class_2960(itemName));
         if (item == class_1802.field_8162) {
            throw new RuntimeException("The taxes map contains an invalid item %s!".formatted(itemName));
         }

         taxes -= Config.getInstance().taxesMap.get(itemName).floatValue();
         Optional<class_1799> stack = this.village.storageBuffer.stream().filter(i -> i.method_31574(item) && i.method_7947() < i.method_7914()).findAny();
         if (stack.isPresent()) {
            stack.get().method_7933(1);
         } else if (this.village.storageBuffer.size() < 1024) {
            this.village.storageBuffer.add(new class_1799(item, 1));
         }
      }

      if (moodImpact != 0) {
         this.village.pushMood(moodImpact * this.village.getPopulation());
      }

      this.deliverTaxes(world);
   }

   public void deliverTaxes(class_3218 world) {
      if (this.village.hasStoredResource() && WorldUtils.isChunkLoaded(world, this.village.getCenter())) {
         this.village.getBuildingsOfType("storage").forEach(building -> building.getBlocks().values().stream().flatMap(Collection::stream).forEach(p -> {
            if (this.village.hasStoredResource()) {
               this.tryToPutIntoInventory(world, p);
            }
         }));
      }
   }

   private void tryToPutIntoInventory(class_3218 world, class_2338 p) {
      class_2680 state = world.method_8320(p);
      if (state.method_31709()
         && world.method_8321(p) instanceof class_1263 inventory
         && inventory instanceof class_2595
         && state.method_26204() instanceof class_2281 chest) {
         class_1263 var8 = class_2281.method_17458(chest, state, world, p, true);
         if (var8 != null) {
            this.putIntoInventory(var8);
         }
      }
   }

   private void putIntoInventory(class_1263 inventory) {
      for (int i = 0; i < inventory.method_5439(); i++) {
         boolean changes = true;

         while (changes) {
            changes = false;
            class_1799 stack = inventory.method_5438(i);
            class_1799 tax = this.village.storageBuffer.get(0);
            if (stack.method_7909() == tax.method_7909()) {
               int diff = Math.min(tax.method_7947(), stack.method_7914() - stack.method_7947());
               if (diff > 0) {
                  stack.method_7933(diff);
                  tax.method_7934(diff);
                  if (tax.method_7960()) {
                     this.village.storageBuffer.remove(0);
                     changes = true;
                  }

                  inventory.method_5431();
               }
            } else if (stack.method_7960()) {
               inventory.method_5447(i, tax);
               inventory.method_5431();
               this.village.storageBuffer.remove(0);
               changes = true;
            }

            if (!this.village.hasStoredResource()) {
               return;
            }
         }
      }
   }
}
