package forge.net.mca.entity.ai.brain;

import forge.net.mca.Config;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.ActivityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.entity.ai.Mood;
import forge.net.mca.entity.ai.MoodGroup;
import forge.net.mca.entity.ai.MoveState;
import forge.net.mca.entity.ai.relationship.Personality;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CEnumParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.Nullable;

public class VillagerBrain<E extends Mob & VillagerLike<E>> {
   private static final CDataParameter<CompoundTag> MEMORIES = CParameter.create("memories", new CompoundTag());
   private static final CEnumParameter<Personality> PERSONALITY = CParameter.create("personality", Personality.UNASSIGNED);
   private static final CDataParameter<Integer> MOOD = CParameter.create("mood", 0);
   private static final CEnumParameter<MoveState> MOVE_STATE = CParameter.create("moveState", MoveState.MOVE);
   private static final CEnumParameter<Chore> ACTIVE_CHORE = CParameter.create("activeChore", Chore.NONE);
   private static final CDataParameter<Optional<UUID>> CHORE_ASSIGNING_PLAYER = CParameter.create("choreAssigningPlayer", Optional.empty());
   private static final CDataParameter<Boolean> PANICKING = CParameter.create("isPanicking", false);
   private static final CDataParameter<Boolean> WEAR_ARMOR = CParameter.create("wearArmor", false);
   private static final long GRIEVE_COOLDOWN = 168000L;
   private final Random random = new Random();
   private final E entity;

   public static <E2 extends Entity> CDataManager.Builder<E2> createTrackedData(CDataManager.Builder<E2> builder) {
      return builder.addAll(MEMORIES, PERSONALITY, MOOD, MOVE_STATE, ACTIVE_CHORE, CHORE_ASSIGNING_PLAYER, PANICKING, WEAR_ARMOR);
   }

   public VillagerBrain(E entity) {
      this.entity = entity;
   }

   public void think() {
      if (this.entity.getTrackedValue(ACTIVE_CHORE) != Chore.NONE) {
         this.entity.m_6274_().m_21968_().ifPresent(activity -> {
            if (!activity.equals(ActivityMCA.CHORE.get())) {
               this.entity.m_6274_().m_21889_((Activity)ActivityMCA.CHORE.get());
            }
         });
      }

      boolean panicking = this.entity.m_6274_().m_21954_(Activity.f_37984_);
      if (panicking != this.entity.getTrackedValue(PANICKING)) {
         this.entity.setTrackedValue(PANICKING, panicking);
      }

      if (this.entity.f_19797_ % 20 != 0) {
         this.updateMoveState();
      }

      if (this.entity.f_19797_ % Math.max(1, Config.getInstance().interactionFatigueCooldown) == 0) {
         CompoundTag nbt = this.entity.getTrackedValue(MEMORIES);
         if (nbt != null) {
            for (String uuid : nbt.m_128431_()) {
               Memories memories = Memories.fromCNBT(this.entity, nbt.m_128469_(uuid));
               int fatigue = memories.getInteractionFatigue();
               if (fatigue > 0) {
                  memories.setInteractionFatigue(fatigue - 1);
               }
            }
         }
      }
   }

   public Chore getCurrentJob() {
      return this.entity.getTrackedValue(ACTIVE_CHORE);
   }

   public Optional<Player> getJobAssigner() {
      return this.entity.getTrackedValue(CHORE_ASSIGNING_PLAYER).map(id -> this.entity.m_9236_().m_46003_(id));
   }

   public void abandonJob() {
      this.entity.m_6274_().m_21889_(Activity.f_37979_);
      this.entity.setTrackedValue(ACTIVE_CHORE, Chore.NONE);
      this.entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.empty());
      this.resetsBrain();
   }

   public void assignJob(Chore chore, Player player) {
      this.entity.m_6274_().m_21889_((Activity)ActivityMCA.CHORE.get());
      this.entity.setTrackedValue(ACTIVE_CHORE, chore);
      this.entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.of(player.m_20148_()));
      this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
      this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get());
      this.resetsBrain();
   }

   public void randomize() {
      this.entity.setTrackedValue(PERSONALITY, Personality.getRandom());
      this.entity.setTrackedValue(MOOD, this.entity.m_9236_().f_46441_.m_188503_(31) + -15);
   }

   public void setPersonality(Personality p) {
      this.entity.setTrackedValue(PERSONALITY, p);
   }

   public void updateMemories(Memories memories) {
      CompoundTag nbt = this.entity.getTrackedValue(MEMORIES);
      nbt = nbt == null ? new CompoundTag() : nbt.m_6426_();
      nbt.m_128365_(memories.getPlayerUUID().toString(), memories.toCNBT());
      this.entity.setTrackedValue(MEMORIES, nbt);
   }

   public Map<UUID, Memories> getMemories() {
      CompoundTag nbt = this.entity.getTrackedValue(MEMORIES);
      Map<UUID, Memories> memories = new HashMap<>();

      for (String uuid : nbt.m_128431_()) {
         memories.put(UUID.fromString(uuid), Memories.fromCNBT(this.entity, nbt.m_128469_(uuid)));
      }

      return memories;
   }

   public Memories getMemoriesForPlayer(Player player) {
      CompoundTag nbt = this.entity.getTrackedValue(MEMORIES);
      nbt = nbt == null ? new CompoundTag() : nbt;
      CompoundTag compoundTag = nbt.m_128469_(player.m_20148_().toString());
      Memories returnMemories = Memories.fromCNBT(this.entity, compoundTag);
      if (returnMemories == null) {
         returnMemories = new Memories(this, player.m_9236_().m_46468_(), player.m_20148_());
         nbt.m_128365_(player.m_20148_().toString(), returnMemories.toCNBT());
         this.entity.setTrackedValue(MEMORIES, nbt);
      }

      return returnMemories;
   }

   public Personality getPersonality() {
      return this.entity.getTrackedValue(PERSONALITY);
   }

   public Mood getMood() {
      return MoodGroup.INSTANCE.getMood(this.entity.getTrackedValue(MOOD));
   }

   public boolean isPanicking() {
      return this.entity.getTrackedValue(PANICKING);
   }

   public void modifyMoodValue(int mood) {
      this.entity.setTrackedValue(MOOD, MoodGroup.clampMood(this.getMoodValue() + mood));
   }

   public int getMoodValue() {
      return this.entity.getTrackedValue(MOOD);
   }

   public MoveState getMoveState() {
      return this.entity.getTrackedValue(MOVE_STATE);
   }

   public void setMoveState(MoveState state, @Nullable Player leader) {
      this.entity.setTrackedValue(MOVE_STATE, state);
      if (state == MoveState.MOVE) {
         this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
         this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get());
      }

      if (state == MoveState.STAY) {
         this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
         this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get(), true);
      }

      if (state == MoveState.FOLLOW) {
         this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(), leader);
         this.entity.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get());
         this.abandonJob();
      }

      this.resetsBrain();
   }

   private void resetsBrain() {
      if (this.entity.asEntity() instanceof VillagerEntityMCA villager) {
         villager.m_35483_((ServerLevel)villager.m_9236_());
      }
   }

   public void setArmorWear(boolean s) {
      this.entity.setTrackedValue(WEAR_ARMOR, s);
   }

   public boolean getArmorWear() {
      return this.entity.getTrackedValue(WEAR_ARMOR);
   }

   public void setGrieving() {
      this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.LAST_GRIEVE.get(), -168000L);
   }

   public void justGrieved() {
      this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.LAST_GRIEVE.get(), this.entity.m_9236_().m_46467_());
   }

   public boolean shouldGrieve() {
      Optional<Long> memory = this.entity.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.LAST_GRIEVE.get());
      if (memory.isPresent()) {
         return this.entity.m_9236_().m_46467_() - memory.get() > 168000L;
      }

      this.entity.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.LAST_GRIEVE.get(), this.entity.m_9236_().m_46467_() - this.random.nextLong(168000L));
      return false;
   }

   public void updateMoveState() {
      if (this.getMoveState() == MoveState.FOLLOW && this.entity.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isEmpty()) {
         if (this.entity.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get()).isPresent()) {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.STAY);
         } else if (this.entity.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent()) {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.FOLLOW);
         } else {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.MOVE);
         }
      }
   }

   public void rewardHearts(ServerPlayer player, int hearts) {
      Memories memory = this.entity.getVillagerBrain().getMemoriesForPlayer(player);
      if (hearts != 0) {
         if (hearts > 0) {
            this.entity.m_9236_().m_7605_(this.entity, (byte)16);
         } else {
            this.entity.m_9236_().m_7605_(this.entity, (byte)15);
            if (this.entity.getVillagerBrain().getPersonality() == Personality.SENSITIVE) {
               hearts *= 2;
            }
         }

         memory.modInteractionFatigue(1);
         memory.modHearts(hearts);
         CriterionMCA.HEARTS_CRITERION.trigger(player, memory.getHearts(), hearts, "interaction");
         this.entity.getVillagerBrain().modifyMoodValue(hearts);
      }
   }
}
