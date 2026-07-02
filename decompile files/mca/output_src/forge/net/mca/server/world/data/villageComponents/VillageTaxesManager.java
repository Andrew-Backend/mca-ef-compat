package forge.net.mca.server.world.data.villageComponents;

import forge.net.mca.Config;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.util.WorldUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VillageTaxesManager {
   private static final int MAX_STORAGE_SIZE = 1024;
   private final Village village;

   public VillageTaxesManager(Village village) {
      this.village = village;
   }

   public void taxes(ServerLevel world) {
      double taxes = Config.getInstance().taxesFactor * this.village.getPopulation() * this.village.getTaxes() + world.f_46441_.m_188500_();
      int moodImpact = 0;
      float r = this.village.getTaxes() + (world.f_46441_.m_188501_() - 0.5F) * world.f_46441_.m_188501_();
      Component msg;
      if (this.village.getTaxes() == 0.0F) {
         msg = Component.m_237110_("gui.village.taxes.no", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.GREEN);
         moodImpact = 5;
         taxes = 0.0;
      } else if (r < 0.1) {
         msg = Component.m_237110_("gui.village.taxes.more", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.GREEN);
         taxes += this.village.getPopulation() * 0.25;
      } else if (r < 0.3) {
         msg = Component.m_237110_("gui.village.taxes.happy", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.DARK_GREEN);
         moodImpact = 5;
      } else if (r < 0.7) {
         msg = Component.m_237110_("gui.village.taxes", new Object[]{this.village.getName()});
      } else if (r < 0.8) {
         msg = Component.m_237110_("gui.village.taxes.sad", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.GOLD);
         moodImpact = -5;
      } else if (r < 0.9) {
         msg = Component.m_237110_("gui.village.taxes.angry", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.RED);
         moodImpact = -10;
      } else {
         msg = Component.m_237110_("gui.village.taxes.riot", new Object[]{this.village.getName()}).m_130940_(ChatFormatting.DARK_RED);
         taxes = 0.0;
      }

      world.m_6907_().stream().filter(v -> Tasks.getRank(this.village, v).isAtLeast(Rank.MERCHANT)).forEach(player -> player.m_5661_(msg, true));
      if (this.village.hasBuilding("library")) {
         taxes *= 1.5;
      }

      while (taxes > 0.0) {
         double finalTaxes = taxes;
         List<String> valids = Config.getInstance()
            .taxesMap
            .entrySet()
            .stream()
            .filter(e -> e.getValue() * world.f_46441_.m_188501_() < finalTaxes)
            .map(Entry::getKey)
            .toList();
         if (valids.isEmpty()) {
            break;
         }

         String itemName = valids.get(world.f_46441_.m_188503_(valids.size()));
         Item item = (Item)BuiltInRegistries.f_257033_.m_7745_(new ResourceLocation(itemName));
         if (item == Items.f_41852_) {
            throw new RuntimeException("The taxes map contains an invalid item %s!".formatted(itemName));
         }

         taxes -= Config.getInstance().taxesMap.get(itemName).floatValue();
         Optional<ItemStack> stack = this.village.storageBuffer.stream().filter(i -> i.m_150930_(item) && i.m_41613_() < i.m_41741_()).findAny();
         if (stack.isPresent()) {
            stack.get().m_41769_(1);
         } else if (this.village.storageBuffer.size() < 1024) {
            this.village.storageBuffer.add(new ItemStack(item, 1));
         }
      }

      if (moodImpact != 0) {
         this.village.pushMood(moodImpact * this.village.getPopulation());
      }

      this.deliverTaxes(world);
   }

   public void deliverTaxes(ServerLevel world) {
      if (this.village.hasStoredResource() && WorldUtils.isChunkLoaded(world, this.village.getCenter())) {
         this.village.getBuildingsOfType("storage").forEach(building -> building.getBlocks().values().stream().flatMap(Collection::stream).forEach(p -> {
            if (this.village.hasStoredResource()) {
               this.tryToPutIntoInventory(world, p);
            }
         }));
      }
   }

   private void tryToPutIntoInventory(ServerLevel world, BlockPos p) {
      BlockState state = world.m_8055_(p);
      if (state.m_155947_()
         && world.m_7702_(p) instanceof Container inventory
         && inventory instanceof ChestBlockEntity
         && state.m_60734_() instanceof ChestBlock chest) {
         Container var8 = ChestBlock.m_51511_(chest, state, world, p, true);
         if (var8 != null) {
            this.putIntoInventory(var8);
         }
      }
   }

   private void putIntoInventory(Container inventory) {
      for (int i = 0; i < inventory.m_6643_(); i++) {
         boolean changes = true;

         while (changes) {
            changes = false;
            ItemStack stack = inventory.m_8020_(i);
            ItemStack tax = this.village.storageBuffer.get(0);
            if (stack.m_41720_() == tax.m_41720_()) {
               int diff = Math.min(tax.m_41613_(), stack.m_41741_() - stack.m_41613_());
               if (diff > 0) {
                  stack.m_41769_(diff);
                  tax.m_41774_(diff);
                  if (tax.m_41619_()) {
                     this.village.storageBuffer.remove(0);
                     changes = true;
                  }

                  inventory.m_6596_();
               }
            } else if (stack.m_41619_()) {
               inventory.m_6836_(i, tax);
               inventory.m_6596_();
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
