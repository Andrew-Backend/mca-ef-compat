package fabric.net.mca.entity;

import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.TagsMCA;
import fabric.net.mca.entity.ai.Genetics;
import fabric.net.mca.entity.ai.Relationship;
import fabric.net.mca.entity.ai.Traits;
import fabric.net.mca.entity.ai.brain.VillagerBrain;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.CompassionateEntity;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.entity.interaction.ZombieCommandHandler;
import fabric.net.mca.util.InventoryUtils;
import fabric.net.mca.util.network.datasync.CDataManager;
import net.minecraft.class_124;
import net.minecraft.class_1266;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1315;
import net.minecraft.class_1641;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2940;
import net.minecraft.class_3222;
import net.minecraft.class_3730;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import net.minecraft.class_5425;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieVillagerEntityMCA
   extends class_1641
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

   public ZombieVillagerEntityMCA(class_1299<? extends class_1641> type, class_1937 world, Gender gender) {
      super(type, world);
      this.genetics.setGender(gender);
   }

   protected void method_5693() {
      super.method_5693();
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
   public final class_2561 method_5797() {
      String value = this.getTrackedValue(VILLAGER_NAME);
      return MCA.isBlankString(value) ? null : class_2561.method_43470(value).method_27692(class_124.field_1061);
   }

   public double method_5678() {
      return -0.35;
   }

   public class_4048 method_18377(class_4050 pose) {
      if (pose == class_4050.field_18078) {
         return field_18072;
      }

      float height = this.method_17825() * 2.0F;
      float width = this.getHorizontalScaleFactor() * 0.6F;
      return class_4048.method_18384(width, height);
   }

   public float method_17825() {
      return Math.min(0.999F, this.getRawScaleFactor());
   }

   protected float method_18394(class_4050 pose, class_4048 size) {
      return this.method_17825() * 1.75F;
   }

   public final class_1269 method_5664(class_1657 player, class_243 pos, @NotNull class_1268 hand) {
      class_1799 stack = player.method_5998(hand);
      if (hand.equals(class_1268.field_5808)
         && !stack.method_31573(TagsMCA.Items.ZOMBIE_EGGS)
         && stack.method_7909() != class_1802.field_8463
         && player instanceof class_3222) {
         String t = new String(new char[this.method_6051().method_43048(8) + 2]).replace("\u0000", ". ");
         this.sendChatMessage(class_2561.method_43470(t), player);
      }

      return super.method_5664(player, pos, hand);
   }

   @Nullable
   public class_1315 method_5943(
      class_5425 world, class_1266 difficulty, class_3730 spawnReason, @Nullable class_1315 entityData, @Nullable class_2487 entityNbt
   ) {
      class_1315 data = super.method_5943(world, difficulty, spawnReason, entityData, entityNbt);
      if (this.getAgeState() == AgeState.UNASSIGNED) {
         if (this.field_5974.method_43057() < Config.getInstance().babyZombieChance) {
            this.setAgeState(this.method_6109() ? AgeState.BABY : AgeState.random());
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

   protected void method_18249(class_1657 player, class_1308 child) {
      child.method_5943((class_5425)this.method_37908(), this.method_37908().method_8404(child.method_24515()), class_3730.field_16465, null, null);
   }

   public void method_6007() {
      super.method_6007();
      this.burned--;
      if (this.method_5809()) {
         this.burned = Config.getInstance().burnedClothingTickLength;
      }

      if (this.burned > 0) {
         this.spawnBurntParticles();
      }
   }

   public void method_7217(boolean isBaby) {
      super.method_7217(isBaby);
      this.setAgeState(isBaby ? AgeState.BABY : AgeState.ADULT);
   }

   public void method_5665(@Nullable class_2561 name) {
      super.method_5665(name);
      if (name != null) {
         this.setName(name.getString());
      }
   }

   @Override
   public boolean isHostile() {
      return true;
   }

   public void method_6078(class_1282 cause) {
      super.method_6078(cause);
      if (!this.method_37908().field_9236) {
         InventoryUtils.dropAllItems(this, this.inventory);
         this.relations.onDeath(cause);
      }
   }

   public void setInventory(UpdatableInventory inventory) {
      class_2487 nbt = new class_2487();
      InventoryUtils.saveToNBT(inventory, nbt);
      InventoryUtils.readFromNBT(this.inventory, nbt);
   }

   @Nullable
   public <T extends class_1308> T method_29243(class_1299<T> type, boolean keepInventory) {
      T mob;
      if (!this.method_31481() && type == class_1299.field_6077) {
         mob = (T)super.method_29243(this.getGenetics().getGender().getVillagerType(), keepInventory);
      } else {
         mob = (T)super.method_29243(type, keepInventory);
      }

      if (mob instanceof VillagerLike<?> villager) {
         villager.copyVillagerAttributesFrom(this);
         villager.setInfected(false);
      }

      if (mob instanceof VillagerEntityMCA villager) {
         villager.method_5826(this.method_5667());
         villager.setInventory(this.inventory);
         villager.method_5614(this.getAgeState().toAge());
      }

      return mob;
   }

   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      this.getTypeDataManager().load(this, nbt);
      this.relations.readFromNbt(nbt);
      this.updateSpeed();
      this.inventory.method_5448();
      InventoryUtils.readFromNBT(this.inventory, nbt);
      this.validateClothes();
   }

   public final void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      this.getTypeDataManager().save(this, nbt);
      this.relations.writeToNbt(nbt);
      InventoryUtils.saveToNBT(this.inventory, nbt);
   }

   public void method_5674(class_2940<?> par) {
      if (this.getTypeDataManager().isParam(AGE_STATE, par) || this.getTypeDataManager().isParam(Genetics.SIZE.getParam(), par)) {
         this.method_18382();
      }

      super.method_5674(par);
   }

   protected boolean method_23734() {
      return !this.method_5947();
   }
}
