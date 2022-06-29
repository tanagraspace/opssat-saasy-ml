package esa.mo.nmf.apps;
import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

import esa.mo.nmf.apps.verticles.MainVerticle;

/**
 * The main App class
 * 
 * @author Georges Labreche
 */
public final class SaaSyMLApp {
    private static final Logger LOGGER = Logger.getLogger(SaaSyMLApp.class.getName());
    
    private SaaSyMLApp() throws Exception {

        LOGGER.log(Level.INFO, "Initializing the app.");

        // initialize application's NMF provider
        NanoSatMOConnectorImpl connector = new NanoSatMOConnectorImpl();
        connector.init(AppMCAdapter.getInstance());

        // initialize application's NMF consumer (consuming the supervisor)
        SpaceMOApdapterImpl supervisorSMA =
            SpaceMOApdapterImpl.forNMFSupervisor(connector.readCentralDirectoryServiceURI());

        // once all initialized, pass them to the M&C interface that handles the application's logic
        AppMCAdapter.getInstance().setConnector(connector);
        AppMCAdapter.getInstance().setSupervisorSMA(supervisorSMA);
        
        // register data received listener
        AppMCAdapter.getInstance().getSupervisorSMA().addDataReceivedListener(new AggregationWriter());
    }
   
    
    /**
     * Starts the application.
     */
    public void start() throws Exception {
        // logging
        LOGGER.log(Level.INFO, "Starting the app.");

        LOGGER.log(Level.INFO, "Starting the Main Vertical.");
        MainVerticle mv = new MainVerticle();
        mv.start();
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
            SaaSyMLApp app = new SaaSyMLApp();
            app.start();
        }
        catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
