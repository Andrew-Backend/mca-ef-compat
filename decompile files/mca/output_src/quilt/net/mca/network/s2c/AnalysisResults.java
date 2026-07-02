package quilt.net.mca.network.s2c;

import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.resources.data.analysis.Analysis;

public class AnalysisResults implements Message {
   private static final long serialVersionUID = 2451914344295985363L;
   public final Analysis<?> analysis;

   public AnalysisResults(Analysis<?> analysis) {
      this.analysis = analysis;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleSkinListResponse(this);
   }
}
