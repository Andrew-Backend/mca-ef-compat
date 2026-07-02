package forge.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.resources.data.tasks.AdvancementTask;
import forge.net.mca.resources.data.tasks.BlockingTask;
import forge.net.mca.resources.data.tasks.BuildingTask;
import forge.net.mca.resources.data.tasks.PopulationTask;
import forge.net.mca.resources.data.tasks.ReputationTask;
import forge.net.mca.resources.data.tasks.Task;
import forge.net.mca.server.world.data.Village;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class Tasks extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = MCA.locate("tasks");
   private static Tasks INSTANCE;
   public final Map<Rank, List<Task>> tasks = new HashMap<>();
   public static final Map<String, Function<JsonObject, Task>> TASK_TYPES = new HashMap<>();

   public static Tasks getInstance() {
      return INSTANCE;
   }

   public Tasks() {
      super(Resources.GSON, ID.m_135815_());
      INSTANCE = this;
   }

   protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      this.tasks.clear();

      for (Rank r : Rank.values()) {
         this.tasks.put(r, new LinkedList<>());
      }

      data.forEach((id, file) -> {
         Rank rank = Rank.fromName(id.m_135815_().split("\\.")[0]);
         file.getAsJsonArray().forEach(entry -> {
            String type = GsonHelper.m_13906_(entry.getAsJsonObject(), "type");
            Function<JsonObject, Task> myNew = TASK_TYPES.get(type);
            Task task = myNew.apply(entry.getAsJsonObject());
            this.tasks.get(rank).add(task);
         });
      });
   }

   public static Set<String> getCompletedIds(Village village, ServerPlayer player) {
      return getInstance()
         .tasks
         .values()
         .stream()
         .flatMap(Collection::stream)
         .filter(t -> t.isCompleted(village, player))
         .map(Task::getId)
         .collect(Collectors.toSet());
   }

   public static Rank getRank(Village village, ServerPlayer player) {
      Rank[] ranks = Rank.values();

      for (int i = ranks.length - 1; i >= 0; i--) {
         if (getInstance().tasks.get(ranks[i]).stream().allMatch(t -> !t.isRequired() || t.isCompleted(village, player))) {
            return ranks[i];
         }
      }

      return Rank.OUTLAW;
   }

   static {
      TASK_TYPES.put("blocking", BlockingTask::new);
      TASK_TYPES.put("building", BuildingTask::new);
      TASK_TYPES.put("population", PopulationTask::new);
      TASK_TYPES.put("reputation", ReputationTask::new);
      TASK_TYPES.put("advancement", AdvancementTask::new);
   }
}
