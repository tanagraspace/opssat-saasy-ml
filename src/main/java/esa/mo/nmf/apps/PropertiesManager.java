package esa.mo.nmf.apps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {
    private static final Logger LOGGER = Logger.getLogger(PropertiesManager.class.getName());
    
    // path to the application configuration file
    private static final String PROPERTIES_FILE_PATH = "config.properties";
    
    // prefix on property keys
    public static final String PROPS_PREFIX = "";
    
    // number of threads to spawn
    public static final String PROPS_THREADS = PROPS_PREFIX + "threads";
    
    // number of loop iterations for a simulated app
    public static final String PROPS_THREAD_ITERATIONS = PROPS_PREFIX + "iterations";
    
    // loop iteration intervals in milliseconds
    public static final String PROPS_THREAD_INTERVAL = PROPS_PREFIX + "interval";
    
    // number of datapool parameters to get
    public static final String PROPS_THREAD_PARAMS_GET_COUNT = PROPS_PREFIX + "params.get.count";
    
    // the type of parameters to get
    public static final String PROPS_THREAD_PARAMS_GET_TYPE = PROPS_PREFIX + "params.get.type";
    
    // number of datapool parameters to set
    public static final String PROPS_THREAD_PARAMS_SET_COUNT = PROPS_PREFIX + "params.set.count";
    
    public static final String PROPS_CSV_OUTPUT_FILEPATH = PROPS_PREFIX + "params.get.output.csv";
    
    // configuration properties holder
    private Properties properties;
    
    // singleton instance
    private static PropertiesManager instance;
    
    /**
     * Hide constructor.
     */
    private PropertiesManager(){
        // load properties file
        loadProperties();
    }
    
    /**
     * Returns the PropertiesManager instance of the application.
     * Singleton.
     *
     * @return the PropertiesManager instance.
     */
    public static PropertiesManager getInstance() {
        if (instance == null) {
            instance = new PropertiesManager();
        }
        return instance;
    }
    
    /**
     * Loads the properties from the configuration file.
     */
    public void loadProperties() {
        // read and load config properties file
        try (InputStream input = new FileInputStream(PROPERTIES_FILE_PATH)) {
            this.properties = new Properties();
            this.properties.load(input);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading the application configuration properties", e);
        }

        LOGGER.log(Level.INFO, String.format("Loaded configuration properties from file %s", PROPERTIES_FILE_PATH));
    }

    /**
     * Searches for the property with the specified key in the application's properties.
     *
     * @param key The property key
     * @return The property or null if the property is not found
     */
    public String getProperty(String key) {
        String property = this.properties.getProperty(key);
        if (property == null) {
            LOGGER.log(Level.SEVERE,
            String.format("Couldn't find property with key %s, returning null", key));
        }
        return property;
    }
    
    public int getThreadCount() {
        return Integer.parseInt(getProperty(PROPS_THREADS));
    }
    
    /**
     * Get property for a thread with the given id.
     * @param threadId
     * @param key
     * @return
     */
    public String getThreadProperty(int threadId, String key) {
        String appPropKey = key + "." + threadId;
        return getProperty(appPropKey);
    }
    
    public int getThreadIterations(int threadId) {
        return Integer.parseInt(getThreadProperty(threadId, PROPS_THREAD_ITERATIONS));
    }
    
    public int getThreadInterval(int threadId) {
        return Integer.parseInt(getThreadProperty(threadId, PROPS_THREAD_INTERVAL));
    }
    
    public int getThreadParamsGetCount(int threadId) {
        return Integer.parseInt(getThreadProperty(threadId, PROPS_THREAD_PARAMS_GET_COUNT));
    }
    
    public String getThreadParamsGetType(int threadId) {
        return getThreadProperty(threadId, PROPS_THREAD_PARAMS_GET_TYPE);
    }
    
    public String getThreadCsvOutputFilepath(int threadId) {
        return getThreadProperty(threadId, PROPS_CSV_OUTPUT_FILEPATH);
    }
    
    public int getThreadParamsSetCount(int threadId) {
        return Integer.parseInt(getThreadProperty(threadId, PROPS_THREAD_PARAMS_SET_COUNT));
    }
}
