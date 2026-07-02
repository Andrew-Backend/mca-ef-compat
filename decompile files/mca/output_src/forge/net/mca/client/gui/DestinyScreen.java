package forge.net.mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.MCAClient;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.c2s.DestinyMessage;
import forge.net.mca.util.compat.ButtonWidget;
import forge.net.mca.util.localization.FlowingText;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class DestinyScreen extends VillagerEditorScreen {
   private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("mca:textures/banner.png");
   private final LinkedList<Component> story = new LinkedList<>();
   private String location;
   private boolean teleported = false;
   private final boolean allowTeleportation;
   private ButtonWidget acceptWidget;

   public DestinyScreen(UUID playerUUID, boolean allowTeleportation) {
      super(playerUUID, playerUUID);
      this.allowTeleportation = allowTeleportation;
   }

   @Override
   public boolean m_7043_() {
      return true;
   }

   public void m_7379_() {
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

   public void m_280273_(GuiGraphics context) {
      assert Minecraft.m_91087_().f_91073_ != null;
      this.m_280039_(context);
   }

   private void drawScaledText(GuiGraphics context, Component text, int x, int y, float scale) {
      PoseStack matrices = context.m_280168_();
      matrices.m_85836_();
      matrices.m_85841_(scale, scale, scale);
      context.m_280653_(this.f_96547_, text, (int)(x / scale), (int)(y / scale), -1);
      matrices.m_85849_();
   }

   @Override
   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.m_88315_(context, mouseX, mouseY, delta);
      PoseStack matrices = context.m_280168_();
      switch (this.page) {
         case "general":
            this.drawScaledText(context, Component.m_237115_("gui.destiny.whoareyou"), this.f_96543_ / 2, this.f_96544_ / 2 - 24, 1.5F);
            matrices.m_85836_();
            matrices.m_85841_(0.25F, 0.25F, 0.25F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.m_280163_(LOGO_TEXTURE, this.f_96543_ * 2 - 512, -40, 0.0F, 0.0F, 1024, 512, 1024, 512);
            matrices.m_85849_();
            break;
         case "destiny":
            this.drawScaledText(context, Component.m_237115_("gui.destiny.journey"), this.f_96543_ / 2, this.f_96544_ / 2 - 48, 1.5F);
            break;
         case "story":
            List<Component> text = FlowingText.wrap(this.story.getFirst(), 256);
            int y = (int)(this.f_96544_ / 2.0 - 20.0 - 7.5F * text.size());

            for (Component t : text) {
               this.drawScaledText(context, t, this.f_96543_ / 2, y, 1.25F);
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
         super.m_7379_();
      } else if (page.equals("destiny") && Config.getServerConfig().destinySpawnLocations.size() == 1) {
         this.selectStory(Config.getServerConfig().destinySpawnLocations.get(0));
      } else {
         this.page = page;
         this.m_169413_();
         switch (page) {
            case "general":
               this.drawName(this.f_96543_ / 2 - 87, this.f_96544_ / 2, namex -> {
                  this.updateName(namex);
                  if (this.acceptWidget != null) {
                     this.acceptWidget.f_93623_ = !MCA.isBlankString(namex);
                  }
               });
               this.drawGender(this.f_96543_ / 2 - 87, this.f_96544_ / 2 + 24);
               this.addModelSelectionWidgets(this.f_96543_ / 2 - 87, this.f_96544_ / 2 + 24 + 22);
               this.acceptWidget = (ButtonWidget)this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 - 32, this.f_96544_ / 2 + 60 + 22, 64, 20, Component.m_237115_("gui.button.accept"), sender -> {
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
                  MutableComponent name = Component.m_237115_("gui.destiny." + this.getPath(location));
                  this.m_142416_(
                     new ButtonWidget(
                        (int)(this.f_96543_ / 2.0F - 144.0F + (x + offsetX) * 96.0F),
                        (int)(this.f_96544_ / 2.0F + (y + offsetY) * 20.0F - 16.0F),
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
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 48, this.f_96544_ / 2 + 32, 96, 20, Component.m_237115_("gui.destiny.next"), sender -> {
                  if (!this.teleported) {
                     NetworkHandler.sendToServer(new DestinyMessage(this.location));
                     MCAClient.getDestinyManager().allowClosing();
                     this.teleported = true;
                  }

                  if (this.story.size() > 1) {
                     this.story.remove(0);
                  } else {
                     NetworkHandler.sendToServer(new DestinyMessage(true));
                     super.m_7379_();
                  }
               }));
               break;
            default:
               super.setPage(page);
         }
      }
   }

   private void selectStory(String location) {
      this.story.clear();
      this.story.add(Component.m_237115_("destiny.story.reason"));
      Map<String, String> map = Config.getServerConfig().destinyLocationsToTranslationMap;
      this.story.add(Component.m_237115_(map.getOrDefault(location, map.getOrDefault("default", "missing_default"))));
      this.story.add(Component.m_237115_("destiny.story." + this.getPath(location)));
      this.location = location;
      this.setPage("story");
   }
}
