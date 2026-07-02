package forge.net.mca.entity;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.entity.ai.DialogueType;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Messenger;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.VillagerDimensions;
import forge.net.mca.entity.interaction.EntityCommandHandler;
import forge.net.mca.resources.ClothingList;
import forge.net.mca.resources.HairList;
import forge.net.mca.resources.Names;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CEnumParameter;
import forge.net.mca.util.network.datasync.CParameter;
import forge.net.mca.util.network.datasync.CTrackedEntity;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public interface VillagerLike<E extends Entity & VillagerLike<E>> extends CTrackedEntity<E>, VillagerDataHolder, Infectable, Messenger {
   CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
   CDataParameter<String> CUSTOM_SKIN = CParameter.create("custom_skin", "");
   CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
   CDataParameter<String> HAIR = CParameter.create("hair", "");
   CDataParameter<Float> HAIR_COLOR_RED = CParameter.create("hair_color_red", 0.0F);
   CDataParameter<Float> HAIR_COLOR_GREEN = CParameter.create("hair_color_green", 0.0F);
   CDataParameter<Float> HAIR_COLOR_BLUE = CParameter.create("hair_color_blue", 0.0F);
   CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);
   UUID SPEED_ID = UUID.fromString("1eaf83ff-7207-5596-c37a-d7a07b3ec4ce");

   static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
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

   default void initialize(MobSpawnType spawnReason) {
      if (spawnReason != MobSpawnType.CONVERSION) {
         if (spawnReason != MobSpawnType.BREEDING) {
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
      this.asEntity().m_6210_();
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
      if (!this.asEntity().m_9236_().f_46443_) {
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
         Minecraft minecraftClient = Minecraft.m_91087_();
         Map<Type, MinecraftProfileTexture> map = minecraftClient.m_91109_().m_118815_(this.getGameProfile());
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

   default InteractionHand getDominantHand() {
      return this.getTraits().hasTrait(Traits.LEFT_HANDED) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   default InteractionHand getOpposingHand() {
      return this.getDominantHand() == InteractionHand.OFF_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
   }

   default EquipmentSlot getSlotForHand(InteractionHand hand) {
      return hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
   }

   default EquipmentSlot getDominantSlot() {
      return this.getSlotForHand(this.getDominantHand());
   }

   default EquipmentSlot getOpposingSlot() {
      return this.getSlotForHand(this.getOpposingHand());
   }

   default ResourceLocation getProfessionId() {
      return MCA.locate("none");
   }

   default String getProfessionName() {
      String professionName = (this.getProfessionId().m_135827_().equalsIgnoreCase("minecraft")
            ? (this.getProfessionId().m_135815_().equals("none") ? "mca.none" : this.getProfessionId().m_135815_())
            : this.getProfessionId().toString())
         .replace(":", ".");
      return MCA.isBlankString(professionName) ? "mca.none" : professionName;
   }

   default MutableComponent getProfessionText() {
      return Component.m_237115_("entity.minecraft.villager." + this.getProfessionName());
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

   default void setClothes(ResourceLocation clothes) {
      this.setClothes(clothes.toString());
   }

   default void setClothes(String clothes) {
      this.setTrackedValue(CLOTHES, clothes);
   }

   default String getHair() {
      return this.getTrackedValue(HAIR);
   }

   default void setHair(ResourceLocation hair) {
      this.setHair(hair.toString());
   }

   default void setHair(String hair) {
      this.setTrackedValue(HAIR, hair);
   }

   default void setHairDye(DyeColor color) {
      float[] components = (float[])color.m_41068_().clone();
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
      AttributeInstance entityAttributeInstance = this.asEntity().m_21051_(Attributes.f_22279_);
      if (entityAttributeInstance != null) {
         if (entityAttributeInstance.m_22111_(SPEED_ID) != null) {
            entityAttributeInstance.m_22120_(SPEED_ID);
         }

         AttributeModifier speedModifier = new AttributeModifier(SPEED_ID, "Speed", speed - 1.0F, Operation.MULTIPLY_BASE);
         entityAttributeInstance.m_22118_(speedModifier);
      }
   }

   default boolean setAgeState(AgeState state) {
      AgeState old = this.getAgeState();
      if (state == old) {
         return false;
      }

      this.setTrackedValue(AGE_STATE, state);
      this.asEntity().m_6210_();
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
         return this.asEntity().m_6162_() ? 0.5F : 1.0F;
      }
   }

   default float getRawScaleFactor() {
      if (this.getGenetics() != null && !Config.getInstance().useSquidwardModels) {
         return this.getGenetics().getVerticalScaleFactor()
            * this.getTraits().getVerticalScaleFactor()
            * this.getVillagerDimensions().getHeight()
            * this.getGenetics().getGender().getScaleFactor();
      } else {
         return this.asEntity().m_6162_() ? 0.5F : 1.0F;
      }
   }

   @Override
   default DialogueType getDialogueType(Player receiver) {
      if (!receiver.m_9236_().f_46443_) {
         DialogueType type = DialogueType.fromAge(this.getAgeState());
         if (!receiver.m_9236_().f_46443_) {
            Optional<EntityRelationship> r = EntityRelationship.of(this.asEntity());
            if (r.isPresent()) {
               FamilyTreeNode relationship = r.get().getFamilyEntry();
               if (r.get().isMarriedTo(receiver.m_20148_())) {
                  return DialogueType.SPOUSE;
               }

               if (r.get().isEngagedWith(receiver.m_20148_())) {
                  return DialogueType.ENGAGED;
               }

               if (relationship.isParent(receiver.m_20148_())) {
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
         Mob entity = this.asEntity();
         if (entity.m_217043_().m_188501_() < Config.getInstance().coloredHairChance) {
            int n = entity.m_217043_().m_188503_(25);
            int o = DyeColor.values().length;
            int p = n % o;
            int q = (n + 1) % o;
            float r = entity.m_217043_().m_188501_();
            float[] fs = Sheep.m_29829_(DyeColor.m_41053_(p));
            float[] gs = Sheep.m_29829_(DyeColor.m_41053_(q));
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
      if (!this.asEntity().m_9236_().m_5776_()) {
         if (!this.getClothes().startsWith("immersive_library") && !ClothingList.getInstance().clothing.containsKey(this.getClothes())) {
            if (this.getClothes() != null) {
               ResourceLocation identifier = new ResourceLocation(this.getClothes());
               String id = identifier.m_135827_() + ":skins/clothing/normal/" + identifier.m_135815_();
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

   default CompoundTag toNbtForConversion(EntityType<?> convertingTo) {
      CompoundTag output = new CompoundTag();
      this.getTypeDataManager().save((E)this.asEntity(), output);
      return output;
   }

   default void readNbtForConversion(EntityType<?> convertingFrom, CompoundTag input) {
      this.getTypeDataManager().load((E)this.asEntity(), input);
   }

   default void copyVillagerAttributesFrom(VillagerLike<?> other) {
      this.readNbtForConversion(other.asEntity().m_6095_(), other.toNbtForConversion(this.asEntity().m_6095_()));
   }

   static VillagerLike<?> toVillager(PlayerSaveData player) {
      CompoundTag villagerData = player.getEntityData();
      VillagerEntityMCA villager = (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(player.getWorld());
      assert villager != null;
      villager.m_7378_(villagerData);
      return villager;
   }

   static VillagerLike<?> toVillager(Entity entity) {
      if (entity instanceof VillagerLike) {
         return (VillagerLike<?>)entity;
      } else {
         return entity instanceof ServerPlayer playerEntity ? toVillager(PlayerSaveData.get(playerEntity)) : null;
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
      RandomSource random = this.asEntity().m_217043_();
      if (random.m_188503_(4) == 0) {
         double d = random.m_188583_() * 0.02;
         double e = random.m_188583_() * 0.02;
         double f = random.m_188583_() * 0.02;
         this.asEntity()
            .m_9236_()
            .m_7106_(ParticleTypes.f_123762_, this.asEntity().m_20208_(1.0), this.asEntity().m_20187_() + 1.0, this.asEntity().m_20262_(1.0), d, e, f);
      }
   }

   enum PlayerModel {
      VILLAGER,
      PLAYER,
      VANILLA;

      static final VillagerLike.PlayerModel[] VALUES = values();
   }
}
