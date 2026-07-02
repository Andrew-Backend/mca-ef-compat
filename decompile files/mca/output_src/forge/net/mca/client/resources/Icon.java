package forge.net.mca.client.resources;

public final class Icon {
   public static final Icon EMPTY = new Icon(0, 0, 0, 0);
   private final int u;
   private final int v;
   private final int x;
   private final int y;

   public int u() {
      return this.u;
   }

   public int v() {
      return this.v;
   }

   public int x() {
      return this.x;
   }

   public int y() {
      return this.y;
   }

   public Icon(int u, int v, int x, int y) {
      this.u = u;
      this.v = v;
      this.x = x;
      this.y = y;
   }
}
