package esa.mo.nmf.apps;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mal.provider.MALInteraction;

import java.util.logging.Level;

import esa.mo.nmf.CloseAppListener;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

public class StressTesterMCAdapter extends MonitorAndControlNMFAdapter{
    private static final Logger LOGGER = Logger.getLogger(MonitorAndControlNMFAdapter.class.getName());
    
    private SimAppHandler simulationHandler;
    
    public StressTesterMCAdapter() {
        this.simulationHandler = new SimAppHandler(this);
    }
    
    public SimAppHandler getDataHandler() {
        return simulationHandler;
    }
    
    public void startSimulation() throws Exception{
        simulationHandler.startSimulation();
    }

    public void stopSimulation() throws Exception{
        simulationHandler.stopSimulation();
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
        
         // Define application behavior when closed
         this.connector.setCloseAppListener(new CloseAppListener() {
             @Override
             public Boolean onClose() {
                 return StressTesterMCAdapter.this.onClose(true);
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
        
        // Stop fetching data in supervisor
        /*
        if (dataHandler.toggleSupervisorParametersSubscription(false) != null) {
            success = false;
        }*/
        
        
        // Signal the simulation threads to exit their loops.
        // FIXME: Simulation thread may still be sleeping before it gets the signal. Might have to use Thread.interrupt.
        simulationHandler.stopSimulation();
        
        
        // Close supervisor consumer connections
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