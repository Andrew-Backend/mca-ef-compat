package quilt.net.mca.server.world.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3852;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.ai.relationship.EntityRelationship;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.RelationshipState;
import quilt.net.mca.util.NbtHelper;

public final class FamilyTreeNode implements Serializable {
   private static final long serialVersionUID = -7307057982785253721L;
   private final boolean isPlayer;
   private Gender gender;
   private String name;
   private String profession = class_7923.field_41195.method_10221(class_3852.field_17051).toString();
   private final UUID id;
   private UUID father;
   private UUID mother;
   private UUID partner = class_156.field_25140;
   private RelationshipState relationshipState = RelationshipState.SINGLE;
   private boolean deceased;
   private final Set<UUID> children = new HashSet<>();
   private final transient FamilyTree rootNode;

   public FamilyTreeNode(FamilyTree rootNode, UUID id, String name, boolean isPlayer, Gender gender, UUID father, UUID mother) {
      this.rootNode = rootNode;
      this.id = id;
      this.name = name;
      this.isPlayer = isPlayer;
      this.gender = gender;
      this.father = father;
      this.mother = mother;
   }

   public FamilyTreeNode(FamilyTree rootNode, UUID id, class_2487 nbt) {
      this(
         rootNode,
         id,
         nbt.method_10558("name"),
         nbt.method_10577("isPlayer"),
         Gender.byId(nbt.method_10550("gender")),
         nbt.method_25926("father"),
         nbt.method_25926("mother")
      );
      this.children.addAll(NbtHelper.toList(nbt.method_10554("children", 10), c -> ((class_2487)c).method_25926("uuid")));
      this.profession = nbt.method_10558("profession");
      this.deceased = nbt.method_10577("isDeceased");
      if (nbt.method_25928("spouse")) {
         this.partner = nbt.method_25926("spouse");
      }

      this.relationshipState = RelationshipState.byId(nbt.method_10550("marriageState"));
   }

   public UUID id() {
      return this.id;
   }

   private void markDirty() {
      if (this.rootNode != null) {
         this.rootNode.method_80();
      }
   }

   public boolean isDeceased() {
      return this.deceased;
   }

   public void setDeceased(boolean deceased) {
      this.deceased = deceased;
      this.markDirty();
   }

   public void setName(String name) {
      this.name = name;
      this.markDirty();
   }

   public String getName() {
      return this.name;
   }

   public void setProfession(class_3852 profession) {
      this.profession = class_7923.field_41195.method_10221(profession).toString();
      this.markDirty();
   }

   public class_3852 getProfession() {
      return (class_3852)class_7923.field_41195.method_10223(this.getProfessionId());
   }

   public class_2960 getProfessionId() {
      return class_2960.method_12829(this.profession);
   }

   public String getProfessionName() {
      String professionName = (this.getProfessionId().method_12836().equalsIgnoreCase("minecraft")
            ? (this.getProfessionId().method_12832().equals("none") ? "mca.none" : this.getProfessionId().method_12832())
            : this.getProfessionId().toString())
         .replace(":", ".");
      return MCA.isBlankString(professionName) ? "mca.none" : professionName;
   }

   public class_5250 getProfessionText() {
      return class_2561.method_43471("entity.minecraft.villager." + this.getProfessionName());
   }

   public boolean isPlayer() {
      return this.isPlayer;
   }

   public Gender gender() {
      return this.gender;
   }

   public UUID father() {
      return this.father;
   }

   public UUID mother() {
      return this.mother;
   }

   public UUID partner() {
      return this.partner;
   }

   public RelationshipState getRelationshipState() {
      return this.relationshipState;
   }

   public void setRelationshipState(RelationshipState relationshipState) {
      this.relationshipState = relationshipState;
   }

   public void updatePartner(@Nullable class_1297 newPartner, @Nullable RelationshipState state) {
      if (!this.partner.equals(class_156.field_25140) && (newPartner == null || !this.partner.equals(newPartner.method_5667()))) {
         this.getRoot().getOrEmpty(this.partner).ifPresent(n -> {
            n.partner = class_156.field_25140;
            n.relationshipState = RelationshipState.SINGLE;
         });
      }

      this.partner = newPartner == null ? class_156.field_25140 : newPartner.method_5667();
      this.relationshipState = state == null && newPartner == null ? RelationshipState.SINGLE : state;
      if (newPartner != null) {
         this.rootNode.getOrCreate(newPartner);
      }

      this.rootNode.method_80();
   }

   public void updatePartner(FamilyTreeNode spouse) {
      this.partner = spouse.id();
      this.relationshipState = spouse.isPlayer ? RelationshipState.MARRIED_TO_PLAYER : RelationshipState.MARRIED_TO_VILLAGER;
      this.markDirty();
   }

   public Set<UUID> children() {
      return this.children;
   }

   public Stream<UUID> streamChildren() {
      return this.children.stream().filter(FamilyTreeNode::isValid);
   }

   public Stream<UUID> streamParents() {
      return Stream.of(this.father(), this.mother()).filter(FamilyTreeNode::isValid);
   }

   public Set<UUID> siblings() {
      Set<UUID> siblings = new HashSet<>();
      this.streamParents().forEach(parent -> this.getRoot().getOrEmpty(parent).ifPresent(p -> gatherChildren(p, siblings, 1)));
      return siblings;
   }

   public Stream<UUID> getChildren() {
      return this.getRelatives(0, 1);
   }

   public Stream<UUID> getAllRelatives(int depth) {
      Set<UUID> family = new HashSet<>();
      Set<UUID> todo = new HashSet<>();
      todo.add(this.id);

      for (int d = 0; d < depth; d++) {
         Set<UUID> nextTodo = new HashSet<>();

         for (UUID uuid : todo) {
            if (!family.contains(uuid)) {
               this.rootNode.getOrEmpty(uuid).ifPresent(node -> {
                  family.add(uuid);
                  node.streamParents().forEach(nextTodo::add);
                  node.streamChildren().forEach(nextTodo::add);
               });
            }
         }

         todo = nextTodo;
      }

      family.remove(this.id);
      return family.stream();
   }

   public Stream<UUID> getRelatives(int parentDepth, int childrenDepth) {
      Set<UUID> family = new HashSet<>();
      gatherParents(this, family, parentDepth);
      gatherChildren(this, family, childrenDepth);
      family.remove(this.id);
      return family.stream();
   }

   public boolean isRelative(UUID with) {
      return this.getAllRelatives(9).anyMatch(with::equals);
   }

   public Stream<FamilyTreeNode> getParents() {
      return this.lookup(this.streamParents());
   }

   public Stream<FamilyTreeNode> getSiblings() {
      return this.lookup(this.siblings().stream());
   }

   public Stream<FamilyTreeNode> lookup(Stream<UUID> uuids) {
      return uuids.map(this.getRoot()::getOrEmpty).filter(Optional::isPresent).map(Optional::get);
   }

   public boolean isParent(UUID id) {
      return this.streamParents().anyMatch(parent -> parent.equals(id));
   }

   public boolean isGrandParent(UUID id) {
      return this.getParents().anyMatch(parent -> parent.isParent(id));
   }

   public boolean isUncle(UUID id) {
      return this.getParents().flatMap(parent -> parent.siblings().stream()).distinct().anyMatch(id::equals);
   }

   public void addChild(UUID child) {
      this.children.add(child);
   }

   public FamilyTree getRoot() {
      return this.rootNode;
   }

   public boolean replaceParents(EntityRelationship one, EntityRelationship two) {
      return this.replaceParents(Stream.of(one.getFamilyEntry(), two.getFamilyEntry()));
   }

   public boolean replaceParents(FamilyTreeNode one, Optional<FamilyTreeNode> two) {
      return this.replaceParents(Stream.concat(Stream.of(one), two.stream()));
   }

   public boolean replaceParents(Stream<FamilyTreeNode> parents) {
      boolean result = this.clearParents();
      return parents.map(this::assignParent).reduce(result, (changed, parentChanged) -> changed | parentChanged);
   }

   public boolean assignParent(FamilyTreeNode parent) {
      int parents = (isValid(this.father) ? 1 : 0) + (isValid(this.mother) ? 1 : 0);
      if (parents == 1) {
         if (!isValid(this.father)) {
            return this.setFather(parent);
         } else {
            return !isValid(this.mother) ? this.setMother(parent) : true;
         }
      } else {
         return parent.gender() == Gender.MALE ? this.setFather(parent) : this.setMother(parent);
      }
   }

   public boolean clearParents() {
      return this.removeFather() | this.removeMother();
   }

   public boolean setFather(FamilyTreeNode parent) {
      this.father = parent.id();
      parent.children().add(this.id);
      this.markDirty();
      return true;
   }

   public boolean setMother(FamilyTreeNode parent) {
      this.mother = parent.id();
      parent.children().add(this.id);
      this.markDirty();
      return true;
   }

   public boolean removeFather() {
      if (isValid(this.father)) {
         this.rootNode.getOrEmpty(this.father).ifPresent(e -> e.children.remove(this.id));
         this.father = class_156.field_25140;
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeMother() {
      if (isValid(this.mother)) {
         this.rootNode.getOrEmpty(this.mother).ifPresent(e -> e.children.remove(this.id));
         this.mother = class_156.field_25140;
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public void setGender(Gender gender) {
      this.gender = gender;
      this.markDirty();
   }

   public boolean probablyGenerated() {
      return this.mother.equals(class_156.field_25140)
         && this.father.equals(class_156.field_25140)
         && this.children.size() == 1
         && this.deceased
         && !this.isPlayer();
   }

   public boolean willBeRemembered() {
      if (!this.children.isEmpty()) {
         return true;
      } else {
         return !this.partner.equals(class_156.field_25140) ? true : !this.getParents().allMatch(FamilyTreeNode::probablyGenerated);
      }
   }

   public static boolean isValid(@Nullable UUID uuid) {
      return uuid != null && !class_156.field_25140.equals(uuid);
   }

   private static void gatherParents(FamilyTreeNode current, Set<UUID> family, int depth) {
      gather(current, family, depth, FamilyTreeNode::streamParents);
   }

   private static void gatherChildren(FamilyTreeNode current, Set<UUID> family, int depth) {
      gather(current, family, depth, FamilyTreeNode::streamChildren);
   }

   private static void gather(@Nullable FamilyTreeNode entry, Set<UUID> output, int depth, Function<FamilyTreeNode, Stream<UUID>> walker) {
      if (entry != null && depth > 0) {
         walker.apply(entry).forEach(id -> {
            if (!class_156.field_25140.equals(id)) {
               output.add(id);
            }

            if (depth > 1) {
               entry.getRoot().getOrEmpty(id).ifPresent(e -> gather(e, output, depth - 1, walker));
            }
         });
      }
   }

   public class_2487 save() {
      class_2487 nbt = new class_2487();
      nbt.method_10582("name", this.name);
      nbt.method_10556("isPlayer", this.isPlayer);
      nbt.method_10556("isDeceased", this.deceased);
      nbt.method_10569("gender", this.gender.getId());
      nbt.method_25927("father", this.father);
      nbt.method_25927("mother", this.mother);
      nbt.method_25927("spouse", this.partner);
      nbt.method_10569("marriageState", this.relationshipState.ordinal());
      nbt.method_10566("children", NbtHelper.fromList(this.children, child -> {
         class_2487 n = new class_2487();
         n.method_25927("uuid", child);
         return n;
      }));
      return nbt;
   }
}
