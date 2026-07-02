package yesman.epicfight.world.entity.ai.attribute;

import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherGhostPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.CreeperPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.HoglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.IronGolemPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.PiglinBrutePatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.PiglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.RavagerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.VindicatorPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitherSkeletonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZoglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZombiePatch;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.WitherGhostClone;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD)
public class EpicFightAttributes {
   public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "epicfight");
   public static final RegistryObject<Attribute> MAX_STAMINA = ATTRIBUTES.register(
      "staminar", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.staminar"), 15.0, 0.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> STAMINA_REGEN = ATTRIBUTES.register(
      "stamina_regen", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.stamina_regen"), 1.0, 0.0, 30.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> STUN_ARMOR = ATTRIBUTES.register(
      "stun_armor", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.stun_armor"), 0.0, 0.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> WEIGHT = ATTRIBUTES.register(
      "weight", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.weight"), 0.0, 0.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> MAX_STRIKES = ATTRIBUTES.register(
      "max_strikes", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.max_strikes"), 1.0, 1.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> ARMOR_NEGATION = ATTRIBUTES.register(
      "armor_negation", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.armor_negation"), 0.0, 0.0, 100.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> IMPACT = ATTRIBUTES.register(
      "impact", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.impact"), 0.5, 0.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> EXECUTION_RESISTANCE = ATTRIBUTES.register(
      "execution_resistance", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.execution_resistance"), 0.0, 0.0, 10.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> OFFHAND_ATTACK_SPEED = ATTRIBUTES.register(
      "offhand_attack_speed", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.offhand_attack_speed"), 4.0, 0.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> OFFHAND_MAX_STRIKES = ATTRIBUTES.register(
      "offhand_max_strikes", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.offhand_max_strikes"), 1.0, 1.0, 1024.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> OFFHAND_ARMOR_NEGATION = ATTRIBUTES.register(
      "offhand_armor_negation", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.offhand_armor_negation"), 0.0, 0.0, 100.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> OFFHAND_IMPACT = ATTRIBUTES.register(
      "offhand_impact", () -> new RangedAttribute(EpicFightMod.format("attribute.name.%s.offhand_impact"), 0.5, 0.0, 1024.0).m_22084_(true)
   );
   public static final UUID ARMOR_NEGATION_MODIFIER = UUID.fromString("b0a7436e-5734-11eb-ae93-0242ac130002");
   public static final UUID MAX_STRIKE_MODIFIER = UUID.fromString("b0a745b2-5734-11eb-ae93-0242ac130002");
   public static final UUID IMPACT_MODIFIER = UUID.fromString("b0a746ac-5734-11eb-ae93-0242ac130002");
   public static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("1c224694-19f3-11ec-9621-0242ac130002");
   public static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("1c2249f0-19f3-11ec-9621-0242ac130002");

   @SubscribeEvent
   public static void entityAttributeCreationEvent(EntityAttributeCreationEvent event) {
      event.put((EntityType)EpicFightEntities.WITHER_SKELETON_MINION.get(), AbstractSkeleton.m_32166_().m_22265_());
      event.put((EntityType)EpicFightEntities.WITHER_GHOST_CLONE.get(), WitherGhostClone.createAttributes().m_22265_());
      event.put((EntityType)EpicFightEntities.DODGE_LOCATION_INDICATOR.get(), LivingEntity.m_21183_().m_22265_());
   }

   @SubscribeEvent
   public static void entityAttributeModificationEvent(EntityAttributeModificationEvent event) {
      common(EntityType.f_20554_, event);
      common(EntityType.f_20568_, event);
      common(EntityType.f_20460_, event);
      humanoid(EntityType.f_20513_, event);
      common(EntityType.f_20518_, event);
      common(EntityType.f_20479_, event);
      common(EntityType.f_20491_, event);
      humanoid(EntityType.f_20493_, event);
      humanoid(EntityType.f_20495_, event);
      common(EntityType.f_20456_, event);
      common(EntityType.f_20500_, event);
      common(EntityType.f_20565_, event);
      common(EntityType.f_20558_, event);
      humanoid(EntityType.f_20562_, event);
      common(EntityType.f_20566_, event);
      humanoid(EntityType.f_20458_, event);
      humanoid(EntityType.f_20511_, event);
      humanoid(EntityType.f_20512_, event);
      humanoid(EntityType.f_20524_, event);
      humanoid(EntityType.f_20481_, event);
      humanoid(EntityType.f_20497_, event);
      humanoid(EntityType.f_20501_, event);
      humanoid(EntityType.f_20530_, event);
      humanoid(EntityType.f_20531_, event);
      common((EntityType<? extends LivingEntity>)EpicFightEntities.WITHER_SKELETON_MINION.get(), event);
      player(EntityType.f_20532_, event);
      dragon(EntityType.f_20565_, event);
      common(EntityType.f_20496_, event);
      CreeperPatch.initAttributes(event);
      EnderDragonPatch.initAttributes(event);
      EndermanPatch.initAttributes(event);
      HoglinPatch.initAttributes(event);
      IronGolemPatch.initAttributes(event);
      PiglinBrutePatch.initAttributes(event);
      PiglinPatch.initAttributes(event);
      RavagerPatch.initAttributes(event);
      VindicatorPatch.initAttributes(event);
      WitherPatch.initAttributes(event);
      WitherGhostPatch.initAttributes(event);
      WitherSkeletonPatch.initAttributes(event);
      ZoglinPatch.initAttributes(event);
      ZombiePatch.initAttributes(event);
   }

   private static void common(EntityType<? extends LivingEntity> entityType, EntityAttributeModificationEvent event) {
      event.add(entityType, (Attribute)WEIGHT.get());
      event.add(entityType, (Attribute)ARMOR_NEGATION.get());
      event.add(entityType, (Attribute)IMPACT.get());
      event.add(entityType, (Attribute)MAX_STRIKES.get());
      event.add(entityType, (Attribute)STUN_ARMOR.get());
   }

   private static void humanoid(EntityType<? extends LivingEntity> entityType, EntityAttributeModificationEvent event) {
      common(entityType, event);
      event.add(entityType, (Attribute)OFFHAND_ATTACK_SPEED.get());
      event.add(entityType, (Attribute)OFFHAND_MAX_STRIKES.get());
      event.add(entityType, (Attribute)OFFHAND_ARMOR_NEGATION.get());
      event.add(entityType, (Attribute)OFFHAND_IMPACT.get());
   }

   private static void player(EntityType<? extends LivingEntity> entityType, EntityAttributeModificationEvent event) {
      humanoid(entityType, event);
      event.add(entityType, (Attribute)MAX_STAMINA.get());
      event.add(entityType, (Attribute)STAMINA_REGEN.get());
   }

   private static void dragon(EntityType<? extends EnderDragon> entityType, EntityAttributeModificationEvent event) {
      common(entityType, event);
      event.add(entityType, Attributes.f_22281_);
   }

   public static AttributeModifier getArmorNegationModifier(double value) {
      return new AttributeModifier(ARMOR_NEGATION_MODIFIER, EpicFightMod.prefix("weapon_modifier"), value, Operation.ADDITION);
   }

   public static AttributeModifier getMaxStrikesModifier(int value) {
      return new AttributeModifier(MAX_STRIKE_MODIFIER, EpicFightMod.prefix("weapon_modifier"), value, Operation.ADDITION);
   }

   public static AttributeModifier getImpactModifier(double value) {
      return new AttributeModifier(IMPACT_MODIFIER, EpicFightMod.prefix("weapon_modifier"), value, Operation.ADDITION);
   }

   public static AttributeModifier getDamageBonusModifier(double value) {
      return new AttributeModifier(ATTACK_DAMAGE_MODIFIER, EpicFightMod.prefix("weapon_modifier"), value, Operation.ADDITION);
   }

   public static AttributeModifier getSpeedBonusModifier(double value) {
      return new AttributeModifier(ATTACK_SPEED_MODIFIER, EpicFightMod.prefix("weapon_modifier"), value, Operation.ADDITION);
   }

   static {
      Attributes.f_22281_.m_22084_(true);
   }
}
