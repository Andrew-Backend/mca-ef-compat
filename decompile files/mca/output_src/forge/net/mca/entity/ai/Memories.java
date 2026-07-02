package forge.net.mca.entity.ai;

import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
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

   public CompoundTag toCNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.m_128362_("playerUUID", this.playerUUID);
      nbt.m_128405_("hearts", this.hearts);
      nbt.m_128405_("interactionFatigue", this.interactionFatigue);
      nbt.m_128405_("dialogueType", this.dialogueType.ordinal());
      nbt.m_128356_("lastSeen", this.lastSeen);
      return nbt;
   }

   public static <E extends Mob & VillagerLike<E>> Memories fromCNBT(E villager, @Nullable CompoundTag tag) {
      if (tag != null && !tag.m_128456_()) {
         Memories memories = new Memories(villager.getVillagerBrain(), villager.m_9236_().m_46468_(), tag.m_128342_("playerUUID"));
         memories.hearts = tag.m_128451_("hearts");
         memories.interactionFatigue = tag.m_128451_("interactionFatigue");
         memories.dialogueType = DialogueType.byId(tag.m_128451_("dialogueType"));
         memories.lastSeen = tag.m_128454_("lastSeen");
         return memories;
      } else {
         return null;
      }
   }
}
