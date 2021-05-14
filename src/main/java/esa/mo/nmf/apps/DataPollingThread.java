package esa.mo.nmf.apps;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.ccsds.moims.mo.mal.structures.UInteger;

public class DataPollingThread extends Thread {
    
    private static final Logger LOGGER = Logger.getLogger(DataPollingThread.class.getName());
    
    private String logPrefix;
    
    private int id;
    private int iterations;
    private int interval;
    private List<String> paramsToGet;
    
    private DataPollingAggregationHandler dataGetHandler;
    
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
    DataPollingThread(DataPollingAppMCAdapter adapter, int id, int iterations, int interval, List<String> paramsToGet) {
        this.id = id;
        this.iterations = iterations;
        this.interval = interval;
        this.paramsToGet = paramsToGet;
        
        this.logPrefix = "[App #" + this.id + "] ";
        
        // data handler for this simulated app instance
        double intervalsInSeconds = interval / 1000;
        this.dataGetHandler = new DataPollingAggregationHandler(adapter, id, intervalsInSeconds, paramsToGet);
        
        this.dataGetHandler.toggleSupervisorParametersSubscription(true);
    }

    @Override
    public void run() {
        
        // error code to check for errors
        UInteger errorCode = null;
        
        try {
            
            // log start of simulation app
            LOGGER.log(Level.INFO, this.logPrefix + "Starting App, Fetching: " + String.join(", ", this.paramsToGet));
            
            // subscribe to parameter data provisioning service
            errorCode = this.dataGetHandler.toggleSupervisorParametersSubscription(true);
            
            // check if no error
            if(errorCode != null) {
                LOGGER.log(Level.SEVERE, this.logPrefix + "Error Code " + errorCode.getValue() + ": Failed to subscribe to parameters service.");
                return;
            }
            
            for (int i = 1; i <= this.iterations; i++) {
                
                LOGGER.log(Level.INFO,this.logPrefix + "Iteration: " + i + "/" + this.iterations);

                // check if we are stopping the app B\before waiting to make the next request
                if(!ApplicationManager.getInstance().isDataPollingThreadsKeepAlive()) {
                    break;
                }
                
                // wait for a bit
                Thread.sleep(this.interval);
                Pair<Long, Map<String, String>> parametersValues = this.dataGetHandler.getParametersValues();
                
                Long timestamp = parametersValues.getLeft();
                
                // build CSV row string of fetched values
                StringBuffer line = new StringBuffer();
                line.append(String.format("%.3f", timestamp / 1000.0));
                line.append(",");
                
                for (String param : this.dataGetHandler.getParametersNames()) {
                    String value = parametersValues.getRight().get(param);
                    
                    // skip samples with at least one null value
                    if (DataPollingAggregationHandler.PARAMS_DEFAULT_VALUE.equals(value)) {
                        return;
                        }
                  
                    line.append(value);
                    line.append(",");
                }
                
                // TODO: Write into CSV file.
                line.replace(line.length() - 1, line.length(), "\n");
                
                
                /**
                 * Check again if we are stopping the app before fetching data pool parameters.
                 * Do this in case the sleep was long.
                 * 
                 * Note that the best way to stop a thread with long waits/sleeps is to use Thread.interrupt, see here:
                 * https://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
                 */
                if(!ApplicationManager.getInstance().isDataPollingThreadsKeepAlive()) {
                    break;
                }
            }
            
            // unsubscribe to parameter data provisioning service
            errorCode = this.dataGetHandler.toggleSupervisorParametersSubscription(false);
            
            // check if no error
            if(errorCode != null) {
                LOGGER.log(Level.SEVERE, this.logPrefix + "Error Code " + errorCode.getValue() + ": Failed to unsubscribe to parameters service.");
                return;
            }
                
        } catch(InterruptedException e) {
            LOGGER.log(Level.SEVERE, this.logPrefix + e.getMessage(), e);
        }
    }
}
