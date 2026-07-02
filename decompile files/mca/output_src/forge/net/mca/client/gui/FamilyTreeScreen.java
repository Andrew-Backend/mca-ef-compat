package forge.net.mca.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import forge.net.mca.MCA;
import forge.net.mca.client.resources.Icon;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.network.c2s.GetFamilyTreeRequest;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.util.compat.ButtonWidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class FamilyTreeScreen extends Screen {
   private static final int HORIZONTAL_SPACING = 20;
   private static final int VERTICAL_SPACING = 60;
   private static final int SPOUSE_HORIZONTAL_SPACING = 50;
   private UUID focusedEntityId;
   private final Map<UUID, FamilyTreeNode> family = new HashMap<>();
   private final FamilyTreeScreen.TreeNode emptyNode = new FamilyTreeScreen.TreeNode();
   private FamilyTreeScreen.TreeNode tree = this.emptyNode;
   @Nullable
   private FamilyTreeScreen.TreeNode focused;
   private double scrollX;
   private double scrollY;
   private final Screen parent;

   public FamilyTreeScreen(UUID entityId) {
      super(Component.m_237115_("gui.family_tree.title"));
      this.focusedEntityId = entityId;
      this.parent = Minecraft.m_91087_().f_91080_;
   }

   public boolean m_7043_() {
      return false;
   }

   public void setFamilyData(UUID uuid, Map<UUID, FamilyTreeNode> family) {
      this.focusedEntityId = uuid;
      this.family.putAll(family);
      this.rebuildTree();
   }

   private boolean focusEntity(UUID id) {
      this.focusedEntityId = id;
      NetworkHandler.sendToServer(new GetFamilyTreeRequest(id));
      return false;
   }

   public void m_7856_() {
      this.focusEntity(this.focusedEntityId);
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 100, this.f_96544_ - 25, 200, 20, Component.m_237115_("gui.done"), sender -> this.m_7379_()));
   }

   public void m_7379_() {
      assert this.f_96541_ != null;
      this.f_96541_.m_91152_(this.parent);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0) {
         this.scrollX += deltaX;
         this.scrollY += deltaY;
         return true;
      } else {
         return super.m_7979_(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (button == 0 && this.focused != null) {
         Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_263171_(SoundEvents.f_12490_, 1.0F));
         if (this.focusEntity(this.focused.id)) {
            this.rebuildTree();
         }

         return true;
      } else {
         return super.m_6375_(mouseX, mouseY, button);
      }
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.m_280273_(context);
      context.m_280509_(0, 30, this.f_96543_, this.f_96544_ - 30, 1711276032);
      this.focused = null;
      Window window = Minecraft.m_91087_().m_91268_();
      double f = window.m_85449_();
      int windowHeight = (int)Math.round(window.m_85446_() * f);
      int x = 0;
      int y = (int)(30.0 * f);
      int w = (int)(this.f_96543_ * f);
      int h = (int)((this.f_96544_ - 60) * f);
      GL11.glScissor(x, windowHeight - h - y, w, h);
      GL11.glEnable(3089);
      PoseStack matrices = context.m_280168_();
      matrices.m_85836_();
      int xx = (int)(this.scrollX + this.f_96543_ / 2);
      int yy = (int)(this.scrollY + this.f_96544_ / 2);
      matrices.m_252880_(xx, yy, 0.0F);
      this.tree.render(context, mouseX - xx, mouseY - yy);
      matrices.m_85849_();
      GL11.glDisable(3089);
      FamilyTreeNode selected = this.family.get(this.focusedEntityId);
      Component label = (Component)(selected == null ? this.f_96539_ : Component.m_237113_(selected.getName()).m_130946_("'s ").m_7220_(this.f_96539_));
      context.m_280653_(this.f_96547_, label, this.f_96543_ / 2, 10, 16777215);
      super.m_88315_(context, mouseX, mouseY, delta);
   }

   private void rebuildTree() {
      this.scrollX = 14.0;
      this.scrollY = -69.0;
      FamilyTreeNode focusedNode = this.family.get(this.focusedEntityId);
      this.focused = null;
      this.tree = this.emptyNode;
      if (focusedNode != null) {
         this.tree = this.insertParents(new FamilyTreeScreen.TreeNode(focusedNode, true), focusedNode, 2);
      }
   }

   private FamilyTreeScreen.TreeNode insertParents(FamilyTreeScreen.TreeNode root, FamilyTreeNode focusedNode, int levels) {
      FamilyTreeNode father = this.family.get(focusedNode.father());
      FamilyTreeNode mother = this.family.get(focusedNode.mother());
      FamilyTreeNode newRoot = father != null ? father : mother;
      FamilyTreeScreen.TreeNode fNode = newRoot == null ? new FamilyTreeScreen.TreeNode() : new FamilyTreeScreen.TreeNode(newRoot, false);
      fNode.children.add(root);
      FamilyTreeNode spouse = newRoot == father ? mother : father;
      fNode.spouse = spouse == null ? new FamilyTreeScreen.TreeNode() : new FamilyTreeScreen.TreeNode(spouse, false);
      return newRoot != null && levels > 0 ? this.insertParents(fNode, newRoot, levels - 1) : fNode;
   }

   static final class Bounds {
      final int left;
      final int right;
      final int top;
      final int bottom;

      public Bounds(int left, int right, int top, int bottom) {
         this.left = left;
         this.right = right;
         this.top = top;
         this.bottom = bottom;
      }

      public FamilyTreeScreen.Bounds add(int x, int y) {
         return new FamilyTreeScreen.Bounds(this.left + x, this.right + x, this.top + y, this.bottom + y);
      }

      public boolean contains(int mouseX, int mouseY) {
         return mouseX >= this.left && mouseY >= this.top && mouseX <= this.right && mouseY <= this.bottom;
      }
   }

   private final class TreeNode {
      private boolean widthComputed;
      private int width;
      private int labelWidth;
      private final List<Component> label = new ArrayList<>();
      private final List<FamilyTreeScreen.TreeNode> children = new ArrayList<>();
      private FamilyTreeScreen.Bounds bounds;
      FamilyTreeScreen.TreeNode spouse;
      final UUID id;
      final boolean deceased;
      private final RelationshipState relationship;
      private final String defaultNodeName = "???";

      private TreeNode() {
         this.id = null;
         this.deceased = false;
         this.relationship = RelationshipState.SINGLE;
         this.label.add(Component.m_237113_("???"));
      }

      public TreeNode(FamilyTreeNode node, boolean recurse) {
         this(node, new HashSet<>(), recurse);
      }

      public TreeNode(FamilyTreeNode node, Set<UUID> parsed, boolean recurse) {
         this.id = node.id();
         this.deceased = node.isDeceased();
         this.relationship = node.getRelationshipState();
         MutableComponent text = Component.m_237113_(MCA.isBlankString(node.getName()) ? "???" : node.getName());
         this.label.add(text.m_6270_(text.m_7383_().m_178520_(node.gender().getColor())));
         this.label.add(node.getProfessionText().m_130940_(ChatFormatting.GRAY));
         FamilyTreeNode father = FamilyTreeScreen.this.family.get(node.father());
         FamilyTreeNode mother = FamilyTreeScreen.this.family.get(node.mother());
         if ((father == null || father.isDeceased()) && (mother == null || mother.isDeceased())) {
            this.label.add(Component.m_237115_("gui.family_tree.label.orphan").m_130940_(ChatFormatting.GRAY));
         }

         if (node.getRelationshipState() != RelationshipState.SINGLE) {
            this.label.add(Component.m_237115_("marriage." + node.getRelationshipState().base().getIcon()));
         }

         if (recurse) {
            node.children().forEach(child -> {
               FamilyTreeNode e = FamilyTreeScreen.this.family.get(child);
               if (e != null) {
                  this.children.add(FamilyTreeScreen.this.new TreeNode(e, parsed, parsed.add(child)));
               }
            });
            FamilyTreeNode spouse = FamilyTreeScreen.this.family.get(node.partner());
            if (spouse != null) {
               this.spouse = FamilyTreeScreen.this.new TreeNode(spouse, parsed, false);
            } else if (!this.children.isEmpty()) {
               this.spouse = FamilyTreeScreen.this.new TreeNode();
            }
         }
      }

      public void render(GuiGraphics context, int mouseX, int mouseY) {
         PoseStack matrices = context.m_280168_();
         FamilyTreeScreen.Bounds bounds = this.getBounds();
         boolean isFocused = this.id != null && bounds.contains(mouseX, mouseY);
         if (isFocused) {
            FamilyTreeScreen.this.focused = this;
         }

         int childrenStartX = -this.getWidth() / 2;

         for (FamilyTreeScreen.TreeNode node : this.children) {
            childrenStartX += (node.getWidth() + 20) / 2;
            int x = childrenStartX + 10;
            int y = 60;
            this.drawHook(context, x, y);
            matrices.m_85836_();
            matrices.m_252880_(x, y, 0.0F);
            node.render(context, mouseX - x, mouseY - y);
            matrices.m_85849_();
            childrenStartX += (node.getWidth() + 20) / 2;
         }

         matrices.m_85836_();
         matrices.m_252880_(0.0F, 0.0F, 400.0F);
         int fillColor = isFocused ? -267386816 : -267386864;
         int borderColor = isFocused ? -14155649 : 1347420415;
         context.m_280509_(bounds.left, bounds.top + 1, bounds.left + 1, bounds.bottom - 1, fillColor);
         context.m_280509_(bounds.right - 1, bounds.top + 1, bounds.right, bounds.bottom - 1, fillColor);
         context.m_280509_(bounds.left + 1, bounds.top, bounds.right - 1, bounds.bottom, fillColor);
         context.m_280509_(bounds.left + 1, bounds.top + 1, bounds.left + 2, bounds.bottom - 1, borderColor);
         context.m_280509_(bounds.right - 2, bounds.top + 1, bounds.right - 1, bounds.bottom - 1, borderColor);
         context.m_280509_(bounds.left + 2, bounds.top + 1, bounds.right - 2, bounds.top + 2, borderColor);
         context.m_280509_(bounds.left + 2, bounds.bottom - 2, bounds.right - 2, bounds.bottom - 1, borderColor);
         BufferSource immediate = MultiBufferSource.m_109898_(Tesselator.m_85913_().m_85915_());
         int l = bounds.top + 5;
         int k = bounds.left + 6;
         if (this.deceased) {
            k += 20;
         }

         Matrix4f matrix4f = matrices.m_85850_().m_252922_();
         Font r = Minecraft.m_91087_().f_91062_;

         for (int s = 0; s < this.label.size(); s++) {
            Component line = this.label.get(s);
            if (line != null) {
               r.m_272077_(line, k, l, -1, true, matrix4f, immediate, DisplayMode.NORMAL, 0, 15728880);
            }

            if (s == 0) {
               l += 2;
            }

            l += 10;
         }

         immediate.m_109911_();
         matrices.m_85849_();
         if (this.deceased) {
            Icon icon = MCAScreens.getInstance().getIcon("deceased");
            context.m_280398_(InteractScreen.ICON_TEXTURES, bounds.left + 6, bounds.top + 6, 0, icon.u(), icon.v(), 16, 16, 256, 256);
            if (isFocused && mouseX <= bounds.left + 20) {
               matrices.m_85836_();
               matrices.m_252880_(0.0F, 0.0F, 20.0F);
               context.m_280557_(FamilyTreeScreen.this.f_96547_, Component.m_237115_("gui.family_tree.label.deceased"), mouseX, mouseY);
               matrices.m_85849_();
            }
         }

         if (this.spouse != null) {
            int x = bounds.left - 50;
            int y = bounds.top + bounds.bottom / 2;
            context.m_280656_(x, bounds.left - 1, y, -1);
            if (this.relationship == RelationshipState.MARRIED_TO_PLAYER
               || this.relationship == RelationshipState.MARRIED_TO_VILLAGER
               || this.relationship == RelationshipState.ENGAGED
               || this.relationship == RelationshipState.PROMISED
               || this.relationship == RelationshipState.WIDOW) {
               Icon icon = MCAScreens.getInstance().getIcon(this.relationship.getIcon());
               context.m_280398_(InteractScreen.ICON_TEXTURES, bounds.left - 25 - 8, y - 8, 0, icon.u(), icon.v(), 16, 16, 256, 256);
            }

            y -= this.spouse.label.size() * 9 / 2;
            x -= this.spouse.getWidth() / 2 - 6;
            matrices.m_85836_();
            matrices.m_252880_(x, y, 0.0F);
            this.spouse.render(context, mouseX - x, mouseY - y);
            matrices.m_85849_();
         }
      }

      private void drawHook(GuiGraphics context, int endX, int endY) {
         int midY = endY / 2;
         context.m_280315_(0, 0, midY, -1);
         context.m_280656_(0, endX, midY, -1);
         context.m_280315_(endX, midY, endY, -1);
      }

      public int getWidth() {
         if (!this.widthComputed) {
            this.widthComputed = true;
            this.labelWidth = this.label.stream().mapToInt(FamilyTreeScreen.this.f_96547_::m_92852_).max().orElse(0);
            if (this.deceased) {
               this.labelWidth += 20;
            }

            this.width = Math.max(this.labelWidth + 10, this.children.stream().mapToInt(FamilyTreeScreen.TreeNode::getWidth).sum()) + 10;
            if (this.spouse != null) {
               this.width = this.width + this.spouse.getWidth() + 50;
            }
         }

         return this.width;
      }

      public FamilyTreeScreen.Bounds getBounds() {
         if (this.bounds == null) {
            this.getWidth();
            int padding = 4;
            this.bounds = new FamilyTreeScreen.Bounds(
               -this.labelWidth / 2 - padding, this.labelWidth / 2 + padding * 2, -padding, 9 * this.label.size() + padding * 2
            );
         }

         return this.bounds;
      }
   }
}
