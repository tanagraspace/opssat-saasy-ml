package esa.mo.nmf.apps;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

/**
 * Datapool Stress Tester App
 * 
 * @author Georges Labreche
 */
public final class StressTesterApp{
    private static final Logger LOGGER = Logger.getLogger(StressTesterApp.class.getName());
    
    // app Monitor and Control (M&C) Adapter
    private StressTesterMCAdapter adapter;
    
    private StressTesterApp() {
        // initialize M&C interface
        adapter = new StressTesterMCAdapter();

        // initialize application's NMF provider
        NanoSatMOConnectorImpl connector = new NanoSatMOConnectorImpl();
        connector.init(adapter);

        // initialize application's NMF consumer (consuming the supervisor)
        SpaceMOApdapterImpl supervisorSMA =
            SpaceMOApdapterImpl.forNMFSupervisor(connector.readCentralDirectoryServiceURI());

        // once all initialized, pass them to the M&C interface that handles the application's logic
        adapter.setConnector(connector);
        adapter.setSupervisorSMA(supervisorSMA);

        LOGGER.log(Level.INFO, "Stress Tester App initialized.");
    }
    
    /**
     * Starts the application.
     */
    public void start() throws Exception{
        // logging
        LOGGER.log(Level.INFO, "Starting the Datapool Stress Tester App.");
        
        // start simulation
        adapter.startSimulation();
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
            StressTesterApp app = new StressTesterApp();
            app.start();
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
