package esa.mo.nmf.apps;
import java.util.logging.Logger;

import java.util.logging.Level;

import esa.mo.nmf.CloseAppListener;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

public class DataPollingAppMCAdapter extends MonitorAndControlNMFAdapter{
    private static final Logger LOGGER = Logger.getLogger(MonitorAndControlNMFAdapter.class.getName());
    
    private DataPollingThreadHandler dataPollingThreadHandler;
    
    public DataPollingAppMCAdapter() {
        this.dataPollingThreadHandler = new DataPollingThreadHandler(this);
    }
    
    public DataPollingThreadHandler getDataHandler() {
        return dataPollingThreadHandler;
    }
    
    public void startDataPolling() throws Exception{
        dataPollingThreadHandler.startDataPollingThreads();
    }

    public void stopDataPolling() throws Exception{
        dataPollingThreadHandler.stopDataPollingThreads();
    }

    //----------------------------------- NMF components --------------------------------------------
    
    /**
    * The application's NMF provider.
    */
    private NanoSatMOConnectorImpl connector;
    
    /**
    * The application's NMF consumer (consuming supervisor).
    */
    private SpaceMOApdapterImpl supervisorSMA;
    
    /**
    * Returns the NMF connector, the application's NMF provider.
    * 
    * @return the connector
    */
    public NanoSatMOConnectorImpl getConnector() {
        return connector;
    }
    
    /**
     * Sets the NMF connector, the application's NMF provider.
     * 
     * @param the connector to set
     */
    public void setConnector(NanoSatMOConnectorImpl connector) {
         this.connector = connector;
        
         // define application behavior when closed
         this.connector.setCloseAppListener(new CloseAppListener() {
             @Override
             public Boolean onClose() {
                 return DataPollingAppMCAdapter.this.onClose(true);
             }
        });
    }
    
    /**
     * Gracefully closes the application.
     *
     * @param requestFromUser flag indicating if request comes from user
     * @return true in case of success, false otherwise
     */
    public boolean onClose(boolean requestFromUser) {
        boolean success = true;
        
        // signal the aggregation threads to exit their loops
        dataPollingThreadHandler.stopDataPollingThreads();
        
        // close supervisor consumer connections
        supervisorSMA.closeConnections();
        
        LOGGER.log(Level.INFO, "Closed application successfully: " + success);
        
        // if experiment is over
        if(!requestFromUser) {
            System.exit(success ? 0 : 1);
        }
        
        return success;
    }
    
    
    /**
     * Returns the application's NMF consumer (consuming supervisor).
     * 
     * @return the consumer
     */
    public SpaceMOApdapterImpl getSupervisorSMA() {
        return supervisorSMA;
    }
    
    /**
     * Sets the the application's NMF consumer (consuming supervisor).
     * 
     * @param the consumer to set
     */
    public void setSupervisorSMA(SpaceMOApdapterImpl supervisorSMA) {
        this.supervisorSMA = supervisorSMA;
    }
}