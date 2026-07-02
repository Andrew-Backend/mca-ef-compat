package yesman.epicfight.skill.identity;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class MeteorSlamSkill extends Skill {
   private static final UUID EVENT_UUID = UUID.fromString("03181ad0-e750-11ed-a05b-0242ac120003");
   protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> slamMotions;
   private final double minDistance = 6.0;

   public static float getFallDistance(SkillContainer skillContainer) {
      return skillContainer.getDataManager().<Float>getDataValue((SkillDataKey<Float>)SkillDataKeys.FALL_DISTANCE.get());
   }

   public static MeteorSlamSkill.Builder createMeteorSlamBuilder() {
      return new MeteorSlamSkill.Builder()
         .addSlamMotion(CapabilityItem.WeaponCategories.SPEAR, (item, player) -> Animations.METEOR_SLAM)
         .addSlamMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.METEOR_SLAM)
         .addSlamMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.METEOR_SLAM)
         .addSlamMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.METEOR_SLAM)
         .setCategory(SkillCategories.IDENTITY)
         .setResource(Skill.Resource.NONE);
   }

   public MeteorSlamSkill(MeteorSlamSkill.Builder builder) {
      super(builder);
      this.slamMotions = builder.slamMotions;
   }

   @Override
   public void onInitiate(SkillContainer container) {
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.SKILL_CAST_EVENT,
         EVENT_UUID,
         event -> {
            if (!container.getExecutor().isLogicalClient()) {
               Skill skill = event.getSkillContainer().getSkill();
               if (skill.getCategory() != SkillCategories.BASIC_ATTACK) {
                  return;
               }

               if (container.getExecutor().getOriginal().m_20096_() || container.getExecutor().getOriginal().m_146909_() < 40.0F) {
                  return;
               }

               CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
               if (!this.slamMotions.containsKey(holdingItem.getWeaponCategory())) {
                  return;
               }

               AnimationManager.AnimationAccessor<? extends StaticAnimation> slamAnimation = this.slamMotions
                  .get(holdingItem.getWeaponCategory())
                  .apply(holdingItem, container.getExecutor());
               if (slamAnimation == null) {
                  return;
               }

               Vec3 vec3 = container.getExecutor().getOriginal().m_20299_(1.0F);
               Vec3 vec31 = container.getExecutor().getOriginal().m_20252_(1.0F);
               Vec3 vec32 = vec3.m_82520_(vec31.f_82479_ * 50.0, vec31.f_82480_ * 50.0, vec31.f_82481_ * 50.0);
               HitResult hitResult = container.getExecutor()
                  .getOriginal()
                  .m_9236_()
                  .m_45547_(new ClipContext(vec3, vec32, Block.COLLIDER, Fluid.NONE, container.getExecutor().getOriginal()));
               if (hitResult.m_6662_() != Type.MISS) {
                  Vec3 to = hitResult.m_82450_();
                  Vec3 from = container.getExecutor().getOriginal().m_20182_();
                  double distance = to.m_82554_(from);
                  if (distance > 6.0) {
                     container.getExecutor().playAnimationSynchronized(slamAnimation, 0.0F);
                     container.getDataManager().setDataSync((SkillDataKey<Float>)SkillDataKeys.FALL_DISTANCE.get(), (float)distance);
                     container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
                     event.setCanceled(true);
                  }
               }
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT,
         EVENT_UUID,
         event -> {
            if (event.getDamageSource().m_269533_(DamageTypeTags.f_268549_)
               && container.getDataManager().<Boolean>getDataValue((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get())) {
               float stamina = container.getExecutor().getStamina();
               float damage = event.getDamage();
               event.attachValueModifier(ValueModifier.adder(-stamina));
               container.getExecutor().setStamina(stamina - damage);
               container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.FALL_EVENT,
         EVENT_UUID,
         event -> {
            if (LevelUtil.calculateLivingEntityFallDamage(
                  event.getForgeEvent().getEntity(), event.getForgeEvent().getDamageMultiplier(), event.getForgeEvent().getDistance()
               )
               == 0) {
               container.getDataManager().setData((SkillDataKey<Boolean>)SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
            }
         }
      );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.FALL_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID);
   }

   @Override
   public Set<WeaponCategory> getAvailableWeaponCategories() {
      return this.slamMotions.keySet();
   }

   @Override
   public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
      consumptionList.add(
         Component.m_237115_("attribute.name.epicfight.stamina.consume.tooltip"),
         Component.m_237115_("skill.epicfight.meteor_slam.consume.tooltip"),
         SkillBookScreen.STAMINA_TEXTURE_INFO
      );
      return true;
   }

   public static class Builder extends SkillBuilder<MeteorSlamSkill> {
      protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> slamMotions = Maps.newHashMap();

      public MeteorSlamSkill.Builder addSlamMotion(
         WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function
      ) {
         this.slamMotions.put(weaponCategory, function);
         return this;
      }
   }
}
