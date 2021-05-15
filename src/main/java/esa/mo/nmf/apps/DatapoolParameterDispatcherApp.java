package esa.mo.nmf.apps;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

/**
 * The main App class
 * 
 * @author Georges Labreche
 */
public final class DatapoolParameterDispatcherApp{
    private static final Logger LOGGER = Logger.getLogger(DatapoolParameterDispatcherApp.class.getName());
    
    // app Monitor and Control (M&C) Adapter
    private AppMCAdapter adapter;
    
    private DatapoolParameterDispatcherApp() throws Exception{
        
        // initialize M&C interface
        adapter = new AppMCAdapter();

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

        LOGGER.log(Level.INFO, "Initialized the app.");
    }
    
    /**
     * Starts the application.
     */
    public void start() throws Exception{
        // logging
        LOGGER.log(Level.INFO, "Starting the app.");
        
        // start simulation
        adapter.startFetchingParameters();
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
            DatapoolParameterDispatcherApp app = new DatapoolParameterDispatcherApp();
            app.start();
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
