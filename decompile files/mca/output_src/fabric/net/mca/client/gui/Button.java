package fabric.net.mca.client.gui;

import fabric.net.mca.entity.interaction.Constraint;
import java.util.Set;
import java.util.stream.Stream;

public final class Button {
   private final String identifier;
   private final String align;
   private final int x;
   private final int col;
   private static final int COL_WIDTH = 66;
   private final int y;
   private final int row;
   private static final int ROW_HEIGHT = 21;
   private final int width;
   private final int height;
   private final boolean notifyServer;
   private final boolean targetServer;
   private final String constraints;
   private final boolean hideOnFail;
   private final boolean isInteraction;

   public String identifier() {
      return this.identifier;
   }

   public String align() {
      return this.align == null ? "top_right" : this.align;
   }

   public int x() {
      return this.x + this.col * 66;
   }

   public int y() {
      return this.y + this.row * 21;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public boolean notifyServer() {
      return this.notifyServer;
   }

   public boolean targetServer() {
      return this.targetServer;
   }

   public String constraints() {
      return this.constraints;
   }

   public boolean hideOnFail() {
      return this.hideOnFail;
   }

   public boolean isInteraction() {
      return this.isInteraction;
   }

   public Button(
      String identifier,
      String align,
      int x,
      int col,
      int y,
      int row,
      int width,
      int height,
      boolean notifyServer,
      boolean targetServer,
      String constraints,
      boolean hideOnFail,
      boolean isInteraction
   ) {
      this.identifier = identifier;
      this.align = align;
      this.x = x;
      this.col = col;
      this.y = y;
      this.row = row;
      this.width = width;
      this.height = height;
      this.notifyServer = notifyServer;
      this.targetServer = targetServer;
      this.constraints = constraints;
      this.hideOnFail = hideOnFail;
      this.isInteraction = isInteraction;
   }

   public Stream<Constraint> getConstraints() {
      return Constraint.fromStringList(this.constraints).stream();
   }

   public boolean isValidForConstraint(Set<Constraint> constraints) {
      return this.getConstraints().allMatch(constraints::contains);
   }
}
