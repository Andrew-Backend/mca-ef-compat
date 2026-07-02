package forge.net.mca.entity.ai;

import forge.net.mca.ClientProxy;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntityWrapper;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.network.s2c.VillagerMessage;
import forge.net.mca.resources.API;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Messenger extends EntityWrapper {
   TargetingConditions CAN_RECEIVE = TargetingConditions.m_148353_();

   default boolean isSpeechImpaired() {
      return false;
   }

   default boolean isToYoungToSpeak() {
      return false;
   }

   default void playSpeechEffect() {
   }

   default DialogueType getDialogueType(Player receiver) {
      return DialogueType.UNASSIGNED;
   }

   default MutableComponent getTranslatable(Player target, String phraseId, Object... params) {
      String genderString = "";
      String targetName;
      if (target.m_9236_() instanceof ServerLevel world) {
         targetName = FamilyTree.get(world)
            .getOrEmpty(target.m_20148_())
            .map(FamilyTreeNode::getName)
            .filter(n -> !MCA.isBlankString(n))
            .orElse(target.m_7755_().getString());
         genderString = "#G" + PlayerSaveData.get((ServerPlayer)target).getGender().name().toLowerCase(Locale.ROOT) + ".";
      } else {
         targetName = target.m_7755_().getString();
      }

      Object[] newParams = new Object[params.length + 1];
      System.arraycopy(params, 0, newParams, 1, params.length);
      newParams[0] = targetName;
      String professionString = "";
      if (!this.asEntity().m_6162_() && this.asEntity() instanceof VillagerEntityMCA v) {
         professionString = "#P" + BuiltInRegistries.f_256735_.m_7981_(v.getProfession()).m_135815_() + ".";
      }

      String personalityString = "";
      if (this.asEntity() instanceof VillagerEntityMCA v) {
         personalityString = "#E" + v.getVillagerBrain().getPersonality().name() + ".";
      }

      return Component.m_237110_(genderString + personalityString + professionString + "#T" + this.getDialogueType(target).name() + "." + phraseId, newParams);
   }

   default void sendChatToAllAround(MutableComponent phrase) {
      for (Player player : this.asEntity().m_9236_().m_45955_(CAN_RECEIVE, this.asEntity(), this.asEntity().m_20191_().m_82400_(20.0))) {
         float dist = player.m_20270_(this.asEntity());
         this.sendChatMessage(phrase.m_130940_(dist < 10.0F ? ChatFormatting.WHITE : ChatFormatting.GRAY), player);
      }
   }

   default void sendChatToAllAround(String phrase, Object... params) {
      for (Player player : this.asEntity().m_9236_().m_45955_(CAN_RECEIVE, this.asEntity(), this.asEntity().m_20191_().m_82400_(20.0))) {
         float dist = player.m_20270_(this.asEntity());
         this.sendChatMessage(this.getTranslatable(player, phrase, params).m_130940_(dist < 10.0F ? ChatFormatting.WHITE : ChatFormatting.GRAY), player);
      }
   }

   default void sendChatMessage(Player target, String phraseId, Object... params) {
      this.sendChatMessage(this.getTranslatable(target, phraseId, params), target);
   }

   default MutableComponent transformMessage(MutableComponent message) {
      if (this.isSpeechImpaired()) {
         return Component.m_237113_(API.getRandomSentence("zombie", message.getString()));
      } else {
         return this.isToYoungToSpeak() ? Component.m_237113_(API.getRandomSentence("baby", message.getString())) : message;
      }
   }

   default MutableComponent sendChatMessage(MutableComponent message, Entity receiver) {
      message = this.transformMessage(message);
      MutableComponent prefix = Component.m_237113_(Config.getInstance().villagerChatPrefix).m_7220_(this.asEntity().m_5446_()).m_130946_(": ");
      VillagerMessage msg = new VillagerMessage(prefix, message, this.asEntity().m_20148_());
      if (receiver instanceof ServerPlayer serverPlayer) {
         NetworkHandler.sendToPlayer(msg, serverPlayer);
      } else {
         ClientProxy.getNetworkHandler().handleVillagerMessage(msg);
      }

      this.playSpeechEffect();
      return prefix.m_7220_(message);
   }

   default void sendEventMessage(Component message, Player receiver) {
      receiver.m_5661_(message, true);
   }

   default void sendEventMessage(Component message) {
      if (this instanceof Entity) {
         sendEventMessage(((Entity)this).m_9236_(), message);
      }
   }

   static void sendEventMessage(Level world, Component message) {
      world.m_6907_().forEach(player -> player.m_5661_(message, true));
   }
}
