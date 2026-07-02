package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class WeaponInnateSkill extends Skill {
   protected List<Map<AnimationProperty.AttackPhaseProperty<?>, Object>> properties = Lists.newArrayList();
   private static final Vec2f[] CLOCK_POS = new Vec2f[]{
      new Vec2f(0.5F, 0.5F), new Vec2f(0.5F, 0.0F), new Vec2f(0.0F, 0.0F), new Vec2f(0.0F, 1.0F), new Vec2f(1.0F, 1.0F), new Vec2f(1.0F, 0.0F)
   };

   public static SkillBuilder<WeaponInnateSkill> createWeaponInnateBuilder() {
      return new SkillBuilder().setCategory(SkillCategories.WEAPON_INNATE).setResource(Skill.Resource.WEAPON_CHARGE);
   }

   public WeaponInnateSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
      super(builder);
   }

   @Override
   public boolean canExecute(SkillContainer container) {
      ItemStack itemstack = container.getExecutor().getOriginal().m_21205_();
      return super.canExecute(container)
         && EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this
         && container.getExecutor().getOriginal().m_20202_() == null
         && (!this.isActivated(container) || this.activateType == Skill.ActivateType.TOGGLE);
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = Lists.newArrayList();
      String traslatableText = this.getTranslationKey();
      list.add(
         Component.m_237115_(traslatableText)
            .m_130940_(ChatFormatting.WHITE)
            .m_7220_(Component.m_237113_(String.format("[%.0f]", this.consumption)).m_130940_(ChatFormatting.AQUA))
      );
      list.add(Component.m_237115_(traslatableText + ".tooltip").m_130940_(ChatFormatting.DARK_GRAY));
      return list;
   }

   protected void generateTooltipforPhase(
      List<Component> list,
      ItemStack itemstack,
      CapabilityItem itemcap,
      PlayerPatch<?> playerpatch,
      Map<AnimationProperty.AttackPhaseProperty<?>, Object> propertyMap,
      String title
   ) {
      double weaponBaseDamage = playerpatch.getWeaponAttribute(Attributes.f_22281_, itemstack);
      double armorNegation = playerpatch.getWeaponAttribute((Attribute)EpicFightAttributes.ARMOR_NEGATION.get(), itemstack);
      double impact = playerpatch.getWeaponAttribute((Attribute)EpicFightAttributes.IMPACT.get(), itemstack);
      double maxStrikes = playerpatch.getWeaponAttribute((Attribute)EpicFightAttributes.MAX_STRIKES.get(), itemstack);
      ValueModifier.ResultCalculator damageModifier = ValueModifier.calculator();
      ValueModifier.ResultCalculator armorNegationModifier = ValueModifier.calculator();
      ValueModifier.ResultCalculator impactModifier = ValueModifier.calculator();
      ValueModifier.ResultCalculator maxStrikesModifier = ValueModifier.calculator();
      this.getProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, propertyMap).ifPresent(damageModifier::attach);
      this.getProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, propertyMap).ifPresent(armorNegationModifier::attach);
      this.getProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, propertyMap).ifPresent(impactModifier::attach);
      this.getProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, propertyMap).ifPresent(maxStrikesModifier::attach);
      impactModifier.multiply(1.0F + itemstack.getEnchantmentLevel(Enchantments.f_44980_) * 0.12F);
      double fBaseDamage = weaponBaseDamage;
      weaponBaseDamage = damageModifier.getResult(playerpatch.getModifiedBaseDamage((float)weaponBaseDamage));
      armorNegation = armorNegationModifier.getResult((float)armorNegation);
      impact = impactModifier.getResult((float)impact);
      maxStrikes = maxStrikesModifier.getResult((float)maxStrikes);
      list.add(Component.m_237113_(title).m_130940_(ChatFormatting.UNDERLINE).m_130940_(ChatFormatting.GRAY));
      MutableComponent damageComponent = Component.m_237110_(
            "damage_source.epicfight.damage", new Object[]{Component.m_237113_(ItemStack.f_41584_.format(weaponBaseDamage)).m_130940_(ChatFormatting.RED)}
         )
         .m_130940_(ChatFormatting.DARK_GRAY);
      this.getProperty(AnimationProperty.AttackPhaseProperty.EXTRA_DAMAGE, propertyMap)
         .ifPresent(extraDamageSet -> extraDamageSet.forEach(extraDamage -> extraDamage.setTooltips(itemstack, damageComponent, fBaseDamage)));
      list.add(damageComponent);
      if (armorNegation != 0.0) {
         list.add(
            Component.m_237113_(ItemStack.f_41584_.format(armorNegation) + "% ")
               .m_130940_(ChatFormatting.GOLD)
               .m_7220_(Component.m_237115_(((Attribute)EpicFightAttributes.ARMOR_NEGATION.get()).m_22087_()).m_130940_(ChatFormatting.DARK_GRAY))
         );
      }

      if (impact != 0.0) {
         list.add(
            Component.m_237110_(
                  ((Attribute)EpicFightAttributes.IMPACT.get()).m_22087_() + ".value",
                  new Object[]{Component.m_237113_(ItemStack.f_41584_.format(impact)).m_130940_(ChatFormatting.AQUA)}
               )
               .m_130940_(ChatFormatting.DARK_GRAY)
         );
      }

      list.add(
         Component.m_237110_(
               ((Attribute)EpicFightAttributes.MAX_STRIKES.get()).m_22087_() + ".value",
               new Object[]{Component.m_237113_(ItemStack.f_41584_.format(maxStrikes)).m_130940_(ChatFormatting.WHITE)}
            )
            .m_130940_(ChatFormatting.DARK_GRAY)
      );
      Optional<StunType> stunOption = this.getProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, propertyMap);
      stunOption.ifPresent(stunType -> list.add(Component.m_237115_(stunType.toString()).m_130940_(ChatFormatting.DARK_GRAY)));
      if (!stunOption.isPresent()) {
         list.add(Component.m_237115_(StunType.SHORT.toString()).m_130940_(ChatFormatting.DARK_GRAY));
      }
   }

   protected <V> Optional<V> getProperty(AnimationProperty.AttackPhaseProperty<V> propertyKey, Map<AnimationProperty.AttackPhaseProperty<?>, Object> map) {
      return Optional.ofNullable((V)map.get(propertyKey));
   }

   public WeaponInnateSkill newProperty() {
      this.properties.add(Maps.newHashMap());
      return this;
   }

   public <T> WeaponInnateSkill addProperty(AnimationProperty.AttackPhaseProperty<T> propertyKey, T object) {
      this.properties.get(this.properties.size() - 1).put(propertyKey, object);
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      boolean creative = container.getExecutor().getOriginal().m_7500_();
      boolean fullstack = creative || container.isFull();
      boolean canUse = !container.isDisabled() && container.getSkill().checkExecuteCondition(container);
      float cooldownRatio = !fullstack && !container.isActivated() ? container.getResource(partialTick) : 1.0F;
      int vertexNum = 0;
      float iconSize = 32.0F;
      float bottom = y + iconSize;
      float right = x + iconSize;
      float middle = x + iconSize * 0.5F;
      float lastVertexX = 0.0F;
      float lastVertexY = 0.0F;
      float lastTexX = 0.0F;
      float lastTexY = 0.0F;
      byte var24;
      if (cooldownRatio < 0.125F) {
         var24 = 6;
         lastTexX = cooldownRatio / 0.25F;
         lastTexY = 0.0F;
         lastVertexX = middle + iconSize * lastTexX;
         lastVertexY = y;
         lastTexX += 0.5F;
      } else if (cooldownRatio < 0.375F) {
         var24 = 5;
         lastTexX = 1.0F;
         lastTexY = (cooldownRatio - 0.125F) / 0.25F;
         lastVertexX = right;
         lastVertexY = y + iconSize * lastTexY;
      } else if (cooldownRatio < 0.625F) {
         var24 = 4;
         lastTexX = (cooldownRatio - 0.375F) / 0.25F;
         lastTexY = 1.0F;
         lastVertexX = right - iconSize * lastTexX;
         lastVertexY = bottom;
         lastTexX = 1.0F - lastTexX;
      } else if (cooldownRatio < 0.875F) {
         var24 = 3;
         lastTexX = 0.0F;
         lastTexY = (cooldownRatio - 0.625F) / 0.25F;
         lastVertexX = x;
         lastVertexY = bottom - iconSize * lastTexY;
         lastTexY = 1.0F - lastTexY;
      } else {
         var24 = 2;
         lastTexX = (cooldownRatio - 0.875F) / 0.25F;
         lastTexY = 0.0F;
         lastVertexX = x + iconSize * lastTexX;
         lastVertexY = y;
      }

      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, container.getSkill().getSkillTexture());
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      if (canUse) {
         if (container.getStack() > 0) {
            RenderSystem.setShaderColor(0.0F, 0.64F, 0.72F, 0.8F);
         } else {
            RenderSystem.setShaderColor(0.0F, 0.5F, 0.5F, 0.6F);
         }
      } else {
         RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
      }

      Tesselator tessellator = Tesselator.m_85913_();
      BufferBuilder bufferbuilder = tessellator.m_85915_();
      bufferbuilder.m_166779_(Mode.TRIANGLE_FAN, DefaultVertexFormat.f_85817_);

      for (int j = 0; j < var24; j++) {
         bufferbuilder.m_252986_(guiGraphics.m_280168_().m_85850_().m_252922_(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F)
            .m_7421_(CLOCK_POS[j].x, CLOCK_POS[j].y)
            .m_5752_();
      }

      bufferbuilder.m_252986_(guiGraphics.m_280168_().m_85850_().m_252922_(), lastVertexX, lastVertexY, 0.0F).m_7421_(lastTexX, lastTexY).m_5752_();
      tessellator.m_85914_();
      if (canUse) {
         RenderSystem.setShaderColor(0.08F, 0.79F, 0.95F, 1.0F);
      } else {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      GL11.glCullFace(1028);
      bufferbuilder.m_166779_(Mode.TRIANGLE_FAN, DefaultVertexFormat.f_85817_);

      for (int j = 0; j < 2; j++) {
         bufferbuilder.m_252986_(guiGraphics.m_280168_().m_85850_().m_252922_(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F)
            .m_7421_(CLOCK_POS[j].x, CLOCK_POS[j].y)
            .m_5752_();
      }

      for (int j = CLOCK_POS.length - 1; j >= var24; j--) {
         bufferbuilder.m_252986_(guiGraphics.m_280168_().m_85850_().m_252922_(), x + iconSize * CLOCK_POS[j].x, y + iconSize * CLOCK_POS[j].y, 0.0F)
            .m_7421_(CLOCK_POS[j].x, CLOCK_POS[j].y)
            .m_5752_();
      }

      bufferbuilder.m_252986_(guiGraphics.m_280168_().m_85850_().m_252922_(), lastVertexX, lastVertexY, 0.0F).m_7421_(lastTexX, lastTexY).m_5752_();
      tessellator.m_85914_();
      GL11.glCullFace(1029);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (!container.isActivated()
         || container.getSkill().getActivateType() != Skill.ActivateType.DURATION
            && container.getSkill().getActivateType() != Skill.ActivateType.DURATION_INFINITE) {
         if (!fullstack) {
            String s = String.valueOf((int)(cooldownRatio * 100.0F));
            int stringWidth = (gui.getFont().m_92895_(s) - 6) / 3;
            guiGraphics.drawString(gui.getFont(), s, x + 13.0F - stringWidth, y + 13.0F, 16777215, true);
         }
      } else {
         String s = String.format("%.0f", container.getRemainDuration() / 20.0F);
         int stringWidth = (gui.getFont().m_92895_(s) - 6) / 3;
         guiGraphics.drawString(gui.getFont(), s, x + 13.0F - stringWidth, y + 13.0F, 16777215, true);
      }

      if (container.getSkill().getMaxStack() > 1) {
         String s = String.valueOf(container.getStack());
         int stringWidth = (gui.getFont().m_92895_(s) - 6) / 3;
         guiGraphics.drawString(gui.getFont(), s, x + 25.0F - stringWidth, y + 22.0F, 16777215, true);
      }

      guiGraphics.m_280168_().m_85849_();
   }
}
