package fabric.net.mca.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.MCAClient;
import fabric.net.mca.ParticleTypesMCA;
import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.SoundsMCA;
import fabric.net.mca.TagsMCA;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.client.model.CommonVillagerModel;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.ai.BreedableRelationship;
import fabric.net.mca.entity.ai.Chore;
import fabric.net.mca.entity.ai.ConversationManager;
import fabric.net.mca.entity.ai.Genetics;
import fabric.net.mca.entity.ai.LongTermMemory;
import fabric.net.mca.entity.ai.Memories;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import fabric.net.mca.entity.ai.Mood;
import fabric.net.mca.entity.ai.MoveState;
import fabric.net.mca.entity.ai.Residency;
import fabric.net.mca.entity.ai.StatusEffectDangerSet;
import fabric.net.mca.entity.ai.Traits;
import fabric.net.mca.entity.ai.brain.VillagerBrain;
import fabric.net.mca.entity.ai.brain.VillagerTasksMCA;
import fabric.net.mca.entity.ai.pathfinder.VillagerNavigation;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.CompassionateEntity;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.entity.ai.relationship.Personality;
import fabric.net.mca.entity.ai.relationship.VillagerDimensions;
import fabric.net.mca.entity.interaction.VillagerCommandHandler;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.network.c2s.InteractionVillagerMessage;
import fabric.net.mca.resources.Names;
import fabric.net.mca.resources.Rank;
import fabric.net.mca.resources.Tasks;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.server.world.data.VillagerTrackerManager;
import fabric.net.mca.util.InventoryUtils;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CDataParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.class_124;
import net.minecraft.class_1263;
import net.minecraft.class_1266;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1277;
import net.minecraft.class_1282;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1314;
import net.minecraft.class_1315;
import net.minecraft.class_1322;
import net.minecraft.class_1324;
import net.minecraft.class_1335;
import net.minecraft.class_1355;
import net.minecraft.class_1405;
import net.minecraft.class_1408;
import net.minecraft.class_1439;
import net.minecraft.class_1496;
import net.minecraft.class_1538;
import net.minecraft.class_1641;
import net.minecraft.class_1642;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1665;
import net.minecraft.class_1675;
import net.minecraft.class_1676;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1811;
import net.minecraft.class_1890;
import net.minecraft.class_1914;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2631;
import net.minecraft.class_2940;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3468;
import net.minecraft.class_3532;
import net.minecraft.class_3730;
import net.minecraft.class_3745;
import net.minecraft.class_3850;
import net.minecraft.class_3852;
import net.minecraft.class_3854;
import net.minecraft.class_3908;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import net.minecraft.class_4095;
import net.minecraft.class_4099;
import net.minecraft.class_4140;
import net.minecraft.class_4142;
import net.minecraft.class_5134;
import net.minecraft.class_5425;
import net.minecraft.class_7923;
import net.minecraft.class_8103;
import net.minecraft.class_1322.class_1323;
import net.minecraft.class_1642.class_1644;
import net.minecraft.class_5132.class_5133;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VillagerEntityMCA extends class_1646 implements VillagerLike<VillagerEntityMCA>, class_3908, CompassionateEntity<BreedableRelationship>, class_3745 {
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

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(Class<E> type) {
      return VillagerLike.createTrackedData(type)
         .addAll(INFECTION_PROGRESS, GROWTH_AMOUNT)
         .add(Residency::createTrackedData)
         .add(BreedableRelationship::createTrackedData);
   }

   public VillagerEntityMCA(class_1299<VillagerEntityMCA> type, class_1937 w, Gender gender) {
      super(type, w);
      this.inventory.method_5489(this::onInvChange);
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

   public void method_19182() {
      super.method_19182();
      if (!this.method_37908().field_9236) {
         Optional<Village> village = this.residency.getHomeVillage();
         if (village.isPresent() && Config.getInstance().villagerRestockNotification) {
            village.get().broadCastMessage((class_3218)this.method_37908(), "events.restock", this.method_5477().getString());
         }
      }
   }

   protected void method_5693() {
      super.method_5693();
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
         class_2631.method_11335(this.gameProfile, profile -> this.gameProfile = profile);
      } else {
         this.gameProfile = null;
      }
   }

   @Override
   public CDataManager<VillagerEntityMCA> getTypeDataManager() {
      return DATA;
   }

   protected class_1408 method_5965(class_1937 world) {
      return new VillagerNavigation(this, world);
   }

   protected class_4095<?> method_18867(Dynamic<?> dynamic) {
      return VillagerTasksMCA.initializeTasks(this, VillagerTasksMCA.createProfile().method_28335(dynamic));
   }

   public void method_19179(class_3218 world) {
      class_4095<VillagerEntityMCA> brain = this.getMCABrain();
      brain.method_18900(world, this);
      this.field_18321 = brain.method_18911();
      VillagerTasksMCA.initializeTasks(this, this.getMCABrain());
   }

   public class_4095<VillagerEntityMCA> getMCABrain() {
      return this.field_18321;
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

   protected class_2561 method_23315() {
      return this.getProfessionText();
   }

   @Nullable
   public class_1315 method_5943(
      class_5425 world, class_1266 difficulty, class_3730 spawnReason, @Nullable class_1315 entityData, @Nullable class_2487 entityNbt
   ) {
      class_1315 data = super.method_5943(world, difficulty, spawnReason, entityData, entityNbt);
      this.initialize(spawnReason);
      this.setAgeState(AgeState.byCurrentAge(this.method_5618()));
      FamilyTreeNode entry = this.getRelationships().getFamilyEntry();
      if (!FamilyTreeNode.isValid(entry.father()) && !FamilyTreeNode.isValid(entry.mother())) {
         FamilyTree tree = FamilyTree.get(world.method_8410());
         FamilyTreeNode father = tree.getOrCreate(UUID.randomUUID(), Names.pickCitizenName(Gender.MALE), Gender.MALE);
         FamilyTreeNode mother = tree.getOrCreate(UUID.randomUUID(), Names.pickCitizenName(Gender.FEMALE), Gender.FEMALE);
         father.setDeceased(true);
         mother.setDeceased(true);
         entry.setFather(father);
         entry.setMother(mother);
      }

      return data;
   }

   public final class_3852 getProfession() {
      return this.method_7231().method_16924();
   }

   public final void setProfession(class_3852 profession) {
      this.method_7195(this.method_7231().method_16921(profession));
      this.method_19179((class_3218)this.method_37908());
   }

   @Override
   public class_2960 getProfessionId() {
      return class_7923.field_41195.method_10221(this.getProfession());
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
      return !ProfessionsMCA.canNotTrade.contains(this.getProfession()) || this.field_17721 != null && !this.field_17721.isEmpty();
   }

   public void method_7195(class_3850 data) {
      boolean hasChanged = !this.method_37908().field_9236 && this.getProfession() != data.method_16924() && data.method_16924() != ProfessionsMCA.OUTLAW.get();
      super.method_7195(data);
      if (hasChanged) {
         this.randomizeClothes();
         this.getRelationships().getFamilyEntry().setProfession(data.method_16924());
      }
   }

   public void method_7217(boolean isBaby) {
      this.method_5614(isBaby ? -AgeState.getMaxAge() : 0);
   }

   public void method_5665(@Nullable class_2561 name) {
      super.method_5665(name);
      if (name != null) {
         this.setName(name.getString());
      }
   }

   public void method_5614(int age) {
      super.method_5614(age);
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

   public boolean method_6121(class_1297 target) {
      this.attackedEntity(target);
      double baseDamage = this.method_26825(class_5134.field_23721);
      float damage = (float)(baseDamage * (this.getProfession() == ProfessionsMCA.GUARD.get() ? 3.0F : 1.0F));
      float knockback = (float)this.method_26825(class_5134.field_23722);
      if (this.getTraits().hasTrait(Traits.WEAK)) {
         damage *= 0.75F;
      }

      if (this.getTraits().hasTrait(Traits.TOUGH)) {
         damage *= 1.25F;
      }

      if (target instanceof class_1309 livingEntity) {
         damage += class_1890.method_8218(this.method_6047(), livingEntity.method_6046());
         knockback += class_1890.method_8205(this);
      }

      int i = class_1890.method_8199(this);
      if (i > 0) {
         target.method_5639(i * 4);
      }

      boolean damageDealt = target.method_5643(this.method_37908().method_48963().method_48812(this), damage);
      if (damageDealt) {
         if (knockback > 0.0F && target instanceof class_1309 livingEntity) {
            livingEntity.method_6005(
               knockback / 2.0F,
               class_3532.method_15374(this.method_36454() * (float) (Math.PI / 180.0)),
               -class_3532.method_15362(this.method_36454() * (float) (Math.PI / 180.0))
            );
            this.method_18799(this.method_18798().method_18805(0.6, 1.0, 0.6));
         }

         this.method_5723(this, target);
         this.method_6114(target);
      }

      return damageDealt;
   }

   private void attackedEntity(class_1297 target) {
      if (target instanceof class_1657 player) {
         this.pardonPlayers(player);
      }
   }

   private void pardonPlayers() {
      this.pardonPlayers(1);
   }

   public void pardonPlayers(int amount) {
      int bounty = this.getSmallBounty();
      if (bounty <= amount) {
         this.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get());
         this.method_18868().method_18875(class_4140.field_22355);
         this.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.HIT_BY_PLAYER.get());
      } else {
         this.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), bounty - amount);
      }
   }

   private void pardonPlayers(class_1657 attacker) {
      this.pardonPlayers();
      int bounty = this.getSmallBounty();
      if (bounty <= this.getMaxWarnings(attacker)) {
         this.method_18868().method_18875(class_4140.field_22355);
      }
   }

   public boolean canInteractWithItemStackInHand(class_1799 stack) {
      return stack.method_7909() != ItemsMCA.VILLAGER_EDITOR.get()
         && stack.method_7909() != ItemsMCA.NEEDLE_AND_THREAD.get()
         && stack.method_7909() != ItemsMCA.COMB.get()
         && stack.method_7909() != ItemsMCA.POTION_OF_FEMINITY.get()
         && stack.method_7909() != ItemsMCA.POTION_OF_MASCULINITY.get();
   }

   public final class_1269 method_5664(class_1657 player, class_243 pos, @NotNull class_1268 hand) {
      if (this.method_5854() != null && this.method_5854().equals(player)) {
         return class_1269.field_5811;
      }

      class_1799 stack = player.method_5998(hand);
      boolean isOnBlacklist = Config.getInstance()
         .villagerInteractionItemBlacklist
         .contains(class_7923.field_41178.method_10221(stack.method_7909()).toString());
      if (hand.equals(class_1268.field_5808)
         && !isOnBlacklist
         && !stack.method_31573(TagsMCA.Items.VILLAGER_EGGS)
         && this.canInteractWithItemStackInHand(stack)
         && !this.getVillagerBrain().isPanicking()) {
         this.getDialogueType(player);
         if (!player.method_5715()) {
            this.playWelcomeSound();
            this.interactedWith = true;
            return this.interactions.interactAt(player, pos, hand);
         }

         if (player instanceof class_3222 e) {
            NetworkHandler.sendToPlayer(new InteractionVillagerMessage("trade", this.field_6021), e);
         }
      }

      return super.method_5664(player, pos, hand);
   }

   public class_1269 method_5992(class_1657 player, class_1268 hand) {
      if (this.method_5854() != null && this.method_5854().equals(player)) {
         return class_1269.field_5811;
      }

      class_1799 stack = player.method_5998(hand);
      if (!stack.method_31573(TagsMCA.Items.VILLAGER_EGGS)
         && this.method_5805()
         && !this.method_18009()
         && !this.method_6113()
         && this.canInteractWithItemStackInHand(stack)
         && !this.getVillagerBrain().isPanicking()) {
         if (this.method_6109()) {
            this.copiedSayNo();
         } else {
            boolean hasOffers = this.hasTradeOffers();
            if (hand == class_1268.field_5808) {
               if (!hasOffers && !this.method_37908().field_9236) {
                  this.copiedSayNo();
               }

               player.method_7281(class_3468.field_15384);
            }

            if (hasOffers && !this.method_37908().field_9236) {
               this.copiedBeginTradeWith(player);
            }
         }

         return class_1269.method_29236(this.method_37908().field_9236);
      } else {
         return class_1269.field_5811;
      }
   }

   private void copiedSayNo() {
      this.method_20507(40);
      if (!this.method_37908().method_8608()) {
         this.method_5783(this.getNoSound(), this.method_6107(), this.method_6017());
      }
   }

   public boolean hasTradeOffers() {
      return !this.method_8264().isEmpty();
   }

   public void beginTradeWith(class_1657 customer) {
      this.copiedBeginTradeWith(customer);
   }

   private void copiedBeginTradeWith(class_1657 customer) {
      this.copiedPrepareOffersFor(customer);
      this.method_8259(customer);
      this.method_17449(customer, this.method_5476(), this.method_7231().method_16925());
   }

   private void copiedPrepareOffersFor(class_1657 player) {
      int reputation = this.method_20594(player);
      if (reputation != 0) {
         for (class_1914 tradeOffer : this.method_8264()) {
            tradeOffer.method_8245(-class_3532.method_15375(reputation * tradeOffer.method_19278()));
         }
      }

      if (player.method_6059(class_1294.field_18980)) {
         class_1293 statusEffect = player.method_6112(class_1294.field_18980);
         int amplifier = statusEffect.method_5578();

         for (class_1914 tradeOffer2 : this.method_8264()) {
            double d = 0.3 + 0.0625 * amplifier;
            int k = (int)Math.floor(d * tradeOffer2.method_8246().method_7947());
            tradeOffer2.method_8245(-Math.max(k, 1));
         }
      }
   }

   public VillagerEntityMCA createChild(class_3218 world, class_1296 partner) {
      VillagerEntityMCA child = partner instanceof VillagerEntityMCA partnerVillager
         ? this.relations.getPregnancy().createChild(Gender.getRandom(), partnerVillager)
         : this.relations.getPregnancy().createChild(Gender.getRandom());
      child.method_7195(child.method_7231().method_16922(this.getRandomType(partner)));
      child.method_5943(world, world.method_8404(child.method_24515()), class_3730.field_16466, null, null);
      return child;
   }

   private class_3854 getRandomType(class_1296 partner) {
      double d = this.field_5974.method_43058();
      if (d < 0.5) {
         return class_3854.method_16930(this.method_37908().method_23753(this.method_24515()));
      } else {
         return d < 0.75 ? this.method_7231().method_16919() : ((class_1646)partner).method_7231().method_16919();
      }
   }

   public final boolean method_5643(class_1282 source, float damageAmount) {
      if (this.method_5854() instanceof class_1657) {
         return super.method_5643(source, 0.0F);
      }

      if (!Config.getInstance().canHurtBabies && !source.method_48789(class_8103.field_43116) && this.getAgeState() == AgeState.BABY) {
         if (source.method_5529() instanceof class_1657 && this.requestCooldown()) {
            this.sendEventMessage(class_2561.method_43471("villager.baby_hit"));
         }

         return super.method_5643(source, 0.0F);
      } else {
         if (this.getProfession() == ProfessionsMCA.GUARD.get()) {
            damageAmount *= 0.5F;
         }

         if (this.getTraits().hasTrait(Traits.TOUGH)) {
            damageAmount *= 0.75F;
         }

         if (!this.method_37908().field_9236) {
            if (source.method_5529() instanceof class_1657 player) {
               if (this.method_37908().method_8510() - this.lastHit > 40L) {
                  this.lastHit = this.method_37908().method_8510();
                  if ((!this.isGuard() || this.getSmallBounty() == 0) && this.requestCooldown()) {
                     if (this.method_6032() < this.method_6063() / 2.0F) {
                        this.sendChatMessage(player, "villager.badly_hurt");
                     } else {
                        this.sendChatMessage(player, "villager.hurt");
                     }
                  }
               }

               int trustIssues = (int)((1.0 - this.method_6032() / this.method_6063() * 0.75) * (3.0 + 2.0 * damageAmount));
               this.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-trustIssues);
            }

            if (source.method_5526() instanceof class_1642
               && this.getProfession() != ProfessionsMCA.GUARD.get()
               && Config.getInstance().enableInfection
               && this.field_5974.method_43057() < Config.getInstance().zombieBiteInfectionChance
               && this.field_5974.method_43057() > (this.method_7231().method_16925() - 1) * Config.getInstance().infectionChanceDecreasePerLevel
               && (this.getResidency().getHomeVillage().filter(v -> v.hasBuilding("infirmary")).isEmpty() || this.field_5974.method_43056())) {
               this.setInfected(true);
               this.sendChatToAllAround("villager.bitten");
               MCA.LOGGER.info("{} has been infected", this.method_5477());
            }
         }

         class_1297 attacker = source != null ? source.method_5529() : null;
         if (attacker instanceof class_1309 livingEntity && !this.isHostile() && !this.isFriend(attacker.method_5864())) {
            this.method_18868().method_18879((class_4140)MemoryModuleTypeMCA.HIT_BY_PLAYER.get(), Optional.of(livingEntity));
            this.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), this.getSmallBounty() + 1);
            class_243 pos = this.method_19538();
            this.method_37908().method_18467(VillagerEntityMCA.class, new class_238(pos, pos).method_1014(32.0)).forEach(v -> {
               if (this.method_5858(v) <= (v.method_5968() == null ? 1024 : 64)) {
                  if (attacker instanceof class_1657 player) {
                     int bounty = v.getSmallBounty();
                     if (v.isGuard()) {
                        int maxWarning = v.getMaxWarnings(player);
                        if (bounty > maxWarning) {
                           v.method_18868().method_18878(class_4140.field_22355, livingEntity);
                        } else if (bounty == 0 || bounty == maxWarning) {
                           v.sendChatMessage(player, "villager.warning");
                        }

                        v.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get(), bounty + 1);
                     }
                  } else if (v.isGuard()) {
                     v.method_18868().method_18878(class_4140.field_22355, livingEntity);
                  }
               }
            });
         }

         if (attacker instanceof class_1439 golem) {
            golem.method_29922();

            try {
               Field targetSelector = class_1308.class.getDeclaredField("targetSelector");
               ((class_1355)targetSelector.get(attacker)).method_19048().forEach(g -> {
                  if (g.method_19058() instanceof class_1405) {
                     g.method_19058().method_6270();
                  }
               });
            } catch (NoSuchFieldException | IllegalAccessException var6) {
            }

            damageAmount *= 0.0F;
         }

         return super.method_5643(source, damageAmount);
      }
   }

   private boolean requestCooldown() {
      if (this.method_37908().method_8510() - this.lastCooldown > 100L) {
         this.lastCooldown = this.method_37908().method_8510();
         return true;
      } else {
         return false;
      }
   }

   public boolean isGuard() {
      return this.getProfession() == ProfessionsMCA.GUARD.get() || this.getProfession() == ProfessionsMCA.ARCHER.get();
   }

   public int getSmallBounty() {
      return Objects.requireNonNull(this.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get())).orElse(0);
   }

   public boolean isHitBy(class_3222 player) {
      return Objects.requireNonNull(this.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.HIT_BY_PLAYER.get())).filter(v -> v == player).isPresent();
   }

   private int getMaxWarnings(class_1657 attacker) {
      return this.getVillagerBrain().getMemoriesForPlayer(attacker).getHearts() / Math.max(1, Config.getInstance().heartsForPardonHit);
   }

   public void method_6007() {
      this.method_6119();
      super.method_6007();
      this.burned--;
      if (this.method_5809()) {
         this.burned = Config.getInstance().burnedClothingTickLength;
      }

      if (this.burned > 0) {
         this.spawnBurntParticles();
      }

      if (!this.method_37908().field_9236) {
         if (this.field_6012 % 200 == 0 && this.method_6032() < this.method_6063() && !this.method_18868().method_46873(class_4140.field_22355).isPresent()) {
            class_1799 food = this.method_6047();
            if (food.method_19267()) {
               this.method_18866(this.method_37908(), food);
            } else if (!this.findAndEquipToMain(
               i -> i.method_19267()
                  && i.method_7909().method_19264().method_19230() > 0
                  && i.method_7909()
                     .method_19264()
                     .method_19235()
                     .stream()
                     .noneMatch(e -> StatusEffectDangerSet.isDanger.contains(((class_1293)e.getFirst()).method_5579()))
            )) {
               this.method_6025(1.0F);
            }
         }

         this.tickDespawnDelay();
         this.residency.tick();
         this.relations.tick(this.field_6012);
         this.inventory.update(this);
         if (this.field_6012 % Config.getInstance().pardonPlayerTicks == 0) {
            this.pardonPlayers();
         }

         this.mcaBrain.think();
         if (this.field_6012 % Config.getInstance().giftDesaturationReset == 0) {
            this.getRelationships().getGiftSaturation().pop();
         }

         if (this.interactedWith && this.field_6012 % Config.getInstance().trackVillagerPositionEveryNTicks == 0) {
            VillagerTrackerManager.update(this);
         }
      }
   }

   protected boolean findAndEquipToMain(Predicate<class_1799> predicate) {
      int slot = InventoryUtils.getFirstSlotContainingItem(this.method_35199(), predicate);
      if (slot > -1) {
         class_1799 replacement = this.method_35199().method_5438(slot).method_7971(1);
         if (!replacement.method_7960()) {
            this.method_6122(this.getDominantHand(), replacement);
            return true;
         }
      }

      return false;
   }

   public void method_5773() {
      super.method_5773();
      int age = this.getTrackedValue(GROWTH_AMOUNT);
      if (age / 100 != this.prevGrowthAmount / 100) {
         this.prevGrowthAmount = age;
         this.method_18382();
      }

      if (this.method_37908().field_9236) {
         if (this.relations.isProcreating()) {
            this.field_6241 += 50.0F;
         }

         Mood mood = this.mcaBrain.getMood();
         if (mood.getParticle() != null && this.field_6012 % mood.getParticleInterval() == 0 && this.method_37908().field_9229.method_43056()) {
            this.method_18007(mood.getParticle());
         }
      } else {
         float infection = this.getInfectionProgress();
         if (infection > 0.0F && this.field_6012 % 20 == 0) {
            if (infection > 0.2F && this.method_37908().field_9229.method_43048(25) == 0) {
               this.sendChatToAllAround("villager.sickness");
            }

            infection += 1.0F / Config.getInstance().infectionTime;
            this.setInfectionProgress(infection);
            if (infection > 1.0F) {
               this.method_29243(class_1299.field_6054, false);
               this.method_31472();
            }
         }

         if (this.field_6012 % 90 == 0 && this.mcaBrain.isPanicking()) {
            this.sendChatToAllAround("villager.scream");
         }

         if (this.field_6012 % 60 == 0 && this.field_5974.method_43048(50) == 0 && this.traits.hasTrait(Traits.SIRBEN)) {
            this.sendChatToAllAround("sirben");
         }

         class_1324 instance = this.method_6127().method_26842(class_5134.field_23716);
         if (instance != null) {
            int level = this.method_7231().method_16925() - 1;
            instance.method_6200(this.EXTRA_HEALTH_EFFECT_ID);
            instance.method_26835(
               new class_1322(
                  this.EXTRA_HEALTH_EFFECT_ID, "level health boost", Config.getInstance().villagerHealthBonusPerLevel * level, class_1323.field_6328
               )
            );
         }

         if (this.field_6012 % 12000 == 0) {
            int base = Math.round(this.mcaBrain.getMoodValue() / 12.0F);
            int value = this.field_5974.method_43048(7) - 3;
            this.mcaBrain.modifyMoodValue(value - base);
         }
      }
   }

   public void method_18382() {
      AgeState current = this.getAgeState();
      AgeState next = current.getNext();
      if (next != current) {
         this.dimensions.interpolate(current, next, AgeState.getDelta(this.getTrackedValue(GROWTH_AMOUNT).intValue()));
      } else {
         this.dimensions.set(current);
      }

      boolean oldOnGround = this.method_24828();
      super.method_18382();
      this.method_24830(oldOnGround);
   }

   public class_1799 method_18866(class_1937 world, class_1799 stack) {
      if (stack.method_19267()) {
         this.method_6025(stack.method_7909().method_19264().method_19230());
      }

      return super.method_18866(world, stack);
   }

   public void method_5842() {
      super.method_5842();
      class_1297 vehicle = this.method_5854();
      if (vehicle instanceof class_1314 pathAwareEntity) {
         this.field_6283 = pathAwareEntity.field_6283;
      }

      if (vehicle instanceof class_1657 player) {
         List<class_1297> passengers = vehicle.method_5685();
         float yaw = -player.field_6283 * (float) (Math.PI / 180.0);
         boolean left = passengers.get(0) == this;
         boolean head = passengers.size() > 2 && passengers.get(2) == this;
         class_243 offset = head ? new class_243(0.0, 0.35F, 0.0) : new class_243(left ? 0.4F : -0.4F, 0.05F, 0.0).method_1024(yaw);
         if (this.method_38069() && MCAClient.useGeneticsRenderer(vehicle.method_5667())) {
            float height = CommonVillagerModel.getVillager(vehicle).getRawScaleFactor();
            offset = offset.method_18805(1.0, height, 1.0);
            offset = offset.method_1031(0.0, vehicle.method_5621() * height - vehicle.method_5621(), 0.0);
         }

         class_243 pos = this.method_19538();
         this.method_23327(pos.method_10216() + offset.method_10216(), pos.method_10214() + offset.method_10214(), pos.method_10215() + offset.method_10215());
         if (vehicle.method_5715()) {
            this.method_5848();
         }
      }
   }

   public double method_5678() {
      class_1297 vehicle = this.method_5854();
      return vehicle instanceof class_1657 ? -0.2 : -0.35;
   }

   public class_4048 method_18377(class_4050 pose) {
      class_1297 vehicle = this.method_5854();
      if (vehicle instanceof class_1657) {
         return field_18072;
      }

      if (pose == class_4050.field_18078) {
         return field_18072;
      }

      float height = this.method_17825() * 2.0F;
      float width = this.getHorizontalScaleFactor() * 0.6F;
      return class_4048.method_18384(width, height);
   }

   public void method_6078(class_1282 cause) {
      for (class_1304 slot : class_1304.values()) {
         this.method_5673(slot, class_1799.field_8037);
      }

      if (!this.method_37908().field_9236) {
         this.getResidency().getHomeVillage().flatMap(Village::getCivilRegistry).ifPresent(r -> r.addText(this.method_6066().method_5548()));
      }

      super.method_6078(cause);
      if (!this.method_37908().field_9236) {
         InventoryUtils.dropAllItems(this, this.inventory);
         this.relations.onDeath(cause);
         Optional<Village> village = this.residency.getHomeVillage();
         if (village.isPresent()) {
            class_3218 servRef = (class_3218)this.method_37908();
            Map<UUID, Memories> memories = this.mcaBrain.getMemories();
            if (cause.method_5529() != null) {
               servRef.method_18456().forEach(player -> {
                  Rank relationToVillage = Tasks.getRank(village.get(), player);
                  class_2960 causeId = class_1299.method_5890(cause.method_5529().method_5864());
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

   public class_1335 method_5962() {
      return this.isRidingHorse() ? this.field_6207 : super.method_5962();
   }

   public class_1408 method_5942() {
      return this.isRidingHorse() ? this.field_6189 : super.method_5942();
   }

   protected boolean isRidingHorse() {
      return this.method_5765() && this.method_5854() instanceof class_1496;
   }

   public void method_5859(double destX, double destY, double destZ) {
      if (this.method_5765()) {
         class_1297 rootVehicle = this.method_5668();
         if (rootVehicle instanceof class_1308) {
            rootVehicle.method_5859(destX, destY, destZ);
            return;
         }
      }

      super.method_5859(destX, destY, destZ);
   }

   public class_3414 method_6002() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (class_3414)SoundsMCA.VILLAGER_MALE_SCREAM.get()
            : (class_3414)SoundsMCA.VILLAGER_FEMALE_SCREAM.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.method_6002() : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   public class_3414 getSurprisedSound() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (class_3414)SoundsMCA.VILLAGER_MALE_SURPRISE.get()
            : (class_3414)SoundsMCA.VILLAGER_FEMALE_SURPRISE.get();
      } else {
         return (class_3414)SoundsMCA.SILENT.get();
      }
   }

   @Nullable
   protected final class_3414 method_5994() {
      if (Config.getInstance().useMCAVoices) {
         if (this.getAgeState() == AgeState.BABY) {
            return (class_3414)SoundsMCA.VILLAGER_BABY_LAUGH.get();
         } else if (this.method_6113()) {
            return this.getGenetics().getGender() == Gender.MALE
               ? (class_3414)SoundsMCA.VILLAGER_MALE_SNORE.get()
               : (class_3414)SoundsMCA.VILLAGER_FEMALE_SNORE.get();
         } else if (this.getVillagerBrain().isPanicking()) {
            return this.method_6002();
         } else if (this.isInfected() && this.field_5974.method_43056()) {
            return this.getGenetics().getGender() == Gender.MALE
               ? (class_3414)SoundsMCA.VILLAGER_MALE_COUGH.get()
               : (class_3414)SoundsMCA.VILLAGER_FEMALE_COUGH.get();
         } else if (this.field_5974.method_43056() && this.getTraits().hasTrait(Traits.SIRBEN)) {
            return (class_3414)SoundsMCA.SIRBEN.get();
         } else {
            Mood mood = this.getVillagerBrain().getMood();
            if (mood.getSoundInterval() > 0 && this.field_6012 % mood.getSoundInterval() == 0) {
               return this.getGenetics().getGender() == Gender.MALE ? mood.getSoundMale() : mood.getSoundFemale();
            } else {
               return (class_3414)SoundsMCA.SILENT.get();
            }
         }
      } else {
         return Config.getInstance().useVanillaVoices ? super.method_5994() : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   protected final class_3414 method_6011(class_1282 cause) {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE
            ? (class_3414)SoundsMCA.VILLAGER_MALE_HURT.get()
            : (class_3414)SoundsMCA.VILLAGER_FEMALE_HURT.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.method_6011(cause) : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   public final void playWelcomeSound() {
      if (Config.getInstance().useMCAVoices && !this.getVillagerBrain().isPanicking() && this.getAgeState() != AgeState.BABY) {
         this.method_5783(
            this.getGenetics().getGender() == Gender.MALE ? (class_3414)SoundsMCA.VILLAGER_MALE_GREET.get() : (class_3414)SoundsMCA.VILLAGER_FEMALE_GREET.get(),
            this.method_6107(),
            this.method_6017()
         );
      }
   }

   public final void playSurprisedSound() {
      if (Config.getInstance().useMCAVoices) {
         this.method_5783(this.getSurprisedSound(), this.method_6107(), this.method_6017());
      }
   }

   public class_3414 method_18010() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE ? (class_3414)SoundsMCA.VILLAGER_MALE_YES.get() : (class_3414)SoundsMCA.VILLAGER_FEMALE_YES.get();
      } else {
         return Config.getInstance().useVanillaVoices ? super.method_18010() : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   public class_3414 getNoSound() {
      if (Config.getInstance().useMCAVoices) {
         return this.getGenetics().getGender() == Gender.MALE ? (class_3414)SoundsMCA.VILLAGER_MALE_NO.get() : (class_3414)SoundsMCA.VILLAGER_FEMALE_NO.get();
      } else {
         return Config.getInstance().useVanillaVoices ? class_3417.field_15008 : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   protected class_3414 method_18012(boolean sold) {
      if (Config.getInstance().useMCAVoices) {
         return sold ? this.method_18010() : this.getNoSound();
      } else {
         return Config.getInstance().useVanillaVoices ? super.method_18012(sold) : (class_3414)SoundsMCA.SILENT.get();
      }
   }

   public void method_20010() {
      if (Config.getInstance().useMCAVoices) {
         this.method_5783(
            this.getGenetics().getGender() == Gender.MALE
               ? (class_3414)SoundsMCA.VILLAGER_MALE_CELEBRATE.get()
               : (class_3414)SoundsMCA.VILLAGER_FEMALE_CELEBRATE.get(),
            this.method_6107(),
            this.method_6017()
         );
      } else if (Config.getInstance().useVanillaVoices) {
         super.method_20010();
      } else {
         this.method_5783((class_3414)SoundsMCA.SILENT.get(), this.method_6107(), this.method_6017());
      }
   }

   public float method_6017() {
      float r = (this.field_5974.method_43057() - 0.5F) * 0.05F;
      float g = (this.genetics.getGene(Genetics.VOICE) - 0.5F) * 0.3F;
      float a = class_3532.method_16439(AgeState.getDelta(this.field_6012), this.getAgeState().getPitch(), this.getAgeState().getNext().getPitch());
      return a + r + g;
   }

   public final class_2561 method_5476() {
      class_2561 name = super.method_5476();
      if (this.getVillagerBrain() != null) {
         MoveState state = this.getVillagerBrain().getMoveState();
         if (state != MoveState.MOVE) {
            name = name.method_27662().method_27693(" (").method_10852(state.getName()).method_27693(")");
         }

         Chore chore = this.getVillagerBrain().getCurrentJob();
         if (chore != Chore.NONE) {
            name = name.method_27662().method_27693(" (").method_10852(chore.getName()).method_27693(")");
         }
      }

      if (this.isInfected()) {
         return name.method_27662().method_27692(class_124.field_1060);
      } else {
         return (class_2561)(this.getProfession() == ProfessionsMCA.OUTLAW.get() ? name.method_27662().method_27692(class_124.field_1061) : name);
      }
   }

   @Nullable
   public final class_2561 method_5797() {
      String value = this.getTrackedValue(VILLAGER_NAME);
      return MCA.isBlankString(value) ? null : class_2561.method_43470(value);
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
         this.method_5783(class_3417.field_15174, this.method_6107(), this.method_6017());
      }
   }

   public void method_18007(class_2394 parameters) {
      super.method_18007(parameters);
   }

   @Nullable
   public class_1703 createMenu(int i, class_1661 playerInventory, class_1657 playerEntity) {
      return class_1707.method_19245(i, playerInventory, this.inventory);
   }

   @Override
   public VillagerDimensions getVillagerDimensions() {
      return this.dimensions;
   }

   @Override
   public boolean setAgeState(AgeState state) {
      if (VillagerLike.super.setAgeState(state)) {
         if (!this.method_37908().field_9236) {
            this.relations
               .getParents()
               .filter(class_3222.class::isInstance)
               .map(class_3222.class::cast)
               .forEach(e -> CriterionMCA.CHILD_AGE_STATE_CHANGE.trigger(e, state.name()));
            if (state == AgeState.ADULT) {
               this.relations
                  .getParents()
                  .filter(class_1657.class::isInstance)
                  .map(class_1657.class::cast)
                  .forEach(p -> this.sendEventMessage(class_2561.method_43469("notify.child.grownup", new Object[]{this.method_5477()}), p));
            }

            this.method_19179((class_3218)this.method_37908());
            this.randomizeClothes();
         }

         return true;
      } else {
         return false;
      }
   }

   public float method_17825() {
      return Math.min(0.999F, this.getRawScaleFactor());
   }

   protected float method_18394(class_4050 pose, class_4048 size) {
      return this.method_41328(class_4050.field_18078) ? super.method_18394(pose, size) : this.method_17825() * 1.75F;
   }

   public void method_5674(class_2940<?> par) {
      if (this.getTypeDataManager().isParam(AGE_STATE, par) || this.getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
         this.method_18382();
      }

      if (this.getTypeDataManager().isParam(CUSTOM_SKIN, par)) {
         this.updateCustomSkin();
      }

      super.method_5674(par);
   }

   public class_1277 method_35199() {
      return this.inventory;
   }

   public void moveTowards(class_2338 pos, float speed, int closeEnoughDist) {
      this.field_18321.method_18878(class_4140.field_18445, new class_4142(pos, speed, closeEnoughDist));
      this.lookAt(pos);
   }

   public void moveTowards(class_2338 pos, float speed) {
      this.moveTowards(pos, speed, 1);
   }

   public void moveTowards(class_2338 pos) {
      this.moveTowards(pos, 0.5F);
   }

   public void lookAt(class_2338 pos) {
      this.field_18321.method_18878(class_4140.field_18446, new class_4099(pos));
   }

   public void method_5711(byte id) {
      switch (id) {
         case 15:
            this.method_37908()
               .method_17452(
                  (class_2394)ParticleTypesMCA.NEG_INTERACTION.get(), true, this.method_23317(), this.method_23320() + 0.5, this.method_23321(), 0.0, 0.0, 0.0
               );
            break;
         case 16:
            this.method_37908()
               .method_17452(
                  (class_2394)ParticleTypesMCA.POS_INTERACTION.get(), true, this.method_23317(), this.method_23320() + 0.5, this.method_23321(), 0.0, 0.0, 0.0
               );
            break;
         case 17:
            this.method_18007(class_2398.field_11209);
            break;
         default:
            super.method_5711(id);
      }
   }

   public void onInvChange(class_1263 inventoryFromListener) {
   }

   public void setInventory(UpdatableInventory inventory) {
      class_2487 nbt = new class_2487();
      InventoryUtils.saveToNBT(inventory, nbt);
      InventoryUtils.readFromNBT(this.inventory, nbt);
   }

   @Nullable
   public <T extends class_1308> T method_29243(class_1299<T> type, boolean keepInventory) {
      this.residency.leaveHome();
      T mob;
      if (!this.method_31481() && type == class_1299.field_6054) {
         mob = (T)super.method_29243(this.getGenetics().getGender().getZombieType(), keepInventory);
      } else {
         mob = (T)super.method_29243(type, keepInventory);
      }

      if (mob instanceof VillagerLike<?> zombie) {
         zombie.copyVillagerAttributesFrom(this);
      }

      if (mob instanceof class_1641 zombie) {
         zombie.method_5943(
            (class_3218)this.method_37908(), this.method_37908().method_8404(zombie.method_24515()), class_3730.field_16468, new class_1644(false, true), null
         );
         zombie.method_7195(this.method_7231());
         zombie.method_21649((class_2520)this.method_21651().method_19067(class_2509.field_11560));
         zombie.method_16916(this.method_8264().method_8268());
         zombie.method_19622(this.method_19269());
         zombie.method_5826(this.method_5667());
         zombie.method_5971();
         this.method_37908().method_8444(null, 1026, this.method_24515(), 0);
      }

      if (mob instanceof ZombieVillagerEntityMCA zombie) {
         zombie.setInventory(this.inventory);
      }

      return mob;
   }

   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      this.getTypeDataManager().load(this, nbt);
      this.relations.readFromNbt(nbt);
      this.longTermMemory.readFromNbt(nbt);
      this.playerModel = VillagerLike.PlayerModel.VALUES[nbt.method_10550("playerModel")];
      this.updateSpeed();
      this.inventory.method_5448();
      InventoryUtils.readFromNBT(this.inventory, nbt);
      if (nbt.method_10545("DespawnDelay")) {
         this.despawnDelay = nbt.method_10550("DespawnDelay");
      }

      if (nbt.method_10545("InteractedWith")) {
         this.interactedWith = nbt.method_10577("InteractedWith");
      }

      if (nbt.method_10545("clothes")) {
         this.validateClothes();
      }

      if (this.getVillagerBrain().getPersonality() == Personality.UNASSIGNED) {
         this.getVillagerBrain().randomize();
      }
   }

   public final void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      this.getTypeDataManager().save(this, nbt);
      this.relations.writeToNbt(nbt);
      this.longTermMemory.writeToNbt(nbt);
      nbt.method_10569("DespawnDelay", this.despawnDelay);
      nbt.method_10556("InteractedWith", this.interactedWith);
      InventoryUtils.saveToNBT(this.inventory, nbt);
      if (this.interactedWith) {
         VillagerTrackerManager.update(this);
      }
   }

   @Override
   public boolean isHostile() {
      return this.getProfession() == ProfessionsMCA.OUTLAW.get();
   }

   public boolean isFriend(class_1299<?> type) {
      return type == class_1299.field_6147 || type == EntitiesMCA.FEMALE_VILLAGER.get() || type == EntitiesMCA.MALE_VILLAGER.get();
   }

   public boolean method_25938(class_1811 weapon) {
      return true;
   }

   public void method_24654(class_1309 arg, float f) {
      class_1268 lv = class_1675.method_18812(arg, class_1802.field_8399);
      class_1799 lv2 = arg.method_5998(lv);
      if (arg.method_24518(class_1802.field_8399)) {
         class_1764.method_7777(arg.method_37908(), arg, lv, lv2, f, 4.0F);
      }

      this.method_24651();
   }

   public void method_7110(boolean charging) {
   }

   public void method_18811(class_1309 target, class_1799 crossbow, class_1676 projectile, float multiShotSpray) {
      this.method_24652(this, target, projectile, multiShotSpray, 1.6F);
   }

   public void method_24651() {
   }

   public void method_5800(class_3218 world, class_1538 lightning) {
      this.getTraits().addTrait(Traits.ELECTRIFIED);
   }

   public void method_7105(class_1309 target, float pullProgress) {
      this.method_5980(target);
      this.attackedEntity(target);
      if (this.method_24518(class_1802.field_8399)) {
         this.method_24654(this, 1.75F);
      } else if (this.method_24518(class_1802.field_8102)) {
         class_1799 itemStack = this.method_18808(this.method_5998(class_1675.method_18812(this, class_1802.field_8102)));
         class_1665 persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
         double x = target.method_23317() - this.method_23317();
         double y = target.method_23323(0.3333333333333333) - persistentProjectileEntity.method_23318();
         double z = target.method_23321() - this.method_23321();
         double vel = Math.sqrt(x * x + z * z);
         persistentProjectileEntity.method_7485(x, y + vel * 0.2F, z, 1.6F, 3.0F);
         this.method_5783(class_3417.field_14633, 1.0F, 1.0F / (this.method_6051().method_43057() * 0.4F + 0.8F));
         this.method_37908().method_8649(persistentProjectileEntity);
      }
   }

   protected class_1665 createArrowProjectile(class_1799 arrow, float damageModifier) {
      return class_1675.method_18813(this, arrow, damageModifier);
   }

   public class_1799 method_18808(class_1799 stack) {
      if (stack.method_7909() instanceof class_1811 weapon) {
         Predicate<class_1799> predicate = weapon.method_20310();
         class_1799 itemStack = class_1811.method_18815(this, predicate);
         return itemStack.method_7960() ? new class_1799(class_1802.field_8107) : itemStack;
      } else {
         return class_1799.field_8037;
      }
   }

   public static class_5133 createVillagerAttributes() {
      return class_1646.method_26955()
         .method_26868(class_5134.field_23721, 3.0)
         .method_26868(class_5134.field_23722, 1.0)
         .method_26868(class_5134.field_23716, Config.getInstance().villagerMaxHealth)
         .method_26868(class_5134.field_23717, Config.getInstance().getVillagerPathfindingDistance());
   }

   private void tickDespawnDelay() {
      if (this.despawnDelay > 0 && !this.method_18009() && --this.despawnDelay == 0) {
         if (!this.getRelationships().getPartner().isPresent()
            && !this.getVillagerBrain()
               .getMemories()
               .values()
               .stream()
               .anyMatch(m -> this.field_5974.method_43048(Config.getInstance().marriageHeartsRequirement) < m.getHearts())) {
            this.method_31472();
         } else {
            this.setProfession(class_3852.field_17051);
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
      this.setProfession((class_3852)ProfessionsMCA.MERCENARY.get());
      this.inventory.method_5491(new class_1799(class_1802.field_8371));
      this.inventory.method_5491(new class_1799(class_1802.field_8475));
      this.inventory.method_5491(new class_1799(class_1802.field_8403));
      this.inventory.method_5491(new class_1799(class_1802.field_8609));
      this.inventory.method_5491(new class_1799(class_1802.field_8378));
      this.inventory.method_5491(new class_1799(class_1802.field_8229, 16));
   }

   public void customLevelUp() {
      this.method_7195(this.method_7231().method_16920(this.method_7231().method_16925() + 1));
      this.method_7237();
   }
}
