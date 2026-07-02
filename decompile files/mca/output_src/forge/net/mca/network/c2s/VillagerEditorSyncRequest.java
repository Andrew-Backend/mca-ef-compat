package forge.net.mca.network.c2s;

import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.NbtDataMessage;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.resources.ClothingList;
import forge.net.mca.resources.HairList;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;

public class VillagerEditorSyncRequest extends NbtDataMessage implements Message {
   private static final long serialVersionUID = -5581564927127176555L;
   private final String command;
   private final UUID uuid;

   public VillagerEditorSyncRequest(String command, UUID uuid, CompoundTag data) {
      super(data);
      this.command = command;
      this.uuid = uuid;
   }

   private void setHair(ServerPlayer player, Entity entity) {
      CompoundTag villagerData = GetVillagerRequest.getVillagerData(entity);
      if (villagerData != null) {
         String hair;
         if (this.getData().m_128441_("offset")) {
            hair = HairList.getInstance().getPool(this.getGender(villagerData)).pickNext(villagerData.m_128461_("hair"), this.getData().m_128451_("offset"));
         } else {
            hair = HairList.getInstance().getPool(this.getGender(villagerData)).pickOne();
         }

         villagerData.m_128359_("hair", hair);
         this.saveEntity(player, entity, villagerData);
      }
   }

   private void setClothing(ServerPlayer player, Entity entity) {
      CompoundTag villagerData = GetVillagerRequest.getVillagerData(entity);
      if (villagerData != null) {
         String clothes = "mca:missing";
         if (entity instanceof Player) {
            if (this.getData().m_128441_("offset")) {
               clothes = ClothingList.getInstance()
                  .getPool(this.getGender(villagerData), VillagerProfession.f_35585_)
                  .pickNext(villagerData.m_128461_("clothes"), this.getData().m_128451_("offset"));
            } else {
               clothes = ClothingList.getInstance().getPool(this.getGender(villagerData), VillagerProfession.f_35585_).pickOne();
            }
         } else if (entity instanceof VillagerLike<?> villager) {
            if (this.getData().m_128441_("offset")) {
               clothes = ClothingList.getInstance().getPool(villager).pickNext(villager.getClothes(), this.getData().m_128451_("offset"));
            } else {
               clothes = ClothingList.getInstance().getPool(villager).pickOne();
            }
         }

         villagerData.m_128359_("clothes", clothes);
         this.saveEntity(player, entity, villagerData);
      }
   }

   @Override
   public void receive(ServerPlayer player) {
      Entity entity = player.m_284548_().m_8791_(this.uuid);
      switch (this.command) {
         case "hair":
            this.setHair(player, entity);
            break;
         case "clothing":
            this.setClothing(player, entity);
            break;
         case "gender":
            this.setHair(player, entity);
            this.setClothing(player, entity);
            break;
         case "sync":
            this.saveEntity(player, entity, this.getData());
            break;
         case "profession":
            if (entity instanceof VillagerEntityMCA villager) {
               VillagerProfession profession = (VillagerProfession)BuiltInRegistries.f_256735_
                  .m_7745_(new ResourceLocation(this.getData().m_128461_("profession")));
               villager.setProfession(profession);
            }
      }

      this.getData();
   }

   private void saveEntity(ServerPlayer player, Entity entity, CompoundTag villagerData) {
      if (entity instanceof ServerPlayer serverPlayer) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayer);
         data.setEntityData(villagerData);
         data.setEntityDataSet(true);
         this.syncFamilyTree(player, entity, villagerData);
         serverPlayer.m_284548_().m_6907_().forEach(p -> NetworkHandler.sendToPlayer(new PlayerDataMessage(player.m_20148_(), villagerData), p));
      } else if (entity instanceof VillagerLike) {
         ((LivingEntity)entity).m_7378_(villagerData);
         entity.m_6210_();
         this.syncFamilyTree(player, entity, villagerData);
         if (entity instanceof VillagerEntityMCA villager) {
            villager.getResidency().getHomeVillage().ifPresent(b -> b.updateResident(villager));
         }
      }
   }

   private Gender getGender(CompoundTag villagerData) {
      return Gender.byId(villagerData.m_128451_("gender"));
   }

   private Optional<FamilyTreeNode> getFamilyNode(ServerPlayer player, FamilyTree tree, String name, Gender gender) {
      try {
         UUID uuid = UUID.fromString(name);
         Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
         if (node.isPresent()) {
            player.m_5661_(Component.m_237110_("gui.villager_editor.uuid_known", new Object[]{name, node.get().getName()}), true);
            return node;
         } else {
            player.m_5661_(Component.m_237110_("gui.villager_editor.uuid_unknown", new Object[]{name}).m_130940_(ChatFormatting.RED), true);
            return Optional.empty();
         }
      } catch (IllegalArgumentException exception) {
         List<FamilyTreeNode> nodes = tree.getAllWithName(name).toList();
         if (nodes.isEmpty()) {
            player.m_5661_(Component.m_237110_("gui.villager_editor.name_created", new Object[]{name}).m_130940_(ChatFormatting.YELLOW), true);
            return Optional.of(tree.getOrCreate(UUID.randomUUID(), name, gender));
         }

         if (nodes.size() > 1) {
            player.m_5661_(Component.m_237110_("gui.villager_editor.name_not_unique", new Object[]{name}).m_130940_(ChatFormatting.RED), true);
            String uuids = nodes.stream().map(FamilyTreeNode::id).map(UUID::toString).collect(Collectors.joining(", "));
            player.m_5661_(Component.m_237110_("gui.villager_editor.list_of_ids", new Object[]{uuids}), false);
         } else {
            player.m_5661_(Component.m_237110_("gui.villager_editor.name_unique", new Object[]{name}), true);
         }

         return Optional.ofNullable(nodes.get(0));
      }
   }

   private void syncFamilyTree(ServerPlayer player, Entity entity, CompoundTag villagerData) {
      FamilyTree tree = FamilyTree.get((ServerLevel)entity.m_9236_());
      FamilyTreeNode entry = tree.getOrCreate(entity);
      entry.setGender(this.getGender(this.getData()));
      entry.setName(this.getData().m_128461_("villagerName"));
      if (villagerData.m_128441_("tree_father_new")) {
         String name = villagerData.m_128461_("tree_father_new");
         if (MCA.isBlankString(name)) {
            entry.removeFather();
         } else {
            this.getFamilyNode(player, tree, name, Gender.MALE).ifPresent(entry::setFather);
         }
      }

      if (villagerData.m_128441_("tree_mother_new")) {
         String name = villagerData.m_128461_("tree_mother_new");
         if (MCA.isBlankString(name)) {
            entry.removeMother();
         } else {
            this.getFamilyNode(player, tree, name, Gender.FEMALE).ifPresent(entry::setMother);
         }
      }

      if (villagerData.m_128441_("tree_spouse_new")) {
         String name = villagerData.m_128461_("tree_spouse_new");
         if (MCA.isBlankString(name)) {
            Optional.of(entry.partner()).flatMap(tree::getOrEmpty).ifPresent(node -> node.updatePartner(null, null));
            entry.updatePartner(null, null);
         } else {
            this.getFamilyNode(player, tree, name, entry.gender().opposite()).ifPresent(node -> {
               entry.updatePartner(node);
               node.updatePartner(entry);
            });
         }
      }
   }
}
