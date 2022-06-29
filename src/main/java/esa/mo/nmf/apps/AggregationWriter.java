package esa.mo.nmf.apps;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ccsds.moims.mo.mc.aggregation.structures.AggregationParameterValueList;
import org.ccsds.moims.mo.mc.parameter.structures.ParameterValue;

import esa.mo.helpertools.helpers.HelperAttributes;
import esa.mo.mc.impl.provider.AggregationInstance;
import esa.mo.nmf.commonmoadapter.CompleteAggregationReceivedListener;

// todo: turn this to a vertical? PersistTrainingDataVertical?
// only if we need to send something over the event bust to another vertical... do we?
public class AggregationWriter implements CompleteAggregationReceivedListener {    

    // logger
    private static final Logger LOGGER = Logger.getLogger(AggregationWriter.class.getName());

    // constructor
    public AggregationWriter() throws Exception {
    }
    
    @Override
    public void onDataReceived(AggregationInstance aggregationInstance) {
        if (aggregationInstance == null) {
            LOGGER.log(Level.WARNING, "Received null aggregation instance.");
            return;
        }
        
        // get aggregation id
        String aggId = aggregationInstance.getName().getValue();
        
        try {  
            // get experiment id
            int expId = Utils.getExpIdFromAggId(aggId);

            // get dataset id
            int datasetId = Utils.getDatasetIdFromAggId(aggId);

            // keep track of data received count
            ApplicationManager.getInstance().incrementReceivedDataCounter(expId, datasetId);
        
            // get aggregation timestamp and parameter values
            Long timestamp = aggregationInstance.getTimestamp().getValue();
            List<String> paramValues = getParameterValues(aggregationInstance);

            // todo: persist in database
            LOGGER.log(Level.INFO, "Fetched data for experiment " + expId + " (dataset " + datasetId + "): [" + timestamp + "] " + paramValues.toString());
        
        }catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching and writing parameters for aggregation " + aggId + ".", e);
        }
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
