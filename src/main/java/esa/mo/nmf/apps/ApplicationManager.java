package esa.mo.nmf.apps;

import java.util.concurrent.locks.ReentrantLock;

public class ApplicationManager {

    private static volatile ApplicationManager instance;
    private static Object mutex = new Object();
    
    // Flag indicating if the app simulation threads should be stopped
    private boolean simKeepAlive = true;
    
    // Flag indicating we a data received listener has already been registered for the Aggregation service
    private boolean aggregationListenerRegistered = false;

    // Hide the constructor
    private ApplicationManager() {}

    public static ApplicationManager getInstance() {
        
        /**
         * The local variable result seems unnecessary. But, it’s there to improve the performance of our code.
         * In cases where the instance is already initialized (most of the time), the volatile field is only accessed once (due to "return result;" instead of "return instance;").
         * This can improve the method’s overall performance by as much as 25 percent.
         * Source: https://www.journaldev.com/171/thread-safety-in-java-singleton-classes
         */
        ApplicationManager result = instance;
        
        // Enforce Singleton design pattern.
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new ApplicationManager();
            }
        }
        
        // Return singleton instance.
        return result;
    }
    
    public void setSimKeepAlive(boolean alive) {
        this.simKeepAlive = alive;
    }
    
    public boolean isSimKeepAlive() {
        return this.simKeepAlive;
    }
    
    public void setAggregationListenerRegistered(boolean registered) {
        this.aggregationListenerRegistered = registered;
    }
    
    public boolean isAggregationListenerRegistered(){
        return this.aggregationListenerRegistered;
    }

}