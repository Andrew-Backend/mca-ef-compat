package fabric.net.mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.client.gui.immersive_library.Api;
import fabric.net.mca.client.gui.immersive_library.Auth;
import fabric.net.mca.client.gui.immersive_library.SkinCache;
import fabric.net.mca.client.gui.immersive_library.Utils;
import fabric.net.mca.client.gui.immersive_library.Workspace;
import fabric.net.mca.client.gui.immersive_library.responses.ContentIdResponse;
import fabric.net.mca.client.gui.immersive_library.responses.ContentListResponse;
import fabric.net.mca.client.gui.immersive_library.responses.ErrorResponse;
import fabric.net.mca.client.gui.immersive_library.responses.IsAuthResponse;
import fabric.net.mca.client.gui.immersive_library.responses.Response;
import fabric.net.mca.client.gui.immersive_library.responses.SuccessResponse;
import fabric.net.mca.client.gui.immersive_library.responses.UserResponse;
import fabric.net.mca.client.gui.immersive_library.types.LiteContent;
import fabric.net.mca.client.gui.immersive_library.types.User;
import fabric.net.mca.client.gui.widget.ColorPickerWidget;
import fabric.net.mca.client.gui.widget.HorizontalColorPickerWidget;
import fabric.net.mca.client.gui.widget.HorizontalGradientWidget;
import fabric.net.mca.client.gui.widget.IntegerSliderWidget;
import fabric.net.mca.client.gui.widget.ItemButtonWidget;
import fabric.net.mca.client.gui.widget.ToggleableTooltipButtonWidget;
import fabric.net.mca.client.gui.widget.ToggleableTooltipIconButtonWidget;
import fabric.net.mca.client.gui.widget.TooltipButtonWidget;
import fabric.net.mca.client.gui.widget.WidgetUtils;
import fabric.net.mca.client.model.CommonVillagerModel;
import fabric.net.mca.client.resources.ClientUtils;
import fabric.net.mca.client.resources.ProfessionIcons;
import fabric.net.mca.client.resources.SkinLocations;
import fabric.net.mca.client.resources.SkinMeta;
import fabric.net.mca.client.resources.SkinPorter;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.Genetics;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.network.c2s.AddCustomClothingMessage;
import fabric.net.mca.network.c2s.RemoveCustomClothingMessage;
import fabric.net.mca.resources.data.skin.Clothing;
import fabric.net.mca.resources.data.skin.Hair;
import fabric.net.mca.resources.data.skin.SkinListEntry;
import fabric.net.mca.util.compat.ButtonWidget;
import fabric.net.mca.util.localization.FlowingText;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.class_1011;
import net.minecraft.class_124;
import net.minecraft.class_1299;
import net.minecraft.class_156;
import net.minecraft.class_1802;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_3852;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_5250;
import net.minecraft.class_5676;
import net.minecraft.class_757;
import net.minecraft.class_7923;

public class SkinLibraryScreen extends class_437 implements SkinListUpdateListener {
   private static final class_2960 TEMPLATE_IDENTIFIER = MCA.locate("textures/skin_template.png");
   private static final class_2960 EMPTY_IDENTIFIER = MCA.locate("skins/empty.png");
   private static final class_2960 CANVAS_IDENTIFIER = MCA.locate("temp");
   private static final float CANVAS_SCALE = 2.35F;
   private String filteredString = "";
   private SkinLibraryScreen.SortingMode sortingMode = SkinLibraryScreen.SortingMode.LIKES;
   private boolean filterInvalidSkins = true;
   private boolean moderatorMode = false;
   private boolean filterHair = false;
   private boolean filterClothing = false;
   private final List<LiteContent> serverContent = new ArrayList<>();
   private SkinLibraryScreen.SubscriptionFilter subscriptionFilter = SkinLibraryScreen.SubscriptionFilter.LIBRARY;
   private User currentUser;
   private int selectionPage;
   private LiteContent focusedContent;
   private LiteContent hoveredContent;
   private LiteContent deleteConfirmationContent;
   private LiteContent reportConfirmationContent;
   private SkinLibraryScreen.Page page;
   private String lastFilteredString = "";
   private int lastLoadedPage = -1;
   private ButtonWidget pageWidget;
   private Workspace workspace;
   private final ColorSelector color = new ColorSelector();
   private int activeMouseButton;
   private int lastPixelMouseX;
   private int lastPixelMouseY;
   private float x0;
   private float x1;
   private float y0;
   private float y1;
   private boolean isPanning;
   private boolean hasPanned;
   private double lastMouseX;
   private double lastMouseY;
   private int timeSinceLastRebuild;
   private class_2561 error;
   private final VillagerEditorScreen previousScreen;
   protected final VillagerEntityMCA villagerVisualization = Objects.requireNonNull(
      (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(class_310.method_1551().field_1687)
   );
   static final int CLOTHES_H = 7;
   static final int CLOTHES_V = 2;
   static final int CLOTHES_PER_PAGE = 15;
   private boolean authenticated = false;
   private boolean awaitingAuthentication = false;
   private boolean isBrowserOpen = false;
   private boolean uploading = false;
   private Thread thread;
   private class_342 textFieldWidget;
   private boolean skipHairWarning;
   private final List<LiteContent> contents = new LinkedList<>();
   private List<LiteContent> libraryContents = new LinkedList<>();

   public SkinLibraryScreen() {
      this(null, null);
   }

   public SkinLibraryScreen(VillagerEditorScreen screen, VillagerEntityMCA villagerVisualization) {
      super(class_2561.method_43471("gui.skin_library.title"));
      this.previousScreen = screen;
      if (this.previousScreen instanceof NeedleScreen) {
         this.filterHair = true;
      }

      if (this.previousScreen instanceof CombScreen) {
         this.filterClothing = true;
      }

      if (villagerVisualization != null) {
         class_2487 nbt = new class_2487();
         villagerVisualization.method_5652(nbt);
         this.villagerVisualization.method_5749(nbt);
      } else {
         assert class_310.method_1551().field_1724 != null;
         if (CommonVillagerModel.getVillager(class_310.method_1551().field_1687, class_310.method_1551().field_1724.method_5667()) instanceof VillagerEntityMCA villager
            )
          {
            class_2487 nbt = new class_2487();
            villager.method_5652(nbt);
            this.villagerVisualization.method_5749(nbt);
         }
      }
   }

   public void method_25420(class_332 context) {
      if (this.previousScreen instanceof DestinyScreen) {
         assert class_310.method_1551().field_1687 != null;
         this.method_25434(context);
      }
   }

   public void method_25419() {
      if (this.previousScreen == null) {
         super.method_25419();
      } else {
         class_310.method_1551().method_1507(this.previousScreen);
      }
   }

   protected void method_25426() {
      super.method_25426();
      this.refreshServerContent();
      this.thread = Thread.currentThread();
      if (this.page == null) {
         if (Auth.loadToken() == null) {
            this.setPage(SkinLibraryScreen.Page.LOADING);
         } else {
            this.setPage(SkinLibraryScreen.Page.LOGIN);
         }

         this.reloadDatabase();
      } else {
         this.refreshPage();
      }
   }

   private void reloadDatabase() {
      this.reloadDatabase(() -> {
         if (this.page == SkinLibraryScreen.Page.LOADING) {
            this.setPage(SkinLibraryScreen.Page.LIBRARY);
         }
      });
   }

   private void reloadDatabase(Runnable callback) {
      CompletableFuture.runAsync(() -> {
         if (Auth.hasToken() && this.authenticated) {
            if (Api.request(Api.HttpMethod.GET, UserResponse.class, "user/mca/me") instanceof UserResponse userResponse) {
               this.currentUser = userResponse.user();
               this.refreshContentList();
            } else {
               this.setError(class_2561.method_43471("gui.skin_library.list_fetch_failed"));
            }
         }
      }).thenRunAsync(callback);
   }

   private void loadPage() {
      this.loadPage(false);
   }

   private void loadPage(boolean force) {
      if (this.lastLoadedPage != this.selectionPage || !this.lastFilteredString.equals(this.filteredString) || force) {
         this.lastFilteredString = this.filteredString;
         this.lastLoadedPage = this.selectionPage;
         CompletableFuture.runAsync(
            () -> {
               if (Api.request(
                  Api.HttpMethod.GET,
                  ContentListResponse.class,
                  "v2/content/mca",
                  Map.of(
                     "whitelist",
                     this.filteredString,
                     "blacklist",
                     (this.filterInvalidSkins ? "invalid" : "") + (this.filterHair ? ",hair" : "") + (this.filterClothing ? ",clothing" : ""),
                     "order",
                     this.sortingMode.order,
                     "descending",
                     "true",
                     "offset",
                     String.valueOf(this.selectionPage * 15),
                     "limit",
                     String.valueOf(15),
                     "moderator",
                     String.valueOf(this.moderatorMode)
                  )
               ) instanceof ContentListResponse contentListResponse) {
                  this.libraryContents = new ArrayList<>(Arrays.asList(contentListResponse.contents()));
                  this.refreshContentList();
               } else {
                  this.setError(class_2561.method_43471("gui.skin_library.list_fetch_failed"));
               }
            }
         );
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrices = context.method_51448();
      this.method_25420(context);
      this.hoveredContent = null;
      this.villagerVisualization.method_5614(0);
      this.villagerVisualization.method_18382();
      switch (this.page) {
         case LIBRARY:
            int i = 0;

            for (int y = 0; y < 2; y++) {
               for (int x = 0; x < 7 + y && this.contents.size() > i; x++) {
                  LiteContent c = this.contents.get(i);
                  this.setDummyTexture(c);
                  int cxx = this.field_22789 / 2 + (int)((x - 3.5 + 0.5 - 0.5 * (y % 2)) * 55.0);
                  int cyx = this.field_22790 / 2 + 15 + (int)((y - 1.0 + 0.5) * 80.0);
                  if (Math.abs(cxx - mouseX) <= 15 && Math.abs(cyx - mouseY - 25) <= 24) {
                     this.hoveredContent = c;
                     context.method_51434(this.field_22793, this.getMetaDataText(c), mouseX, mouseY);
                  }

                  this.villagerVisualization.getGenetics().setGender(SkinCache.getMeta(c).map(SkinMeta::getGender).orElse(Gender.MALE).binary());
                  class_490.method_2486(
                     context, cxx, cyx, this.hoveredContent == c ? 30 : 28, -(mouseX - cxx) / 2.0F, -(mouseY - cyx) / 2.0F, this.villagerVisualization
                  );
                  i++;
               }
            }

            if (!this.authenticated
               && (
                  this.subscriptionFilter == SkinLibraryScreen.SubscriptionFilter.LIKED
                     || this.subscriptionFilter == SkinLibraryScreen.SubscriptionFilter.SUBMISSIONS
               )) {
               this.drawTextBox(context, class_2561.method_43471("gui.skin_library.like_locked"));
            }
            break;
         case EDITOR_LOCKED:
            this.drawTextBox(context, class_2561.method_43471("gui.skin_library.locked"));
            break;
         case EDITOR_PREPARE:
            this.drawTextBox(context, class_2561.method_43471("gui.skin_library.drop"));
            break;
         case EDITOR_TYPE:
            this.drawTextBox(context, class_2561.method_43471("gui.skin_library.prepare"));
            break;
         case EDITOR: {
            if (this.workspace.isDirty()) {
               this.workspace.backendTexture.method_4524();
               class_310.method_1551().method_1531().method_4616(CANVAS_IDENTIFIER, this.workspace.backendTexture);
               this.workspace.setDirty(false);
            }

            int tw = 64;
            int th = 64;
            RenderSystem.setShader(class_757::method_34542);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            matrices.method_22903();
            matrices.method_46416(this.field_22789 / 2.0F - tw * 2.35F / 2.0F, this.field_22790 / 2.0F - th * 2.35F / 2.0F, 0.0F);
            matrices.method_22905(2.35F, 2.35F, 1.0F);
            float vx0 = class_3532.method_15363(0.0F, this.x0, this.x1);
            float vx1 = class_3532.method_15363(1.0F, this.x0, this.x1);
            float vy0 = class_3532.method_15363(0.0F, this.y0, this.y1);
            float vy1 = class_3532.method_15363(1.0F, this.y0, this.y1);
            float uvx0 = (vx0 - this.x0) / (this.x1 - this.x0);
            float uvx1 = (vx1 - this.x0) / (this.x1 - this.x0);
            float uvy0 = (vy0 - this.y0) / (this.y1 - this.y0);
            float uvy1 = (vy1 - this.y0) / (this.y1 - this.y0);
            RenderSystem.setShaderTexture(0, TEMPLATE_IDENTIFIER);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.25F);
            WidgetUtils.drawTexturedQuad(
               matrices.method_23760().method_23761(), vx0 * 64.0F, vx1 * 64.0F, vy0 * 64.0F, vy1 * 64.0F, 0.0F, uvx0, uvx1, uvy0, uvy1
            );
            RenderSystem.setShaderTexture(0, CANVAS_IDENTIFIER);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            WidgetUtils.drawTexturedQuad(
               matrices.method_23760().method_23761(), vx0 * 64.0F, vx1 * 64.0F, vy0 * 64.0F, vy1 * 64.0F, 0.0F, uvx0, uvx1, uvy0, uvy1
            );
            WidgetUtils.drawRectangle(context, -1, -1, tw + 1, th + 1, -1426063361);
            matrices.method_22909();
            if (this.workspace.skinType == SkinLibraryScreen.SkinType.CLOTHING) {
               this.villagerVisualization.setHair(EMPTY_IDENTIFIER);
               this.villagerVisualization.setClothes(CANVAS_IDENTIFIER);
            } else {
               this.villagerVisualization.setHair(CANVAS_IDENTIFIER);
               this.villagerVisualization.setClothes(EMPTY_IDENTIFIER);
            }

            int cx = this.field_22789 / 2 + 150;
            int cy = this.field_22790 / 2 - 10;
            this.villagerVisualization.getGenetics().setGender(this.workspace.gender.binary());
            WidgetUtils.drawBackgroundEntity(cx, cy, 50, -(mouseX - cx) / 2.0F, -(mouseY - cy + 32) / 2.0F, this.villagerVisualization);
            if (this.workspace.skinType == SkinLibraryScreen.SkinType.HAIR) {
               context.method_27534(
                  this.field_22793, class_2561.method_43471("gui.skin_library.hair_color"), this.field_22789 / 2 - 150, this.field_22790 / 2 - 40, -1426063361
               );
            }

            int x = (int)this.getPixelX();
            int y = (int)this.getPixelY();
            if (x >= 0 && x < 64 && y >= 0 && y < 64) {
               SkinLocations.Part part = SkinLocations.LOOKUP[x][y];
               if (part != null) {
                  class_2561 text = part.getTranslation();
                  int textWidth = this.field_22793.method_27525(text);
                  context.method_51438(this.field_22793, text, this.field_22789 / 2 - textWidth / 2 - 12, this.field_22790 / 2 - 68);
               }
            }
         }
         case HELP:
         case LOGOUT:
         default:
            break;
         case LOGIN:
            if (!this.awaitingAuthentication) {
               this.awaitingAuthentication = true;
               CompletableFuture.runAsync(() -> {
                  try {
                     if ((Auth.hasToken() ? Api.request(Api.HttpMethod.GET, IsAuthResponse.class, "auth") : null) instanceof IsAuthResponse authResponse) {
                        if (authResponse.authenticated()) {
                           this.authenticated = true;
                           this.clearError();
                           this.reloadDatabase();
                           Auth.saveToken();
                           this.setPage(SkinLibraryScreen.Page.LIBRARY);
                        } else {
                           Auth.clearToken();
                           if (!this.isBrowserOpen) {
                              this.setPage(SkinLibraryScreen.Page.LIBRARY);
                              this.setError(class_2561.method_43471("gui.skin_library.is_auth_failed"));
                           }
                        }
                     } else {
                        this.setError(class_2561.method_43471("gui.skin_library.is_auth_failed"));
                     }

                     Thread.sleep(2000L);
                  } catch (Exception e) {
                     MCA.LOGGER.error(e);
                  }

                  this.awaitingAuthentication = false;
               });
            }

            if (this.isBrowserOpen) {
               this.drawTextBox(context, class_2561.method_43471("gui.skin_library.authenticating_browser"));
            } else if (this.error != null) {
               this.drawTextBox(context, class_2561.method_43471("gui.skin_library.authenticating"));
            } else {
               this.drawTextBox(
                  context,
                  class_2561.method_43471("gui.skin_library.authenticating")
                     .method_10852(class_2561.method_43470(" " + ".".repeat((int)(System.currentTimeMillis() / 500L % 4L))))
               );
            }
            break;
         case DETAIL: {
            this.setDummyTexture(this.focusedContent);
            int cx = this.field_22789 / 2;
            int cy = this.field_22790 / 2 + 50;
            this.villagerVisualization.getGenetics().setGender(SkinCache.getMeta(this.focusedContent).map(SkinMeta::getGender).orElse(Gender.MALE).binary());
            class_490.method_2486(context, cx, cy, 60, -(mouseX - cx) / 2.0F, -(mouseY - cy) / 2.0F, this.villagerVisualization);
            context.method_51434(this.field_22793, this.getMetaDataText(this.focusedContent), this.field_22789 / 2 + 200, this.field_22790 / 2 - 50);
            break;
         }
         case DELETE:
            this.drawTextBox(context, class_2561.method_43471("gui.skin_library.delete_confirm"));
            break;
         case LOADING:
            context.method_27535(this.field_22793, class_2561.method_43471("gui.loading"), this.field_22789 / 2, this.field_22790 / 2, -1);
            break;
         case REPORT:
            this.drawTextBox(context, class_2561.method_43471("gui.skin_library.report_confirm"));
      }

      if (this.error != null) {
         context.method_27534(this.field_22793, this.error, this.field_22789 / 2, this.field_22790 / 2, -65536);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void setDummyTexture(LiteContent content) {
      if (content.hasTag("clothing")) {
         this.villagerVisualization.setHair(EMPTY_IDENTIFIER);
         this.villagerVisualization.setClothes(SkinCache.getTextureIdentifier(content));
      } else {
         this.villagerVisualization.setHair(SkinCache.getTextureIdentifier(content));
         this.villagerVisualization.setClothes(EMPTY_IDENTIFIER);
      }
   }

   private List<class_2561> getMetaDataText(LiteContent content) {
      Optional<SkinMeta> meta = SkinCache.getMeta(content);
      if (meta.isEmpty()) {
         return List.of(class_2561.method_43470(content.title()));
      }

      List<class_2561> wrap = FlowingText.wrap(class_2561.method_43470(String.join(", ", content.tags())).method_27692(class_124.field_1054), 160);
      ArrayList<class_2561> texts = new ArrayList<>(
         List.of(
            class_2561.method_43470(content.title()),
            class_2561.method_43469("gui.skin_library.meta.by", new Object[]{content.username()}).method_27692(class_124.field_1056),
            class_2561.method_43469("gui.skin_library.meta.likes", new Object[]{content.likes()}).method_27692(class_124.field_1080),
            class_2561.method_43469(
               "gui.skin_library.gender",
               new Object[]{
                  meta.get().getGender() == Gender.MALE
                     ? class_2561.method_43471("gui.villager_editor.masculine")
                     : (
                        meta.get().getGender() == Gender.FEMALE
                           ? class_2561.method_43471("gui.villager_editor.feminine")
                           : class_2561.method_43471("gui.villager_editor.neutral")
                     )
               }
            ),
            class_2561.method_43469(
               "gui.skin_library.profession",
               new Object[]{
                  meta.get().getProfession() == null
                     ? class_2561.method_43471("entity.minecraft.villager")
                     : class_2561.method_43471("entity.minecraft.villager." + meta.get().getProfession())
               }
            ),
            class_2561.method_43469(
               "gui.skin_library.temperature", new Object[]{class_2561.method_43471("gui.skin_library.temperature." + (meta.get().getTemperature() + 2))}
            ),
            class_2561.method_43469("gui.skin_library.chance_val", new Object[]{(int)(meta.get().getChance() * 100.0F)}).method_27692(class_124.field_1080)
         )
      );
      texts.addAll(wrap);
      if (content.tags().contains("invalid")) {
         texts.add(class_2561.method_43471("gui.skin_library.probably_not_valids").method_27692(class_124.field_1067).method_27692(class_124.field_1061));
      }

      return texts;
   }

   private double getScreenScaleX() {
      assert this.field_22787 != null;
      return (double)this.field_22787.method_22683().method_4486() / this.field_22787.method_22683().method_4480();
   }

   private double getScreenScaleY() {
      assert this.field_22787 != null;
      return (double)this.field_22787.method_22683().method_4502() / this.field_22787.method_22683().method_4507();
   }

   private double getCanvasX() {
      assert this.field_22787 != null;
      double x = this.field_22787.field_1729.method_1603() * this.getScreenScaleX();
      return (x - this.field_22789 / 2.0 + 75.2F) / 2.35F / 64.0;
   }

   private double getCanvasY() {
      assert this.field_22787 != null;
      double y = this.field_22787.field_1729.method_1604() * this.getScreenScaleY();
      return (y - this.field_22790 / 2.0 + 75.2F) / 2.35F / 64.0;
   }

   private float getPixelX() {
      double cx = this.getCanvasX();
      return (int)((cx - this.x0) / (this.x1 - this.x0) * 64.0);
   }

   private float getPixelY() {
      double cy = this.getCanvasY();
      return (int)((cy - this.y0) / (this.y1 - this.y0) * 64.0);
   }

   public void method_16014(double mouseX, double mouseY) {
      this.mouseDeltaMoved(this.lastMouseX - mouseX, this.lastMouseY - mouseY);
      this.lastMouseX = mouseX;
      this.lastMouseY = mouseY;
      super.method_16014(mouseX, mouseY);
   }

   protected void mouseDeltaMoved(double deltaX, double deltaY) {
      if (this.isPanning) {
         float ox = (float)(deltaX / 64.0 / 2.35F);
         this.x0 -= ox;
         this.x1 -= ox;
         float oy = (float)(deltaY / 64.0 / 2.35F);
         this.y0 -= oy;
         this.y1 -= oy;
         this.hasPanned = true;
      } else {
         this.hasPanned = false;
      }
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 32 || this.textFieldWidget != null && this.textFieldWidget.method_25370()) {
         if (this.page == SkinLibraryScreen.Page.EDITOR && (this.textFieldWidget == null || !this.textFieldWidget.method_25370())) {
            if (keyCode == 82) {
               this.x0 = 0.0F;
               this.x1 = 1.0F;
               this.y0 = 0.0F;
               this.y1 = 1.0F;
               return true;
            }

            if (keyCode == 70) {
               this.workspace.fillDelete((int)this.getPixelX(), (int)this.getPixelY());
               return true;
            }

            if (keyCode == 89 || keyCode == 90) {
               this.workspace.undo();
               return true;
            }

            if (keyCode == 80) {
               this.pickColor();
               return true;
            }
         }

         return super.method_25404(keyCode, scanCode, modifiers);
      } else {
         this.isPanning = true;
         return true;
      }
   }

   public boolean method_16803(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 32) {
         this.isPanning = false;
      }

      return super.method_16803(keyCode, scanCode, modifiers);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.isPanning && this.activeMouseButton >= 0 && this.page == SkinLibraryScreen.Page.EDITOR) {
         int x = (int)this.getPixelX();
         int y = (int)this.getPixelY();
         ClientUtils.bethlehemLine(this.lastPixelMouseX, this.lastPixelMouseY, x, y, this::paint);
         this.lastPixelMouseX = x;
         this.lastPixelMouseY = y;
      }

      return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean method_25401(double mouseX, double mouseY, double amount) {
      if (this.page == SkinLibraryScreen.Page.EDITOR) {
         float zoom = (float)(amount * 0.2F) * (this.x1 - this.x0);
         float ox = this.getPixelX() / 64.0F;
         this.x0 -= zoom * ox;
         this.x1 += zoom * (1.0F - ox);
         float oy = this.getPixelY() / 64.0F;
         this.y0 -= zoom * oy;
         this.y1 += zoom * (1.0F - oy);
      }

      return super.method_25401(mouseX, mouseY, amount);
   }

   public void method_25393() {
      super.method_25393();
      this.timeSinceLastRebuild++;
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.timeSinceLastRebuild < 2) {
         return false;
      }

      if (this.page == SkinLibraryScreen.Page.EDITOR) {
         if (button == 0 || button == 1) {
            this.activeMouseButton = button;
            int x = (int)this.getPixelX();
            int y = (int)this.getPixelY();
            if (this.workspace.validPixel(x, y)) {
               this.workspace.saveSnapshot(true);
            }

            if (!this.isPanning) {
               this.paint(x, y);
            }

            this.lastPixelMouseX = x;
            this.lastPixelMouseY = y;
         } else if (button == 2) {
            this.isPanning = true;
         }
      } else if (this.hoveredContent != null) {
         if (this.previousScreen == null) {
            this.focusedContent = this.hoveredContent;
            this.setPage(SkinLibraryScreen.Page.DETAIL);
         } else if (this.hoveredContent.hasTag("clothing")) {
            this.previousScreen.getVillager().setClothes("immersive_library:" + this.hoveredContent.contentid());
            this.returnToPreviousScreen();
         } else if (this.hoveredContent.hasTag("hair")) {
            this.previousScreen.getVillager().setHair("immersive_library:" + this.hoveredContent.contentid());
            this.returnToPreviousScreen();
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   private void returnToPreviousScreen() {
      this.previousScreen.syncVillagerData();
      if (this.previousScreen instanceof DestinyScreen) {
         this.method_25419();
      } else {
         this.previousScreen.method_25419();
      }
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      this.activeMouseButton = -1;
      if (button == 2) {
         this.isPanning = false;
         if (!this.hasPanned && this.page == SkinLibraryScreen.Page.EDITOR) {
            this.pickColor();
         }
      }

      return super.method_25406(mouseX, mouseY, button);
   }

   private void drawTextBox(class_332 context, class_2561 text) {
      List<class_2561> wrap = FlowingText.wrap(text, 220);
      int y = this.field_22790 / 2 - 20 - wrap.size() * 12;
      context.method_25294(this.field_22789 / 2 - 115, y - 5, this.field_22789 / 2 + 115, y + 12 * wrap.size(), 1342177280);

      for (class_2561 t : wrap) {
         context.method_27534(this.field_22793, t, this.field_22789 / 2, y, -1);
         y += 12;
      }
   }

   private void paint(int x, int y) {
      if (this.page == SkinLibraryScreen.Page.EDITOR && this.workspace.validPixel(x, y)) {
         if (this.activeMouseButton == 0) {
            this.workspace.currentImage.method_4305(x, y, this.color.getColor());
            this.workspace.setDirty(true);
         } else if (this.activeMouseButton == 1) {
            this.workspace.currentImage.method_4305(x, y, 0);
            this.workspace.setDirty(true);
         }
      }
   }

   private void pickColor() {
      int x = (int)this.getPixelX();
      int y = (int)this.getPixelY();
      if (this.workspace.validPixel(x, y)) {
         this.color
            .setRGB(
               (this.workspace.currentImage.method_35623(x, y) & 0xFF) / 255.0,
               (this.workspace.currentImage.method_35625(x, y) & 0xFF) / 255.0,
               (this.workspace.currentImage.method_35626(x, y) & 0xFF) / 255.0
            );
         if (this.workspace.skinType == SkinLibraryScreen.SkinType.HAIR) {
            this.color.setHSV(0.0, 0.0, this.color.brightness);
         }
      }
   }

   private void rebuild() {
      this.method_37067();
      this.timeSinceLastRebuild = 0;
      if (this.page == SkinLibraryScreen.Page.LIBRARY
         || this.page == SkinLibraryScreen.Page.EDITOR_LOCKED
         || this.page == SkinLibraryScreen.Page.EDITOR_PREPARE
         || this.page == SkinLibraryScreen.Page.EDITOR_TYPE) {
         List<SkinLibraryScreen.Page> b = new LinkedList<>();
         b.add(SkinLibraryScreen.Page.LIBRARY);
         b.add(SkinLibraryScreen.Page.EDITOR_PREPARE);
         b.add(SkinLibraryScreen.Page.HELP);
         if (this.authenticated) {
            b.add(SkinLibraryScreen.Page.LOGOUT);
         } else {
            b.add(SkinLibraryScreen.Page.LOGIN);
         }

         int x = this.page == SkinLibraryScreen.Page.LIBRARY ? this.field_22789 / 2 - 20 : this.field_22789 / 2 - 110;
         int w = 220 / b.size();

         for (SkinLibraryScreen.Page page : b) {
            ((ButtonWidget)this.method_37063(
                  new ButtonWidget(
                     x,
                     this.field_22790 / 2 - 110,
                     w,
                     20,
                     class_2561.method_43471("gui.skin_library.page." + page.name().toLowerCase(Locale.ROOT)),
                     sender -> this.setPage(page)
                  )
               ))
               .field_22763 = page != this.page;
            x += w;
         }
      }

      switch (this.page) {
         case LIBRARY:
            this.method_37063(new ButtonWidget(this.field_22789 / 2 - 30 - 30, this.field_22790 / 2 + 80, 30, 20, class_2561.method_43470("<<"), sender -> {
               this.setSelectionPage(this.selectionPage - 1);
               this.refreshContentList();
            }));
            this.pageWidget = (ButtonWidget)this.method_37063(
               new ButtonWidget(this.field_22789 / 2 - 30, this.field_22790 / 2 + 80, 60, 20, class_2561.method_43470(""), sender -> {})
            );
            this.method_37063(new ButtonWidget(this.field_22789 / 2 + 30, this.field_22790 / 2 + 80, 30, 20, class_2561.method_43470(">>"), sender -> {
               this.setSelectionPage(this.selectionPage + 1);
               this.refreshContentList();
            }));
            this.setSelectionPage(this.selectionPage);
            int iconX = this.field_22789 / 2 + 80;
            this.method_37063(
               new ToggleableTooltipIconButtonWidget(
                  iconX,
                  this.field_22790 / 2 + 82,
                  96,
                  48,
                  this.sortingMode == SkinLibraryScreen.SortingMode.LIKES,
                  class_2561.method_43471("gui.skin_library.sort_likes"),
                  v -> {
                     this.sortingMode = SkinLibraryScreen.SortingMode.LIKES;
                     this.loadPage(true);
                  }
               )
            );
            this.method_37063(
               new ToggleableTooltipIconButtonWidget(
                  iconX + 22,
                  this.field_22790 / 2 + 82,
                  112,
                  48,
                  this.sortingMode == SkinLibraryScreen.SortingMode.NEWEST,
                  class_2561.method_43471("gui.skin_library.sort_newest"),
                  v -> {
                     this.sortingMode = SkinLibraryScreen.SortingMode.NEWEST;
                     this.loadPage(true);
                  }
               )
            );
            if (this.subscriptionFilter == SkinLibraryScreen.SubscriptionFilter.LIBRARY) {
               this.method_37063(
                  new ToggleableTooltipIconButtonWidget(
                     iconX + 44,
                     this.field_22790 / 2 + 82,
                     144,
                     48,
                     this.filterInvalidSkins,
                     class_2561.method_43471("gui.skin_library.filter_invalid"),
                     v -> {
                        this.filterInvalidSkins = !this.filterInvalidSkins;
                        this.loadPage(true);
                     }
                  )
               );
               this.method_37063(
                  new ToggleableTooltipIconButtonWidget(
                     iconX + 66, this.field_22790 / 2 + 82, 192, 48, this.filterClothing, class_2561.method_43471("gui.skin_library.filter_clothing"), v -> {
                        this.filterClothing = !this.filterClothing;
                        this.loadPage(true);
                     }
                  )
               );
               this.method_37063(
                  new ToggleableTooltipIconButtonWidget(
                     iconX + 88, this.field_22790 / 2 + 82, 208, 48, this.filterHair, class_2561.method_43471("gui.skin_library.filter_hair"), v -> {
                        this.filterHair = !this.filterHair;
                        this.loadPage(true);
                     }
                  )
               );
               if (this.isModerator()) {
                  this.method_37063(
                     new ToggleableTooltipIconButtonWidget(
                        iconX + 110,
                        this.field_22790 / 2 + 82,
                        176,
                        48,
                        this.moderatorMode,
                        class_2561.method_43471("gui.skin_library.filter_moderator"),
                        v -> {
                           this.moderatorMode = !this.moderatorMode;
                           this.loadPage(true);
                        }
                     )
                  );
               }
            }

            if (this.subscriptionFilter == SkinLibraryScreen.SubscriptionFilter.LIBRARY) {
               class_342 textFieldWidgetx = (class_342)this.method_37063(
                  new class_342(
                     this.field_22793,
                     this.field_22789 / 2 - 200 + 65,
                     this.field_22790 / 2 - 110 + 2,
                     110,
                     16,
                     class_2561.method_43471("gui.skin_library.search")
                  )
               );
               textFieldWidgetx.method_1880(64);
               textFieldWidgetx.method_1852(this.filteredString);
               if (this.filteredString.isEmpty()) {
                  textFieldWidgetx.method_1887("Search");
               }

               textFieldWidgetx.method_1863(s -> {
                  this.filteredString = s;
                  this.refreshContentList();
                  textFieldWidget.method_1887(null);
               });
            }

            this.method_37063(
               class_5676.method_32606(SkinLibraryScreen.SubscriptionFilter::getText)
                  .method_32624(SkinLibraryScreen.SubscriptionFilter.values())
                  .method_32619(this.subscriptionFilter)
                  .method_32616()
                  .method_32617(this.field_22789 / 2 - 200, this.field_22790 / 2 - 110, 60, 20, class_2561.method_43470(""), (button, filter) -> {
                     this.subscriptionFilter = filter;
                     this.refreshContentList();
                  })
            );
            int i = 0;

            for (int y = 0; y < 2; y++) {
               for (int x = 0; x < 7 + y && this.contents.size() > i; x++) {
                  LiteContent c = this.contents.get(i);
                  int cx = this.field_22789 / 2 + (int)((x - 3.5 + 0.5 - 0.5 * (y % 2)) * 55.0);
                  int cy = this.field_22790 / 2 + 15 + (int)((y - 1.0 + 0.5) * 80.0);
                  this.drawControls(c, false, cx, cy);
                  if (this.isModerator()) {
                     this.method_37063(
                        new ToggleableTooltipIconButtonWidget(
                           cx + 16,
                           cy - 48,
                           160,
                           48,
                           false,
                           class_2561.method_43470("Toggle invalid"),
                           v -> this.setTag(c.contentid(), "invalid", !c.hasTag("invalid"))
                        )
                     );
                  }

                  if (c.tags().contains("invalid")) {
                     this.method_37063(
                        new ToggleableTooltipIconButtonWidget(
                           cx + 12, cy - 16, 144, 48, true, class_2561.method_43471("gui.skin_library.probably_not_valids"), v -> {}
                        )
                     );
                  }

                  i++;
               }
            }
         case EDITOR_LOCKED:
         case HELP:
         case LOGOUT:
         case LOADING:
         default:
            break;
         case EDITOR_PREPARE:
            class_342 textFieldWidget = (class_342)this.method_37063(
               new class_342(this.field_22793, this.field_22789 / 2 - 90, this.field_22790 / 2 - 18, 180, 16, class_2561.method_43470("URL"))
            );
            textFieldWidget.method_1880(1024);
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 50,
                  this.field_22790 / 2 + 5,
                  100,
                  20,
                  class_2561.method_43471("gui.skin_library.load_image"),
                  sender -> this.loadImage(textFieldWidget.method_1882())
               )
            );
            break;
         case EDITOR_TYPE:
            this.method_37063(
               new ButtonWidget(this.field_22789 / 2 - 100, this.field_22790 / 2, 95, 20, class_2561.method_43471("gui.skin_library.prepare.hair"), v -> {
                  this.workspace.skinType = SkinLibraryScreen.SkinType.HAIR;
                  this.color.setHSV(0.0, 0.0, 0.5);
                  this.setPage(SkinLibraryScreen.Page.EDITOR);
               })
            );
            this.method_37063(
               new ButtonWidget(this.field_22789 / 2 + 5, this.field_22790 / 2, 95, 20, class_2561.method_43471("gui.skin_library.prepare.clothing"), v -> {
                  this.workspace.skinType = SkinLibraryScreen.SkinType.CLOTHING;
                  this.setPage(SkinLibraryScreen.Page.EDITOR);
               })
            );
            this.method_37063(
               new TooltipButtonWidget(
                  this.field_22789 / 2 - 10,
                  this.field_22790 / 2 + 30,
                  20,
                  20,
                  class_2561.method_43470("?"),
                  class_2561.method_43471("gui.skin_library.help"),
                  v -> this.openHelp()
               )
            );
            break;
         case EDITOR:
            this.textFieldWidget = (class_342)this.method_37063(
               new class_342(this.field_22793, this.field_22789 / 2 - 60, this.field_22790 / 2 - 105, 120, 20, class_2561.method_43471("gui.skin_library.name"))
            );
            this.textFieldWidget.method_1880(1024);
            this.textFieldWidget.method_1852(this.workspace.title);
            if (this.workspace.title.isEmpty()) {
               this.textFieldWidget.method_1887(this.workspace.title);
            }

            this.textFieldWidget.method_25365(false);
            this.textFieldWidget.method_1863(v -> {
               this.workspace.title = v;
               this.textFieldWidget.method_1887(null);
            });
            this.method_37063(
               new TooltipButtonWidget(
                  this.field_22789 / 2 + 65,
                  this.field_22790 / 2 - 105,
                  20,
                  20,
                  class_2561.method_43470("?"),
                  class_2561.method_43471("gui.skin_library.tool_help"),
                  v -> this.openHelp()
               )
            );
            this.method_37063(
               class_5676.method_32606(Gender::getText)
                  .method_32624(new Gender[]{Gender.MALE, Gender.NEUTRAL, Gender.FEMALE})
                  .method_32619(this.workspace.gender)
                  .method_32616()
                  .method_32617(
                     this.field_22789 / 2 - 200,
                     this.field_22790 / 2 - 80,
                     105,
                     20,
                     class_2561.method_43470(""),
                     (button, gender) -> this.workspace.gender = gender
                  )
            );
            if (this.workspace.skinType == SkinLibraryScreen.SkinType.CLOTHING) {
               this.method_37063(
                  new IntegerSliderWidget(
                     this.field_22789 / 2 - 200,
                     this.field_22790 / 2 - 60,
                     105,
                     20,
                     this.workspace.temperature,
                     -2,
                     2,
                     v -> this.workspace.temperature = v,
                     v -> class_2561.method_43471("gui.skin_library.temperature." + (v + 2)),
                     () -> class_2561.method_43471("gui.skin_library.temperature.tooltip")
                  )
               );
            }

            if (this.workspace.skinType == SkinLibraryScreen.SkinType.CLOTHING) {
               int ox = 0;
               int oy = 0;
               List<ItemButtonWidget> widgets = new LinkedList<>();

               for (class_3852 profession : class_7923.field_41195) {
                  class_5250 text = class_2561.method_43471("entity.minecraft.villager." + profession.comp_818());
                  ItemButtonWidget widget = (ItemButtonWidget)this.method_37063(
                     new ItemButtonWidget(
                        this.field_22789 / 2 - 200 + ox * 21,
                        this.field_22790 / 2 - 30 + oy * 21,
                        20,
                        text,
                        ProfessionIcons.ICONS.getOrDefault(profession.comp_818(), class_1802.field_17535.method_7854()),
                        v -> {
                           this.workspace.profession = profession == class_3852.field_17051 ? null : profession.comp_818();
                           widgets.forEach(b -> b.field_22763 = true);
                           v.field_22763 = false;
                        }
                     )
                  );
                  widget.field_22763 = !Objects.equals(this.workspace.profession, profession == class_3852.field_17051 ? null : profession.comp_818());
                  widgets.add(widget);
                  if (++ox >= 5) {
                     ox = 0;
                     oy++;
                  }
               }
            }

            int y = this.field_22790 / 2 - 5;
            if (this.workspace.skinType == SkinLibraryScreen.SkinType.CLOTHING) {
               this.color.hueWidget = (HorizontalColorPickerWidget)this.method_37063(
                  new HorizontalColorPickerWidget(
                     this.field_22789 / 2 + 100,
                     y,
                     100,
                     15,
                     this.color.hue / 360.0,
                     MCA.locate("textures/colormap/hue.png"),
                     (vx, vy) -> this.color.setHSV(vx * 360.0, this.color.saturation, this.color.brightness)
                  )
               );
               this.color.saturationWidget = (HorizontalColorPickerWidget)this.method_37063(
                  new HorizontalGradientWidget(this.field_22789 / 2 + 100, y + 20, 100, 15, this.color.saturation, () -> {
                     double[] doubles = ClientUtils.HSV2RGB(this.color.hue, 0.0, 1.0);
                     return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                  }, () -> {
                     double[] doubles = ClientUtils.HSV2RGB(this.color.hue, 1.0, 1.0);
                     return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                  }, (vx, vy) -> this.color.setHSV(this.color.hue, vx, this.color.brightness))
               );
            }

            this.color.brightnessWidget = (HorizontalColorPickerWidget)this.method_37063(
               new HorizontalGradientWidget(this.field_22789 / 2 + 100, y + 40, 100, 15, this.color.brightness, () -> {
                  double[] doubles = ClientUtils.HSV2RGB(this.color.hue, this.color.saturation, 0.0);
                  return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
               }, () -> {
                  double[] doubles = ClientUtils.HSV2RGB(this.color.hue, this.color.saturation, 1.0);
                  return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
               }, (vx, vy) -> this.color.setHSV(this.color.hue, this.color.saturation, vx))
            );
            this.method_37063(
               new IntegerSliderWidget(
                  this.field_22789 / 2 + 100,
                  y + 60,
                  100,
                  20,
                  this.workspace.fillToolThreshold,
                  0,
                  128,
                  v -> this.workspace.fillToolThreshold = v,
                  v -> class_2561.method_43471("gui.skin_library.fillToolThreshold"),
                  () -> class_2561.method_43471("gui.skin_library.fillToolThreshold.tooltip")
               )
            );
            this.method_37063(
               new ButtonWidget(this.field_22789 / 2 + 100, y + 85, 100, 20, class_2561.method_43471("gui.skin_library.undo"), v -> this.workspace.undo())
            );
            if (this.workspace.skinType == SkinLibraryScreen.SkinType.HAIR) {
               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2 + 100,
                     y - 20,
                     100,
                     20,
                     class_2561.method_43471("gui.skin_library.remove_saturation"),
                     v -> this.workspace.removeSaturation()
                  )
               );
               this.method_37063(
                  new TooltipButtonWidget(
                     this.field_22789 / 2 + 100,
                     y,
                     50,
                     20,
                     class_2561.method_43470("C -"),
                     class_2561.method_43471("gui.skin_library.less_contrast"),
                     v -> this.workspace.addContrast(-0.15F)
                  )
               );
               this.method_37063(
                  new TooltipButtonWidget(
                     this.field_22789 / 2 + 150,
                     y,
                     50,
                     20,
                     class_2561.method_43470("C +"),
                     class_2561.method_43471("gui.skin_library.more_contrast"),
                     v -> this.workspace.addContrast(0.15F)
                  )
               );
               this.method_37063(
                  new TooltipButtonWidget(
                     this.field_22789 / 2 + 100,
                     y + 20,
                     50,
                     20,
                     class_2561.method_43470("B -"),
                     class_2561.method_43471("gui.skin_library.less_brightness"),
                     v -> this.workspace.addBrightness(-8)
                  )
               );
               this.method_37063(
                  new TooltipButtonWidget(
                     this.field_22789 / 2 + 150,
                     y + 20,
                     50,
                     20,
                     class_2561.method_43470("B +"),
                     class_2561.method_43471("gui.skin_library.more_brightness"),
                     v -> this.workspace.addBrightness(8)
                  )
               );
               Genetics genetics = this.villagerVisualization.getGenetics();
               this.method_37063(
                  new ColorPickerWidget(
                     this.field_22789 / 2 - 200,
                     this.field_22790 / 2 - 30,
                     100,
                     100,
                     genetics.getGene(Genetics.PHEOMELANIN),
                     genetics.getGene(Genetics.EUMELANIN),
                     MCA.locate("textures/colormap/villager_hair.png"),
                     (vx, vy) -> {
                        genetics.setGene(Genetics.PHEOMELANIN, vx.floatValue());
                        genetics.setGene(Genetics.EUMELANIN, vy.floatValue());
                     }
                  )
               );
            }

            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 80,
                  this.field_22790 / 2 + 80,
                  75,
                  20,
                  class_2561.method_43471("gui.skin_library.cancel"),
                  v -> this.setPage(SkinLibraryScreen.Page.LIBRARY)
               )
            );
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 + 5, this.field_22790 / 2 + 80, 75, 20, class_2561.method_43471("gui.skin_library.publish"), v -> this.publish()
               )
            );
            break;
         case LOGIN:
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 50,
                  this.field_22790 / 2 + 25,
                  100,
                  20,
                  class_2561.method_43471("gui.skin_library.cancel"),
                  v -> this.setPage(SkinLibraryScreen.Page.LIBRARY)
               )
            );
            break;
         case DETAIL:
            if (this.canModifyFocusedContent()) {
               class_342 tagNameWidget = (class_342)this.method_37063(
                  new class_342(this.field_22793, this.field_22789 / 2 - 200, this.field_22790 / 2 - 100 + 2, 95, 16, class_2561.method_43470(""))
               );
               tagNameWidget.method_1880(20);
               tagNameWidget.method_1887("New Tag Name");
               tagNameWidget.method_1863(v -> tagNameWidget.method_1887(null));
               this.method_37063(new TooltipButtonWidget(this.field_22789 / 2 - 100, this.field_22790 / 2 - 100, 40, 20, "gui.skin_library.add", sender -> {
                  String tag = tagNameWidget.method_1882().trim().toLowerCase(Locale.ROOT);
                  if (!tag.isEmpty()) {
                     this.setTag(this.focusedContent.contentid(), tag, true);
                     tagNameWidget.method_1852("");
                     this.rebuild();
                  }
               }));
            }

            this.drawControls(this.focusedContent, true, this.field_22789 / 2 + 130, this.field_22790 / 2 + 60);
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 40,
                  this.field_22790 / 2 + 60,
                  80,
                  20,
                  class_2561.method_43471("gui.skin_library.close"),
                  v -> this.setPage(SkinLibraryScreen.Page.LIBRARY)
               )
            );
            int ty = this.field_22790 / 2 - 70;

            for (String tag : this.focusedContent.tags()) {
               if (!tag.equals("clothing") && !tag.equals("hair") && !tag.equals("invalid") || this.isModerator()) {
                  int w = this.field_22793.method_1727(tag) + 10;
                  if (this.canModifyFocusedContent()) {
                     this.method_37063(new ButtonWidget(this.field_22789 / 2 - 200, ty, 20, 20, class_2561.method_43470("X"), v -> {
                        this.setTag(this.focusedContent.contentid(), tag, false);
                        this.rebuild();
                     }));
                  }

                  this.method_37063(new ButtonWidget(this.field_22789 / 2 - 200 + 20, ty, w, 20, class_2561.method_43470(tag), v -> {}));
                  ty += 20;
               }
            }
            break;
         case DELETE:
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 65,
                  this.field_22790 / 2,
                  60,
                  20,
                  class_2561.method_43471("gui.skin_library.cancel"),
                  v -> this.setPage(SkinLibraryScreen.Page.DETAIL)
               )
            );
            this.method_37063(
               new ButtonWidget(this.field_22789 / 2 + 5, this.field_22790 / 2, 60, 20, class_2561.method_43471("gui.skin_library.delete"), v -> {
                  this.removeContent(this.deleteConfirmationContent.contentid());
                  this.setPage(SkinLibraryScreen.Page.LIBRARY);
               })
            );
            break;
         case REPORT:
            this.method_37063(
               new TooltipButtonWidget(
                  this.field_22789 / 2 - 105,
                  this.field_22790 / 2,
                  100,
                  20,
                  class_2561.method_43471("gui.skin_library.report_invalid"),
                  class_2561.method_43471("gui.skin_library.report_invalid_tooltip"),
                  v -> {
                     this.reportContent(this.reportConfirmationContent.contentid(), "INVALID");
                     this.setPage(SkinLibraryScreen.Page.DETAIL);
                  }
               )
            );
            this.method_37063(
               new TooltipButtonWidget(
                  this.field_22789 / 2 + 5,
                  this.field_22790 / 2,
                  100,
                  20,
                  class_2561.method_43471("gui.skin_library.report_default"),
                  class_2561.method_43471("gui.skin_library.report_default_tooltip"),
                  v -> {
                     this.reportContent(this.reportConfirmationContent.contentid(), "DEFAULT");
                     this.setPage(SkinLibraryScreen.Page.DETAIL);
                  }
               )
            );
            this.method_37063(
               new ButtonWidget(
                  this.field_22789 / 2 - 50,
                  this.field_22790 / 2 + 22,
                  100,
                  20,
                  class_2561.method_43471("gui.skin_library.cancel"),
                  v -> this.setPage(SkinLibraryScreen.Page.DETAIL)
               )
            );
            if (this.isModerator()) {
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 50, this.field_22790 / 2 + 44, 100, 20, class_2561.method_43470("Counter Report"), v -> {
                     this.reportContent(this.reportConfirmationContent.contentid(), "COUNTER_DEFAULT");
                     this.setPage(SkinLibraryScreen.Page.DETAIL);
                  })
               );
            }
      }
   }

   private void drawControls(LiteContent content, boolean advanced, int cx, int cy) {
      int w = advanced ? 20 : 16;
      List<TooltipButtonWidget> widgets = new LinkedList<>();
      if (this.isOp() || Config.getServerConfig().allowEveryoneToAddContentGlobally) {
         widgets.add(
            new ToggleableTooltipIconButtonWidget(
               0,
               0,
               0,
               48,
               this.getServerContentById(content.contentid()).isPresent(),
               class_2561.method_43471("gui.skin_library.subscribe"),
               v -> {
                  if (((ToggleableTooltipButtonWidget)v).toggle) {
                     NetworkHandler.sendToServer(
                        new RemoveCustomClothingMessage(
                           content.hasTag("clothing") ? RemoveCustomClothingMessage.Type.CLOTHING : RemoveCustomClothingMessage.Type.HAIR,
                           new class_2960("immersive_library", String.valueOf(content.contentid()))
                        )
                     );
                  } else {
                     this.toListEntry(content).ifPresent(e -> NetworkHandler.sendToServer(new AddCustomClothingMessage(e)));
                  }

                  ((ToggleableTooltipButtonWidget)v).toggle = !((ToggleableTooltipButtonWidget)v).toggle;
               }
            )
         );
      }

      if (this.authenticated) {
         widgets.add(new ToggleableTooltipIconButtonWidget(0, 0, 16, 48, this.isLiked(content), class_2561.method_43471("gui.skin_library.like"), v -> {
            ((ToggleableTooltipButtonWidget)v).toggle = !((ToggleableTooltipButtonWidget)v).toggle;
            this.setLike(content.contentid(), ((ToggleableTooltipButtonWidget)v).toggle);
         }));
      }

      if (advanced && this.canModifyContent(content)) {
         widgets.add(
            new ToggleableTooltipIconButtonWidget(
               0,
               0,
               32,
               48,
               false,
               class_2561.method_43471("gui.skin_library.edit"),
               v -> SkinCache.getImage(content).ifPresent(image -> SkinCache.getMeta(content).ifPresent(meta -> {
                  this.workspace = new Workspace(image, meta, content);
                  this.setPage(SkinLibraryScreen.Page.EDITOR);
                  if (this.workspace.skinType == SkinLibraryScreen.SkinType.HAIR) {
                     this.color.setHSV(0.0, 0.0, 0.5);
                  }
               }))
            )
         );
      }

      if (advanced && this.authenticated) {
         widgets.add(new ToggleableTooltipIconButtonWidget(cx - 12 + 25, cy, 160, 48, true, class_2561.method_43471("gui.skin_library.report"), v -> {
            this.reportConfirmationContent = content;
            this.setPage(SkinLibraryScreen.Page.REPORT);
         }));
      }

      if (advanced && this.canModifyContent(content)) {
         widgets.add(new ToggleableTooltipIconButtonWidget(cx - 12 + 25, cy, 48, 48, true, class_2561.method_43471("gui.skin_library.delete"), v -> {
            this.deleteConfirmationContent = content;
            this.setPage(SkinLibraryScreen.Page.DELETE);
         }));
      }

      if (!advanced) {
         widgets.add(new ToggleableTooltipIconButtonWidget(cx - 12 + 25, cy, 64, 64, true, class_2561.method_43471("gui.skin_library.details"), v -> {
            if (this.isPanning && this.isModerator()) {
               this.reportContent(content.contentid(), "DEFAULT");
               this.refreshContentList();
            } else {
               this.focusedContent = content;
               this.setPage(SkinLibraryScreen.Page.DETAIL);
            }
         }));
      }

      if (advanced && this.isModerator()) {
         widgets.add(new ToggleableTooltipIconButtonWidget(cx - 12 + 25, cy, 80, 48, false, class_2561.method_43471("gui.skin_library.ban"), v -> {
            this.setBan(content.userid(), true);
            this.refreshContentList();
         }));
         widgets.add(new ToggleableTooltipIconButtonWidget(cx - 12 + 25, cy, 176, 48, false, class_2561.method_43471("gui.skin_library.unban"), v -> {
            this.setBan(content.userid(), false);
            this.refreshContentList();
         }));
      }

      int wx = cx - widgets.size() * w / 2;

      for (TooltipButtonWidget buttonWidget : widgets) {
         this.method_37063(buttonWidget);
         buttonWidget.method_46421(wx);
         buttonWidget.method_46419(cy);
         wx += w;
      }
   }

   private Optional<SkinListEntry> toListEntry(LiteContent content) {
      return SkinCache.getMeta(content)
         .map(
            meta -> content.hasTag("clothing")
               ? new Clothing("immersive_library:" + content.contentid(), meta.getProfession(), meta.getTemperature(), false, meta.getGender())
               : new Hair("immersive_library:" + content.contentid())
         );
   }

   private boolean canModifyFocusedContent() {
      return this.canModifyContent(this.focusedContent);
   }

   private boolean canModifyContent(LiteContent content) {
      return this.currentUser != null && (this.currentUser.moderator() || this.currentUser.userid() == content.userid());
   }

   private boolean isModerator() {
      return this.currentUser != null && this.currentUser.moderator();
   }

   private boolean isLiked(LiteContent content) {
      return this.currentUser != null && this.currentUser.likes().stream().anyMatch(c -> c.contentid() == content.contentid());
   }

   public void setPage(SkinLibraryScreen.Page page) {
      if (Thread.currentThread() != this.thread) {
         assert this.field_22787 != null;
         this.field_22787.method_40000(() -> this.setPage(page));
      } else {
         this.clearError();
         if ((page == SkinLibraryScreen.Page.EDITOR_TYPE || page == SkinLibraryScreen.Page.EDITOR_PREPARE) && !this.authenticated) {
            this.setPage(SkinLibraryScreen.Page.EDITOR_LOCKED);
         } else if (page == SkinLibraryScreen.Page.HELP) {
            this.openHelp();
         } else {
            if (page == SkinLibraryScreen.Page.LOGIN) {
               if (Auth.loadToken() == null) {
                  this.isBrowserOpen = true;
                  Auth.authenticate(this.getPlayerName());
               } else {
                  this.isBrowserOpen = false;
               }
            }

            if (page == SkinLibraryScreen.Page.LOGOUT) {
               this.authenticated = false;
               this.currentUser = null;
               Auth.clearToken();
               this.refreshPage();
            } else {
               this.page = page;
               if (page == SkinLibraryScreen.Page.EDITOR) {
                  this.x0 = 0.0F;
                  this.x1 = 1.0F;
                  this.y0 = 0.0F;
                  this.y1 = 1.0F;
                  this.uploading = false;
               }

               if (page == SkinLibraryScreen.Page.LIBRARY) {
                  this.refreshContentList();
               } else {
                  this.rebuild();
               }
            }
         }
      }
   }

   private void openHelp() {
      try {
         class_156.method_668().method_673(URI.create("https://github.com/Luke100000/minecraft-comes-alive/wiki/Skin-Editor"));
      } catch (Exception e) {
         MCA.LOGGER.error(e);
      }
   }

   private void refreshContentList() {
      if (Thread.currentThread() != this.thread) {
         assert this.field_22787 != null;
         this.field_22787.method_40000(this::refreshContentList);
      } else {
         this.refreshServerContent();

         List newList = switch (this.subscriptionFilter) {
            case LIBRARY -> {
               this.loadPage();
               yield this.libraryContents;
            }
            case GLOBAL -> this.serverContent;
            case LIKED -> this.currentUser != null ? this.currentUser.likes() : Collections.emptyList();
            case SUBMISSIONS -> this.currentUser != null ? this.currentUser.submissions() : Collections.emptyList();
            default -> throw new IllegalStateException("Unexpected value: " + this.subscriptionFilter);
         };
         if (this.subscriptionFilter != SkinLibraryScreen.SubscriptionFilter.LIBRARY && !newList.isEmpty()) {
            if (this.selectionPage * 15 >= newList.size()) {
               newList = new LinkedList();
            } else {
               newList = newList.subList(this.selectionPage * 15, Math.min(newList.size(), (this.selectionPage + 1) * 15));
            }
         }

         this.contents.clear();
         this.contents.addAll(newList);
         if (this.contents.isEmpty() && this.selectionPage > 0) {
            this.selectionPage--;
            if (this.subscriptionFilter == SkinLibraryScreen.SubscriptionFilter.LIBRARY) {
               this.loadPage();
            } else {
               this.refreshContentList();
            }
         } else {
            this.rebuild();
            this.setSelectionPage(this.selectionPage);
         }
      }
   }

   private String getPlayerName() {
      return class_310.method_1551().field_1724 == null ? "Unknown" : class_310.method_1551().field_1724.method_7334().getName();
   }

   private boolean isOp() {
      return class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724.method_5687(4);
   }

   private void setSelectionPage(int p) {
      if (this.pageWidget != null) {
         this.selectionPage = Math.max(0, p);
         this.pageWidget.method_25355(class_2561.method_43469("gui.villager_editor.page", new Object[]{this.selectionPage + 1}));
      }
   }

   public void method_29638(List<Path> paths) {
      Path path = paths.get(0);
      this.loadImage(path.toString());
   }

   private void loadImage(String path) {
      InputStream stream = null;

      try {
         stream = new URL(path).openStream();
      } catch (Exception exception) {
         try {
            stream = new FileInputStream(path);
         } catch (Exception e) {
            MCA.LOGGER.error(e);
         }
      }

      if (stream != null) {
         try {
            class_1011 image = class_1011.method_4309(stream);
            stream.close();
            if (image.method_4307() == 64 && image.method_4323() == 64) {
               if (SkinPorter.isSlimFormat(image)) {
                  SkinPorter.convertSlimToDefault(image);
               }

               this.workspace = new Workspace(image);
               this.setPage(SkinLibraryScreen.Page.EDITOR_TYPE);
            } else if (image.method_4307() == 64 && image.method_4323() == 32) {
               this.workspace = new Workspace(SkinPorter.portLegacySkin(image));
               this.setPage(SkinLibraryScreen.Page.EDITOR_TYPE);
            } else {
               this.setError(class_2561.method_43471("gui.skin_library.not_64"));
            }
         } catch (IOException e) {
            MCA.LOGGER.error(e);
         }
      }
   }

   private void publish() {
      if (this.workspace.title.equals("Unnamed Asset")) {
         this.setError(class_2561.method_43471("gui.skin_library.choose_name"));
      } else if (!Utils.verify(this.workspace.currentImage)) {
         this.setError(class_2561.method_43471("gui.skin_library.read_the_help"));
      } else if (this.workspace.skinType == SkinLibraryScreen.SkinType.HAIR && !Utils.verifyHair(this.workspace.currentImage) && !this.skipHairWarning) {
         this.setError(class_2561.method_43471("gui.skin_library.read_the_help_hair"));
         this.skipHairWarning = true;
      } else {
         this.skipHairWarning = false;
         if (!this.uploading) {
            this.uploading = true;
            CompletableFuture.runAsync(
               () -> {
                  if (Auth.hasToken()) {
                     Response request = null;

                     try {
                        request = Api.request(
                           this.workspace.contentid == -1 ? Api.HttpMethod.POST : Api.HttpMethod.PUT,
                           this.workspace.contentid == -1 ? ContentIdResponse.class : SuccessResponse.class,
                           this.workspace.contentid == -1 ? "content/mca" : "content/mca/" + this.workspace.contentid,
                           Map.of(),
                           Map.of(
                              "title",
                              this.workspace.title,
                              "meta",
                              this.workspace.toListEntry().toJson().toString(),
                              "data",
                              new String(Base64.getEncoder().encode(this.workspace.currentImage.method_24036()))
                           )
                        );
                     } catch (IOException e) {
                        MCA.LOGGER.error(e);
                     }

                     if (request instanceof ContentIdResponse || request instanceof SuccessResponse) {
                        Response finalRequest = request;
                        this.reloadDatabase(() -> {
                           int contentid = finalRequest instanceof ContentIdResponse responsex ? responsex.contentid() : this.workspace.contentid;
                           this.setTag(contentid, this.workspace.skinType.name().toLowerCase(Locale.ROOT), true);
                           if (this.workspace.profession != null) {
                              this.setTag(contentid, this.workspace.profession.replace("mca.", ""), true);
                           }

                           this.getSubmittedContent(contentid).or(() -> this.getContentById(contentid)).ifPresent(content -> {
                              this.focusedContent = content;
                              this.setPage(SkinLibraryScreen.Page.DETAIL);
                              this.uploading = false;
                           });
                           SkinCache.enforceSync(contentid);
                        });
                     } else if (request instanceof ErrorResponse response) {
                        if (response.code() == 428) {
                           this.setError(class_2561.method_43471("gui.skin_library.upload_duplicate"));
                        } else {
                           this.setError(class_2561.method_43471("gui.skin_library.upload_failed"));
                        }

                        this.uploading = false;
                     }
                  }
               }
            );
         } else {
            this.setError(class_2561.method_43471("gui.skin_library.already_uploading"));
         }
      }
   }

   private Optional<LiteContent> getContentById(int contentid) {
      return Stream.concat(this.libraryContents.stream(), this.serverContent.stream()).filter(v -> v.contentid() == contentid).findAny();
   }

   private Optional<LiteContent> getServerContentById(int contentid) {
      return this.serverContent.stream().filter(v -> v.contentid() == contentid).findAny();
   }

   private Optional<LiteContent> getSubmittedContent(int contentid) {
      return this.currentUser == null ? Optional.empty() : this.currentUser.submissions().stream().filter(v -> v.contentid() == contentid).findAny();
   }

   private void setTag(int contentid, String tag, boolean add) {
      if (Auth.hasToken()) {
         Api.request(add ? Api.HttpMethod.POST : Api.HttpMethod.DELETE, SuccessResponse.class, "tag/mca/" + contentid + "/" + tag);
         this.getContentById(contentid).ifPresent(c -> {
            if (add) {
               c.tags().add(tag);
            } else {
               c.tags().remove(tag);
            }
         });
         this.getSubmittedContent(contentid).ifPresent(c -> {
            if (add) {
               c.tags().add(tag);
            } else {
               c.tags().remove(tag);
            }
         });
      }
   }

   private void removeContent(int contentId) {
      if (Auth.hasToken()) {
         Api.request(Api.HttpMethod.DELETE, SuccessResponse.class, "content/mca/" + contentId);
         this.removeContentLocally(contentId);
      }
   }

   private void removeContentLocally(int contentId) {
      this.libraryContents.removeIf(v -> v.contentid() == contentId);
      if (this.currentUser != null) {
         this.currentUser.likes().removeIf(v -> v.contentid() == contentId);
         this.currentUser.submissions().removeIf(v -> v.contentid() == contentId);
      }
   }

   private void reportContent(int contentId, String reason) {
      if (Auth.hasToken()) {
         Api.request(Api.HttpMethod.POST, SuccessResponse.class, "report/mca/" + contentId + "/" + reason);
         if (reason.equals("DEFAULT")) {
            this.removeContentLocally(contentId);
         }

         this.setError(class_2561.method_43471("gui.skin_library.reported"));
      }
   }

   private void setLike(int contentid, boolean add) {
      if (Auth.hasToken() && this.currentUser != null) {
         Api.request(add ? Api.HttpMethod.POST : Api.HttpMethod.DELETE, SuccessResponse.class, "like/mca/" + contentid);
         if (add) {
            this.getContentById(contentid).ifPresent(this.currentUser.likes()::add);
         } else {
            this.currentUser.likes().removeIf(v -> v.contentid() == contentid);
         }
      }
   }

   private void setBan(int userid, boolean banned) {
      if (Auth.hasToken() && this.currentUser != null) {
         Api.request(Api.HttpMethod.PUT, SuccessResponse.class, "user/" + userid, Map.of("banned", Boolean.toString(banned)));
      }
   }

   public void refreshPage() {
      this.setPage(this.page);
   }

   public void clearError() {
      this.error = null;
   }

   public void setError(class_2561 text) {
      this.error = text;
   }

   private <T> void addServerContent(Map<String, T> map, String type) {
      for (Entry<String, T> entry : map.entrySet()) {
         if (entry.getKey().startsWith("immersive_library:")) {
            try {
               int contentid = Integer.parseInt(entry.getKey().substring(18));
               this.serverContent.add(this.getContentById(contentid).orElse(new LiteContent(contentid, -1, "unknown", -1, Set.of(type), "unknown", -1)));
            } catch (NumberFormatException var6) {
            }
         }
      }
   }

   @Override
   public void skinListUpdatedCallback() {
      this.refreshServerContent();
      if (this.page == SkinLibraryScreen.Page.LIBRARY) {
         this.refreshContentList();
      }
   }

   private void refreshServerContent() {
      this.serverContent.clear();
      this.addServerContent(VillagerEditorScreen.getClothing(), "clothing");
      this.addServerContent(VillagerEditorScreen.getHair(), "hair");
   }

   public enum Page {
      LIBRARY,
      EDITOR_LOCKED,
      EDITOR_PREPARE,
      EDITOR_TYPE,
      EDITOR,
      HELP,
      LOGIN,
      LOGOUT,
      DETAIL,
      DELETE,
      LOADING,
      REPORT;
   }

   public enum SkinType {
      CLOTHING,
      HAIR;
   }

   public enum SortingMode {
      LIKES("likes"),
      NEWEST("date"),
      REPORTS("reports");

      public final String order;

      SortingMode(String order) {
         this.order = order;
      }
   }

   public enum SubscriptionFilter {
      LIBRARY,
      GLOBAL,
      LIKED,
      SUBMISSIONS;

      public static class_2561 getText(SkinLibraryScreen.SubscriptionFilter t) {
         return class_2561.method_43471("gui.skin_library.subscription_filter." + t.name().toLowerCase(Locale.ROOT));
      }
   }
}
