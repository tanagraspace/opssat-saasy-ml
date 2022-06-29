package esa.mo.nmf.apps.verticles;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import esa.mo.nmf.apps.ApplicationManager;

public class FetchTrainingDataVerticle extends AbstractVerticle {
  
    // logger
    private static final Logger LOGGER = Logger.getLogger(FetchTrainingDataVerticle.class.getName());

    @Override
    public void start() throws Exception {

        // log
        LOGGER.log(Level.INFO, "Starting a Verticle instance with deployment id " + this.deploymentID() + ".");

        // subscribe to a training data feed
        vertx.eventBus().consumer("saasyml.training.data.subscribe", msg -> {

            // the request payload (Json)
            JsonObject payload = (JsonObject)(msg.body());
            LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

            // parse the Json payload
            final int expId = payload.getInteger("expId").intValue();
            final int datasetId = payload.getInteger("datasetId").intValue();
            final double interval = payload.getInteger("interval").doubleValue();
            
            // iterations payload parameter is optional, set it to -1 if it wasn't provided
            final int iterations = payload.containsKey("iterations") ? payload.getInteger("iterations").intValue() : -1;
            
            // create list of training data param names from JsonArray
            final JsonArray trainingDataParamNameJsonArray = payload.getJsonArray("params");

            List<String> paramNameList = new ArrayList<String>();
            for(int i = 0; i < trainingDataParamNameJsonArray.size(); i++){
                paramNameList.add(trainingDataParamNameJsonArray.getString(i));
            }

            try {

                // create aggregation handler and subscribe the parameter feed
                ApplicationManager.getInstance().createAggregationHandler(expId, datasetId, interval, paramNameList, true);

                // check periodically when to stop fetching datam if "interations" is set and > 0
                if(iterations > 0)
                {
                    // register periodic timer
                    vertx.setPeriodic(500, id -> {
                        try {
                            // get data received counter value
                            int counter = ApplicationManager.getInstance().getReceivedDataCounter(expId, datasetId);

                            if(counter < 0){
                                vertx.cancelTimer(id);

                            }else if(counter >= iterations){
                                // disable parameter feed
                                ApplicationManager.getInstance().enableSupervisorParametersSubscription(expId, datasetId, false);
    
                                // remove from map
                                ApplicationManager.getInstance().removeAggregationHandler(expId, datasetId);

                                // remove data received counter
                                ApplicationManager.getInstance().removeReceivedDataCounter(expId, datasetId);
                            }
    
                        } catch(Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to unsubscribe from training data feed.", e);
                        }
                        
                    });
                }

                // response: success
                msg.reply("Successfully subscribed to training data feed.");

            } catch(Exception e){
                // log
                LOGGER.log(Level.SEVERE, "Failed to start Aggregation Handler.", e);

                // response: error
                msg.reply("Failed to subscribe to training data feed.");
            } 
        });

        // unsubscribe to a training data feed
        vertx.eventBus().consumer("saasyml.training.data.unsubscribe", msg -> {
            // the request payload (Json)
            JsonObject payload = (JsonObject)(msg.body());
            LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

            // parse the Json payload
            final int expId = payload.getInteger("expId").intValue();
            final int datasetId = payload.getInteger("datasetId").intValue();

            try {
                // disable parameter feed
                ApplicationManager.getInstance().enableSupervisorParametersSubscription(expId, datasetId, false);

                // remove from map
                ApplicationManager.getInstance().removeAggregationHandler(expId, datasetId);

                // remove data received counter
                ApplicationManager.getInstance().removeReceivedDataCounter(expId, datasetId);

                // response: success
                msg.reply("Successfully unsubscribed to training data feed.");

            } catch(Exception e) {
                // log
                LOGGER.log(Level.SEVERE, "Failed to unsubscribe from training data feed.", e);

                // response: error
                msg.reply("Failed to unsubscribe from training data feed.");
            }
        });    
    }
}