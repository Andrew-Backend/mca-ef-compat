package fabric.net.mca.server.world.data;

import fabric.net.mca.Config;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.relationship.EntityRelationship;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.entity.ai.relationship.RelationshipState;
import fabric.net.mca.entity.ai.relationship.RelationshipType;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.network.s2c.ShowToastRequest;
import fabric.net.mca.resources.API;
import fabric.net.mca.resources.Rank;
import fabric.net.mca.resources.Tasks;
import fabric.net.mca.util.NbtHelper;
import fabric.net.mca.util.WorldUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_18;
import net.minecraft.class_1935;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSaveData extends class_18 implements EntityRelationship {
   private final class_3218 world;
   private final UUID uuid;
   private Optional<Integer> lastSeenVillage = Optional.empty();
   private boolean entityDataSet;
   private class_2487 entityData;
   private final List<class_2487> inbox = new LinkedList<>();

   public static PlayerSaveData get(class_3222 player) {
      return get((class_3218)player.method_37908(), player.method_5667());
   }

   public static PlayerSaveData get(class_3218 world, UUID uuid) {
      return WorldUtils.loadData(
         world.method_8503().method_30002(), nbt -> new PlayerSaveData(world, uuid, nbt), w -> new PlayerSaveData(world, uuid), "mca_player_" + uuid
      );
   }

   public static Optional<PlayerSaveData> getIfPresent(class_3218 world, UUID uuid) {
      return Optional.ofNullable((PlayerSaveData)world.method_17983().method_20786(nbt -> new PlayerSaveData(world, uuid, nbt), "mca_player_" + uuid));
   }

   PlayerSaveData(class_3218 world, UUID uuid) {
      this.world = world;
      this.uuid = uuid;
      this.resetEntityData();
   }

   PlayerSaveData(class_3218 world, UUID uuid, class_2487 nbt) {
      this.world = world;
      this.uuid = uuid;
      this.lastSeenVillage = nbt.method_10573("lastSeenVillage", 3) ? Optional.of(nbt.method_10550("lastSeenVillage")) : Optional.empty();
      this.entityDataSet = nbt.method_10545("entityDataSet") && nbt.method_10577("entityDataSet");
      if (nbt.method_10545("entityData")) {
         this.entityData = nbt.method_10562("entityData");
      } else {
         this.resetEntityData();
      }

      class_2499 inbox = nbt.method_10554("inbox", 10);
      if (inbox != null) {
         this.inbox.clear();

         for (int i = 0; i < inbox.size(); i++) {
            this.inbox.add(inbox.method_10602(i));
         }
      }
   }

   private void resetEntityData() {
      this.entityData = new class_2487();
      VillagerEntityMCA villager = (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(this.world);
      assert villager != null;
      villager.initializeSkin(true);
      villager.getGenetics().randomize();
      villager.getTraits().randomize();
      villager.getVillagerBrain().randomize();
      villager.method_5652(this.entityData);
   }

   public boolean isEntityDataSet() {
      return this.entityDataSet;
   }

   public class_2487 getEntityData() {
      return this.entityData;
   }

   public void setEntityDataSet(boolean entityDataSet) {
      this.entityDataSet = entityDataSet;
   }

   public void setEntityData(class_2487 entityData) {
      this.entityData = entityData;
   }

   @Override
   public void onTragedy(class_1282 cause, @Nullable class_2338 burialSite, RelationshipType type, class_1297 victim) {
      EntityRelationship.super.onTragedy(cause, burialSite, type, victim);
      if (victim instanceof VillagerEntityMCA victimVillager) {
         this.sendLetterOfCondolence(
            victimVillager.method_5477().getString(),
            victimVillager.getResidency().getHomeVillage().map(Village::getName).orElse(API.getVillagePool().pickVillageName("village"))
         );
      }
   }

   public void updateLastSeenVillage(VillageManager manager, class_3222 self) {
      Optional<Village> prevVillage = this.getLastSeenVillage(manager);
      Optional<Village> nextVillage = prevVillage.filter(v -> v.isWithinBorder(self)).or(() -> manager.findNearestVillage(self));
      this.setLastSeenVillage(self, prevVillage.orElse(null), nextVillage.orElse(null));
      if (nextVillage.isPresent()) {
         Rank rank = Tasks.getRank(nextVillage.get(), self);
         CriterionMCA.RANK.trigger(self, rank);
      }
   }

   public void setLastSeenVillage(class_3222 self, Village oldVillage, @Nullable Village newVillage) {
      this.lastSeenVillage = Optional.ofNullable(newVillage).map(Village::getId);
      this.method_80();
      if (oldVillage != newVillage) {
         if (oldVillage != null) {
            this.onLeave(self, oldVillage);
         }

         if (newVillage != null) {
            this.onEnter(self, newVillage);
         }
      }
   }

   public Optional<Village> getLastSeenVillage(VillageManager manager) {
      return this.lastSeenVillage.flatMap(manager::getOrEmpty);
   }

   public Optional<Integer> getLastSeenVillageId() {
      return this.lastSeenVillage;
   }

   protected void onLeave(class_1657 self, Village village) {
      if (Config.getInstance().enterVillageNotification && village.isVillage()) {
         self.method_7353(class_2561.method_43469("gui.village.left", new Object[]{village.getName()}).method_27692(class_124.field_1065), true);
      }
   }

   protected void onEnter(class_1657 self, Village village) {
      if (Config.getInstance().enterVillageNotification && village.isVillage()) {
         self.method_7353(class_2561.method_43469("gui.village.welcome", new Object[]{village.getName()}).method_27692(class_124.field_1065), true);
      }

      village.onEnter(this.world);
   }

   @Override
   public void marry(class_1297 spouse) {
      EntityRelationship.super.marry(spouse);
      this.method_80();
   }

   @Override
   public void endRelationShip(RelationshipState newState) {
      EntityRelationship.super.endRelationShip(newState);
      this.method_80();
   }

   @Override
   public class_3218 getWorld() {
      return this.world;
   }

   @Override
   public UUID getUUID() {
      return this.uuid;
   }

   @Override
   public Gender getGender() {
      return Gender.byId(this.getEntityData().method_10550("gender"));
   }

   @NotNull
   @Override
   public FamilyTreeNode getFamilyEntry() {
      return this.getFamilyTree().getOrEmpty(this.uuid).orElseGet(() -> {
         String name = Optional.ofNullable(this.world.method_18470(this.uuid)).map(p -> p.method_5477().getString()).orElse("Unnamed Adventurer");
         return this.getFamilyTree().getOrCreate(this.uuid, name, this.getGender(), true);
      });
   }

   public void reset() {
      this.endRelationShip(RelationshipState.SINGLE);
      this.method_80();
   }

   public class_2487 method_75(class_2487 nbt) {
      this.lastSeenVillage.ifPresent(id -> nbt.method_10569("lastSeenVillage", id));
      nbt.method_10566("entityData", this.entityData);
      nbt.method_10556("entityDataSet", this.entityDataSet);
      nbt.method_10566("inbox", NbtHelper.fromList(this.inbox, v -> v));
      return nbt;
   }

   public void sendMail(class_2487 nbt) {
      if (Config.getInstance().enableVillagerMailingPlayers) {
         this.inbox.add(nbt);
      }

      this.method_80();
   }

   public boolean hasMail() {
      return !this.inbox.isEmpty();
   }

   public class_1799 getMail() {
      if (this.hasMail()) {
         class_2487 nbt = this.inbox.remove(0);
         class_1799 stack = new class_1799((class_1935)ItemsMCA.LETTER.get(), 1);
         stack.method_7980(nbt);
         return stack;
      } else {
         return null;
      }
   }

   public void sendLetterOfCondolence(String name, String village) {
      this.sendLetter(
         List.of("{ \"translate\": \"mca.letter.condolence\", \"with\": [\"" + this.getFamilyEntry().getName() + "\", \"" + name + "\", \"" + village + "\"] }")
      );
   }

   public void sendLetter(List<String> lines) {
      class_2499 l = new class_2499();

      for (String line : lines) {
         l.method_10531(0, class_2519.method_23256(line));
      }

      class_2487 nbt = new class_2487();
      nbt.method_10566("pages", l);
      this.sendMail(nbt);
      Optional.ofNullable(this.world.method_18470(this.uuid)).ifPresent(p -> showMailNotification((class_3222)p));
   }

   public static void showMailNotification(class_3222 player) {
      NetworkHandler.sendToPlayer(new ShowToastRequest("server.mail.title", "server.mail.description"), player);
   }
}
