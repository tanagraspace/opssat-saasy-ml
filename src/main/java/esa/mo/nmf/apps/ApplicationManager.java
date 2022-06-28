package esa.mo.nmf.apps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationManager {

    private static volatile ApplicationManager instance;
    private static Object mutex = new Object();
    
    // track if we are finished fetching data or not for each aggregation
    private Map<String, Boolean> dataFetchingCompleteMap;

    // hide the constructor
    private ApplicationManager() {
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
}