package quilt.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3300;
import net.minecraft.class_3518;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import quilt.net.mca.MCA;
import quilt.net.mca.resources.data.tasks.AdvancementTask;
import quilt.net.mca.resources.data.tasks.BlockingTask;
import quilt.net.mca.resources.data.tasks.BuildingTask;
import quilt.net.mca.resources.data.tasks.PopulationTask;
import quilt.net.mca.resources.data.tasks.ReputationTask;
import quilt.net.mca.resources.data.tasks.Task;
import quilt.net.mca.server.world.data.Village;

public class Tasks extends class_4309 {
   protected static final class_2960 ID = MCA.locate("tasks");
   private static Tasks INSTANCE;
   public final Map<Rank, List<Task>> tasks = new HashMap<>();
   public static final Map<String, Function<JsonObject, Task>> TASK_TYPES = new HashMap<>();

   public static Tasks getInstance() {
      return INSTANCE;
   }

   public Tasks() {
      super(Resources.GSON, ID.method_12832());
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.tasks.clear();

      for (Rank r : Rank.values()) {
         this.tasks.put(r, new LinkedList<>());
      }

      data.forEach((id, file) -> {
         Rank rank = Rank.fromName(id.method_12832().split("\\.")[0]);
         file.getAsJsonArray().forEach(entry -> {
            String type = class_3518.method_15265(entry.getAsJsonObject(), "type");
            Function<JsonObject, Task> myNew = TASK_TYPES.get(type);
            Task task = myNew.apply(entry.getAsJsonObject());
            this.tasks.get(rank).add(task);
         });
      });
   }

   public static Set<String> getCompletedIds(Village village, class_3222 player) {
      return getInstance()
         .tasks
         .values()
         .stream()
         .flatMap(Collection::stream)
         .filter(t -> t.isCompleted(village, player))
         .map(Task::getId)
         .collect(Collectors.toSet());
   }

   public static Rank getRank(Village village, class_3222 player) {
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
