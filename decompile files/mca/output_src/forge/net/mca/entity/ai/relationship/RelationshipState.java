package forge.net.mca.entity.ai.relationship;

public enum RelationshipState {
   SINGLE("notMarried"),
   PROMISED("promised"),
   ENGAGED("engaged"),
   MARRIED_TO_VILLAGER("married"),
   MARRIED_TO_PLAYER("marriedToPlayer"),
   WIDOW("widow");

   private static final RelationshipState[] VALUES = values();
   private final String icon;

   RelationshipState(String icon) {
      this.icon = icon;
   }

   public boolean isMarried() {
      return this == MARRIED_TO_PLAYER || this == MARRIED_TO_VILLAGER;
   }

   public RelationshipState base() {
      return this == MARRIED_TO_PLAYER ? MARRIED_TO_VILLAGER : this;
   }

   public String getIcon() {
      return this.icon;
   }

   public static RelationshipState byId(int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : SINGLE;
   }
}
