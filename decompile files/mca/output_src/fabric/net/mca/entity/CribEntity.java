package fabric.net.mca.entity;

import fabric.net.mca.MCA;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.item.BabyItem;
import fabric.net.mca.item.CribItem;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CDataParameter;
import fabric.net.mca.util.network.datasync.CEnumParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import fabric.net.mca.util.network.datasync.CTrackedEntity;
import java.util.Arrays;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1313;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1767;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2388;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2596;
import net.minecraft.class_2602;
import net.minecraft.class_2604;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_8103;

public class CribEntity extends class_1297 implements CTrackedEntity<CribEntity> {
   VillagerEntityMCA infant;
   private static final CDataParameter<class_1799> BABY = CParameter.create("babyItem", class_1799.field_8037);
   private static final CEnumParameter<CribWoodType> WOOD = CParameter.create("wood", CribWoodType.OAK);
   private static final CEnumParameter<class_1767> COLOR = CParameter.create("color", class_1767.field_7964);
   private static final CDataManager<CribEntity> DATA = createTrackedData().build();

   static CDataManager.Builder<CribEntity> createTrackedData() {
      return new CDataManager.Builder<>(CribEntity.class).addAll(BABY, WOOD, COLOR);
   }

   public CribEntity(class_1299<? extends CribEntity> type, class_1937 world) {
      super(type, world);
   }

   public CribWoodType getWoodType() {
      return this.getTrackedValue(WOOD);
   }

   public void setWoodType(CribWoodType wood) {
      this.setTrackedValue(WOOD, wood);
   }

   public class_1767 getColor() {
      return this.getTrackedValue(COLOR);
   }

   public void setColor(class_1767 color) {
      this.setTrackedValue(COLOR, color);
   }

   public class_1799 getBabyItem() {
      return this.getTrackedValue(BABY);
   }

   private boolean isOccupied() {
      return !this.getTrackedValue(BABY).equals(class_1799.field_8037) || this.infant != null;
   }

   public boolean method_30948() {
      return true;
   }

   public boolean method_5675() {
      return false;
   }

   public boolean method_5810() {
      return false;
   }

   protected void method_5693() {
      this.getTypeDataManager().register(this);
   }

   protected void method_5749(class_2487 nbt) {
      class_2487 compound = nbt.method_10562("mca");
      if (compound.method_10545("Baby")) {
         this.setTrackedValue(BABY, class_1799.method_7915(compound.method_10562("Baby")));
         if (this.getTrackedValue(BABY).equals(class_1799.field_8037)) {
            MCA.LOGGER.warn("Issue deseriaslizing baby item from crib NBT!");
         }
      }

      if (compound.method_10545("Wood")) {
         this.setTrackedValue(WOOD, CribWoodType.values()[compound.method_10550("Wood")]);
      }

      if (compound.method_10545("Color")) {
         this.setTrackedValue(COLOR, class_1767.values()[compound.method_10550("Color")]);
      }
   }

   public double method_5621() {
      return 0.42;
   }

   protected void method_5652(class_2487 nbt) {
      class_2487 mcaCompound = new class_2487();
      if (!this.getTrackedValue(BABY).equals(class_1799.field_8037)) {
         class_2487 babyCompound = new class_2487();
         this.getTrackedValue(BABY).method_7953(babyCompound);
         mcaCompound.method_10566("Baby", babyCompound);
      }

      mcaCompound.method_10569("Wood", Arrays.asList(CribWoodType.values()).indexOf(this.getTrackedValue(WOOD)));
      mcaCompound.method_10569("Color", Arrays.asList(class_1767.values()).indexOf(this.getTrackedValue(COLOR)));
      nbt.method_10566("mca", mcaCompound);
   }

   public class_2596<class_2602> method_18002() {
      return new class_2604(this);
   }

   private void setEntityOccupant(VillagerEntityMCA occupant) {
      this.infant = occupant;
      this.infant.method_5684(true);
   }

   private void unsetEntityOccupant() {
      if (this.infant != null) {
         this.infant.method_5684(false);
         this.infant = null;
      }
   }

   public class_1269 method_5688(class_1657 player, class_1268 hand) {
      if (this.method_5782() && this.method_31483() instanceof VillagerEntityMCA && this.infant == null) {
         this.setEntityOccupant((VillagerEntityMCA)this.method_31483());
      }

      if (this.infant != null && this.infant.method_5854() == this) {
         this.infant.method_5873(player, true);
         this.unsetEntityOccupant();
      } else if (!this.getTrackedValue(BABY).equals(class_1799.field_8037)) {
         player.method_31548().method_7394(this.getTrackedValue(BABY));
         this.setTrackedValue(BABY, class_1799.field_8037);
      } else if (player.method_31548().method_7391() != class_1799.field_8037 && player.method_31548().method_7391().method_7909() instanceof BabyItem) {
         this.setTrackedValue(BABY, player.method_31548().method_7391());
         player.method_31548().method_7378(this.getTrackedValue(BABY));
      } else {
         if (player.method_31483() == null || !(player.method_31483() instanceof VillagerEntityMCA)) {
            return class_1269.field_5811;
         }

         VillagerEntityMCA rider = (VillagerEntityMCA)player.method_31483();
         if (rider.getAgeState() == AgeState.BABY) {
            this.setEntityOccupant(rider);
            this.infant.method_5873(this, true);
         }
      }

      return class_1269.field_5812;
   }

   public boolean method_5698(class_1297 attacker) {
      return attacker instanceof class_1657 && !this.method_37908().method_8505((class_1657)attacker, this.method_24515());
   }

   public boolean method_5863() {
      return true;
   }

   public void method_5773() {
      super.method_5773();
      if (this.method_24828()) {
         this.method_18799(class_243.field_1353);
      } else if (!this.method_5740()) {
         this.method_18799(this.method_18798().method_1031(0.0, -0.04, 0.0));
      }

      this.method_5784(class_1313.field_6308, this.method_18798());
      if (this.getTrackedValue(BABY) != class_1799.field_8037 && this.getTrackedValue(BABY).method_7909() instanceof BabyItem) {
         this.getTrackedValue(BABY).method_7909().method_7888(this.getTrackedValue(BABY), this.method_37908(), this, 0, false);
      }
   }

   public boolean method_5643(class_1282 source, float amount) {
      if (this.method_37908().field_9236 || this.method_31481()) {
         return false;
      }

      if (this.isOccupied()) {
         return false;
      }

      if (this.method_5679(source)) {
         return false;
      }

      if (!source.method_48789(class_8103.field_42249) && !source.method_48789(class_8103.field_42246)) {
         boolean bl = source.method_5526() instanceof class_1665;
         boolean bl2 = bl && ((class_1665)source.method_5526()).method_7447() > 0;
         boolean bl3 = "player".equals(source.method_5525());
         if (!bl3 && !bl) {
            return false;
         } else if (source.method_5529() instanceof class_1657 && !((class_1657)source.method_5529()).method_31549().field_7476) {
            return false;
         } else if (source.method_5530()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.method_5768();
            return bl2;
         } else {
            CribItem matchingType = (CribItem)ItemsMCA.CRIBS
               .stream()
               .filter(c -> ((CribItem)c.get()).getColor() == this.getTrackedValue(COLOR) && ((CribItem)c.get()).getWood() == this.getTrackedValue(WOOD))
               .findFirst()
               .get()
               .get();
            class_2248.method_9577(this.method_37908(), this.method_24515(), new class_1799(matchingType));
            this.spawnBreakParticles();
            this.method_5768();
            return true;
         }
      } else {
         this.method_5768();
         return false;
      }
   }

   private void spawnBreakParticles() {
      if (this.method_37908() instanceof class_3218) {
         ((class_3218)this.method_37908())
            .method_14199(
               new class_2388(class_2398.field_11217, class_2246.field_10161.method_9564()),
               this.method_23317(),
               this.method_23323(0.6666666666666666),
               this.method_23321(),
               10,
               this.method_17681() / 4.0F,
               this.method_17682() / 4.0F,
               this.method_17681() / 4.0F,
               0.05
            );
      }
   }

   private void playBreakSound() {
      this.method_37908()
         .method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), class_3417.field_15118, this.method_5634(), 1.0F, 1.0F);
   }

   @Override
   public CDataManager<CribEntity> getTypeDataManager() {
      return DATA;
   }
}
