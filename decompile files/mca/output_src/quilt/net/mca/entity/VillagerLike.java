package quilt.net.mca.entity;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1322;
import net.minecraft.class_1324;
import net.minecraft.class_1472;
import net.minecraft.class_1657;
import net.minecraft.class_1767;
import net.minecraft.class_2398;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3222;
import net.minecraft.class_3730;
import net.minecraft.class_3851;
import net.minecraft.class_5134;
import net.minecraft.class_5250;
import net.minecraft.class_5819;
import net.minecraft.class_1322.class_1323;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.ai.DialogueType;
import quilt.net.mca.entity.ai.Genetics;
import quilt.net.mca.entity.ai.Messenger;
import quilt.net.mca.entity.ai.Traits;
import quilt.net.mca.entity.ai.brain.VillagerBrain;
import quilt.net.mca.entity.ai.relationship.AgeState;
import quilt.net.mca.entity.ai.relationship.EntityRelationship;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.VillagerDimensions;
import quilt.net.mca.entity.interaction.EntityCommandHandler;
import quilt.net.mca.resources.ClothingList;
import quilt.net.mca.resources.HairList;
import quilt.net.mca.resources.Names;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.server.world.data.PlayerSaveData;
import quilt.net.mca.util.network.datasync.CDataManager;
import quilt.net.mca.util.network.datasync.CDataParameter;
import quilt.net.mca.util.network.datasync.CEnumParameter;
import quilt.net.mca.util.network.datasync.CParameter;
import quilt.net.mca.util.network.datasync.CTrackedEntity;

public interface VillagerLike<E extends class_1297 & VillagerLike<E>> extends CTrackedEntity<E>, class_3851, Infectable, Messenger {
   CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
   CDataParameter<String> CUSTOM_SKIN = CParameter.create("custom_skin", "");
   CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
   CDataParameter<String> HAIR = CParameter.create("hair", "");
   CDataParameter<Float> HAIR_COLOR_RED = CParameter.create("hair_color_red", 0.0F);
   CDataParameter<Float> HAIR_COLOR_GREEN = CParameter.create("hair_color_green", 0.0F);
   CDataParameter<Float> HAIR_COLOR_BLUE = CParameter.create("hair_color_blue", 0.0F);
   CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);
   UUID SPEED_ID = UUID.fromString("1eaf83ff-7207-5596-c37a-d7a07b3ec4ce");

   static <E extends class_1297> CDataManager.Builder<E> createTrackedData(Class<E> type) {
      return new CDataManager.Builder<>(type)
         .addAll(VILLAGER_NAME, CUSTOM_SKIN, CLOTHES, HAIR, HAIR_COLOR_RED, HAIR_COLOR_GREEN, HAIR_COLOR_BLUE, AGE_STATE)
         .add(Genetics::createTrackedData)
         .add(Traits::createTrackedData)
         .add(VillagerBrain::createTrackedData);
   }

   Genetics getGenetics();

   Traits getTraits();

   VillagerBrain<?> getVillagerBrain();

   EntityCommandHandler<?> getInteractions();

   default void initialize(class_3730 spawnReason) {
      if (spawnReason != class_3730.field_16468) {
         if (spawnReason != class_3730.field_16466) {
            this.getGenetics().randomize();
            this.getTraits().randomize();
         }

         this.initializeSkin(false);
         this.getVillagerBrain().randomize();
      }

      if (this.getGenetics().getGender() == Gender.UNASSIGNED) {
         this.getGenetics().setGender(Gender.getRandom());
      }

      if (Strings.isNullOrEmpty(this.getTrackedValue(VILLAGER_NAME))) {
         this.setName(Names.pickCitizenName(this.getGenetics().getGender(), this.asEntity()));
      }

      this.validateClothes();
      this.asEntity().method_18382();
   }

   @Override
   default boolean isSpeechImpaired() {
      return this.getInfectionProgress() > 0.6F;
   }

   @Override
   default boolean isToYoungToSpeak() {
      return this.getAgeState() == AgeState.BABY;
   }

   default void setName(String name) {
      this.setTrackedValue(VILLAGER_NAME, name);
      if (!this.asEntity().method_37908().field_9236) {
         EntityRelationship.of(this.asEntity()).ifPresent(relationship -> relationship.getFamilyEntry().setName(name));
      }
   }

   default void setCustomSkin(String name) {
      this.setTrackedValue(CUSTOM_SKIN, name);
   }

   default void updateCustomSkin() {
   }

   default GameProfile getGameProfile() {
      return null;
   }

   default boolean hasCustomSkin() {
      if (!MCA.isBlankString(this.getTrackedValue(CUSTOM_SKIN)) && this.getGameProfile() != null) {
         class_310 minecraftClient = class_310.method_1551();
         Map<Type, MinecraftProfileTexture> map = minecraftClient.method_1582().method_4654(this.getGameProfile());
         return map.containsKey(Type.SKIN);
      } else {
         return false;
      }
   }

   default Set<Gender> getAttractedGenderSet(VillagerLike<?> villager) {
      if (villager.getTraits().hasTrait(Traits.BISEXUAL)) {
         return Set.of(Gender.MALE, Gender.FEMALE, Gender.NEUTRAL);
      } else if (villager.getTraits().hasTrait(Traits.HOMOSEXUAL)) {
         return Set.of(villager.getGenetics().getGender(), Gender.NEUTRAL);
      } else {
         return villager.getTraits().hasTrait(Traits.ASEXUAL) ? Set.of(Gender.NEUTRAL) : Set.of(villager.getGenetics().getGender().opposite(), Gender.NEUTRAL);
      }
   }

   default boolean canBeAttractedTo(VillagerLike<?> other) {
      return this.getAttractedGenderSet(this).contains(other.getGenetics().getGender())
         && this.getAttractedGenderSet(other).contains(this.getGenetics().getGender());
   }

   default boolean canBeAttractedTo(PlayerSaveData other) {
      return !Config.getInstance().enableGenderCheckForPlayers || this.canBeAttractedTo(toVillager(other));
   }

   default class_1268 getDominantHand() {
      return this.getTraits().hasTrait(Traits.LEFT_HANDED) ? class_1268.field_5810 : class_1268.field_5808;
   }

   default class_1268 getOpposingHand() {
      return this.getDominantHand() == class_1268.field_5810 ? class_1268.field_5808 : class_1268.field_5810;
   }

   default class_1304 getSlotForHand(class_1268 hand) {
      return hand == class_1268.field_5810 ? class_1304.field_6171 : class_1304.field_6173;
   }

   default class_1304 getDominantSlot() {
      return this.getSlotForHand(this.getDominantHand());
   }

   default class_1304 getOpposingSlot() {
      return this.getSlotForHand(this.getOpposingHand());
   }

   default class_2960 getProfessionId() {
      return MCA.locate("none");
   }

   default String getProfessionName() {
      String professionName = (this.getProfessionId().method_12836().equalsIgnoreCase("minecraft")
            ? (this.getProfessionId().method_12832().equals("none") ? "mca.none" : this.getProfessionId().method_12832())
            : this.getProfessionId().toString())
         .replace(":", ".");
      return MCA.isBlankString(professionName) ? "mca.none" : professionName;
   }

   default class_5250 getProfessionText() {
      return class_2561.method_43471("entity.minecraft.villager." + this.getProfessionName());
   }

   default boolean isProfessionImportant() {
      return false;
   }

   default boolean requiresHome() {
      return false;
   }

   default boolean canTradeWithProfession() {
      return false;
   }

   default String getClothes() {
      return this.getTrackedValue(CLOTHES);
   }

   default void setClothes(class_2960 clothes) {
      this.setClothes(clothes.toString());
   }

   default void setClothes(String clothes) {
      this.setTrackedValue(CLOTHES, clothes);
   }

   default String getHair() {
      return this.getTrackedValue(HAIR);
   }

   default void setHair(class_2960 hair) {
      this.setHair(hair.toString());
   }

   default void setHair(String hair) {
      this.setTrackedValue(HAIR, hair);
   }

   default void setHairDye(class_1767 color) {
      float[] components = (float[])color.method_7787().clone();
      float[] dye = this.getHairDye();
      if (dye[0] > 0.0F) {
         components[0] = components[0] * 0.5F + dye[0] * 0.5F;
         components[1] = components[1] * 0.5F + dye[1] * 0.5F;
         components[2] = components[2] * 0.5F + dye[2] * 0.5F;
      }

      this.setTrackedValue(HAIR_COLOR_RED, components[0]);
      this.setTrackedValue(HAIR_COLOR_GREEN, components[1]);
      this.setTrackedValue(HAIR_COLOR_BLUE, components[2]);
   }

   default void setHairDye(float r, float g, float b) {
      this.setTrackedValue(HAIR_COLOR_RED, r);
      this.setTrackedValue(HAIR_COLOR_GREEN, g);
      this.setTrackedValue(HAIR_COLOR_BLUE, b);
   }

   default void clearHairDye() {
      this.setHairDye(0.0F, 0.0F, 0.0F);
   }

   default float[] getHairDye() {
      return new float[]{this.getTrackedValue(HAIR_COLOR_RED), this.getTrackedValue(HAIR_COLOR_GREEN), this.getTrackedValue(HAIR_COLOR_BLUE)};
   }

   default AgeState getAgeState() {
      return this.getTrackedValue(AGE_STATE);
   }

   default VillagerDimensions getVillagerDimensions() {
      return this.getAgeState();
   }

   default void updateSpeed() {
      float speed = this.getVillagerBrain().getPersonality().getSpeedModifier();
      speed /= 0.9F + this.getGenetics().getGene(Genetics.WIDTH) * 0.2F;
      speed *= 0.9F + this.getGenetics().getGene(Genetics.SIZE) * 0.2F;
      speed *= this.getAgeState().getSpeed();
      class_1324 entityAttributeInstance = this.asEntity().method_5996(class_5134.field_23719);
      if (entityAttributeInstance != null) {
         if (entityAttributeInstance.method_6199(SPEED_ID) != null) {
            entityAttributeInstance.method_6200(SPEED_ID);
         }

         class_1322 speedModifier = new class_1322(SPEED_ID, "Speed", speed - 1.0F, class_1323.field_6330);
         entityAttributeInstance.method_26835(speedModifier);
      }
   }

   default boolean setAgeState(AgeState state) {
      AgeState old = this.getAgeState();
      if (state == old) {
         return false;
      }

      this.setTrackedValue(AGE_STATE, state);
      this.asEntity().method_18382();
      this.updateSpeed();
      return old != AgeState.UNASSIGNED;
   }

   default float getHorizontalScaleFactor() {
      if (this.getGenetics() != null && !Config.getInstance().useSquidwardModels) {
         return Math.min(
            0.999F,
            this.getGenetics().getHorizontalScaleFactor()
               * this.getTraits().getHorizontalScaleFactor()
               * this.getVillagerDimensions().getWidth()
               * this.getGenetics().getGender().getHorizontalScaleFactor()
         );
      } else {
         return this.asEntity().method_6109() ? 0.5F : 1.0F;
      }
   }

   default float getRawScaleFactor() {
      if (this.getGenetics() != null && !Config.getInstance().useSquidwardModels) {
         return this.getGenetics().getVerticalScaleFactor()
            * this.getTraits().getVerticalScaleFactor()
            * this.getVillagerDimensions().getHeight()
            * this.getGenetics().getGender().getScaleFactor();
      } else {
         return this.asEntity().method_6109() ? 0.5F : 1.0F;
      }
   }

   @Override
   default DialogueType getDialogueType(class_1657 receiver) {
      if (!receiver.method_37908().field_9236) {
         DialogueType type = DialogueType.fromAge(this.getAgeState());
         if (!receiver.method_37908().field_9236) {
            Optional<EntityRelationship> r = EntityRelationship.of(this.asEntity());
            if (r.isPresent()) {
               FamilyTreeNode relationship = r.get().getFamilyEntry();
               if (r.get().isMarriedTo(receiver.method_5667())) {
                  return DialogueType.SPOUSE;
               }

               if (r.get().isEngagedWith(receiver.method_5667())) {
                  return DialogueType.ENGAGED;
               }

               if (relationship.isParent(receiver.method_5667())) {
                  return type.toChild();
               }
            }
         }

         this.getVillagerBrain().getMemoriesForPlayer(receiver).setDialogueType(type);
      }

      return this.getVillagerBrain().getMemoriesForPlayer(receiver).getDialogueType();
   }

   default void initializeSkin(boolean isPlayer) {
      this.randomizeClothes();
      this.randomizeHair();
      if (!isPlayer) {
         class_1308 entity = this.asEntity();
         if (entity.method_6051().method_43057() < Config.getInstance().coloredHairChance) {
            int n = entity.method_6051().method_43048(25);
            int o = class_1767.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = entity.method_6051().method_43057();
            float[] fs = class_1472.method_6634(class_1767.method_7791(p));
            float[] gs = class_1472.method_6634(class_1767.method_7791(q));
            this.setTrackedValue(HAIR_COLOR_RED, fs[0] * (1.0F - r) + gs[0] * r);
            this.setTrackedValue(HAIR_COLOR_GREEN, fs[1] * (1.0F - r) + gs[1] * r);
            this.setTrackedValue(HAIR_COLOR_BLUE, fs[2] * (1.0F - r) + gs[2] * r);
         }
      }
   }

   default void randomizeClothes() {
      this.setClothes(ClothingList.getInstance().getPool(this).pickOne());
   }

   default void randomizeHair() {
      this.setHair(HairList.getInstance().getPool(this.getGenetics().getGender()).pickOne());
   }

   default void validateClothes() {
      if (!this.asEntity().method_37908().method_8608()) {
         if (!this.getClothes().startsWith("immersive_library") && !ClothingList.getInstance().clothing.containsKey(this.getClothes())) {
            if (this.getClothes() != null) {
               class_2960 identifier = new class_2960(this.getClothes());
               String id = identifier.method_12836() + ":skins/clothing/normal/" + identifier.method_12832();
               if (ClothingList.getInstance().clothing.containsKey(id)) {
                  this.setClothes(id);
               } else {
                  MCA.LOGGER.info(String.format(Locale.ROOT, "Villagers clothing %s does not exist!", this.getClothes()));
                  this.randomizeClothes();
               }
            } else {
               MCA.LOGGER.info(String.format(Locale.ROOT, "Villagers clothing %s does not exist!", this.getClothes()));
               this.randomizeClothes();
            }
         }

         if (!this.getHair().startsWith("immersive_library") && !HairList.getInstance().hair.containsKey(this.getHair())) {
            MCA.LOGGER.info(String.format(Locale.ROOT, "Villagers hair %s does not exist!", this.getHair()));
            this.randomizeHair();
         }
      }
   }

   default class_2487 toNbtForConversion(class_1299<?> convertingTo) {
      class_2487 output = new class_2487();
      this.getTypeDataManager().save((E)this.asEntity(), output);
      return output;
   }

   default void readNbtForConversion(class_1299<?> convertingFrom, class_2487 input) {
      this.getTypeDataManager().load((E)this.asEntity(), input);
   }

   default void copyVillagerAttributesFrom(VillagerLike<?> other) {
      this.readNbtForConversion(other.asEntity().method_5864(), other.toNbtForConversion(this.asEntity().method_5864()));
   }

   static VillagerLike<?> toVillager(PlayerSaveData player) {
      class_2487 villagerData = player.getEntityData();
      VillagerEntityMCA villager = (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(player.getWorld());
      assert villager != null;
      villager.method_5749(villagerData);
      return villager;
   }

   static VillagerLike<?> toVillager(class_1297 entity) {
      if (entity instanceof VillagerLike) {
         return (VillagerLike<?>)entity;
      } else {
         return entity instanceof class_3222 playerEntity ? toVillager(PlayerSaveData.get(playerEntity)) : null;
      }
   }

   default boolean isHostile() {
      return false;
   }

   default VillagerLike.PlayerModel getPlayerModel() {
      return VillagerLike.PlayerModel.VILLAGER;
   }

   boolean isBurned();

   default void spawnBurntParticles() {
      class_5819 random = this.asEntity().method_6051();
      if (random.method_43048(4) == 0) {
         double d = random.method_43059() * 0.02;
         double e = random.method_43059() * 0.02;
         double f = random.method_43059() * 0.02;
         this.asEntity()
            .method_37908()
            .method_8406(
               class_2398.field_11251, this.asEntity().method_23322(1.0), this.asEntity().method_23319() + 1.0, this.asEntity().method_23325(1.0), d, e, f
            );
      }
   }

   enum PlayerModel {
      VILLAGER,
      PLAYER,
      VANILLA;

      static final VillagerLike.PlayerModel[] VALUES = values();
   }
}
