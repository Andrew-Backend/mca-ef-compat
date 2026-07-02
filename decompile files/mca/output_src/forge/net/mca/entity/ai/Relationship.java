package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import forge.net.mca.TagsMCA;
import forge.net.mca.block.BlocksMCA;
import forge.net.mca.block.TombstoneBlock;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.RelationshipType;
import forge.net.mca.entity.interaction.gifts.GiftSaturation;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.GraveyardManager;
import forge.net.mca.util.WorldUtils;
import forge.net.mca.util.network.datasync.CDataManager;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Relationship<T extends Mob & VillagerLike<T>> implements EntityRelationship {
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

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
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
   public ServerLevel getWorld() {
      return (ServerLevel)this.entity.m_9236_();
   }

   @Override
   public UUID getUUID() {
      return this.entity.m_20148_();
   }

   @NotNull
   @Override
   public FamilyTreeNode getFamilyEntry() {
      return this.getFamilyTree().getOrCreate(this.entity);
   }

   private Optional<BlockPos> placeTombstone(ServerLevel world, BlockPos entityPos) {
      int range = 2;

      for (int y = -range; y <= range; y++) {
         BlockPos pos = entityPos.m_7918_(0, y, 0);
         if (world.m_8055_(pos).m_60795_()) {
            world.m_46597_(pos, ((Block)BlocksMCA.CROSS_HEADSTONE.get()).m_49966_());
            return Optional.ofNullable(pos);
         }

         for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
               if (x != 0 || z != 0) {
                  pos = entityPos.m_7918_(x, y, z);
                  if (world.m_8055_(pos).m_60795_()) {
                     world.m_46597_(pos, ((Block)BlocksMCA.CROSS_HEADSTONE.get()).m_49966_());
                     return Optional.ofNullable(pos);
                  }
               }
            }
         }
      }

      return Optional.empty();
   }

   public void onDeath(DamageSource cause) {
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
         ServerLevel world = (ServerLevel)this.entity.m_9236_();
         Optional<BlockPos> nearest = GraveyardManager.get(world).findNearest(this.entity.m_20183_(), GraveyardManager.TombstoneState.EMPTY, 10);
         if ((beRemembered || beLoved) && nearest.isEmpty()) {
            nearest = this.placeTombstone(world, this.entity.m_20183_());
         }

         nearest.ifPresentOrElse(
            pos -> {
               if (this.entity.m_9236_().m_8055_(pos).m_204336_(TagsMCA.Blocks.TOMBSTONES)
                  && this.entity.m_9236_().m_7702_(pos) instanceof TombstoneBlock.Data tombstone) {
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
         this.getFamilyTree().remove(this.entity.m_20148_());
      }
   }

   public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite) {
      if (!this.entity.isHostile()) {
         WorldUtils.getCloseEntities(this.entity.m_9236_(), this.entity, 32.0, (Class<T>)VillagerEntityMCA.class)
            .forEach(villager -> villager.getRelationships().onTragedy(cause, burialSite, RelationshipType.STRANGER, this.entity));
      }

      this.onTragedy(cause, burialSite, RelationshipType.SELF, this.entity);
   }

   @Override
   public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type, Entity with) {
      if (!cause.m_276093_(DamageTypes.f_268724_)) {
         int moodAffect = 5 * type.getProximityAmplifier();
         this.entity.m_9236_().m_7605_(this.entity, (byte)17);
         this.entity.getVillagerBrain().modifyMoodValue(-moodAffect);
         if (cause.m_7639_() instanceof Player player) {
            this.entity.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-20);
         }
      }

      if (burialSite != null && type != RelationshipType.STRANGER) {
         this.entity.getVillagerBrain().setGrieving();
         this.entity.m_6274_().m_21879_(MemoryModuleType.f_26370_, new WalkTarget(burialSite, 1.0F, 1));
         this.entity.m_6274_().m_21879_(MemoryModuleType.f_26371_, new BlockPosTracker(burialSite));
         this.entity.m_6274_().m_21889_((Activity)ActivityMCA.GRIEVE.get());
      }

      EntityRelationship.super.onTragedy(cause, burialSite, type, with);
   }

   public GiftSaturation getGiftSaturation() {
      return this.giftSaturation;
   }

   public void readFromNbt(CompoundTag nbt) {
      this.giftSaturation.readFromNbt(nbt.m_128437_("giftSaturationQueue", 8));
   }

   public void writeToNbt(CompoundTag nbt) {
      nbt.m_128365_("giftSaturationQueue", this.giftSaturation.toNbt());
   }

   public interface Predicate extends BiPredicate<CompassionateEntity<?>, Entity> {
      boolean test(CompassionateEntity<?> var1, UUID var2);

      default boolean test(CompassionateEntity<?> villager, Entity partner) {
         return partner != null && this.test(villager, partner.m_20148_());
      }

      default Relationship.Predicate or(Relationship.Predicate b) {
         return (villager, partner) -> this.test(villager, partner) || b.test(villager, partner);
      }

      default Relationship.Predicate negate() {
         return (villager, partner) -> !this.test(villager, partner);
      }

      default BiPredicate<VillagerLike<?>, ServerPlayer> asConstraint() {
         return (villager, player) -> villager instanceof CompassionateEntity && this.test((CompassionateEntity<?>)villager, player);
      }
   }
}
