package esa.mo.nmf.apps;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

/**
 * Datapool Polling App
 * 
 * @author Georges Labreche
 */
public final class DataPollingApp{
    private static final Logger LOGGER = Logger.getLogger(DataPollingApp.class.getName());
    
    // app Monitor and Control (M&C) Adapter
    private DataPollingAppMCAdapter adapter;
    
    private DataPollingApp() throws Exception{
        
        
        
        // initialize M&C interface
        adapter = new DataPollingAppMCAdapter();

        // initialize application's NMF provider
        NanoSatMOConnectorImpl connector = new NanoSatMOConnectorImpl();
        connector.init(adapter);

        // initialize application's NMF consumer (consuming the supervisor)
        SpaceMOApdapterImpl supervisorSMA =
            SpaceMOApdapterImpl.forNMFSupervisor(connector.readCentralDirectoryServiceURI());

        // once all initialized, pass them to the M&C interface that handles the application's logic
        adapter.setConnector(connector);
        adapter.setSupervisorSMA(supervisorSMA);
        
        adapter.getSupervisorSMA().addDataReceivedListener(new AggregationWriter());

        LOGGER.log(Level.INFO, "Datapool Polling App initialized.");
    }
    
    /**
     * Starts the application.
     */
    public void start() throws Exception{
        // logging
        LOGGER.log(Level.INFO, "Starting the Datapool Polling App.");
        
        // start simulation
        adapter.startDataPolling();
    }
    
    
    /**
     * Main command line entry point.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception If there is an error
     */
    public static void main(final String args[]) throws Exception {
        try{
            // create and start the app
            DataPollingApp app = new DataPollingApp();
            app.start();
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
