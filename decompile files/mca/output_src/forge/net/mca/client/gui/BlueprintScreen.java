package forge.net.mca.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCA;
import forge.net.mca.client.gui.widget.TooltipButtonWidget;
import forge.net.mca.client.gui.widget.WidgetUtils;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.c2s.GetVillageRequest;
import forge.net.mca.network.c2s.RenameVillageMessage;
import forge.net.mca.network.c2s.ReportBuildingMessage;
import forge.net.mca.network.c2s.SaveVillageMessage;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.data.BuildingType;
import forge.net.mca.resources.data.tasks.Task;
import forge.net.mca.server.world.data.Building;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.util.compat.ButtonWidget;
import forge.net.mca.util.localization.FlowingText;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

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
   private final List<net.minecraft.client.gui.components.Button> catalogButtons = new LinkedList<>();
   private static final ResourceLocation ICON_TEXTURES = MCA.locate("textures/buildings.png");
   private BuildingType selectedBuilding;
   private UUID selectedVillager;
   private int mouseX;
   private int mouseY;
   private Map<Rank, List<Task>> tasks;

   public BlueprintScreen() {
      super(Component.m_237113_("Blueprint"));
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

   private ButtonWidget[] createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress, Component tooltip) {
      ButtonWidget[] buttons = new ButtonWidget[]{
         null,
         (ButtonWidget)this.m_142416_(new ButtonWidget(x - w / 2, y, w / 4, h, Component.m_237113_("<<"), b -> onPress.accept(false))),
         (ButtonWidget)this.m_142416_(new ButtonWidget(x + w / 4, y, w / 4, h, Component.m_237113_(">>"), b -> onPress.accept(true)))
      };
      buttons[0] = (ButtonWidget)this.m_142416_(new ButtonWidget(x - w / 4, y, w / 2, h, Component.m_237113_(""), b -> {}, tooltip));
      return buttons;
   }

   protected void drawBuildingIcon(GuiGraphics context, ResourceLocation texture, int x, int y, int u, int v) {
      PoseStack matrices = context.m_280168_();
      matrices.m_85836_();
      matrices.m_85837_(x - 6.6, y - 6.6, 0.0);
      matrices.m_85841_(0.66F, 0.66F, 0.66F);
      context.m_280218_(texture, 0, 0, u, v, 20, 20);
      matrices.m_85849_();
   }

   public void m_7856_() {
      NetworkHandler.sendToServer(new GetVillageRequest());
      this.setPage("waiting");
   }

   private void setPage(String page) {
      if (page.equals("close")) {
         assert this.f_96541_ != null;
         this.f_96541_.m_91152_(null);
      } else {
         this.page = page;
         this.m_169413_();
         this.m_142416_(new ButtonWidget(5, 5, 20, 20, Component.m_237115_("gui.button.backarrow"), b -> this.setPage("close")));
         int bx = this.f_96543_ / 2 - 180;
         int by = this.f_96544_ / 2 - 56;
         if (!page.equals("rename") && !page.equals("empty") && !page.equals("waiting")) {
            for (String p : new String[]{"map", "rank", "catalog", "villagers", "rules", "refresh"}) {
               ButtonWidget widget = new ButtonWidget(bx, by, 80, 20, Component.m_237115_("gui.blueprint." + p), b -> this.setPage(p));
               this.m_142416_(widget);
               if (page.equals(p)) {
                  widget.f_93623_ = false;
               }

               by += 22;
            }
         }

         switch (page) {
            case "empty":
               bx = this.f_96543_ / 2 - 48;
               by = this.f_96544_ / 2;
               this.m_142416_(new TooltipButtonWidget(bx - 50, by + 5, 96, 20, "gui.blueprint.addRoom", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD_ROOM));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.m_7379_();
               }));
               this.m_142416_(new TooltipButtonWidget(bx + 50, by + 5, 96, 20, "gui.blueprint.addBuilding", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.m_7379_();
               }));
               break;
            case "refresh":
               NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.FULL_SCAN));
               NetworkHandler.sendToServer(new GetVillageRequest());
               assert this.f_96541_ != null;
               assert this.f_96541_.f_91074_ != null;
               this.f_96541_.f_91074_.m_5661_(Component.m_237115_("blueprint.refreshed"), true);
               this.setPage("map");
               break;
            case "advanced":
               bx = this.f_96543_ / 2 + 180 - 64 - 16;
               by = this.f_96544_ / 2 - 56;
               MutableComponent text = Component.m_237115_("gui.blueprint.autoScan");
               if (this.village.isAutoScan()) {
                  text.m_130940_(ChatFormatting.GREEN);
               } else {
                  text.m_130940_(ChatFormatting.GRAY).m_130940_(ChatFormatting.STRIKETHROUGH);
               }

               this.m_142416_(new TooltipButtonWidget(bx, by, 96, 20, text, Component.m_237115_("gui.blueprint.autoScan.tooltip"), b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.AUTO_SCAN));
                  NetworkHandler.sendToServer(new GetVillageRequest());
                  this.village.toggleAutoScan();
                  this.setPage(page);
               }));
               by += 22;
               this.m_142416_(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.restrictAccess", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.FORCE_TYPE, "blocked"));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               this.m_142416_(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.addBuilding", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 66;
               if (this.isVillage) {
                  this.m_142416_(new ButtonWidget(bx, by, 96, 20, Component.m_237115_("gui.blueprint.renameVillage"), b -> this.setPage("rename")));
               }
            case "map":
               bx = this.f_96543_ / 2 + 180 - 64 - 16;
               by = this.f_96544_ / 2 - 56 + 66;
               this.m_142416_(new TooltipButtonWidget(bx, by, 96, 20, "gui.blueprint.addRoom", b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD_ROOM));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               this.m_142416_(new ButtonWidget(bx, by, 96, 20, Component.m_237115_("gui.blueprint.removeBuilding"), b -> {
                  NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.REMOVE));
                  NetworkHandler.sendToServer(new GetVillageRequest());
               }));
               by += 22;
               if (!page.equals("advanced")) {
                  this.m_142416_(new ButtonWidget(bx, by, 96, 20, Component.m_237115_("gui.blueprint.advanced"), b -> this.setPage("advanced")));
               }
            case "rank":
            default:
               break;
            case "catalog":
               int row = 0;
               int col = 0;
               int size = 21;
               int x = this.f_96543_ / 2 - 4 * size - 8;
               int y = (int)(this.f_96544_ / 2 - 2.0 * size);
               this.catalogButtons.clear();

               for (BuildingType bt : BuildingTypes.getInstance()) {
                  if (bt.visible()) {
                     ImageButton widget = new ImageButton(
                        row * size + x + 10, col * size + y - 10, 20, 20, bt.iconU(), bt.iconV() + 20, 20, ICON_TEXTURES, 256, 256, button -> {
                           this.selectBuilding(bt);
                           button.f_93623_ = false;
                           this.catalogButtons.forEach(b -> b.f_93623_ = true);
                        }, Component.m_237115_("buildingType." + bt.name())
                     );
                     this.catalogButtons.add((net.minecraft.client.gui.components.Button)this.m_142416_(widget));
                     if (++row > 4) {
                        row = 0;
                        col++;
                     }
                  }
               }
               break;
            case "villagers":
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 24 - 20, this.f_96544_ / 2 + 54, 20, 20, Component.m_237113_("<"), b -> {
                  if (this.pageNumber > 0) {
                     this.pageNumber--;
                  }
               }));
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 24, this.f_96544_ / 2 + 54, 20, 20, Component.m_237113_(">"), b -> {
                  if (this.pageNumber < Math.ceil(this.village.getPopulation() / 9.0) - 1.0) {
                     this.pageNumber++;
                  }
               }));
               this.buttonPage = (ButtonWidget)this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 - 24, this.f_96544_ / 2 + 54, 48, 20, Component.m_237113_("0/0)"), b -> {})
               );
               break;
            case "rules":
               this.buttonTaxes = this.createValueChanger(
                  this.f_96543_ / 2,
                  this.f_96544_ / 2 + -60 + 10,
                  80,
                  20,
                  b -> this.changeTaxes(b ? 0.125F : -0.125F),
                  Component.m_237115_("gui.blueprint.tooltip.taxes")
               );
               this.toggleButtons(this.buttonTaxes, false);
               this.buttonBirths = this.createValueChanger(
                  this.f_96543_ / 2,
                  this.f_96544_ / 2 + -10 + 10,
                  80,
                  20,
                  b -> this.changePopulationThreshold(b ? 0.125F : -0.125F),
                  Component.m_237115_("gui.blueprint.tooltip.births")
               );
               this.toggleButtons(this.buttonBirths, false);
               this.buttonMarriage = this.createValueChanger(
                  this.f_96543_ / 2,
                  this.f_96544_ / 2 + 40 + 10,
                  80,
                  20,
                  b -> this.changeMarriageThreshold(b ? 0.125F : -0.125F),
                  Component.m_237115_("gui.blueprint.tooltip.marriage")
               );
               this.toggleButtons(this.buttonMarriage, false);
               break;
            case "rename":
               EditBox field = (EditBox)this.m_142416_(
                  new EditBox(this.f_96547_, this.f_96543_ / 2 - 65, this.f_96544_ / 2 - 16, 130, 20, Component.m_237115_("gui.blueprint.renameVillage"))
               );
               field.m_94199_(32);
               field.m_94144_(this.village.getName());
               this.m_142416_(
                  new ButtonWidget(this.f_96543_ / 2 - 66, this.f_96544_ / 2 + 8, 64, 20, Component.m_237115_("gui.blueprint.cancel"), b -> this.setPage("map"))
               );
               this.m_142416_(new ButtonWidget(this.f_96543_ / 2 + 2, this.f_96544_ / 2 + 8, 64, 20, Component.m_237115_("gui.blueprint.rename"), b -> {
                  NetworkHandler.sendToServer(new RenameVillageMessage(this.village.getId(), field.m_94155_()));
                  this.village.setName(field.m_94155_());
                  this.setPage("map");
               }));
         }
      }
   }

   private void selectBuilding(BuildingType b) {
      this.selectedBuilding = b;
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_88315_(GuiGraphics context, int sizeX, int sizeY, float offset) {
      this.m_280273_(context);
      assert this.f_96541_ != null;
      this.mouseX = (int)(this.f_96541_.f_91067_.m_91589_() * this.f_96543_ / this.f_96541_.m_91268_().m_85441_());
      this.mouseY = (int)(this.f_96541_.f_91067_.m_91594_() * this.f_96544_ / this.f_96541_.m_91268_().m_85442_());
      switch (this.page) {
         case "waiting":
            context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.waiting"), this.f_96543_ / 2, this.f_96544_ / 2, -5592406);
            break;
         case "empty":
            context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.empty"), this.f_96543_ / 2, this.f_96544_ / 2 - 20, -5592406);
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

      super.m_88315_(context, sizeX, sizeY, offset);
   }

   private void renderName(GuiGraphics context) {
      PoseStack matrices = context.m_280168_();
      matrices.m_85836_();
      matrices.m_85841_(2.0F, 2.0F, 2.0F);
      if (this.isVillage) {
         context.m_280137_(this.f_96547_, this.village.getName(), this.f_96543_ / 4, this.f_96544_ / 4 - 48, -1);
      } else {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.settlement"), this.f_96543_ / 4, this.f_96544_ / 4 - 48, -1);
      }

      matrices.m_85849_();
   }

   private void renderStats(GuiGraphics context) {
      int x = this.f_96543_ / 2 + (this.page.equals("rank") ? -70 : 105);
      int y = this.f_96544_ / 2 - 50;
      Component rankStr = Component.m_237115_(this.rank.getTranslationKey());
      int rankColor = this.rank.ordinal() == 0 ? -65536 : -256;
      context.m_280430_(this.f_96547_, Component.m_237110_("gui.blueprint.currentRank", new Object[]{rankStr}), x, y, rankColor);
      context.m_280430_(
         this.f_96547_,
         Component.m_237110_("gui.blueprint.reputation", new Object[]{String.valueOf(this.reputation)}),
         x,
         y + 11,
         this.rank.ordinal() == 0 ? -65536 : -1
      );
      context.m_280430_(this.f_96547_, Component.m_237110_("gui.blueprint.buildings", new Object[]{this.village.getBuildings().size()}), x, y + 22, -1);
      context.m_280430_(
         this.f_96547_,
         Component.m_237110_("gui.blueprint.population", new Object[]{this.village.getPopulation(), this.village.getMaxPopulation()}),
         x,
         y + 33,
         -1
      );
   }

   private void renderMap(GuiGraphics context) {
      PoseStack matrices = context.m_280168_();
      int mapSize = 75;
      int y = this.f_96544_ / 2 + 8;
      WidgetUtils.drawRectangle(context, this.f_96543_ / 2 - mapSize, y - mapSize, this.f_96543_ / 2 + mapSize, y + mapSize, -120);
      if (!this.village.isAutoScan() && this.village.getBuildings().size() <= 1) {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.autoScanDisabled"), this.f_96543_ / 2, this.f_96544_ / 2 + 90, -1426063361);
      }

      matrices.m_85836_();
      float sc = Math.min((float)mapSize / (this.village.getBox().getMaxBlockCount() + 3) * 2.0F, 2.0F);
      int mouseLocalX = (int)((this.mouseX - this.f_96543_ / 2.0) / sc + this.village.getCenter().m_123341_());
      int mouseLocalY = (int)((this.mouseY - y) / sc + this.village.getCenter().m_123343_());
      matrices.m_85837_(this.f_96543_ / 2.0, y, 0.0);
      matrices.m_85841_(sc, sc, 0.0F);
      matrices.m_252880_(-this.village.getCenter().m_123341_(), -this.village.getCenter().m_123343_(), 0.0F);
      assert this.f_96541_ != null;
      LocalPlayer player = this.f_96541_.f_91074_;
      if (player != null) {
         WidgetUtils.drawRectangle(
            context, (int)player.m_20185_() - 1, (int)player.m_20189_() - 1, (int)player.m_20185_() + 1, (int)player.m_20189_() + 1, -65281
         );
      }

      List<Building> hoverBuildings = new LinkedList<>();

      for (Building building : this.village.getBuildings().values()) {
         if (building.isComplete()) {
            BuildingType bt = building.getBuildingType();
            if (bt.isIcon()) {
               BlockPos c = building.getCenter();
               this.drawBuildingIcon(context, ICON_TEXTURES, c.m_123341_(), c.m_123343_(), bt.iconU(), bt.iconV());
               int margin = 6;
               if (c.m_123331_(new Vec3i(mouseLocalX, c.m_123342_(), mouseLocalY)) < margin * margin) {
                  hoverBuildings.add(building);
               }
            } else {
               BlockPos p0 = building.getPos0();
               BlockPos p1 = building.getPos1();
               WidgetUtils.drawRectangle(context, p0.m_123341_(), p0.m_123343_(), p1.m_123341_(), p1.m_123343_(), bt.getColor());
               if (bt.visible()) {
                  BlockPos c = building.getCenter();
                  this.drawBuildingIcon(context, ICON_TEXTURES, c.m_123341_(), c.m_123343_(), bt.iconU(), bt.iconV());
               }

               int margin = 1;
               if (mouseLocalX >= p0.m_123341_() - margin
                  && mouseLocalX <= p1.m_123341_() + margin
                  && mouseLocalY >= p0.m_123343_() - margin
                  && mouseLocalY <= p1.m_123343_() + margin) {
                  hoverBuildings.add(building);
               }
            }
         }
      }

      matrices.m_85849_();
      hoverBuildings.sort((a, bx) -> bx.getCenter().m_123342_() - a.getCenter().m_123342_());
      List<List<Component>> tooltips = new LinkedList<>();

      for (Building b : hoverBuildings) {
         tooltips.add(this.getBuildingTooltip(b));
      }

      int h = 0;

      for (List<Component> b : tooltips) {
         h += this.getTooltipHeight(b) + 9;
      }

      int py = this.mouseY - h / 2 + 12;

      for (List<Component> b : tooltips) {
         context.m_280666_(this.f_96547_, b, this.mouseX, py);
         py += this.getTooltipHeight(b) + 9;
      }
   }

   private List<Component> getBuildingTooltip(Building hoverBuilding) {
      List<Component> lines = new LinkedList<>();
      BuildingType bt = BuildingTypes.getInstance().getBuildingType(hoverBuilding.getType());
      lines.add(Component.m_237115_("buildingType." + bt.name()));

      for (String name : this.village.getResidents(hoverBuilding.getId())) {
         lines.add(Component.m_237113_(name));
      }

      for (Entry<ResourceLocation, List<BlockPos>> block : hoverBuilding.getBlocks().entrySet()) {
         lines.add(Component.m_237113_(block.getValue().size() + " x ").m_7220_(this.getBlockName(block.getKey())).m_130940_(ChatFormatting.GRAY));
      }

      return lines;
   }

   private void renderTasks(GuiGraphics context) {
      if (this.rank != null) {
         int y = this.f_96544_ / 2 + 5;
         int x = this.f_96543_ / 2 - 70;

         for (Task task : this.tasks.get(this.rank.promote())) {
            boolean completed = this.completedTasks.contains(task.getId());
            Component t = task.getTranslatable().m_130940_(completed ? ChatFormatting.STRIKETHROUGH : ChatFormatting.RESET);
            context.m_280430_(this.f_96547_, t, x, y, completed ? -7798904 : -43691);
            y += 11;
         }
      }
   }

   private void renderCatalog(GuiGraphics context) {
      PoseStack matrices = context.m_280168_();
      matrices.m_85836_();
      matrices.m_85841_(2.0F, 2.0F, 2.0F);
      context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.catalogFull"), this.f_96543_ / 4, this.f_96544_ / 4 - 52, -1);
      matrices.m_85849_();
      context.m_280653_(
         this.f_96547_, Component.m_237115_("gui.blueprint.catalogHint").m_130940_(ChatFormatting.GRAY), this.f_96543_ / 2, this.f_96544_ / 2 - 82, -1
      );
      int x = this.f_96543_ / 2 + 35;
      int y = this.f_96544_ / 2 - 50;
      if (this.selectedBuilding != null) {
         context.m_280430_(this.f_96547_, Component.m_237115_("buildingType." + this.selectedBuilding.name()), x, y, this.selectedBuilding.getColor());
         y += 12;

         for (Component t : FlowingText.wrap(
            Component.m_237115_("buildingType." + this.selectedBuilding.name() + ".description")
               .m_130940_(ChatFormatting.GRAY)
               .m_130940_(ChatFormatting.ITALIC),
            150
         )) {
            context.m_280430_(this.f_96547_, t, x, y, -1);
            y += 10;
         }

         y += 24;

         for (Entry<ResourceLocation, Integer> b : this.selectedBuilding.getGroups().entrySet()) {
            context.m_280430_(this.f_96547_, Component.m_237113_(b.getValue() + " x ").m_7220_(this.getBlockName(b.getKey())), x, y, -1);
            y += 10;
         }
      } else {
         for (Component t : FlowingText.wrap(
            Component.m_237115_("gui.blueprint.buildingTypes").m_130940_(ChatFormatting.GRAY).m_130940_(ChatFormatting.ITALIC), 150
         )) {
            context.m_280430_(this.f_96547_, t, x, y, -1);
            y += 10;
         }
      }
   }

   private void renderVillagers(GuiGraphics context) {
      int maxPages = (int)Math.ceil(this.village.getPopulation() / 9.0);
      this.buttonPage.m_93666_(Component.m_237113_(this.pageNumber + 1 + "/" + maxPages));
      List<Entry<UUID, String>> villager = this.village.getResidentNames().entrySet().stream().sorted(Entry.comparingByValue()).toList();
      this.selectedVillager = null;

      for (int i = 0; i < 9; i++) {
         int index = i + this.pageNumber * 9;
         if (index >= villager.size()) {
            break;
         }

         int y = this.f_96544_ / 2 - 51 + i * 11;
         boolean hover = this.isMouseWithin(this.f_96543_ / 2 - 50, y - 1, 100, 11);
         context.m_280653_(this.f_96547_, Component.m_237113_(villager.get(index).getValue()), this.f_96543_ / 2, y, hover ? -2631804 : -1);
         if (hover) {
            this.selectedVillager = villager.get(index).getKey();
         }
      }
   }

   private void renderRules(GuiGraphics context) {
      this.buttonTaxes[0].m_93666_(Component.m_237113_((int)(this.village.getTaxes() * 100.0F) + "%"));
      this.buttonMarriage[0].m_93666_(Component.m_237113_((int)(this.village.getMarriageThreshold() * 100.0F) + "%"));
      this.buttonBirths[0].m_93666_(Component.m_237113_((int)(this.village.getPopulationThreshold() * 100.0F) + "%"));
      context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.taxes"), this.f_96543_ / 2, this.f_96544_ / 2 + -60, -1);
      if (!this.rank.isAtLeast(Rank.MERCHANT)) {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.rankTooLow"), this.f_96543_ / 2, this.f_96544_ / 2 + -60 + 15, -1);
         this.toggleButtons(this.buttonTaxes, false);
      } else {
         this.toggleButtons(this.buttonTaxes, true);
      }

      context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.birth"), this.f_96543_ / 2, this.f_96544_ / 2 + -10, -1);
      if (!this.rank.isAtLeast(Rank.NOBLE)) {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.rankTooLow"), this.f_96543_ / 2, this.f_96544_ / 2 + -10 + 15, -1);
         this.toggleButtons(this.buttonBirths, false);
      } else {
         this.toggleButtons(this.buttonBirths, true);
      }

      context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.marriage"), this.f_96543_ / 2, this.f_96544_ / 2 + 40, -1);
      if (!this.rank.isAtLeast(Rank.MAYOR)) {
         context.m_280653_(this.f_96547_, Component.m_237115_("gui.blueprint.rankTooLow"), this.f_96543_ / 2, this.f_96544_ / 2 + 40 + 15, -1);
         this.toggleButtons(this.buttonMarriage, false);
      } else {
         this.toggleButtons(this.buttonMarriage, true);
      }
   }

   private Component getBlockName(ResourceLocation id) {
      return BuiltInRegistries.f_256975_.m_7804_(id)
         ? Component.m_237115_(((Block)BuiltInRegistries.f_256975_.m_7745_(id)).m_7705_())
         : Component.m_237115_("tag." + id.toString());
   }

   private void toggleButtons(ButtonWidget[] buttons, boolean active) {
      for (ButtonWidget b : buttons) {
         b.f_93623_ = active;
         b.f_93624_ = active;
      }
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.page.equals("villagers") && this.selectedVillager != null) {
         assert this.f_96541_ != null;
         this.f_96541_.m_91152_(new FamilyTreeScreen(this.selectedVillager));
      }

      return super.m_6375_(mouseX, mouseY, button);
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
