package fabric.net.mca.client.gui;

import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.MCAClient;
import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.client.gui.widget.ColorPickerWidget;
import fabric.net.mca.client.gui.widget.GeneSliderWidget;
import fabric.net.mca.client.gui.widget.HorizontalColorPickerWidget;
import fabric.net.mca.client.gui.widget.HorizontalGradientWidget;
import fabric.net.mca.client.gui.widget.NamedTextFieldWidget;
import fabric.net.mca.client.gui.widget.TooltipButtonWidget;
import fabric.net.mca.client.resources.ClientUtils;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.Genetics;
import fabric.net.mca.entity.ai.Memories;
import fabric.net.mca.entity.ai.Traits;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.entity.ai.relationship.Personality;
import fabric.net.mca.network.c2s.GetVillagerRequest;
import fabric.net.mca.network.c2s.SkinListRequest;
import fabric.net.mca.network.c2s.VillagerEditorSyncRequest;
import fabric.net.mca.network.c2s.VillagerNameRequest;
import fabric.net.mca.resources.data.skin.Clothing;
import fabric.net.mca.resources.data.skin.Hair;
import fabric.net.mca.resources.data.skin.SkinListEntry;
import fabric.net.mca.util.compat.ButtonWidget;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.class_124;
import net.minecraft.class_1299;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3852;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_5250;
import net.minecraft.class_7923;

public class VillagerEditorScreen extends class_437 implements SkinListUpdateListener {
   final UUID villagerUUID;
   final UUID playerUUID;
   final boolean allowPlayerModel;
   final boolean allowVillagerModel;
   private int villagerBreedingAge;
   protected String page;
   protected final VillagerEntityMCA villager = Objects.requireNonNull(
      (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(class_310.method_1551().field_1687)
   );
   protected final VillagerEntityMCA villagerVisualization = Objects.requireNonNull(
      (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(class_310.method_1551().field_1687)
   );
   protected static final int DATA_WIDTH = 175;
   private int traitPage = 0;
   private static final int TRAITS_PER_PAGE = 8;
   protected class_2487 villagerData;
   private class_342 villagerNameField;
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
      super(class_2561.method_43471("gui.VillagerEditorScreen.title"));
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

   public boolean method_25421() {
      return false;
   }

   public void method_25426() {
      this.setPage(this.page);
   }

   private int doubleGeneSliders(int y, Genetics.GeneType... genes) {
      boolean right = false;
      Genetics genetics = this.villager.getGenetics();

      for (Genetics.GeneType g : genes) {
         this.method_37063(
            new GeneSliderWidget(
               this.field_22789 / 2 + (right ? 87 : 0),
               y,
               87,
               20,
               class_2561.method_43471(g.getTranslationKey()),
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

   private int integerChanger(int y, IntConsumer onClick, Supplier<class_2561> content) {
      int bw = 22;
      ButtonWidget current = (ButtonWidget)this.method_37063(new ButtonWidget(this.field_22789 / 2 + bw * 2, y, 175 - bw * 4, 20, content.get(), b -> {}));
      this.method_37063(new ButtonWidget(this.field_22789 / 2, y, bw, 20, class_2561.method_43470("-5"), b -> {
         onClick.accept(-5);
         current.method_25355(content.get());
      }));
      this.method_37063(new ButtonWidget(this.field_22789 / 2 + bw, y, bw, 20, class_2561.method_43470("-50"), b -> {
         onClick.accept(-50);
         current.method_25355(content.get());
      }));
      this.method_37063(new ButtonWidget(this.field_22789 / 2 + 175 - bw * 2, y, bw, 20, class_2561.method_43470("+50"), b -> {
         onClick.accept(50);
         current.method_25355(content.get());
      }));
      this.method_37063(new ButtonWidget(this.field_22789 / 2 + 175 - bw, y, bw, 20, class_2561.method_43470("+5"), b -> {
         onClick.accept(5);
         current.method_25355(content.get());
      }));
      return y + 22;
   }

   protected void setPage(String page) {
      this.page = page;
      this.method_37067();
      if (!page.equals("loading")) {
         if (this.shouldShowPageSelection()) {
            String[] pages = this.getPages();
            int w = 350 / pages.length;
            int x = (int)(this.field_22789 / 2.0 - pages.length / 2.0 * w);

            for (String p : pages) {
               ((ButtonWidget)this.method_37063(
                     new ButtonWidget(x, this.field_22790 / 2 - 105, w, 20, class_2561.method_43471("gui.villager_editor.page." + p), sender -> this.setPage(p))
                  ))
                  .field_22763 = !p.equals(page);
               x += w;
            }

            this.doneWidget = (ButtonWidget)this.method_37063(
               new ButtonWidget(this.field_22789 / 2 - 175 + 20, this.field_22790 / 2 + 85, 135, 20, class_2561.method_43471("gui.done"), sender -> {
                  this.syncVillagerData();
                  this.method_25419();
               })
            );
         }

         int y = this.field_22790 / 2 - 80;
         int margin = 40;
         Genetics genetics = this.villager.getGenetics();
         switch (page) {
            case "general": {
               this.drawName(this.field_22789 / 2, y);
               y += 20;
               this.drawGender(this.field_22789 / 2, y);
               y += 22;
               if (this.villagerUUID.equals(this.playerUUID)) {
                  this.addModelSelectionWidgets(this.field_22789 / 2, y);
                  y += 22;
               }

               if (!this.villagerUUID.equals(this.playerUUID)) {
                  this.method_37063(
                     new GeneSliderWidget(
                        this.field_22789 / 2,
                        y,
                        175,
                        20,
                        class_2561.method_43471("gui.villager_editor.age"),
                        1.0 + (double)this.villagerBreedingAge / AgeState.getMaxAge(),
                        b -> {
                           this.villagerBreedingAge = -((int)((1.0 - b) * AgeState.getMaxAge())) + 1;
                           this.villager.method_5614(this.villagerBreedingAge);
                           this.villager.method_18382();
                        }
                     )
                  );
                  y += 28;
               }

               for (String who : new String[]{"father", "mother", "spouse"}) {
                  class_342 textFieldWidgetx = (class_342)this.method_37063(
                     new NamedTextFieldWidget(
                        this.field_22793, this.field_22789 / 2, y, 175, 18, class_2561.method_43471("gui.villager_editor.relation." + who)
                     )
                  );
                  textFieldWidgetx.method_1880(64);
                  textFieldWidgetx.method_1852(this.villagerData.method_10558("tree_" + who + "_name"));
                  textFieldWidgetx.method_1863(name -> this.villagerData.method_10582("tree_" + who + "_new", name));
                  y += 20;
               }

               y += 4;
               class_342 textFieldWidget = (class_342)this.method_37063(
                  new class_342(this.field_22793, this.field_22789 / 2, y, 175, 18, class_2561.method_43470("UUID"))
               );
               textFieldWidget.method_1880(64);
               textFieldWidget.method_1852(this.villagerUUID.toString());
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

               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2, y, 87, 20, class_2561.method_43471("gui.villager_editor.randClothing"), b -> this.sendCommand("clothing")
                  )
               );
               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2 + 87, y, 87, 20, class_2561.method_43471("gui.villager_editor.selectClothing"), b -> this.setPage("clothing")
                  )
               );
               y += 22;
               this.method_37063(new ButtonWidget(this.field_22789 / 2, y, 87, 20, class_2561.method_43471("gui.villager_editor.prev"), b -> {
                  class_2487 compound = new class_2487();
                  compound.method_10569("offset", -1);
                  this.sendCommand("clothing", compound);
               }));
               this.method_37063(new ButtonWidget(this.field_22789 / 2 + 87, y, 87, 20, class_2561.method_43471("gui.villager_editor.next"), b -> {
                  class_2487 compound = new class_2487();
                  compound.method_10569("offset", 1);
                  this.sendCommand("clothing", compound);
               }));
               y += 22;
               this.method_37063(
                  new ColorPickerWidget(
                     this.field_22789 / 2 + margin,
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
               this.method_37063(
                  new TooltipButtonWidget(
                     this.field_22789 / 2 + 87,
                     y,
                     87,
                     20,
                     class_2561.method_43471(this.hsvColoredHair ? "gui.villager_editor.hair_hsv" : "gui.villager_editor.hair_genetic"),
                     class_2561.method_43471("gui.villager_editor.hair_mode.tooltip"),
                     b -> {
                        this.hsvColoredHair = !this.hsvColoredHair;
                        this.method_25426();
                     }
                  )
               );
               y = this.doubleGeneSliders(y, Genetics.FACE);
               y = this.doubleGeneSliders(y, Genetics.VOICE_TONE, Genetics.VOICE);
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2, y, 87, 20, class_2561.method_43471("gui.villager_editor.randHair"), b -> this.sendCommand("hair"))
               );
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 + 87, y, 87, 20, class_2561.method_43471("gui.villager_editor.selectHair"), b -> this.setPage("hair"))
               );
               y += 22;
               this.method_37063(new ButtonWidget(this.field_22789 / 2, y, 87, 20, class_2561.method_43471("gui.villager_editor.prev"), b -> {
                  class_2487 compound = new class_2487();
                  compound.method_10569("offset", -1);
                  this.sendCommand("hair", compound);
               }));
               this.method_37063(new ButtonWidget(this.field_22789 / 2 + 87, y, 87, 20, class_2561.method_43471("gui.villager_editor.next"), b -> {
                  class_2487 compound = new class_2487();
                  compound.method_10569("offset", 1);
                  this.sendCommand("hair", compound);
               }));
               y += 22;
               if (this.hsvColoredHair) {
                  this.color.hueWidget = (HorizontalColorPickerWidget)this.method_37063(
                     new HorizontalColorPickerWidget(
                        this.field_22789 / 2 + 20, y, 135, 15, this.color.hue / 360.0, MCA.locate("textures/colormap/hue.png"), (vx, vy) -> {
                           this.color.setHSV(vx * 360.0, this.color.saturation, this.color.brightness);
                           this.refreshHairColor();
                        }
                     )
                  );
                  this.color.saturationWidget = (HorizontalColorPickerWidget)this.method_37063(
                     new HorizontalGradientWidget(this.field_22789 / 2 + 20, y + 20, 135, 15, this.color.saturation, () -> {
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
                  this.color.brightnessWidget = (HorizontalColorPickerWidget)this.method_37063(
                     new HorizontalGradientWidget(this.field_22789 / 2 + 20, y + 40, 135, 15, this.color.brightness, () -> {
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
                  this.method_37063(new ButtonWidget(this.field_22789 / 2, y, 175, 20, class_2561.method_43471("gui.villager_editor.clear_hair"), b -> {
                     this.villager.clearHairDye();
                     this.method_25426();
                  }));
               } else {
                  this.method_37063(
                     new ColorPickerWidget(
                        this.field_22789 / 2 + margin,
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

                     ButtonWidget widget = (ButtonWidget)this.method_37063(new ButtonWidget(this.field_22789 / 2 + 87 * row, y, 87, 20, p.getName(), b -> {
                        this.villager.getVillagerBrain().setPersonality(p);
                        personalityButtons.forEach(v -> v.field_22763 = true);
                        b.field_22763 = false;
                     }));
                     widget.field_22763 = p != this.villager.getVillagerBrain().getPersonality();
                     personalityButtons.add(widget);
                     row++;
                  }
               }
               break;
            case "traits":
               this.method_37063(new ButtonWidget(this.field_22789 / 2, y, 32, 20, class_2561.method_43470("<"), b -> this.setTraitPage(this.traitPage - 1)));
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 + 175 - 32, y, 32, 20, class_2561.method_43470(">"), b -> this.setTraitPage(this.traitPage + 1))
               );
               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2 + 32,
                     y,
                     111,
                     20,
                     class_2561.method_43469("gui.villager_editor.page", new Object[]{this.traitPage + 1}),
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
                  class_5250 name = t.getName()
                     .method_27661()
                     .method_27692(this.villager.getTraits().hasTrait(t) ? class_124.field_1060 : class_124.field_1080);
                  this.method_37063(
                     new ButtonWidget(
                        this.field_22789 / 2,
                        y,
                        175,
                        20,
                        name,
                        b -> {
                           if (this.villager.getTraits().hasTrait(t)) {
                              this.villager.getTraits().removeTrait(t);
                           } else {
                              this.villager.getTraits().addTrait(t);
                           }

                           b.method_25355(
                              t.getName().method_27661().method_27692(this.villager.getTraits().hasTrait(t) ? class_124.field_1060 : class_124.field_1080)
                           );
                        }
                     )
                  );
                  y += 20;
               }
               break;
            case "debug":
               boolean right = false;
               List<ButtonWidget> professionButtons = new LinkedList<>();

               for (class_3852 p : new class_3852[]{
                  class_3852.field_17051,
                  (class_3852)ProfessionsMCA.GUARD.get(),
                  (class_3852)ProfessionsMCA.ARCHER.get(),
                  (class_3852)ProfessionsMCA.OUTLAW.get(),
                  (class_3852)ProfessionsMCA.ADVENTURER.get(),
                  (class_3852)ProfessionsMCA.CULTIST.get()
               }) {
                  class_5250 text = class_2561.method_43471("entity.minecraft.villager." + p);
                  ButtonWidget widget = (ButtonWidget)this.method_37063(new ButtonWidget(this.field_22789 / 2 + (right ? 87 : 0), y, 87, 20, text, b -> {
                     class_2487 compound = new class_2487();
                     compound.method_10582("profession", class_7923.field_41195.method_10221(p).toString());
                     this.syncVillagerData();
                     NetworkHandler.sendToServer(new VillagerEditorSyncRequest("profession", this.villagerUUID, compound));
                     this.requestVillagerData();
                     professionButtons.forEach(button -> button.field_22763 = true);
                     b.field_22763 = false;
                  }));
                  professionButtons.add(widget);
                  widget.field_22763 = this.villager.getProfession() != p;
                  if (right) {
                     y += 20;
                  }

                  right = !right;
               }

               y += 4;
               this.method_37063(
                  new GeneSliderWidget(
                     this.field_22789 / 2, y, 175, 20, class_2561.method_43471("gui.villager_editor.infection"), this.villager.getInfectionProgress(), b -> {
                        this.villager.setInfected(b > 0.0);
                        this.villager.setInfectionProgress(b.floatValue());
                     }
                  )
               );
               y += 22;
               assert this.field_22787 != null;
               assert this.field_22787.field_1724 != null;
               Memories player = this.villager.getVillagerBrain().getMemoriesForPlayer(this.field_22787.field_1724);
               y = this.integerChanger(y, player::modHearts, () -> class_2561.method_43469("gui.blueprint.reputation", new Object[]{player.getHearts()}));
               this.integerChanger(
                  y,
                  v -> this.villager.getVillagerBrain().modifyMoodValue(v),
                  () -> class_2561.method_43469("gui.interact.label.mood", new Object[]{this.villager.getVillagerBrain().getMoodValue()})
               );
               break;
            case "clothing":
            case "hair": {
               this.filterGender = this.villager.getGenetics().getGender();
               this.searchString = "";
               class_342 textFieldWidget = (class_342)this.method_37063(
                  new class_342(
                     this.field_22793, this.field_22789 / 2 - 87, this.field_22790 / 2 - 100, 175, 18, class_2561.method_43471("gui.villager_editor.search")
                  )
               );
               textFieldWidget.method_1880(64);
               textFieldWidget.method_1863(v -> {
                  this.searchString = v;
                  this.filter();
               });
               y = this.field_22790 / 2 + 85;
               this.pageButtonWidget = (ButtonWidget)this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 30, y, 60, 20, class_2561.method_43470(""), b -> {})
               );
               this.method_37063(new ButtonWidget(this.field_22789 / 2 - 32 - 28, y, 28, 20, class_2561.method_43470("<<"), b -> {
                  this.clothingPage = Math.max(0, this.clothingPage - 1);
                  this.updateClothingPageWidget();
               }));
               this.method_37063(new ButtonWidget(this.field_22789 / 2 + 32, y, 28, 20, class_2561.method_43470(">>"), b -> {
                  this.clothingPage = Math.max(0, Math.min(this.clothingPageCount - 1, this.clothingPage + 1));
                  this.updateClothingPageWidget();
               }));
               this.method_37063(new ButtonWidget(this.field_22789 / 2 + 32 + 32, y, 64, 20, class_2561.method_43471("gui.button.done"), b -> {
                  if (page.equals("clothing")) {
                     this.setPage("body");
                  } else {
                     this.setPage("head");
                  }
               }));
               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2 + 128,
                     y,
                     64,
                     20,
                     class_2561.method_43471("gui.button.library"),
                     b -> class_310.method_1551().method_1507(new SkinLibraryScreen(this, this.villagerVisualization))
                  )
               );
               this.widgetMasculine = (ButtonWidget)this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 32 - 96 - 64, y, 64, 20, class_2561.method_43471("gui.villager_editor.masculine"), b -> {
                     this.filterGender = Gender.MALE;
                     this.filter();
                     this.widgetMasculine.field_22763 = false;
                     this.widgetFeminine.field_22763 = true;
                  })
               );
               this.widgetMasculine.field_22763 = this.filterGender != Gender.MALE;
               this.widgetFeminine = (ButtonWidget)this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 32 - 96 - 64 + 64, y, 64, 20, class_2561.method_43471("gui.villager_editor.feminine"), b -> {
                     this.filterGender = Gender.FEMALE;
                     this.filter();
                     this.widgetMasculine.field_22763 = true;
                     this.widgetFeminine.field_22763 = false;
                  })
               );
               this.widgetFeminine.field_22763 = this.filterGender != Gender.FEMALE;
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
         this.pageButtonWidget.method_25355(class_2561.method_43470(String.format("%d / %d", this.clothingPage + 1, this.clothingPageCount)));
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
            this.doneWidget.field_22763 = !MCA.isBlankString(name);
         }
      });
   }

   protected void drawName(int x, int y, Consumer<String> onChanged) {
      this.villagerNameField = (class_342)this.method_37063(
         new class_342(this.field_22793, x, y, 116, 18, class_2561.method_43471("structure_block.structure_name"))
      );
      this.villagerNameField.method_1880(32);
      this.villagerNameField.method_1852(this.getName().getString());
      this.villagerNameField.method_1863(onChanged);
      this.method_37063(
         new ButtonWidget(
            x + 116 + 1,
            y - 1,
            56,
            20,
            class_2561.method_43471("gui.button.random"),
            b -> NetworkHandler.sendToServer(new VillagerNameRequest(this.villager.getGenetics().getGender()))
         )
      );
   }

   public class_2561 getName() {
      class_2561 villagerName = null;
      boolean isPlayer = this.villagerUUID.equals(this.playerUUID);
      if (isPlayer) {
         assert this.field_22787 != null;
         assert this.field_22787.field_1724 != null;
         villagerName = this.field_22787.field_1724.method_5797();
      } else if (this.villager.method_16914()) {
         villagerName = this.villager.method_5797();
      }

      if (villagerName == null || MCA.isBlankString(villagerName.getString())) {
         if (isPlayer) {
            assert this.field_22787 != null;
            assert this.field_22787.field_1724 != null;
            villagerName = this.field_22787.field_1724.method_5477();
         } else {
            villagerName = this.villager.method_5477();
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
         class_2561 newName = class_2561.method_30163(name);
         boolean isPlayer = this.villagerUUID.equals(this.playerUUID);
         if (isPlayer) {
            assert this.field_22787 != null;
            assert this.field_22787.field_1724 != null;
            class_2561 realName = this.field_22787.field_1724.method_5477();
            if (realName.getString().equals(name)) {
               newName = null;
            }

            this.field_22787.field_1724.method_5665(newName);
            this.field_22787.field_1724.method_5880(newName != null);
            if (this.field_22787.field_1724.method_5807()) {
               this.villager.method_5665(newName);
            } else {
               this.villager.setName(realName.getString());
            }
         } else {
            this.villager.method_5665(newName);
         }
      }
   }

   void drawGender(int x, int y) {
      this.genderButtonFemale = new ButtonWidget(x, y, 87, 20, class_2561.method_43471("gui.villager_editor.feminine"), sender -> {
         this.villager.getGenetics().setGender(Gender.FEMALE);
         this.sendCommand("gender");
         this.genderButtonFemale.field_22763 = false;
         this.genderButtonMale.field_22763 = true;
      });
      this.method_37063(this.genderButtonFemale);
      this.genderButtonMale = new ButtonWidget(x + 87, y, 87, 20, class_2561.method_43471("gui.villager_editor.masculine"), sender -> {
         this.villager.getGenetics().setGender(Gender.MALE);
         this.sendCommand("gender");
         this.genderButtonFemale.field_22763 = true;
         this.genderButtonMale.field_22763 = false;
      });
      this.method_37063(this.genderButtonMale);
      this.genderButtonFemale.field_22763 = this.villager.getGenetics().getGender() != Gender.FEMALE;
      this.genderButtonMale.field_22763 = this.villager.getGenetics().getGender() != Gender.MALE;
   }

   void addModelSelectionWidgets(int x, int y) {
      if (this.allowPlayerModel && this.allowVillagerModel) {
         this.villagerSkinWidget = (ButtonWidget)this.method_37063(new TooltipButtonWidget(x, y, 58, 20, "gui.villager_editor.villager_skin", b -> {
            this.villagerData.method_10569("playerModel", VillagerLike.PlayerModel.VILLAGER.ordinal());
            this.syncVillagerData();
            this.playerSkinWidget.field_22763 = true;
            this.villagerSkinWidget.field_22763 = false;
            this.vanillaSkinWidget.field_22763 = true;
         }));
         this.villagerSkinWidget.field_22763 = this.villagerData.method_10550("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal();
         this.playerSkinWidget = (ButtonWidget)this.method_37063(new TooltipButtonWidget(x + 58, y, 58, 20, "gui.villager_editor.player_skin", b -> {
            this.villagerData.method_10569("playerModel", VillagerLike.PlayerModel.PLAYER.ordinal());
            this.syncVillagerData();
            this.playerSkinWidget.field_22763 = false;
            this.villagerSkinWidget.field_22763 = true;
            this.vanillaSkinWidget.field_22763 = true;
         }));
         this.playerSkinWidget.field_22763 = this.villagerData.method_10550("playerModel") != VillagerLike.PlayerModel.PLAYER.ordinal();
         this.vanillaSkinWidget = (ButtonWidget)this.method_37063(new TooltipButtonWidget(x + 116, y, 58, 20, "gui.villager_editor.vanilla_skin", b -> {
            this.villagerData.method_10569("playerModel", VillagerLike.PlayerModel.VANILLA.ordinal());
            this.syncVillagerData();
            this.villagerSkinWidget.field_22763 = true;
            this.playerSkinWidget.field_22763 = true;
            this.vanillaSkinWidget.field_22763 = false;
         }));
         this.vanillaSkinWidget.field_22763 = this.villagerData.method_10550("playerModel") != VillagerLike.PlayerModel.VANILLA.ordinal();
      } else {
         ((TooltipButtonWidget)this.method_37063(new TooltipButtonWidget(x, y, 175, 20, "gui.villager_editor.model_blacklist_hint", b -> {}))).field_22763 = false;
      }
   }

   private void sendCommand(String command) {
      this.sendCommand(command, new class_2487());
   }

   private void sendCommand(String command, class_2487 nbt) {
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

   public boolean method_25402(double mouseX, double mouseY, int button) {
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
         return super.method_25402(mouseX, mouseY, button);
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

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_25294(0, 20, this.field_22789, this.field_22790 - 20, 1711276032);
      if (this.villager != null) {
         this.villager.field_6012 = (int)(System.currentTimeMillis() / 50L);
         if (this.shouldDrawEntity()) {
            int x = this.field_22789 / 2 - 87;
            int y = this.field_22790 / 2 + 70;
            if (this.villagerUUID.equals(this.playerUUID) && this.shouldUsePlayerModel()) {
               assert class_310.method_1551().field_1724 != null;
               class_490.method_2486(context, x, y, 60, x - mouseX, y - 50 - mouseY, class_310.method_1551().field_1724);
            } else {
               class_490.method_2486(context, x, y, 60, x - mouseX, y - 50 - mouseY, this.villager);
            }

            if (this.shouldPrintPlayerHint()
               && this.villagerUUID.equals(this.playerUUID)
               && this.villagerData.method_10550("playerModel") != VillagerLike.PlayerModel.VILLAGER.ordinal()) {
               class_4587 matrices = context.method_51448();
               matrices.method_22903();
               matrices.method_46416(x, y - 145, 0.0F);
               matrices.method_22905(0.5F, 0.5F, 0.5F);
               context.method_27534(this.field_22793, class_2561.method_43471("gui.villager_editor.model_hint"), 0, 0, -1426063361);
               matrices.method_22909();
            }
         }

         if (this.page.equals("clothing") || this.page.equals("hair")) {
            class_2487 nbt = new class_2487();
            this.villager.method_5652(nbt);
            this.villagerVisualization.method_5749(nbt);
            this.villagerVisualization.method_5614(this.villager.method_5618());
            this.villagerVisualization.method_18382();
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

                  int cx = this.field_22789 / 2 + (int)((x - 4.0 + 0.5 - 0.5 * (y % 2)) * 40.0);
                  int cy = this.field_22790 / 2 + 25 + (int)((y - 1.0 + 0.5) * 65.0);
                  if (Math.abs(cx - mouseX) <= 20 && Math.abs(cy - mouseY - 30) <= 30) {
                     this.hoveredClothingId = index;
                  }

                  class_490.method_2486(
                     context, cx, cy, this.hoveredClothingId == index ? 35 : 30, -(mouseX - cx) / 2.0F, -(mouseY - cy - 64) / 2.0F, this.villagerVisualization
                  );
                  i++;
               }
            }
         }

         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   protected boolean shouldDrawEntity() {
      return !this.page.equals("loading") && !this.page.equals("clothing") && !this.page.equals("hair");
   }

   protected boolean shouldShowPageSelection() {
      return !this.page.equals("clothing") && !this.page.equals("hair");
   }

   public void setVillagerName(String name) {
      this.villagerNameField.method_1852(name);
      this.updateName(name);
   }

   public void setVillagerData(class_2487 villagerData) {
      if (this.villager != null) {
         this.villagerData = villagerData;
         this.villager.method_5749(villagerData);
         float[] hairDye = this.villager.getHairDye();
         this.hsvColoredHair = hairDye[0] > 0.0F;
         this.color.setRGB(hairDye[0], hairDye[1], hairDye[2]);
         this.villagerBreedingAge = villagerData.method_10550("Age");
         this.villager.method_5614(this.villagerBreedingAge);
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            this.villager
               .method_23327(this.field_22787.field_1724.method_23317(), this.field_22787.field_1724.method_23318(), this.field_22787.field_1724.method_23321());
            this.villagerVisualization
               .method_23327(this.field_22787.field_1724.method_23317(), this.field_22787.field_1724.method_23318(), this.field_22787.field_1724.method_23321());
         }

         this.villager.method_18382();
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
      class_2487 nbt = this.villagerData;
      this.villager.method_5652(nbt);
      nbt.method_10569("Age", this.villagerBreedingAge);
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
