package forge.net.mca.server.world.data;

import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class CivilRegistryManager extends SavedData {
   private final LinkedList<Component> entries = new LinkedList<>();

   public static CivilRegistryManager get(ServerLevel world, Village village) {
      return WorldUtils.loadData(world.m_7654_().m_129783_(), CivilRegistryManager::new, CivilRegistryManager::new, "mca_civil_registry_" + village.getId());
   }

   CivilRegistryManager(ServerLevel world) {
   }

   CivilRegistryManager(CompoundTag nbt) {
      this.entries.addAll(NbtHelper.toList(nbt.m_128423_("entries"), element -> Serializer.m_130701_(element.m_7916_())));
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      ListTag elements = NbtHelper.fromList(this.entries, a -> StringTag.m_129297_(Serializer.m_130703_(a)));
      CompoundTag compound = new CompoundTag();
      compound.m_128365_("entries", elements);
      return compound;
   }

   public void addText(Component text) {
      this.entries.addFirst(text);
      this.m_77762_();
   }

   public List<Component> getPage(int from, int to) {
      to = Math.min(this.entries.size(), to);
      return to <= from ? List.of() : this.entries.subList(from, to);
   }
}
