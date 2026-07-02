package yesman.epicfight.client.gui;

import java.util.function.BiFunction;
import yesman.epicfight.api.utils.math.Vec2i;

public class ScreenCalculations {
   private static final BiFunction<Integer, Integer, Integer> ORIGIN = (screenLength, value) -> value;
   private static final BiFunction<Integer, Integer, Integer> SCREEN_EDGE = (screenLength, value) -> screenLength - value;
   private static final BiFunction<Integer, Integer, Integer> CENTER = (screenLength, value) -> screenLength / 2 + value;
   private static final BiFunction<Integer, Integer, Integer> CENTER_SAVE = (screenLength, value) -> value - screenLength / 2;
   private static final ScreenCalculations.StartCoordGetter START_HORIZONTAL = (x, y, width, height, icons, horBasis, verBasis) -> horBasis
         == ScreenCalculations.HorizontalBasis.CENTER
      ? new Vec2i(x - width * (icons - 1) / 2, y)
      : new Vec2i(x, y);
   private static final ScreenCalculations.StartCoordGetter START_VERTICAL = (x, y, width, height, icons, horBasis, verBasis) -> verBasis
         == ScreenCalculations.VerticalBasis.CENTER
      ? new Vec2i(x, y - height * (icons - 1) / 2)
      : new Vec2i(x, y);
   private static final ScreenCalculations.NextCoordGetter NEXT_HORIZONTAL = (horBasis, verBasis, oldPos, width, height) -> horBasis
            != ScreenCalculations.HorizontalBasis.LEFT
         && horBasis != ScreenCalculations.HorizontalBasis.CENTER
      ? new Vec2i(oldPos.x - width, oldPos.y)
      : new Vec2i(oldPos.x + width, oldPos.y);
   private static final ScreenCalculations.NextCoordGetter NEXT_VERTICAL = (horBasis, verBasis, oldPos, width, height) -> verBasis
            != ScreenCalculations.VerticalBasis.TOP
         && verBasis != ScreenCalculations.VerticalBasis.CENTER
      ? new Vec2i(oldPos.x, oldPos.y - height)
      : new Vec2i(oldPos.x, oldPos.y + height);

   public enum AlignDirection {
      HORIZONTAL(ScreenCalculations.START_HORIZONTAL, ScreenCalculations.NEXT_HORIZONTAL),
      VERTICAL(ScreenCalculations.START_VERTICAL, ScreenCalculations.NEXT_VERTICAL);

      public final ScreenCalculations.StartCoordGetter startCoordGetter;
      public final ScreenCalculations.NextCoordGetter nextPositionGetter;

      AlignDirection(ScreenCalculations.StartCoordGetter startCoordGetter, ScreenCalculations.NextCoordGetter nextPositionGetter) {
         this.startCoordGetter = startCoordGetter;
         this.nextPositionGetter = nextPositionGetter;
      }
   }

   public enum HorizontalBasis {
      LEFT(ScreenCalculations.ORIGIN, ScreenCalculations.ORIGIN),
      RIGHT(ScreenCalculations.SCREEN_EDGE, ScreenCalculations.SCREEN_EDGE),
      CENTER(ScreenCalculations.CENTER, ScreenCalculations.CENTER_SAVE);

      public final BiFunction<Integer, Integer, Integer> positionGetter;
      public final BiFunction<Integer, Integer, Integer> saveCoordGetter;

      HorizontalBasis(BiFunction<Integer, Integer, Integer> positionGetter, BiFunction<Integer, Integer, Integer> saveCoordGetter) {
         this.positionGetter = positionGetter;
         this.saveCoordGetter = saveCoordGetter;
      }
   }

   @FunctionalInterface
   public interface NextCoordGetter {
      Vec2i getNext(ScreenCalculations.HorizontalBasis var1, ScreenCalculations.VerticalBasis var2, Vec2i var3, int var4, int var5);
   }

   @FunctionalInterface
   public interface StartCoordGetter {
      Vec2i get(int var1, int var2, int var3, int var4, int var5, ScreenCalculations.HorizontalBasis var6, ScreenCalculations.VerticalBasis var7);
   }

   public enum VerticalBasis {
      TOP(ScreenCalculations.ORIGIN, ScreenCalculations.ORIGIN),
      BOTTOM(ScreenCalculations.SCREEN_EDGE, ScreenCalculations.SCREEN_EDGE),
      CENTER(ScreenCalculations.CENTER, ScreenCalculations.CENTER_SAVE);

      public final BiFunction<Integer, Integer, Integer> positionGetter;
      public final BiFunction<Integer, Integer, Integer> saveCoordGetter;

      VerticalBasis(BiFunction<Integer, Integer, Integer> positionGetter, BiFunction<Integer, Integer, Integer> saveCoordGetter) {
         this.positionGetter = positionGetter;
         this.saveCoordGetter = saveCoordGetter;
      }
   }
}
