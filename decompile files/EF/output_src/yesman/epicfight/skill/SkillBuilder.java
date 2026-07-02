package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class SkillBuilder<T extends Skill> {
   protected ResourceLocation registryName;
   protected CreativeModeTab tab;
   protected SkillCategory category;
   protected Skill.ActivateType activateType = Skill.ActivateType.ONE_SHOT;
   protected Skill.Resource resource = Skill.Resource.NONE;

   public <B extends SkillBuilder<T>> B setRegistryName(ResourceLocation registryName) {
      this.registryName = registryName;
      return (B)this;
   }

   public <B extends SkillBuilder<T>> B setCreativeTab(CreativeModeTab tab) {
      this.tab = tab;
      return (B)this;
   }

   public <B extends SkillBuilder<T>> B setCategory(SkillCategory category) {
      this.category = category;
      return (B)this;
   }

   public <B extends SkillBuilder<T>> B setActivateType(Skill.ActivateType activateType) {
      this.activateType = activateType;
      return (B)this;
   }

   public <B extends SkillBuilder<T>> B setResource(Skill.Resource resource) {
      this.resource = resource;
      return (B)this;
   }
}
