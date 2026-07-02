package fabric.net.mca.server;

import fabric.net.mca.Config;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.ai.relationship.EntityRelationship;
import fabric.net.mca.entity.ai.relationship.RelationshipState;
import fabric.net.mca.item.BabyItem;
import fabric.net.mca.network.s2c.OpenDestinyGuiRequest;
import fabric.net.mca.network.s2c.ShowToastRequest;
import fabric.net.mca.server.world.data.PlayerSaveData;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_5250;

public class ServerInteractionManager {
   private static final ServerInteractionManager INSTANCE = new ServerInteractionManager();
   private final Map<UUID, List<UUID>> proposals = new HashMap<>();
   private final Object2LongArrayMap<UUID> procreateMap = new Object2LongArrayMap();

   private ServerInteractionManager() {
   }

   public static ServerInteractionManager getInstance() {
      return INSTANCE;
   }

   public static void launchDestiny(class_3222 player) {
      NetworkHandler.sendToPlayer(new OpenDestinyGuiRequest(player), player);
   }

   public void tick() {
      List<UUID> removals = new ArrayList<>();
      this.procreateMap.keySet().stream().filter(k -> this.procreateMap.getLong(k) < System.currentTimeMillis()).forEach(removals::add);
      removals.forEach(this.procreateMap::removeLong);
   }

   public void onPlayerJoin(class_3222 player) {
      PlayerSaveData playerData = PlayerSaveData.get(player);
      if (!playerData.isEntityDataSet()) {
         if (Config.getInstance().launchIntoDestiny) {
            launchDestiny(player);
            player.method_6092(new class_1293(class_1294.field_5905, 3600));
            player.method_6092(new class_1293(class_1294.field_5914, 3600));
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

   private boolean hasProposalFrom(class_3222 sender, class_3222 receiver) {
      return this.getProposalsFor(receiver).contains(sender.method_5667());
   }

   private List<UUID> getProposalsFor(class_3222 player) {
      return this.proposals.getOrDefault(player.method_5667(), new ArrayList<>());
   }

   private void removeProposalFor(class_3222 target, class_3222 proposer) {
      List<UUID> list = this.getProposalsFor(target);
      list.remove(proposer.method_5667());
      this.proposals.put(target.method_5667(), list);
   }

   public void listProposals(class_3222 sender) {
      List<UUID> proposals = this.getProposalsFor(sender);
      if (proposals.size() == 0) {
         this.infoMessage(sender, class_2561.method_43471("server.noProposals"));
      } else {
         this.infoMessage(sender, class_2561.method_43471("server.proposals"));
      }

      proposals.forEach(uuid -> {
         class_1657 player = sender.method_37908().method_18470(uuid);
         if (player != null) {
            this.infoMessage(sender, class_2561.method_43470("- ").method_10852(class_2561.method_43470(player.method_5820())));
         }
      });
   }

   public void sendProposal(class_3222 sender, class_3222 receiver) {
      if (!Config.getInstance().allowPlayerMarriage) {
         this.failMessage(sender, class_2561.method_43471("notify.playerMarriage.disabled"));
      } else if (PlayerSaveData.get(sender).isMarried()) {
         this.failMessage(sender, class_2561.method_43471("server.alreadyMarried"));
      } else if (sender == receiver) {
         this.failMessage(sender, class_2561.method_43471("server.proposedToYourself"));
      } else {
         if (this.hasProposalFrom(sender, receiver)) {
            this.failMessage(sender, class_2561.method_43469("server.sentProposal", new Object[]{receiver.method_5820()}));
         } else {
            this.successMessage(sender, class_2561.method_43469("server.proposalSent", new Object[]{receiver.method_5820()}));
            this.infoMessage(receiver, class_2561.method_43469("server.proposedMarriage", new Object[]{sender.method_5820()}));
            List<UUID> list = this.getProposalsFor(receiver);
            list.add(sender.method_5667());
            this.proposals.put(receiver.method_5667(), list);
         }
      }
   }

   public void rejectProposal(class_3222 sender, class_3222 receiver) {
      if (!this.hasProposalFrom(receiver, sender)) {
         this.failMessage(sender, class_2561.method_43469("server.noProposal", new Object[]{receiver.method_5476()}));
      } else {
         this.successMessage(sender, class_2561.method_43471("server.proposalRejectionSent"));
         this.failMessage(receiver, class_2561.method_43469("server.proposalRejected", new Object[]{sender.method_5820()}));
         this.removeProposalFor(sender, receiver);
      }
   }

   public void acceptProposal(class_3222 sender, class_3222 receiver) {
      if (!this.hasProposalFrom(receiver, sender)) {
         this.failMessage(sender, class_2561.method_43469("server.noProposal", new Object[]{receiver.method_5476()}));
      } else {
         this.successMessage(receiver, class_2561.method_43469("server.proposalAccepted", new Object[]{sender.method_5476()}));
         PlayerSaveData.get(sender).marry(receiver);
         PlayerSaveData.get(receiver).marry(sender);
         this.successMessage(sender, class_2561.method_43469("server.married", new Object[]{receiver.method_5476()}));
         this.successMessage(receiver, class_2561.method_43469("server.married", new Object[]{sender.method_5476()}));
         this.removeProposalFor(sender, receiver);
      }
   }

   public void endMarriage(class_3222 sender) {
      EntityRelationship.of(sender)
         .ifPresent(
            senderData -> {
               if (!senderData.isMarried()) {
                  this.failMessage(sender, class_2561.method_43471("server.endMarriageNotMarried"));
               } else if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
                  this.failMessage(sender, class_2561.method_43471("server.marriedToVillager"));
               } else {
                  senderData.getPartnerName()
                     .ifPresent(name -> this.successMessage(sender, class_2561.method_43469("server.endMarriage", new Object[]{name.getString()})));
                  senderData.getPartner().ifPresent(spouse -> {
                     if (spouse instanceof class_1657 player) {
                        this.failMessage(player, class_2561.method_43469("server.marriageEnded", new Object[]{sender.method_5820()}));
                     }
                  });
                  senderData.endRelationShip(RelationshipState.SINGLE);
                  senderData.getPartnerUUID().map(id -> PlayerSaveData.get(sender)).ifPresent(r -> r.endRelationShip(RelationshipState.SINGLE));
               }
            }
         );
   }

   public void procreate(class_3222 sender) {
      PlayerSaveData senderData = PlayerSaveData.get(sender);
      if (!senderData.isMarried()) {
         this.failMessage(sender, class_2561.method_43471("server.notMarried"));
      } else if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
         this.failMessage(sender, class_2561.method_43471("server.marriedToVillager"));
      } else {
         senderData.getPartner().filter(e -> e instanceof class_1657).map(class_1657.class::cast).ifPresentOrElse(spouse -> {
            if (!this.procreateMap.containsKey(spouse.method_5667())) {
               this.procreateMap.put(sender.method_5667(), System.currentTimeMillis() + 10000L);
               this.infoMessage(spouse, class_2561.method_43469("server.procreationRequest", new Object[]{sender.method_5820()}));
            } else {
               this.successMessage(sender, class_2561.method_43471("server.procreationSuccessful"));
               this.successMessage(spouse, class_2561.method_43471("server.procreationSuccessful"));
               spouse.method_7270(BabyItem.createItem(spouse, sender, spouse.method_6051().method_43055()));
            }
         }, () -> this.failMessage(sender, class_2561.method_43471("server.spouseNotPresent")));
      }
   }

   private void successMessage(class_1657 player, class_5250 message) {
      player.method_43496(message.method_27692(class_124.field_1060));
   }

   private void failMessage(class_1657 player, class_5250 message) {
      player.method_43496(message.method_27692(class_124.field_1061));
   }

   private void infoMessage(class_1657 player, class_5250 message) {
      player.method_43496(message.method_27692(class_124.field_1054));
   }
}
