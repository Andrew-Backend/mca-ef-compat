package fabric.net.mca.entity.ai;

import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.brain.VillagerBrain;
import java.util.UUID;
import net.minecraft.class_1308;
import net.minecraft.class_2487;
import org.jetbrains.annotations.Nullable;

public class Memories {
   private int hearts;
   private final UUID playerUUID;
   private int interactionFatigue;
   private DialogueType dialogueType;
   private final VillagerBrain<?> brain;
   private long lastSeen;

   public Memories(VillagerBrain<?> brain, long time, UUID uuid) {
      this.brain = brain;
      this.playerUUID = uuid;
      this.dialogueType = DialogueType.UNASSIGNED;
      this.lastSeen = time / 24000L;
   }

   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   public int getHearts() {
      return this.hearts;
   }

   public void setHearts(int value) {
      this.hearts = value;
      this.brain.updateMemories(this);
   }

   public void modHearts(int value) {
      this.setHearts(this.hearts += value);
   }

   public int getInteractionFatigue() {
      return this.interactionFatigue;
   }

   public void setInteractionFatigue(int value) {
      this.interactionFatigue = value;
      this.brain.updateMemories(this);
   }

   public void modInteractionFatigue(int value) {
      this.interactionFatigue += value;
      this.brain.updateMemories(this);
   }

   public DialogueType getDialogueType() {
      return this.dialogueType;
   }

   public void setDialogueType(DialogueType dialogueType) {
      this.dialogueType = dialogueType;
      this.brain.updateMemories(this);
   }

   public long getLastSeen() {
      return this.lastSeen;
   }

   public void setLastSeen(int lastSeen) {
      this.lastSeen = lastSeen;
      this.brain.updateMemories(this);
   }

   public class_2487 toCNBT() {
      class_2487 nbt = new class_2487();
      nbt.method_25927("playerUUID", this.playerUUID);
      nbt.method_10569("hearts", this.hearts);
      nbt.method_10569("interactionFatigue", this.interactionFatigue);
      nbt.method_10569("dialogueType", this.dialogueType.ordinal());
      nbt.method_10544("lastSeen", this.lastSeen);
      return nbt;
   }

   public static <E extends class_1308 & VillagerLike<E>> Memories fromCNBT(E villager, @Nullable class_2487 tag) {
      if (tag != null && !tag.method_33133()) {
         Memories memories = new Memories(villager.getVillagerBrain(), villager.method_37908().method_8532(), tag.method_25926("playerUUID"));
         memories.hearts = tag.method_10550("hearts");
         memories.interactionFatigue = tag.method_10550("interactionFatigue");
         memories.dialogueType = DialogueType.byId(tag.method_10550("dialogueType"));
         memories.lastSeen = tag.method_10537("lastSeen");
         return memories;
      } else {
         return null;
      }
   }
}
