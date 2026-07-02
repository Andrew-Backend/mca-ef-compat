package fabric.net.mca.entity.ai;

import fabric.net.mca.Config;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CDataParameter;
import fabric.net.mca.util.network.datasync.CEnumParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1959;
import net.minecraft.class_3532;
import net.minecraft.class_5819;

public class Genetics implements Iterable<Genetics.Gene> {
   private static final Set<Genetics.GeneType> GENOMES = new HashSet<>();
   public static final Genetics.GeneType SIZE = new Genetics.GeneType("gene_size");
   public static final Genetics.GeneType WIDTH = new Genetics.GeneType("gene_width");
   public static final Genetics.GeneType BREAST = new Genetics.GeneType("gene_breast");
   public static final Genetics.GeneType MELANIN = new Genetics.GeneType("gene_melanin");
   public static final Genetics.GeneType HEMOGLOBIN = new Genetics.GeneType("gene_hemoglobin");
   public static final Genetics.GeneType EUMELANIN = new Genetics.GeneType("gene_eumelanin");
   public static final Genetics.GeneType PHEOMELANIN = new Genetics.GeneType("gene_pheomelanin");
   public static final Genetics.GeneType SKIN = new Genetics.GeneType("gene_skin");
   public static final Genetics.GeneType FACE = new Genetics.GeneType("gene_face");
   public static final Genetics.GeneType VOICE = new Genetics.GeneType("gene_voice");
   public static final Genetics.GeneType VOICE_TONE = new Genetics.GeneType("gene_voice_tone");
   private static final CEnumParameter<Gender> GENDER = CParameter.create("gender", Gender.UNASSIGNED);
   private class_5819 random = class_5819.method_43047();
   private final Map<Genetics.GeneType, Genetics.Gene> genes = new HashMap<>();
   private final VillagerLike<?> entity;

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      GENOMES.forEach(g -> builder.addAll(g.getParam()));
      return builder.addAll(GENDER);
   }

   public Genetics(VillagerLike<?> entity) {
      this.entity = entity;
   }

   public float getVerticalScaleFactor() {
      return 0.75F + this.getGene(SIZE) / 2.0F;
   }

   public float getHorizontalScaleFactor() {
      return 0.75F + this.getGene(WIDTH) / 2.0F;
   }

   public void setGender(Gender gender) {
      this.entity.setTrackedValue(GENDER, gender);
   }

   public Gender getGender() {
      return this.entity.getTrackedValue(GENDER);
   }

   public float getBreastSize() {
      return this.getGender() == Gender.FEMALE ? this.getGene(BREAST) : 0.0F;
   }

   @Override
   public Iterator<Genetics.Gene> iterator() {
      return this.genes.values().iterator();
   }

   public void setGene(Genetics.GeneType type, float value) {
      this.getGenome(type).set(value);
   }

   public float getGene(Genetics.GeneType type) {
      return this.getGenome(type).get();
   }

   public Genetics.Gene getGenome(Genetics.GeneType type) {
      return this.genes.computeIfAbsent(type, x$0 -> new Genetics.Gene(x$0));
   }

   public void randomize() {
      for (Genetics.GeneType type : GENOMES) {
         this.getGenome(type).randomize();
      }

      this.setGene(SIZE, this.centeredRandom());
      this.setGene(WIDTH, this.centeredRandom());
      float temp = ((class_1959)this.entity.asEntity().method_37908().method_23753(this.entity.asEntity().method_24515()).comp_349()).method_8712();
      if (this.random.method_43057() < Config.getInstance().geneticImmigrantChance) {
         temp = this.random.method_43057() * 2.0F - 0.5F;
      }

      float height = this.entity.asEntity().method_24515().method_10264();
      height -= this.entity.asEntity().method_37908().method_8615();
      height /= 128.0F;
      this.setGene(MELANIN, class_3532.method_15363(this.temperatureBaseRandom(temp) - height * 0.2F, 0.0F, 1.0F));
      this.setGene(HEMOGLOBIN, class_3532.method_15363(this.temperatureBaseRandom(temp) * 0.5F + height * 0.5F, 0.0F, 1.0F));
      this.setGene(EUMELANIN, this.random.method_43057());
      this.setGene(PHEOMELANIN, this.random.method_43057());
   }

   private float centeredRandom() {
      return Math.min(1.0F, Math.max(0.0F, (this.random.method_43057() - 0.5F) * (this.random.method_43057() - 0.5F) + 0.5F));
   }

   private float temperatureBaseRandom(float temp) {
      return (this.random.method_43057() - 0.5F) * 0.35F + temp * 0.4F + 0.1F;
   }

   public void combine(Genetics mother, Genetics father) {
      for (Genetics.GeneType type : GENOMES) {
         this.getGenome(type).mutate(mother, father);
      }
   }

   public void combine(Genetics mother, Genetics father, long seed) {
      class_5819 old = this.random;
      this.random = class_5819.method_43049(seed);
      this.combine(mother, father);
      this.random = old;
   }

   public class Gene {
      private final Genetics.GeneType type;

      public Gene(Genetics.GeneType type) {
         this.type = type;
      }

      public Genetics.GeneType getType() {
         return this.type;
      }

      public float get() {
         return Genetics.this.entity.getTrackedValue(this.type.parameter);
      }

      public void set(float value) {
         Genetics.this.entity.setTrackedValue(this.type.parameter, value);
      }

      public void randomize() {
         this.set(Genetics.this.random.method_43057());
      }

      public void mutate(Genetics mother, Genetics father) {
         float m = mother.getGene(this.type);
         float f = father.getGene(this.type);
         float interpolation = Genetics.this.random.method_43057();
         float mutation = (Genetics.this.random.method_43057() - 0.5F) * 0.2F;
         float g = m * interpolation + f * (1.0F - interpolation) + mutation;
         this.set((float)Math.min(1.0, Math.max(0.0, g)));
      }
   }

   public static class GeneType implements Comparable<Genetics.GeneType> {
      private final String key;
      private final CDataParameter<Float> parameter;

      public GeneType(String key) {
         this.key = key;
         this.parameter = CParameter.create(key, 0.5F);
         Genetics.GENOMES.add(this);
      }

      public String key() {
         return this.key;
      }

      public String getTranslationKey() {
         return this.key().replace("_", ".");
      }

      public CDataParameter<Float> getParam() {
         return this.parameter;
      }

      public int compareTo(Genetics.GeneType o) {
         return this.key().compareTo(o.key());
      }

      @Override
      public int hashCode() {
         return this.key.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof Genetics.GeneType geneType && geneType.key().equals(this.key());
      }
   }
}
