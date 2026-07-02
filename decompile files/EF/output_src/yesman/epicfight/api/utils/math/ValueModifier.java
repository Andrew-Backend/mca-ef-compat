package yesman.epicfight.api.utils.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface ValueModifier {
   Codec<ValueModifier.Unified> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.FLOAT.fieldOf("adder").forGetter(ValueModifier.Unified::adder),
            Codec.FLOAT.fieldOf("multiplier").forGetter(ValueModifier.Unified::multiplier),
            Codec.FLOAT.fieldOf("setter").forGetter(ValueModifier.Unified::setter)
         )
         .apply(instance, ValueModifier.Unified::new)
   );

   void attach(ValueModifier.ResultCalculator var1);

   static ValueModifier adder(float value) {
      return new ValueModifier.Adder(value);
   }

   static ValueModifier multiplier(float value) {
      return new ValueModifier.Multiplier(value);
   }

   static ValueModifier setter(float arg) {
      return new ValueModifier.Setter(arg);
   }

   static ValueModifier.ResultCalculator calculator() {
      return new ValueModifier.ResultCalculator();
   }

   record Adder(float adder) implements ValueModifier {
      @Override
      public void attach(ValueModifier.ResultCalculator calculator) {
         calculator.add = calculator.add + this.adder;
      }
   }

   record Multiplier(float multiplier) implements ValueModifier {
      @Override
      public void attach(ValueModifier.ResultCalculator calculator) {
         calculator.multiply = calculator.multiply * this.multiplier;
      }
   }

   class ResultCalculator implements ValueModifier {
      private float set = Float.NaN;
      private float add = 0.0F;
      private float multiply = 1.0F;

      public ValueModifier.ResultCalculator attach(ValueModifier valueModifier) {
         valueModifier.attach(this);
         return this;
      }

      @Override
      public void attach(ValueModifier.ResultCalculator calculator) {
         if (Float.isNaN(calculator.set)) {
            calculator.set = this.set;
         } else if (!Float.isNaN(this.set)) {
            calculator.set = Math.min(calculator.set, this.set);
         }

         calculator.add = calculator.add + this.add;
         calculator.multiply = calculator.multiply * this.multiply;
      }

      public ValueModifier toValueModifier() {
         if (Float.isNaN(this.set)) {
            if (Float.compare(this.add, 0.0F) == 0 && Float.compare(this.multiply, 1.0F) != 0) {
               return new ValueModifier.Multiplier(this.multiply);
            }

            if (Float.compare(this.add, 0.0F) != 0 && Float.compare(this.multiply, 1.0F) == 0) {
               return new ValueModifier.Adder(this.add);
            }
         } else if (Float.compare(this.add, 0.0F) == 0 && Float.compare(this.multiply, 1.0F) == 0) {
            return new ValueModifier.Setter(this.set);
         }

         return new ValueModifier.Unified(this.set, this.add, this.multiply);
      }

      public void set(float f) {
         this.set = f;
      }

      public void add(float f) {
         this.add = this.add + this.add;
      }

      public void multiply(float f) {
         this.multiply *= f;
      }

      public float getResult(float baseValue) {
         float result = baseValue;
         if (!Float.isNaN(this.set)) {
            result = this.set;
         }

         result += this.add;
         return result * this.multiply;
      }
   }

   record Setter(float setter) implements ValueModifier {
      @Override
      public void attach(ValueModifier.ResultCalculator calculator) {
         if (Float.isNaN(calculator.set)) {
            calculator.set = this.setter;
         } else if (!Float.isNaN(this.setter)) {
            calculator.set = Math.min(calculator.set, this.setter);
         }
      }
   }

   record Unified(float adder, float multiplier, float setter) implements ValueModifier {
      @Override
      public void attach(ValueModifier.ResultCalculator calculator) {
         if (Float.isNaN(calculator.set)) {
            calculator.set = this.setter;
         } else if (!Float.isNaN(this.setter)) {
            calculator.set = Math.min(calculator.set, this.setter);
         }

         calculator.add = calculator.add + this.adder;
         calculator.multiply = calculator.multiply * this.multiplier;
      }
   }
}
