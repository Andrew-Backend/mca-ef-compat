package yesman.epicfight.server.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

public class AnimationArgument implements ArgumentType<AnimationManager.AnimationAccessor<? extends StaticAnimation>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("epicfight:biped_idle");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_SKILL = new DynamicCommandExceptionType(
      obj -> Component.m_237110_("epicfight.animationNotFound", new Object[]{obj})
   );

   public static AnimationArgument animation() {
      return new AnimationArgument();
   }

   public static AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimation(CommandContext<CommandSourceStack> commandContext, String name) {
      return (AnimationManager.AnimationAccessor<? extends StaticAnimation>)commandContext.getArgument(name, AnimationManager.AnimationAccessor.class);
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> parse(StringReader p_98428_) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.m_135818_(p_98428_);
      AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byKey(resourcelocation);
      return Optional.ofNullable(animation).orElseThrow(() -> ERROR_UNKNOWN_SKILL.create(resourcelocation));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
      return SharedSuggestionProvider.m_82957_(
         AnimationManager.getInstance().getAnimations(animation -> animation.registryName() != null).entrySet().stream().map(e -> e.getValue().registryName()),
         suggestionsBuilder
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
