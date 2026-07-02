package forge.net.mca.entity.ai.pathfinder;

import net.minecraft.world.level.pathfinder.BlockPathTypes;

public enum ExtendedPathNodeType {
   BLOCKED(-1.0F),
   OPEN(0.0F),
   WALKABLE(0.0F),
   WALKABLE_DOOR(0.0F),
   TRAPDOOR(0.0F),
   POWDER_SNOW(-1.0F),
   DANGER_POWDER_SNOW(0.0F),
   FENCE(-1.0F),
   LAVA(-1.0F),
   WATER(8.0F),
   WATER_BORDER(8.0F),
   RAIL(0.0F),
   UNPASSABLE_RAIL(-1.0F),
   DANGER_FIRE(8.0F),
   DAMAGE_FIRE(16.0F),
   DANGER_OTHER(8.0F),
   DAMAGE_OTHER(-1.0F),
   DOOR_OPEN(0.0F),
   DOOR_WOOD_CLOSED(-1.0F),
   DOOR_IRON_CLOSED(-1.0F),
   BREACH(4.0F),
   LEAVES(-1.0F),
   STICKY_HONEY(8.0F),
   COCOA(0.0F),
   GRASS(-1.0F, BlockPathTypes.BLOCKED),
   PATH(-1.0F, BlockPathTypes.BLOCKED),
   WALKABLE_GRASS(0.0F, BlockPathTypes.WALKABLE),
   WALKABLE_PATH(0.0F, BlockPathTypes.WALKABLE);

   private final float defaultPenalty;
   private BlockPathTypes vanilla;

   ExtendedPathNodeType(float defaultPenalty) {
      this(defaultPenalty, null);
      if (this.vanilla == null) {
         this.vanilla = BlockPathTypes.valueOf(this.name());
      }
   }

   ExtendedPathNodeType(float defaultPenalty, BlockPathTypes vanilla) {
      this.defaultPenalty = defaultPenalty;
      this.vanilla = vanilla;
   }

   public float getDefaultPenalty() {
      return this.defaultPenalty;
   }

   public BlockPathTypes toVanilla() {
      return this.vanilla;
   }

   public boolean isWalkable() {
      return this == WALKABLE || this == WALKABLE_GRASS || this == WALKABLE_PATH;
   }

   public float getBonusPenalty() {
      return this.defaultPenalty >= 0.0F ? (this == WALKABLE_GRASS ? 2.0F : (this == WALKABLE_PATH ? 0.001F : (this == OPEN ? 0.0F : 1.0F))) : 0.0F;
   }
}
