package quilt.net.mca.network.c2s;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3852;
import net.minecraft.class_7923;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.network.NbtDataMessage;
import quilt.net.mca.network.s2c.PlayerDataMessage;
import quilt.net.mca.resources.ClothingList;
import quilt.net.mca.resources.HairList;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.FamilyTreeNode;
import quilt.net.mca.server.world.data.PlayerSaveData;

public class VillagerEditorSyncRequest extends NbtDataMessage implements Message {
   private static final long serialVersionUID = -5581564927127176555L;
   private final String command;
   private final UUID uuid;

   public VillagerEditorSyncRequest(String command, UUID uuid, class_2487 data) {
      super(data);
      this.command = command;
      this.uuid = uuid;
   }

   private void setHair(class_3222 player, class_1297 entity) {
      class_2487 villagerData = GetVillagerRequest.getVillagerData(entity);
      if (villagerData != null) {
         String hair;
         if (this.getData().method_10545("offset")) {
            hair = HairList.getInstance()
               .getPool(this.getGender(villagerData))
               .pickNext(villagerData.method_10558("hair"), this.getData().method_10550("offset"));
         } else {
            hair = HairList.getInstance().getPool(this.getGender(villagerData)).pickOne();
         }

         villagerData.method_10582("hair", hair);
         this.saveEntity(player, entity, villagerData);
      }
   }

   private void setClothing(class_3222 player, class_1297 entity) {
      class_2487 villagerData = GetVillagerRequest.getVillagerData(entity);
      if (villagerData != null) {
         String clothes = "mca:missing";
         if (entity instanceof class_1657) {
            if (this.getData().method_10545("offset")) {
               clothes = ClothingList.getInstance()
                  .getPool(this.getGender(villagerData), class_3852.field_17051)
                  .pickNext(villagerData.method_10558("clothes"), this.getData().method_10550("offset"));
            } else {
               clothes = ClothingList.getInstance().getPool(this.getGender(villagerData), class_3852.field_17051).pickOne();
            }
         } else if (entity instanceof VillagerLike<?> villager) {
            if (this.getData().method_10545("offset")) {
               clothes = ClothingList.getInstance().getPool(villager).pickNext(villager.getClothes(), this.getData().method_10550("offset"));
            } else {
               clothes = ClothingList.getInstance().getPool(villager).pickOne();
            }
         }

         villagerData.method_10582("clothes", clothes);
         this.saveEntity(player, entity, villagerData);
      }
   }

   @Override
   public void receive(class_3222 player) {
      class_1297 entity = player.method_51469().method_14190(this.uuid);
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
               class_3852 profession = (class_3852)class_7923.field_41195.method_10223(new class_2960(this.getData().method_10558("profession")));
               villager.setProfession(profession);
            }
      }

      this.getData();
   }

   private void saveEntity(class_3222 player, class_1297 entity, class_2487 villagerData) {
      if (entity instanceof class_3222 serverPlayer) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayer);
         data.setEntityData(villagerData);
         data.setEntityDataSet(true);
         this.syncFamilyTree(player, entity, villagerData);
         serverPlayer.method_51469().method_18456().forEach(p -> NetworkHandler.sendToPlayer(new PlayerDataMessage(player.method_5667(), villagerData), p));
      } else if (entity instanceof VillagerLike) {
         ((class_1309)entity).method_5749(villagerData);
         entity.method_18382();
         this.syncFamilyTree(player, entity, villagerData);
         if (entity instanceof VillagerEntityMCA villager) {
            villager.getResidency().getHomeVillage().ifPresent(b -> b.updateResident(villager));
         }
      }
   }

   private Gender getGender(class_2487 villagerData) {
      return Gender.byId(villagerData.method_10550("gender"));
   }

   private Optional<FamilyTreeNode> getFamilyNode(class_3222 player, FamilyTree tree, String name, Gender gender) {
      try {
         UUID uuid = UUID.fromString(name);
         Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
         if (node.isPresent()) {
            player.method_7353(class_2561.method_43469("gui.villager_editor.uuid_known", new Object[]{name, node.get().getName()}), true);
            return node;
         } else {
            player.method_7353(class_2561.method_43469("gui.villager_editor.uuid_unknown", new Object[]{name}).method_27692(class_124.field_1061), true);
            return Optional.empty();
         }
      } catch (IllegalArgumentException exception) {
         List<FamilyTreeNode> nodes = tree.getAllWithName(name).toList();
         if (nodes.isEmpty()) {
            player.method_7353(class_2561.method_43469("gui.villager_editor.name_created", new Object[]{name}).method_27692(class_124.field_1054), true);
            return Optional.of(tree.getOrCreate(UUID.randomUUID(), name, gender));
         }

         if (nodes.size() > 1) {
            player.method_7353(class_2561.method_43469("gui.villager_editor.name_not_unique", new Object[]{name}).method_27692(class_124.field_1061), true);
            String uuids = nodes.stream().map(FamilyTreeNode::id).map(UUID::toString).collect(Collectors.joining(", "));
            player.method_7353(class_2561.method_43469("gui.villager_editor.list_of_ids", new Object[]{uuids}), false);
         } else {
            player.method_7353(class_2561.method_43469("gui.villager_editor.name_unique", new Object[]{name}), true);
         }

         return Optional.ofNullable(nodes.get(0));
      }
   }

   private void syncFamilyTree(class_3222 player, class_1297 entity, class_2487 villagerData) {
      FamilyTree tree = FamilyTree.get((class_3218)entity.method_37908());
      FamilyTreeNode entry = tree.getOrCreate(entity);
      entry.setGender(this.getGender(this.getData()));
      entry.setName(this.getData().method_10558("villagerName"));
      if (villagerData.method_10545("tree_father_new")) {
         String name = villagerData.method_10558("tree_father_new");
         if (MCA.isBlankString(name)) {
            entry.removeFather();
         } else {
            this.getFamilyNode(player, tree, name, Gender.MALE).ifPresent(entry::setFather);
         }
      }

      if (villagerData.method_10545("tree_mother_new")) {
         String name = villagerData.method_10558("tree_mother_new");
         if (MCA.isBlankString(name)) {
            entry.removeMother();
         } else {
            this.getFamilyNode(player, tree, name, Gender.FEMALE).ifPresent(entry::setMother);
         }
      }

      if (villagerData.method_10545("tree_spouse_new")) {
         String name = villagerData.method_10558("tree_spouse_new");
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
