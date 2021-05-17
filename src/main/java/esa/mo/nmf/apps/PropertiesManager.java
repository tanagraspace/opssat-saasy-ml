package esa.mo.nmf.apps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {
    private static final Logger LOGGER = Logger.getLogger(PropertiesManager.class.getName());
    
    // path to the application configuration file
    private static final String PROPERTIES_FILE_PATH = "config.properties";
    
    // prefix on property keys
    public static final String PROPS_PREFIX = "";
    
    // number of aggregations to spawn
    public static final String PROPS_AGGREGATIONS = PROPS_PREFIX + "aggregations";
    
    // flush data to file for this batch size of fetched data
    public static final String PROPS_FLUSH_WRITE_AT = PROPS_PREFIX + "flush.write.at";
    
    // number of loop iterations for an aggregation
    public static final String PROPS_AGGREGATION_ITERATIONS = PROPS_PREFIX + "iterations";
    
    // loop iteration intervals in milliseconds
    public static final String PROPS_AGGREGATION_INTERVAL = PROPS_PREFIX + "interval";
    
    // predefined list of aggregations to fetch
    public static final String PROPS_AGGREGATION_PARAMS_GET_NAMES = PROPS_PREFIX + "params.get.names";
    
    // number of datapool parameters to get
    public static final String PROPS_AGGREGATION_PARAMS_GET_COUNT = PROPS_PREFIX + "params.get.count";
    
    // type of parameters to get
    public static final String PROPS_AGGREGATION_PARAMS_GET_TYPE = PROPS_PREFIX + "params.get.type";
    
    // path to the CSV file to write values to
    public static final String PROPS_AGGREGATION_CSV_OUTPUT_FILEPATH = PROPS_PREFIX + "params.get.output.csv";
    
    // flag indicating whether or not fetched values should be appended to existing CSV file or not
    // if set to false then CSV file will be overwritten if it already exists
    public static final String PROPS_AGGREGATION_CSV_OUTPUT_APPEND = PROPS_PREFIX + "params.get.output.csv.append";
    
    // number of datapool parameters to set
    public static final String PROPS_AGGREGATION_PARAMS_SET_COUNT = PROPS_PREFIX + "params.set.count";
    
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
    
    public int getAggregationCount() {
        return Integer.parseInt(getProperty(PROPS_AGGREGATIONS));
    }
    
    public int getFlushWriteAt() {
        return Integer.parseInt(getProperty(PROPS_FLUSH_WRITE_AT));
    }
    
    public boolean isAggregationParamsGetNamesPredefined(int threadId) {
        String key = PROPS_AGGREGATION_PARAMS_GET_NAMES + "." + threadId;
        return this.properties.containsKey(key);
    }
    
    /**
     * Get property for an aggregation with the given id.
     * @param threadId
     * @param key
     * @return
     */
    public String getAggregationProperty(int threadId, String key) {
        String appPropKey = key + "." + threadId;
        return getProperty(appPropKey);
    }
    
    public int getAggregationIterations(int threadId) {
        return Integer.parseInt(getAggregationProperty(threadId, PROPS_AGGREGATION_ITERATIONS));
    }
    
    public int getAggregationInterval(int threadId) {
        return Integer.parseInt(getAggregationProperty(threadId, PROPS_AGGREGATION_INTERVAL));
    }
    
    public List<String> getAggregationParamsGetNames(int threadId) {
        String paramNames = getAggregationProperty(threadId, PROPS_AGGREGATION_PARAMS_GET_NAMES);
        if(paramNames != null) {
            return Arrays.asList(paramNames.split(","));
        }else {
            return null;
        }
    }
    
    public int getAggregationParamsGetCount(int threadId) {
        return Integer.parseInt(getAggregationProperty(threadId, PROPS_AGGREGATION_PARAMS_GET_COUNT));
    }
    
    public String getAggregationParamsGetType(int threadId) {
        return getAggregationProperty(threadId, PROPS_AGGREGATION_PARAMS_GET_TYPE);
    }
    
    public String getAggregationCsvOutputFilepath(int threadId) {
        return getAggregationProperty(threadId, PROPS_AGGREGATION_CSV_OUTPUT_FILEPATH);
    }
    
    public boolean isAggregationCsvOutputAppend(int threadId) {
        return Boolean.parseBoolean(getAggregationProperty(threadId, PROPS_AGGREGATION_CSV_OUTPUT_APPEND));
    }
    
    public int getAggregationParamsSetCount(int threadId) {
        return Integer.parseInt(getAggregationProperty(threadId, PROPS_AGGREGATION_PARAMS_SET_COUNT));
    }
}
