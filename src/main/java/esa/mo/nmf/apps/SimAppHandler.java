package esa.mo.nmf.apps;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.apps.DatapoolXmlManager.DatapoolParamTypes;

public class SimAppHandler {

    private static final Logger LOGGER = Logger.getLogger(SimAppHandler.class.getName());
    
    /**
     * M&C interface of the application.
     */
    private final StressTesterMCAdapter adapter;
    
    public SimAppHandler(StressTesterMCAdapter adapter) {
        this.adapter = adapter;
    }
    
    
    /**
     * Starts the learning.
     *
     * @return null if it was successful. If not null, then the returned value holds the error number
     */
    public void startSimulation() throws Exception {
        LOGGER.log(Level.INFO, "Starting app simulations...");
        
        // Get numbers of apps to simulate.
        String appCountStr = PropertiesManager.getinstance().getProperty(PropertiesManager.PROPS_APP_COUNT);
        int appCount = Integer.parseInt(appCountStr);
        
        // Instanciate app simulations as threads.
        for(int appId = 1; appId <= appCount; appId++) {
            
            // Fetch app simulation properties. 
            int iterations = PropertiesManager.getinstance().getAppSimIterations(appId);
            int intervals = PropertiesManager.getinstance().getAppSimInterval(appId);
            int paramGetCount = PropertiesManager.getinstance().getAppSimParamsGetCount(appId);
                    
            // Param names to be fetched by the app simulation.
            List<String> paramsToGet = DatapoolXmlManager.getinstance().getParamNames(paramGetCount, DatapoolParamTypes.Float);
                    
            // Start app simulation.
            SimAppThread appThread = new SimAppThread(this.adapter, appId, iterations, intervals, paramsToGet);
            appThread.start();
        }
    }
    
    public void stopSimulation() {
        ApplicationManager.getInstance().setSimKeepAlive(false);
    }
}
