package esa.mo.nmf.apps;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.apps.DatapoolXmlManager.DatapoolParamTypes;

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
        String appCountStr = PropertiesManager.getinstance().getProperty(PropertiesManager.PROPS_THREADS);
        int appCount = Integer.parseInt(appCountStr);
        
        // instanciate app simulations as threads
        for(int threadId = 1; threadId <= appCount; threadId++) {
            
            // fetch app simulation iterations and interval
            int iterations = PropertiesManager.getinstance().getThreadIterations(threadId);
            int interval = PropertiesManager.getinstance().getThreadInterval(threadId);
            
            // fetch how many parameters to get
            int paramGetCount = PropertiesManager.getinstance().getThreadParamsGetCount(threadId);
            
            // fetch the parameter types
            String paramTypeStr =  PropertiesManager.getinstance().getThreadParamsGetType(threadId);
            DatapoolParamTypes paramType = DatapoolXmlManager.getinstance().getDatapoolParamTypeFromString(paramTypeStr);
                    
            // param names to be fetched by the app simulation
            List<String> paramsToGet = DatapoolXmlManager.getinstance().getParamNames(paramGetCount, paramType);
                    
            // start app simulation
            DataPollingThread appThread = new DataPollingThread(this.adapter, threadId, iterations, interval, paramsToGet);
            appThread.start();
        }
    }
    
    public void stopDataPollingThreads() {
        ApplicationManager.getInstance().setDataPollingThreadsKeepAlive(false);
    }
}
