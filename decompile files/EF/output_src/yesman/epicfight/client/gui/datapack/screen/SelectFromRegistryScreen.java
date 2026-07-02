package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraftforge.registries.IForgeRegistry;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

public class SelectFromRegistryScreen<T> extends Screen {
   private final SelectFromRegistryScreen<T>.RegistryList registryList;
   private final Screen parentScreen;
   private final Consumer<T> onPressRow;
   private final BiConsumer<String, T> onAccept;
   private final BiConsumer<String, T> onCancel;

   public SelectFromRegistryScreen(
      Screen parentScreen, IForgeRegistry<T> registry, BiConsumer<String, T> onAccept, BiConsumer<String, T> onCancel, Predicate<T> filter
   ) {
      this(parentScreen, registry, onAccept, onCancel, select -> {}, filter);
   }

   public SelectFromRegistryScreen(
      Screen parentScreen,
      IForgeRegistry<T> registry,
      BiConsumer<String, T> onAccept,
      BiConsumer<String, T> onCancel,
      Consumer<T> onPressRow,
      Predicate<T> filter
   ) {
      super(Component.m_237110_("gui.epicfight.select", new Object[]{ParseUtil.snakeToSpacedCamel(registry.getRegistryName().m_135815_())}));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      Map<ResourceLocation, T> filteredItems = Maps.newHashMap();
      registry.getValues().stream().filter(filter).forEach(value -> filteredItems.put(registry.getKey(value), (T)value));
      this.registryList = new SelectFromRegistryScreen.RegistryList(
         parentScreen.getMinecraft(), this.f_96543_, this.f_96544_, 36, this.f_96544_ - 16, 21, filteredItems
      );
      this.onPressRow = onPressRow;
      this.onAccept = onAccept;
      this.onCancel = onCancel;
   }

   public SelectFromRegistryScreen(
      Screen parentScreen,
      Set<Pair<ResourceLocation, T>> entries,
      String title,
      BiConsumer<String, T> onAccept,
      BiConsumer<String, T> onCancel,
      Consumer<T> onPressRow,
      Predicate<T> filter
   ) {
      super(Component.m_237110_("gui.epicfight.select", new Object[]{ParseUtil.snakeToSpacedCamel(title)}));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      Map<ResourceLocation, T> filteredItems = entries.stream()
         .filter(entry -> filter.test((T)entry.getSecond()))
         .reduce(Maps.newHashMap(), (map, element) -> {
            map.put((ResourceLocation)element.getFirst(), (T)element.getSecond());
            return map;
         }, (map1, map2) -> {
            map1.putAll(map2);
            return map1;
         });
      this.registryList = new SelectFromRegistryScreen.RegistryList(
         parentScreen.getMinecraft(), this.f_96543_, this.f_96544_, 36, this.f_96544_ - 16, 21, filteredItems
      );
      this.onPressRow = onPressRow;
      this.onAccept = onAccept;
      this.onCancel = onCancel;
   }

   protected void m_7856_() {
      this.registryList.m_93437_(this.f_96543_, this.f_96544_, 36, this.f_96544_ - 32);
      EditBox editBox = new EditBox(this.f_96541_.f_91062_, this.f_96543_ / 2, 12, this.f_96543_ / 2 - 12, 16, Component.m_237113_(EpicFightMod.prefix("")));
      editBox.m_94151_(this.registryList::applyFilter);
      this.m_142416_(this.registryList);
      this.m_142416_(editBox);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button$1 -> {
                  if (this.registryList.m_93511_() == null) {
                     this.f_96541_.m_91152_(new MessageScreen("", "Select an item from the list", this, button$2 -> this.f_96541_.m_91152_(this), 180, 60));
                  } else {
                     try {
                        this.onAccept
                           .accept(
                              ((SelectFromRegistryScreen.RegistryList.RegistryEntry)this.registryList.m_93511_()).name,
                              (T)((SelectFromRegistryScreen.RegistryList.RegistryEntry)this.registryList.m_93511_()).item
                           );
                        this.f_96541_.m_91152_(this.parentScreen);
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen("", e.getMessage(), this.parentScreen, button$2 -> this.f_96541_.m_91152_(this.parentScreen), 180, 70)
                                 .autoCalculateHeight()
                           );
                     }
                  }
               }
            )
            .m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 28)
            .m_253046_(160, 21)
            .m_253136_()
      );
      this.m_142416_(Button.m_253074_(CommonComponents.f_130656_, button -> {
         this.onCancel.accept("", null);
         this.f_96541_.m_91152_(this.parentScreen);
      }).m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 28).m_253046_(160, 21).m_253136_());
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_280039_(guiGraphics);
      guiGraphics.m_280430_(this.f_96547_, this.f_96539_, 20, 16, 16777215);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   class RegistryList extends ObjectSelectionList<SelectFromRegistryScreen<T>.RegistryList.RegistryEntry> {
      private final Map<ResourceLocation, T> registry;

      public RegistryList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, Map<ResourceLocation, T> registry) {
         super(minecraft, width, height, y0, y1, itemHeight);
         this.registry = registry;
         registry.entrySet()
            .stream()
            .sorted((entry1, entry2) -> entry1.getKey().toString().compareTo(entry2.getKey().toString()))
            .forEach(entry -> this.m_7085_(new SelectFromRegistryScreen.RegistryList.RegistryEntry(entry.getValue(), entry.getKey().toString())));
      }

      public void setSelected(@Nullable SelectFromRegistryScreen<T>.RegistryList.RegistryEntry selEntry) {
         SelectFromRegistryScreen.this.onPressRow.accept((T)selEntry.item);
         super.m_6987_(selEntry);
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public void applyFilter(String keyward) {
         this.m_93410_(0.0);
         this.m_6702_().clear();
         this.registry
            .entrySet()
            .stream()
            .sorted((entry1, entry2) -> entry1.getKey().toString().compareTo(entry2.getKey().toString()))
            .filter(entry -> StringUtil.m_14408_(keyward) ? true : entry.getKey().toString().contains(keyward))
            .map(entry -> new SelectFromRegistryScreen.RegistryList.RegistryEntry(entry.getValue(), entry.getKey().toString()))
            .forEach(x$0 -> this.m_7085_(x$0));
      }

      class RegistryEntry extends Entry<SelectFromRegistryScreen<T>.RegistryList.RegistryEntry> {
         private final Object item;
         private final String name;

         public RegistryEntry(T item, String name) {
            this.item = item;
            this.name = name;
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            guiGraphics.m_280056_(SelectFromRegistryScreen.this.f_96541_.f_91062_, this.name, left + 25, top + 5, 16777215, false);
         }

         public Component m_142172_() {
            return Component.m_237115_("narrator.select");
         }

         public boolean m_6375_(double mouseX, double mouseY, int button) {
            if (button == 0) {
               if (RegistryList.this.m_93511_() == this) {
                  try {
                     SelectFromRegistryScreen.this.onAccept.accept(this.name, (T)this.item);
                     SelectFromRegistryScreen.this.f_96541_.m_91152_(SelectFromRegistryScreen.this.parentScreen);
                  } catch (Exception e) {
                     SelectFromRegistryScreen.this.f_96541_
                        .m_91152_(
                           new MessageScreen(
                                 "",
                                 e.getMessage(),
                                 SelectFromRegistryScreen.this.parentScreen,
                                 button$2 -> SelectFromRegistryScreen.this.f_96541_.m_91152_(SelectFromRegistryScreen.this.parentScreen),
                                 180,
                                 70
                              )
                              .autoCalculateHeight()
                        );
                  }

                  return true;
               } else {
                  RegistryList.this.setSelected(this);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }
}
