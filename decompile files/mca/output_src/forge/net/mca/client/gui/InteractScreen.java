package forge.net.mca.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.network.c2s.GetInteractDataRequest;
import forge.net.mca.network.c2s.InteractionCloseRequest;
import forge.net.mca.network.c2s.InteractionDialogueInitMessage;
import forge.net.mca.network.c2s.InteractionDialogueMessage;
import forge.net.mca.network.c2s.InteractionVillagerMessage;
import forge.net.mca.resources.data.analysis.Analysis;
import forge.net.mca.resources.data.dialogue.Question;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

public class InteractScreen extends AbstractDynamicScreen {
   public static final ResourceLocation ICON_TEXTURES = MCA.locate("textures/gui.png");
   private final VillagerLike<?> villager;
   private final Player player = Objects.requireNonNull(Minecraft.m_91087_().f_91074_);
   private boolean inGiftMode;
   private int timeSinceLastClick;
   private String father;
   private String mother;
   private RelationshipState marriageState;
   private Component spouse;
   private List<String> dialogAnswers;
   private String dialogAnswerHover;
   private List<FormattedCharSequence> dialogQuestionText;
   private String dialogQuestionId;
   private static Analysis<?> analysis;

   public InteractScreen(VillagerLike<?> villager) {
      super(Component.m_237113_("Interact"));
      this.villager = villager;
   }

   public void setParents(String father, String mother) {
      this.father = father;
      this.mother = mother;
   }

   public void setSpouse(RelationshipState marriageState, String spouse) {
      this.marriageState = marriageState;
      this.spouse = spouse == null ? Component.m_237115_("gui.interact.label.parentUnknown") : Component.m_237113_(spouse);
   }

   public boolean m_7043_() {
      return false;
   }

   public void m_7379_() {
      Objects.requireNonNull(this.f_96541_).m_91152_(null);
      NetworkHandler.sendToServer(new InteractionCloseRequest(this.villager.asEntity().m_20148_()));
   }

   public void m_7856_() {
      NetworkHandler.sendToServer(new GetInteractDataRequest(this.villager.asEntity().m_20148_()));
   }

   public void m_86600_() {
      this.timeSinceLastClick++;
   }

   @Override
   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
      super.m_88315_(context, mouseX, mouseY, tickDelta);
      this.drawIcons(context);
      this.drawTextPopups(context);
   }

   public boolean m_6050_(double x, double y, double d) {
      if (d < 0.0) {
         this.player.m_150109_().f_35977_ = this.player.m_150109_().f_35977_ == 8 ? 0 : this.player.m_150109_().f_35977_ + 1;
      } else if (d > 0.0) {
         this.player.m_150109_().f_35977_ = this.player.m_150109_().f_35977_ == 0 ? 8 : this.player.m_150109_().f_35977_ - 1;
      }

      return super.m_6050_(x, y, d);
   }

   public boolean m_6375_(double posX, double posY, int button) {
      super.m_6375_(posX, posY, button);
      if (button == 0 && this.dialogAnswerHover != null && this.dialogQuestionText != null) {
         NetworkHandler.sendToServer(new InteractionDialogueMessage(this.villager.asEntity().m_20148_(), this.dialogQuestionId, this.dialogAnswerHover));
      }

      if (this.inGiftMode && button == 1) {
         NetworkHandler.sendToServer(new InteractionVillagerMessage("gui.button.gift", this.villager.asEntity().m_20148_()));
         return true;
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keyChar, int keyCode, int unknown) {
      if (keyChar == 256) {
         if (this.inGiftMode) {
            this.inGiftMode = false;
            this.setLayout("interact");
         } else {
            this.m_7379_();
         }

         return true;
      } else {
         return false;
      }
   }

   private void drawIcons(GuiGraphics context) {
      PoseStack matrices = context.m_280168_();
      Memories memory = this.villager.getVillagerBrain().getMemoriesForPlayer(this.player);
      matrices.m_85836_();
      matrices.m_85841_(1.5F, 1.5F, 1.5F);
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

      matrices.m_85849_();
   }

   private void drawTextPopups(GuiGraphics context) {
      int h = 17;
      if (this.inGiftMode) {
         context.m_280557_(this.f_96547_, Component.m_237115_("gui.interact.label.giveGift"), 10, 28);
      } else {
         context.m_280557_(this.f_96547_, this.villager.asEntity().m_7755_(), 10, 28);
      }

      context.m_280557_(
         this.f_96547_, (Component)(this.villager.asEntity().m_6162_() ? this.villager.getAgeState().getName() : this.villager.getProfessionText()), 10, 30 + h
      );
      VillagerBrain<?> brain = this.villager.getVillagerBrain();
      context.m_280557_(
         this.f_96547_,
         Component.m_237110_("gui.interact.label.mood", new Object[]{brain.getMood().getText()}).m_130940_(brain.getMood().getColor()),
         10,
         30 + h * 2
      );
      if (this.hoveringOverText(10, 30 + h * 3, 128)) {
         context.m_280557_(this.f_96547_, brain.getPersonality().getDescription(), 10, 30 + h * 3);
      } else {
         context.m_280557_(
            this.f_96547_,
            Component.m_237110_("gui.interact.label.personality", new Object[]{brain.getPersonality().getName()}).m_130940_(ChatFormatting.WHITE),
            10,
            30 + h * 3
         );
      }

      Set<Traits.Trait> traits = this.villager.getTraits().getTraits();
      if (traits.size() > 0) {
         if (this.hoveringOverText(10, 30 + h * 4, 128)) {
            List<Component> traitText = traits.stream().map(Traits.Trait::getDescription).collect(Collectors.toList());
            traitText.add(0, Component.m_237115_("traits.title"));
            context.m_280666_(this.f_96547_, traitText, 10, 30 + h * 4);
         } else {
            MutableComponent traitText = Component.m_237115_("traits.title");
            traits.stream().map(Traits.Trait::getName).forEach(tx -> {
               if (traitText.m_7360_().size() > 0) {
                  traitText.m_7220_(Component.m_237113_(", "));
               }

               traitText.m_7220_(tx);
            });
            context.m_280557_(this.f_96547_, traitText, 10, 30 + h * 4);
         }
      }

      if (this.hoveringOverIcon("redHeart")) {
         int hearts = brain.getMemoriesForPlayer(this.player).getHearts();
         this.drawHoveringIconText(context, Component.m_237113_(hearts + " hearts"), "redHeart");
      }

      if (this.marriageState != null && this.hoveringOverIcon("married") && this.villager instanceof CompassionateEntity) {
         String ms = this.marriageState.base().getIcon().toLowerCase(Locale.ENGLISH);
         this.drawHoveringIconText(context, Component.m_237110_("gui.interact.label." + ms, new Object[]{this.spouse}), "married");
      }

      if (this.canDrawParentsIcon() && this.hoveringOverIcon("parents")) {
         this.drawHoveringIconText(
            context,
            Component.m_237110_(
               "gui.interact.label.parents",
               new Object[]{
                  this.father == null ? Component.m_237115_("gui.interact.label.parentUnknown") : this.father,
                  this.mother == null ? Component.m_237115_("gui.interact.label.parentUnknown") : this.mother
               }
            ),
            "parents"
         );
      }

      if (this.canDrawGiftIcon() && this.hoveringOverIcon("gift")) {
         this.drawHoveringIconText(context, Component.m_237115_("gui.interact.label.gift"), "gift");
      }

      if (this.hoveringOverIcon("genes")) {
         List<Component> lines = new LinkedList<>();
         lines.add(Component.m_237113_("Genes"));

         for (Genetics.Gene gene : this.villager.getGenetics()) {
            String key = gene.getType().getTranslationKey();
            int value = (int)(gene.get() * 100.0F);
            lines.add(Component.m_237110_("gene.tooltip", new Object[]{Component.m_237115_(key), value}));
         }

         this.drawHoveringIconText(context, lines, "genes");
      }

      if (this.hoveringOverIcon("analysis") && analysis != null) {
         List<Component> lines = new LinkedList<>();
         lines.add(Component.m_237115_("analysis.title").m_130940_(ChatFormatting.GRAY));

         for (Analysis.AnalysisElement d : analysis) {
            lines.add(
               Component.m_237115_("analysis." + d.getKey())
                  .m_7220_(Component.m_237113_(": " + (d.isPositive() ? "+" : "") + d.getValue()))
                  .m_130940_(d.isPositive() ? ChatFormatting.GREEN : ChatFormatting.RED)
            );
         }

         String chance = analysis.getTotalAsString();
         lines.add(Component.m_237115_("analysis.total").m_130946_(": " + chance));
         this.drawHoveringIconText(context, lines, "analysis");
      }

      if (this.dialogQuestionText != null) {
         context.m_280509_(
            this.f_96543_ / 2 - 85,
            this.f_96544_ / 2 - 50 - 10 * this.dialogQuestionText.size(),
            this.f_96543_ / 2 + 85,
            this.f_96544_ / 2 - 30 + 10 * this.dialogAnswers.size(),
            1996488704
         );
         int i = -this.dialogQuestionText.size();

         for (FormattedCharSequence t : this.dialogQuestionText) {
            i++;
            context.m_280648_(this.f_96547_, t, this.f_96543_ / 2 - this.f_96547_.m_92724_(t) / 2, this.f_96544_ / 2 - 50 + i * 10, -1);
         }

         this.dialogAnswerHover = null;
         context.m_280656_(this.f_96543_ / 2 - 75, this.f_96543_ / 2 + 75, this.f_96544_ / 2 - 40, -1426063361);
         int y = this.f_96544_ / 2 - 35;

         for (String a : this.dialogAnswers) {
            boolean hover = this.hoveringOver(this.f_96543_ / 2 - 100, y - 3, 200, 10);
            context.m_280653_(
               this.f_96547_, Component.m_237115_(Question.getTranslationKey(this.dialogQuestionId, a)), this.f_96543_ / 2, y, hover ? -2631804 : -1426063361
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

   public void setLastPhrase(MutableComponent questionText, boolean silent) {
      MutableComponent text;
      if (!silent) {
         text = this.villager.sendChatMessage(questionText, this.player);
      } else {
         text = this.villager.transformMessage(questionText);
      }

      this.dialogQuestionText = this.f_96547_.m_92923_(text, 160);
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
            Minecraft.m_91087_().m_91152_(new FamilyTreeScreen(this.villager.asEntity().m_20148_()));
         } else if (id.equals("gui.button.talk")) {
            this.m_169413_();
            NetworkHandler.sendToServer(new InteractionDialogueInitMessage(this.villager.asEntity().m_20148_()));
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
               NetworkHandler.sendToServer(new InteractionVillagerMessage(id, this.villager.asEntity().m_20148_()));
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
