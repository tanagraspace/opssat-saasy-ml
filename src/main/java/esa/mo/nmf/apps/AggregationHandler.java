package esa.mo.nmf.apps;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ccsds.moims.mo.com.structures.InstanceBooleanPair;
import org.ccsds.moims.mo.com.structures.InstanceBooleanPairList;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.structures.Duration;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mc.aggregation.consumer.AggregationStub;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationCategory;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationCreationRequest;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationCreationRequestList;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationDefinitionDetails;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationDefinitionDetailsList;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationParameterSet;
import org.ccsds.moims.mo.mc.aggregation.structures.AggregationParameterSetList;

import org.ccsds.moims.mo.mc.parameter.consumer.ParameterStub;
import org.ccsds.moims.mo.mc.structures.ObjectInstancePairList;

import esa.mo.nmf.apps.exceptions.AddAggregationDidNotReturnAggregationId;


public class AggregationHandler {
    private static final Logger LOGGER = Logger.getLogger(AggregationHandler.class.getName());
    
    // parameters default value before first acquisition
    public static final String PARAMS_DEFAULT_VALUE = "null";
    
    
    // aggregation Id string
    private String aggIdStr;
    
    // aggregation Id of the aggregation we create
    private Long aggId;
    
    // aggregation description
    private String aggDescription;
    
    // time interval between 2 sampling iterations in milliseconds for the simulated app
    private double paramSamplingInterval;
    
    // supervisor (OBSW) parameters names
    private List<String> paramNames;
    
    // prefix for log messages so that we know what simulated app instance triggered the log message
    private String logPrefix;
    
    /**
     * @param expId the experiment id
     * @param datasetId the dataset id
     * @param paramSamplingInterval sampling interval in seconds
     * @param parametersNames names of datapool parameters to sample
     */
    public AggregationHandler(int expId, int datasetId, double paramSamplingInterval, List<String> paramNames) throws Exception {
        this.paramSamplingInterval = paramSamplingInterval;
        this.paramNames = paramNames;
        
        this.aggIdStr = Utils.generateAggregationId(expId, datasetId);
        this.aggDescription = Utils.generateAggregationDescription(expId, datasetId);
        this.logPrefix = Utils.generateLogPrefix(expId, datasetId);
    }

    /**
     * Toggle the subscription to the OBSW parameters values we need.
     * 
     * @param subscribe True if we want supervisor to push new parameters data, false to stop the push
     * @return null if it was successful. If not null, then the returned value holds the error number
     */
    public synchronized void enableSupervisorParametersSubscription(boolean subscribe) throws Exception{    
        if (subscribe) {
            enableSupervisorParameterSubscription();
        } else {
            disableSupervisorParametersSubscription();
        }
    }


    /**
     * Subscribes to the OBSW parameters values we need by creating and enabling an aggregation in the
     * aggregation service of the supervisor.
     */
    private void enableSupervisorParameterSubscription() throws Exception{
        // get parameter ids
        LongList paramIds = getParamIds();
        
        // create (or update) and enable an aggregation for the parameters to fetch
        createOrUpdateAggForParams(paramIds);
  
        LOGGER.log(Level.INFO, this.logPrefix + "Started fetching parameters from supervisor.");
    }


    /**
     * Stops the subscription to the OBSW parameters values by disabling the generation of the
     * aggregation we created in the aggregation service of the supervisor.
     */
    private void disableSupervisorParametersSubscription() throws Exception{
        // get aggregation stub
        AggregationStub aggStub = 
            AppMCAdapter.getInstance().getSupervisorSMA().getMCServices().getAggregationService().getAggregationStub();

        // disable generation of aggregation
        InstanceBooleanPairList instBoolPairList = new InstanceBooleanPairList();
        instBoolPairList.add(new InstanceBooleanPair(this.aggId, false));
        aggStub.enableGeneration(false, instBoolPairList);
      
        // log
        LOGGER.log(Level.INFO, this.logPrefix + "Stopped fetching parameters from supervisor.");
    }
    

    /**
     * Creates (or updates) and enables an aggregation in the supervisor containing the parameters to
     * fetch.
     * 
     * @param paramIds Instance ids of the parameters
     * @return null if it was successful. If not null, then the returned value holds the error number
     */
    private void createOrUpdateAggForParams(LongList paramIds) throws Exception{
        AggregationStub aggStub =
            AppMCAdapter.getInstance().getSupervisorSMA().getMCServices().getAggregationService().getAggregationStub();
      
        Identifier aggIdentifier = new Identifier(this.aggIdStr);

        // list aggregations to test if we already have one defined
        IdentifierList identifierList = new IdentifierList();
        identifierList.add(aggIdentifier);
      
        // get aggregation id
        try {
            ObjectInstancePairList objInstPairList = aggStub.listDefinition(identifierList);
            aggId = objInstPairList.get(0).getObjIdentityInstanceId();
        } catch (MALInteractionException e) {
            // only throw exception if the error is unexpected
            if (!MALHelper.UNKNOWN_ERROR_NUMBER.equals(e.getStandardError().getErrorNumber())) {
                throw e;
            }
        }

        // prepare aggregation details containing the parameters to fetch
        AggregationParameterSet paramSet =
                new AggregationParameterSet(null, paramIds, new Duration(0), null);
      
        AggregationParameterSetList paramSetList = new AggregationParameterSetList();
        paramSetList.add(paramSet);
      
        AggregationDefinitionDetails aggDetails = new AggregationDefinitionDetails(
                this.aggDescription, AggregationCategory.GENERAL.getOrdinalUOctet(),
                new Duration(this.paramSamplingInterval), true, false, false, new Duration(0), true, paramSetList);

        // update existing definition
        if (this.aggId != null) {
            LongList aggIdList = new LongList();
            aggIdList.add(this.aggId);
            AggregationDefinitionDetailsList aggDetailsList = new AggregationDefinitionDetailsList();
            aggDetailsList.add(aggDetails);

            aggStub.updateDefinition(aggIdList, aggDetailsList);
        
        } else { 
            // create new definition
            AggregationCreationRequest aggCreationRequest =
                    new AggregationCreationRequest(aggIdentifier, aggDetails);
        
            AggregationCreationRequestList aggCreationRequestList = new AggregationCreationRequestList();
            aggCreationRequestList.add(aggCreationRequest);
        
            // add aggregation
            ObjectInstancePairList aggObjInstPairList = aggStub.addAggregation(aggCreationRequestList);
      
            // check if the aggregation was successfully created
            if (aggObjInstPairList.size() > 0 && aggObjInstPairList.get(0).getObjIdentityInstanceId() != null) {
                this.aggId = aggObjInstPairList.get(0).getObjIdentityInstanceId();
            }
            
            if (this.aggId == null) {
                throw new AddAggregationDidNotReturnAggregationId("AddAggregation didn't return an aggregation id.");
            }
        }
    }


    public LongList getParamIds() throws Exception{
        ParameterStub paramStub =
            AppMCAdapter.getInstance().getSupervisorSMA().getMCServices().getParameterService().getParameterStub();
    
        // list parameters to fetch and get their IDs from the supervisor
        IdentifierList paramIdentifierList = new IdentifierList();
        this.paramNames.stream().forEach(name -> paramIdentifierList.add(new Identifier(name)));
        
        // list parameter definition
        ObjectInstancePairList objInstPairList;
        objInstPairList = paramStub.listDefinition(paramIdentifierList);

        // build list of param ids
        LongList paramIds = new LongList();
        objInstPairList.stream()
            .forEach(objInstPair -> paramIds.add(objInstPair.getObjIdentityInstanceId()));
      
        // return list of param ids
        return paramIds;
    }
}
