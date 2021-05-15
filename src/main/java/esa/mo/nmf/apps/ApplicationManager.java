package esa.mo.nmf.apps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationManager {

    private static volatile ApplicationManager instance;
    private static Object mutex = new Object();
    
    // flag indicating if all threads should be stopped
    private boolean dataPollingThreadsKeepAlive;
    
    // parameter names to fetch for each aggregation
    private Map<String, List<String>> paramNamesMap;
    
    // track if we are finished fetching data or not for each aggregation
    private Map<String, Boolean> dataFetchingCompleteMap;

    // hide the constructor
    private ApplicationManager() {
        this.dataPollingThreadsKeepAlive = true;
        this.paramNamesMap =  new HashMap<String, List<String>>();
        this.dataFetchingCompleteMap = new ConcurrentHashMap<String, Boolean>();
    }

    public static ApplicationManager getInstance() {
        // the local variable result seems unnecessary but it's there to improve performance
        // in cases where the instance is already initialized (most of the time), the volatile field is only accessed once (due to "return result;" instead of "return instance;").
        // this can improve the method’s overall performance by as much as 25 percent.
        // source: https://www.journaldev.com/171/thread-safety-in-java-singleton-classes
        ApplicationManager result = instance;
        
        // enforce Singleton design pattern
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new ApplicationManager();
            }
        }
        
        // return singleton instance
        return result;
    }
    
    public void setDataPollingThreadsKeepAlive(boolean alive) {
        this.dataPollingThreadsKeepAlive = alive;
    }
    
    public boolean isDataPollingThreadsKeepAlive() {
        return this.dataPollingThreadsKeepAlive;
    }
    
    public void setParamNames(int threadId, List<String> paramNames){
        String threadIdStr = Integer.toString(threadId);
        this.paramNamesMap.put(threadIdStr, paramNames);
    }
    
    public List<String> getParamNames(int threadId){
        String threadIdStr = Integer.toString(threadId);
        
        if(paramNamesMap.containsKey(threadIdStr)) {
            return paramNamesMap.get(threadIdStr);
        }else {
            return null;
        }
    }
    
    public void setDataFetchingComplete(int threadId, boolean complete){
        String threadIdStr = Integer.toString(threadId);
        this.dataFetchingCompleteMap.put(threadIdStr, complete);
    }
    
    public boolean isDataFetchingComplete(int threadId) {
        String threadIdStr = Integer.toString(threadId);
        
        if(dataFetchingCompleteMap.containsKey(threadIdStr)) {
            return dataFetchingCompleteMap.get(threadIdStr);
        }else {
            // If key is not present then data fetching has not started
            return false;
        }
    }
}