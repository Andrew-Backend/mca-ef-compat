package yesman.epicfight.client.gui;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.effect.VisibleMobEffect;

public class HealthBar extends EntityUI {
   public static final ResourceLocation HEALTHBARS1 = EpicFightMod.identifier("textures/gui/healthbars1.png");
   public static final ResourceLocation HEALTHBARS2 = EpicFightMod.identifier("textures/gui/healthbars2.png");
   private final Map<LivingEntity, HealthBar.EntityAttributeTracker> trackingEntities = Maps.newConcurrentMap();

   @Override
   public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks) {
      ClientConfig.HealthBarVisibility healthBarVisibility = ClientConfig.healthBarVisibility;
      Minecraft mc = Minecraft.m_91087_();
      if (healthBarVisibility == ClientConfig.HealthBarVisibility.NONE) {
         return false;
      }

      if (entity.m_6095_().m_204039_(EntityTypes.BOSSES)) {
         return false;
      }

      HealthBar.EntityAttributeTracker healthTracker = this.trackingEntities
         .computeIfAbsent(entity, key -> new HealthBar.EntityAttributeTracker(key, entitypatch));
      healthTracker.checkChange(partialTicks);
      healthTracker.setKeepUp(true);
      if (!entity.m_20177_((Player)playerpatch.getOriginal()) && entity != playerpatch.getOriginal().m_20202_()) {
         if (entity.m_20280_(mc.m_91288_()) >= 400.0) {
            return false;
         }

         if (entity instanceof Player player) {
            if (player == playerpatch.getOriginal() && playerpatch.getMaxStunShield() <= 0.0F) {
               return false;
            }

            if (player.m_7500_() || player.m_5833_()) {
               return false;
            }
         }

         boolean showTarget = false;
         if (healthBarVisibility == ClientConfig.HealthBarVisibility.TARGET) {
            return playerpatch.getTarget() == entity;
         }

         if (healthBarVisibility == ClientConfig.HealthBarVisibility.TARGET_AND_HURT) {
            showTarget = playerpatch.getTarget() == entity;
         }

         return (!entity.m_21220_().isEmpty() || entity.m_21223_() < entity.m_21233_() || showTarget) && !entity.m_213877_();
      } else {
         return false;
      }
   }

   @Override
   public void draw(
      LivingEntity entity,
      @Nullable LivingEntityPatch<?> entitypatch,
      LocalPlayerPatch playerpatch,
      PoseStack poseStack,
      MultiBufferSource buffers,
      float partialTicks
   ) {
      Matrix4f modelViewMatrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, entity.m_20206_() + 0.25F, 0.0F, true, partialTicks);
      Collection<MobEffectInstance> activeEffects = entity.m_21220_();
      if (!activeEffects.isEmpty() && !entity.m_7306_(playerpatch.getOriginal())) {
         Iterator<MobEffectInstance> iter = activeEffects.iterator();
         int acives = activeEffects.size();
         int row = acives > 1 ? 1 : 0;
         int column = (acives - 1) / 2;
         float startX = -0.8F + -0.3F * row;
         float startY = -0.15F + 0.15F * column;

         for (int i = 0; i <= column; i++) {
            for (int j = 0; j <= row; j++) {
               MobEffectInstance effectInstance = iter.next();
               MobEffect effect = effectInstance.m_19544_();
               ResourceLocation rl;
               if (effect instanceof VisibleMobEffect visibleMobEffect) {
                  rl = visibleMobEffect.getIcon(effectInstance);
               } else {
                  rl = ResourceLocation.fromNamespaceAndPath(
                     ForgeRegistries.MOB_EFFECTS.getKey(effect).m_135827_(),
                     "textures/mob_effect/" + ForgeRegistries.MOB_EFFECTS.getKey(effect).m_135815_() + ".png"
                  );
               }

               float x = startX + 0.3F * j;
               float y = startY + -0.3F * i;
               drawUIAsLevelModel(modelViewMatrix, rl, buffers, x, y, x + 0.3F, y + 0.3F, 0, 0, 256, 256, 256);
               if (!iter.hasNext()) {
                  break;
               }
            }
         }
      }

      HealthBar.EntityAttributeTracker attributeTracker = this.trackingEntities.get(entity);
      ResourceLocation healthBarTexture;
      int damageColor;
      int textureIndex;
      if (entitypatch != null) {
         Faction faction = entitypatch.getFaction();
         healthBarTexture = faction.healthBarTexture();
         textureIndex = faction.healthBarIndex();
         damageColor = faction.damageColor();
      } else {
         healthBarTexture = HEALTHBARS2;
         textureIndex = 0;
         damageColor = -65536;
      }

      int texV = textureIndex * 10;
      float healthBarHeight = 0.048828125F;
      float innerHealthBarHeight = 0.029296875F;
      float maxHealth = entity.m_21233_();
      float partialHealth = attributeTracker.healthState.getAnimatedValue(entity.f_19797_, partialTicks);
      float partialAbsorption = attributeTracker.absorptionState.getAnimatedValue(entity.f_19797_, partialTicks);
      drawUIAsLevelModel(modelViewMatrix, healthBarTexture, buffers, -0.5F, -0.048828125F, 0.5F, 0.048828125F, 0.0F, texV / 64.0F, 1.0F, (texV + 5) / 64.0F);
      float healthEnd;
      if (!(partialAbsorption > 0.0F) && !attributeTracker.absorptionState.hasAnimation()) {
         healthEnd = 1.0F;
      } else {
         boolean isTotalOverMaxHealth = partialHealth + partialAbsorption > maxHealth;
         float absorptionStart = isTotalOverMaxHealth
            ? Mth.m_14036_(partialHealth / (partialHealth + partialAbsorption), 0.0F, 1.0F)
            : partialHealth / maxHealth;
         float absorptionEnd = isTotalOverMaxHealth
            ? Mth.m_14036_((partialHealth + partialAbsorption) / maxHealth, 0.0F, 1.0F)
            : (partialHealth + partialAbsorption) / maxHealth;
         drawUIAsLevelModel(
            modelViewMatrix,
            HEALTHBARS2,
            buffers,
            absorptionStart - 0.5F,
            -0.048828125F,
            absorptionEnd - 0.5F,
            0.048828125F,
            absorptionStart,
            0.921875F,
            absorptionEnd,
            1.0F
         );
         if (attributeTracker.absorptionState.hasAnimation()) {
            float lostAbsorptionStart = Mth.m_14036_(entity.m_6103_() / partialAbsorption, 0.0F, 1.0F);
            lostAbsorptionStart = absorptionStart + (absorptionEnd - absorptionStart) * lostAbsorptionStart;
            drawColoredQuadAsLevelModel(modelViewMatrix, buffers, lostAbsorptionStart - 0.5F, -0.029296875F, absorptionEnd - 0.5F, 0.029296875F, 1694437888);
         }

         healthEnd = isTotalOverMaxHealth ? absorptionStart : 1.0F;
      }

      float ratio = Mth.m_14036_(entity.m_21223_() / maxHealth, 0.0F, 1.0F);
      float filledHealthEnd = Math.max(-0.5F + ratio * healthEnd, -0.46875F);
      drawUIAsLevelModel(
         modelViewMatrix,
         healthBarTexture,
         buffers,
         -0.5F,
         -0.048828125F,
         filledHealthEnd,
         0.048828125F,
         0.0F,
         (texV + 5) / 64.0F,
         ratio * healthEnd,
         (texV + 10) / 64.0F
      );
      if (attributeTracker.healthState.hasAnimation()) {
         float animatedHealthRatio = Mth.m_14036_(partialHealth / maxHealth, 0.0F, 1.0F);
         float animatedHealthModelX = Math.min(-0.5F + animatedHealthRatio, 0.46875F);
         drawColoredQuadAsLevelModel(modelViewMatrix, buffers, filledHealthEnd, -0.029296875F, animatedHealthModelX, 0.029296875F, damageColor);
      }

      if (attributeTracker.stunShieldState != null) {
         if (entitypatch.getStunShield() == 0.0F) {
            return;
         }

         drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.5F, -0.08F, 0.5F, -0.048828125F, 1, 0, 63, 5, 256);
         float stunShieldRatio = Mth.m_14036_(entitypatch.getStunShield() / entitypatch.getMaxStunShield(), 0.0F, 1.0F);
         float shieldRatio = -0.5F + stunShieldRatio;
         int stunShieldTextureRatio = (int)(62.0F * stunShieldRatio);
         drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.5F, -0.08F, shieldRatio, -0.048828125F, 1, 5, stunShieldTextureRatio, 10, 256);
         float animatedStunShieldPosition = Mth.m_14036_(
            attributeTracker.stunShieldState.getAnimatedValue(entity.f_19797_, partialTicks) / entitypatch.getMaxStunShield(), 0.0F, 1.0F
         );
         drawColoredQuadAsLevelModel(modelViewMatrix, buffers, shieldRatio, -0.08F, animatedStunShieldPosition - 0.5F, -0.048828125F, -1996549632);
      }
   }

   public void reset() {
      this.trackingEntities.values().forEach(tracker -> tracker.setKeepUp(false));
   }

   public void remove() {
      this.trackingEntities.entrySet().removeIf(entry -> !entry.getValue().canKeepUp());
   }

   public void tick() {
      this.trackingEntities.values().forEach(HealthBar.EntityAttributeTracker::tick);
   }

   public class EntityAttributeTracker {
      private final LivingEntity entity;
      private final HealthBar.EntityAttributeTracker.AttributeState healthState;
      private final HealthBar.EntityAttributeTracker.AttributeState absorptionState;
      @Nullable
      private final HealthBar.EntityAttributeTracker.AttributeState stunShieldState;
      private boolean canKeepUp;

      public EntityAttributeTracker(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch) {
         this.entity = entity;
         this.healthState = new HealthBar.EntityAttributeTracker.AttributeState(() -> entity.m_21233_(), () -> entity.m_21223_());
         this.absorptionState = new HealthBar.EntityAttributeTracker.AttributeState(() -> entity.m_21233_(), () -> entity.m_6103_());
         this.stunShieldState = entitypatch != null
            ? new HealthBar.EntityAttributeTracker.AttributeState(() -> entitypatch.getMaxStunShield(), () -> entitypatch.getStunShield())
            : null;
      }

      public void setKeepUp(boolean canKeepUp) {
         this.canKeepUp = canKeepUp;
      }

      public boolean canKeepUp() {
         return this.canKeepUp;
      }

      public void checkChange(float partialTick) {
         this.healthState.checkState(this.entity.f_19797_, partialTick);
         this.absorptionState.checkState(this.entity.f_19797_, partialTick);
         if (this.stunShieldState != null) {
            this.stunShieldState.checkState(this.entity.f_19797_, partialTick);
         }
      }

      public void tick() {
         this.healthState.tick(this.entity.f_19797_);
         this.absorptionState.tick(this.entity.f_19797_);
         if (this.stunShieldState != null) {
            this.stunShieldState.tick(this.entity.f_19797_);
         }
      }

      class AttributeState {
         final Supplier<Float> maxValueGetter;
         final Supplier<Float> currentValueGetter;
         float value;
         float valueO;
         int lastChangeTick;
         int animationFrames;

         AttributeState(Supplier<Float> maxValueGetter, Supplier<Float> currentValueGetter) {
            this.maxValueGetter = maxValueGetter;
            this.currentValueGetter = currentValueGetter;
            float initValue = currentValueGetter.get();
            this.value = initValue;
            this.valueO = initValue;
            this.lastChangeTick = 0;
            this.animationFrames = 0;
         }

         void checkState(int tickCount, float partialTick) {
            float currentValue = this.currentValueGetter.get();
            if (this.value != currentValue) {
               if (this.animationFrames > 0) {
                  this.valueO = this.getAnimatedValue(tickCount, partialTick);
               }

               this.lastChangeTick = tickCount + 3;
               this.animationFrames = currentValue == 0.0F ? 4 : Math.max(4, (int)(Math.abs(this.valueO - currentValue) / this.maxValueGetter.get() * 10.0F));
               this.value = currentValue;
            }
         }

         boolean hasAnimation() {
            return this.animationFrames > 0;
         }

         void tick(int tickCount) {
            if (tickCount - this.lastChangeTick >= this.animationFrames) {
               this.animationFrames = 0;
               this.valueO = this.value;
            }
         }

         float getAnimatedValue(int tickCount, float partialTick) {
            if (this.animationFrames == 0) {
               return this.value;
            }

            if (tickCount < this.lastChangeTick) {
               return this.valueO;
            }

            float divide = (float)(tickCount - this.lastChangeTick) / this.animationFrames;
            float partial = divide + partialTick / this.animationFrames;
            return this.valueO + (this.value - this.valueO) * partial;
         }
      }
   }
}
