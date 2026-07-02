package forge.net.mca.server.world.data;

import forge.net.mca.Config;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.entity.ai.relationship.RelationshipType;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.network.s2c.ShowToastRequest;
import forge.net.mca.resources.API;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSaveData extends SavedData implements EntityRelationship {
   private final ServerLevel world;
   private final UUID uuid;
   private Optional<Integer> lastSeenVillage = Optional.empty();
   private boolean entityDataSet;
   private CompoundTag entityData;
   private final List<CompoundTag> inbox = new LinkedList<>();

   public static PlayerSaveData get(ServerPlayer player) {
      return get((ServerLevel)player.m_9236_(), player.m_20148_());
   }

   public static PlayerSaveData get(ServerLevel world, UUID uuid) {
      return WorldUtils.loadData(
         world.m_7654_().m_129783_(), nbt -> new PlayerSaveData(world, uuid, nbt), w -> new PlayerSaveData(world, uuid), "mca_player_" + uuid
      );
   }

   public static Optional<PlayerSaveData> getIfPresent(ServerLevel world, UUID uuid) {
      return Optional.ofNullable((PlayerSaveData)world.m_8895_().m_164858_(nbt -> new PlayerSaveData(world, uuid, nbt), "mca_player_" + uuid));
   }

   PlayerSaveData(ServerLevel world, UUID uuid) {
      this.world = world;
      this.uuid = uuid;
      this.resetEntityData();
   }

   PlayerSaveData(ServerLevel world, UUID uuid, CompoundTag nbt) {
      this.world = world;
      this.uuid = uuid;
      this.lastSeenVillage = nbt.m_128425_("lastSeenVillage", 3) ? Optional.of(nbt.m_128451_("lastSeenVillage")) : Optional.empty();
      this.entityDataSet = nbt.m_128441_("entityDataSet") && nbt.m_128471_("entityDataSet");
      if (nbt.m_128441_("entityData")) {
         this.entityData = nbt.m_128469_("entityData");
      } else {
         this.resetEntityData();
      }

      ListTag inbox = nbt.m_128437_("inbox", 10);
      if (inbox != null) {
         this.inbox.clear();

         for (int i = 0; i < inbox.size(); i++) {
            this.inbox.add(inbox.m_128728_(i));
         }
      }
   }

   private void resetEntityData() {
      this.entityData = new CompoundTag();
      VillagerEntityMCA villager = (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(this.world);
      assert villager != null;
      villager.initializeSkin(true);
      villager.getGenetics().randomize();
      villager.getTraits().randomize();
      villager.getVillagerBrain().randomize();
      villager.m_7380_(this.entityData);
   }

   public boolean isEntityDataSet() {
      return this.entityDataSet;
   }

   public CompoundTag getEntityData() {
      return this.entityData;
   }

   public void setEntityDataSet(boolean entityDataSet) {
      this.entityDataSet = entityDataSet;
   }

   public void setEntityData(CompoundTag entityData) {
      this.entityData = entityData;
   }

   @Override
   public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type, Entity victim) {
      EntityRelationship.super.onTragedy(cause, burialSite, type, victim);
      if (victim instanceof VillagerEntityMCA victimVillager) {
         this.sendLetterOfCondolence(
            victimVillager.m_7755_().getString(),
            victimVillager.getResidency().getHomeVillage().map(Village::getName).orElse(API.getVillagePool().pickVillageName("village"))
         );
      }
   }

   public void updateLastSeenVillage(VillageManager manager, ServerPlayer self) {
      Optional<Village> prevVillage = this.getLastSeenVillage(manager);
      Optional<Village> nextVillage = prevVillage.filter(v -> v.isWithinBorder(self)).or(() -> manager.findNearestVillage(self));
      this.setLastSeenVillage(self, prevVillage.orElse(null), nextVillage.orElse(null));
      if (nextVillage.isPresent()) {
         Rank rank = Tasks.getRank(nextVillage.get(), self);
         CriterionMCA.RANK.trigger(self, rank);
      }
   }

   public void setLastSeenVillage(ServerPlayer self, Village oldVillage, @Nullable Village newVillage) {
      this.lastSeenVillage = Optional.ofNullable(newVillage).map(Village::getId);
      this.m_77762_();
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

   protected void onLeave(Player self, Village village) {
      if (Config.getInstance().enterVillageNotification && village.isVillage()) {
         self.m_5661_(Component.m_237110_("gui.village.left", new Object[]{village.getName()}).m_130940_(ChatFormatting.GOLD), true);
      }
   }

   protected void onEnter(Player self, Village village) {
      if (Config.getInstance().enterVillageNotification && village.isVillage()) {
         self.m_5661_(Component.m_237110_("gui.village.welcome", new Object[]{village.getName()}).m_130940_(ChatFormatting.GOLD), true);
      }

      village.onEnter(this.world);
   }

   @Override
   public void marry(Entity spouse) {
      EntityRelationship.super.marry(spouse);
      this.m_77762_();
   }

   @Override
   public void endRelationShip(RelationshipState newState) {
      EntityRelationship.super.endRelationShip(newState);
      this.m_77762_();
   }

   @Override
   public ServerLevel getWorld() {
      return this.world;
   }

   @Override
   public UUID getUUID() {
      return this.uuid;
   }

   @Override
   public Gender getGender() {
      return Gender.byId(this.getEntityData().m_128451_("gender"));
   }

   @NotNull
   @Override
   public FamilyTreeNode getFamilyEntry() {
      return this.getFamilyTree().getOrEmpty(this.uuid).orElseGet(() -> {
         String name = Optional.ofNullable(this.world.m_46003_(this.uuid)).map(p -> p.m_7755_().getString()).orElse("Unnamed Adventurer");
         return this.getFamilyTree().getOrCreate(this.uuid, name, this.getGender(), true);
      });
   }

   public void reset() {
      this.endRelationShip(RelationshipState.SINGLE);
      this.m_77762_();
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      this.lastSeenVillage.ifPresent(id -> nbt.m_128405_("lastSeenVillage", id));
      nbt.m_128365_("entityData", this.entityData);
      nbt.m_128379_("entityDataSet", this.entityDataSet);
      nbt.m_128365_("inbox", NbtHelper.fromList(this.inbox, v -> v));
      return nbt;
   }

   public void sendMail(CompoundTag nbt) {
      if (Config.getInstance().enableVillagerMailingPlayers) {
         this.inbox.add(nbt);
      }

      this.m_77762_();
   }

   public boolean hasMail() {
      return !this.inbox.isEmpty();
   }

   public ItemStack getMail() {
      if (this.hasMail()) {
         CompoundTag nbt = this.inbox.remove(0);
         ItemStack stack = new ItemStack((ItemLike)ItemsMCA.LETTER.get(), 1);
         stack.m_41751_(nbt);
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
      ListTag l = new ListTag();

      for (String line : lines) {
         l.add(0, StringTag.m_129297_(line));
      }

      CompoundTag nbt = new CompoundTag();
      nbt.m_128365_("pages", l);
      this.sendMail(nbt);
      Optional.ofNullable(this.world.m_46003_(this.uuid)).ifPresent(p -> showMailNotification((ServerPlayer)p));
   }

   public static void showMailNotification(ServerPlayer player) {
      NetworkHandler.sendToPlayer(new ShowToastRequest("server.mail.title", "server.mail.description"), player);
   }
}
