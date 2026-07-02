package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.kubejs.skill.CustomChargeableSkill;
import yesman.epicfight.compat.kubejs.skill.CustomPassiveSkill;
import yesman.epicfight.compat.kubejs.skill.CustomSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class EpicFightKubeJSPlugin extends KubeJSPlugin {
   public static final RegistryInfo<Skill> SKILL_REGISTRY = RegistryInfo.of(SkillManager.SKILL_REGISTRY_KEY, Skill.class);

   public void init() {
      SKILL_REGISTRY.addType("basic", CustomSkill.CustomSkillBuilder.class, CustomSkill.CustomSkillBuilder::new);
      SKILL_REGISTRY.addType("passive", CustomPassiveSkill.CustomPassiveSkillBuilder.class, CustomPassiveSkill.CustomPassiveSkillBuilder::new);
      SKILL_REGISTRY.addType("chargeable", CustomChargeableSkill.CustomChargeableSkillBuilder.class, CustomChargeableSkill.CustomChargeableSkillBuilder::new);
   }

   public void registerBindings(BindingsEvent event) {
      event.add("EpicFightCapabilities", EpicFightCapabilities.class);
      event.add("ServerPlayerPatch", ServerPlayerPatch.class);
      event.add("PlayerPatch", PlayerPatch.class);
      event.add("EventType", PlayerEventListener.EventType.class);
      event.add("SkillSlots", SkillSlots.class);
      event.add("SkillDataKeys", SkillDataKeys.class);
      event.add("EFUtils", EFUtilsJS.class);
      if (event.getType() == ScriptType.CLIENT && FMLEnvironment.dist.isClient()) {
         event.add("ClientEngine", ClientEngine.getInstance());
         event.add("ControlEngine", ClientEngine.getInstance().controlEngine);
         event.add("LocalPlayerPatch", LocalPlayerPatch.class);
      }
   }

   public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
      typeWrappers.registerSimple(
         Skill.class,
         o -> {
            if (o instanceof Skill skill) {
               return skill;
            } else if (o instanceof String) {
               return (Skill)SkillManager.getSkillRegistry().getValue(ResourceLocation.parse((String)o));
            } else if (o instanceof ResourceLocation) {
               return (Skill)SkillManager.getSkillRegistry().getValue((ResourceLocation)o);
            } else if (o instanceof RegistryObject reg) {
               return (Skill)reg.get();
            } else if (o instanceof BuilderBase builder) {
               return (Skill)SkillManager.getSkillRegistry().getValue(builder.id);
            } else {
               throw new IllegalArgumentException(
                  "Object " + o + " of class " + o.getClass().getName() + " cannot be converted to type yesman.epicfight.skill.Skill"
               );
            }
         }
      );
   }
}
