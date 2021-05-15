package esa.mo.nmf.apps;
import java.util.logging.Logger;

import java.util.logging.Level;

import esa.mo.nmf.CloseAppListener;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

public class AppMCAdapter extends MonitorAndControlNMFAdapter{
    private static final Logger LOGGER = Logger.getLogger(MonitorAndControlNMFAdapter.class.getName());
    
    private ParameterSubscriptionHandler parameterSubscriptionHandler;
    
    public AppMCAdapter() {
        this.parameterSubscriptionHandler = new ParameterSubscriptionHandler(this);
    }
    
    public ParameterSubscriptionHandler getParameterSubscriptionHandler() {
        return parameterSubscriptionHandler;
    }
    
    public void startFetchingParameters() throws Exception{
        parameterSubscriptionHandler.startParameterSubscriptionThreads();
    }

    public void stopFetchingParameters() throws Exception{
        parameterSubscriptionHandler.stopParameterSubscriptionThreads();
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
                 return AppMCAdapter.this.onClose(true);
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
        
        try {
        
            // signal the parameter subscription thread to exit their loops
            parameterSubscriptionHandler.stopParameterSubscriptionThreads();
            
            // close supervisor consumer connections
            supervisorSMA.closeConnections();
            
            LOGGER.log(Level.INFO, "Closed application successfully.");
            
            // if experiment is over
            if(!requestFromUser) {
                System.exit(0);
            }
        
        }catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to close application successfully.", e);
            return false;
        }
        
        return true;
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