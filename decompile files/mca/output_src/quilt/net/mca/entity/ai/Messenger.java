package quilt.net.mca.entity.ai;

import java.util.Locale;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4051;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.Config;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.EntityWrapper;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.network.s2c.VillagerMessage;
import quilt.net.mca.resources.API;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.server.world.data.PlayerSaveData;

public interface Messenger extends EntityWrapper {
   class_4051 CAN_RECEIVE = class_4051.method_36626();

   default boolean isSpeechImpaired() {
      return false;
   }

   default boolean isToYoungToSpeak() {
      return false;
   }

   default void playSpeechEffect() {
   }

   default DialogueType getDialogueType(class_1657 receiver) {
      return DialogueType.UNASSIGNED;
   }

   default class_5250 getTranslatable(class_1657 target, String phraseId, Object... params) {
      String genderString = "";
      String targetName;
      if (target.method_37908() instanceof class_3218 world) {
         targetName = FamilyTree.get(world)
            .getOrEmpty(target.method_5667())
            .map(FamilyTreeNode::getName)
            .filter(n -> !MCA.isBlankString(n))
            .orElse(target.method_5477().getString());
         genderString = "#G" + PlayerSaveData.get((class_3222)target).getGender().name().toLowerCase(Locale.ROOT) + ".";
      } else {
         targetName = target.method_5477().getString();
      }

      Object[] newParams = new Object[params.length + 1];
      System.arraycopy(params, 0, newParams, 1, params.length);
      newParams[0] = targetName;
      String professionString = "";
      if (!this.asEntity().method_6109() && this.asEntity() instanceof VillagerEntityMCA v) {
         professionString = "#P" + class_7923.field_41195.method_10221(v.getProfession()).method_12832() + ".";
      }

      String personalityString = "";
      if (this.asEntity() instanceof VillagerEntityMCA v) {
         personalityString = "#E" + v.getVillagerBrain().getPersonality().name() + ".";
      }

      return class_2561.method_43469(
         genderString + personalityString + professionString + "#T" + this.getDialogueType(target).name() + "." + phraseId, newParams
      );
   }

   default void sendChatToAllAround(class_5250 phrase) {
      for (class_1657 player : this.asEntity().method_37908().method_18464(CAN_RECEIVE, this.asEntity(), this.asEntity().method_5829().method_1014(20.0))) {
         float dist = player.method_5739(this.asEntity());
         this.sendChatMessage(phrase.method_27692(dist < 10.0F ? class_124.field_1068 : class_124.field_1080), player);
      }
   }

   default void sendChatToAllAround(String phrase, Object... params) {
      for (class_1657 player : this.asEntity().method_37908().method_18464(CAN_RECEIVE, this.asEntity(), this.asEntity().method_5829().method_1014(20.0))) {
         float dist = player.method_5739(this.asEntity());
         this.sendChatMessage(this.getTranslatable(player, phrase, params).method_27692(dist < 10.0F ? class_124.field_1068 : class_124.field_1080), player);
      }
   }

   default void sendChatMessage(class_1657 target, String phraseId, Object... params) {
      this.sendChatMessage(this.getTranslatable(target, phraseId, params), target);
   }

   default class_5250 transformMessage(class_5250 message) {
      if (this.isSpeechImpaired()) {
         return class_2561.method_43470(API.getRandomSentence("zombie", message.getString()));
      } else {
         return this.isToYoungToSpeak() ? class_2561.method_43470(API.getRandomSentence("baby", message.getString())) : message;
      }
   }

   default class_5250 sendChatMessage(class_5250 message, class_1297 receiver) {
      message = this.transformMessage(message);
      class_5250 prefix = class_2561.method_43470(Config.getInstance().villagerChatPrefix).method_10852(this.asEntity().method_5476()).method_27693(": ");
      VillagerMessage msg = new VillagerMessage(prefix, message, this.asEntity().method_5667());
      if (receiver instanceof class_3222 serverPlayer) {
         NetworkHandler.sendToPlayer(msg, serverPlayer);
      } else {
         ClientProxy.getNetworkHandler().handleVillagerMessage(msg);
      }

      this.playSpeechEffect();
      return prefix.method_10852(message);
   }

   default void sendEventMessage(class_2561 message, class_1657 receiver) {
      receiver.method_7353(message, true);
   }

   default void sendEventMessage(class_2561 message) {
      if (this instanceof class_1297) {
         sendEventMessage(((class_1297)this).method_37908(), message);
      }
   }

   static void sendEventMessage(class_1937 world, class_2561 message) {
      world.method_18456().forEach(player -> player.method_7353(message, true));
   }
}
