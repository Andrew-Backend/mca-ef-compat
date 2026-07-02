package forge.net.mca.resources.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.util.RegistryHelper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;

public final class BuildingType implements Serializable {
   private static final long serialVersionUID = 2215455350801127280L;
   private final String name;
   private final int margin;
   private final String color;
   private final int priority;
   private final boolean visible;
   private final boolean noBeds;
   private final Map<String, Integer> blocks;
   private transient Map<ResourceLocation, ResourceLocation> blockToGroup;
   private transient Map<TagKey<Block>, ResourceLocation> tagToGroup;
   private transient Map<ResourceLocation, Integer> groups;
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
      this.margin = GsonHelper.m_13824_(value, "margin", 0);
      this.color = GsonHelper.m_13851_(value, "color", "ffffffff");
      this.priority = GsonHelper.m_13824_(value, "priority", 0);
      this.visible = GsonHelper.m_13855_(value, "visible", true);
      this.noBeds = GsonHelper.m_13855_(value, "noBeds", false);
      this.icon = GsonHelper.m_13855_(value, "icon", false);
      this.iconU = GsonHelper.m_13824_(value, "iconU", 0);
      this.iconV = GsonHelper.m_13824_(value, "iconV", 0);
      this.grouped = GsonHelper.m_13855_(value, "grouped", false);
      this.mergeRange = GsonHelper.m_13824_(value, "mergeRange", 0);
      this.blocks = new HashMap<>();
      if (GsonHelper.m_144772_(value, "blocks")) {
         JsonObject blocks = GsonHelper.m_13930_(value, "blocks");

         for (Entry<String, JsonElement> entry : blocks.entrySet()) {
            this.blocks.put(entry.getKey(), entry.getValue().getAsInt());
         }
      }

      this.groups = new HashMap<>();
      if (GsonHelper.m_144772_(value, "groups")) {
         JsonObject blocks = GsonHelper.m_13930_(value, "groups");

         for (Entry<String, JsonElement> entry : blocks.entrySet()) {
            this.groups.put(new ResourceLocation(entry.getKey()), entry.getValue().getAsInt());
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

   public Map<ResourceLocation, ResourceLocation> getBlockToGroup() {
      if (this.blockToGroup == null) {
         this.blockToGroup = new HashMap<>();
         this.tagToGroup = new HashMap<>();
         this.groups = new HashMap<>();

         for (Entry<String, Integer> requirement : this.blocks.entrySet()) {
            ResourceLocation identifier;
            if (requirement.getKey().startsWith("#")) {
               identifier = new ResourceLocation(requirement.getKey().substring(1));
               TagKey<Block> tag = TagKey.m_203882_(Registries.f_256747_, identifier);
               if (tag == null || RegistryHelper.isTagEmpty(tag)) {
                  MCA.LOGGER.error("Unknown building type tag " + identifier);
               }

               this.tagToGroup.put(tag, identifier);
            } else {
               identifier = new ResourceLocation(requirement.getKey());
               this.blockToGroup.put(identifier, identifier);
            }

            this.groups.put(identifier, requirement.getValue());
         }
      }

      return this.blockToGroup;
   }

   private Optional<ResourceLocation> getGroupForBlock(ResourceLocation blockId) {
      this.getBlockToGroup();
      ResourceLocation directGroup = this.blockToGroup.get(blockId);
      if (directGroup != null) {
         return Optional.of(directGroup);
      }

      Optional<Reference<Block>> entry = BuiltInRegistries.f_256975_.m_203636_(ResourceKey.m_135785_(Registries.f_256747_, blockId));
      if (entry.isEmpty()) {
         return Optional.empty();
      }

      for (Entry<TagKey<Block>, ResourceLocation> tagEntry : this.tagToGroup.entrySet()) {
         if (entry.get().m_203656_(tagEntry.getKey())) {
            return Optional.of(tagEntry.getValue());
         }
      }

      return Optional.empty();
   }

   public boolean matchesBlock(ResourceLocation blockId) {
      return this.getGroupForBlock(blockId).isPresent();
   }

   public Map<ResourceLocation, Integer> getGroups() {
      this.getBlockToGroup();
      return this.groups;
   }

   public Map<ResourceLocation, List<BlockPos>> getGroups(Map<ResourceLocation, List<BlockPos>> blocks) {
      HashMap<ResourceLocation, List<BlockPos>> available = new HashMap<>();

      for (Entry<ResourceLocation, List<BlockPos>> entry : blocks.entrySet()) {
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
