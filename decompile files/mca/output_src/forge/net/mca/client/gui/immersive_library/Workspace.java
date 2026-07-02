package forge.net.mca.client.gui.immersive_library;

import com.mojang.blaze3d.platform.NativeImage;
import forge.net.mca.client.gui.SkinLibraryScreen;
import forge.net.mca.client.gui.immersive_library.types.LiteContent;
import forge.net.mca.client.resources.SkinMeta;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.resources.data.skin.Hair;
import forge.net.mca.resources.data.skin.SkinListEntry;
import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;

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
   public final NativeImage currentImage;
   public final DynamicTexture backendTexture;
   public LinkedList<NativeImage> history = new LinkedList<>();
   private boolean dirty;
   private boolean dirtySinceSnapshot;

   public Workspace(NativeImage image) {
      this.currentImage = image;
      this.backendTexture = new DynamicTexture(this.currentImage);
      this.dirty = true;
   }

   public Workspace(NativeImage image, SkinMeta meta, LiteContent content) {
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
            x, y, this.currentImage.m_166408_(x, y), this.currentImage.m_166415_(x, y), this.currentImage.m_166418_(x, y), this.currentImage.m_85087_(x, y)
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
            int r = this.currentImage.m_166408_(x, y) & 255;
            int g = this.currentImage.m_166415_(x, y) & 255;
            int b = this.currentImage.m_166418_(x, y) & 255;
            int a = this.currentImage.m_85087_(x, y) & 255;
            int l = Mth.m_14045_((int)(0.2126 * r + 0.7152 * g + 0.0722 * b), 0, 255);
            this.currentImage.m_84988_(x, y, a << 24 | l << 16 | l << 8 | l);
         }
      }

      this.dirty = true;
   }

   public void addBrightness(int i) {
      this.saveSnapshot(true);

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = Mth.m_14045_((this.currentImage.m_166408_(x, y) & 255) + i, 0, 255);
            int g = Mth.m_14045_((this.currentImage.m_166415_(x, y) & 255) + i, 0, 255);
            int b = Mth.m_14045_((this.currentImage.m_166418_(x, y) & 255) + i, 0, 255);
            int a = this.currentImage.m_85087_(x, y) & 255;
            this.currentImage.m_84988_(x, y, a << 24 | r << 16 | g << 8 | b);
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
            int a = this.currentImage.m_85087_(x, y) & 255;
            if (a > 0) {
               average += this.currentImage.m_166408_(x, y) & 255;
               average += this.currentImage.m_166418_(x, y) & 255;
               average += this.currentImage.m_166415_(x, y) & 255;
               samples += 3;
            }
         }
      }

      average /= samples;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = Mth.m_14045_((int)(((this.currentImage.m_166408_(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int g = Mth.m_14045_((int)(((this.currentImage.m_166415_(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int b = Mth.m_14045_((int)(((this.currentImage.m_166418_(x, y) & 255) - average) * (1.0F + c) + average), 0, 255);
            int a = this.currentImage.m_85087_(x, y) & 255;
            this.currentImage.m_84988_(x, y, a << 24 | r << 16 | g << 8 | b);
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
               x, y, this.currentImage.m_166408_(x, y), this.currentImage.m_166415_(x, y), this.currentImage.m_166418_(x, y), this.currentImage.m_85087_(x, y)
            )
         );

         while (!todo.isEmpty()) {
            Workspace.FillTodo entry = todo.poll();
            if (this.currentImage.m_85087_(entry.x, entry.y) != 0) {
               this.currentImage.m_84988_(entry.x, entry.y, 0);
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

         NativeImage image = new NativeImage(64, 64, false);
         image.m_85054_(this.currentImage);
         this.history.add(image);
      }
   }

   public void undo() {
      if (this.history.size() > 0) {
         NativeImage image = this.history.removeLast();
         this.currentImage.m_85054_(image);
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
