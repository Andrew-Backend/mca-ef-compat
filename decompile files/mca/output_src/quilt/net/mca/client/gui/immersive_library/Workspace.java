package quilt.net.mca.client.gui.immersive_library;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_3532;
import quilt.net.mca.client.gui.SkinLibraryScreen;
import quilt.net.mca.client.gui.immersive_library.types.LiteContent;
import quilt.net.mca.client.resources.SkinMeta;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.data.skin.Clothing;
import quilt.net.mca.resources.data.skin.Hair;
import quilt.net.mca.resources.data.skin.SkinListEntry;

public final class Workspace {
   private static final int MAX_HISTORY = 50;
   public SkinLibraryScreen.SkinType skinType;
   public int contentid = -1;
   public int temperature;
   public double chance = 1.0;
   public String title = "Unnamed Asset";
   public String profession;
   public Gender gender = Gender.NEUTRAL;
   public int fillToolThreshold = 32;
   public final class_1011 currentImage;
   public final class_1043 backendTexture;
   public LinkedList<class_1011> history = new LinkedList<>();
   private boolean dirty;
   private boolean dirtySinceSnapshot;

   public Workspace(class_1011 image) {
      this.currentImage = image;
      this.backendTexture = new class_1043(this.currentImage);
      this.dirty = true;
   }

   public Workspace(class_1011 image, SkinMeta meta, LiteContent content) {
      this(image);
      this.contentid = content.contentid();
      this.title = content.title();
      this.skinType = content.hasTag("clothing") ? SkinLibraryScreen.SkinType.CLOTHING : SkinLibraryScreen.SkinType.HAIR;
      this.chance = meta.getChance();
      this.gender = meta.getGender();
      this.profession = meta.getProfession();
      this.temperature = meta.getTemperature();
   }

   public SkinListEntry toListEntry() {
      return this.skinType == SkinLibraryScreen.SkinType.CLOTHING
         ? new Clothing("immersive_library:" + this.contentid, this.profession, this.temperature, false, this.gender)
         : new Hair("immersive_library:" + this.contentid);
   }

   private void fillDeleteFunc(Workspace.FillTodo entry, Queue<Workspace.FillTodo> todo, int x, int y) {
      if (x >= 0 && y >= 0 && x < 64 && y < 64) {
         Workspace.FillTodo nextEntry = new Workspace.FillTodo(
            x,
            y,
            this.currentImage.method_35623(x, y),
            this.currentImage.method_35625(x, y),
            this.currentImage.method_35626(x, y),
            this.currentImage.method_4311(x, y)
         );
         if (Math.abs(nextEntry.red - entry.red) <= this.fillToolThreshold) {
            if (Math.abs(nextEntry.green - entry.green) <= this.fillToolThreshold) {
               if (Math.abs(nextEntry.blue - entry.blue) <= this.fillToolThreshold) {
                  if (Math.abs(nextEntry.alpha - entry.alpha) <= this.fillToolThreshold) {
                     todo.add(nextEntry);
                  }
               }
            }
         }
      }
   }

   public void removeSaturation() {
      this.saveSnapshot(true);

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = this.currentImage.method_35623(x, y) & 255;
            int g = this.currentImage.method_35625(x, y) & 255;
            int b = this.currentImage.method_35626(x, y) & 255;
            int a = this.currentImage.method_4311(x, y) & 255;
            int l = class_3532.method_15340((int)(0.2126 * r + 0.7152 * g + 0.0722 * b), 0, 255);
            this.currentImage.method_4305(x, y, a << 24 | l << 16 | l << 8 | l);
         }
      }

      this.dirty = true;
   }

   public void addBrightness(int i) {
      this.saveSnapshot(true);

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = class_3532.method_15340((this.currentImage.method_35623(x, y) & 255) + i, 0, 255);
            int g = class_3532.method_15340((this.currentImage.method_35625(x, y) & 255) + i, 0, 255);
            int b = class_3532.method_15340((this.currentImage.method_35626(x, y) & 255) + i, 0, 255);
            int a = this.currentImage.method_4311(x, y) & 255;
            this.currentImage.method_4305(x, y, a << 24 | r << 16 | g << 8 | b);
         }
      }

      this.dirty = true;
   }

   public void addContrast(float c) {
      this.saveSnapshot(true);
      int average = 0;
      int samples = 0;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int a = this.currentImage.method_4311(x, y) & 255;
            if (a > 0) {
               average += this.currentImage.method_35623(x, y) & 255;
               average += this.currentImage.method_35626(x, y) & 255;
               average += this.currentImage.method_35625(x, y) & 255;
               samples += 3;
            }
         }
      }

      average /= samples;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = class_3532.method_15340((int)(((this.currentImage.method_35623(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int g = class_3532.method_15340((int)(((this.currentImage.method_35625(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int b = class_3532.method_15340((int)(((this.currentImage.method_35626(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int a = this.currentImage.method_4311(x, y) & 255;
            this.currentImage.method_4305(x, y, a << 24 | r << 16 | g << 8 | b);
         }
      }

      this.dirty = true;
   }

   public void fillDelete(int x, int y) {
      if (x >= 0 && y >= 0 && x < 64 && y < 64) {
         this.saveSnapshot(true);
         Queue<Workspace.FillTodo> todo = new LinkedList<>();
         todo.add(
            new Workspace.FillTodo(
               x,
               y,
               this.currentImage.method_35623(x, y),
               this.currentImage.method_35625(x, y),
               this.currentImage.method_35626(x, y),
               this.currentImage.method_4311(x, y)
            )
         );

         while (!todo.isEmpty()) {
            Workspace.FillTodo entry = todo.poll();
            if (this.currentImage.method_4311(entry.x, entry.y) != 0) {
               this.currentImage.method_4305(entry.x, entry.y, 0);
               this.dirty = true;

               for (int ox = -1; ox <= 1; ox++) {
                  for (int oy = -1; oy <= 1; oy++) {
                     if (ox != 0 || oy != 0) {
                        this.fillDeleteFunc(entry, todo, entry.x + ox, entry.y + oy);
                     }
                  }
               }
            }
         }
      }
   }

   public boolean validPixel(int x, int y) {
      return x >= 0 && x < 64 && y >= 0 && y < 64;
   }

   public void saveSnapshot(boolean always) {
      if (always || this.dirtySinceSnapshot) {
         this.dirtySinceSnapshot = false;

         while (this.history.size() > 50) {
            this.history.removeFirst().close();
         }

         class_1011 image = new class_1011(64, 64, false);
         image.method_4317(this.currentImage);
         this.history.add(image);
      }
   }

   public void undo() {
      if (this.history.size() > 0) {
         class_1011 image = this.history.removeLast();
         this.currentImage.method_4317(image);
         image.close();
         this.dirty = true;
         this.dirtySinceSnapshot = false;
      }
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty(boolean dirty) {
      this.dirty = dirty;
      if (dirty) {
         this.dirtySinceSnapshot = true;
      }
   }

   private record FillTodo(int x, int y, int red, int green, int blue, int alpha) {
   }
}
