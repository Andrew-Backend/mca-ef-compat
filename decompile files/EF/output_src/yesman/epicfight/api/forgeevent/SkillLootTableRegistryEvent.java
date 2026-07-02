package yesman.epicfight.api.forgeevent;

import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class SkillLootTableRegistryEvent extends Event implements IModBusEvent {
   private final Map<EntityType<?>, Builder> builders;

   public SkillLootTableRegistryEvent(Map<EntityType<?>, Builder> builders) {
      this.builders = builders;
   }

   public Builder get(EntityType<?> entityType) {
      return this.builders.get(entityType);
   }

   public SkillLootTableRegistryEvent put(EntityType<?> entityType, Builder builder) {
      this.builders.put(entityType, builder);
      return this;
   }

   public SkillLootTableRegistryEvent add(EntityType<?> entityType, net.minecraft.world.level.storage.loot.LootPool.Builder builder) {
      this.builders.computeIfAbsent(entityType, k -> LootTable.m_79147_()).m_79161_(builder);
      return this;
   }
}
