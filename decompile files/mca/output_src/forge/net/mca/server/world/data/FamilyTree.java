package forge.net.mca.server.world.data;

import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FamilyTree extends SavedData {
   private static final String DATA_ID = "MCA-FamilyTree";
   private final Map<UUID, FamilyTreeNode> entries;

   public static FamilyTree get(ServerLevel world) {
      return WorldUtils.loadData(world.m_7654_().m_129783_(), FamilyTree::new, FamilyTree::new, "MCA-FamilyTree");
   }

   FamilyTree(ServerLevel world) {
      this.entries = new HashMap<>();
   }

   FamilyTree(CompoundTag nbt) {
      this.entries = NbtHelper.toMap(nbt, UUID::fromString, (id, element) -> new FamilyTreeNode(this, id, (CompoundTag)element));
      UUID uuid = UUID.fromString("12341234-1234-1234-1234-123412341234");
      if (!this.entries.containsKey(uuid)) {
         this.entries.put(uuid, this.createEntry(uuid, "debug", Gender.NEUTRAL, false));
         this.m_77762_();
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

   public CompoundTag m_7176_(CompoundTag nbt) {
      return NbtHelper.fromMap(nbt, this.entries, UUID::toString, FamilyTreeNode::save);
   }

   public Optional<FamilyTreeNode> getOrEmpty(@Nullable UUID id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.entries.get(id));
   }

   public Stream<FamilyTreeNode> getAllWithName(String name) {
      return this.entries.values().stream().filter(n -> n.getName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)));
   }

   @NotNull
   public FamilyTreeNode getOrCreate(Entity entity) {
      return this.entries
         .computeIfAbsent(
            entity.m_20148_(),
            uuid -> this.createEntry(
               entity.m_20148_(),
               entity.m_7755_().getString(),
               EntityRelationship.of(entity).map(EntityRelationship::getGender).orElse(Gender.MALE),
               entity instanceof Player
            )
         );
   }

   public void remove(UUID id) {
      this.entries.remove(id);
      this.m_77762_();
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
      this.m_77762_();
      return new FamilyTreeNode(this, uuid, name, isPlayer, gender, Util.f_137441_, Util.f_137441_);
   }
}
