package forge.net.mca.entity.ai.relationship;

import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityRelationship {
   default Gender getGender() {
      return Gender.MALE;
   }

   default FamilyTree getFamilyTree() {
      return FamilyTree.get(this.getWorld());
   }

   ServerLevel getWorld();

   UUID getUUID();

   @NotNull
   FamilyTreeNode getFamilyEntry();

   default Stream<Entity> getFamily(int parents, int children) {
      return this.getFamilyEntry()
         .getRelatives(parents, children)
         .<Entity>map(this.getWorld()::m_8791_)
         .filter(Objects::nonNull)
         .filter(e -> !e.m_20148_().equals(this.getUUID()));
   }

   default Stream<Entity> getParents() {
      return this.getFamilyEntry().streamParents().<Entity>map(this.getWorld()::m_8791_).filter(Objects::nonNull);
   }

   default Optional<Entity> getPartner() {
      return Optional.ofNullable(this.getWorld().m_8791_(this.getFamilyEntry().partner()));
   }

   default Stream<EntityRelationship> getRelationshipStream(Stream<UUID> uuids) {
      return uuids.<Optional<EntityRelationship>>map(
            uuid -> PlayerSaveData.getIfPresent(this.getWorld(), uuid).map(p -> (EntityRelationship)p).or(() -> of(this.getWorld().m_8791_(uuid)))
         )
         .filter(Optional::isPresent)
         .map(Optional::get);
   }

   default void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type, Entity victim) {
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

   default void marry(Entity spouse) {
      RelationshipState state = spouse instanceof Player ? RelationshipState.MARRIED_TO_PLAYER : RelationshipState.MARRIED_TO_VILLAGER;
      if (spouse instanceof ServerPlayer spouseEntity) {
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(spouseEntity, "marriage");
      }

      this.getFamilyEntry().updatePartner(spouse, state);
   }

   default void engage(Entity spouse) {
      if (spouse instanceof ServerPlayer spouseEntity) {
         CriterionMCA.GENERIC_EVENT_CRITERION.trigger(spouseEntity, "engage");
      }

      this.getFamilyEntry().updatePartner(spouse, RelationshipState.ENGAGED);
   }

   default void promise(Entity spouse) {
      if (spouse instanceof ServerPlayer spouseEntity) {
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
      return spouse.equals(Util.f_137441_) ? Optional.empty() : Optional.of(spouse);
   }

   default Optional<Component> getPartnerName() {
      return this.getFamilyTree().getOrEmpty(this.getFamilyEntry().partner()).map(FamilyTreeNode::getName).map(Component::m_237113_);
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
      return this.getPartnerUUID().orElse(Util.f_137441_).equals(uuid) && this.isPromised();
   }

   default boolean isMarriedTo(UUID uuid) {
      return this.getPartnerUUID().orElse(Util.f_137441_).equals(uuid) && this.isMarried();
   }

   default boolean isEngagedWith(UUID uuid) {
      return this.getPartnerUUID().orElse(Util.f_137441_).equals(uuid) && this.isEngaged();
   }

   static Optional<EntityRelationship> of(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         return Optional.ofNullable(PlayerSaveData.get(player));
      } else {
         return entity instanceof CompassionateEntity<?> compassionateEntity ? Optional.ofNullable(compassionateEntity.getRelationships()) : Optional.empty();
      }
   }
}
