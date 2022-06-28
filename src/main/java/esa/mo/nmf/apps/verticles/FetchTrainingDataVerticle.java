package esa.mo.nmf.apps.verticles;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import java.util.List;
import java.util.ArrayList;

import esa.mo.nmf.apps.AggregationHandler;

public class FetchTrainingDataVerticle extends AbstractVerticle {
  
    private static final Logger LOGGER = Logger.getLogger(FetchTrainingDataVerticle.class.getName());

    private AggregationHandler aggregationHandler;


    @Override
    public void start() throws Exception {

        vertx.eventBus().consumer("saasyml.training.data.subscribe", msg -> {

            // the request payload (Json)
            JsonObject payload = (JsonObject)(msg.body());
            LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

            // parse the Json payload
            final int expId = payload.getInteger("expId").intValue();
            final int datasetId = payload.getInteger("datasetId").intValue();
            final double interval = payload.getInteger("interval").doubleValue();
            final int iterations = payload.getInteger("iterations").intValue();
            final JsonArray trainingDataParamNameJsonArray = payload.getJsonArray("params");

            // create list of training data param names
            List<String> paramNameList = new ArrayList<String>();
            for(int i = 0; i < trainingDataParamNameJsonArray.size(); i++){
                paramNameList.add(trainingDataParamNameJsonArray.getString(i));
            }

            try {
                // initialize the aggregation handler
                this.aggregationHandler = new AggregationHandler(expId, datasetId, interval, paramNameList);
                
                // start fetching training data by subscribing to the Supervisor parameter subscription
                this.aggregationHandler.toggleSupervisorParametersSubscription(true);

                // todo: check periodically when to stop fetching datam if "interations" is set and > 0
                /**
                vertx.setPeriodic(500, id -> {

                    try {
                        this.aggregationHandler.toggleSupervisorParametersSubscription(false);
                    } catch(Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to unsubscribe from training data feed.", e);
                    }
                    
                });
                */
                

                // response: success
                msg.reply("Successfully subscribed to training data feed.");

            } catch(Exception e){
                // log
                LOGGER.log(Level.SEVERE, "Failed to start Aggregation Handler.", e);

                // response: error
                msg.reply("Failed to subscribe to training data feed.");
            } 
        });

        vertx.eventBus().consumer("saasyml.training.data.unsubscribe", msg -> {
            // the request payload (Json)
            JsonObject payload = (JsonObject)(msg.body());
            LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

            // parse the Json payload
            final int expId = payload.getInteger("expId").intValue();
            final int datasetId = payload.getInteger("datasetId").intValue();

            try {
                // unscubscribe from the Supervisor parameter subscription
                this.aggregationHandler.toggleSupervisorParametersSubscription(false);

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