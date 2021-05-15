package esa.mo.nmf.apps;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.apps.DatapoolXmlManager.DatapoolParamTypes;


public class ParameterSubscriptionThread extends Thread {
    
    private static final Logger LOGGER = Logger.getLogger(ParameterSubscriptionThread.class.getName());
    
    // log prefix
    private String logPrefix;
    
    // thread id
    private int id;
    
    // parameter names
    List<String> paramNames;
    
    private AggregationHandler aggregationHandler;
    
    /**
     * Make default constructor inaccessible.
     */
    @SuppressWarnings("unused")
    private ParameterSubscriptionThread(){}
    
    /**
     * 
     * @param adapter the adapter
     * @param id the id of the app simulation
     * @param iterations the number of run loop iterations in the app simulation
     * @param interval the interval between each loop iteration in the app simulation (in milliseconds)
     */
    ParameterSubscriptionThread(AppMCAdapter adapter, int id, int iterations, int interval) throws Exception{
        this.id = id;

        // interval in seconds
        double intervalsInSeconds = interval / 1000.0;
        
        // log message prefix
        this.logPrefix = Utils.generateLogPrefix(id);

        // define the list of param names that will be fetched
        if(PropertiesManager.getInstance().isAggregationParamsGetNamesPredefined(id)) {
            // param names have been predefined in the config.properties file as a comma separated list of param names
            this.paramNames = PropertiesManager.getInstance().getAggregationParamsGetNames(id);
            
        }else {
            // param names are fetched from the datapool.xml file
            
            // get the data type of the parameters that we want to fetch
            String paramTypeStr =  PropertiesManager.getInstance().getAggregationParamsGetType(id);
            DatapoolParamTypes paramType = DatapoolXmlManager.getinstance().getDatapoolParamTypeFromString(paramTypeStr);
            
            // get how many parameters to fetch
            int paramGetCount = PropertiesManager.getInstance().getAggregationParamsGetCount(id);
            
            // get the param names from the datapool.xml file
            this.paramNames = DatapoolXmlManager.getinstance().getParamNames(paramGetCount, paramType);
        }
        
        // set the param name list in the application manager so that it can be later accessed from within the AggregationWriter
        ApplicationManager.getInstance().setParamNames(id, this.paramNames);
        
        // instanciate the aggregation handler
        this.aggregationHandler = new AggregationHandler(adapter, id, intervalsInSeconds, this.paramNames);
    }

    @Override
    public void run() { 
        try {
            // log start of the thread
            LOGGER.log(Level.INFO, this.logPrefix + "Starting thread to fetch params: " + String.join(", ", this.paramNames));
            
            // subscribe to parameter data provisioning service
            this.aggregationHandler.toggleSupervisorParametersSubscription(true);

            // break out of the thread loop when iteration is complete 
            while(!ApplicationManager.getInstance().isDataFetchingComplete(this.id)){
                
                // sleep
                Thread.sleep(1000);
                
                // also break out the thread loop if the application stop is triggered by the user
                if(!ApplicationManager.getInstance().isDataPollingThreadsKeepAlive()) {
                    // FIXME: flush print writer data that has not been written and close the print writer?
                    break;
                }
            }
            
            // unsubscribe to parameter data provisioning service
            this.aggregationHandler.toggleSupervisorParametersSubscription(false);
 
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, this.logPrefix + e.getMessage(), e);
        }
    }
}
