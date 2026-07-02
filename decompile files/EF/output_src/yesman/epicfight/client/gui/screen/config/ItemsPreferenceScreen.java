package yesman.epicfight.client.gui.screen.config;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.item.WeaponItem;

public class ItemsPreferenceScreen extends Screen {
   private static final Set<Class<? extends Item>> WEAPON_CATEGORIZED_ITEM_CLASSES = new HashSet<>();
   private static final Set<Class<? extends Item>> TOOL_CATEGORIZED_ITEM_CLASSES = new HashSet<>();
   private static final Component RESET_TOOLTIP = Component.m_237115_(EpicFightMod.format("gui.%s.item_preferences.tooltip.find_weapon"));
   private static final Component SWAP_ALL = Component.m_237115_(EpicFightMod.format("%s.gui.item_preferences.swap_all"));
   public static final Component GUI_FIND_WEAPONS = Component.m_237115_(EpicFightMod.format("gui.%s.find_weapon"));
   protected final Screen parentScreen;
   private ItemsPreferenceScreen.ItemList combatPreferredItems;
   private ItemsPreferenceScreen.ItemList miningPreferredItems;
   private Set<Item> combatItems = new HashSet<>();
   private Set<Item> miningItems = new HashSet<>();

   @SafeVarargs
   public static void registerWeaponCategorizedItemClasses(Class<? extends Item>... weaponClasses) {
      WEAPON_CATEGORIZED_ITEM_CLASSES.addAll(Set.of(weaponClasses));
   }

   @SafeVarargs
   public static void registerToolCategorizedItemClasses(Class<? extends Item>... toolClasses) {
      TOOL_CATEGORIZED_ITEM_CLASSES.addAll(Set.of(toolClasses));
   }

   private static boolean judgeItemPreference(Item item) {
      boolean isweapon = false;
      boolean foundMatchingClass = false;

      for (Class<?> itemClass = item.getClass(); itemClass != Item.class; itemClass = itemClass.getSuperclass()) {
         if (WEAPON_CATEGORIZED_ITEM_CLASSES.contains(itemClass)) {
            isweapon = true;
            foundMatchingClass = true;
            break;
         }

         if (TOOL_CATEGORIZED_ITEM_CLASSES.contains(itemClass)) {
            isweapon = false;
            foundMatchingClass = true;
            break;
         }
      }

      if (!foundMatchingClass) {
         CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapability(item.m_7968_());
         if (itemCapability != null && itemCapability instanceof WeaponCapability) {
            isweapon = true;
         }
      }

      return isweapon;
   }

   public static void resetItems() {
      ClientConfig.combatPreferredItems.clear();
      ClientConfig.miningPreferredItems.clear();
      ForgeRegistries.ITEMS.getValues().forEach(item -> {
         if (judgeItemPreference(item)) {
            ClientConfig.combatPreferredItems.add(item);
         } else {
            ClientConfig.miningPreferredItems.add(item);
         }
      });
   }

   public static void addWeaponCategorizedItemClass(Class<? extends Item> cls) {
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(cls);
   }

   public ItemsPreferenceScreen(Screen parentScreen) {
      super(Component.m_237115_("epicfight.gui.configuration.item_preferences"));
      this.parentScreen = parentScreen;
      ForgeRegistries.ITEMS.forEach(item -> {
         if (ClientConfig.combatPreferredItems.contains(item)) {
            this.combatItems.add(item);
         } else if (ClientConfig.miningPreferredItems.contains(item)) {
            this.miningItems.add(item);
         } else if (judgeItemPreference(item)) {
            this.combatItems.add(item);
         } else {
            this.miningItems.add(item);
         }
      });
   }

   protected void m_7856_() {
      if (this.combatPreferredItems == null) {
         this.combatPreferredItems = new ItemsPreferenceScreen.ItemList(
            200,
            this.f_96544_,
            true,
            Component.m_237115_("epicfight.gui.combat_perferred"),
            Component.m_237115_("epicfight.gui.combat_perferred.tooltip"),
            () -> this.miningPreferredItems
         );
         this.combatItems.stream().sorted(ItemsPreferenceScreen.ItemList.ItemEntry::compare).forEach(item -> this.combatPreferredItems.addEntry(item));
      } else {
         this.combatPreferredItems.resize(200, this.f_96544_);
         this.combatPreferredItems.m_93410_(this.combatPreferredItems.m_93517_());
      }

      if (this.miningPreferredItems == null) {
         this.miningPreferredItems = new ItemsPreferenceScreen.ItemList(
            200,
            this.f_96544_,
            false,
            Component.m_237115_("epicfight.gui.mining_preferred"),
            Component.m_237115_("epicfight.gui.mining_preferred.tooltip"),
            () -> this.combatPreferredItems
         );
         this.miningItems.stream().sorted(ItemsPreferenceScreen.ItemList.ItemEntry::compare).forEach(item -> this.miningPreferredItems.addEntry(item));
      } else {
         this.miningPreferredItems.resize(200, this.f_96544_);
         this.combatPreferredItems.m_93410_(this.combatPreferredItems.m_93517_());
      }

      this.combatPreferredItems.m_93507_(this.f_96543_ / 2 - 204);
      this.miningPreferredItems.m_93507_(this.f_96543_ / 2 + 4);
      this.m_142416_(this.combatPreferredItems);
      this.m_142416_(this.miningPreferredItems);
      EditBox keywordEditBox = new EditBox(this.f_96541_.f_91062_, this.f_96543_ / 2 - 75, this.f_96544_ - 50, 150, 15, Component.m_237119_());
      keywordEditBox.m_94151_(keyword -> {
         this.combatPreferredItems.filter(keyword);
         this.miningPreferredItems.filter(keyword);
         this.combatPreferredItems.m_93410_(this.combatPreferredItems.m_93517_());
         this.miningPreferredItems.m_93410_(this.miningPreferredItems.m_93517_());
      });
      this.m_142416_(keywordEditBox);
      this.m_142416_(Button.m_253074_(Component.m_237113_("<"), button -> {
         this.miningPreferredItems.m_6702_().removeIf(itemEntry -> {
            String name = Component.m_237115_(itemEntry.itemStack.m_41720_().m_5524_()).getString();
            if (ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keywordEditBox.m_94155_()))) {
               this.miningItems.remove(itemEntry.itemStack.m_41720_());
               this.combatItems.add(itemEntry.itemStack.m_41720_());
               this.combatPreferredItems.createOpponent(itemEntry);
               return true;
            } else {
               return false;
            }
         });
         this.combatPreferredItems.m_93410_(this.combatPreferredItems.m_93517_());
         this.miningPreferredItems.m_93410_(this.miningPreferredItems.m_93517_());
      }).m_252987_(this.f_96543_ / 2 - 92, this.f_96544_ - 52, 15, 19).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_(">"), button -> {
         this.combatPreferredItems.m_6702_().removeIf(itemEntry -> {
            String name = Component.m_237115_(itemEntry.itemStack.m_41720_().m_5524_()).getString();
            if (ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keywordEditBox.m_94155_()))) {
               this.combatItems.remove(itemEntry.itemStack.m_41720_());
               this.miningItems.add(itemEntry.itemStack.m_41720_());
               this.miningPreferredItems.createOpponent(itemEntry);
               return true;
            } else {
               return false;
            }
         });
         this.combatPreferredItems.m_93410_(this.combatPreferredItems.m_93517_());
         this.miningPreferredItems.m_93410_(this.miningPreferredItems.m_93517_());
      }).m_252987_(this.f_96543_ / 2 + 77, this.f_96544_ - 52, 15, 19).m_253136_());
      this.m_142416_(Button.m_253074_(CommonComponents.f_130655_, button -> {
         ClientConfig.combatPreferredItems.clear();
         ClientConfig.miningPreferredItems.clear();
         ClientConfig.combatPreferredItems.addAll(this.combatItems);
         ClientConfig.miningPreferredItems.addAll(this.miningItems);
         ClientConfig.saveChanges();
         this.m_7379_();
      }).m_252987_(this.f_96543_ / 2 - 102, this.f_96544_ - 28, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(GUI_FIND_WEAPONS, button -> {
         resetItems();
         this.combatPreferredItems.m_6702_().clear();
         this.miningPreferredItems.m_6702_().clear();
         this.combatItems.clear();
         this.miningItems.clear();
         ClientConfig.combatPreferredItems.stream().sorted(ItemsPreferenceScreen.ItemList.ItemEntry::compare).forEach(item -> {
            this.combatItems.add(item);
            this.combatPreferredItems.addEntry(item);
         });
         ClientConfig.miningPreferredItems.stream().sorted(ItemsPreferenceScreen.ItemList.ItemEntry::compare).forEach(item -> {
            this.miningItems.add(item);
            this.miningPreferredItems.addEntry(item);
         });
         this.combatPreferredItems.m_93410_(0.0);
         this.miningPreferredItems.m_93410_(0.0);
      }).m_252987_(this.f_96543_ / 2 + 2, this.f_96544_ - 28, 100, 20).m_257505_(Tooltip.m_257550_(RESET_TOOLTIP)).m_253136_());
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_280039_(guiGraphics);
      this.combatPreferredItems.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      this.miningPreferredItems.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 16, 16777215);
      guiGraphics.m_280653_(this.f_96547_, SWAP_ALL, this.f_96543_ / 2 - 120, this.f_96544_ - 45, 16777215);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   static {
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(SwordItem.class);
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(WeaponItem.class);
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(BowItem.class);
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(CrossbowItem.class);
      WEAPON_CATEGORIZED_ITEM_CLASSES.add(TridentItem.class);
      TOOL_CATEGORIZED_ITEM_CLASSES.add(AxeItem.class);
      TOOL_CATEGORIZED_ITEM_CLASSES.add(HoeItem.class);
      TOOL_CATEGORIZED_ITEM_CLASSES.add(PickaxeItem.class);
      TOOL_CATEGORIZED_ITEM_CLASSES.add(ShovelItem.class);
   }

   class ItemList extends ObjectSelectionList<ItemsPreferenceScreen.ItemList.ItemEntry> {
      private final Component title;
      private final Component tooltip;
      private final Supplier<ItemsPreferenceScreen.ItemList> opponent;
      private final boolean left;

      public ItemList(int width, int height, boolean left, MutableComponent title, Component tooltip, Supplier<ItemsPreferenceScreen.ItemList> opponent) {
         super(ItemsPreferenceScreen.this.f_96541_, width, height, 32, height - 55, 22);
         this.title = title.m_130940_(ChatFormatting.UNDERLINE);
         this.tooltip = tooltip;
         this.opponent = opponent;
         this.left = left;
         this.m_93473_(true, 13);
         if (this.m_93511_() != null) {
            this.m_93494_((ItemsPreferenceScreen.ItemList.ItemEntry)this.m_93511_());
         }
      }

      public void resize(int width, int height) {
         this.f_93388_ = width;
         this.f_93389_ = height;
         this.f_93390_ = 32;
         this.f_93391_ = height - 55;
         this.f_93393_ = 0;
         this.f_93392_ = width;
      }

      protected void m_7415_(GuiGraphics guiGraphics, int x, int y) {
         guiGraphics.m_280614_(
            this.f_93386_.f_91062_,
            this.title,
            x + this.f_93388_ / 2 - this.f_93386_.f_91062_.m_92852_(this.title) / 2,
            Math.min(this.f_93390_ + 3, y),
            16777215,
            false
         );
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
         int width = this.f_93386_.f_91062_.m_92852_(this.title);
         int x = this.m_5747_();
         int y = this.f_93390_ + 4 - (int)this.m_93517_();
         x = x + this.f_93388_ / 2 - width / 2;
         y = Math.min(this.f_93390_ + 3, y);
         if (x < mouseX && y < mouseY && mouseX < x + width && mouseY < y + 15) {
            ItemsPreferenceScreen.this.m_257404_(this.tooltip);
         }
      }

      protected void addEntry(Item item) {
         this.m_6702_().add(new ItemsPreferenceScreen.ItemList.ItemEntry(item.m_7968_(), this.left));
      }

      protected void removeEntryHaving(Item item) {
         this.m_6702_().removeIf(entry -> entry.itemStack.m_41720_().equals(item));
      }

      protected void createOpponent(ItemsPreferenceScreen.ItemList.ItemEntry itemEntry) {
         ItemsPreferenceScreen.ItemList.ItemEntry newEntry = new ItemsPreferenceScreen.ItemList.ItemEntry(itemEntry.itemStack, this.left);
         this.m_7085_(newEntry);
         this.m_93494_(newEntry);
         this.m_7522_(newEntry);
      }

      public void filter(String keyword) {
         this.m_6702_().clear();
         Set<Item> list = this.left ? ItemsPreferenceScreen.this.combatItems : ItemsPreferenceScreen.this.miningItems;
         list.stream().filter(item -> {
            String name = Component.m_237115_(item.m_5524_()).getString();
            return ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keyword));
         }).sorted(ItemsPreferenceScreen.ItemList.ItemEntry::compare).forEach(this::addEntry);
      }

      class ItemEntry extends Entry<ItemsPreferenceScreen.ItemList.ItemEntry> {
         private static final Set<Item> UNRENDERABLE_ITEMS = Sets.newHashSet();
         private final ItemStack itemStack;
         private final Button switchButton;

         public static int compare(Item e1, Item e2) {
            String e1DisplayName = Component.m_237115_(e1.m_5524_()).getString();
            String e2DisplayName = Component.m_237115_(e2.m_5524_()).getString();
            return e1DisplayName.compareTo(e2DisplayName);
         }

         public ItemEntry(ItemStack itemStack, boolean left) {
            this.itemStack = itemStack;
            this.switchButton = Button.m_253074_(Component.m_237113_(left ? ">" : "<"), button -> {
               ItemList.this.m_93502_(this);
               ItemList.this.m_93410_(ItemList.this.m_93517_());
               ItemList.this.opponent.get().createOpponent(this);
               if (left) {
                  ItemsPreferenceScreen.this.combatItems.remove(itemStack.m_41720_());
                  ItemsPreferenceScreen.this.miningItems.add(itemStack.m_41720_());
               } else {
                  ItemsPreferenceScreen.this.miningItems.remove(itemStack.m_41720_());
                  ItemsPreferenceScreen.this.combatItems.add(itemStack.m_41720_());
               }
            }).m_252987_(0, 0, 20, 20).m_253136_();
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick
         ) {
            try {
               if (!UNRENDERABLE_ITEMS.contains(this.itemStack.m_41720_())) {
                  guiGraphics.m_280480_(this.itemStack, left + 4, top + 1);
               }
            } catch (Exception e) {
               UNRENDERABLE_ITEMS.add(this.itemStack.m_41720_());
            }

            this.switchButton.m_252865_(left + 170);
            this.switchButton.m_253211_(top - 1);
            this.switchButton.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
            Component component = this.itemStack.m_41786_();
            guiGraphics.m_280614_(ItemList.this.f_93386_.f_91062_, component, left + 30, top + 5, 16777215, false);
         }

         public boolean m_6375_(double mouseX, double mouseY, int button) {
            if (button == 0) {
               if (this.switchButton.m_5953_(mouseX, mouseY)) {
                  this.switchButton.m_7435_(ItemsPreferenceScreen.this.f_96541_.m_91106_());
                  this.switchButton.m_5691_();
               }

               ItemList.this.m_6987_(this);
               return true;
            } else {
               return false;
            }
         }

         public boolean equals(Object obj) {
            return obj instanceof ItemsPreferenceScreen.ItemList.ItemEntry itemEntry ? this.itemStack.equals(itemEntry.itemStack) : super.equals(obj);
         }

         public Component m_142172_() {
            return Component.m_237110_("narrator.select", new Object[]{this.itemStack.m_41786_()});
         }
      }
   }
}
