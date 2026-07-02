package quilt.net.mca.entity.ai.relationship;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.server.world.data.PlayerSaveData;

public interface EntityRelationship {
   default Gender getGender() {
      return Gender.MALE;
   }

   default FamilyTree getFamilyTree() {
      return FamilyTree.get(this.getWorld());
   }

   class_3218 getWorld();

   UUID getUUID();

   @NotNull
   FamilyTreeNode getFamilyEntry();

   default Stream<class_1297> getFamily(int parents, int children) {
      return this.getFamilyEntry()
         .getRelatives(parents, children)
         .<class_1297>map(this.getWorld()::method_14190)
         .filter(Objects::nonNull)
         .filter(e -> !e.method_5667().equals(this.getUUID()));
   }

   default Stream<class_1297> getParents() {
      return this.getFamilyEntry().streamParents().<class_1297>map(this.getWorld()::method_14190).filter(Objects::nonNull);
   }

   default Optional<class_1297> getPartner() {
      return Optional.ofNullable(this.getWorld().method_14190(this.getFamilyEntry().partner()));
   }

   default Stream<EntityRelationship> getRelationshipStream(Stream<UUID> uuids) {
      return uuids.<Optional<EntityRelationship>>map(
            uuid -> PlayerSaveData.getIfPresent(this.getWorld(), uuid).map(p -> (EntityRelationship)p).or(() -> of(this.getWorld().method_14190(uuid)))
         )
         .filter(Optional::isPresent)
         .map(Optional::get);
   }

   default void onTragedy(class_1282 cause, @Nullable class_2338 burialSite, RelationshipType type, class_1297 victim) {
      if (type != RelationshipType.STRANGER) {
         if (type == RelationshipType.SELF) {
            this.getRelationshipStream(this.getFamilyEntry().streamParents()).forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.CHILD, victim));
            this.getRelationshipStream(this.getFamilyEntry().siblings().stream())
               .forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.SIBLING, victim));
            this.getRelationshipStream(Stream.of(this.getFamilyEntry().partner()))
               .forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.SPOUSE, victim));
         }

         if (type == RelationshipType.SPOUSE || type == RelationshipType.SELF) {
            if (this.getRelationshipState().isMarried()) {
               this.endRelationShip(RelationshipState.WIDOW);
            } else {
               this.endRelationShip(RelationshipState.SINGLE);
            }
         }
      }
   }

   default void marry(class_1297 spouse) {
      RelationshipState state = spouse instanceof class_1657 ? RelationshipState.MARRIED_TO_PLAYER : RelationshipState.MARRIED_TO_VILLAGER;
      if (spouse instanceof class_3222 spouseEntity) {
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(spouseEntity, "marriage");
      }

      this.getFamilyEntry().updatePartner(spouse, state);
   }

   default void engage(class_1297 spouse) {
      if (spouse instanceof class_3222 spouseEntity) {
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(spouseEntity, "engage");
      }

      this.getFamilyEntry().updatePartner(spouse, RelationshipState.ENGAGED);
   }

   default void promise(class_1297 spouse) {
      if (spouse instanceof class_3222 spouseEntity) {
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(spouseEntity, "promise");
      }

      this.getFamilyEntry().updatePartner(spouse, RelationshipState.PROMISED);
   }

   default void endRelationShip(RelationshipState newState) {
      this.getFamilyEntry().updatePartner(null, newState);
   }

   default RelationshipState getRelationshipState() {
      return this.getFamilyEntry().getRelationshipState();
   }

   default Optional<UUID> getPartnerUUID() {
      UUID spouse = this.getFamilyEntry().partner();
      return spouse.equals(class_156.field_25140) ? Optional.empty() : Optional.of(spouse);
   }

   default Optional<class_2561> getPartnerName() {
      return this.getFamilyTree().getOrEmpty(this.getFamilyEntry().partner()).map(FamilyTreeNode::getName).map(class_2561::method_43470);
   }

   default boolean isMarried() {
      return this.getRelationshipState() == RelationshipState.MARRIED_TO_PLAYER || this.getRelationshipState() == RelationshipState.MARRIED_TO_VILLAGER;
   }

   default boolean isEngaged() {
      return this.getRelationshipState() == RelationshipState.ENGAGED;
   }

   default boolean isPromised() {
      return this.getRelationshipState() == RelationshipState.PROMISED;
   }

   default boolean isPromisedTo(UUID uuid) {
      return this.getPartnerUUID().orElse(class_156.field_25140).equals(uuid) && this.isPromised();
   }

   default boolean isMarriedTo(UUID uuid) {
      return this.getPartnerUUID().orElse(class_156.field_25140).equals(uuid) && this.isMarried();
   }

   default boolean isEngagedWith(UUID uuid) {
      return this.getPartnerUUID().orElse(class_156.field_25140).equals(uuid) && this.isEngaged();
   }

   static Optional<EntityRelationship> of(class_1297 entity) {
      if (entity instanceof class_3222 player) {
         return Optional.ofNullable(PlayerSaveData.get(player));
      } else {
         return entity instanceof CompassionateEntity<?> compassionateEntity ? Optional.ofNullable(compassionateEntity.getRelationships()) : Optional.empty();
      }
   }
}
