package quilt.net.mca.server.world.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_1657;
import net.minecraft.class_18;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.entity.ai.relationship.EntityRelationship;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class FamilyTree extends class_18 {
   private static final String DATA_ID = "MCA-FamilyTree";
   private final Map<UUID, FamilyTreeNode> entries;

   public static FamilyTree get(class_3218 world) {
      return WorldUtils.loadData(world.method_8503().method_30002(), FamilyTree::new, FamilyTree::new, "MCA-FamilyTree");
   }

   FamilyTree(class_3218 world) {
      this.entries = new HashMap<>();
   }

   FamilyTree(class_2487 nbt) {
      this.entries = NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> new FamilyTreeNode(this, id, (class_2487)element));
      UUID uuid = UUID.fromString("12341234-1234-1234-1234-123412341234");
      if (!this.entries.containsKey(uuid)) {
         this.entries.put(uuid, this.createEntry(uuid, "debug", Gender.NEUTRAL, false));
         this.method_80();
         this.entries.values().forEach(e -> {
            FamilyTreeNode partner = this.entries.get(e.partner());
            boolean partnerIsPlayer = partner != null && partner.isPlayer();
            if (e.getRelationshipState() == RelationshipState.ENGAGED && partnerIsPlayer == e.isPlayer()) {
               e.setRelationshipState(RelationshipState.MARRIED_TO_VILLAGER);
            }

            if (e.getRelationshipState() == RelationshipState.MARRIED_TO_VILLAGER && partnerIsPlayer) {
               e.setRelationshipState(RelationshipState.MARRIED_TO_PLAYER);
            }

            if (e.getRelationshipState() == RelationshipState.MARRIED_TO_PLAYER && !partnerIsPlayer) {
               e.setRelationshipState(RelationshipState.WIDOW);
            }
         });
      }
   }

   public class_2487 method_75(class_2487 nbt) {
      return NbtHelper.fromMap(nbt, this.entries, UUID::toString, FamilyTreeNode::save);
   }

   public Optional<FamilyTreeNode> getOrEmpty(@Nullable UUID id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.entries.get(id));
   }

   public Stream<FamilyTreeNode> getAllWithName(String name) {
      return this.entries.values().stream().filter(n -> n.getName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)));
   }

   @NotNull
   public FamilyTreeNode getOrCreate(class_1297 entity) {
      return this.entries
         .computeIfAbsent(
            entity.method_5667(),
            uuid -> this.createEntry(
               entity.method_5667(),
               entity.method_5477().getString(),
               EntityRelationship.of(entity).map(EntityRelationship::getGender).orElse(Gender.MALE),
               entity instanceof class_1657
            )
         );
   }

   public void remove(UUID id) {
      this.entries.remove(id);
      this.method_80();
   }

   @NotNull
   public FamilyTreeNode getOrCreate(UUID id, String name, Gender gender) {
      return this.getOrCreate(id, name, gender, false);
   }

   @NotNull
   public FamilyTreeNode getOrCreate(UUID id, String name, Gender gender, boolean isPlayer) {
      return this.entries.computeIfAbsent(id, uuid -> this.createEntry(uuid, name, gender, isPlayer));
   }

   private FamilyTreeNode createEntry(UUID uuid, String name, Gender gender, boolean isPlayer) {
      this.method_80();
      return new FamilyTreeNode(this, uuid, name, isPlayer, gender, class_156.field_25140, class_156.field_25140);
   }
}
