package quilt.net.mca.entity.ai;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1657;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4099;
import net.minecraft.class_4140;
import net.minecraft.class_4142;
import net.minecraft.class_4168;
import net.minecraft.class_8111;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.Config;
import quilt.net.mca.TagsMCA;
import quilt.net.mca.block.BlocksMCA;
import quilt.net.mca.block.TombstoneBlock;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.relationship.CompassionateEntity;
import quilt.net.mca.entity.ai.relationship.EntityRelationship;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.RelationshipType;
import quilt.net.mca.entity.interaction.gifts.GiftSaturation;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.server.world.data.GraveyardManager;
import quilt.net.mca.util.WorldUtils;
import quilt.net.mca.util.network.datasync.CDataManager;

public class Relationship<T extends class_1308 & VillagerLike<T>> implements EntityRelationship {
   public static final Relationship.Predicate IS_MARRIED = (villager, player) -> villager.getRelationships().isMarriedTo(player);
   public static final Relationship.Predicate IS_ENGAGED = (villager, player) -> villager.getRelationships().isEngagedWith(player);
   public static final Relationship.Predicate IS_PROMISED = (villager, player) -> villager.getRelationships().isPromisedTo(player);
   public static final Relationship.Predicate IS_RELATIVE = (villager, player) -> villager.getRelationships().getFamilyEntry().isRelative(player);
   public static final Relationship.Predicate IS_FAMILY = IS_MARRIED.or(IS_RELATIVE);
   public static final Relationship.Predicate IS_PARENT = (villager, player) -> villager.getRelationships().getFamilyEntry().isParent(player);
   public static final Relationship.Predicate IS_KID = (villager, player) -> FamilyTree.get(villager.getRelationships().getWorld())
      .getOrEmpty(player)
      .filter(n -> n.isParent(villager.getRelationships().getUUID()))
      .isPresent();
   public static final Relationship.Predicate IS_ORPHAN = (villager, player) -> villager.getRelationships()
      .getFamilyEntry()
      .getParents()
      .allMatch(FamilyTreeNode::isDeceased);
   protected final T entity;
   private final GiftSaturation giftSaturation = new GiftSaturation();

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll();
   }

   public Relationship(T entity) {
      this.entity = entity;
   }

   @Override
   public Gender getGender() {
      return this.entity.getGenetics().getGender();
   }

   @Override
   public class_3218 getWorld() {
      return (class_3218)this.entity.method_37908();
   }

   @Override
   public UUID getUUID() {
      return this.entity.method_5667();
   }

   @NotNull
   @Override
   public FamilyTreeNode getFamilyEntry() {
      return this.getFamilyTree().getOrCreate(this.entity);
   }

   private Optional<class_2338> placeTombstone(class_3218 world, class_2338 entityPos) {
      int range = 2;

      for (int y = -range; y <= range; y++) {
         class_2338 pos = entityPos.method_10069(0, y, 0);
         if (world.method_8320(pos).method_26215()) {
            world.method_8501(pos, ((class_2248)BlocksMCA.CROSS_HEADSTONE.get()).method_9564());
            return Optional.ofNullable(pos);
         }

         for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
               if (x != 0 || z != 0) {
                  pos = entityPos.method_10069(x, y, z);
                  if (world.method_8320(pos).method_26215()) {
                     world.method_8501(pos, ((class_2248)BlocksMCA.CROSS_HEADSTONE.get()).method_9564());
                     return Optional.ofNullable(pos);
                  }
               }
            }
         }
      }

      return Optional.empty();
   }

   public void onDeath(class_1282 cause) {
      boolean beRemembered = this.getFamilyEntry().willBeRemembered();
      boolean beLoved = this.entity
         .getVillagerBrain()
         .getMemories()
         .values()
         .stream()
         .anyMatch(m -> m.getHearts() > Config.getInstance().heartsRequiredToAutoSpawnGravestone);
      if (!beRemembered && !beLoved && this.entity.isHostile()) {
         this.onTragedy(cause, null);
      } else {
         this.getFamilyEntry().setDeceased(true);
         class_3218 world = (class_3218)this.entity.method_37908();
         Optional<class_2338> nearest = GraveyardManager.get(world).findNearest(this.entity.method_24515(), GraveyardManager.TombstoneState.EMPTY, 10);
         if ((beRemembered || beLoved) && nearest.isEmpty()) {
            nearest = this.placeTombstone(world, this.entity.method_24515());
         }

         nearest.ifPresentOrElse(
            pos -> {
               if (this.entity.method_37908().method_8320(pos).method_26164(TagsMCA.Blocks.TOMBSTONES)
                  && this.entity.method_37908().method_8321(pos) instanceof TombstoneBlock.Data tombstone) {
                  this.onTragedy(cause, pos);
                  tombstone.setEntity(this.entity);
               } else {
                  this.onTragedy(cause, null);
               }
            },
            () -> this.onTragedy(cause, null)
         );
      }

      if (!beRemembered) {
         this.getFamilyEntry().streamParents().forEach(uuid -> this.getFamilyTree().remove(uuid));
         this.getFamilyTree().remove(this.entity.method_5667());
      }
   }

   public void onTragedy(class_1282 cause, @Nullable class_2338 burialSite) {
      if (!this.entity.isHostile()) {
         WorldUtils.getCloseEntities(this.entity.method_37908(), this.entity, 32.0, (Class<T>)VillagerEntityMCA.class)
            .forEach(villager -> villager.getRelationships().onTragedy(cause, burialSite, RelationshipType.STRANGER, this.entity));
      }

      this.onTragedy(cause, burialSite, RelationshipType.SELF, this.entity);
   }

   @Override
   public void onTragedy(class_1282 cause, @Nullable class_2338 burialSite, RelationshipType type, class_1297 with) {
      if (!cause.method_49708(class_8111.field_42347)) {
         int moodAffect = 5 * type.getProximityAmplifier();
         this.entity.method_37908().method_8421(this.entity, (byte)17);
         this.entity.getVillagerBrain().modifyMoodValue(-moodAffect);
         if (cause.method_5529() instanceof class_1657 player) {
            this.entity.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-20);
         }
      }

      if (burialSite != null && type != RelationshipType.STRANGER) {
         this.entity.getVillagerBrain().setGrieving();
         this.entity.method_18868().method_18878(class_4140.field_18445, new class_4142(burialSite, 1.0F, 1));
         this.entity.method_18868().method_18878(class_4140.field_18446, new class_4099(burialSite));
         this.entity.method_18868().method_24526((class_4168)ActivityMCA.GRIEVE.get());
      }

      EntityRelationship.super.onTragedy(cause, burialSite, type, with);
   }

   public GiftSaturation getGiftSaturation() {
      return this.giftSaturation;
   }

   public void readFromNbt(class_2487 nbt) {
      this.giftSaturation.readFromNbt(nbt.method_10554("giftSaturationQueue", 8));
   }

   public void writeToNbt(class_2487 nbt) {
      nbt.method_10566("giftSaturationQueue", this.giftSaturation.toNbt());
   }

   public interface Predicate extends BiPredicate<CompassionateEntity<?>, class_1297> {
      boolean test(CompassionateEntity<?> var1, UUID var2);

      default boolean test(CompassionateEntity<?> villager, class_1297 partner) {
         return partner != null && this.test(villager, partner.method_5667());
      }

      default Relationship.Predicate or(Relationship.Predicate b) {
         return (villager, partner) -> this.test(villager, partner) || b.test(villager, partner);
      }

      default Relationship.Predicate negate() {
         return (villager, partner) -> !this.test(villager, partner);
      }

      default BiPredicate<VillagerLike<?>, class_3222> asConstraint() {
         return (villager, player) -> villager instanceof CompassionateEntity && this.test((CompassionateEntity<?>)villager, player);
      }
   }
}
