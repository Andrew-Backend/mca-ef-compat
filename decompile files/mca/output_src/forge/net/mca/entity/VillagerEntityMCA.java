package forge.net.mca.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.MCAClient;
import forge.net.mca.ParticleTypesMCA;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.SoundsMCA;
import forge.net.mca.TagsMCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.ai.BreedableRelationship;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.ConversationManager;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.LongTermMemory;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.entity.ai.Mood;
import forge.net.mca.entity.ai.MoveState;
import forge.net.mca.entity.ai.Residency;
import forge.net.mca.entity.ai.StatusEffectDangerSet;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import forge.net.mca.entity.ai.brain.VillagerTasksMCA;
import forge.net.mca.entity.ai.pathfinder.VillagerNavigation;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.Personality;
import forge.net.mca.entity.ai.relationship.VillagerDimensions;
import forge.net.mca.entity.interaction.VillagerCommandHandler;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.network.c2s.InteractionVillagerMessage;
import forge.net.mca.resources.Names;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillagerTrackerManager;
import forge.net.mca.util.InventoryUtils;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.Zombie.ZombieGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerEntityMCA
   extends Villager
   implements VillagerLike<VillagerEntityMCA>,
   MenuProvider,
   CompassionateEntity<BreedableRelationship>,
   CrossbowAttackMob {
   final UUID EXTRA_HEALTH_EFFECT_ID = UUID.fromString("87f56a96-686f-4796-b035-22e16ee9e038");
   private static final CDataParameter<Float> INFECTION_PROGRESS = CParameter.create("infectionProgress", 0.0F);
   private static final CDataParameter<Integer> GROWTH_AMOUNT = CParameter.create("growthAmount", -AgeState.getMaxAge());
   private static final CDataManager<VillagerEntityMCA> DATA = createTrackedData(VillagerEntityMCA.class).build();
   public final ConversationManager conversationManager = new ConversationManager(this);
   private final VillagerBrain<VillagerEntityMCA> mcaBrain = new VillagerBrain(this);
   private final LongTermMemory longTermMemory = new LongTermMemory(this);
   private final Genetics genetics = new Genetics(this);
   private final Traits traits = new Traits(this);
   private final Residency residency = new Residency(this);
   private final BreedableRelationship relations = new BreedableRelationship(this);
   private final VillagerCommandHandler interactions = new VillagerCommandHandler(this);
   private final UpdatableInventory inventory = new UpdatableInventory(27);
   private final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.UNASSIGNED);
   private GameProfile gameProfile;
   private VillagerLike.PlayerModel playerModel;
   private int despawnDelay;
   private int burned;
   private long lastHit = 0L;
   private int prevGrowthAmount;
   private boolean interactedWith;
   private static final int RECALCULATE_DIMENSIONS_EVERY_N_TICKS = 100;
   long lastCooldown = 0L;

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
      return VillagerLike.createTrackedData(type)
         .addAll(INFECTION_PROGRESS, GROWTH_AMOUNT)
         .add(Residency::createTrackedData)
         .add(BreedableRelationship::createTrackedData);
   }

   public VillagerEntityMCA(EntityType<VillagerEntityMCA> type, Level w, Gender gender) {
      super(type, w);
      this.inventory.m_19164_(this::onInvChange);
      this.genetics.setGender(gender);
   }

   @Override
   public VillagerLike.PlayerModel getPlayerModel() {
      return this.playerModel;
   }

   @Override
   public boolean isBurned() {
      return this.burned > 0;
   }

   public void m_35510_() {
      super.m_35510_();
      if (!this.m_9236_().f_46443_) {
         Optional<Village> village = this.residency.getHomeVillage();
         if (village.isPresent() && Config.getInstance().villagerRestockNotification) {
            village.get().broadCastMessage((ServerLevel)this.m_9236_(), "events.restock", this.m_7755_().getString());
         }
      }
   }

   protected void m_8097_() {
      super.m_8097_();
      this.getTypeDataManager().register(this);
   }

   @Override
   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   @Override
   public void updateCustomSkin() {
      if (!MCA.isBlankString(this.getTrackedValue(CUSTOM_SKIN))) {
         this.gameProfile = new GameProfile(null, this.getTrackedValue(CUSTOM_SKIN));
         SkullBlockEntity.m_155738_(this.gameProfile, profile -> this.gameProfile = profile);
      } else {
         this.gameProfile = null;
      }
   }

   @Override
   public CDataManager<VillagerEntityMCA> getTypeDataManager() {
      return DATA;
   }

   protected PathNavigation m_6037_(Level world) {
      return new VillagerNavigation(this, world);
   }

   protected Brain<?> m_8075_(Dynamic<?> dynamic) {
      return VillagerTasksMCA.initializeTasks(this, VillagerTasksMCA.createProfile().m_22073_(dynamic));
   }

   public void m_35483_(ServerLevel world) {
      Brain<VillagerEntityMCA> brain = this.getMCABrain();
      brain.m_21933_(world, this);
      this.f_20939_ = brain.m_21973_();
      VillagerTasksMCA.initializeTasks(this, this.getMCABrain());
   }

   public Brain<VillagerEntityMCA> getMCABrain() {
      return this.f_20939_;
   }

   @Override
   public Genetics getGenetics() {
      return this.genetics;
   }

   @Override
   public Traits getTraits() {
      return this.traits;
   }

   public BreedableRelationship getRelationships() {
      return this.relations;
   }

   @Override
   public VillagerBrain<?> getVillagerBrain() {
      return this.mcaBrain;
   }

   public LongTermMemory getLongTermMemory() {
      return this.longTermMemory;
   }

   public Residency getResidency() {
      return this.residency;
   }

   public VillagerCommandHandler getInteractions() {
      return this.interactions;
   }

   protected Component m_5677_() {
      return this.getProfessionText();
   }

   @Nullable
   public SpawnGroupData m_6518_(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt
   ) {
      SpawnGroupData data = super.m_6518_(world, difficulty, spawnReason, entityData, entityNbt);
      this.initialize(spawnReason);
      this.setAgeState(AgeState.byCurrentAge(this.m_146764_()));
      FamilyTreeNode entry = this.getRelationships().getFamilyEntry();
      if (!FamilyTreeNode.isValid(entry.father()) && !FamilyTreeNode.isValid(entry.mother())) {
         FamilyTree tree = FamilyTree.get(world.m_6018_());
         FamilyTreeNode father = tree.getOrCreate(UUID.randomUUID(), Names.pickCitizenName(Gender.MALE), Gender.MALE);
         FamilyTreeNode mother = tree.getOrCreate(UUID.randomUUID(), Names.pickCitizenName(Gender.FEMALE), Gender.FEMALE);
         father.setDeceased(true);
         mother.setDeceased(true);
         entry.setFather(father);
         entry.setMother(mother);
      }

      return data;
   }

   public final VillagerProfession getProfession() {
      return this.m_7141_().m_35571_();
   }

   public final void setProfession(VillagerProfession profession) {
      this.m_34375_(this.m_7141_().m_35565_(profession));
      this.m_35483_((ServerLevel)this.m_9236_());
   }

   @Override
   public ResourceLocation getProfessionId() {
      return BuiltInRegistries.f_256735_.m_7981_(this.getProfession());
   }

   @Override
   public boolean isProfessionImportant() {
      return ProfessionsMCA.isImportant.contains(this.getProfession());
   }

   @Override
   public boolean requiresHome() {
      return !ProfessionsMCA.needsNoHome.contains(this.getProfession()) && this.getDespawnDelay() <= 0;
   }

   @Override
   public boolean canTradeWithProfession() {
      return !ProfessionsMCA.canNotTrade.contains(this.getProfession()) || this.f_35261_ != null && !this.f_35261_.isEmpty();
   }

   public void m_34375_(VillagerData data) {
      boolean hasChanged = !this.m_9236_().f_46443_ && this.getProfession() != data.m_35571_() && data.m_35571_() != ProfessionsMCA.OUTLAW.get();
      super.m_34375_(data);
      if (hasChanged) {
         this.randomizeClothes();
         this.getRelationships().getFamilyEntry().setProfession(data.m_35571_());
      }
   }

   public void m_6863_(boolean isBaby) {
      this.m_146762_(isBaby ? -AgeState.getMaxAge() : 0);
   }

   public void m_6593_(@Nullable Component name) {
      super.m_6593_(name);
      if (name != null) {
         this.setName(name.getString());
      }
   }

   public void m_146762_(int age) {
      super.m_146762_(age);
      if (age != -2) {
         this.setTrackedValue(GROWTH_AMOUNT, age);
         this.setAgeState(AgeState.byCurrentAge(age));
         AgeState current = this.getAgeState();
         AgeState next = current.getNext();
         if (current != next) {
            this.dimensions.interpolate(current, next, AgeState.getDelta(age));
         } else {
            this.dimensions.set(current);
         }
      }
   }

   public boolean m_7327_(Entity target) {
      this.attackedEntity(target);
      double baseDamage = this.m_21133_(Attributes.f_22281_);
      float damage = (float)(baseDamage * (this.getProfession() == ProfessionsMCA.GUARD.get() ? 3.0F : 1.0F));
      float knockback = (float)this.m_21133_(Attributes.f_22282_);
      if (this.getTraits().hasTrait(Traits.WEAK)) {
         damage *= 0.75F;
      }

      if (this.getTraits().hasTrait(Traits.TOUGH)) {
         damage *= 1.25F;
      }

      if (target instanceof LivingEntity livingEntity) {
         damage += EnchantmentHelper.m_44833_(this.m_21205_(), livingEntity.m_6336_());
         knockback += EnchantmentHelper.m_44894_(this);
      }

      int i = EnchantmentHelper.m_44914_(this);
      if (i > 0) {
         target.m_20254_(i * 4);
      }

      boolean damageDealt = target.m_6469_(this.m_9236_().m_269111_().m_269333_(this), damage);
      if (damageDealt) {
         if (knockback > 0.0F && target instanceof LivingEntity livingEntity) {
            livingEntity.m_147240_(
               knockback / 2.0F, Mth.m_14031_(this.m_146908_() * (float) (Math.PI / 180.0)), -Mth.m_14089_(this.m_146908_() * (float) (Math.PI / 180.0))
            );
            this.m_20256_(this.m_20184_().m_82542_(0.6, 1.0, 0.6));
         }

         this.m_19970_(this, target);
         this.m_21335_(target);
      }

      return damageDealt;
   }

   private void attackedEntity(Entity target) {
      if (target instanceof Player player) {
         this.pardonPlayers(player);
      }
   }

   private void pardonPlayers() {
      this.pardonPlayers(1);
   }

   public void pardonPlayers(int amount) {
      int bounty = this.getSmallBounty();
      if (bounty <= amount) {
         this.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get());
         this.m_6274_().m_21936_(MemoryModuleType.f_26372_);
         this.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.HIT_BY_PLAYER.get());
      } else {
         this.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), bounty - amount);
      }
   }

   private void pardonPlayers(Player attacker) {
      this.pardonPlayers();
      int bounty = this.getSmallBounty();
      if (bounty <= this.getMaxWarnings(attacker)) {
         this.m_6274_().m_21936_(MemoryModuleType.f_26372_);
      }
   }

   public boolean canInteractWithItemStackInHand(ItemStack stack) {
      return stack.m_41720_() != ItemsMCA.VILLAGER_EDITOR.get()
         && stack.m_41720_() != ItemsMCA.NEEDLE_AND_THREAD.get()
         && stack.m_41720_() != ItemsMCA.COMB.get()
         && stack.m_41720_() != ItemsMCA.POTION_OF_FEMINITY.get()
         && stack.m_41720_() != ItemsMCA.POTION_OF_MASCULINITY.get();
   }

   public final InteractionResult m_7111_(Player player, Vec3 pos, @NotNull InteractionHand hand) {
      if (this.m_20202_() != null && this.m_20202_().equals(player)) {
         return InteractionResult.PASS;
      }

      ItemStack stack = player.m_21120_(hand);
      boolean isOnBlacklist = Config.getInstance().villagerInteractionItemBlacklist.contains(BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_()).toString());
      if (hand.equals(InteractionHand.MAIN_HAND)
         && !isOnBlacklist
         && !stack.m_204117_(TagsMCA.Items.VILLAGER_EGGS)
         && this.canInteractWithItemStackInHand(stack)
         && !this.getVillagerBrain().isPanicking()) {
         this.getDialogueType(player);
         if (!player.m_6144_()) {
            this.playWelcomeSound();
            this.interactedWith = true;
            return this.interactions.interactAt(player, pos, hand);
         }

         if (player instanceof ServerPlayer e) {
            NetworkHandler.sendToPlayer(new InteractionVillagerMessage("trade", this.f_19820_), e);
         }
      }

      return super.m_7111_(player, pos, hand);
   }

   public InteractionResult m_6071_(Player player, InteractionHand hand) {
      if (this.m_20202_() != null && this.m_20202_().equals(player)) {
         return InteractionResult.PASS;
      }

      ItemStack stack = player.m_21120_(hand);
      if (!stack.m_204117_(TagsMCA.Items.VILLAGER_EGGS)
         && this.m_6084_()
         && !this.m_35306_()
         && !this.m_5803_()
         && this.canInteractWithItemStackInHand(stack)
         && !this.getVillagerBrain().isPanicking()) {
         if (this.m_6162_()) {
            this.copiedSayNo();
         } else {
            boolean hasOffers = this.hasTradeOffers();
            if (hand == InteractionHand.MAIN_HAND) {
               if (!hasOffers && !this.m_9236_().f_46443_) {
                  this.copiedSayNo();
               }

               player.m_36220_(Stats.f_12940_);
            }

            if (hasOffers && !this.m_9236_().f_46443_) {
               this.copiedBeginTradeWith(player);
            }
         }

         return InteractionResult.m_19078_(this.m_9236_().f_46443_);
      } else {
         return InteractionResult.PASS;
      }
   }

   private void copiedSayNo() {
      this.m_35319_(40);
      if (!this.m_9236_().m_5776_()) {
         this.m_5496_(this.getNoSound(), this.m_6121_(), this.m_6100_());
      }
   }

   public boolean hasTradeOffers() {
      return !this.m_6616_().isEmpty();
   }

   public void beginTradeWith(Player customer) {
      this.copiedBeginTradeWith(customer);
   }

   private void copiedBeginTradeWith(Player customer) {
      this.copiedPrepareOffersFor(customer);
      this.m_7189_(customer);
      this.m_45301_(customer, this.m_5446_(), this.m_7141_().m_35576_());
   }

   private void copiedPrepareOffersFor(Player player) {
      int reputation = this.m_35532_(player);
      if (reputation != 0) {
         for (MerchantOffer tradeOffer : this.m_6616_()) {
            tradeOffer.m_45353_(-Mth.m_14143_(reputation * tradeOffer.m_45378_()));
         }
      }

      if (player.m_21023_(MobEffects.f_19595_)) {
         MobEffectInstance statusEffect = player.m_21124_(MobEffects.f_19595_);
         int amplifier = statusEffect.m_19564_();

         for (MerchantOffer tradeOffer2 : this.m_6616_()) {
            double d = 0.3 + 0.0625 * amplifier;
            int k = (int)Math.floor(d * tradeOffer2.m_45352_().m_41613_());
            tradeOffer2.m_45353_(-Math.max(k, 1));
         }
      }
   }

   public VillagerEntityMCA createChild(ServerLevel world, AgeableMob partner) {
      VillagerEntityMCA child = partner instanceof VillagerEntityMCA partnerVillager
         ? this.relations.getPregnancy().createChild(Gender.getRandom(), partnerVillager)
         : this.relations.getPregnancy().createChild(Gender.getRandom());
      child.m_34375_(child.m_7141_().m_35567_(this.getRandomType(partner)));
      child.m_6518_(world, world.m_6436_(child.m_20183_()), MobSpawnType.BREEDING, null, null);
      return child;
   }

   private VillagerType getRandomType(AgeableMob partner) {
      double d = this.f_19796_.m_188500_();
      if (d < 0.5) {
         return VillagerType.m_204073_(this.m_9236_().m_204166_(this.m_20183_()));
      } else {
         return d < 0.75 ? this.m_7141_().m_35560_() : ((Villager)partner).m_7141_().m_35560_();
      }
   }

   public final boolean m_6469_(DamageSource source, float damageAmount) {
      if (this.m_20202_() instanceof Player) {
         return super.m_6469_(source, 0.0F);
      }

      if (!Config.getInstance().canHurtBabies && !source.m_269533_(DamageTypeTags.f_276146_) && this.getAgeState() == AgeState.BABY) {
         if (source.m_7639_() instanceof Player && this.requestCooldown()) {
            this.sendEventMessage(Component.m_237115_("villager.baby_hit"));
         }

         return super.m_6469_(source, 0.0F);
      } else {
         if (this.getProfession() == ProfessionsMCA.GUARD.get()) {
            damageAmount *= 0.5F;
         }

         if (this.getTraits().hasTrait(Traits.TOUGH)) {
            damageAmount *= 0.75F;
         }

         if (!this.m_9236_().f_46443_) {
            if (source.m_7639_() instanceof Player player) {
               if (this.m_9236_().m_46467_() - this.lastHit > 40L) {
                  this.lastHit = this.m_9236_().m_46467_();
                  if ((!this.isGuard() || this.getSmallBounty() == 0) && this.requestCooldown()) {
                     if (this.m_21223_() < this.m_21233_() / 2.0F) {
                        this.sendChatMessage(player, "villager.badly_hurt");
                     } else {
                        this.sendChatMessage(player, "villager.hurt");
                     }
                  }
               }

               int trustIssues = (int)((1.0 - this.m_21223_() / this.m_21233_() * 0.75) * (3.0 + 2.0 * damageAmount));
               this.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-trustIssues);
            }

            if (source.m_7640_() instanceof Zombie
               && this.getProfession() != ProfessionsMCA.GUARD.get()
               && Config.getInstance().enableInfection
               && this.f_19796_.m_188501_() < Config.getInstance().zombieBiteInfectionChance
               && this.f_19796_.m_188501_() > (this.m_7141_().m_35576_() - 1) * Config.getInstance().infectionChanceDecreasePerLevel
               && (this.getResidency().getHomeVillage().filter(v -> v.hasBuilding("infirmary")).isEmpty() || this.f_19796_.m_188499_())) {
               this.setInfected(true);
               this.sendChatToAllAround("villager.bitten");
               MCA.LOGGER.info("{} has been infected", this.m_7755_());
            }
         }

         Entity attacker = source != null ? source.m_7639_() : null;
         if (attacker instanceof LivingEntity livingEntity && !this.isHostile() && !this.isFriend(attacker.m_6095_())) {
            this.m_6274_().m_21886_((MemoryModuleType)MemoryModuleTypeMCA.HIT_BY_PLAYER.get(), Optional.of(livingEntity));
            this.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), this.getSmallBounty() + 1);
            Vec3 pos = this.m_20182_();
            this.m_9236_().m_45976_(VillagerEntityMCA.class, new AABB(pos, pos).m_82400_(32.0)).forEach(v -> {
               if (this.m_20280_(v) <= (v.m_5448_() == null ? 1024 : 64)) {
                  if (attacker instanceof Player player) {
                     int bounty = v.getSmallBounty();
                     if (v.isGuard()) {
                        int maxWarning = v.getMaxWarnings(player);
                        if (bounty > maxWarning) {
                           v.m_6274_().m_21879_(MemoryModuleType.f_26372_, livingEntity);
                        } else if (bounty == 0 || bounty == maxWarning) {
                           v.sendChatMessage(player, "villager.warning");
                        }

                        v.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), bounty + 1);
                     }
                  } else if (v.isGuard()) {
                     v.m_6274_().m_21879_(MemoryModuleType.f_26372_, livingEntity);
                  }
               }
            });
         }

         if (attacker instanceof IronGolem golem) {
            golem.m_21662_();

            try {
               Field targetSelector = Mob.class.getDeclaredField("targetSelector");
               ((GoalSelector)targetSelector.get(attacker)).m_25386_().forEach(g -> {
                  if (g.m_26015_() instanceof TargetGoal) {
                     g.m_26015_().m_8041_();
                  }
               });
            } catch (NoSuchFieldException | IllegalAccessException var6) {
            }

            damageAmount *= 0.0F;
         }

         return super.m_6469_(source, damageAmount);
      }
   }

   private boolean requestCooldown() {
      if (this.m_9236_().m_46467_() - this.lastCooldown > 100L) {
         this.lastCooldown = this.m_9236_().m_46467_();
         return true;
      } else {
         return false;
      }
   }

   public boolean isGuard() {
      return this.getProfession() == ProfessionsMCA.GUARD.get() || this.getProfession() == ProfessionsMCA.ARCHER.get();
   }

   public int getSmallBounty() {
      return Objects.requireNonNull(this.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get())).orElse(0);
   }

   public boolean isHitBy(ServerPlayer player) {
      return Objects.requireNonNull(this.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.HIT_BY_PLAYER.get())).filter(v -> v == player).isPresent();
   }

   private int getMaxWarnings(Player attacker) {
      return this.getVillagerBrain().getMemoriesForPlayer(attacker).getHearts() / Math.max(1, Config.getInstance().heartsForPardonHit);
   }

   public void m_8107_() {
      this.m_21203_();
      super.m_8107_();
      this.burned--;
      if (this.m_6060_()) {
         this.burned = Config.getInstance().burnedClothingTickLength;
      }

      if (this.burned > 0) {
         this.spawnBurntParticles();
      }

      if (!this.m_9236_().f_46443_) {
         if (this.f_19797_ % 200 == 0 && this.m_21223_() < this.m_21233_() && !this.m_6274_().m_257414_(MemoryModuleType.f_26372_).isPresent()) {
            ItemStack food = this.m_21205_();
            if (food.m_41614_()) {
               this.m_5584_(this.m_9236_(), food);
            } else if (!this.findAndEquipToMain(
               i -> i.m_41614_()
                  && i.m_41720_().m_41473_().m_38744_() > 0
                  && i.m_41720_()
                     .m_41473_()
                     .m_38749_()
                     .stream()
                     .noneMatch(e -> StatusEffectDangerSet.isDanger.contains(((MobEffectInstance)e.getFirst()).m_19544_()))
            )) {
               this.m_5634_(1.0F);
            }
         }

         this.tickDespawnDelay();
         this.residency.tick();
         this.relations.tick(this.f_19797_);
         this.inventory.update(this);
         if (this.f_19797_ % Config.getInstance().pardonPlayerTicks == 0) {
            this.pardonPlayers();
         }

         this.mcaBrain.think();
         if (this.f_19797_ % Config.getInstance().giftDesaturationReset == 0) {
            this.getRelationships().getGiftSaturation().pop();
         }

         if (this.interactedWith && this.f_19797_ % Config.getInstance().trackVillagerPositionEveryNTicks == 0) {
            VillagerTrackerManager.update(this);
         }
      }
   }

   protected boolean findAndEquipToMain(Predicate<ItemStack> predicate) {
      int slot = InventoryUtils.getFirstSlotContainingItem(this.m_35311_(), predicate);
      if (slot > -1) {
         ItemStack replacement = this.m_35311_().m_8020_(slot).m_41620_(1);
         if (!replacement.m_41619_()) {
            this.m_21008_(this.getDominantHand(), replacement);
            return true;
         }
      }

      return false;
   }

   public void m_8119_() {
      super.m_8119_();
      int age = this.getTrackedValue(GROWTH_AMOUNT);
      if (age / 100 != this.prevGrowthAmount / 100) {
         this.prevGrowthAmount = age;
         this.m_6210_();
      }

      if (this.m_9236_().f_46443_) {
         if (this.relations.isProcreating()) {
            this.f_20885_ += 50.0F;
         }

         Mood mood = this.mcaBrain.getMood();
         if (mood.getParticle() != null && this.f_19797_ % mood.getParticleInterval() == 0 && this.m_9236_().f_46441_.m_188499_()) {
            this.m_35287_(mood.getParticle());
         }
      } else {
         float infection = this.getInfectionProgress();
         if (infection > 0.0F && this.f_19797_ % 20 == 0) {
            if (infection > 0.2F && this.m_9236_().f_46441_.m_188503_(25) == 0) {
               this.sendChatToAllAround("villager.sickness");
            }

            infection += 1.0F / Config.getInstance().infectionTime;
            this.setInfectionProgress(infection);
            if (infection > 1.0F) {
               this.m_21406_(EntityType.f_20530_, false);
               this.m_146870_();
            }
         }

         if (this.f_19797_ % 90 == 0 && this.mcaBrain.isPanicking()) {
            this.sendChatToAllAround("villager.scream");
         }

         if (this.f_19797_ % 60 == 0 && this.f_19796_.m_188503_(50) == 0 && this.traits.hasTrait(Traits.SIRBEN)) {
            this.sendChatToAllAround("sirben");
         }

         AttributeInstance instance = this.m_21204_().m_22146_(Attributes.f_22276_);
         if (instance != null) {
            int level = this.m_7141_().m_35576_() - 1;
            instance.m_22120_(this.EXTRA_HEALTH_EFFECT_ID);
            instance.m_22118_(
               new AttributeModifier(
                  this.EXTRA_HEALTH_EFFECT_ID, "level health boost", Config.getInstance().villagerHealthBonusPerLevel * level, Operation.ADDITION
               )
            );
         }

         if (this.f_19797_ % 12000 == 0) {
            int base = Math.round(this.mcaBrain.getMoodValue() / 12.0F);
            int value = this.f_19796_.m_188503_(7) - 3;
            this.mcaBrain.modifyMoodValue(value - base);
         }
      }
   }

   public void m_6210_() {
      AgeState current = this.getAgeState();
      AgeState next = current.getNext();
      if (next != current) {
         this.dimensions.interpolate(current, next, AgeState.getDelta(this.getTrackedValue(GROWTH_AMOUNT).intValue()));
      } else {
         this.dimensions.set(current);
      }

      boolean oldOnGround = this.m_20096_();
      super.m_6210_();
      this.m_6853_(oldOnGround);
   }

   public ItemStack m_5584_(Level world, ItemStack stack) {
      if (stack.m_41614_()) {
         this.m_5634_(stack.m_41720_().m_41473_().m_38744_());
      }

      return super.m_5584_(world, stack);
   }

   public void m_6083_() {
      super.m_6083_();
      Entity vehicle = this.m_20202_();
      if (vehicle instanceof PathfinderMob pathAwareEntity) {
         this.f_20883_ = pathAwareEntity.f_20883_;
      }

      if (vehicle instanceof Player player) {
         List<Entity> passengers = vehicle.m_20197_();
         float yaw = -player.f_20883_ * (float) (Math.PI / 180.0);
         boolean left = passengers.get(0) == this;
         boolean head = passengers.size() > 2 && passengers.get(2) == this;
         Vec3 offset = head ? new Vec3(0.0, 0.35F, 0.0) : new Vec3(left ? 0.4F : -0.4F, 0.05F, 0.0).m_82524_(yaw);
         if (this.m_183595_() && MCAClient.useGeneticsRenderer(vehicle.m_20148_())) {
            float height = CommonVillagerModel.getVillager(vehicle).getRawScaleFactor();
            offset = offset.m_82542_(1.0, height, 1.0);
            offset = offset.m_82520_(0.0, vehicle.m_6048_() * height - vehicle.m_6048_(), 0.0);
         }

         Vec3 pos = this.m_20182_();
         this.m_20343_(pos.m_7096_() + offset.m_7096_(), pos.m_7098_() + offset.m_7098_(), pos.m_7094_() + offset.m_7094_());
         if (vehicle.m_6144_()) {
            this.m_8127_();
         }
      }
   }

   public double m_6049_() {
      Entity vehicle = this.m_20202_();
      return vehicle instanceof Player ? -0.2 : -0.35;
   }

   public EntityDimensions m_6972_(Pose pose) {
      Entity vehicle = this.m_20202_();
      if (vehicle instanceof Player) {
         return f_20910_;
      }

      if (pose == Pose.SLEEPING) {
         return f_20910_;
      }

      float height = this.m_6134_() * 2.0F;
      float width = this.getHorizontalScaleFactor() * 0.6F;
      return EntityDimensions.m_20395_(width, height);
   }

   public void m_6667_(DamageSource cause) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         this.m_8061_(slot, ItemStack.f_41583_);
      }

      if (!this.m_9236_().f_46443_) {
         this.getResidency().getHomeVillage().flatMap(Village::getCivilRegistry).ifPresent(r -> r.addText(this.m_21231_().m_19293_()));
      }

      super.m_6667_(cause);
      if (!this.m_9236_().f_46443_) {
         InventoryUtils.dropAllItems(this, this.inventory);
         this.relations.onDeath(cause);
         Optional<Village> village = this.residency.getHomeVillage();
         if (village.isPresent()) {
            ServerLevel servRef = (ServerLevel)this.m_9236_();
            Map<UUID, Memories> memories = this.mcaBrain.getMemories();
            if (cause.m_7639_() != null) {
               servRef.m_6907_().forEach(player -> {
                  Rank relationToVillage = Tasks.getRank(village.get(), player);
                  ResourceLocation causeId = EntityType.m_20613_(cause.m_7639_().m_6095_());
                  CriterionMCA.FATE.trigger(player, causeId, relationToVillage);
               });
            }

            for (Entry<UUID, Memories> entry : memories.entrySet()) {
               village.get().pushHearts(entry.getKey(), entry.getValue().getHearts());
            }
         }

         this.residency.leaveHome();
         if (this.interactedWith) {
            VillagerTrackerManager.update(this);
         }
      }
   }

   public MoveControl m_21566_() {
      return this.isRidingHorse() ? this.f_21342_ : super.m_21566_();
   }

   public PathNavigation m_21573_() {
      return this.isRidingHorse() ? this.f_21344_ : super.m_21573_();
   }

   protected boolean isRidingHorse() {
      return this.m_20159_() && this.m_20202_() instanceof AbstractHorse;
   }

   public void m_6021_(double destX, double destY, double destZ) {
      if (this.m_20159_()) {
         Entity rootVehicle = this.m_20201_();
         if (rootVehicle instanceof Mob) {
            rootVehicle.m_6021_(destX, destY, destZ);
            return;
         }
      }

      super.m_6021_(destX, destY, destZ);
   }

   public SoundEvent m_5592_() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (SoundEvent)SoundsMCA.VILLAGER_MALE_SCREAM.get()
            : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_SCREAM.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.m_5592_() : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   public SoundEvent getSurprisedSound() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (SoundEvent)SoundsMCA.VILLAGER_MALE_SURPRISE.get()
            : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_SURPRISE.get();
      } else {
         return (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   @Nullable
   protected final SoundEvent m_7515_() {
      if (Config.getInstance().useMCAVoices) {
         if (this.getAgeState() == AgeState.BABY) {
            return (SoundEvent)SoundsMCA.VILLAGER_BABY_LAUGH.get();
         } else if (this.m_5803_()) {
            return this.getGenetics().getGender() == Gender.MALE
               ? (SoundEvent)SoundsMCA.VILLAGER_MALE_SNORE.get()
               : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_SNORE.get();
         } else if (this.getVillagerBrain().isPanicking()) {
            return this.m_5592_();
         } else if (this.isInfected() && this.f_19796_.m_188499_()) {
            return this.getGenetics().getGender() == Gender.MALE
               ? (SoundEvent)SoundsMCA.VILLAGER_MALE_COUGH.get()
               : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_COUGH.get();
         } else if (this.f_19796_.m_188499_() && this.getTraits().hasTrait(Traits.SIRBEN)) {
            return (SoundEvent)SoundsMCA.SIRBEN.get();
         } else {
            Mood mood = this.getVillagerBrain().getMood();
            if (mood.getSoundInterval() > 0 && this.f_19797_ % mood.getSoundInterval() == 0) {
               return this.getGenetics().getGender() == Gender.MALE ? mood.getSoundMale() : mood.getSoundFemale();
            } else {
               return (SoundEvent)SoundsMCA.SILENT.get();
            }
         }
      } else {
         return Config.getInstance().useVanillaVoices ? super.m_7515_() : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   protected final SoundEvent m_7975_(DamageSource cause) {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (SoundEvent)SoundsMCA.VILLAGER_MALE_HURT.get()
            : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_HURT.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.m_7975_(cause) : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   public final void playWelcomeSound() {
      if (Config.getInstance().useMCAVoices && !this.getVillagerBrain().isPanicking() && this.getAgeState() != AgeState.BABY) {
         this.m_5496_(
            this.getGenetics().getGender() == Gender.MALE ? (SoundEvent)SoundsMCA.VILLAGER_MALE_GREET.get() : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_GREET.get(),
            this.m_6121_(),
            this.m_6100_()
         );
      }
   }

   public final void playSurprisedSound() {
      if (Config.getInstance().useMCAVoices) {
         this.m_5496_(this.getSurprisedSound(), this.m_6121_(), this.m_6100_());
      }
   }

   public SoundEvent m_7596_() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE ? (SoundEvent)SoundsMCA.VILLAGER_MALE_YES.get() : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_YES.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.m_7596_() : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   public SoundEvent getNoSound() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE ? (SoundEvent)SoundsMCA.VILLAGER_MALE_NO.get() : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_NO.get();
      } else {
         return Config.getInstance().useVanillaVoices ? SoundEvents.f_12507_ : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   protected SoundEvent m_6068_(boolean sold) {
      if (Config.getInstance().useMCAVoices) {
         return sold ? this.m_7596_() : this.getNoSound();
      } else {
         return Config.getInstance().useVanillaVoices ? super.m_6068_(sold) : (SoundEvent)SoundsMCA.SILENT.get();
      }
   }

   public void m_35310_() {
      if (Config.getInstance().useMCAVoices) {
         this.m_5496_(
            this.getGenetics().getGender() == Gender.MALE
               ? (SoundEvent)SoundsMCA.VILLAGER_MALE_CELEBRATE.get()
               : (SoundEvent)SoundsMCA.VILLAGER_FEMALE_CELEBRATE.get(),
            this.m_6121_(),
            this.m_6100_()
         );
      } else if (Config.getInstance().useVanillaVoices) {
         super.m_35310_();
      } else {
         this.m_5496_((SoundEvent)SoundsMCA.SILENT.get(), this.m_6121_(), this.m_6100_());
      }
   }

   public float m_6100_() {
      float r = (this.f_19796_.m_188501_() - 0.5F) * 0.05F;
      float g = (this.genetics.getGene(Genetics.VOICE) - 0.5F) * 0.3F;
      float a = Mth.m_14179_(AgeState.getDelta(this.f_19797_), this.getAgeState().getPitch(), this.getAgeState().getNext().getPitch());
      return a + r + g;
   }

   public final Component m_5446_() {
      Component name = super.m_5446_();
      if (this.getVillagerBrain() != null) {
         MoveState state = this.getVillagerBrain().getMoveState();
         if (state != MoveState.MOVE) {
            name = name.m_6879_().m_130946_(" (").m_7220_(state.getName()).m_130946_(")");
         }

         Chore chore = this.getVillagerBrain().getCurrentJob();
         if (chore != Chore.NONE) {
            name = name.m_6879_().m_130946_(" (").m_7220_(chore.getName()).m_130946_(")");
         }
      }

      if (this.isInfected()) {
         return name.m_6879_().m_130940_(ChatFormatting.GREEN);
      } else {
         return (Component)(this.getProfession() == ProfessionsMCA.OUTLAW.get() ? name.m_6879_().m_130940_(ChatFormatting.RED) : name);
      }
   }

   @Nullable
   public final Component m_7770_() {
      String value = this.getTrackedValue(VILLAGER_NAME);
      return MCA.isBlankString(value) ? null : Component.m_237113_(value);
   }

   @Override
   public float getInfectionProgress() {
      return this.getTrackedValue(INFECTION_PROGRESS);
   }

   @Override
   public void setInfectionProgress(float progress) {
      this.setTrackedValue(INFECTION_PROGRESS, progress);
   }

   @Override
   public void playSpeechEffect() {
      if (this.isSpeechImpaired()) {
         this.m_5496_(SoundEvents.f_12598_, this.m_6121_(), this.m_6100_());
      }
   }

   public void m_35287_(ParticleOptions parameters) {
      super.m_35287_(parameters);
   }

   @Nullable
   public AbstractContainerMenu m_7208_(int i, Inventory playerInventory, Player playerEntity) {
      return ChestMenu.m_39237_(i, playerInventory, this.inventory);
   }

   @Override
   public VillagerDimensions getVillagerDimensions() {
      return this.dimensions;
   }

   @Override
   public boolean setAgeState(AgeState state) {
      if (VillagerLike.super.setAgeState(state)) {
         if (!this.m_9236_().f_46443_) {
            this.relations
               .getParents()
               .filter(ServerPlayer.class::isInstance)
               .map(ServerPlayer.class::cast)
               .forEach(e -> CriterionMCA.CHILD_AGE_STATE_CHANGE.trigger(e, state.name()));
            if (state == AgeState.ADULT) {
               this.relations
                  .getParents()
                  .filter(Player.class::isInstance)
                  .map(Player.class::cast)
                  .forEach(p -> this.sendEventMessage(Component.m_237110_("notify.child.grownup", new Object[]{this.m_7755_()}), p));
            }

            this.m_35483_((ServerLevel)this.m_9236_());
            this.randomizeClothes();
         }

         return true;
      } else {
         return false;
      }
   }

   public float m_6134_() {
      return Math.min(0.999F, this.getRawScaleFactor());
   }

   protected float m_6431_(Pose pose, EntityDimensions size) {
      return this.m_217003_(Pose.SLEEPING) ? super.m_6431_(pose, size) : this.m_6134_() * 1.75F;
   }

   public void m_7350_(EntityDataAccessor<?> par) {
      if (this.getTypeDataManager().isParam(AGE_STATE, par) || this.getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
         this.m_6210_();
      }

      if (this.getTypeDataManager().isParam(CUSTOM_SKIN, par)) {
         this.updateCustomSkin();
      }

      super.m_7350_(par);
   }

   public SimpleContainer m_35311_() {
      return this.inventory;
   }

   public void moveTowards(BlockPos pos, float speed, int closeEnoughDist) {
      this.f_20939_.m_21879_(MemoryModuleType.f_26370_, new WalkTarget(pos, speed, closeEnoughDist));
      this.lookAt(pos);
   }

   public void moveTowards(BlockPos pos, float speed) {
      this.moveTowards(pos, speed, 1);
   }

   public void moveTowards(BlockPos pos) {
      this.moveTowards(pos, 0.5F);
   }

   public void lookAt(BlockPos pos) {
      this.f_20939_.m_21879_(MemoryModuleType.f_26371_, new BlockPosTracker(pos));
   }

   public void m_7822_(byte id) {
      switch (id) {
         case 15:
            this.m_9236_()
               .m_6485_((ParticleOptions)ParticleTypesMCA.NEG_INTERACTION.get(), true, this.m_20185_(), this.m_20188_() + 0.5, this.m_20189_(), 0.0, 0.0, 0.0);
            break;
         case 16:
            this.m_9236_()
               .m_6485_((ParticleOptions)ParticleTypesMCA.POS_INTERACTION.get(), true, this.m_20185_(), this.m_20188_() + 0.5, this.m_20189_(), 0.0, 0.0, 0.0);
            break;
         case 17:
            this.m_35287_(ParticleTypes.f_123798_);
            break;
         default:
            super.m_7822_(id);
      }
   }

   public void onInvChange(Container inventoryFromListener) {
   }

   public void setInventory(UpdatableInventory inventory) {
      CompoundTag nbt = new CompoundTag();
      InventoryUtils.saveToNBT(inventory, nbt);
      InventoryUtils.readFromNBT(this.inventory, nbt);
   }

   @Nullable
   public <T extends Mob> T m_21406_(EntityType<T> type, boolean keepInventory) {
      this.residency.leaveHome();
      T mob;
      if (!this.m_213877_() && type == EntityType.f_20530_) {
         mob = (T)super.m_21406_(this.getGenetics().getGender().getZombieType(), keepInventory);
      } else {
         mob = (T)super.m_21406_(type, keepInventory);
      }

      if (mob instanceof VillagerLike<?> zombie) {
         zombie.copyVillagerAttributesFrom(this);
      }

      if (mob instanceof ZombieVillager zombie) {
         zombie.m_6518_((ServerLevel)this.m_9236_(), this.m_9236_().m_6436_(zombie.m_20183_()), MobSpawnType.CONVERSION, new ZombieGroupData(false, true), null);
         zombie.m_34375_(this.m_7141_());
         zombie.m_34391_((Tag)this.m_35517_().m_262795_(NbtOps.f_128958_));
         zombie.m_34411_(this.m_6616_().m_45388_());
         zombie.m_34373_(this.m_7809_());
         zombie.m_20084_(this.m_20148_());
         zombie.m_21530_();
         this.m_9236_().m_5898_(null, 1026, this.m_20183_(), 0);
      }

      if (mob instanceof ZombieVillagerEntityMCA zombie) {
         zombie.setInventory(this.inventory);
      }

      return mob;
   }

   public void m_7378_(CompoundTag nbt) {
      super.m_7378_(nbt);
      this.getTypeDataManager().load(this, nbt);
      this.relations.readFromNbt(nbt);
      this.longTermMemory.readFromNbt(nbt);
      this.playerModel = VillagerLike.PlayerModel.VALUES[nbt.m_128451_("playerModel")];
      this.updateSpeed();
      this.inventory.m_6211_();
      InventoryUtils.readFromNBT(this.inventory, nbt);
      if (nbt.m_128441_("DespawnDelay")) {
         this.despawnDelay = nbt.m_128451_("DespawnDelay");
      }

      if (nbt.m_128441_("InteractedWith")) {
         this.interactedWith = nbt.m_128471_("InteractedWith");
      }

      if (nbt.m_128441_("clothes")) {
         this.validateClothes();
      }

      if (this.getVillagerBrain().getPersonality() == Personality.UNASSIGNED) {
         this.getVillagerBrain().randomize();
      }
   }

   public final void m_7380_(CompoundTag nbt) {
      super.m_7380_(nbt);
      this.getTypeDataManager().save(this, nbt);
      this.relations.writeToNbt(nbt);
      this.longTermMemory.writeToNbt(nbt);
      nbt.m_128405_("DespawnDelay", this.despawnDelay);
      nbt.m_128379_("InteractedWith", this.interactedWith);
      InventoryUtils.saveToNBT(this.inventory, nbt);
      if (this.interactedWith) {
         VillagerTrackerManager.update(this);
      }
   }

   @Override
   public boolean isHostile() {
      return this.getProfession() == ProfessionsMCA.OUTLAW.get();
   }

   public boolean isFriend(EntityType<?> type) {
      return type == EntityType.f_20460_ || type == EntitiesMCA.FEMALE_VILLAGER.get() || type == EntitiesMCA.MALE_VILLAGER.get();
   }

   public boolean m_5886_(ProjectileWeaponItem weapon) {
      return true;
   }

   public void m_32336_(LivingEntity arg, float f) {
      InteractionHand lv = ProjectileUtil.m_37297_(arg, Items.f_42717_);
      ItemStack lv2 = arg.m_21120_(lv);
      if (arg.m_21055_(Items.f_42717_)) {
         CrossbowItem.m_40887_(arg.m_9236_(), arg, lv, lv2, f, 4.0F);
      }

      this.m_5847_();
   }

   public void m_6136_(boolean charging) {
   }

   public void m_5811_(LivingEntity target, ItemStack crossbow, Projectile projectile, float multiShotSpray) {
      this.m_32322_(this, target, projectile, multiShotSpray, 1.6F);
   }

   public void m_5847_() {
   }

   public void m_8038_(ServerLevel world, LightningBolt lightning) {
      this.getTraits().addTrait(Traits.ELECTRIFIED);
   }

   public void m_6504_(LivingEntity target, float pullProgress) {
      this.m_6710_(target);
      this.attackedEntity(target);
      if (this.m_21055_(Items.f_42717_)) {
         this.m_32336_(this, 1.75F);
      } else if (this.m_21055_(Items.f_42411_)) {
         ItemStack itemStack = this.m_6298_(this.m_21120_(ProjectileUtil.m_37297_(this, Items.f_42411_)));
         AbstractArrow persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
         double x = target.m_20185_() - this.m_20185_();
         double y = target.m_20227_(0.3333333333333333) - persistentProjectileEntity.m_20186_();
         double z = target.m_20189_() - this.m_20189_();
         double vel = Math.sqrt(x * x + z * z);
         persistentProjectileEntity.m_6686_(x, y + vel * 0.2F, z, 1.6F, 3.0F);
         this.m_5496_(SoundEvents.f_12382_, 1.0F, 1.0F / (this.m_217043_().m_188501_() * 0.4F + 0.8F));
         this.m_9236_().m_7967_(persistentProjectileEntity);
      }
   }

   protected AbstractArrow createArrowProjectile(ItemStack arrow, float damageModifier) {
      return ProjectileUtil.m_37300_(this, arrow, damageModifier);
   }

   public ItemStack m_6298_(ItemStack stack) {
      if (stack.m_41720_() instanceof ProjectileWeaponItem weapon) {
         Predicate<ItemStack> predicate = weapon.m_6442_();
         ItemStack itemStack = ProjectileWeaponItem.m_43010_(this, predicate);
         return itemStack.m_41619_() ? new ItemStack(Items.f_42412_) : itemStack;
      } else {
         return ItemStack.f_41583_;
      }
   }

   public static Builder createVillagerAttributes() {
      return Villager.m_35503_()
         .m_22268_(Attributes.f_22281_, 3.0)
         .m_22268_(Attributes.f_22282_, 1.0)
         .m_22268_(Attributes.f_22276_, Config.getInstance().villagerMaxHealth)
         .m_22268_(Attributes.f_22277_, Config.getInstance().getVillagerPathfindingDistance());
   }

   private void tickDespawnDelay() {
      if (this.despawnDelay > 0 && !this.m_35306_() && --this.despawnDelay == 0) {
         if (!this.getRelationships().getPartner().isPresent()
            && !this.getVillagerBrain()
               .getMemories()
               .values()
               .stream()
               .anyMatch(m -> this.f_19796_.m_188503_(Config.getInstance().marriageHeartsRequirement) < m.getHearts())) {
            this.m_146870_();
         } else {
            this.setProfession(VillagerProfession.f_35585_);
            this.setDespawnDelay(0);
         }
      }
   }

   public void setDespawnDelay(int despawnDelay) {
      this.despawnDelay = despawnDelay;
   }

   public int getDespawnDelay() {
      return this.despawnDelay;
   }

   public void makeMercenary() {
      this.setProfession((VillagerProfession)ProfessionsMCA.MERCENARY.get());
      this.inventory.m_19173_(new ItemStack(Items.f_42383_));
      this.inventory.m_19173_(new ItemStack(Items.f_42386_));
      this.inventory.m_19173_(new ItemStack(Items.f_42385_));
      this.inventory.m_19173_(new ItemStack(Items.f_42387_));
      this.inventory.m_19173_(new ItemStack(Items.f_42523_));
      this.inventory.m_19173_(new ItemStack(Items.f_42406_, 16));
   }

   public void customLevelUp() {
      this.m_34375_(this.m_7141_().m_35561_(this.m_7141_().m_35576_() + 1));
      this.m_7604_();
   }
}
