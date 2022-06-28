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
}
