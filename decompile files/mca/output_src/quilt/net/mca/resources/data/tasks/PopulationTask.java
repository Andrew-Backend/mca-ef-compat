package quilt.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_5250;
import quilt.net.mca.server.world.data.Village;

public class PopulationTask extends Task {
   private static final long serialVersionUID = 5252203744206810361L;
   private final int population;

   public PopulationTask(int population) {
      super("population_" + population);
      this.population = population;
   }

   public PopulationTask(JsonObject json) {
      this(class_3518.method_15260(json, "population"));
   }

   @Override
   public boolean isCompleted(Village village, class_3222 player) {
      return village.getPopulation() >= this.population;
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public class_5250 getTranslatable() {
      return class_2561.method_43469("task.population", new Object[]{this.population});
   }
}
