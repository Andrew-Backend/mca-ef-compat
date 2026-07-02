package yesman.epicfight.epicskins.client.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.exception.ExceptionUtils;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothColliderPresets;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.client.gui.datapack.widgets.CheckBox;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.epicskins.animation.EpicSkinsAnimations;
import yesman.epicfight.epicskins.client.widget.CapePopupBox;
import yesman.epicfight.epicskins.exception.HttpResponseException;
import yesman.epicfight.epicskins.user.AuthenticationHelperImpl;
import yesman.epicfight.epicskins.user.Cosmetic;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.AuthenticationHelper;

@OnlyIn(Dist.CLIENT)
public class AvatarEditScreen extends Screen {
   private static final List<AssetAccessor<? extends StaticAnimation>> IDLE_ANIMATIONS = List.of(
      EpicSkinsAnimations.BIPED_IDLE1, EpicSkinsAnimations.BIPED_IDLE2
   );
   private final Screen parentScreen;
   private final Queue<Runnable> deferredWorks = new ConcurrentLinkedQueue<>();
   private AvatarEditScreen.LayoutElements<?> capeOptionsLayout;
   private AvatarEditScreen.LayoutElements<?> capeSelectionLayout;
   private AvatarEditScreen.LayoutElements<?> activatedLayout;
   private CapePopupBox capePopupBox;
   private AvatarEditScreen.CapeList capeList;
   private InputComponentList<JsonElement> capeOptions;
   private ModelPreviewer mainModelPreviewer;
   private ColorSlider hueSlider;
   private ColorSlider saturationSlider;
   private ColorSlider brightnessSlider;
   private CheckBox useVanillTextureCheckBox;
   private int nextPlaying;
   private Button saveButton;
   private Button quitButton;
   private Button signOutButton;
   private Button backButton;

   public AvatarEditScreen(Screen screen) {
      super(Component.m_237115_("gui.epicskins.skin_config"));
      this.parentScreen = screen;
      this.f_96541_ = screen.getMinecraft();
      this.f_96547_ = this.f_96541_.f_91062_;
      this.saveButton = Button.m_253074_(Component.m_237115_("gui.epicskins.button.save"), button -> {
         this.popupResponseAwaiting();
         AuthenticationHelperImpl.CapeProperties epicskinsInfox = AuthenticationHelperImpl.getInstance().capeProperties();
         epicskinsInfox.setCape(this.capePopupBox._getValue() != null ? this.capePopupBox._getValue().seq() : -1);
         epicskinsInfox.setHue(this.hueSlider.getPosition());
         epicskinsInfox.setSaturation(this.saturationSlider.getPosition());
         epicskinsInfox.setBrightness(this.brightnessSlider.getPosition());
         epicskinsInfox.setVanillaTextureUse(this.useVanillTextureCheckBox._getValue());
         AuthenticationHelperImpl.getInstance().sendSaveRequest(ex -> {
            if (ex instanceof HttpResponseException httpFailResponseException) {
               httpFailResponseException.printStackTrace();
               this.popupHttpResponseException(httpFailResponseException.getHttpStatusCode(), httpFailResponseException.getResponseBody());
            } else if (ex != null) {
               this.popupConnectionFail(ex);
            } else {
               this.popupSavedSuccess();
            }
         });
      }).m_253046_(80, 20).m_253136_();
      this.saveButton.f_93623_ = AuthenticationHelperImpl.getInstance().status() == AuthenticationHelper.Status.AUTHENTICATED;
      this.quitButton = Button.m_253074_(Component.m_237115_("gui.epicskins.button.quit"), button -> Minecraft.m_91087_().m_91152_(this.parentScreen))
         .m_253046_(80, 20)
         .m_253136_();
      this.signOutButton = Button.m_253074_(Component.m_237115_("gui.epicskins.button.delete_account"), button -> this.popupSignOutAsk())
         .m_253046_(90, 20)
         .m_253136_();
      this.backButton = Button.m_253074_(
            Component.m_237115_("gui.epicskins.button.back"),
            button -> this.deferredWorks
               .add(
                  () -> {
                     this.capeOptionsLayout.accept(x$0 -> this.m_142416_((GuiEventListener)x$0));
                     this.capeSelectionLayout.accept(x$0 -> this.m_169411_((GuiEventListener)x$0));
                     this.activatedLayout = this.capeOptionsLayout;
                     Cosmetic cosmeticx = this.capePopupBox._getValue();
                     if (cosmeticx != null) {
                        cosmeticx.getAsMesh(
                           mesh -> {
                              this.mainModelPreviewer.setCloakColor(cosmeticx.useIntParam1() ? this.brightnessSlider.getColor() : -1);
                              this.mainModelPreviewer
                                 .initCloakInfo(
                                    (SoftBodyTranslatable)mesh,
                                    cosmeticx.textureLocation(),
                                    ClothSimulator.ClothObjectBuilder.create().parentJoint(Armatures.BIPED.get().torso).putAll(ClothColliderPresets.BIPED)
                                 );
                              this.capePopupBox._setValue(cosmeticx);
                              this.refreshOptionComponents(cosmeticx);
                           }
                        );
                     } else {
                        this.mainModelPreviewer.removeCloak();
                     }
                  }
               )
         )
         .m_253046_(200, 20)
         .m_253136_();
      this.mainModelPreviewer = new ModelPreviewer(
         165, 10, 15, 34, ResizableComponent.HorizontalSizing.WIDTH_RIGHT, ResizableComponent.VerticalSizing.TOP_BOTTOM, Armatures.BIPED, Meshes.BIPED
      );
      this.mainModelPreviewer.setBackgroundClearColor(new Vec4f(0.0F, 0.0F, 0.0F, 0.0F));
      this.mainModelPreviewer.enableZoomingCamera(false);
      this.mainModelPreviewer.enableCameraMove(false);
      this.mainModelPreviewer.setCameraTransform(-3.0, 28.0F, 160.0F, 0.0F, 0.0F);
      this.mainModelPreviewer.setFigureTexture(AuthenticationHelperImpl.getInstance().playerInfo().m_105337_());
      if ("slim".equals(AuthenticationHelperImpl.getInstance().playerInfo().m_105336_())) {
         this.mainModelPreviewer.setMesh(Meshes.ALEX);
      } else {
         this.mainModelPreviewer.setMesh(Meshes.BIPED);
      }

      this.mainModelPreviewer.addAnimationToPlay(EpicSkinsAnimations.BIPED_STANDING);
      RandomSource randomSource = RandomSource.m_216335_(System.currentTimeMillis());
      this.nextPlaying = 160 + randomSource.m_188503_(120);
      this.capeOptions = new InputComponentList<JsonElement>(this, 0, 0, 0, 0, 30) {
         public void importTag(JsonElement tag) {
         }
      };
      this.capePopupBox = new CapePopupBox(
         this,
         this.f_96547_,
         0,
         10,
         60,
         15,
         ResizableComponent.HorizontalSizing.LEFT_RIGHT,
         null,
         Component.m_237115_("gui.epicskins.avatar.cape_type"),
         cosmeticx -> ParseUtil.nullOrToString(cosmeticx, Cosmetic::title),
         () -> this.deferredWorks.add(() -> {
            label25:
            for (AvatarEditScreen.CapeList.CosmeticsEntry e : this.capeList.m_6702_()) {
               for (GuiEventListener block : e.m_6702_()) {
                  AvatarEditScreen.CapeList.CapeBlock capeBlock = (AvatarEditScreen.CapeList.CapeBlock)block;
                  if (capeBlock.cosmetic == this.capePopupBox._getValue()) {
                     this.capeList.m_7522_(e);
                     if (this.capeList.focused != null) {
                        this.capeList.focused.m_93692_(false);
                     }

                     this.capeList.focused = capeBlock;
                     capeBlock.m_93692_(true);
                     break label25;
                  }
               }
            }

            this.capeList.moveScrollToFocus();
            this.capeOptionsLayout.accept(x$0 -> this.m_169411_((GuiEventListener)x$0));
            this.capeSelectionLayout.accept(x$0 -> this.m_142416_((GuiEventListener)x$0));
            this.activatedLayout = this.capeSelectionLayout;
         }),
         null
      );
      this.capeOptions.newRow();
      this.capeOptions
         .addComponentCurrentRow(
            new Static(this, this.capeOptions.nextStart(8), 80, 60, 15, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, "gui.epicskins.avatar.cape_type")
         );
      this.capeOptions.addComponentCurrentRow(this.capePopupBox.relocateX(this.m_264198_(), this.capeOptions.nextStart(8)));
      this.hueSlider = new ColorSlider(
         this.f_96547_,
         this.capeOptions.nextStart(8),
         10,
         60,
         15,
         ResizableComponent.HorizontalSizing.LEFT_RIGHT,
         null,
         Component.m_237115_("gui.epicskins.cape_color"),
         ColorSlider.Style.SIMPLE,
         0.0,
         (position, color) -> {
            this.saturationSlider.changeColor(color, 0);
            this.brightnessSlider.changeColor(this.saturationSlider.getColor(), 0);
            if ((!this.capePopupBox._getValue().useBoolParam1() || !this.useVanillTextureCheckBox._getValue()) && this.capePopupBox._getValue().useIntParam1()) {
               this.mainModelPreviewer.setCloakColor(this.brightnessSlider.getColor());
            }
         }
      );
      this.saturationSlider = new ColorSlider(
         this.f_96547_,
         0,
         10,
         60,
         15,
         ResizableComponent.HorizontalSizing.LEFT_RIGHT,
         null,
         Component.m_237115_("gui.epicskins.cape_color"),
         ColorSlider.Style.SIMPLE,
         0.0,
         (position, color) -> {
            this.brightnessSlider.changeColor(color, 0);
            if ((!this.capePopupBox._getValue().useBoolParam1() || !this.useVanillTextureCheckBox._getValue()) && this.capePopupBox._getValue().useIntParam1()) {
               this.mainModelPreviewer.setCloakColor(this.brightnessSlider.getColor());
            }
         },
         -65536,
         -1
      );
      this.brightnessSlider = new ColorSlider(
         this.f_96547_,
         0,
         10,
         60,
         15,
         ResizableComponent.HorizontalSizing.LEFT_RIGHT,
         null,
         Component.m_237115_("gui.epicskins.cape_color"),
         ColorSlider.Style.SIMPLE,
         0.0,
         (position, color) -> {
            if ((!this.capePopupBox._getValue().useBoolParam1() || !this.useVanillTextureCheckBox._getValue()) && this.capePopupBox._getValue().useIntParam1()) {
               this.mainModelPreviewer.setCloakColor(color);
            }
         },
         -65536,
         -16777216
      );
      this.useVanillTextureCheckBox = new CheckBox(
         this.f_96547_, this.capeOptions.nextStart(8), 60, 0, 10, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, false, Component.m_237113_(""), null
      );
      Consumer<Boolean> responder = val -> {
         if (this.capeList != null) {
            this.mainModelPreviewer
               .setCloakTexture(val ? AuthenticationHelperImpl.getInstance().playerInfo().m_105338_() : this.capePopupBox._getValue().textureLocation());
            this.mainModelPreviewer.setCloakColor(!val && this.capePopupBox._getValue().useIntParam1() ? this.brightnessSlider.getColor() : -1);
         }
      };
      AuthenticationHelperImpl.CapeProperties epicskinsInfo = AuthenticationHelperImpl.getInstance().capeProperties();
      Cosmetic cosmetic = AuthenticationHelperImpl.getInstance().getCosmetic(epicskinsInfo.capeSeq());
      if (cosmetic != null) {
         cosmetic.getAsMesh(
            mesh -> {
               this.mainModelPreviewer.setCloakColor(cosmetic.useIntParam1() && !epicskinsInfo.useVanillaTexture() ? this.brightnessSlider.getColor() : -1);
               this.mainModelPreviewer
                  .initCloakInfo(
                     (SoftBodyTranslatable)mesh,
                     cosmetic.useBoolParam1() && epicskinsInfo.useVanillaTexture()
                        ? AuthenticationHelperImpl.getInstance().playerInfo().m_105338_()
                        : cosmetic.textureLocation(),
                     ClothSimulator.ClothObjectBuilder.create().parentJoint(Armatures.BIPED.get().torso).putAll(ClothColliderPresets.BIPED)
                  );
               this.refreshOptionComponents(cosmetic);
            }
         );
         this.capePopupBox._setValue(cosmetic);
         this.useVanillTextureCheckBox._setResponder(responder);
         this.useVanillTextureCheckBox._setValue(epicskinsInfo.useVanillaTexture());
         this.hueSlider.m_93611_(epicskinsInfo.hue());
         this.saturationSlider.m_93611_(epicskinsInfo.saturation());
         this.brightnessSlider.m_93611_(epicskinsInfo.brightness());
      } else {
         this.useVanillTextureCheckBox._setValue(false);
         this.useVanillTextureCheckBox._setResponder(responder);
      }

      this.capeList = new AvatarEditScreen.CapeList(this.f_96541_, (int)(this.f_96543_ * 0.6F) - 20, this.f_96544_ - 64, 30, this.f_96544_ - 34);
      this.capeOptionsLayout = new AvatarEditScreen.LayoutElements(this.capeOptions, this.saveButton, this.quitButton, this.signOutButton);
      this.capeSelectionLayout = new AvatarEditScreen.LayoutElements(this.capeList, this.backButton);
   }

   public void refreshOptionComponents(Cosmetic selectedCosmetic) {
      this.capeOptions.clearComponents();
      switch (selectedCosmetic.slot()) {
         case CAPE:
            this.capeOptions.newRow();
            this.capeOptions
               .addComponentCurrentRow(
                  new Static(
                     this, this.capeOptions.nextStart(8), 80, 60, 15, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, "gui.epicskins.avatar.cape_type"
                  )
               );
            this.capeOptions.addComponentCurrentRow(this.capePopupBox.relocateX(this.m_264198_(), this.capeOptions.nextStart(8)));
            if (selectedCosmetic.useIntParam1()) {
               this.capeOptions.newRow();
               this.capeOptions
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.capeOptions.nextStart(8),
                        80,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "gui.epicskins.avatar.cape_color"
                     )
                  );
               this.capeOptions.addComponentCurrentRow(this.hueSlider.relocateX(this.m_264198_(), this.capeOptions.nextStart(8)));
               this.capeOptions.newRow();
               this.capeOptions.addComponentCurrentRow(this.saturationSlider.relocateX(this.m_264198_(), this.capeOptions.nextStart(96)));
               this.capeOptions.newRow();
               this.capeOptions.addComponentCurrentRow(this.brightnessSlider.relocateX(this.m_264198_(), this.capeOptions.nextStart(96)));
            }

            if (selectedCosmetic.useBoolParam1() && AuthenticationHelperImpl.getInstance().playerInfo().m_105338_() != null) {
               this.capeOptions.newRow();
               this.capeOptions
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.capeOptions.nextStart(8),
                        80,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "gui.epicskins.avatar.vanilla_texture"
                     )
                  );
               this.capeOptions.addComponentCurrentRow(this.useVanillTextureCheckBox.relocateX(this.m_264198_(), this.capeOptions.nextStart(8)));
            }
         default:
            this.capeOptions.m_93437_((int)(this.f_96543_ * 0.6F) - 20, this.f_96544_ - 64, 30, this.f_96544_ - 34);
            this.capeOptions.m_93507_(10);
      }
   }

   protected void m_7856_() {
      this.saveButton.m_264152_(this.f_96543_ / 2 - 125, this.f_96544_ - 28);
      this.quitButton.m_264152_(this.f_96543_ / 2 - 40, this.f_96544_ - 28);
      this.signOutButton.m_264152_(this.f_96543_ / 2 + 45, this.f_96544_ - 28);
      this.signOutButton.f_93623_ = AuthenticationHelperImpl.getInstance().status() == AuthenticationHelper.Status.AUTHENTICATED;
      this.backButton.m_264152_(this.f_96543_ / 2 - 100, this.f_96544_ - 28);
      this.mainModelPreviewer.setX1((int)(this.f_96543_ * 0.4F));
      this.mainModelPreviewer.resize(this.m_264198_());
      this.capeOptions.m_93437_((int)(this.f_96543_ * 0.6F) - 20, this.f_96544_ - 64, 30, this.f_96544_ - 34);
      this.capeOptions.m_93507_(10);
      this.capeList.m_93437_((int)(this.f_96543_ * 0.6F) - 20, this.f_96544_ - 64, 30, this.f_96544_ - 34);
      this.capeList.m_93507_(10);
      this.capeList.rearrangeCosmetics();
      this.m_142416_(this.mainModelPreviewer);
      if (!this.f_267454_) {
         if (this.capePopupBox._getValue() != null && this.capePopupBox._getValue().useBoolParam1() && this.useVanillTextureCheckBox._getValue()) {
            this.mainModelPreviewer.setCloakTexture(AuthenticationHelperImpl.getInstance().playerInfo().m_105338_());
         }

         this.f_267454_ = true;
         this.capeOptionsLayout.accept(x$0 -> this.m_142416_((GuiEventListener)x$0));
         this.activatedLayout = this.capeOptionsLayout;
         switch (AuthenticationHelperImpl.getInstance().status()) {
            case UNAUTHENTICATED:
               this.popupStartSigningIn();
               break;
            case OFFLINE_MODE:
               this.popupOfflineModeWarning();
            case AUTHENTICATED:
         }
      } else {
         this.activatedLayout.accept(x$0 -> this.m_142416_((GuiEventListener)x$0));
      }
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.m_94729_(mouseX, mouseY).filter(listener -> listener.m_7979_(mouseX, mouseY, button, dx, dy)).isPresent();
   }

   public void m_86600_() {
      Runnable work = null;

      while ((work = this.deferredWorks.poll()) != null) {
         work.run();
      }

      if (--this.nextPlaying < 0) {
         RandomSource randomSource = RandomSource.m_216335_(System.currentTimeMillis());
         this.mainModelPreviewer.getAnimator().playAnimation(IDLE_ANIMATIONS.get(randomSource.m_188503_(IDLE_ANIMATIONS.size())), 0.0F);
         this.nextPlaying = 160 + randomSource.m_188503_(120);
      }

      this.mainModelPreviewer._tick();
   }

   public void onSigninSuccess() {
      this.deferredWorks.add(() -> {
         this.saveButton.f_93623_ = true;
         this.f_96541_.m_91152_(this);
         this.capeList.rearrangeCosmetics();
         AuthenticationHelperImpl.CapeProperties userskinInfo = AuthenticationHelperImpl.getInstance().capeProperties();
         Cosmetic cosmetic = AuthenticationHelperImpl.getInstance().getCosmetic(userskinInfo.capeSeq());
         if (cosmetic != null) {
            this.capePopupBox._setValue(cosmetic);
            this.hueSlider.m_93611_(userskinInfo.hue());
            this.saturationSlider.m_93611_(userskinInfo.saturation());
            this.brightnessSlider.m_93611_(userskinInfo.brightness());
            this.useVanillTextureCheckBox._setValue(userskinInfo.useVanillaTexture());
            this.refreshOptionComponents(cosmetic);
         }
      });
   }

   public void popupStartSigningIn() {
      this.deferredWorks.add(() -> this.f_96541_.m_91152_(new MessageScreen("Sign up/in first before setting up cosmetics", "", this, button -> {
         this.popupAuthCodeEnter();
         AuthenticationHelperImpl.getInstance().openAuthenticateBrowser();
      }, 240, 70).autoCalculateHeight()));
   }

   public void popupAuthCodeEnter() {
      this.deferredWorks.add(() -> {
         ResizableEditBox verifyingBox = new ResizableEditBox(this.f_96541_.f_91062_, 0, 0, 0, 16, Component.m_237113_(""), null, null);
         verifyingBox.m_94199_(6);
         this.f_96541_.m_91152_(new MessageScreen<>("Enter the 6-characters verifying code", "", this, code -> {
            this.popupResponseAwaiting();
            AuthenticationHelperImpl.getInstance().loginWithAuthCode(code, () -> this.onSigninSuccess(), ex -> {
               ex.printStackTrace();
               if (ex instanceof HttpResponseException httpFailResponseException) {
                  switch (httpFailResponseException.getResponseBody()) {
                     case "expired":
                        this.popupVerifyCodeExpired();
                        break;
                     case "invalid_code":
                        this.popupInvalidVerifyingCode();
                        break;
                     case "not_an_owner":
                        this.popupInvalidVerifyingCode();
                        break;
                     default:
                        this.popupHttpResponseException(httpFailResponseException.getHttpStatusCode(), httpFailResponseException.getResponseBody());
                  }
               } else {
                  this.popupException(ex);
               }
            });
         }, button -> this.f_96541_.m_91152_(this), verifyingBox, 300, 90));
      });
   }

   public void popupInvalidVerifyingCode() {
      this.deferredWorks
         .add(
            () -> {
               ResizableEditBox verifyingBox = new ResizableEditBox(this.f_96541_.f_91062_, 0, 0, 0, 16, Component.m_237113_(""), null, null);
               verifyingBox.m_94199_(6);
               this.f_96541_
                  .m_91152_(
                     new MessageScreen<>(
                        "Invalid verifying code!",
                        "Please try again",
                        this,
                        code -> AuthenticationHelperImpl.getInstance().loginWithAuthCode(code, () -> this.onSigninSuccess(), ex -> {
                           if (ex instanceof HttpResponseException httpFailResponseException) {
                              switch (httpFailResponseException.getResponseBody()) {
                                 case "expired":
                                    this.popupVerifyCodeExpired();
                                    break;
                                 case "invalid_code":
                                    this.popupInvalidVerifyingCode();
                                    break;
                                 case "not_an_owner":
                                    this.popupInvalidVerifyingCode();
                              }
                           } else {
                              this.popupException(ex);
                           }
                        }),
                        button -> this.f_96541_.m_91152_(this),
                        verifyingBox,
                        200,
                        100
                     )
                  );
            }
         );
   }

   public void popupVerifyCodeExpired() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen("Verifying code expired!", "", this, button2 -> this.f_96541_.m_91152_(this.parentScreen), 200, 70).autoCalculateHeight()
               )
         );
   }

   public void popupOfflineModeWarning() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Failed to verify your Minecraft account",
                        "You may use cracked or unofficial launcher, please use official launcher to connect your Minecraft account.",
                        this,
                        button2 -> this.f_96541_.m_91152_(this.parentScreen),
                        300,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupConnectionFail(Throwable exception) {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Connection failed",
                        ExceptionUtils.getRootCause(exception).getMessage()
                           + "\n\nPlease try later\nIf the problem persists, please contact us by the links below:\nOfficial Discord Server: https://discord.com/invite/NbAJwj8RHg\nGithub Issue Trakcer: https://github.com/Yesssssman/epicfightmod/issues",
                        this,
                        button2 -> this.f_96541_.m_91152_(this.parentScreen),
                        300,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupException(Throwable exception) {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Connection failed",
                        ExceptionUtils.getRootCause(exception)
                           + ": "
                           + ExceptionUtils.getRootCause(exception).getMessage()
                           + "\n\nPlease report the issue to the link below\nGithub Issue Trakcer: https://github.com/Yesssssman/epicfightmod/issues",
                        this,
                        button2 -> this.f_96541_.m_91152_(this.parentScreen),
                        300,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupHttpResponseException(int responseCode, String responseBody) {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        responseCode + " Response",
                        responseBody + "\n\nPlease report the issue to the link below\nGithub Issue Trakcer: https://github.com/Yesssssman/epicfightmod/issues",
                        this,
                        button2 -> this.f_96541_.m_91152_(this.parentScreen),
                        300,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupAccessDenied() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen("Failed in Discord authentication", "Access denied.", this, button2 -> this.f_96541_.m_91152_(this), 250, 70)
                     .autoCalculateHeight()
               )
         );
   }

   public void popupNoUserInfo() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Failed in Discord authentication",
                        "Can't find your account in Epic Fight discord server. Please join our Discord server to identify your account.\nOfficial Discord Server: https://discord.com/invite/NbAJwj8RHg",
                        this,
                        button2 -> this.f_96541_.m_91152_(this),
                        250,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupDuplicatedDiscordID(String errMsg) {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Failed in Discord authentication",
                        errMsg + "\nPlease contact our developer team to get any supports.\nOfficial Discord Server: https://discord.com/invite/NbAJwj8RHg",
                        this,
                        button2 -> this.f_96541_.m_91152_(this),
                        330,
                        70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupSignOutAsk() {
      this.deferredWorks.add(() -> this.f_96541_.m_91152_(new MessageScreen("Do you want to sign out?", "", this, ok -> {
         this.popupResponseAwaiting();
         AuthenticationHelperImpl.getInstance().signOut(() -> this.popupSignOutSuccess(), ex -> {
            if (ex instanceof HttpResponseException httpFailResponseException) {
               if (httpFailResponseException.getHttpStatusCode() == 404) {
                  AuthenticationHelperImpl.getInstance().onSignOut();
               }

               this.popupHttpResponseException(httpFailResponseException.getHttpStatusCode(), httpFailResponseException.getResponseBody());
            } else {
               this.popupException(ex);
            }
         });
      }, cancel -> this.f_96541_.m_91152_(this), 200, 90).autoCalculateHeight()));
   }

   public void popupSavedSuccess() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(new MessageScreen("Successfully saved!", "", this, button2 -> this.f_96541_.m_91152_(this), 200, 70).autoCalculateHeight())
         );
   }

   public void popupSignOutSuccess() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen("Successfully signed out!", "", this, button2 -> this.f_96541_.m_91152_(new TitleScreen()), 200, 70).autoCalculateHeight()
               )
         );
   }

   public void popupTimeout() {
      this.deferredWorks
         .add(
            () -> this.f_96541_
               .m_91152_(
                  new MessageScreen(
                        "Failed in Discord authentication", "Session timed out! Please try it later.", this, button2 -> this.f_96541_.m_91152_(this), 300, 70
                     )
                     .autoCalculateHeight()
               )
         );
   }

   public void popupResponseAwaiting() {
      this.deferredWorks
         .add(() -> this.f_96541_.m_91152_(new AwaitIconMessageScreen("Awaiting for response...", "", this, 200, 90).withOkTitle(CommonComponents.f_130656_)));
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280273_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.m_280653_(this.f_96547_, this.f_96539_, 56, 15, 16777215);
   }

   public void m_7379_() {
      this.capeList.destroy();
      this.f_96541_.m_91152_(this.parentScreen);
   }

   @OnlyIn(Dist.CLIENT)
   public class CapeList extends ContainerObjectSelectionList<AvatarEditScreen.CapeList.CosmeticsEntry> {
      private AvatarEditScreen.CapeList.CosmeticBlock focused;

      public CapeList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1) {
         super(pMinecraft, pWidth, pHeight, pY0, pY1, 0);
         this.m_93488_(false);
         this.m_93496_(false);
      }

      public Cosmetic getSelectedCloak() {
         return this.focused != null ? this.focused.getCosmetic() : null;
      }

      public void moveScrollToFocus() {
         int idx = this.m_6702_().indexOf(this.m_7222_());
         if (idx > -1) {
            this.m_93410_(this.f_93387_ * idx);
         }
      }

      public void rearrangeCosmetics() {
         int blockLength = 0;
         int columns = 1;

         while (true) {
            int rowWidth = this.f_93388_ / columns;
            if (rowWidth < 200) {
               blockLength = rowWidth;
               this.f_93387_ = blockLength;
               this.m_93516_();
               rowWidth = 0;
               int row = 0;
               Builder builder = ImmutableList.builder();

               for (Cosmetic cosmetic : AuthenticationHelperImpl.getInstance().getAllCosmetics()) {
                  AvatarEditScreen.CapeList.CapeBlock capeButton = new AvatarEditScreen.CapeList.CapeBlock(
                     this.f_93393_ + rowWidth * blockLength + 3, this.f_93390_ + row * blockLength + 3, blockLength - 6, blockLength - 6, cosmetic
                  );
                  capeButton.m_257544_(Tooltip.m_257550_(Component.m_237113_(cosmetic.description())));
                  builder.add(capeButton);
                  if (++rowWidth == columns) {
                     this.m_7085_(new AvatarEditScreen.CapeList.CosmeticsEntry(builder.build()));
                     builder = ImmutableList.builder();
                     rowWidth = 0;
                     row++;
                  }
               }

               List<AvatarEditScreen.CapeList.CosmeticBlock> list = builder.build();
               if (!list.isEmpty()) {
                  this.m_7085_(new AvatarEditScreen.CapeList.CosmeticsEntry(list));
               }

               return;
            }

            columns++;
         }
      }

      protected int m_5756_() {
         return this.getRight() - 6;
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected void m_93516_() {
         this.destroy();
         super.m_93516_();
      }

      public void destroy() {
         this.m_6702_().forEach(entry -> entry.cosmeticsBlock.forEach(AvatarEditScreen.CapeList.CosmeticBlock::destroy));
      }

      public boolean m_6375_(double pMouseX, double pMouseY, int pButton) {
         this.m_93481_(pMouseX, pMouseY, pButton);
         if (!this.m_5953_(pMouseX, pMouseY)) {
            return false;
         }

         for (int i1 = 0; i1 < this.m_5773_(); i1++) {
            int j1 = this.m_7610_(i1);
            int k1 = this.m_93485_(i1);
            if (k1 >= this.f_93390_ && j1 <= this.getBottom()) {
               AvatarEditScreen.CapeList.CosmeticsEntry entry = (AvatarEditScreen.CapeList.CosmeticsEntry)this.m_6702_().get(i1);

               for (AvatarEditScreen.CapeList.CosmeticBlock b : entry.cosmeticsBlock) {
                  if (b.m_6375_(pMouseX, pMouseY, pButton)) {
                     AvatarEditScreen.CapeList.CosmeticsEntry oEntry = (AvatarEditScreen.CapeList.CosmeticsEntry)this.m_7222_();
                     if (entry != oEntry && oEntry instanceof ContainerEventHandler containereventhandler) {
                        containereventhandler.m_7522_((GuiEventListener)null);
                     }

                     this.m_7522_(entry);
                     this.m_7897_(true);
                     b.onPress(b == this.focused);
                     if (this.focused != null) {
                        this.focused.m_93692_(false);
                     }

                     this.focused = b;
                     this.focused.m_93692_(true);
                     return true;
                  }
               }
            }
         }

         return this.f_93399_;
      }

      @OnlyIn(Dist.CLIENT)
      public class CapeBlock extends AvatarEditScreen.CapeList.CosmeticBlock {
         private static final ResourceLocation LOCK_ICON = ResourceLocation.fromNamespaceAndPath("epicfight", "textures/gui/widget/lock.png");
         private final ModelPreviewer modelPreviewer;
         private Cosmetic cosmetic;

         public CapeBlock(int pX, int pY, int pWidth, int pHeight, Cosmetic cosmetic) {
            super(pX, pY, pWidth, pHeight, Component.m_237119_());
            this.cosmetic = cosmetic;
            switch (cosmetic.slot()) {
               case CAPE:
                  this.modelPreviewer = new ModelPreviewer(pX, pWidth, pY, pHeight, null, null, null, Meshes.BIPED);
                  this.modelPreviewer.setBackgroundClearColor(new Vec4f(0.3552F, 0.3552F, 0.3552F, 0.3F));
                  this.modelPreviewer.setCameraTransform(-2.5, 18.75F, 16.25F, -1.1641532E-8F, -0.0F);
                  cosmetic.getAsMesh(mesh -> this.modelPreviewer.initCloakInfo((SoftBodyTranslatable)mesh, cosmetic.textureLocation(), null));
                  return;
               default:
                  throw new UnsupportedOperationException("Unsupported slot type: " + cosmetic.slot());
            }
         }

         protected void m_87963_(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            if (this.m_93696_()) {
               this.modelPreviewer.setBackgroundClearColor(new Vec4f(0.5552F, 0.5552F, 0.5552F, 0.8F));
            } else {
               this.modelPreviewer.setBackgroundClearColor(new Vec4f(0.3552F, 0.3552F, 0.3552F, 0.3F));
            }

            RenderSystem.enableBlend();
            this.modelPreviewer.m_88315_(guiGraphics, pMouseX, pMouseY, pPartialTick);
            if (!this.cosmetic.unlocked()) {
               guiGraphics.m_280509_(
                  this.modelPreviewer.m_252754_(),
                  this.modelPreviewer.m_252907_(),
                  this.modelPreviewer.m_252754_() + this.modelPreviewer.m_5711_(),
                  this.modelPreviewer.m_252907_() + this.modelPreviewer.m_93694_(),
                  -1156509423
               );
               guiGraphics.m_280411_(
                  LOCK_ICON,
                  this.modelPreviewer.m_252754_() + this.modelPreviewer.m_5711_() / 2 - 8,
                  this.modelPreviewer.m_252907_() + this.modelPreviewer.m_93694_() / 2 - 8,
                  16,
                  16,
                  0.0F,
                  0.0F,
                  16,
                  16,
                  16,
                  16
               );
            }

            RenderSystem.disableBlend();
            guiGraphics.m_280137_(
               CapeList.this.f_93386_.f_91062_, this.cosmetic.title(), this.m_252754_() + this.f_93618_ / 2, this.m_252907_() + this.f_93619_ - 12, -1
            );
         }

         @Override
         public void onPress(boolean pressedTwice) {
            if (!pressedTwice) {
               if (this.modelPreviewer.getCloakMesh() != null) {
                  boolean canConfigureColor = !AvatarEditScreen.this.useVanillTextureCheckBox._getValue() && this.cosmetic.useIntParam1();
                  if (canConfigureColor) {
                     AvatarEditScreen.this.mainModelPreviewer.setCloakColor(AvatarEditScreen.this.brightnessSlider.getColor());
                  } else {
                     AvatarEditScreen.this.mainModelPreviewer.setCloakColor(-1);
                  }

                  boolean useVanillaTexture = AvatarEditScreen.this.useVanillTextureCheckBox._getValue() && this.cosmetic.useBoolParam1();
                  AvatarEditScreen.this.mainModelPreviewer
                     .initCloakInfo(
                        this.modelPreviewer.getCloakMesh(),
                        useVanillaTexture ? AuthenticationHelperImpl.getInstance().playerInfo().m_105338_() : this.modelPreviewer.getCloakTexture(),
                        ClothSimulator.ClothObjectBuilder.create().parentJoint(Armatures.BIPED.get().torso).putAll(ClothColliderPresets.BIPED)
                     );
               } else {
                  AvatarEditScreen.this.mainModelPreviewer.removeCloak();
               }
            }

            if (pressedTwice && this.cosmetic.unlocked()) {
               AvatarEditScreen.this.capePopupBox._setValue(this.cosmetic);
               AvatarEditScreen.this.refreshOptionComponents(this.cosmetic);
               AvatarEditScreen.this.capeSelectionLayout.accept(x$0 -> AvatarEditScreen.this.m_169411_((GuiEventListener)x$0));
               AvatarEditScreen.this.capeOptionsLayout.accept(x$0 -> AvatarEditScreen.this.m_142416_((GuiEventListener)x$0));
               AvatarEditScreen.this.activatedLayout = AvatarEditScreen.this.capeOptionsLayout;
            }
         }

         @Override
         public void destroy() {
            this.modelPreviewer.onDestroy();
         }

         @Override
         public Cosmetic getCosmetic() {
            return this.cosmetic;
         }

         protected void m_168797_(NarrationElementOutput pNarrationElementOutput) {
         }

         public void m_252865_(int x) {
            super.m_252865_(x);
            this.modelPreviewer.m_252865_(x);
         }

         public void m_253211_(int y) {
            super.m_253211_(y);
            this.modelPreviewer.m_253211_(y);
         }
      }

      @OnlyIn(Dist.CLIENT)
      public abstract class CosmeticBlock extends AbstractWidget {
         public CosmeticBlock(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
            super(pX, pY, pWidth, pHeight, pMessage);
         }

         public abstract void onPress(boolean var1);

         public abstract Cosmetic getCosmetic();

         public abstract void destroy();
      }

      @OnlyIn(Dist.CLIENT)
      public class CosmeticsEntry extends Entry<AvatarEditScreen.CapeList.CosmeticsEntry> {
         private final List<AvatarEditScreen.CapeList.CosmeticBlock> cosmeticsBlock;

         public CosmeticsEntry(List<AvatarEditScreen.CapeList.CosmeticBlock> cosmeticBlocks) {
            this.cosmeticsBlock = cosmeticBlocks;
         }

         public List<? extends GuiEventListener> m_6702_() {
            return this.cosmeticsBlock;
         }

         public List<? extends NarratableEntry> m_142437_() {
            return this.cosmeticsBlock;
         }

         public void m_6311_(
            GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick
         ) {
            for (AvatarEditScreen.CapeList.CosmeticBlock cosmeticBlock : this.cosmeticsBlock) {
               cosmeticBlock.m_253211_(pTop);
               cosmeticBlock.m_88315_(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LayoutElements<T extends GuiEventListener & Renderable & NarratableEntry> {
      private List<T> components;

      @SafeVarargs
      private LayoutElements(T... components) {
         this.components = Arrays.asList(components);
      }

      public void accept(Consumer<T> consumer) {
         this.components.forEach(consumer);
      }
   }
}
