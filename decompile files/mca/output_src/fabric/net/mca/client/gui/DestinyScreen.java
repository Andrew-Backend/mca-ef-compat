package fabric.net.mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.MCAClient;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.DestinyMessage;
import fabric.net.mca.util.compat.ButtonWidget;
import fabric.net.mca.util.localization.FlowingText;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5250;

public class DestinyScreen extends VillagerEditorScreen {
   private static final class_2960 LOGO_TEXTURE = new class_2960("mca:textures/banner.png");
   private final LinkedList<class_2561> story = new LinkedList<>();
   private String location;
   private boolean teleported = false;
   private final boolean allowTeleportation;
   private ButtonWidget acceptWidget;

   public DestinyScreen(UUID playerUUID, boolean allowTeleportation) {
      super(playerUUID, playerUUID);
      this.allowTeleportation = allowTeleportation;
   }

   @Override
   public boolean method_25421() {
      return true;
   }

   public void method_25419() {
      if (!this.page.equals("general") && !this.page.equals("story")) {
         this.setPage("destiny");
      }
   }

   @Override
   protected String[] getPages() {
      LinkedList<String> pages = new LinkedList<>();
      pages.add("general");
      if (Config.getServerConfig().allowBodyCustomizationInDestiny) {
         pages.add("body");
         pages.add("head");
      }

      if (Config.getServerConfig().allowTraitCustomizationInDestiny) {
         pages.add("traits");
      }

      return pages.toArray(new String[0]);
   }

   public void method_25420(class_332 context) {
      assert class_310.method_1551().field_1687 != null;
      this.method_25434(context);
   }

   private void drawScaledText(class_332 context, class_2561 text, int x, int y, float scale) {
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_22905(scale, scale, scale);
      context.method_27534(this.field_22793, text, (int)(x / scale), (int)(y / scale), -1);
      matrices.method_22909();
   }

   @Override
   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      class_4587 matrices = context.method_51448();
      switch (this.page) {
         case "general":
            this.drawScaledText(context, class_2561.method_43471("gui.destiny.whoareyou"), this.field_22789 / 2, this.field_22790 / 2 - 24, 1.5F);
            matrices.method_22903();
            matrices.method_22905(0.25F, 0.25F, 0.25F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.method_25290(LOGO_TEXTURE, this.field_22789 * 2 - 512, -40, 0.0F, 0.0F, 1024, 512, 1024, 512);
            matrices.method_22909();
            break;
         case "destiny":
            this.drawScaledText(context, class_2561.method_43471("gui.destiny.journey"), this.field_22789 / 2, this.field_22790 / 2 - 48, 1.5F);
            break;
         case "story":
            List<class_2561> text = FlowingText.wrap(this.story.getFirst(), 256);
            int y = (int)(this.field_22790 / 2.0 - 20.0 - 7.5F * text.size());

            for (class_2561 t : text) {
               this.drawScaledText(context, t, this.field_22789 / 2, y, 1.25F);
               y += 15;
            }
      }
   }

   @Override
   protected boolean shouldDrawEntity() {
      return !this.page.equals("general") && !this.page.equals("destiny") && !this.page.equals("story") && super.shouldDrawEntity();
   }

   protected String getPath(String location) {
      String[] split = location.split(":");
      return split[split.length - 1];
   }

   @Override
   protected void setPage(String page) {
      if (page.equals("destiny") && !this.allowTeleportation) {
         NetworkHandler.sendToServer(new DestinyMessage(true));
         MCAClient.getDestinyManager().allowClosing();
         super.method_25419();
      } else if (page.equals("destiny") && Config.getServerConfig().destinySpawnLocations.size() == 1) {
         this.selectStory(Config.getServerConfig().destinySpawnLocations.get(0));
      } else {
         this.page = page;
         this.method_37067();
         switch (page) {
            case "general":
               this.drawName(this.field_22789 / 2 - 87, this.field_22790 / 2, namex -> {
                  this.updateName(namex);
                  if (this.acceptWidget != null) {
                     this.acceptWidget.field_22763 = !MCA.isBlankString(namex);
                  }
               });
               this.drawGender(this.field_22789 / 2 - 87, this.field_22790 / 2 + 24);
               this.addModelSelectionWidgets(this.field_22789 / 2 - 87, this.field_22790 / 2 + 24 + 22);
               this.acceptWidget = (ButtonWidget)this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 32, this.field_22790 / 2 + 60 + 22, 64, 20, class_2561.method_43471("gui.button.accept"), sender -> {
                     if (Config.getServerConfig().allowBodyCustomizationInDestiny) {
                        this.setPage("body");
                     } else if (Config.getServerConfig().allowTraitCustomizationInDestiny) {
                        this.setPage("traits");
                     } else {
                        this.setPage("destiny");
                     }
                  })
               );
               break;
            case "destiny":
               int x = 0;
               int y = 0;

               for (String location : Config.getServerConfig().destinySpawnLocations) {
                  int rows = (int)Math.ceil(Config.getServerConfig().destinySpawnLocations.size() / 3.0F);
                  float offsetX = y + 1 == rows ? (2 - (Config.getServerConfig().destinySpawnLocations.size() - 1) % 3) / 2.0F : 0.0F;
                  float offsetY = Math.max(0, 3 - rows) / 2.0F;
                  class_5250 name = class_2561.method_43471("gui.destiny." + this.getPath(location));
                  this.method_37063(
                     new ButtonWidget(
                        (int)(this.field_22789 / 2.0F - 144.0F + (x + offsetX) * 96.0F),
                        (int)(this.field_22790 / 2.0F + (y + offsetY) * 20.0F - 16.0F),
                        96,
                        20,
                        name,
                        sender -> this.selectStory(location)
                     )
                  );
                  if (++x >= 3) {
                     x = 0;
                     y++;
                  }
               }
               break;
            case "story":
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 48, this.field_22790 / 2 + 32, 96, 20, class_2561.method_43471("gui.destiny.next"), sender -> {
                     if (!this.teleported) {
                        NetworkHandler.sendToServer(new DestinyMessage(this.location));
                        MCAClient.getDestinyManager().allowClosing();
                        this.teleported = true;
                     }

                     if (this.story.size() > 1) {
                        this.story.remove(0);
                     } else {
                        NetworkHandler.sendToServer(new DestinyMessage(true));
                        super.method_25419();
                     }
                  })
               );
               break;
            default:
               super.setPage(page);
         }
      }
   }

   private void selectStory(String location) {
      this.story.clear();
      this.story.add(class_2561.method_43471("destiny.story.reason"));
      Map<String, String> map = Config.getServerConfig().destinyLocationsToTranslationMap;
      this.story.add(class_2561.method_43471(map.getOrDefault(location, map.getOrDefault("default", "missing_default"))));
      this.story.add(class_2561.method_43471("destiny.story." + this.getPath(location)));
      this.location = location;
      this.setPage("story");
   }
}
