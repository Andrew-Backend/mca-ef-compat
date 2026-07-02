package forge.net.mca.server;

import forge.net.mca.Config;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.item.BabyItem;
import forge.net.mca.network.s2c.OpenDestinyGuiRequest;
import forge.net.mca.network.s2c.ShowToastRequest;
import forge.net.mca.server.world.data.PlayerSaveData;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ServerInteractionManager {
   private static final ServerInteractionManager INSTANCE = new ServerInteractionManager();
   private final Map<UUID, List<UUID>> proposals = new HashMap<>();
   private final Object2LongArrayMap<UUID> procreateMap = new Object2LongArrayMap();

   private ServerInteractionManager() {
   }

   public static ServerInteractionManager getInstance() {
      return INSTANCE;
   }

   public static void launchDestiny(ServerPlayer player) {
      NetworkHandler.sendToPlayer(new OpenDestinyGuiRequest(player), player);
   }

   public void tick() {
      List<UUID> removals = new ArrayList<>();
      this.procreateMap.keySet().stream().filter(k -> this.procreateMap.getLong(k) < System.currentTimeMillis()).forEach(removals::add);
      removals.forEach(this.procreateMap::removeLong);
   }

   public void onPlayerJoin(ServerPlayer player) {
      PlayerSaveData playerData = PlayerSaveData.get(player);
      if (!playerData.isEntityDataSet()) {
         if (Config.getInstance().launchIntoDestiny) {
            launchDestiny(player);
            player.m_7292_(new MobEffectInstance(MobEffects.f_19609_, 3600));
            player.m_7292_(new MobEffectInstance(MobEffects.f_19616_, 3600));
         } else if (Config.getInstance().allowDestinyCommandOnce) {
            NetworkHandler.sendToPlayer(new ShowToastRequest("server.destinyNotSet.title", "server.destinyNotSet.description"), player);
         } else if (Config.getInstance().allowFullPlayerEditor) {
            NetworkHandler.sendToPlayer(new ShowToastRequest("server.playerNotCustomized.title", "server.playerNotCustomized.description"), player);
         }
      }

      if (playerData.hasMail()) {
         PlayerSaveData.showMailNotification(player);
      }
   }

   private boolean hasProposalFrom(ServerPlayer sender, ServerPlayer receiver) {
      return this.getProposalsFor(receiver).contains(sender.m_20148_());
   }

   private List<UUID> getProposalsFor(ServerPlayer player) {
      return this.proposals.getOrDefault(player.m_20148_(), new ArrayList<>());
   }

   private void removeProposalFor(ServerPlayer target, ServerPlayer proposer) {
      List<UUID> list = this.getProposalsFor(target);
      list.remove(proposer.m_20148_());
      this.proposals.put(target.m_20148_(), list);
   }

   public void listProposals(ServerPlayer sender) {
      List<UUID> proposals = this.getProposalsFor(sender);
      if (proposals.size() == 0) {
         this.infoMessage(sender, Component.m_237115_("server.noProposals"));
      } else {
         this.infoMessage(sender, Component.m_237115_("server.proposals"));
      }

      proposals.forEach(uuid -> {
         Player player = sender.m_9236_().m_46003_(uuid);
         if (player != null) {
            this.infoMessage(sender, Component.m_237113_("- ").m_7220_(Component.m_237113_(player.m_6302_())));
         }
      });
   }

   public void sendProposal(ServerPlayer sender, ServerPlayer receiver) {
      if (!Config.getInstance().allowPlayerMarriage) {
         this.failMessage(sender, Component.m_237115_("notify.playerMarriage.disabled"));
      } else if (PlayerSaveData.get(sender).isMarried()) {
         this.failMessage(sender, Component.m_237115_("server.alreadyMarried"));
      } else if (sender == receiver) {
         this.failMessage(sender, Component.m_237115_("server.proposedToYourself"));
      } else {
         if (this.hasProposalFrom(sender, receiver)) {
            this.failMessage(sender, Component.m_237110_("server.sentProposal", new Object[]{receiver.m_6302_()}));
         } else {
            this.successMessage(sender, Component.m_237110_("server.proposalSent", new Object[]{receiver.m_6302_()}));
            this.infoMessage(receiver, Component.m_237110_("server.proposedMarriage", new Object[]{sender.m_6302_()}));
            List<UUID> list = this.getProposalsFor(receiver);
            list.add(sender.m_20148_());
            this.proposals.put(receiver.m_20148_(), list);
         }
      }
   }

   public void rejectProposal(ServerPlayer sender, ServerPlayer receiver) {
      if (!this.hasProposalFrom(receiver, sender)) {
         this.failMessage(sender, Component.m_237110_("server.noProposal", new Object[]{receiver.m_5446_()}));
      } else {
         this.successMessage(sender, Component.m_237115_("server.proposalRejectionSent"));
         this.failMessage(receiver, Component.m_237110_("server.proposalRejected", new Object[]{sender.m_6302_()}));
         this.removeProposalFor(sender, receiver);
      }
   }

   public void acceptProposal(ServerPlayer sender, ServerPlayer receiver) {
      if (!this.hasProposalFrom(receiver, sender)) {
         this.failMessage(sender, Component.m_237110_("server.noProposal", new Object[]{receiver.m_5446_()}));
      } else {
         this.successMessage(receiver, Component.m_237110_("server.proposalAccepted", new Object[]{sender.m_5446_()}));
         PlayerSaveData.get(sender).marry(receiver);
         PlayerSaveData.get(receiver).marry(sender);
         this.successMessage(sender, Component.m_237110_("server.married", new Object[]{receiver.m_5446_()}));
         this.successMessage(receiver, Component.m_237110_("server.married", new Object[]{sender.m_5446_()}));
         this.removeProposalFor(sender, receiver);
      }
   }

   public void endMarriage(ServerPlayer sender) {
      EntityRelationship.of(sender)
         .ifPresent(
            senderData -> {
               if (!senderData.isMarried()) {
                  this.failMessage(sender, Component.m_237115_("server.endMarriageNotMarried"));
               } else if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
                  this.failMessage(sender, Component.m_237115_("server.marriedToVillager"));
               } else {
                  senderData.getPartnerName()
                     .ifPresent(name -> this.successMessage(sender, Component.m_237110_("server.endMarriage", new Object[]{name.getString()})));
                  senderData.getPartner().ifPresent(spouse -> {
                     if (spouse instanceof Player player) {
                        this.failMessage(player, Component.m_237110_("server.marriageEnded", new Object[]{sender.m_6302_()}));
                     }
                  });
                  senderData.endRelationShip(RelationshipState.SINGLE);
                  senderData.getPartnerUUID().map(id -> PlayerSaveData.get(sender)).ifPresent(r -> r.endRelationShip(RelationshipState.SINGLE));
               }
            }
         );
   }

   public void procreate(ServerPlayer sender) {
      PlayerSaveData senderData = PlayerSaveData.get(sender);
      if (!senderData.isMarried()) {
         this.failMessage(sender, Component.m_237115_("server.notMarried"));
      } else if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
         this.failMessage(sender, Component.m_237115_("server.marriedToVillager"));
      } else {
         senderData.getPartner().filter(e -> e instanceof Player).map(Player.class::cast).ifPresentOrElse(spouse -> {
            if (!this.procreateMap.containsKey(spouse.m_20148_())) {
               this.procreateMap.put(sender.m_20148_(), System.currentTimeMillis() + 10000L);
               this.infoMessage(spouse, Component.m_237110_("server.procreationRequest", new Object[]{sender.m_6302_()}));
            } else {
               this.successMessage(sender, Component.m_237115_("server.procreationSuccessful"));
               this.successMessage(spouse, Component.m_237115_("server.procreationSuccessful"));
               spouse.m_36356_(BabyItem.createItem(spouse, sender, spouse.m_217043_().m_188505_()));
            }
         }, () -> this.failMessage(sender, Component.m_237115_("server.spouseNotPresent")));
      }
   }

   private void successMessage(Player player, MutableComponent message) {
      player.m_213846_(message.m_130940_(ChatFormatting.GREEN));
   }

   private void failMessage(Player player, MutableComponent message) {
      player.m_213846_(message.m_130940_(ChatFormatting.RED));
   }

   private void infoMessage(Player player, MutableComponent message) {
      player.m_213846_(message.m_130940_(ChatFormatting.YELLOW));
   }
}
