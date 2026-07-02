package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;

public class WeaponCapability extends CapabilityItem {
   protected final Function<LivingEntityPatch<?>, Style> stylegetter;
   protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
   protected final Skill passiveSkill;
   protected final SoundEvent smashingSound;
   protected final SoundEvent hitSound;
   protected final HitParticleType hitParticle;
   protected final Map<Style, List<AnimationManager.AnimationAccessor<? extends AttackAnimation>>> autoAttackMotions;
   protected final Map<Style, Function<ItemStack, Skill>> innateSkill;
   protected final Map<Style, Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
   protected final boolean canBePlacedOffhand;
   @Deprecated
   protected final Function<Style, Boolean> comboCancel;
   protected final ComboCounterHandleEvent.ComboCounterHandler comboCounterHandler;
   protected final CapabilityItem.ZoomInType zoomInType;
   protected final float reach;

   protected WeaponCapability(CapabilityItem.Builder builder) {
      super(builder);
      WeaponCapability.Builder weaponBuilder = (WeaponCapability.Builder)builder;
      this.autoAttackMotions = weaponBuilder.autoAttackMotionMap;
      this.innateSkill = weaponBuilder.innateSkillByStyle;
      this.livingMotionModifiers = weaponBuilder.livingMotionModifiers;
      this.stylegetter = weaponBuilder.styleProvider;
      this.weaponCombinationPredicator = weaponBuilder.weaponCombinationPredicator;
      this.passiveSkill = weaponBuilder.passiveSkill;
      this.smashingSound = weaponBuilder.swingSound;
      this.hitParticle = weaponBuilder.hitParticle;
      this.hitSound = weaponBuilder.hitSound;
      this.canBePlacedOffhand = weaponBuilder.canBePlacedOffhand;
      this.comboCancel = weaponBuilder.comboCancel;
      this.comboCounterHandler = weaponBuilder.comboCounterHandler;
      this.zoomInType = weaponBuilder.zoomInType;
      this.reach = weaponBuilder.reach;
   }

   @Override
   public final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
      return this.autoAttackMotions.getOrDefault(this.getStyle(playerpatch), this.autoAttackMotions.get(CapabilityItem.Styles.COMMON));
   }

   @Override
   public final Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
      Function<ItemStack, Skill> innateProvider = this.innateSkill.getOrDefault(this.getStyle(playerpatch), this.innateSkill.get(CapabilityItem.Styles.COMMON));
      return innateProvider == null ? null : innateProvider.apply(itemstack);
   }

   @Override
   public Skill getPassiveSkill() {
      return this.passiveSkill;
   }

   @Override
   public final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion() {
      return this.autoAttackMotions.get(CapabilityItem.Styles.MOUNT);
   }

   @Override
   public Style getStyle(LivingEntityPatch<?> entitypatch) {
      return this.stylegetter.apply(entitypatch);
   }

   @Override
   public SoundEvent getSmashingSound() {
      return this.smashingSound;
   }

   @Override
   public SoundEvent getHitSound() {
      return this.hitSound;
   }

   @Override
   public HitParticleType getHitParticle() {
      return this.hitParticle;
   }

   @Override
   public boolean canBePlacedOffhand() {
      return this.canBePlacedOffhand;
   }

   @Override
   public boolean shouldCancelCombo(LivingEntityPatch<?> entitypatch) {
      return this.comboCancel.apply(this.getStyle(entitypatch));
   }

   @Override
   public int handleComboCounter(
      ComboCounterHandleEvent.Causal causal,
      PlayerPatch<?> entitypatch,
      @Nullable AnimationManager.AnimationAccessor<? extends MainFrameAnimation> nextAnimation,
      int original
   ) {
      return this.comboCounterHandler.handleComboCounter(this, causal, entitypatch, nextAnimation, original);
   }

   @Override
   public CapabilityItem.ZoomInType getZoomInType() {
      return this.zoomInType;
   }

   @Override
   public Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(
      LivingEntityPatch<?> player, InteractionHand hand
   ) {
      if (this.livingMotionModifiers != null && hand != InteractionHand.OFF_HAND) {
         Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> motions = this.livingMotionModifiers
            .getOrDefault(this.getStyle(player), Maps.newHashMap());
         this.livingMotionModifiers.getOrDefault(CapabilityItem.Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);
         return motions;
      } else {
         return super.getLivingMotionModifier(player, hand);
      }
   }

   @Override
   public UseAnim getUseAnimation(LivingEntityPatch<?> playerpatch) {
      if (this.livingMotionModifiers != null) {
         Style style = this.getStyle(playerpatch);
         if (this.livingMotionModifiers.containsKey(style) && this.livingMotionModifiers.get(style).containsKey(LivingMotions.BLOCK)) {
            return UseAnim.BLOCK;
         }
      }

      return UseAnim.NONE;
   }

   @Override
   public boolean canHoldInOffhandAlone() {
      return false;
   }

   @Override
   public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
      return super.checkOffhandValid(entitypatch) || this.weaponCombinationPredicator.apply(entitypatch);
   }

   @Override
   public boolean availableOnHorse() {
      return this.autoAttackMotions.containsKey(CapabilityItem.Styles.MOUNT);
   }

   @Override
   public float getReach() {
      return this.reach;
   }

   public static WeaponCapability.Builder builder() {
      return new WeaponCapability.Builder();
   }

   public static class Builder extends CapabilityItem.Builder {
      Function<LivingEntityPatch<?>, Style> styleProvider;
      Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
      Skill passiveSkill;
      SoundEvent swingSound;
      SoundEvent hitSound;
      HitParticleType hitParticle;
      Map<Style, List<AnimationManager.AnimationAccessor<? extends AttackAnimation>>> autoAttackMotionMap;
      Map<Style, Function<ItemStack, Skill>> innateSkillByStyle;
      Map<Style, Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
      Function<Style, Boolean> comboCancel;
      ComboCounterHandleEvent.ComboCounterHandler comboCounterHandler;
      boolean canBePlacedOffhand;
      CapabilityItem.ZoomInType zoomInType;
      float reach;

      protected Builder() {
         this.constructor = WeaponCapability::new;
         this.styleProvider = entitypatch -> CapabilityItem.Styles.ONE_HAND;
         this.weaponCombinationPredicator = entitypatch -> false;
         this.passiveSkill = null;
         this.swingSound = (SoundEvent)EpicFightSounds.WHOOSH.get();
         this.hitSound = (SoundEvent)EpicFightSounds.BLUNT_HIT.get();
         this.hitParticle = (HitParticleType)EpicFightParticles.HIT_BLADE.get();
         this.autoAttackMotionMap = Maps.newHashMap();
         this.innateSkillByStyle = Maps.newHashMap();
         this.livingMotionModifiers = null;
         this.canBePlacedOffhand = true;
         this.comboCancel = style -> true;
         this.comboCounterHandler = ComboCounterHandleEvent.ComboCounterHandler.DEFAULT_COMBO_HANDLER;
         this.zoomInType = CapabilityItem.ZoomInType.NONE;
         this.reach = 0.2F;
      }

      public WeaponCapability.Builder category(WeaponCategory category) {
         super.category(category);
         return this;
      }

      public WeaponCapability.Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
         this.styleProvider = styleProvider;
         return this;
      }

      public WeaponCapability.Builder passiveSkill(Skill passiveSkill) {
         this.passiveSkill = passiveSkill;
         return this;
      }

      public WeaponCapability.Builder swingSound(SoundEvent swingSound) {
         this.swingSound = swingSound;
         return this;
      }

      public WeaponCapability.Builder hitSound(SoundEvent hitSound) {
         this.hitSound = hitSound;
         return this;
      }

      public WeaponCapability.Builder hitParticle(HitParticleType hitParticle) {
         this.hitParticle = hitParticle;
         return this;
      }

      public WeaponCapability.Builder collider(Collider collider) {
         this.collider = collider;
         return this;
      }

      public WeaponCapability.Builder canBePlacedOffhand(boolean canBePlacedOffhand) {
         this.canBePlacedOffhand = canBePlacedOffhand;
         return this;
      }

      public WeaponCapability.Builder reach(float reach) {
         this.reach = reach;
         return this;
      }

      public WeaponCapability.Builder livingMotionModifier(
         Style wieldStyle, LivingMotion livingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation
      ) {
         if (AnimationManager.checkNull(animation)) {
            EpicFightMod.LOGGER.warn("Unable to put an empty animation to weapon capability builder: " + livingMotion + ", " + animation);
            return this;
         }

         if (this.livingMotionModifiers == null) {
            this.livingMotionModifiers = Maps.newHashMap();
         }

         if (!this.livingMotionModifiers.containsKey(wieldStyle)) {
            this.livingMotionModifiers.put(wieldStyle, Maps.newHashMap());
         }

         this.livingMotionModifiers.get(wieldStyle).put(livingMotion, animation);
         return this;
      }

      public WeaponCapability.Builder addStyleAttibutes(Style style, Pair<Attribute, AttributeModifier> attributePair) {
         super.addStyleAttibutes(style, attributePair);
         return this;
      }

      @SafeVarargs
      public final WeaponCapability.Builder newStyleCombo(Style style, AnimationManager.AnimationAccessor<? extends AttackAnimation>... animation) {
         this.autoAttackMotionMap.put(style, Lists.newArrayList(animation));
         return this;
      }

      public WeaponCapability.Builder weaponCombinationPredicator(Function<LivingEntityPatch<?>, Boolean> predicator) {
         this.weaponCombinationPredicator = predicator;
         return this;
      }

      public WeaponCapability.Builder innateSkill(Style style, Function<ItemStack, Skill> innateSkill) {
         this.innateSkillByStyle.put(style, innateSkill);
         return this;
      }

      @Deprecated
      public WeaponCapability.Builder comboCancel(Function<Style, Boolean> comboCancel) {
         this.comboCancel = comboCancel;
         return this;
      }

      public WeaponCapability.Builder comboCounterHandler(ComboCounterHandleEvent.ComboCounterHandler comboHandler) {
         this.comboCounterHandler = comboHandler;
         return this;
      }

      public WeaponCapability.Builder zoomInType(CapabilityItem.ZoomInType zoomInType) {
         this.zoomInType = zoomInType;
         return this;
      }

      public Map<Style, List<AnimationManager.AnimationAccessor<? extends AttackAnimation>>> getComboAnimations() {
         return ImmutableMap.copyOf(this.autoAttackMotionMap);
      }
   }
}
