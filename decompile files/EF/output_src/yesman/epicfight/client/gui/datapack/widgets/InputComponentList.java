package yesman.epicfight.client.gui.datapack.widgets;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public abstract class InputComponentList<T> extends ContainerObjectSelectionList<InputComponentList<T>.InputComponentEntry> {
   private final Screen owner;
   private final List<DataBindingComponent<?, ?>> dataBindingComponent = Lists.newArrayList();
   private InputComponentList<T>.InputComponentEntry lastEntry;
   private InputComponentList<T>.InputComponentEntry focusingEntry;

   public InputComponentList(Screen owner, int width, int height, int y0, int y1, int itemHeight) {
      super(owner.getMinecraft(), width, height, y0, y1, itemHeight);
      this.owner = owner;
      this.m_93496_(false);
   }

   public int m_5759_() {
      return this.f_93388_;
   }

   protected int m_5756_() {
      return this.f_93392_ - 6;
   }

   public int nextStart(int spacing) {
      int xPos;
      if (this.lastEntry.children.size() == 0) {
         xPos = 0;
      } else {
         ResizableComponent lastWidget = this.lastEntry.children.get(this.lastEntry.children.size() - 1);
         xPos = lastWidget._getX() + lastWidget._getWidth();
      }

      return xPos + spacing;
   }

   public void addComponentCurrentRow(ResizableComponent inputWidget) {
      this.lastEntry.children.add(inputWidget);
      if (inputWidget instanceof DataBindingComponent<?, ?> dataBindingComponent) {
         this.dataBindingComponent.add(dataBindingComponent);
      }
   }

   public void setDataBindingComponenets(Object[] values) {
      for (int i = 0; i < values.length; i++) {
         DataBindingComponent<Object, Object> dataBinder = (DataBindingComponent<Object, Object>)this.dataBindingComponent.get(i);

         try {
            Consumer<Object> consumer = dataBinder._getResponder();
            dataBinder._setResponder(null);
            dataBinder._setValue(values[i]);
            dataBinder._setResponder(consumer);
         } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
               String.format("Error while binding %sst component [%s] because of %s", String.valueOf(i), dataBinder._getMessage(), e.getMessage())
            );
         }
      }
   }

   public void newRow() {
      this.lastEntry = new InputComponentList.InputComponentEntry();
      this.m_7085_(this.lastEntry);
   }

   public void clearComponents() {
      this.m_6702_().clear();
      this.focusingEntry = null;
      this.dataBindingComponent.clear();
   }

   public void setComponentsActive(boolean active) {
      for (InputComponentList<T>.InputComponentEntry e : this.m_6702_()) {
         for (ResizableComponent componenet : e.children) {
            componenet._setActive(active);
         }
      }
   }

   public void resetComponents() {
      this.dataBindingComponent.forEach(dataBinder -> dataBinder.reset());
   }

   public <O extends ResizableComponent> O getComponent(int row, int column) {
      return (O)((InputComponentList.InputComponentEntry)this.m_6702_().get(row)).children.get(column);
   }

   public abstract void importTag(T var1);

   public void m_93437_(int width, int height, int y0, int y1) {
      super.m_93437_(width, height, y0, y1);

      for (InputComponentList<T>.InputComponentEntry entry : this.m_6702_()) {
         for (ResizableComponent widget : entry.m_6702_()) {
            widget.resize(this.m_264198_());
         }
      }
   }

   public void m_93507_(int xPos) {
      super.m_93507_(xPos);

      for (InputComponentList<T>.InputComponentEntry entry : this.m_6702_()) {
         for (ResizableComponent widget : entry.m_6702_()) {
            widget.resize(this.m_264198_());
         }
      }
   }

   public void tick() {
      for (InputComponentList<T>.InputComponentEntry entry : this.m_6702_()) {
         for (ResizableComponent widget : entry.m_6702_()) {
            widget._tick();
         }
      }
   }

   public boolean m_6375_(double x, double y, int button) {
      if (!this.m_5953_(x, y)) {
         return false;
      }

      this.m_93481_(x, y, button);
      if (this.focusingEntry != null) {
         int i = this.m_6702_().indexOf(this.focusingEntry);
         int j1 = this.m_7610_(i);
         int k1 = this.m_93485_(i);
         if (k1 >= this.f_93390_ && j1 <= this.f_93391_) {
            boolean pressed = false;

            for (GuiEventListener guiEventListener : this.focusingEntry.m_6702_()) {
               boolean mouseClicked = guiEventListener.m_6375_(x, y, button);
               pressed |= mouseClicked;
               if (mouseClicked) {
                  this.owner.m_7522_(guiEventListener);
               }
            }

            if (pressed) {
               return false;
            }
         }
      }

      for (int i = 0; i < this.m_6702_().size(); i++) {
         InputComponentList<T>.InputComponentEntry entry = (InputComponentList.InputComponentEntry)this.m_6702_().get(i);
         if (entry != this.focusingEntry) {
            int j1 = this.m_7610_(i);
            int k1 = this.m_93485_(i);
            if (k1 >= this.f_93390_ && j1 <= this.f_93391_) {
               boolean pressed = false;

               for (GuiEventListener guiEventListener : entry.m_6702_()) {
                  boolean mouseClicked = guiEventListener.m_6375_(x, y, button);
                  pressed |= mouseClicked;
                  if (mouseClicked) {
                     this.owner.m_7522_(guiEventListener);
                  }
               }

               if (pressed) {
                  this.focusingEntry = entry;
                  return false;
               }
            }
         }
      }

      return false;
   }

   public boolean m_6050_(double x, double y, double amount) {
      for (int i = 0; i < this.m_6702_().size(); i++) {
         InputComponentList<T>.InputComponentEntry entry = (InputComponentList.InputComponentEntry)this.m_6702_().get(i);
         int j1 = this.m_7610_(i);
         int k1 = this.m_93485_(i);
         if (k1 >= this.f_93390_ && j1 <= this.f_93391_ && entry.m_94729_(x, y).filter(component -> component.m_6050_(x, y, amount)).isPresent()) {
            return true;
         }
      }

      return super.m_6050_(x, y, amount);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      for (int i = 0; i < this.m_6702_().size(); i++) {
         InputComponentList<T>.InputComponentEntry entry = (InputComponentList.InputComponentEntry)this.m_6702_().get(i);
         int j1 = this.m_7610_(i);
         int k1 = this.m_93485_(i);
         if (k1 >= this.f_93390_
            && j1 <= this.f_93391_
            && entry.m_94729_(mouseX, mouseY).filter(component -> component.m_7979_(mouseX, mouseY, button, dx, dy)).isPresent()) {
            return true;
         }
      }

      return super.m_7979_(mouseX, mouseY, button, dx, dy);
   }

   public class InputComponentEntry extends Entry<InputComponentList<T>.InputComponentEntry> {
      final List<ResizableComponent> children = Lists.newArrayList();

      public Optional<GuiEventListener> m_94729_(double x, double y) {
         for (GuiEventListener guieventlistener : this.children) {
            if (guieventlistener.m_5953_(x, y)) {
               return Optional.of(guieventlistener);
            }
         }

         return Optional.empty();
      }

      public void m_6311_(
         GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
      ) {
         for (ResizableComponent widget : this.children) {
            widget.relocateY(InputComponentList.this.owner.m_264198_(), top + InputComponentList.this.f_93387_ / 2 - widget._getHeight() / 2);
            widget._renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
         }
      }

      public List<? extends ResizableComponent> m_6702_() {
         return this.children;
      }

      public List<? extends ResizableComponent> m_142437_() {
         return this.children;
      }
   }
}
