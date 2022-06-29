package esa.mo.nmf.apps;

public final class Utils {

    public static String generateAggregationId(int expId, int datasetId) {
        return "E" + expId + "_D" + datasetId;
    }
    
    public static String generateAggregationDescription(int expId, int datasetId) {
        return "Experiment " + expId  + ", Dataset #" + datasetId;
    }

    public static String generateLogPrefix(int expId, int datasetId) {
        return "[" + generateAggregationId(expId, datasetId) + "]";
    }
    
    public static int getExpIdFromAggId(String aggId) {
        // e.g AggId: "E888_D1"
        return Integer.parseInt(aggId.substring(1, aggId.lastIndexOf('_')));
    }

    public static int getDatasetIdFromAggId(String aggId) {
        // e.g AggId: "E888_D1"
        return Integer.parseInt(aggId.substring(aggId.lastIndexOf('_')+2, aggId.length()));
    }
}
