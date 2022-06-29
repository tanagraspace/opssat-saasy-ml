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
    private static Object mutex = new Object();
    
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
        // the local variable result seems unnecessary but it's there to improve performance
        // in cases where the instance is already initialized (most of the time), the volatile field is only accessed once (due to "return result;" instead of "return instance;").
        // this can improve the method’s overall performance by as much as 25 percent.
        // source: https://www.journaldev.com/171/thread-safety-in-java-singleton-classes
        PropertiesManager result = instance;
        
        // enforce Singleton design pattern
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new PropertiesManager();
            }
        }
        
        // return singleton instance
        return result;
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

    public int getPort(){
        return Integer.parseInt(getProperty("port"));
    }

    public int getVerticalInstanceCount(String verticalClassName){
        return Integer.parseInt(getProperty("vertical.instance.count." + verticalClassName));
    }
}
