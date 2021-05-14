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
    
    // number of apps to simulate
    public static final String PROPS_APP_COUNT = PROPS_PREFIX + "appCount";
    
    // number of loop iterations for a simulated app
    public static final String PROPS_APP_ITERATIONS = PROPS_PREFIX + "iterations";
    
    // loop iteration intervals in milliseconds
    public static final String PROPS_APP_INTERVAL = PROPS_PREFIX + "interval";
    
    // number of datapool parameters to get
    public static final String PROPS_APP_PARAMS_GET_COUNT = PROPS_PREFIX + "params.get.count";
    
    // number of datapool parameters to set
    public static final String PROPS_APP_PARAMS_SET_COUNT = PROPS_PREFIX + "params.set.count";
    
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
    public static PropertiesManager getinstance() {
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
    
    public String getAppSimProperty(int appId, String key) {
        String appPropKey = key + "." + appId;
        return getProperty(appPropKey);
    }
    
    public int getAppSimIterations(int appId) {
        return Integer.parseInt(getAppSimProperty(appId, PROPS_APP_ITERATIONS));
    }
    
    public int getAppSimInterval(int appId) {
        return Integer.parseInt(getAppSimProperty(appId, PROPS_APP_INTERVAL));
    }
    
    public int getAppSimParamsGetCount(int appId) {
        return Integer.parseInt(getAppSimProperty(appId, PROPS_APP_PARAMS_GET_COUNT));
    }
    
    public int getAppSimParamsSetCount(int appId) {
        return Integer.parseInt(getAppSimProperty(appId, PROPS_APP_PARAMS_SET_COUNT));
    }
}
