package yesman.epicfight.server.commands.arguments;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EpicFightCommandArgumentTypes {
   public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(
      ForgeRegistries.COMMAND_ARGUMENT_TYPES, "epicfight"
   );
   public static final RegistryObject<ArgumentTypeInfo<SkillArgument, ?>> SKILL = COMMAND_ARGUMENT_TYPES.register(
      "skill", () -> SingletonArgumentInfo.m_235451_(SkillArgument::skill)
   );
   public static final RegistryObject<ArgumentTypeInfo<AnimationArgument, ?>> ANIMATION = COMMAND_ARGUMENT_TYPES.register(
      "animation", () -> SingletonArgumentInfo.m_235451_(AnimationArgument::animation)
   );

   public static void registerArgumentTypes() {
      ArgumentTypeInfos.registerByClass(SkillArgument.class, (ArgumentTypeInfo)SKILL.get());
      ArgumentTypeInfos.registerByClass(AnimationArgument.class, (ArgumentTypeInfo)ANIMATION.get());
   }
}
