package forge.net.mca.server.world.data;

import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.EntityRelationship;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.util.NbtHelper;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.jetbrains.annotations.Nullable;

public final class FamilyTreeNode implements Serializable {
   private static final long serialVersionUID = -7307057982785253721L;
   private final boolean isPlayer;
   private Gender gender;
   private String name;
   private String profession = BuiltInRegistries.f_256735_.m_7981_(VillagerProfession.f_35585_).toString();
   private final UUID id;
   private UUID father;
   private UUID mother;
   private UUID partner = Util.f_137441_;
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

   public FamilyTreeNode(FamilyTree rootNode, UUID id, CompoundTag nbt) {
      this(
         rootNode, id, nbt.m_128461_("name"), nbt.m_128471_("isPlayer"), Gender.byId(nbt.m_128451_("gender")), nbt.m_128342_("father"), nbt.m_128342_("mother")
      );
      this.children.addAll(NbtHelper.toList(nbt.m_128437_("children", 10), c -> ((CompoundTag)c).m_128342_("uuid")));
      this.profession = nbt.m_128461_("profession");
      this.deceased = nbt.m_128471_("isDeceased");
      if (nbt.m_128403_("spouse")) {
         this.partner = nbt.m_128342_("spouse");
      }

      this.relationshipState = RelationshipState.byId(nbt.m_128451_("marriageState"));
   }

   public UUID id() {
      return this.id;
   }

   private void markDirty() {
      if (this.rootNode != null) {
         this.rootNode.m_77762_();
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

   public void setProfession(VillagerProfession profession) {
      this.profession = BuiltInRegistries.f_256735_.m_7981_(profession).toString();
      this.markDirty();
   }

   public VillagerProfession getProfession() {
      return (VillagerProfession)BuiltInRegistries.f_256735_.m_7745_(this.getProfessionId());
   }

   public ResourceLocation getProfessionId() {
      return ResourceLocation.m_135820_(this.profession);
   }

   public String getProfessionName() {
      String professionName = (this.getProfessionId().m_135827_().equalsIgnoreCase("minecraft")
            ? (this.getProfessionId().m_135815_().equals("none") ? "mca.none" : this.getProfessionId().m_135815_())
            : this.getProfessionId().toString())
         .replace(":", ".");
      return MCA.isBlankString(professionName) ? "mca.none" : professionName;
   }

   public MutableComponent getProfessionText() {
      return Component.m_237115_("entity.minecraft.villager." + this.getProfessionName());
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

   public void updatePartner(@Nullable Entity newPartner, @Nullable RelationshipState state) {
      if (!this.partner.equals(Util.f_137441_) && (newPartner == null || !this.partner.equals(newPartner.m_20148_()))) {
         this.getRoot().getOrEmpty(this.partner).ifPresent(n -> {
            n.partner = Util.f_137441_;
            n.relationshipState = RelationshipState.SINGLE;
         });
      }

      this.partner = newPartner == null ? Util.f_137441_ : newPartner.m_20148_();
      this.relationshipState = state == null && newPartner == null ? RelationshipState.SINGLE : state;
      if (newPartner != null) {
         this.rootNode.getOrCreate(newPartner);
      }

      this.rootNode.m_77762_();
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
         this.father = Util.f_137441_;
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeMother() {
      if (isValid(this.mother)) {
         this.rootNode.getOrEmpty(this.mother).ifPresent(e -> e.children.remove(this.id));
         this.mother = Util.f_137441_;
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
      return this.mother.equals(Util.f_137441_) && this.father.equals(Util.f_137441_) && this.children.size() == 1 && this.deceased && !this.isPlayer();
   }

   public boolean willBeRemembered() {
      if (!this.children.isEmpty()) {
         return true;
      } else {
         return !this.partner.equals(Util.f_137441_) ? true : !this.getParents().allMatch(FamilyTreeNode::probablyGenerated);
      }
   }

   public static boolean isValid(@Nullable UUID uuid) {
      return uuid != null && !Util.f_137441_.equals(uuid);
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
            if (!Util.f_137441_.equals(id)) {
               output.add(id);
            }

            if (depth > 1) {
               entry.getRoot().getOrEmpty(id).ifPresent(e -> gather(e, output, depth - 1, walker));
            }
         });
      }
   }

   public CompoundTag save() {
      CompoundTag nbt = new CompoundTag();
      nbt.m_128359_("name", this.name);
      nbt.m_128379_("isPlayer", this.isPlayer);
      nbt.m_128379_("isDeceased", this.deceased);
      nbt.m_128405_("gender", this.gender.getId());
      nbt.m_128362_("father", this.father);
      nbt.m_128362_("mother", this.mother);
      nbt.m_128362_("spouse", this.partner);
      nbt.m_128405_("marriageState", this.relationshipState.ordinal());
      nbt.m_128365_("children", NbtHelper.fromList(this.children, child -> {
         CompoundTag n = new CompoundTag();
         n.m_128362_("uuid", child);
         return n;
      }));
      return nbt;
   }
}
