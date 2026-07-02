package forge.net.mca.resources.data.tasks;

import com.google.gson.JsonObject;
import forge.net.mca.server.world.data.Village;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class PopulationTask extends Task {
   private static final long serialVersionUID = 5252203744206810361L;
   private final int population;

   public PopulationTask(int population) {
      super("population_" + population);
      this.population = population;
   }

   public PopulationTask(JsonObject json) {
      this(GsonHelper.m_13927_(json, "population"));
   }

   @Override
   public boolean isCompleted(Village village, ServerPlayer player) {
      return village.getPopulation() >= this.population;
   }

   @Override
   public boolean isRequired() {
      return true;
   }

   @Override
   public MutableComponent getTranslatable() {
      return Component.m_237110_("task.population", new Object[]{this.population});
   }
}
