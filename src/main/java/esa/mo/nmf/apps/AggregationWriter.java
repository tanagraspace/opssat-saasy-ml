package esa.mo.nmf.apps;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mc.aggregation.structures.AggregationParameterValueList;
import org.ccsds.moims.mo.mc.parameter.structures.ParameterValue;

import esa.mo.helpertools.helpers.HelperAttributes;
import esa.mo.mc.impl.provider.AggregationInstance;
import esa.mo.nmf.commonmoadapter.CompleteAggregationReceivedListener;

public class AggregationWriter implements CompleteAggregationReceivedListener {
    
    private static final Logger LOGGER = Logger.getLogger(AggregationWriter.class.getName());
    
    // flush data to file for this batch size of fetched data
    private int flushWriteAt;
    
    // csv file writer map, one per thread
    private Map<String, PrintWriter> csvWriterMap;
    
    // track if csv file already exists or not, use this map to figure out when to append or not
    private Map<String, Boolean> csvFileExistsMap;
    
    // map to count the number of data fetching iterations
    private Map<String, Integer> iterationTrackerMap;

    public AggregationWriter() throws Exception{
        
        // flush write at
        this.flushWriteAt = PropertiesManager.getInstance().getFlushWriteAt();
        
        // instanciate our maps
        this.csvWriterMap = new HashMap<String, PrintWriter>();
        this.csvFileExistsMap = new HashMap<String, Boolean>();
        this.iterationTrackerMap = new HashMap<String, Integer>();
        
        // get the thread count
        int threadCount = PropertiesManager.getInstance().getAggregationCount();
        
        
        // initialize some stuff for each aggregation
        for(int threadId=1; threadId <= threadCount; threadId++) { 
            
            // initialize the flag indicating if the data fetching and writing process is complete or not.
            ApplicationManager.getInstance().setDataFetchingComplete(threadId, false);
            
            // build the aggregation id string
            String aggId = Utils.generateAggregationId(threadId);
            
            // initialize the iteration tracker map for each aggregation
            this.iterationTrackerMap.put(aggId, 0);
            
            // the path of the CSV file
            String csvOuptutFilepath = PropertiesManager.getInstance().getAggregationCsvOutputFilepath(threadId);
            
            // track if file exists already or if we are creating it for the first time
            boolean csvFileExists = new File(csvOuptutFilepath).exists();
            csvFileExistsMap.put(aggId, csvFileExists);
            
            // get file's parent directory path
            Path csvOuptutFileDirPath = Paths.get(csvOuptutFilepath).getParent();
            
            // if parent directory path is null then the given file path is just a file name, roll with it
            // however, if it's not null it means we need to create the parent directories if they don't exist
            if(csvOuptutFileDirPath != null) {
                
                // create a "File" object of our directory path
                // we do this because we want to call .exists() and .mkdirs()
                File csvOuptutDirFile = csvOuptutFileDirPath.toFile();
                
               
                // if file doesn't exist then make sure that all the path directories are created
                if (!csvOuptutDirFile.exists()) {
                    if (!csvOuptutDirFile.mkdirs()) {
                        LOGGER.log(Level.SEVERE,
                            String.format("Couldn't create directories for file path %s", csvOuptutDirFile.getPath()));
                    }else {
                        LOGGER.log(Level.INFO, "Created directories for file path %s", csvOuptutDirFile.getPath());
                    }
                }
            }
            
            // get the flag indicating if we append to the existing file or create a new one
            boolean append = PropertiesManager.getInstance().isAggregationCsvOutputAppend(threadId);
            
            // instanciate a csv print writer for each aggregation
            this.csvWriterMap.put(aggId, new PrintWriter(new FileWriter(csvOuptutFilepath, append)));
        }
    }
    
    @Override
    public void onDataReceived(AggregationInstance aggregationInstance) {
        if (aggregationInstance == null) {
            LOGGER.log(Level.WARNING, "Received null aggregation instance");
            return;
        }
        
        // get aggregation id
        String aggId = aggregationInstance.getName().getValue();
        
        try {
            
            // get thread id
            int threadId = Utils.getThreadIdFromAggId(aggId);
            
            // if it's the first aggregation data is received then write the csv header row
            if(this.iterationTrackerMap.get(aggId) == 0) {
                // get the flag indicating if we append to the existing file or create a new one
                boolean append = PropertiesManager.getInstance().isAggregationCsvOutputAppend(threadId);
                
                // only write the header row if append is set to false or if the file doesn't exist and we are creating a new one 
                if(!append || !csvFileExistsMap.get(aggId)) {
                    writeHeader(aggId);
                }
            }
            
            // get aggregation timestamp and parameter values
            Long timestamp = aggregationInstance.getTimestamp().getValue();
            List<String> paramValues = getParameterValues(aggregationInstance);
            
            // write param values to csv file
            String csvRow = timestamp + "," + String.join(",", paramValues);
            this.csvWriterMap.get(aggId).println(csvRow);
            
            // get iteration counter for this aggregation
            int iterCounter = incrementIterationTracker(aggId);
            
            // flush so that we still have written data in case of failure
            if(iterCounter % this.flushWriteAt == 0) {
                this.csvWriterMap.get(aggId).flush();
            }
            
            // check if we are finished fetching param values for the aggregation
            if(iterCounter >= PropertiesManager.getInstance().getAggregationIterations(threadId)) {
                // set a flag to signal the parameter subscription thread that we are done and it can stop
                ApplicationManager.getInstance().setDataFetchingComplete(threadId, true);
                
                // close the writer when we are finished 
                this.csvWriterMap.get(aggId).close();
            }
        
        }catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching and writing parameters for aggregation " + aggId, e);
        }
    
    }
    
    public void writeHeader(String aggId) {
        int threadId = Utils.getThreadIdFromAggId(aggId);
        
        // write the header row
        List<String> paramNames = ApplicationManager.getInstance().getParamNames(threadId);
        String headers = "timestamp," + String.join(",", paramNames);
        
        // Write header row to file
        this.csvWriterMap.get(aggId).println(headers);
    }
    
    public int incrementIterationTracker(String aggId) {
        // increment aggregation iteration counter
        int iterCounter = this.iterationTrackerMap.get(aggId);
        iterCounter++;
        
        // update iteration tracker map
        this.iterationTrackerMap.put(aggId, iterCounter);
        
        // return decrement iteration counter value
        return iterCounter;
    }
    
    public List<String> getParameterValues(AggregationInstance aggregationInstance) {

        // the list that will contain all the param values
        List<String> paramValues = new ArrayList<String>();

        // the aggregration param value list
        AggregationParameterValueList aggParamValueList =
                aggregationInstance.getAggregationValue().getParameterSetValues().get(0).getValues();

        // populate the list that will be returned
        for (int i = 0; i < aggParamValueList.size(); i++) {
            ParameterValue paramValue = aggParamValueList.get(i).getValue();
            String paramValueStr = HelperAttributes.attribute2string(paramValue.getRawValue());
            paramValues.add(paramValueStr);
        }

        // return the list
        return paramValues;
    }
    
}
