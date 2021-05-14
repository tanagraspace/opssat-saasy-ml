package esa.mo.nmf.apps;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataPollingThreadHandler {

    private static final Logger LOGGER = Logger.getLogger(DataPollingThreadHandler.class.getName());
    
    /**
     * M&C interface of the application.
     */
    private final DataPollingAppMCAdapter adapter;
    
    public DataPollingThreadHandler(DataPollingAppMCAdapter adapter) {
        this.adapter = adapter;
    }
    
    
    /**
     * Starts the learning.
     *
     * @return null if it was successful. If not null, then the returned value holds the error number
     */
    public void startDataPollingThreads() throws Exception {
        LOGGER.log(Level.INFO, "Starting data polling threads...");
      
        // get numbers of apps to simulate
        String appCountStr = PropertiesManager.getInstance().getProperty(PropertiesManager.PROPS_THREADS);
        int appCount = Integer.parseInt(appCountStr);
        
        // instanciate app simulations as threads
        for(int threadId = 1; threadId <= appCount; threadId++) {
            
            // fetch app simulation iterations and interval
            int iterations = PropertiesManager.getInstance().getThreadIterations(threadId);
            int interval = PropertiesManager.getInstance().getThreadInterval(threadId);
              
            // start app simulation
            DataPollingThread appThread = new DataPollingThread(this.adapter, threadId, iterations, interval);
            appThread.start();
        }
    }
    
    public void stopDataPollingThreads() {
        ApplicationManager.getInstance().setDataPollingThreadsKeepAlive(false);
    }
}
