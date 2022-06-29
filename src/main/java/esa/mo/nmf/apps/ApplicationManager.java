package esa.mo.nmf.apps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.util.Pair;

public class ApplicationManager {

    private static volatile ApplicationManager instance;
    private static Object mutex = new Object();
    
    // map that contains all instances of aggregation handlers
    private Map<Pair<Integer, Integer>, AggregationHandler> aggregationHandlerMap;

    // map that counts how many times data was pulled
    private Map<Pair<Integer, Integer>, Integer> receivedDataCounterMap;

    // hide the constructor
    private ApplicationManager() {
        this.aggregationHandlerMap = new ConcurrentHashMap<Pair<Integer, Integer>, AggregationHandler>();
        this.receivedDataCounterMap = new ConcurrentHashMap<Pair<Integer, Integer>, Integer>();
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

    public int incrementReceivedDataCounter(int expId, int datasetId) {
        Pair<Integer, Integer> id = new Pair<Integer, Integer>(expId, datasetId);

        if(!this.receivedDataCounterMap.containsKey(id)){
            this.receivedDataCounterMap.put(id, 1);
        }else{
            this.receivedDataCounterMap.put(
                id, (this.receivedDataCounterMap.get(id)+1)
            );
        }

        return this.receivedDataCounterMap.get(id);
    }

    public int getReceivedDataCounter(int expId, int datasetId) {
        Pair<Integer, Integer> id = new Pair<Integer, Integer>(expId, datasetId);

        if(this.receivedDataCounterMap.containsKey(id)){
            return this.receivedDataCounterMap.get(id);
        }

        return -1;
    }

    public void removeReceivedDataCounter(int expId, int datasetId) {
        this.receivedDataCounterMap.remove(new Pair<Integer, Integer>(expId, datasetId));
    }

    public void addAggregationHandler(int expId, int datasetId, AggregationHandler aggregationHandler) {
        this.aggregationHandlerMap.put(new Pair<Integer, Integer>(expId, datasetId), aggregationHandler);
    }

    public void removeAggregationHandler(int expId, int datasetId) {
        aggregationHandlerMap.remove(new Pair<Integer, Integer>(expId, datasetId));
    }

    public AggregationHandler getAggregationHandler(int expId, int datasetId) {
        return this.aggregationHandlerMap.get(new Pair<Integer, Integer>(expId, datasetId));
    }


    public void enableSupervisorParametersSubscription(int expId, int datasetId, boolean enable) throws Exception {
        Pair<Integer, Integer> id = new Pair<Integer, Integer>(expId, datasetId);
        if(this.aggregationHandlerMap.containsKey(id)){
            this.aggregationHandlerMap.get(id).enableSupervisorParametersSubscription(enable);
        }
    }

    public AggregationHandler createAggregationHandler(int expId, int datasetId, double interval, List<String> paramNameList, boolean subscribeToFeed) throws Exception{
        // the key to access the aggregation handler from the map
        Pair<Integer, Integer> id = new Pair<Integer, Integer>(expId, datasetId);

        // create aggregation handler if it doesn't already exist
        if(!this.aggregationHandlerMap.containsKey(id)){
            
            AggregationHandler aggregationHandler = new AggregationHandler(expId, datasetId, interval, paramNameList);

            // subscribe or unsubscribe to the parameter feed
            aggregationHandler.enableSupervisorParametersSubscription(subscribeToFeed);
            
            // put aggregation handler in map
            this.aggregationHandlerMap.put(id, aggregationHandler);
        }

        // return the aggregation handler
        return this.aggregationHandlerMap.get(id);
    }
}    