package quilt.net.mca.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1041;
import net.minecraft.class_1109;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_289;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5250;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_4597.class_4598;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import quilt.net.mca.MCA;
import quilt.net.mca.client.resources.Icon;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.network.c2s.GetFamilyTreeRequest;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.util.compat.ButtonWidget;

public class FamilyTreeScreen extends class_437 {
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
   private final class_437 parent;

   public FamilyTreeScreen(UUID entityId) {
      super(class_2561.method_43471("gui.family_tree.title"));
      this.focusedEntityId = entityId;
      this.parent = class_310.method_1551().field_1755;
   }

   public boolean method_25421() {
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

   public void method_25426() {
      this.focusEntity(this.focusedEntityId);
      this.method_37063(
         new ButtonWidget(this.field_22789 / 2 - 100, this.field_22790 - 25, 200, 20, class_2561.method_43471("gui.done"), sender -> this.method_25419())
      );
   }

   public void method_25419() {
      assert this.field_22787 != null;
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0) {
         this.scrollX += deltaX;
         this.scrollY += deltaY;
         return true;
      } else {
         return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0 && this.focused != null) {
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
         if (this.focusEntity(this.focused.id)) {
            this.rebuildTree();
         }

         return true;
      } else {
         return super.method_25402(mouseX, mouseY, button);
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_25294(0, 30, this.field_22789, this.field_22790 - 30, 1711276032);
      this.focused = null;
      class_1041 window = class_310.method_1551().method_22683();
      double f = window.method_4495();
      int windowHeight = (int)Math.round(window.method_4502() * f);
      int x = 0;
      int y = (int)(30.0 * f);
      int w = (int)(this.field_22789 * f);
      int h = (int)((this.field_22790 - 60) * f);
      GL11.glScissor(x, windowHeight - h - y, w, h);
      GL11.glEnable(3089);
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      int xx = (int)(this.scrollX + this.field_22789 / 2);
      int yy = (int)(this.scrollY + this.field_22790 / 2);
      matrices.method_46416(xx, yy, 0.0F);
      this.tree.render(context, mouseX - xx, mouseY - yy);
      matrices.method_22909();
      GL11.glDisable(3089);
      FamilyTreeNode selected = this.family.get(this.focusedEntityId);
      class_2561 label = (class_2561)(selected == null
         ? this.field_22785
         : class_2561.method_43470(selected.getName()).method_27693("'s ").method_10852(this.field_22785));
      context.method_27534(this.field_22793, label, this.field_22789 / 2, 10, 16777215);
      super.method_25394(context, mouseX, mouseY, delta);
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
      private final List<class_2561> label = new ArrayList<>();
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
         this.label.add(class_2561.method_43470("???"));
      }

      public TreeNode(FamilyTreeNode node, boolean recurse) {
         this(node, new HashSet<>(), recurse);
      }

      public TreeNode(FamilyTreeNode node, Set<UUID> parsed, boolean recurse) {
         this.id = node.id();
         this.deceased = node.isDeceased();
         this.relationship = node.getRelationshipState();
         class_5250 text = class_2561.method_43470(MCA.isBlankString(node.getName()) ? "???" : node.getName());
         this.label.add(text.method_10862(text.method_10866().method_36139(node.gender().getColor())));
         this.label.add(node.getProfessionText().method_27692(class_124.field_1080));
         FamilyTreeNode father = FamilyTreeScreen.this.family.get(node.father());
         FamilyTreeNode mother = FamilyTreeScreen.this.family.get(node.mother());
         if ((father == null || father.isDeceased()) && (mother == null || mother.isDeceased())) {
            this.label.add(class_2561.method_43471("gui.family_tree.label.orphan").method_27692(class_124.field_1080));
         }

         if (node.getRelationshipState() != RelationshipState.SINGLE) {
            this.label.add(class_2561.method_43471("marriage." + node.getRelationshipState().base().getIcon()));
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

      public void render(class_332 context, int mouseX, int mouseY) {
         class_4587 matrices = context.method_51448();
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
            matrices.method_22903();
            matrices.method_46416(x, y, 0.0F);
            node.render(context, mouseX - x, mouseY - y);
            matrices.method_22909();
            childrenStartX += (node.getWidth() + 20) / 2;
         }

         matrices.method_22903();
         matrices.method_46416(0.0F, 0.0F, 400.0F);
         int fillColor = isFocused ? -267386816 : -267386864;
         int borderColor = isFocused ? -14155649 : 1347420415;
         context.method_25294(bounds.left, bounds.top + 1, bounds.left + 1, bounds.bottom - 1, fillColor);
         context.method_25294(bounds.right - 1, bounds.top + 1, bounds.right, bounds.bottom - 1, fillColor);
         context.method_25294(bounds.left + 1, bounds.top, bounds.right - 1, bounds.bottom, fillColor);
         context.method_25294(bounds.left + 1, bounds.top + 1, bounds.left + 2, bounds.bottom - 1, borderColor);
         context.method_25294(bounds.right - 2, bounds.top + 1, bounds.right - 1, bounds.bottom - 1, borderColor);
         context.method_25294(bounds.left + 2, bounds.top + 1, bounds.right - 2, bounds.top + 2, borderColor);
         context.method_25294(bounds.left + 2, bounds.bottom - 2, bounds.right - 2, bounds.bottom - 1, borderColor);
         class_4598 immediate = class_4597.method_22991(class_289.method_1348().method_1349());
         int l = bounds.top + 5;
         int k = bounds.left + 6;
         if (this.deceased) {
            k += 20;
         }

         Matrix4f matrix4f = matrices.method_23760().method_23761();
         class_327 r = class_310.method_1551().field_1772;

         for (int s = 0; s < this.label.size(); s++) {
            class_2561 line = this.label.get(s);
            if (line != null) {
               r.method_30882(line, k, l, -1, true, matrix4f, immediate, class_6415.field_33993, 0, 15728880);
            }

            if (s == 0) {
               l += 2;
            }

            l += 10;
         }

         immediate.method_22993();
         matrices.method_22909();
         if (this.deceased) {
            Icon icon = MCAScreens.getInstance().getIcon("deceased");
            context.method_25291(InteractScreen.ICON_TEXTURES, bounds.left + 6, bounds.top + 6, 0, icon.u(), icon.v(), 16, 16, 256, 256);
            if (isFocused && mouseX <= bounds.left + 20) {
               matrices.method_22903();
               matrices.method_46416(0.0F, 0.0F, 20.0F);
               context.method_51438(FamilyTreeScreen.this.field_22793, class_2561.method_43471("gui.family_tree.label.deceased"), mouseX, mouseY);
               matrices.method_22909();
            }
         }

         if (this.spouse != null) {
            int x = bounds.left - 50;
            int y = bounds.top + bounds.bottom / 2;
            context.method_25292(x, bounds.left - 1, y, -1);
            if (this.relationship == RelationshipState.MARRIED_TO_PLAYER
               || this.relationship == RelationshipState.MARRIED_TO_VILLAGER
               || this.relationship == RelationshipState.ENGAGED
               || this.relationship == RelationshipState.PROMISED
               || this.relationship == RelationshipState.WIDOW) {
               Icon icon = MCAScreens.getInstance().getIcon(this.relationship.getIcon());
               context.method_25291(InteractScreen.ICON_TEXTURES, bounds.left - 25 - 8, y - 8, 0, icon.u(), icon.v(), 16, 16, 256, 256);
            }

            y -= this.spouse.label.size() * 9 / 2;
            x -= this.spouse.getWidth() / 2 - 6;
            matrices.method_22903();
            matrices.method_46416(x, y, 0.0F);
            this.spouse.render(context, mouseX - x, mouseY - y);
            matrices.method_22909();
         }
      }

      private void drawHook(class_332 context, int endX, int endY) {
         int midY = endY / 2;
         context.method_25301(0, 0, midY, -1);
         context.method_25292(0, endX, midY, -1);
         context.method_25301(endX, midY, endY, -1);
      }

      public int getWidth() {
         if (!this.widthComputed) {
            this.widthComputed = true;
            this.labelWidth = this.label.stream().mapToInt(FamilyTreeScreen.this.field_22793::method_27525).max().orElse(0);
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
