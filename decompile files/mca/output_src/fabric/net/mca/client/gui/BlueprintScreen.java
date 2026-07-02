package fabric.net.mca.client.gui;

import fabric.net.mca.MCA;
import fabric.net.mca.client.gui.widget.TooltipButtonWidget;
import fabric.net.mca.client.gui.widget.WidgetUtils;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.GetVillageRequest;
import fabric.net.mca.network.c2s.RenameVillageMessage;
import fabric.net.mca.network.c2s.ReportBuildingMessage;
import fabric.net.mca.network.c2s.SaveVillageMessage;
import fabric.net.mca.resources.BuildingTypes;
import fabric.net.mca.resources.Rank;
import fabric.net.mca.resources.data.BuildingType;
import fabric.net.mca.resources.data.tasks.Task;
import fabric.net.mca.server.world.data.Building;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.util.compat.ButtonWidget;
import fabric.net.mca.util.localization.FlowingText;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.class_124;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_344;
import net.minecraft.class_4185;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_746;
import net.minecraft.class_7923;

public class BlueprintScreen extends ExtendedScreen {
   private static final int POSITION_TAXES = -60;
   private static final int POSITION_BIRTH = -10;
   private static final int POSITION_MARRIAGE = 40;
   private Village village;
   private int reputation;
   private boolean isVillage;
   private Rank rank;
   private Set<String> completedTasks;
   private String page;
   private ButtonWidget[] buttonTaxes;
   private ButtonWidget[] buttonBirths;
   private ButtonWidget[] buttonMarriage;
   private ButtonWidget buttonPage;
   private int pageNumber = 0;
   private final List<class_4185> catalogButtons = new LinkedList<>();
   private static final class_2960 ICON_TEXTURES = MCA.locate("textures/buildings.png");
   private BuildingType selectedBuilding;
   private UUID selectedVillager;
   private int mouseX;
   private int mouseY;
   private Map<Rank, List<Task>> tasks;

   public BlueprintScreen() {
      super(class_2561.method_43470("Blueprint"));
   }

   private void saveVillage() {
      NetworkHandler.sendToServer(new SaveVillageMessage(this.village));
   }

   private void changeTaxes(float d) {
      this.village.setTaxes(Math.max(0.0F, Math.min(1.0F, this.village.getTaxes() + d)));
      this.saveVillage();
   }

   private void changePopulationThreshold(float d) {
      this.village.setPopulationThreshold(Math.max(0.0F, Math.min(1.0F, this.village.getPopulationThreshold() + d)));
      this.saveVillage();
   }

   private void changeMarriageThreshold(float d) {
      this.village.setMarriageThreshold(Math.max(0.0F, Math.min(1.0F, this.village.getMarriageThreshold() + d)));
      this.saveVillage();
   }

   private ButtonWidget[] createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress, class_2561 tooltip) {
      ButtonWidget[] buttons = new ButtonWidget[]{
         null,
         (ButtonWidget)this.method_37063(new ButtonWidget(x - w / 2, y, w / 4, h, class_2561.method_43470("<<"), b -> onPress.accept(false))),
         (ButtonWidget)this.method_37063(new ButtonWidget(x + w / 4, y, w / 4, h, class_2561.method_43470(">>"), b -> onPress.accept(true)))
      };
      buttons[0] = (ButtonWidget)this.method_37063(new ButtonWidget(x - w / 4, y, w / 2, h, class_2561.method_43470(""), b -> {}, tooltip));
      return buttons;
   }

   protected void drawBuildingIcon(class_332 context, class_2960 texture, int x, int y, int u, int v) {
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_22904(x - 6.6, y - 6.6, 0.0);
      matrices.method_22905(0.66F, 0.66F, 0.66F);
      context.method_25302(texture, 0, 0, u, v, 20, 20);
      matrices.method_22909();
   }

   public void method_25426() {
      NetworkHandler.sendToServer(new GetVillageRequest());
      this.setPage("waiting");
   }

   private void setPage(String page) {
      if (page.equals("close")) {
         assert this.field_22787 != null;
         this.field_22787.method_1507(null);
      } else {
         this.page = page;
         this.method_37067();
         this.method_37063(new ButtonWidget(5, 5, 20, 20, class_2561.method_43471("gui.button.backarrow"), b -> this.setPage("close")));
         int bx = this.field_22789 / 2 - 180;
         int by = this.field_22790 / 2 - 56;
         if (!page.equals("rename") && !page.equals("empty") && !page.equals("waiting")) {
            for (String p : new String[]{"map", "rank", "catalog", "villagers", "rules", "refresh"}) {
               ButtonWidget widget = new ButtonWidget(bx, by, 80, 20, class_2561.method_43471("gui.blueprint." + p), b -> this.setPage(p));
               this.method_37063(widget);
               if (page.equals(p)) {
                  widget.field_22763 = false;
               }

               by += 22;
            }
         }

         switch (page) {
            case "empty":
               bx = this.field_22789 / 2 - 48;
               by = this.field_22790 / 2;
               this.method_37063(new TooltipButtonWidget(bx - 50, by + 5, 96, 20, "gui.blueprint.addRoom", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD_ROOM));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.method_25419();
               }));
               this.method_37063(new TooltipButtonWidget(bx + 50, by + 5, 96, 20, "gui.blueprint.addBuilding", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.method_25419();
               }));
               break;
            case "refresh":
               NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.FULL_SCAN));
               NetworkHandler.sendToServer(new GetVillageRequest());
               assert this.field_22787 != null;
               assert this.field_22787.field_1724 != null;
               this.field_22787.field_1724.method_7353(class_2561.method_43471("blueprint.refreshed"), true);
               this.setPage("map");
               break;
            case "advanced":
               bx = this.field_22789 / 2 + 180 - 64 - 16;
               by = this.field_22790 / 2 - 56;
               class_5250 text = class_2561.method_43471("gui.blueprint.autoScan");
               if (this.village.isAutoScan()) {
                  text.method_27692(class_124.field_1060);
               } else {
                  text.method_27692(class_124.field_1080).method_27692(class_124.field_1055);
               }

               this.method_37063(new TooltipButtonWidget(bx, by, 96, 20, text, class_2561.method_43471("gui.blueprint.autoScan.tooltip"), b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.AUTO_SCAN));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.village.toggleAutoScan();
                  this.setPage(page);
               }));
               by += 22;
               this.method_37063(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.restrictAccess", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.FORCE_TYPE, "blocked"));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               this.method_37063(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.addBuilding", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 66;
               if (this.isVillage) {
                  this.method_37063(new ButtonWidget(bx, by, 96, 20, class_2561.method_43471("gui.blueprint.renameVillage"), b -> this.setPage("rename")));
               }
            case "map":
               bx = this.field_22789 / 2 + 180 - 64 - 16;
               by = this.field_22790 / 2 - 56 + 66;
               this.method_37063(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.addRoom", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD_ROOM));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               this.method_37063(new ButtonWidget(bx, by, 96, 20, class_2561.method_43471("gui.blueprint.removeBuilding"), b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.REMOVE));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               if (!page.equals("advanced")) {
                  this.method_37063(new ButtonWidget(bx, by, 96, 20, class_2561.method_43471("gui.blueprint.advanced"), b -> this.setPage("advanced")));
               }
            case "rank":
            default:
               break;
            case "catalog":
               int row = 0;
               int col = 0;
               int size = 21;
               int x = this.field_22789 / 2 - 4 * size - 8;
               int y = (int)(this.field_22790 / 2 - 2.0 * size);
               this.catalogButtons.clear();

               for (BuildingType bt : BuildingTypes.getInstance()) {
                  if (bt.visible()) {
                     class_344 widget = new class_344(
                        row * size + x + 10, col * size + y - 10, 20, 20, bt.iconU(), bt.iconV() + 20, 20, ICON_TEXTURES, 256, 256, button -> {
                           this.selectBuilding(bt);
                           button.field_22763 = false;
                           this.catalogButtons.forEach(b -> b.field_22763 = true);
                        }, class_2561.method_43471("buildingType." + bt.name())
                     );
                     this.catalogButtons.add((class_4185)this.method_37063(widget));
                     if (++row > 4) {
                        row = 0;
                        col++;
                     }
                  }
               }
               break;
            case "villagers":
               this.method_37063(new ButtonWidget(this.field_22789 / 2 - 24 - 20, this.field_22790 / 2 + 54, 20, 20, class_2561.method_43470("<"), b -> {
                  if (this.pageNumber > 0) {
                     this.pageNumber--;
                  }
               }));
               this.method_37063(new ButtonWidget(this.field_22789 / 2 + 24, this.field_22790 / 2 + 54, 20, 20, class_2561.method_43470(">"), b -> {
                  if (this.pageNumber < Math.ceil(this.village.getPopulation() / 9.0) - 1.0) {
                     this.pageNumber++;
                  }
               }));
               this.buttonPage = (ButtonWidget)this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 - 24, this.field_22790 / 2 + 54, 48, 20, class_2561.method_43470("0/0)"), b -> {})
               );
               break;
            case "rules":
               this.buttonTaxes = this.createValueChanger(
                  this.field_22789 / 2,
                  this.field_22790 / 2 + -60 + 10,
                  80,
                  20,
                  b -> this.changeTaxes(b ? 0.125F : -0.125F),
                  class_2561.method_43471("gui.blueprint.tooltip.taxes")
               );
               this.toggleButtons(this.buttonTaxes, false);
               this.buttonBirths = this.createValueChanger(
                  this.field_22789 / 2,
                  this.field_22790 / 2 + -10 + 10,
                  80,
                  20,
                  b -> this.changePopulationThreshold(b ? 0.125F : -0.125F),
                  class_2561.method_43471("gui.blueprint.tooltip.births")
               );
               this.toggleButtons(this.buttonBirths, false);
               this.buttonMarriage = this.createValueChanger(
                  this.field_22789 / 2,
                  this.field_22790 / 2 + 40 + 10,
                  80,
                  20,
                  b -> this.changeMarriageThreshold(b ? 0.125F : -0.125F),
                  class_2561.method_43471("gui.blueprint.tooltip.marriage")
               );
               this.toggleButtons(this.buttonMarriage, false);
               break;
            case "rename":
               class_342 field = (class_342)this.method_37063(
                  new class_342(
                     this.field_22793, this.field_22789 / 2 - 65, this.field_22790 / 2 - 16, 130, 20, class_2561.method_43471("gui.blueprint.renameVillage")
                  )
               );
               field.method_1880(32);
               field.method_1852(this.village.getName());
               this.method_37063(
                  new ButtonWidget(
                     this.field_22789 / 2 - 66, this.field_22790 / 2 + 8, 64, 20, class_2561.method_43471("gui.blueprint.cancel"), b -> this.setPage("map")
                  )
               );
               this.method_37063(
                  new ButtonWidget(this.field_22789 / 2 + 2, this.field_22790 / 2 + 8, 64, 20, class_2561.method_43471("gui.blueprint.rename"), b -> {
                     NetworkHandler.sendToServer(new RenameVillageMessage(this.village.getId(), field.method_1882()));
                     this.village.setName(field.method_1882());
                     this.setPage("map");
                  })
               );
         }
      }
   }

   private void selectBuilding(BuildingType b) {
      this.selectedBuilding = b;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25394(class_332 context, int sizeX, int sizeY, float offset) {
      this.method_25420(context);
      assert this.field_22787 != null;
      this.mouseX = (int)(this.field_22787.field_1729.method_1603() * this.field_22789 / this.field_22787.method_22683().method_4489());
      this.mouseY = (int)(this.field_22787.field_1729.method_1604() * this.field_22790 / this.field_22787.method_22683().method_4506());
      switch (this.page) {
         case "waiting":
            context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.waiting"), this.field_22789 / 2, this.field_22790 / 2, -5592406);
            break;
         case "empty":
            context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.empty"), this.field_22789 / 2, this.field_22790 / 2 - 20, -5592406);
            break;
         case "map":
            this.renderStats(context);
            this.renderName(context);
            this.renderMap(context);
            break;
         case "advanced":
            this.renderName(context);
            this.renderMap(context);
            break;
         case "rank":
            this.renderTasks(context);
            this.renderStats(context);
            break;
         case "catalog":
            this.renderCatalog(context);
            break;
         case "villagers":
            this.renderVillagers(context);
            break;
         case "rules":
            this.renderRules(context);
      }

      super.method_25394(context, sizeX, sizeY, offset);
   }

   private void renderName(class_332 context) {
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_22905(2.0F, 2.0F, 2.0F);
      if (this.isVillage) {
         context.method_25300(this.field_22793, this.village.getName(), this.field_22789 / 4, this.field_22790 / 4 - 48, -1);
      } else {
         context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.settlement"), this.field_22789 / 4, this.field_22790 / 4 - 48, -1);
      }

      matrices.method_22909();
   }

   private void renderStats(class_332 context) {
      int x = this.field_22789 / 2 + (this.page.equals("rank") ? -70 : 105);
      int y = this.field_22790 / 2 - 50;
      class_2561 rankStr = class_2561.method_43471(this.rank.getTranslationKey());
      int rankColor = this.rank.ordinal() == 0 ? -65536 : -256;
      context.method_27535(this.field_22793, class_2561.method_43469("gui.blueprint.currentRank", new Object[]{rankStr}), x, y, rankColor);
      context.method_27535(
         this.field_22793,
         class_2561.method_43469("gui.blueprint.reputation", new Object[]{String.valueOf(this.reputation)}),
         x,
         y + 11,
         this.rank.ordinal() == 0 ? -65536 : -1
      );
      context.method_27535(
         this.field_22793, class_2561.method_43469("gui.blueprint.buildings", new Object[]{this.village.getBuildings().size()}), x, y + 22, -1
      );
      context.method_27535(
         this.field_22793,
         class_2561.method_43469("gui.blueprint.population", new Object[]{this.village.getPopulation(), this.village.getMaxPopulation()}),
         x,
         y + 33,
         -1
      );
   }

   private void renderMap(class_332 context) {
      class_4587 matrices = context.method_51448();
      int mapSize = 75;
      int y = this.field_22790 / 2 + 8;
      WidgetUtils.drawRectangle(context, this.field_22789 / 2 - mapSize, y - mapSize, this.field_22789 / 2 + mapSize, y + mapSize, -120);
      if (!this.village.isAutoScan() && this.village.getBuildings().size() <= 1) {
         context.method_27534(
            this.field_22793, class_2561.method_43471("gui.blueprint.autoScanDisabled"), this.field_22789 / 2, this.field_22790 / 2 + 90, -1426063361
         );
      }

      matrices.method_22903();
      float sc = Math.min((float)mapSize / (this.village.getBox().getMaxBlockCount() + 3) * 2.0F, 2.0F);
      int mouseLocalX = (int)((this.mouseX - this.field_22789 / 2.0) / sc + this.village.getCenter().method_10263());
      int mouseLocalY = (int)((this.mouseY - y) / sc + this.village.getCenter().method_10260());
      matrices.method_22904(this.field_22789 / 2.0, y, 0.0);
      matrices.method_22905(sc, sc, 0.0F);
      matrices.method_46416(-this.village.getCenter().method_10263(), -this.village.getCenter().method_10260(), 0.0F);
      assert this.field_22787 != null;
      class_746 player = this.field_22787.field_1724;
      if (player != null) {
         WidgetUtils.drawRectangle(
            context, (int)player.method_23317() - 1, (int)player.method_23321() - 1, (int)player.method_23317() + 1, (int)player.method_23321() + 1, -65281
         );
      }

      List<Building> hoverBuildings = new LinkedList<>();

      for (Building building : this.village.getBuildings().values()) {
         if (building.isComplete()) {
            BuildingType bt = building.getBuildingType();
            if (bt.isIcon()) {
               class_2338 c = building.getCenter();
               this.drawBuildingIcon(context, ICON_TEXTURES, c.method_10263(), c.method_10260(), bt.iconU(), bt.iconV());
               int margin = 6;
               if (c.method_10262(new class_2382(mouseLocalX, c.method_10264(), mouseLocalY)) < margin * margin) {
                  hoverBuildings.add(building);
               }
            } else {
               class_2338 p0 = building.getPos0();
               class_2338 p1 = building.getPos1();
               WidgetUtils.drawRectangle(context, p0.method_10263(), p0.method_10260(), p1.method_10263(), p1.method_10260(), bt.getColor());
               if (bt.visible()) {
                  class_2338 c = building.getCenter();
                  this.drawBuildingIcon(context, ICON_TEXTURES, c.method_10263(), c.method_10260(), bt.iconU(), bt.iconV());
               }

               int margin = 1;
               if (mouseLocalX >= p0.method_10263() - margin
                  && mouseLocalX <= p1.method_10263() + margin
                  && mouseLocalY >= p0.method_10260() - margin
                  && mouseLocalY <= p1.method_10260() + margin) {
                  hoverBuildings.add(building);
               }
            }
         }
      }

      matrices.method_22909();
      hoverBuildings.sort((a, bx) -> bx.getCenter().method_10264() - a.getCenter().method_10264());
      List<List<class_2561>> tooltips = new LinkedList<>();

      for (Building b : hoverBuildings) {
         tooltips.add(this.getBuildingTooltip(b));
      }

      int h = 0;

      for (List<class_2561> b : tooltips) {
         h += this.getTooltipHeight(b) + 9;
      }

      int py = this.mouseY - h / 2 + 12;

      for (List<class_2561> b : tooltips) {
         context.method_51434(this.field_22793, b, this.mouseX, py);
         py += this.getTooltipHeight(b) + 9;
      }
   }

   private List<class_2561> getBuildingTooltip(Building hoverBuilding) {
      List<class_2561> lines = new LinkedList<>();
      BuildingType bt = BuildingTypes.getInstance().getBuildingType(hoverBuilding.getType());
      lines.add(class_2561.method_43471("buildingType." + bt.name()));

      for (String name : this.village.getResidents(hoverBuilding.getId())) {
         lines.add(class_2561.method_43470(name));
      }

      for (Entry<class_2960, List<class_2338>> block : hoverBuilding.getBlocks().entrySet()) {
         lines.add(class_2561.method_43470(block.getValue().size() + " x ").method_10852(this.getBlockName(block.getKey())).method_27692(class_124.field_1080));
      }

      return lines;
   }

   private void renderTasks(class_332 context) {
      if (this.rank != null) {
         int y = this.field_22790 / 2 + 5;
         int x = this.field_22789 / 2 - 70;

         for (Task task : this.tasks.get(this.rank.promote())) {
            boolean completed = this.completedTasks.contains(task.getId());
            class_2561 t = task.getTranslatable().method_27692(completed ? class_124.field_1055 : class_124.field_1070);
            context.method_27535(this.field_22793, t, x, y, completed ? -7798904 : -43691);
            y += 11;
         }
      }
   }

   private void renderCatalog(class_332 context) {
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_22905(2.0F, 2.0F, 2.0F);
      context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.catalogFull"), this.field_22789 / 4, this.field_22790 / 4 - 52, -1);
      matrices.method_22909();
      context.method_27534(
         this.field_22793,
         class_2561.method_43471("gui.blueprint.catalogHint").method_27692(class_124.field_1080),
         this.field_22789 / 2,
         this.field_22790 / 2 - 82,
         -1
      );
      int x = this.field_22789 / 2 + 35;
      int y = this.field_22790 / 2 - 50;
      if (this.selectedBuilding != null) {
         context.method_27535(this.field_22793, class_2561.method_43471("buildingType." + this.selectedBuilding.name()), x, y, this.selectedBuilding.getColor());
         y += 12;

         for (class_2561 t : FlowingText.wrap(
            class_2561.method_43471("buildingType." + this.selectedBuilding.name() + ".description")
               .method_27692(class_124.field_1080)
               .method_27692(class_124.field_1056),
            150
         )) {
            context.method_27535(this.field_22793, t, x, y, -1);
            y += 10;
         }

         y += 24;

         for (Entry<class_2960, Integer> b : this.selectedBuilding.getGroups().entrySet()) {
            context.method_27535(this.field_22793, class_2561.method_43470(b.getValue() + " x ").method_10852(this.getBlockName(b.getKey())), x, y, -1);
            y += 10;
         }
      } else {
         for (class_2561 t : FlowingText.wrap(
            class_2561.method_43471("gui.blueprint.buildingTypes").method_27692(class_124.field_1080).method_27692(class_124.field_1056), 150
         )) {
            context.method_27535(this.field_22793, t, x, y, -1);
            y += 10;
         }
      }
   }

   private void renderVillagers(class_332 context) {
      int maxPages = (int)Math.ceil(this.village.getPopulation() / 9.0);
      this.buttonPage.method_25355(class_2561.method_43470(this.pageNumber + 1 + "/" + maxPages));
      List<Entry<UUID, String>> villager = this.village.getResidentNames().entrySet().stream().sorted(Entry.comparingByValue()).toList();
      this.selectedVillager = null;

      for (int i = 0; i < 9; i++) {
         int index = i + this.pageNumber * 9;
         if (index >= villager.size()) {
            break;
         }

         int y = this.field_22790 / 2 - 51 + i * 11;
         boolean hover = this.isMouseWithin(this.field_22789 / 2 - 50, y - 1, 100, 11);
         context.method_27534(this.field_22793, class_2561.method_43470(villager.get(index).getValue()), this.field_22789 / 2, y, hover ? -2631804 : -1);
         if (hover) {
            this.selectedVillager = villager.get(index).getKey();
         }
      }
   }

   private void renderRules(class_332 context) {
      this.buttonTaxes[0].method_25355(class_2561.method_43470((int)(this.village.getTaxes() * 100.0F) + "%"));
      this.buttonMarriage[0].method_25355(class_2561.method_43470((int)(this.village.getMarriageThreshold() * 100.0F) + "%"));
      this.buttonBirths[0].method_25355(class_2561.method_43470((int)(this.village.getPopulationThreshold() * 100.0F) + "%"));
      context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.taxes"), this.field_22789 / 2, this.field_22790 / 2 + -60, -1);
      if (!this.rank.isAtLeast(Rank.MERCHANT)) {
         context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.rankTooLow"), this.field_22789 / 2, this.field_22790 / 2 + -60 + 15, -1);
         this.toggleButtons(this.buttonTaxes, false);
      } else {
         this.toggleButtons(this.buttonTaxes, true);
      }

      context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.birth"), this.field_22789 / 2, this.field_22790 / 2 + -10, -1);
      if (!this.rank.isAtLeast(Rank.NOBLE)) {
         context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.rankTooLow"), this.field_22789 / 2, this.field_22790 / 2 + -10 + 15, -1);
         this.toggleButtons(this.buttonBirths, false);
      } else {
         this.toggleButtons(this.buttonBirths, true);
      }

      context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.marriage"), this.field_22789 / 2, this.field_22790 / 2 + 40, -1);
      if (!this.rank.isAtLeast(Rank.MAYOR)) {
         context.method_27534(this.field_22793, class_2561.method_43471("gui.blueprint.rankTooLow"), this.field_22789 / 2, this.field_22790 / 2 + 40 + 15, -1);
         this.toggleButtons(this.buttonMarriage, false);
      } else {
         this.toggleButtons(this.buttonMarriage, true);
      }
   }

   private class_2561 getBlockName(class_2960 id) {
      return class_7923.field_41175.method_10250(id)
         ? class_2561.method_43471(((class_2248)class_7923.field_41175.method_10223(id)).method_9539())
         : class_2561.method_43471("tag." + id.toString());
   }

   private void toggleButtons(ButtonWidget[] buttons, boolean active) {
      for (ButtonWidget b : buttons) {
         b.field_22763 = active;
         b.field_22764 = active;
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.page.equals("villagers") && this.selectedVillager != null) {
         assert this.field_22787 != null;
         this.field_22787.method_1507(new FamilyTreeScreen(this.selectedVillager));
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   protected boolean isMouseWithin(int x, int y, int w, int h) {
      return this.mouseX >= x && this.mouseX < x + w && this.mouseY >= y && this.mouseY < y + h;
   }

   public void setVillage(Village village) {
      this.village = village;
      if (village == null) {
         this.setPage("empty");
      } else if (this.page.equals("waiting")) {
         this.setPage("map");
      }
   }

   public void setVillageData(Rank rank, int reputation, boolean isVillage, Set<String> completedTasks, Map<Rank, List<Task>> tasks) {
      this.rank = rank;
      this.reputation = reputation;
      this.isVillage = isVillage;
      this.completedTasks = completedTasks;
      this.tasks = tasks;
   }
}
