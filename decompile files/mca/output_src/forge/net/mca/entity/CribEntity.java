package forge.net.mca.entity;

import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.item.BabyItem;
import forge.net.mca.item.CribItem;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CEnumParameter;
import forge.net.mca.util.network.datasync.CParameter;
import forge.net.mca.util.network.datasync.CTrackedEntity;
import java.util.Arrays;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class CribEntity extends Entity implements CTrackedEntity<CribEntity> {
   VillagerEntityMCA infant;
   private static final CDataParameter<ItemStack> BABY = CParameter.create("babyItem", ItemStack.f_41583_);
   private static final CEnumParameter<CribWoodType> WOOD = CParameter.create("wood", CribWoodType.OAK);
   private static final CEnumParameter<DyeColor> COLOR = CParameter.create("color", DyeColor.RED);
   private static final CDataManager<CribEntity> DATA = createTrackedData().build();

   static CDataManager.Builder<CribEntity> createTrackedData() {
      return new CDataManager.Builder<>(CribEntity.class).addAll(BABY, WOOD, COLOR);
   }

   public CribEntity(EntityType<? extends CribEntity> type, Level world) {
      super(type, world);
   }

   public CribWoodType getWoodType() {
      return this.getTrackedValue(WOOD);
   }

   public void setWoodType(CribWoodType wood) {
      this.setTrackedValue(WOOD, wood);
   }

   public DyeColor getColor() {
      return this.getTrackedValue(COLOR);
   }

   public void setColor(DyeColor color) {
      this.setTrackedValue(COLOR, color);
   }

   public ItemStack getBabyItem() {
      return this.getTrackedValue(BABY);
   }

   private boolean isOccupied() {
      return !this.getTrackedValue(BABY).equals(ItemStack.f_41583_) || this.infant != null;
   }

   public boolean m_5829_() {
      return true;
   }

   public boolean m_6063_() {
      return false;
   }

   public boolean m_6094_() {
      return false;
   }

   protected void m_8097_() {
      this.getTypeDataManager().register(this);
   }

   protected void m_7378_(CompoundTag nbt) {
      CompoundTag compound = nbt.m_128469_("mca");
      if (compound.m_128441_("Baby")) {
         this.setTrackedValue(BABY, ItemStack.m_41712_(compound.m_128469_("Baby")));
         if (this.getTrackedValue(BABY).equals(ItemStack.f_41583_)) {
            MCA.LOGGER.warn("Issue deseriaslizing baby item from crib NBT!");
         }
      }

      if (compound.m_128441_("Wood")) {
         this.setTrackedValue(WOOD, CribWoodType.values()[compound.m_128451_("Wood")]);
      }

      if (compound.m_128441_("Color")) {
         this.setTrackedValue(COLOR, DyeColor.values()[compound.m_128451_("Color")]);
      }
   }

   public double m_6048_() {
      return 0.42;
   }

   protected void m_7380_(CompoundTag nbt) {
      CompoundTag mcaCompound = new CompoundTag();
      if (!this.getTrackedValue(BABY).equals(ItemStack.f_41583_)) {
         CompoundTag babyCompound = new CompoundTag();
         this.getTrackedValue(BABY).m_41739_(babyCompound);
         mcaCompound.m_128365_("Baby", babyCompound);
      }

      mcaCompound.m_128405_("Wood", Arrays.asList(CribWoodType.values()).indexOf(this.getTrackedValue(WOOD)));
      mcaCompound.m_128405_("Color", Arrays.asList(DyeColor.values()).indexOf(this.getTrackedValue(COLOR)));
      nbt.m_128365_("mca", mcaCompound);
   }

   public Packet<ClientGamePacketListener> m_5654_() {
      return new ClientboundAddEntityPacket(this);
   }

   private void setEntityOccupant(VillagerEntityMCA occupant) {
      this.infant = occupant;
      this.infant.m_20331_(true);
   }

   private void unsetEntityOccupant() {
      if (this.infant != null) {
         this.infant.m_20331_(false);
         this.infant = null;
      }
   }

   public InteractionResult m_6096_(Player player, InteractionHand hand) {
      if (this.m_20160_() && this.m_146895_() instanceof VillagerEntityMCA && this.infant == null) {
         this.setEntityOccupant((VillagerEntityMCA)this.m_146895_());
      }

      if (this.infant != null && this.infant.m_20202_() == this) {
         this.infant.m_7998_(player, true);
         this.unsetEntityOccupant();
      } else if (!this.getTrackedValue(BABY).equals(ItemStack.f_41583_)) {
         player.m_150109_().m_36054_(this.getTrackedValue(BABY));
         this.setTrackedValue(BABY, ItemStack.f_41583_);
      } else if (player.m_150109_().m_36056_() != ItemStack.f_41583_ && player.m_150109_().m_36056_().m_41720_() instanceof BabyItem) {
         this.setTrackedValue(BABY, player.m_150109_().m_36056_());
         player.m_150109_().m_36057_(this.getTrackedValue(BABY));
      } else {
         if (player.m_146895_() == null || !(player.m_146895_() instanceof VillagerEntityMCA)) {
            return InteractionResult.PASS;
         }

         VillagerEntityMCA rider = (VillagerEntityMCA)player.m_146895_();
         if (rider.getAgeState() == AgeState.BABY) {
            this.setEntityOccupant(rider);
            this.infant.m_7998_(this, true);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public boolean m_7313_(Entity attacker) {
      return attacker instanceof Player && !this.m_9236_().m_7966_((Player)attacker, this.m_20183_());
   }

   public boolean m_6087_() {
      return true;
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.m_20096_()) {
         this.m_20256_(Vec3.f_82478_);
      } else if (!this.m_20068_()) {
         this.m_20256_(this.m_20184_().m_82520_(0.0, -0.04, 0.0));
      }

      this.m_6478_(MoverType.SELF, this.m_20184_());
      if (this.getTrackedValue(BABY) != ItemStack.f_41583_ && this.getTrackedValue(BABY).m_41720_() instanceof BabyItem) {
         this.getTrackedValue(BABY).m_41720_().m_6883_(this.getTrackedValue(BABY), this.m_9236_(), this, 0, false);
      }
   }

   public boolean m_6469_(DamageSource source, float amount) {
      if (this.m_9236_().f_46443_ || this.m_213877_()) {
         return false;
      }

      if (this.isOccupied()) {
         return false;
      }

      if (this.m_6673_(source)) {
         return false;
      }

      if (!source.m_269533_(DamageTypeTags.f_268415_) && !source.m_269533_(DamageTypeTags.f_268745_)) {
         boolean bl = source.m_7640_() instanceof AbstractArrow;
         boolean bl2 = bl && ((AbstractArrow)source.m_7640_()).m_36796_() > 0;
         boolean bl3 = "player".equals(source.m_19385_());
         if (!bl3 && !bl) {
            return false;
         } else if (source.m_7639_() instanceof Player && !((Player)source.m_7639_()).m_150110_().f_35938_) {
            return false;
         } else if (source.m_19390_()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.m_6074_();
            return bl2;
         } else {
            CribItem matchingType = (CribItem)ItemsMCA.CRIBS
               .stream()
               .filter(c -> ((CribItem)c.get()).getColor() == this.getTrackedValue(COLOR) && ((CribItem)c.get()).getWood() == this.getTrackedValue(WOOD))
               .findFirst()
               .get()
               .get();
            Block.m_49840_(this.m_9236_(), this.m_20183_(), new ItemStack(matchingType));
            this.spawnBreakParticles();
            this.m_6074_();
            return true;
         }
      } else {
         this.m_6074_();
         return false;
      }
   }

   private void spawnBreakParticles() {
      if (this.m_9236_() instanceof ServerLevel) {
         ((ServerLevel)this.m_9236_())
            .m_8767_(
               new BlockParticleOption(ParticleTypes.f_123794_, Blocks.f_50705_.m_49966_()),
               this.m_20185_(),
               this.m_20227_(0.6666666666666666),
               this.m_20189_(),
               10,
               this.m_20205_() / 4.0F,
               this.m_20206_() / 4.0F,
               this.m_20205_() / 4.0F,
               0.05
            );
      }
   }

   private void playBreakSound() {
      this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11681_, this.m_5720_(), 1.0F, 1.0F);
   }

   @Override
   public CDataManager<CribEntity> getTypeDataManager() {
      return DATA;
   }
}
