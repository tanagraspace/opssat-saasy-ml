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

import esa.mo.nmf.apps.exceptions.NotEnoughParamsForRequestedType;

public class DatapoolXmlManager {
    private static final Logger LOGGER = Logger.getLogger(DatapoolXmlManager.class.getName());
    
    // path to the datapool xml file
    // FIXME: Can a list be fetched from NMF Supervisor?
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

    // the map containing all the parameter names grouped by there data type
    private Map<String, List<String>> paramMap;
    
    // map used to track the latest param name index fetched from the param name lists
    // this is used to make sure that each simulate app has a different set of param names
    private Map<String, Integer> paramListTrackerMap; 
    
    // singleton instance
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
                
                // build a Map of datapool parameters grouped by their types
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    
                    String paramName = elem.getAttribute(XML_PARAM_NAME);
                    String paramType = elem.getAttribute(XML_PARAM_ATTRIBUTE_TYPE);
                    
                    // initialize the param name list for the first time we encounter a specific type
                    if(!this.paramMap.containsKey(paramType)) {
                        List<String> paramNames = new ArrayList<String>();
                        paramNames.add(paramName);
                        
                        // put initialized param name in list of param names
                        this.paramMap.put(paramType, paramNames);
                        
                        // also initialize the list tracker for the list of param names.
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
    public List<String> getParamNames(int count, DatapoolParamTypes type) throws NotEnoughParamsForRequestedType{
        // fetch all param names that are of the given type
        List<String> allParamNamesForRequestedType = this.paramMap.get(type.name());
        
        // grab n amount of param names that satisfy the configure param count 
        List<String> sampledParamNames = new ArrayList<String>();
        
        // fetch the index from which we need to start getting param names from the list of param names
        int startIndex = this.paramListTrackerMap.get(type.name()).intValue();
        
        // declare the loop index here so that we can use it later
        int i;
        
        // throw exception in case there are not enough params for the requested type
        if((startIndex + count) > allParamNamesForRequestedType.size()) {
            throw new NotEnoughParamsForRequestedType(
                    (startIndex + count) + " unique params of type " + type.toString() + " were requested but there are only "+ 
                            allParamNamesForRequestedType.size() + ".");
        }
        
        for(i = startIndex; i < (startIndex + count); i++) {
            sampledParamNames.add(allParamNamesForRequestedType.get(i));
        }
        
        // update list index
        this.paramListTrackerMap.put(type.name(), Integer.valueOf(i));
        
        // return param names that will be used for stress testing
        return sampledParamNames;
    }
    
    /**
     * Get DatapoolParamType from its string representation.
     * @param paramType
     * @return
     */
    public DatapoolParamTypes getDatapoolParamTypeFromString(String paramType) {
        
        if(paramType.equalsIgnoreCase("Boolean")){
            return DatapoolParamTypes.Boolean;
            
        }else if(paramType.equalsIgnoreCase("Double")){
            return DatapoolParamTypes.Double;
            
        }else if(paramType.equalsIgnoreCase("Float")){
            return DatapoolParamTypes.Float;
            
        }else if(paramType.equalsIgnoreCase("Integer")){
            return DatapoolParamTypes.Integer;
            
        }else if(paramType.equalsIgnoreCase("Long")){
            return DatapoolParamTypes.Long;
            
        }else if(paramType.equalsIgnoreCase("Octet")){
            return DatapoolParamTypes.Octet;
            
        }else if(paramType.equalsIgnoreCase("Short")){
            return DatapoolParamTypes.Short;
            
        }else if(paramType.equalsIgnoreCase("UInteger")){
            return DatapoolParamTypes.UInteger;
            
        }else if(paramType.equalsIgnoreCase("UShort")){
            return DatapoolParamTypes.UShort;
            
        }else if(paramType.equalsIgnoreCase("UOctet")){
            return DatapoolParamTypes.UOctet;
            
        };
        
        return null;
    }
}
