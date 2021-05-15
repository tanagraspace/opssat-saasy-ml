package esa.mo.nmf.apps;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ParameterSubscriptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ParameterSubscriptionHandler.class.getName());
    
    /**
     * M&C interface of the application.
     */
    private final AppMCAdapter adapter;
    
    public ParameterSubscriptionHandler(AppMCAdapter adapter) {
        this.adapter = adapter;
    }
    
    
    /**
     * Starts the learning.
     *
     * @return null if it was successful. If not null, then the returned value holds the error number
     */
    public void startParameterSubscriptionThreads() throws Exception {
        LOGGER.log(Level.INFO, "Starting parameter subscription threads.");
      
        // get numbers of apps to simulate
        String appCountStr = PropertiesManager.getInstance().getProperty(PropertiesManager.PROPS_AGGREGATIONS);
        int appCount = Integer.parseInt(appCountStr);
        
        // instanciate app simulations as threads
        for(int threadId = 1; threadId <= appCount; threadId++) {
            
            // fetch app simulation iterations and interval
            int iterations = PropertiesManager.getInstance().getAggregationIterations(threadId);
            int interval = PropertiesManager.getInstance().getAggregationInterval(threadId);
              
            // start app simulation
            ParameterSubscriptionThread appThread = new ParameterSubscriptionThread(this.adapter, threadId, iterations, interval);
            appThread.start();
        }
    }
    
    public void stopParameterSubscriptionThreads() {
        ApplicationManager.getInstance().setDataPollingThreadsKeepAlive(false);
    }
}
