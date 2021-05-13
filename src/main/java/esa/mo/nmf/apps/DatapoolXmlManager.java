package esa.mo.nmf.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DatapoolXmlManager {
    private static final Logger LOGGER = Logger.getLogger(DatapoolXmlManager.class.getName());
    
    // Path to the datapool xml file.
    private static final String XML_FILE_PATH = "Datapool.xml";
    
    private static final String XML_ELEM_PARAMETER = "parameter";
    private static final String XML_PARAM_NAME = "name";
    private static final String XML_PARAM_ATTRIBUTE_TYPE = "attributeType";
    
    enum DatapoolParamTypes {
        Boolean,
        Double,
        Float,
        Integer,
        Long,
        Octet,
        Short,
        UInteger,
        UShort,
        UOctet,
    }

    // The map containing all the parameter names grouped by there data type
    private Map<String, List<String>> paramMap;
    
    // Map used to track the latest param name index fetched from the param name lists
    // This is used to make sure that each simulate app has a different set of param names.
    private Map<String, Integer> paramListTrackerMap; 
    
    // Singleton instance.
    private static DatapoolXmlManager instance;
    
    /**
     * Hide constructor.
     */
    private DatapoolXmlManager(){
        this.paramMap = new HashMap<String, List<String>>();
        this.paramListTrackerMap = new HashMap<String, Integer>();
        loadXml();
    }
    
    /**
     * Returns the PropertiesManager instance of the application.
     * Singleton.
     *
     * @return the PropertiesManager instance.
     */
    public static DatapoolXmlManager getinstance() {
        if (instance == null) {
            instance = new DatapoolXmlManager();
        }
        return instance;
    }
    
    /**
     * Loads the properties from the configuration file.
     */
    public void loadXml() {
        try {
            File inputFile = new File(XML_FILE_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(XML_ELEM_PARAMETER);
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                
                // Build a Map of datapool parameters grouped by their types
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    
                    String paramName = elem.getAttribute(XML_PARAM_NAME);
                    String paramType = elem.getAttribute(XML_PARAM_ATTRIBUTE_TYPE);
                    
                    // Initialize the param name list for the first time we encounter a specific type
                    if(!this.paramMap.containsKey(paramType)) {
                        List<String> paramNames = new ArrayList<String>();
                        paramNames.add(paramName);
                        
                        // Put initialized param name in list of param names
                        this.paramMap.put(paramType, paramNames);
                        
                        // Also initialize the list tracker for the list of param names.
                        this.paramListTrackerMap.put(paramType, Integer.valueOf(0));
                        
                    }else {
                        this.paramMap.get(paramType).add(paramName);
                    }
                    
                }
            }
            
            LOGGER.log(Level.INFO, String.format("Loaded datapool names and types from file %s", XML_FILE_PATH));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    /**
     * Get a list of parameter names.
     * @param count how many params to fetch
     * @param type the types of params to fetch
     * @return
     */
    public List<String> getParamNames(int count, DatapoolParamTypes type){
        // Fetch all param names that are of the given type
        List<String> allParamNames = this.paramMap.get(type.name());
        
        // Grab n amount of param names that satisfy the configure param count 
        List<String> sampledParamNames = new ArrayList<String>();
        
        // Fetch the index from which we need to start getting param names from the list of param names
        int startIndex = this.paramListTrackerMap.get(type.name()).intValue();
        
        // Declare the loop index here so that we can use it later
        int i;
        
        for(i = startIndex; i < (startIndex + count); i++) {
            sampledParamNames.add(allParamNames.get(i));
        }
        
        // Update list index
        this.paramListTrackerMap.put(type.name(), Integer.valueOf(i));
        
        // Return param names that will be used for stress testing
        return sampledParamNames;
    }
}
