package esa.mo.nmf.apps;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.apps.DatapoolXmlManager.DatapoolParamTypes;


public class DataPollingThread extends Thread {
    
    private static final Logger LOGGER = Logger.getLogger(DataPollingThread.class.getName());
    
    // log prefix
    private String logPrefix;
    
    // thread id
    private int id;
    
    // parameter names
    List<String> paramNames;
    
    private DataPollingAggregationHandler dataPollingAggregationHandler;
    
    /**
     * Make default constructor inaccessible.
     */
    @SuppressWarnings("unused")
    private DataPollingThread(){}
    
    /**
     * 
     * @param adapter the adapter
     * @param id the id of the app simulation
     * @param iterations the number of run loop iterations in the app simulation
     * @param interval the interval between each loop iteration in the app simulation (in milliseconds)
     * @param paramsToGet
     */
    DataPollingThread(DataPollingAppMCAdapter adapter, int id, int iterations, int interval) throws Exception{
        this.id = id;

        // interval in seconds
        double intervalsInSeconds = interval / 1000;
        
        // log message prefix
        this.logPrefix = Utils.generateLogPrefix(id);
        
        // fetch the parameter types
        String paramTypeStr =  PropertiesManager.getInstance().getThreadParamsGetType(id);
        DatapoolParamTypes paramType = DatapoolXmlManager.getinstance().getDatapoolParamTypeFromString(paramTypeStr);
        
        // fetch how many parameters to get
        int paramGetCount = PropertiesManager.getInstance().getThreadParamsGetCount(id);
        
        // param names to be fetched by the app simulation
        this.paramNames = DatapoolXmlManager.getinstance().getParamNames(paramGetCount, paramType);
        
        // set the param names for this thread in the application manager so that they can be accessed from the AggregationWriteer
        ApplicationManager.getInstance().setParamNames(id, this.paramNames);
        
        // instanciate the aggregation handler
        this.dataPollingAggregationHandler = new DataPollingAggregationHandler(adapter, id, intervalsInSeconds, this.paramNames);
    }

    @Override
    public void run() { 
        try {
            // log start of simulation app
            LOGGER.log(Level.INFO, this.logPrefix + "Starting app to fetch: " + String.join(", ", this.paramNames));
            
            // subscribe to parameter data provisioning service
            this.dataPollingAggregationHandler.toggleSupervisorParametersSubscription(true);

            // thread loop
            while(ApplicationManager.getInstance().isDataPollingThreadsKeepAlive()){
                Thread.sleep(1000);
                
                // break out of the loop with iteration is complete 
                if(ApplicationManager.getInstance().isDataFetchingComplete(this.id)) {
                    break;
                }
               
            }
            
            // unsubscribe to parameter data provisioning service
            this.dataPollingAggregationHandler.toggleSupervisorParametersSubscription(false);
 
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, this.logPrefix + e.getMessage(), e);
        }
    }
}
