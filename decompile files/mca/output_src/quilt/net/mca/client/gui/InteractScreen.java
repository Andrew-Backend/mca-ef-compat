package quilt.net.mca.client.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_5250;
import net.minecraft.class_5481;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.Genetics;
import quilt.net.mca.entity.ai.Memories;
import quilt.net.mca.entity.ai.Traits;
import quilt.net.mca.entity.ai.brain.VillagerBrain;
import quilt.net.mca.entity.ai.relationship.CompassionateEntity;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.network.c2s.GetInteractDataRequest;
import quilt.net.mca.network.c2s.InteractionCloseRequest;
import quilt.net.mca.network.c2s.InteractionDialogueInitMessage;
import quilt.net.mca.network.c2s.InteractionDialogueMessage;
import quilt.net.mca.network.c2s.InteractionVillagerMessage;
import quilt.net.mca.resources.data.analysis.Analysis;
import quilt.net.mca.resources.data.dialogue.Question;

public class InteractScreen extends AbstractDynamicScreen {
   public static final class_2960 ICON_TEXTURES = MCA.locate("textures/gui.png");
   private final VillagerLike<?> villager;
   private final class_1657 player = Objects.requireNonNull(class_310.method_1551().field_1724);
   private boolean inGiftMode;
   private int timeSinceLastClick;
   private String father;
   private String mother;
   private RelationshipState marriageState;
   private class_2561 spouse;
   private List<String> dialogAnswers;
   private String dialogAnswerHover;
   private List<class_5481> dialogQuestionText;
   private String dialogQuestionId;
   private static Analysis<?> analysis;

   public InteractScreen(VillagerLike<?> villager) {
      super(class_2561.method_43470("Interact"));
      this.villager = villager;
   }

   public void setParents(String father, String mother) {
      this.father = father;
      this.mother = mother;
   }

   public void setSpouse(RelationshipState marriageState, String spouse) {
      this.marriageState = marriageState;
      this.spouse = spouse == null ? class_2561.method_43471("gui.interact.label.parentUnknown") : class_2561.method_43470(spouse);
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25419() {
      Objects.requireNonNull(this.field_22787).method_1507(null);
      NetworkHandler.sendToServer(new InteractionCloseRequest(this.villager.asEntity().method_5667()));
   }

   public void method_25426() {
      NetworkHandler.sendToServer(new GetInteractDataRequest(this.villager.asEntity().method_5667()));
   }

   public void method_25393() {
      this.timeSinceLastClick++;
   }

   @Override
   public void method_25394(class_332 context, int mouseX, int mouseY, float tickDelta) {
      super.method_25394(context, mouseX, mouseY, tickDelta);
      this.drawIcons(context);
      this.drawTextPopups(context);
   }

   public boolean method_25401(double x, double y, double d) {
      if (d < 0.0) {
         this.player.method_31548().field_7545 = this.player.method_31548().field_7545 == 8 ? 0 : this.player.method_31548().field_7545 + 1;
      } else if (d > 0.0) {
         this.player.method_31548().field_7545 = this.player.method_31548().field_7545 == 0 ? 8 : this.player.method_31548().field_7545 - 1;
      }

      return super.method_25401(x, y, d);
   }

   public boolean method_25402(double posX, double posY, int button) {
      super.method_25402(posX, posY, button);
      if (button == 0 && this.dialogAnswerHover != null && this.dialogQuestionText != null) {
         NetworkHandler.sendToServer(new InteractionDialogueMessage(this.villager.asEntity().method_5667(), this.dialogQuestionId, this.dialogAnswerHover));
      }

      if (this.inGiftMode && button == 1) {
         NetworkHandler.sendToServer(new InteractionVillagerMessage("gui.button.gift", this.villager.asEntity().method_5667()));
         return true;
      } else {
         return false;
      }
   }

   public boolean method_25404(int keyChar, int keyCode, int unknown) {
      if (keyChar == 256) {
         if (this.inGiftMode) {
            this.inGiftMode = false;
            this.setLayout("interact");
         } else {
            this.method_25419();
         }

         return true;
      } else {
         return false;
      }
   }

   private void drawIcons(class_332 context) {
      class_4587 matrices = context.method_51448();
      Memories memory = this.villager.getVillagerBrain().getMemoriesForPlayer(this.player);
      matrices.method_22903();
      matrices.method_22905(1.5F, 1.5F, 1.5F);
      if (this.marriageState != null) {
         this.drawIcon(context, ICON_TEXTURES, this.marriageState.getIcon());
      }

      this.drawIcon(context, ICON_TEXTURES, memory.getHearts() < 0 ? "blackHeart" : (memory.getHearts() >= 100 ? "goldHeart" : "redHeart"));
      this.drawIcon(context, ICON_TEXTURES, "genes");
      if (this.canDrawParentsIcon()) {
         this.drawIcon(context, ICON_TEXTURES, "parents");
      }

      if (this.canDrawGiftIcon()) {
         this.drawIcon(context, ICON_TEXTURES, "gift");
      }

      if (analysis != null) {
         this.drawIcon(context, ICON_TEXTURES, "analysis");
      }

      matrices.method_22909();
   }

   private void drawTextPopups(class_332 context) {
      int h = 17;
      if (this.inGiftMode) {
         context.method_51438(this.field_22793, class_2561.method_43471("gui.interact.label.giveGift"), 10, 28);
      } else {
         context.method_51438(this.field_22793, this.villager.asEntity().method_5477(), 10, 28);
      }

      context.method_51438(
         this.field_22793,
         (class_2561)(this.villager.asEntity().method_6109() ? this.villager.getAgeState().getName() : this.villager.getProfessionText()),
         10,
         30 + h
      );
      VillagerBrain<?> brain = this.villager.getVillagerBrain();
      context.method_51438(
         this.field_22793,
         class_2561.method_43469("gui.interact.label.mood", new Object[]{brain.getMood().getText()}).method_27692(brain.getMood().getColor()),
         10,
         30 + h * 2
      );
      if (this.hoveringOverText(10, 30 + h * 3, 128)) {
         context.method_51438(this.field_22793, brain.getPersonality().getDescription(), 10, 30 + h * 3);
      } else {
         context.method_51438(
            this.field_22793,
            class_2561.method_43469("gui.interact.label.personality", new Object[]{brain.getPersonality().getName()}).method_27692(class_124.field_1068),
            10,
            30 + h * 3
         );
      }

      Set<Traits.Trait> traits = this.villager.getTraits().getTraits();
      if (traits.size() > 0) {
         if (this.hoveringOverText(10, 30 + h * 4, 128)) {
            List<class_2561> traitText = traits.stream().map(Traits.Trait::getDescription).collect(Collectors.toList());
            traitText.add(0, class_2561.method_43471("traits.title"));
            context.method_51434(this.field_22793, traitText, 10, 30 + h * 4);
         } else {
            class_5250 traitText = class_2561.method_43471("traits.title");
            traits.stream().map(Traits.Trait::getName).forEach(tx -> {
               if (traitText.method_10855().size() > 0) {
                  traitText.method_10852(class_2561.method_43470(", "));
               }

               traitText.method_10852(tx);
            });
            context.method_51438(this.field_22793, traitText, 10, 30 + h * 4);
         }
      }

      if (this.hoveringOverIcon("redHeart")) {
         int hearts = brain.getMemoriesForPlayer(this.player).getHearts();
         this.drawHoveringIconText(context, class_2561.method_43470(hearts + " hearts"), "redHeart");
      }

      if (this.marriageState != null && this.hoveringOverIcon("married") && this.villager instanceof CompassionateEntity) {
         String ms = this.marriageState.base().getIcon().toLowerCase(Locale.ENGLISH);
         this.drawHoveringIconText(context, class_2561.method_43469("gui.interact.label." + ms, new Object[]{this.spouse}), "married");
      }

      if (this.canDrawParentsIcon() && this.hoveringOverIcon("parents")) {
         this.drawHoveringIconText(
            context,
            class_2561.method_43469(
               "gui.interact.label.parents",
               new Object[]{
                  this.father == null ? class_2561.method_43471("gui.interact.label.parentUnknown") : this.father,
                  this.mother == null ? class_2561.method_43471("gui.interact.label.parentUnknown") : this.mother
               }
            ),
            "parents"
         );
      }

      if (this.canDrawGiftIcon() && this.hoveringOverIcon("gift")) {
         this.drawHoveringIconText(context, class_2561.method_43471("gui.interact.label.gift"), "gift");
      }

      if (this.hoveringOverIcon("genes")) {
         List<class_2561> lines = new LinkedList<>();
         lines.add(class_2561.method_43470("Genes"));

         for (Genetics.Gene gene : this.villager.getGenetics()) {
            String key = gene.getType().getTranslationKey();
            int value = (int)(gene.get() * 100.0F);
            lines.add(class_2561.method_43469("gene.tooltip", new Object[]{class_2561.method_43471(key), value}));
         }

         this.drawHoveringIconText(context, lines, "genes");
      }

      if (this.hoveringOverIcon("analysis") && analysis != null) {
         List<class_2561> lines = new LinkedList<>();
         lines.add(class_2561.method_43471("analysis.title").method_27692(class_124.field_1080));

         for (Analysis.AnalysisElement d : analysis) {
            lines.add(
               class_2561.method_43471("analysis." + d.getKey())
                  .method_10852(class_2561.method_43470(": " + (d.isPositive() ? "+" : "") + d.getValue()))
                  .method_27692(d.isPositive() ? class_124.field_1060 : class_124.field_1061)
            );
         }

         String chance = analysis.getTotalAsString();
         lines.add(class_2561.method_43471("analysis.total").method_27693(": " + chance));
         this.drawHoveringIconText(context, lines, "analysis");
      }

      if (this.dialogQuestionText != null) {
         context.method_25294(
            this.field_22789 / 2 - 85,
            this.field_22790 / 2 - 50 - 10 * this.dialogQuestionText.size(),
            this.field_22789 / 2 + 85,
            this.field_22790 / 2 - 30 + 10 * this.dialogAnswers.size(),
            1996488704
         );
         int i = -this.dialogQuestionText.size();

         for (class_5481 t : this.dialogQuestionText) {
            i++;
            context.method_35720(this.field_22793, t, this.field_22789 / 2 - this.field_22793.method_30880(t) / 2, this.field_22790 / 2 - 50 + i * 10, -1);
         }

         this.dialogAnswerHover = null;
         context.method_25292(this.field_22789 / 2 - 75, this.field_22789 / 2 + 75, this.field_22790 / 2 - 40, -1426063361);
         int y = this.field_22790 / 2 - 35;

         for (String a : this.dialogAnswers) {
            boolean hover = this.hoveringOver(this.field_22789 / 2 - 100, y - 3, 200, 10);
            context.method_27534(
               this.field_22793,
               class_2561.method_43471(Question.getTranslationKey(this.dialogQuestionId, a)),
               this.field_22789 / 2,
               y,
               hover ? -2631804 : -1426063361
            );
            if (hover) {
               this.dialogAnswerHover = a;
            }

            y += 10;
         }
      }
   }

   private boolean hoveringOverText(int x, int y, int w) {
      return this.hoveringOver(x + 8, y - 16, w, 16);
   }

   private boolean canDrawParentsIcon() {
      return this.father != null || this.mother != null;
   }

   private boolean canDrawGiftIcon() {
      return false;
   }

   public void setDialogue(String dialogue, List<String> answers) {
      this.dialogQuestionId = dialogue;
      this.dialogAnswers = answers;
   }

   public void setLastPhrase(class_5250 questionText, boolean silent) {
      class_5250 text;
      if (!silent) {
         text = this.villager.sendChatMessage(questionText, this.player);
      } else {
         text = this.villager.transformMessage(questionText);
      }

      this.dialogQuestionText = this.field_22793.method_1728(text, 160);
   }

   @Override
   protected void buttonPressed(Button button) {
      String id = button.identifier();
      if (this.timeSinceLastClick > 2) {
         this.timeSinceLastClick = 0;
         if (id.equals("gui.button.interact")) {
            this.setLayout("interact");
         } else if (id.equals("gui.button.command")) {
            this.setLayout("command");
            this.disableButton("gui.button." + this.villager.getVillagerBrain().getMoveState().name().toLowerCase(Locale.ENGLISH));
         } else if (id.equals("gui.button.clothing")) {
            this.setLayout("clothing");
         } else if (id.equals("gui.button.familyTree")) {
            class_310.method_1551().method_1507(new FamilyTreeScreen(this.villager.asEntity().method_5667()));
         } else if (id.equals("gui.button.talk")) {
            this.method_37067();
            NetworkHandler.sendToServer(new InteractionDialogueInitMessage(this.villager.asEntity().method_5667()));
         } else if (id.equals("gui.button.work")) {
            this.setLayout("work");
            this.disableButton("gui.button." + this.villager.getVillagerBrain().getCurrentJob().name().toLowerCase(Locale.ENGLISH));
         } else if (id.equals("gui.button.professions")) {
            this.setLayout("professions");
         } else if (id.equals("gui.button.backarrow")) {
            if (this.inGiftMode) {
               this.inGiftMode = false;
               this.setLayout("interact");
            } else if (this.getActiveScreen().equals("locations")) {
               this.setLayout("interact");
            } else {
               this.setLayout("main");
            }
         } else if (id.equals("gui.button.locations")) {
            this.setLayout("locations");
         } else if (button.notifyServer()) {
            if (!button.targetServer()) {
               NetworkHandler.sendToServer(new InteractionVillagerMessage(id, this.villager.asEntity().method_5667()));
            }
         } else if (id.equals("gui.button.gift")) {
            this.inGiftMode = true;
            this.disableAllButtons();
         }
      }
   }

   public static void setAnalysis(Analysis<?> analysis) {
      InteractScreen.analysis = analysis;
   }
}
