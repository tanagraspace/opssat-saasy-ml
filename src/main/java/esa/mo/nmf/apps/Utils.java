package esa.mo.nmf.apps;

public final class Utils {
    public static String generateAggregationId(int threadId) {
        return "Exp" + Constants.EXPERIMENT_ID + "_AggThread_" + threadId;
    }
    
    public static String generateAggregationDescription(int threadId) {
        return "Exp" + Constants.EXPERIMENT_ID  + " Aggregation Thread #" + threadId;
    }
    
    public static int getThreadIdFromAggId(String aggId) {
        // Egg AggId: "Exp888_AggThread_1"
        return Integer.parseInt(aggId.substring(aggId.lastIndexOf('_')+1, aggId.length()));
    }
    
    public static String generateLogPrefix(int threadId) {
        return "[Thread #" + threadId + "] ";
    }
}
