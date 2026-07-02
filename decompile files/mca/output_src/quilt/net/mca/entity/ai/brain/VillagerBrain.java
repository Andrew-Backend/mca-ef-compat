package quilt.net.mca.entity.ai.brain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4140;
import net.minecraft.class_4168;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.Config;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.ActivityMCA;
import quilt.net.mca.entity.ai.Chore;
import quilt.net.mca.entity.ai.Memories;
import quilt.net.mca.entity.ai.MemoryModuleTypeMCA;
import quilt.net.mca.entity.ai.Mood;
import quilt.net.mca.entity.ai.MoodGroup;
import quilt.net.mca.entity.ai.MoveState;
import quilt.net.mca.entity.ai.relationship.Personality;
import quilt.net.mca.util.network.datasync.CDataManager;
import quilt.net.mca.util.network.datasync.CDataParameter;
import quilt.net.mca.util.network.datasync.CEnumParameter;
import quilt.net.mca.util.network.datasync.CParameter;

public class VillagerBrain<E extends class_1308 & VillagerLike<E>> {
   private static final CDataParameter<class_2487> MEMORIES = CParameter.create("memories", new class_2487());
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

   public static <E2 extends class_1297> CDataManager.Builder<E2> createTrackedData(CDataManager.Builder<E2> builder) {
      return builder.addAll(MEMORIES, PERSONALITY, MOOD, MOVE_STATE, ACTIVE_CHORE, CHORE_ASSIGNING_PLAYER, PANICKING, WEAR_ARMOR);
   }

   public VillagerBrain(E entity) {
      this.entity = entity;
   }

   public void think() {
      if (this.entity.getTrackedValue(ACTIVE_CHORE) != Chore.NONE) {
         this.entity.method_18868().method_24538().ifPresent(activity -> {
            if (!activity.equals(ActivityMCA.CHORE.get())) {
               this.entity.method_18868().method_24526((class_4168)ActivityMCA.CHORE.get());
            }
         });
      }

      boolean panicking = this.entity.method_18868().method_18906(class_4168.field_18599);
      if (panicking != this.entity.getTrackedValue(PANICKING)) {
         this.entity.setTrackedValue(PANICKING, panicking);
      }

      if (this.entity.field_6012 % 20 != 0) {
         this.updateMoveState();
      }

      if (this.entity.field_6012 % Math.max(1, Config.getInstance().interactionFatigueCooldown) == 0) {
         class_2487 nbt = this.entity.getTrackedValue(MEMORIES);
         if (nbt != null) {
            for (String uuid : nbt.method_10541()) {
               Memories memories = Memories.fromCNBT(this.entity, nbt.method_10562(uuid));
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

   public Optional<class_1657> getJobAssigner() {
      return this.entity.getTrackedValue(CHORE_ASSIGNING_PLAYER).map(id -> this.entity.method_37908().method_18470(id));
   }

   public void abandonJob() {
      this.entity.method_18868().method_24526(class_4168.field_18595);
      this.entity.setTrackedValue(ACTIVE_CHORE, Chore.NONE);
      this.entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.empty());
      this.resetsBrain();
   }

   public void assignJob(Chore chore, class_1657 player) {
      this.entity.method_18868().method_24526((class_4168)ActivityMCA.CHORE.get());
      this.entity.setTrackedValue(ACTIVE_CHORE, chore);
      this.entity.setTrackedValue(CHORE_ASSIGNING_PLAYER, Optional.of(player.method_5667()));
      this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
      this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.STAYING.get());
      this.resetsBrain();
   }

   public void randomize() {
      this.entity.setTrackedValue(PERSONALITY, Personality.getRandom());
      this.entity.setTrackedValue(MOOD, this.entity.method_37908().field_9229.method_43048(31) + -15);
   }

   public void setPersonality(Personality p) {
      this.entity.setTrackedValue(PERSONALITY, p);
   }

   public void updateMemories(Memories memories) {
      class_2487 nbt = this.entity.getTrackedValue(MEMORIES);
      nbt = nbt == null ? new class_2487() : nbt.method_10553();
      nbt.method_10566(memories.getPlayerUUID().toString(), memories.toCNBT());
      this.entity.setTrackedValue(MEMORIES, nbt);
   }

   public Map<UUID, Memories> getMemories() {
      class_2487 nbt = this.entity.getTrackedValue(MEMORIES);
      Map<UUID, Memories> memories = new HashMap<>();

      for (String uuid : nbt.method_10541()) {
         memories.put(UUID.fromString(uuid), Memories.fromCNBT(this.entity, nbt.method_10562(uuid)));
      }

      return memories;
   }

   public Memories getMemoriesForPlayer(class_1657 player) {
      class_2487 nbt = this.entity.getTrackedValue(MEMORIES);
      nbt = nbt == null ? new class_2487() : nbt;
      class_2487 compoundTag = nbt.method_10562(player.method_5667().toString());
      Memories returnMemories = Memories.fromCNBT(this.entity, compoundTag);
      if (returnMemories == null) {
         returnMemories = new Memories(this, player.method_37908().method_8532(), player.method_5667());
         nbt.method_10566(player.method_5667().toString(), returnMemories.toCNBT());
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

   public void setMoveState(MoveState state, @Nullable class_1657 leader) {
      this.entity.setTrackedValue(MOVE_STATE, state);
      if (state == MoveState.MOVE) {
         this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
         this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.STAYING.get());
      }

      if (state == MoveState.STAY) {
         this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
         this.entity.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.STAYING.get(), true);
      }

      if (state == MoveState.FOLLOW) {
         this.entity.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(), leader);
         this.entity.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.STAYING.get());
         this.abandonJob();
      }

      this.resetsBrain();
   }

   private void resetsBrain() {
      if (this.entity.asEntity() instanceof VillagerEntityMCA villager) {
         villager.method_19179((class_3218)villager.method_37908());
      }
   }

   public void setArmorWear(boolean s) {
      this.entity.setTrackedValue(WEAR_ARMOR, s);
   }

   public boolean getArmorWear() {
      return this.entity.getTrackedValue(WEAR_ARMOR);
   }

   public void setGrieving() {
      this.entity.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.LAST_GRIEVE.get(), -168000L);
   }

   public void justGrieved() {
      this.entity.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.LAST_GRIEVE.get(), this.entity.method_37908().method_8510());
   }

   public boolean shouldGrieve() {
      Optional<Long> memory = this.entity.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.LAST_GRIEVE.get());
      if (memory.isPresent()) {
         return this.entity.method_37908().method_8510() - memory.get() > 168000L;
      }

      this.entity
         .method_18868()
         .method_18878((class_4140)MemoryModuleTypeMCA.LAST_GRIEVE.get(), this.entity.method_37908().method_8510() - this.random.nextLong(168000L));
      return false;
   }

   public void updateMoveState() {
      if (this.getMoveState() == MoveState.FOLLOW && this.entity.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isEmpty()) {
         if (this.entity.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.STAYING.get()).isPresent()) {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.STAY);
         } else if (this.entity.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent()) {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.FOLLOW);
         } else {
            this.entity.setTrackedValue(MOVE_STATE, MoveState.MOVE);
         }
      }
   }

   public void rewardHearts(class_3222 player, int hearts) {
      Memories memory = this.entity.getVillagerBrain().getMemoriesForPlayer(player);
      if (hearts != 0) {
         if (hearts > 0) {
            this.entity.method_37908().method_8421(this.entity, (byte)16);
         } else {
            this.entity.method_37908().method_8421(this.entity, (byte)15);
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
