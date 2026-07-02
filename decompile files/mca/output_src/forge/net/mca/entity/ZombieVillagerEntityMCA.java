package forge.net.mca.entity;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.TagsMCA;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Relationship;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.interaction.ZombieCommandHandler;
import forge.net.mca.util.InventoryUtils;
import forge.net.mca.util.network.datasync.CDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieVillagerEntityMCA
   extends ZombieVillager
   implements VillagerLike<ZombieVillagerEntityMCA>,
   CompassionateEntity<Relationship<ZombieVillagerEntityMCA>> {
   private static final CDataManager<ZombieVillagerEntityMCA> DATA = VillagerEntityMCA.createTrackedData(ZombieVillagerEntityMCA.class).build();
   private final VillagerBrain<ZombieVillagerEntityMCA> mcaBrain = new VillagerBrain(this);
   private final Genetics genetics = new Genetics(this);
   private final Traits traits = new Traits(this);
   private final Relationship<ZombieVillagerEntityMCA> relations = new Relationship(this);
   private final ZombieCommandHandler interactions = new ZombieCommandHandler(this);
   private final UpdatableInventory inventory = new UpdatableInventory(27);
   private int burned;

   public ZombieVillagerEntityMCA(EntityType<? extends ZombieVillager> type, Level world, Gender gender) {
      super(type, world);
      this.genetics.setGender(gender);
   }

   protected void m_8097_() {
      super.m_8097_();
      this.getTypeDataManager().register(this);
   }

   @Override
   public CDataManager<ZombieVillagerEntityMCA> getTypeDataManager() {
      return DATA;
   }

   @Override
   public Genetics getGenetics() {
      return this.genetics;
   }

   @Override
   public Traits getTraits() {
      return this.traits;
   }

   @Override
   public VillagerBrain<?> getVillagerBrain() {
      return this.mcaBrain;
   }

   public ZombieCommandHandler getInteractions() {
      return this.interactions;
   }

   @Override
   public boolean isBurned() {
      return this.burned > 0;
   }

   public Relationship<ZombieVillagerEntityMCA> getRelationships() {
      return this.relations;
   }

   @Override
   public float getInfectionProgress() {
      return 1.0F;
   }

   @Override
   public void setInfectionProgress(float progress) {
   }

   @Nullable
   public final Component m_7770_() {
      String value = this.getTrackedValue(VILLAGER_NAME);
      return MCA.isBlankString(value) ? null : Component.m_237113_(value).m_130940_(ChatFormatting.RED);
   }

   public double m_6049_() {
      return -0.35;
   }

   public EntityDimensions m_6972_(Pose pose) {
      if (pose == Pose.SLEEPING) {
         return f_20910_;
      }

      float height = this.m_6134_() * 2.0F;
      float width = this.getHorizontalScaleFactor() * 0.6F;
      return EntityDimensions.m_20395_(width, height);
   }

   public float m_6134_() {
      return Math.min(0.999F, this.getRawScaleFactor());
   }

   protected float m_6431_(Pose pose, EntityDimensions size) {
      return this.m_6134_() * 1.75F;
   }

   public final InteractionResult m_7111_(Player player, Vec3 pos, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (hand.equals(InteractionHand.MAIN_HAND)
         && !stack.m_204117_(TagsMCA.Items.ZOMBIE_EGGS)
         && stack.m_41720_() != Items.f_42436_
         && player instanceof ServerPlayer) {
         String t = new String(new char[this.m_217043_().m_188503_(8) + 2]).replace("\u0000", ". ");
         this.sendChatMessage(Component.m_237113_(t), player);
      }

      return super.m_7111_(player, pos, hand);
   }

   @Nullable
   public SpawnGroupData m_6518_(
      ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt
   ) {
      SpawnGroupData data = super.m_6518_(world, difficulty, spawnReason, entityData, entityNbt);
      if (this.getAgeState() == AgeState.UNASSIGNED) {
         if (this.f_19796_.m_188501_() < Config.getInstance().babyZombieChance) {
            this.setAgeState(this.m_6162_() ? AgeState.BABY : AgeState.random());
         } else {
            this.setAgeState(AgeState.ADULT);
         }
      }

      if (this.getAgeState() == AgeState.BABY) {
         this.setAgeState(AgeState.TODDLER);
      }

      this.initialize(spawnReason);
      return data;
   }

   protected void m_5502_(Player player, Mob child) {
      child.m_6518_((ServerLevelAccessor)this.m_9236_(), this.m_9236_().m_6436_(child.m_20183_()), MobSpawnType.SPAWN_EGG, null, null);
   }

   public void m_8107_() {
      super.m_8107_();
      this.burned--;
      if (this.m_6060_()) {
         this.burned = Config.getInstance().burnedClothingTickLength;
      }

      if (this.burned > 0) {
         this.spawnBurntParticles();
      }
   }

   public void m_6863_(boolean isBaby) {
      super.m_6863_(isBaby);
      this.setAgeState(isBaby ? AgeState.BABY : AgeState.ADULT);
   }

   public void m_6593_(@Nullable Component name) {
      super.m_6593_(name);
      if (name != null) {
         this.setName(name.getString());
      }
   }

   @Override
   public boolean isHostile() {
      return true;
   }

   public void m_6667_(DamageSource cause) {
      super.m_6667_(cause);
      if (!this.m_9236_().f_46443_) {
         InventoryUtils.dropAllItems(this, this.inventory);
         this.relations.onDeath(cause);
      }
   }

   public void setInventory(UpdatableInventory inventory) {
      CompoundTag nbt = new CompoundTag();
      InventoryUtils.saveToNBT(inventory, nbt);
      InventoryUtils.readFromNBT(this.inventory, nbt);
   }

   @Nullable
   public <T extends Mob> T m_21406_(EntityType<T> type, boolean keepInventory) {
      T mob;
      if (!this.m_213877_() && type == EntityType.f_20492_) {
         mob = (T)super.m_21406_(this.getGenetics().getGender().getVillagerType(), keepInventory);
      } else {
         mob = (T)super.m_21406_(type, keepInventory);
      }

      if (mob instanceof VillagerLike<?> villager) {
         villager.copyVillagerAttributesFrom(this);
         villager.setInfected(false);
      }

      if (mob instanceof VillagerEntityMCA villager) {
         villager.m_20084_(this.m_20148_());
         villager.setInventory(this.inventory);
         villager.m_146762_(this.getAgeState().toAge());
      }

      return mob;
   }

   public void m_7378_(CompoundTag nbt) {
      super.m_7378_(nbt);
      this.getTypeDataManager().load(this, nbt);
      this.relations.readFromNbt(nbt);
      this.updateSpeed();
      this.inventory.m_6211_();
      InventoryUtils.readFromNBT(this.inventory, nbt);
      this.validateClothes();
   }

   public final void m_7380_(CompoundTag nbt) {
      super.m_7380_(nbt);
      this.getTypeDataManager().save(this, nbt);
      this.relations.writeToNbt(nbt);
      InventoryUtils.saveToNBT(this.inventory, nbt);
   }

   public void m_7350_(EntityDataAccessor<?> par) {
      if (this.getTypeDataManager().isParam(AGE_STATE, par) || this.getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
         this.m_6210_();
      }

      super.m_7350_(par);
   }

   protected boolean m_8028_() {
      return !this.m_21532_();
   }
}
