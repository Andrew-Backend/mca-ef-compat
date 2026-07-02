package quilt.net.mca.resources.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_3518;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_6880.class_6883;
import quilt.net.mca.MCA;
import quilt.net.mca.util.RegistryHelper;

public final class BuildingType implements Serializable {
   private static final long serialVersionUID = 2215455350801127280L;
   private final String name;
   private final int margin;
   private final String color;
   private final int priority;
   private final boolean visible;
   private final boolean noBeds;
   private final Map<String, Integer> blocks;
   private transient Map<class_2960, class_2960> blockToGroup;
   private transient Map<class_6862<class_2248>, class_2960> tagToGroup;
   private transient Map<class_2960, Integer> groups;
   private final boolean icon;
   private final int iconU;
   private final int iconV;
   private final boolean grouped;
   private final int mergeRange;

   public BuildingType() {
      this.name = "?";
      this.margin = 0;
      this.color = "ffffffff";
      this.priority = 0;
      this.visible = true;
      this.noBeds = false;
      this.blocks = Map.of("#minecraft:beds", 1000000000);
      this.blockToGroup = null;
      this.icon = false;
      this.iconU = 0;
      this.iconV = 0;
      this.grouped = false;
      this.mergeRange = 32;
   }

   public BuildingType(String name, JsonObject value) {
      this.name = name;
      this.margin = class_3518.method_15282(value, "margin", 0);
      this.color = class_3518.method_15253(value, "color", "ffffffff");
      this.priority = class_3518.method_15282(value, "priority", 0);
      this.visible = class_3518.method_15258(value, "visible", true);
      this.noBeds = class_3518.method_15258(value, "noBeds", false);
      this.icon = class_3518.method_15258(value, "icon", false);
      this.iconU = class_3518.method_15282(value, "iconU", 0);
      this.iconV = class_3518.method_15282(value, "iconV", 0);
      this.grouped = class_3518.method_15258(value, "grouped", false);
      this.mergeRange = class_3518.method_15282(value, "mergeRange", 0);
      this.blocks = new HashMap<>();
      if (class_3518.method_34923(value, "blocks")) {
         JsonObject blocks = class_3518.method_15296(value, "blocks");

         for (Entry<String, JsonElement> entry : blocks.entrySet()) {
            this.blocks.put(entry.getKey(), entry.getValue().getAsInt());
         }
      }

      this.groups = new HashMap<>();
      if (class_3518.method_34923(value, "groups")) {
         JsonObject blocks = class_3518.method_15296(value, "groups");

         for (Entry<String, JsonElement> entry : blocks.entrySet()) {
            this.groups.put(new class_2960(entry.getKey()), entry.getValue().getAsInt());
         }
      }
   }

   public String name() {
      return this.name;
   }

   public String color() {
      return this.color;
   }

   public int priority() {
      return this.priority;
   }

   public boolean visible() {
      return this.visible;
   }

   public int getColor() {
      return (int)Long.parseLong(this.color, 16);
   }

   public Map<class_2960, class_2960> getBlockToGroup() {
      if (this.blockToGroup == null) {
         this.blockToGroup = new HashMap<>();
         this.tagToGroup = new HashMap<>();
         this.groups = new HashMap<>();

         for (Entry<String, Integer> requirement : this.blocks.entrySet()) {
            class_2960 identifier;
            if (requirement.getKey().startsWith("#")) {
               identifier = new class_2960(requirement.getKey().substring(1));
               class_6862<class_2248> tag = class_6862.method_40092(class_7924.field_41254, identifier);
               if (tag == null || RegistryHelper.isTagEmpty(tag)) {
                  MCA.LOGGER.error("Unknown building type tag " + identifier);
               }

               this.tagToGroup.put(tag, identifier);
            } else {
               identifier = new class_2960(requirement.getKey());
               this.blockToGroup.put(identifier, identifier);
            }

            this.groups.put(identifier, requirement.getValue());
         }
      }

      return this.blockToGroup;
   }

   private Optional<class_2960> getGroupForBlock(class_2960 blockId) {
      this.getBlockToGroup();
      class_2960 directGroup = this.blockToGroup.get(blockId);
      if (directGroup != null) {
         return Optional.of(directGroup);
      }

      Optional<class_6883<class_2248>> entry = class_7923.field_41175.method_40264(class_5321.method_29179(class_7924.field_41254, blockId));
      if (entry.isEmpty()) {
         return Optional.empty();
      }

      for (Entry<class_6862<class_2248>, class_2960> tagEntry : this.tagToGroup.entrySet()) {
         if (entry.get().method_40220(tagEntry.getKey())) {
            return Optional.of(tagEntry.getValue());
         }
      }

      return Optional.empty();
   }

   public boolean matchesBlock(class_2960 blockId) {
      return this.getGroupForBlock(blockId).isPresent();
   }

   public Map<class_2960, Integer> getGroups() {
      this.getBlockToGroup();
      return this.groups;
   }

   public Map<class_2960, List<class_2338>> getGroups(Map<class_2960, List<class_2338>> blocks) {
      HashMap<class_2960, List<class_2338>> available = new HashMap<>();

      for (Entry<class_2960, List<class_2338>> entry : blocks.entrySet()) {
         this.getGroupForBlock(entry.getKey()).ifPresent(group -> available.computeIfAbsent(group, k -> new LinkedList<>()).addAll(entry.getValue()));
      }

      return available;
   }

   public boolean isIcon() {
      return this.icon;
   }

   public int iconU() {
      return this.iconU * 20;
   }

   public int iconV() {
      return this.iconV * 60;
   }

   public boolean grouped() {
      return this.grouped;
   }

   public int mergeRange() {
      return this.mergeRange;
   }

   public boolean noBeds() {
      return this.noBeds;
   }

   public int getMargin() {
      return this.margin;
   }

   public int getMinBlocks() {
      return this.blocks.values().stream().mapToInt(v -> v).sum();
   }
}
