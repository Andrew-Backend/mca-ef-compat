package forge.net.mca.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.MCAClient;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.client.gui.widget.ColorPickerWidget;
import forge.net.mca.client.gui.widget.GeneSliderWidget;
import forge.net.mca.client.gui.widget.HorizontalColorPickerWidget;
import forge.net.mca.client.gui.widget.HorizontalGradientWidget;
import forge.net.mca.client.gui.widget.NamedTextFieldWidget;
import forge.net.mca.client.gui.widget.TooltipButtonWidget;
import forge.net.mca.client.resources.ClientUtils;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.Personality;
import forge.net.mca.network.c2s.GetVillagerRequest;
import forge.net.mca.network.c2s.SkinListRequest;
import forge.net.mca.network.c2s.VillagerEditorSyncRequest;
import forge.net.mca.network.c2s.VillagerNameRequest;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.resources.data.skin.Hair;
import forge.net.mca.resources.data.skin.SkinListEntry;
import forge.net.mca.util.compat.ButtonWidget;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;

public class VillagerEditorScreen extends Screen implements SkinListUpdateListener {
   final UUID villagerUUID;
   final UUID playerUUID;
   final boolean allowPlayerModel;
   final boolean allowVillagerModel;
   private int villagerBreedingAge;
   protected String page;
   protected final VillagerEntityMCA villager = Objects.requireNonNull(
      (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(Minecraft.m_91087_().f_91073_)
   );
   protected final VillagerEntityMCA villagerVisualization = Objects.requireNonNull(
      (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(Minecraft.m_91087_().f_91073_)
   );
   protected static final int DATA_WIDTH = 175;
   private int traitPage = 0;
   private static final int TRAITS_PER_PAGE = 8;
   protected CompoundTag villagerData;
   private EditBox villagerNameField;
   private boolean hsvColoredHair;
   private final ColorSelector color = new ColorSelector();
   private int clothingPage;
   private int clothingPageCount;
   private ButtonWidget pageButtonWidget;
   private List<String> filteredClothing = new LinkedList<>();
   private List<String> filteredHair = new LinkedList<>();
   private static boolean isSkinListOutdated = true;
   private static HashMap<String, Clothing> clothing = new HashMap<>();
   private static HashMap<String, Hair> hair = new HashMap<>();
   private Gender filterGender = Gender.NEUTRAL;
   private String searchString = "";
   private int hoveredClothingId;
   final int CLOTHES_H = 8;
   final int CLOTHES_V = 2;
   final int CLOTHES_PER_PAGE = 17;
   ButtonWidget widgetMasculine;
   ButtonWidget widgetFeminine;
   private ButtonWidget villagerSkinWidget;
   private ButtonWidget playerSkinWidget;
   private ButtonWidget vanillaSkinWidget;
   private ButtonWidget doneWidget;
   private ButtonWidget genderButtonFemale;
   private ButtonWidget genderButtonMale;

   public VillagerEditorScreen(UUID villagerUUID, UUID playerUUID, boolean allowPlayerModel, boolean allowVillagerModel) {
      super(Component.m_237115_("gui.VillagerEditorScreen.title"));
      this.villagerUUID = villagerUUID;
      this.playerUUID = playerUUID;
      this.allowPlayerModel = allowPlayerModel;
      this.allowVillagerModel = allowVillagerModel;
      this.requestVillagerData();
      this.setPage(Objects.requireNonNullElse(this.page, "loading"));
   }

   public VillagerEditorScreen(UUID villagerUUID, UUID playerUUID) {
      this(villagerUUID, playerUUID, MCAClient.isPlayerRendererAllowed(), MCAClient.isVillagerRendererAllowed());
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_7856_() {
      this.setPage(this.page);
   }

   private int doubleGeneSliders(int y, Genetics.GeneType... genes) {
      boolean right = false;
      Genetics genetics = this.villager.getGenetics();

      for (Genetics.GeneType g : genes) {
         this.m_142416_(
            new GeneSliderWidget(
               this.f_96543_ / 2 + (right ? 87 : 0),
               y,
               87,
               20,
               Component.m_237115_(g.getTranslationKey()),
               genetics.getGene(g),
               b -> genetics.setGene(g, b.floatValue())
            )
         );
         if (right) {
            y += 20;
         }

         right = !right;
      }

      return y + 4 + (right ? 20 : 0);
   }

   private int integerChanger(int y, IntConsumer onClick, Supplier<Component> content) {
      int bw = 22;
      ButtonWidget current = (ButtonWidget)this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + bw * 2, y, 175 - bw * 4, 20, content.get(), b -> {}));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, bw, 20, Component.m_237113_("-5"), b -> {
         onClick.accept(-5);
         current.m_93666_(content.get());
      }));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + bw, y, bw, 20, Component.m_237113_("-50"), b -> {
         onClick.accept(-50);
         current.m_93666_(content.get());
      }));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 175 - bw * 2, y, bw, 20, Component.m_237113_("+50"), b -> {
         onClick.accept(50);
         current.m_93666_(content.get());
      }));
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 175 - bw, y, bw, 20, Component.m_237113_("+5"), b -> {
         onClick.accept(5);
         current.m_93666_(content.get());
      }));
      return y + 22;
   }

   protected void setPage(String page) {
      this.page = page;
      this.m_169413_();
      if (!page.equals("loading")) {
         if (this.shouldShowPageSelection()) {
            String[] pages = this.getPages();
            int w = 350 / pages.length;
            int x = (int)(this.f_96543_ / 2.0 - pages.length / 2.0 * w);

            for (String p : pages) {
               ((ButtonWidget)this.m_142416_(
                     new ButtonWidget(x, this.f_96544_ / 2 - 105, w, 20, Component.m_237115_("gui.villager_editor.page." + p), sender -> this.setPage(p))
                  ))
                  .f_93623_ = !p.equals(page);
               x += w;
            }

            this.doneWidget = (ButtonWidget)this.m_142416_(
               new ButtonWidget(this.f_96543_ / 2 - 175 + 20, this.f_96544_ / 2 + 85, 135, 20, Component.m_237115_("gui.done"), sender -> {
                  this.syncVillagerData();
                  this.m_7379_();
               })
            );
         }

         int y = this.f_96544_ / 2 - 80;
         int margin = 40;
         Genetics genetics = this.villager.getGenetics();
         switch (page) {
            case "general": {
               this.drawName(this.f_96543_ / 2, y);
               y += 20;
               this.drawGender(this.f_96543_ / 2, y);
               y += 22;
               if (this.villagerUUID.equals(this.playerUUID)) {
                  this.addModelSelectionWidgets(this.f_96543_ / 2, y);
                  y += 22;
               }

               if (!this.villagerUUID.equals(this.playerUUID)) {
                  this.m_142416_(
                     new GeneSliderWidget(
                        this.f_96543_ / 2,
                        y,
                        175,
                        20,
                        Component.m_237115_("gui.villager_editor.age"),
                        1.0 + (double)this.villagerBreedingAge / AgeState.getMaxAge(),
                        b -> {
                           this.villagerBreedingAge = -((int)((1.0 - b) * AgeState.getMaxAge())) + 1;
                           this.villager.m_146762_(this.villagerBreedingAge);
                           this.villager.m_6210_();
                        }
                     )
                  );
                  y += 28;
               }

               for (String who : new String[]{"father", "mother", "spouse"}) {
                  EditBox textFieldWidgetx = (EditBox)this.m_142416_(
                     new NamedTextFieldWidget(this.f_96547_, this.f_96543_ / 2, y, 175, 18, Component.m_237115_("gui.villager_editor.relation." + who))
                  );
                  textFieldWidgetx.m_94199_(64);
                  textFieldWidgetx.m_94144_(this.villagerData.m_128461_("tree_" + who + "_name"));
                  textFieldWidgetx.m_94151_(name -> this.villagerData.m_128359_("tree_" + who + "_new", name));
                  y += 20;
               }

               y += 4;
               EditBox textFieldWidget = (EditBox)this.m_142416_(new EditBox(this.f_96547_, this.f_96543_ / 2, y, 175, 18, Component.m_237113_("UUID")));
               textFieldWidget.m_94199_(64);
               textFieldWidget.m_94144_(this.villagerUUID.toString());
               break;
            }
            case "body":
               if (!Config.getServerConfig().allowPlayerSizeAdjustment && this.villagerUUID.equals(this.playerUUID)) {
                  y = this.doubleGeneSliders(y, Genetics.BREAST, Genetics.SKIN);
                  genetics.setGene(Genetics.SIZE, 0.8F);
                  genetics.setGene(Genetics.WIDTH, 0.8F);
               } else {
                  y = this.doubleGeneSliders(y, Genetics.SIZE, Genetics.WIDTH, Genetics.BREAST, Genetics.SKIN);
               }

               this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2, y, 87, 20, Component.m_237115_("gui.villager_editor.randClothing"), b -> this.sendCommand("clothing"))
               );
               this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 + 87, y, 87, 20, Component.m_237115_("gui.villager_editor.selectClothing"), b -> this.setPage("clothing"))
               );
               y += 22;
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, 87, 20, Component.m_237115_("gui.villager_editor.prev"), b -> {
                  CompoundTag compound = new CompoundTag();
                  compound.m_128405_("offset", -1);
                  this.sendCommand("clothing", compound);
               }));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 87, y, 87, 20, Component.m_237115_("gui.villager_editor.next"), b -> {
                  CompoundTag compound = new CompoundTag();
                  compound.m_128405_("offset", 1);
                  this.sendCommand("clothing", compound);
               }));
               y += 22;
               this.m_142416_(
                  new ColorPickerWidget(
                     this.f_96543_ / 2 + margin,
                     y,
                     175 - margin * 2,
                     175 - margin * 2,
                     genetics.getGene(Genetics.HEMOGLOBIN),
                     genetics.getGene(Genetics.MELANIN),
                     MCA.locate("textures/colormap/villager_skin.png"),
                     (vx, vy) -> {
                        genetics.setGene(Genetics.HEMOGLOBIN, vx.floatValue());
                        genetics.setGene(Genetics.MELANIN, vy.floatValue());
                     }
                  )
               );
               break;
            case "head":
               this.m_142416_(
                  new TooltipButtonWidget(
                     this.f_96543_ / 2 + 87,
                     y,
                     87,
                     20,
                     Component.m_237115_(this.hsvColoredHair ? "gui.villager_editor.hair_hsv" : "gui.villager_editor.hair_genetic"),
                     Component.m_237115_("gui.villager_editor.hair_mode.tooltip"),
                     b -> {
                        this.hsvColoredHair = !this.hsvColoredHair;
                        this.m_7856_();
                     }
                  )
               );
               y = this.doubleGeneSliders(y, Genetics.FACE);
               y = this.doubleGeneSliders(y, Genetics.VOICE_TONE, Genetics.VOICE);
               this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2, y, 87, 20, Component.m_237115_("gui.villager_editor.randHair"), b -> this.sendCommand("hair"))
               );
               this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 + 87, y, 87, 20, Component.m_237115_("gui.villager_editor.selectHair"), b -> this.setPage("hair"))
               );
               y += 22;
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, 87, 20, Component.m_237115_("gui.villager_editor.prev"), b -> {
                  CompoundTag compound = new CompoundTag();
                  compound.m_128405_("offset", -1);
                  this.sendCommand("hair", compound);
               }));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 87, y, 87, 20, Component.m_237115_("gui.villager_editor.next"), b -> {
                  CompoundTag compound = new CompoundTag();
                  compound.m_128405_("offset", 1);
                  this.sendCommand("hair", compound);
               }));
               y += 22;
               if (this.hsvColoredHair) {
                  this.color.hueWidget = (HorizontalColorPickerWidget)this.m_142416_(
                     new HorizontalColorPickerWidget(
                        this.f_96543_ / 2 + 20, y, 135, 15, this.color.hue / 360.0, MCA.locate("textures/colormap/hue.png"), (vx, vy) -> {
                           this.color.setHSV(vx * 360.0, this.color.saturation, this.color.brightness);
                           this.refreshHairColor();
                        }
                     )
                  );
                  this.color.saturationWidget = (HorizontalColorPickerWidget)this.m_142416_(
                     new HorizontalGradientWidget(this.f_96543_ / 2 + 20, y + 20, 135, 15, this.color.saturation, () -> {
                        double[] doubles = ClientUtils.HSV2RGB(this.color.hue, 0.0, 1.0);
                        return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                     }, () -> {
                        double[] doubles = ClientUtils.HSV2RGB(this.color.hue, 1.0, 1.0);
                        return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                     }, (vx, vy) -> {
                        this.color.setHSV(this.color.hue, vx, this.color.brightness);
                        this.refreshHairColor();
                     })
                  );
                  this.color.brightnessWidget = (HorizontalColorPickerWidget)this.m_142416_(
                     new HorizontalGradientWidget(this.f_96543_ / 2 + 20, y + 40, 135, 15, this.color.brightness, () -> {
                        double[] doubles = ClientUtils.HSV2RGB(this.color.hue, this.color.saturation, 0.0);
                        return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                     }, () -> {
                        double[] doubles = ClientUtils.HSV2RGB(this.color.hue, this.color.saturation, 1.0);
                        return new float[]{(float)doubles[0], (float)doubles[1], (float)doubles[2], 1.0F};
                     }, (vx, vy) -> {
                        this.color.setHSV(this.color.hue, this.color.saturation, vx);
                        this.refreshHairColor();
                     })
                  );
                  y += 65;
                  this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, 175, 20, Component.m_237115_("gui.villager_editor.clear_hair"), b -> {
                     this.villager.clearHairDye();
                     this.m_7856_();
                  }));
               } else {
                  this.m_142416_(
                     new ColorPickerWidget(
                        this.f_96543_ / 2 + margin,
                        y,
                        175 - margin * 2,
                        175 - margin * 2,
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
               break;
            case "personality":
               List<ButtonWidget> personalityButtons = new LinkedList<>();
               int row = 0;
               int BUTTONS_PER_ROW = 2;

               for (Personality p : Personality.values()) {
                  if (p != Personality.UNASSIGNED) {
                     if (row == 2) {
                        row = 0;
                        y += 19;
                     }

                     ButtonWidget widget = (ButtonWidget)this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 87 * row, y, 87, 20, p.getName(), b -> {
                        this.villager.getVillagerBrain().setPersonality(p);
                        personalityButtons.forEach(v -> v.f_93623_ = true);
                        b.f_93623_ = false;
                     }));
                     widget.f_93623_ = p != this.villager.getVillagerBrain().getPersonality();
                     personalityButtons.add(widget);
                     row++;
                  }
               }
               break;
            case "traits":
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, 32, 20, Component.m_237113_("<"), b -> this.setTraitPage(this.traitPage - 1)));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 175 - 32, y, 32, 20, Component.m_237113_(">"), b -> this.setTraitPage(this.traitPage + 1)));
               this.m_142416_(
                  new ButtonWidget(
                     this.f_96543_ / 2 + 32,
                     y,
                     111,
                     20,
                     Component.m_237110_("gui.villager_editor.page", new Object[]{this.traitPage + 1}),
                     b -> this.traitPage++
                  )
               );
               y += 22;
               Traits.Trait[] traits = this.getValidTraits();

               for (int i = 0; i < 8; i++) {
                  int index = i + this.traitPage * 8;
                  if (index >= traits.length) {
                     return;
                  }

                  Traits.Trait t = traits[index];
                  MutableComponent name = t.getName().m_6881_().m_130940_(this.villager.getTraits().hasTrait(t) ? ChatFormatting.GREEN : ChatFormatting.GRAY);
                  this.m_142416_(new ButtonWidget(this.f_96543_ / 2, y, 175, 20, name, b -> {
                     if (this.villager.getTraits().hasTrait(t)) {
                        this.villager.getTraits().removeTrait(t);
                     } else {
                        this.villager.getTraits().addTrait(t);
                     }

                     b.m_93666_(t.getName().m_6881_().m_130940_(this.villager.getTraits().hasTrait(t) ? ChatFormatting.GREEN : ChatFormatting.GRAY));
                  }));
                  y += 20;
               }
               break;
            case "debug":
               boolean right = false;
               List<ButtonWidget> professionButtons = new LinkedList<>();

               for (VillagerProfession p : new VillagerProfession[]{
                  VillagerProfession.f_35585_,
                  (VillagerProfession)ProfessionsMCA.GUARD.get(),
                  (VillagerProfession)ProfessionsMCA.ARCHER.get(),
                  (VillagerProfession)ProfessionsMCA.OUTLAW.get(),
                  (VillagerProfession)ProfessionsMCA.ADVENTURER.get(),
                  (VillagerProfession)ProfessionsMCA.CULTIST.get()
               }) {
                  MutableComponent text = Component.m_237115_("entity.minecraft.villager." + p);
                  ButtonWidget widget = (ButtonWidget)this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + (right ? 87 : 0), y, 87, 20, text, b -> {
                     CompoundTag compound = new CompoundTag();
                     compound.m_128359_("profession", BuiltInRegistries.f_256735_.m_7981_(p).toString());
                     this.syncVillagerData();
                     NetworkHandler.sendToServer(new VillagerEditorSyncRequest("profession", this.villagerUUID, compound));
                     this.requestVillagerData();
                     professionButtons.forEach(button -> button.f_93623_ = true);
                     b.f_93623_ = false;
                  }));
                  professionButtons.add(widget);
                  widget.f_93623_ = this.villager.getProfession() != p;
                  if (right) {
                     y += 20;
                  }

                  right = !right;
               }

               y += 4;
               this.m_142416_(
                  new GeneSliderWidget(
                     this.f_96543_ / 2, y, 175, 20, Component.m_237115_("gui.villager_editor.infection"), this.villager.getInfectionProgress(), b -> {
                        this.villager.setInfected(b > 0.0);
                        this.villager.setInfectionProgress(b.floatValue());
                     }
                  )
               );
               y += 22;
               assert this.f_96541_ != null;
               assert this.f_96541_.f_91074_ != null;
               Memories player = this.villager.getVillagerBrain().getMemoriesForPlayer(this.f_96541_.f_91074_);
               y = this.integerChanger(y, player::modHearts, () -> Component.m_237110_("gui.blueprint.reputation", new Object[]{player.getHearts()}));
               this.integerChanger(
                  y,
                  v -> this.villager.getVillagerBrain().modifyMoodValue(v),
                  () -> Component.m_237110_("gui.interact.label.mood", new Object[]{this.villager.getVillagerBrain().getMoodValue()})
               );
               break;
            case "clothing":
            case "hair": {
               this.filterGender = this.villager.getGenetics().getGender();
               this.searchString = "";
               EditBox textFieldWidget = (EditBox)this.m_142416_(
                  new EditBox(this.f_96547_, this.f_96543_ / 2 - 87, this.f_96544_ / 2 - 100, 175, 18, Component.m_237115_("gui.villager_editor.search"))
               );
               textFieldWidget.m_94199_(64);
               textFieldWidget.m_94151_(v -> {
                  this.searchString = v;
                  this.filter();
               });
               y = this.f_96544_ / 2 + 85;
               this.pageButtonWidget = (ButtonWidget)this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 30, y, 60, 20, Component.m_237113_(""), b -> {}));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 32 - 28, y, 28, 20, Component.m_237113_("<<"), b -> {
                  this.clothingPage = Math.max(0, this.clothingPage - 1);
                  this.updateClothingPageWidget();
               }));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 32, y, 28, 20, Component.m_237113_(">>"), b -> {
                  this.clothingPage = Math.max(0, Math.min(this.clothingPageCount - 1, this.clothingPage + 1));
                  this.updateClothingPageWidget();
               }));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 32 + 32, y, 64, 20, Component.m_237115_("gui.button.done"), b -> {
                  if (page.equals("clothing")) {
                     this.setPage("body");
                  } else {
                     this.setPage("head");
                  }
               }));
               this.m_142416_(
                  new ButtonWidget(
                     this.f_96543_ / 2 + 128,
                     y,
                     64,
                     20,
                     Component.m_237115_("gui.button.library"),
                     b -> Minecraft.m_91087_().m_91152_(new SkinLibraryScreen(this, this.villagerVisualization))
                  )
               );
               this.widgetMasculine = (ButtonWidget)this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 - 32 - 96 - 64, y, 64, 20, Component.m_237115_("gui.villager_editor.masculine"), b -> {
                     this.filterGender = Gender.MALE;
                     this.filter();
                     this.widgetMasculine.f_93623_ = false;
                     this.widgetFeminine.f_93623_ = true;
                  })
               );
               this.widgetMasculine.f_93623_ = this.filterGender != Gender.MALE;
               this.widgetFeminine = (ButtonWidget)this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 - 32 - 96 - 64 + 64, y, 64, 20, Component.m_237115_("gui.villager_editor.feminine"), b -> {
                     this.filterGender = Gender.FEMALE;
                     this.filter();
                     this.widgetMasculine.f_93623_ = true;
                     this.widgetFeminine.f_93623_ = false;
                  })
               );
               this.widgetFeminine.f_93623_ = this.filterGender != Gender.FEMALE;
               this.filter();
            }
         }
      }
   }

   private void refreshHairColor() {
      if (this.villager.getHairDye()[0] == 0.0F) {
         this.color.setHSV(0.0, 0.5, 0.5);
      }

      this.villager
         .setHairDye(
            Math.max(0.003921569F, (float)this.color.red), Math.max(0.003921569F, (float)this.color.green), Math.max(0.003921569F, (float)this.color.blue)
         );
   }

   private Traits.Trait[] getValidTraits() {
      return Traits.Trait.values()
         .stream()
         .filter(
            e -> !this.villagerUUID.equals(this.playerUUID)
               ? e.isEnabled()
               : (Config.getInstance().bypassTraitRestrictions || e.isUsableOnPlayer()) && e.isEnabled()
         )
         .toList()
         .toArray(Traits.Trait[]::new);
   }

   private void updateClothingPageWidget() {
      if (this.pageButtonWidget != null) {
         this.pageButtonWidget.m_93666_(Component.m_237113_(String.format("%d / %d", this.clothingPage + 1, this.clothingPageCount)));
      }
   }

   private void filter() {
      if (Objects.equals(this.page, "clothing")) {
         this.filteredClothing = this.filter(getClothing());
      } else {
         this.filteredHair = this.filter(getHair());
      }
   }

   private <T extends SkinListEntry> List<String> filter(HashMap<String, T> map) {
      List<String> filtered = map.entrySet()
         .stream()
         .filter(v -> this.filterGender == v.getValue().getGender() || v.getValue().getGender() == Gender.NEUTRAL)
         .filter(v -> v.getValue() instanceof Clothing c ? !c.exclude : true)
         .filter(v -> MCA.isBlankString(this.searchString) || v.getKey().contains(this.searchString))
         .map(Entry::getKey)
         .toList();
      this.clothingPageCount = (int)Math.ceil(filtered.size() / 17.0F);
      this.clothingPage = Math.max(0, Math.min(this.clothingPage, this.clothingPageCount - 1));
      this.updateClothingPageWidget();
      return filtered;
   }

   protected String[] getPages() {
      return this.villagerUUID.equals(this.playerUUID)
         ? new String[]{"general", "body", "head", "traits"}
         : new String[]{"general", "body", "head", "personality", "traits", "debug"};
   }

   protected void drawName(int x, int y) {
      this.drawName(x, y, name -> {
         this.updateName(name);
         if (this.doneWidget != null) {
            this.doneWidget.f_93623_ = !MCA.isBlankString(name);
         }
      });
   }

   protected void drawName(int x, int y, Consumer<String> onChanged) {
      this.villagerNameField = (EditBox)this.m_142416_(new EditBox(this.f_96547_, x, y, 116, 18, Component.m_237115_("structure_block.structure_name")));
      this.villagerNameField.m_94199_(32);
      this.villagerNameField.m_94144_(this.getName().getString());
      this.villagerNameField.m_94151_(onChanged);
      this.m_142416_(
         new ButtonWidget(
            x + 116 + 1,
            y - 1,
            56,
            20,
            Component.m_237115_("gui.button.random"),
            b -> NetworkHandler.sendToServer(new VillagerNameRequest(this.villager.getGenetics().getGender()))
         )
      );
   }

   public Component getName() {
      Component villagerName = null;
      boolean isPlayer = this.villagerUUID.equals(this.playerUUID);
      if (isPlayer) {
         assert this.f_96541_ != null;
         assert this.f_96541_.f_91074_ != null;
         villagerName = this.f_96541_.f_91074_.m_7770_();
      } else if (this.villager.m_8077_()) {
         villagerName = this.villager.m_7770_();
      }

      if (villagerName == null || MCA.isBlankString(villagerName.getString())) {
         if (isPlayer) {
            assert this.f_96541_ != null;
            assert this.f_96541_.f_91074_ != null;
            villagerName = this.f_96541_.f_91074_.m_7755_();
         } else {
            villagerName = this.villager.m_7755_();
         }

         if (villagerName != null && !MCA.isBlankString(villagerName.getString())) {
            this.updateName(villagerName.getString());
         } else {
            NetworkHandler.sendToServer(new VillagerNameRequest(this.villager.getGenetics().getGender()));
         }
      }

      return villagerName;
   }

   public void updateName(String name) {
      if (!MCA.isBlankString(name)) {
         Component newName = Component.m_130674_(name);
         boolean isPlayer = this.villagerUUID.equals(this.playerUUID);
         if (isPlayer) {
            assert this.f_96541_ != null;
            assert this.f_96541_.f_91074_ != null;
            Component realName = this.f_96541_.f_91074_.m_7755_();
            if (realName.getString().equals(name)) {
               newName = null;
            }

            this.f_96541_.f_91074_.m_6593_(newName);
            this.f_96541_.f_91074_.m_20340_(newName != null);
            if (this.f_96541_.f_91074_.m_20151_()) {
               this.villager.m_6593_(newName);
            } else {
               this.villager.setName(realName.getString());
            }
         } else {
            this.villager.m_6593_(newName);
         }
      }
   }

   void drawGender(int x, int y) {
      this.genderButtonFemale = new ButtonWidget(x, y, 87, 20, Component.m_237115_("gui.villager_editor.feminine"), sender -> {
         this.villager.getGenetics().setGender(Gender.FEMALE);
         this.sendCommand("gender");
         this.genderButtonFemale.f_93623_ = false;
         this.genderButtonMale.f_93623_ = true;
      });
      this.m_142416_(this.genderButtonFemale);
      this.genderButtonMale = new ButtonWidget(x + 87, y, 87, 20, Component.m_237115_("gui.villager_editor.masculine"), sender -> {
         this.villager.getGenetics().setGender(Gender.MALE);
         this.sendCommand("gender");
         this.genderButtonFemale.f_93623_ = true;
         this.genderButtonMale.f_93623_ = false;
      });
      this.m_142416_(this.genderButtonMale);
      this.genderButtonFemale.f_93623_ = this.villager.getGenetics().getGender() != Gender.FEMALE;
      this.genderButtonMale.f_93623_ = this.villager.getGenetics().getGender() != Gender.MALE;
   }

   void addModelSelectionWidgets(int x, int y) {
      if (this.allowPlayerModel && this.allowVillagerModel) {
         this.villagerSkinWidget = (ButtonWidget)this.m_142416_(new TooltipButtonWidget(x, y, 58, 20, "gui.villager_editor.villager_skin", b -> {
            this.villagerData.m_128405_("playerModel", VillagerLike.PlayerModel.VILLAGER.ordinal());
            this.syncVillagerData();
            this.playerSkinWidget.f_93623_ = true;
            this.villagerSkinWidget.f_93623_ = false;
            this.vanillaSkinWidget.f_93623_ = true;
         }));
         this.villagerSkinWidget.f_93623_ = this.villagerData.m_128451_("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal();
         this.playerSkinWidget = (ButtonWidget)this.m_142416_(new TooltipButtonWidget(x + 58, y, 58, 20, "gui.villager_editor.player_skin", b -> {
            this.villagerData.m_128405_("playerModel", VillagerLike.PlayerModel.PLAYER.ordinal());
            this.syncVillagerData();
            this.playerSkinWidget.f_93623_ = false;
            this.villagerSkinWidget.f_93623_ = true;
            this.vanillaSkinWidget.f_93623_ = true;
         }));
         this.playerSkinWidget.f_93623_ = this.villagerData.m_128451_("playerModel") != VillagerLike.PlayerModel.PLAYER.ordinal();
         this.vanillaSkinWidget = (ButtonWidget)this.m_142416_(new TooltipButtonWidget(x + 116, y, 58, 20, "gui.villager_editor.vanilla_skin", b -> {
            this.villagerData.m_128405_("playerModel", VillagerLike.PlayerModel.VANILLA.ordinal());
            this.syncVillagerData();
            this.villagerSkinWidget.f_93623_ = true;
            this.playerSkinWidget.f_93623_ = true;
            this.vanillaSkinWidget.f_93623_ = false;
         }));
         this.vanillaSkinWidget.f_93623_ = this.villagerData.m_128451_("playerModel") != VillagerLike.PlayerModel.VANILLA.ordinal();
      } else {
         ((TooltipButtonWidget)this.m_142416_(new TooltipButtonWidget(x, y, 175, 20, "gui.villager_editor.model_blacklist_hint", b -> {}))).f_93623_ = false;
      }
   }

   private void sendCommand(String command) {
      this.sendCommand(command, new CompoundTag());
   }

   private void sendCommand(String command, CompoundTag nbt) {
      this.syncVillagerData();
      NetworkHandler.sendToServer(new VillagerEditorSyncRequest(command, this.villagerUUID, nbt));
      this.requestVillagerData();
   }

   private void setTraitPage(int i) {
      Traits.Trait[] traits = this.getValidTraits();
      int maxPage = (int)Math.ceil(traits.length / 8.0) - 1;
      this.traitPage = Math.max(0, Math.min(maxPage, i));
      this.setPage("traits");
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.page.equals("clothing") && this.hoveredClothingId >= 0 && this.filteredClothing.size() > this.hoveredClothingId) {
         this.villager.setClothes(this.filteredClothing.get(this.hoveredClothingId));
         this.setPage("body");
         this.eventCallback("clothing");
         return true;
      } else if (this.page.equals("hair") && this.hoveredClothingId >= 0 && this.filteredHair.size() > this.hoveredClothingId) {
         this.villager.setHair(this.filteredHair.get(this.hoveredClothingId));
         this.setPage("head");
         this.eventCallback("hair");
         return true;
      } else {
         return super.m_6375_(mouseX, mouseY, button);
      }
   }

   protected void eventCallback(String event) {
   }

   protected boolean shouldUsePlayerModel() {
      return false;
   }

   protected boolean shouldPrintPlayerHint() {
      return true;
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.m_280273_(context);
      context.m_280509_(0, 20, this.f_96543_, this.f_96544_ - 20, 1711276032);
      if (this.villager != null) {
         this.villager.f_19797_ = (int)(System.currentTimeMillis() / 50L);
         if (this.shouldDrawEntity()) {
            int x = this.f_96543_ / 2 - 87;
            int y = this.f_96544_ / 2 + 70;
            if (this.villagerUUID.equals(this.playerUUID) && this.shouldUsePlayerModel()) {
               assert Minecraft.m_91087_().f_91074_ != null;
               InventoryScreen.m_274545_(context, x, y, 60, x - mouseX, y - 50 - mouseY, Minecraft.m_91087_().f_91074_);
            } else {
               InventoryScreen.m_274545_(context, x, y, 60, x - mouseX, y - 50 - mouseY, this.villager);
            }

            if (this.shouldPrintPlayerHint()
               && this.villagerUUID.equals(this.playerUUID)
               && this.villagerData.m_128451_("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal()) {
               PoseStack matrices = context.m_280168_();
               matrices.m_85836_();
               matrices.m_252880_(x, y - 145, 0.0F);
               matrices.m_85841_(0.5F, 0.5F, 0.5F);
               context.m_280653_(this.f_96547_, Component.m_237115_("gui.villager_editor.model_hint"), 0, 0, -1426063361);
               matrices.m_85849_();
            }
         }

         if (this.page.equals("clothing") || this.page.equals("hair")) {
            CompoundTag nbt = new CompoundTag();
            this.villager.m_7380_(nbt);
            this.villagerVisualization.m_7378_(nbt);
            this.villagerVisualization.m_146762_(this.villager.m_146764_());
            this.villagerVisualization.m_6210_();
            int i = 0;
            this.hoveredClothingId = -1;

            for (int y = 0; y < 2; y++) {
               for (int x = 0; x < 8 + y; x++) {
                  int index = this.clothingPage * 17 + i;
                  if ((this.page.equals("clothing") ? this.filteredClothing : this.filteredHair).size() <= index) {
                     break;
                  }

                  if (this.page.equals("clothing")) {
                     this.villagerVisualization.setClothes(this.filteredClothing.get(index));
                  } else {
                     this.villagerVisualization.setHair(this.filteredHair.get(index));
                  }

                  int cx = this.f_96543_ / 2 + (int)((x - 4.0 + 0.5 - 0.5 * (y % 2)) * 40.0);
                  int cy = this.f_96544_ / 2 + 25 + (int)((y - 1.0 + 0.5) * 65.0);
                  if (Math.abs(cx - mouseX) <= 20 && Math.abs(cy - mouseY - 30) <= 30) {
                     this.hoveredClothingId = index;
                  }

                  InventoryScreen.m_274545_(
                     context, cx, cy, this.hoveredClothingId == index ? 35 : 30, -(mouseX - cx) / 2.0F, -(mouseY - cy - 64) / 2.0F, this.villagerVisualization
                  );
                  i++;
               }
            }
         }

         super.m_88315_(context, mouseX, mouseY, delta);
      }
   }

   protected boolean shouldDrawEntity() {
      return !this.page.equals("loading") && !this.page.equals("clothing") && !this.page.equals("hair");
   }

   protected boolean shouldShowPageSelection() {
      return !this.page.equals("clothing") && !this.page.equals("hair");
   }

   public void setVillagerName(String name) {
      this.villagerNameField.m_94144_(name);
      this.updateName(name);
   }

   public void setVillagerData(CompoundTag villagerData) {
      if (this.villager != null) {
         this.villagerData = villagerData;
         this.villager.m_7378_(villagerData);
         float[] hairDye = this.villager.getHairDye();
         this.hsvColoredHair = hairDye[0] > 0.0F;
         this.color.setRGB(hairDye[0], hairDye[1], hairDye[2]);
         this.villagerBreedingAge = villagerData.m_128451_("Age");
         this.villager.m_146762_(this.villagerBreedingAge);
         if (this.f_96541_ != null && this.f_96541_.f_91074_ != null) {
            this.villager.m_20343_(this.f_96541_.f_91074_.m_20185_(), this.f_96541_.f_91074_.m_20186_(), this.f_96541_.f_91074_.m_20189_());
            this.villagerVisualization.m_20343_(this.f_96541_.f_91074_.m_20185_(), this.f_96541_.f_91074_.m_20186_(), this.f_96541_.f_91074_.m_20189_());
         }

         this.villager.m_6210_();
      }

      if (this.page.equals("loading")) {
         this.setPage("general");
      } else {
         this.setPage(this.page);
      }
   }

   private void requestVillagerData() {
      NetworkHandler.sendToServer(new GetVillagerRequest(this.villagerUUID));
   }

   public void syncVillagerData() {
      CompoundTag nbt = this.villagerData;
      this.villager.m_7380_(nbt);
      nbt.m_128405_("Age", this.villagerBreedingAge);
      NetworkHandler.sendToServer(new VillagerEditorSyncRequest("sync", this.villagerUUID, nbt));
   }

   public static void setSkinList(HashMap<String, Clothing> clothing, HashMap<String, Hair> hair) {
      VillagerEditorScreen.clothing = clothing;
      VillagerEditorScreen.hair = hair;
   }

   @Override
   public void skinListUpdatedCallback() {
      this.filter();
   }

   public static void sync() {
      if (isSkinListOutdated) {
         NetworkHandler.sendToServer(new SkinListRequest());
         isSkinListOutdated = false;
      }
   }

   public static HashMap<String, Clothing> getClothing() {
      sync();
      return clothing;
   }

   public static HashMap<String, Hair> getHair() {
      sync();
      return hair;
   }

   public static void setSkinListOutdated() {
      isSkinListOutdated = true;
   }

   public VillagerEntityMCA getVillager() {
      return this.villager;
   }
}
