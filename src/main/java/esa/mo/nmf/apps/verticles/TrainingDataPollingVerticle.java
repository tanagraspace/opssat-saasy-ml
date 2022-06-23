package esa.mo.nmf.apps.verticles;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class TrainingDataPollingVerticle extends AbstractVerticle {
  
    private static final Logger LOGGER = Logger.getLogger(TrainingDataPollingVerticle.class.getName());

    private long startTime = System.currentTimeMillis();

    @Override
    public void start() throws Exception {

        vertx.eventBus().consumer("saasyml.training.data.feed.subscribe", msg -> {

            // the request payload
            JsonObject payload = (JsonObject)(msg.body());

            LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

            vertx.setPeriodic(payload.getInteger("delay"), counter -> {
                long runTime = (System.currentTimeMillis() - startTime) / 1000;
                LOGGER.log(Level.INFO, "Data fetching runtime: " + runTime + " seconds.");

                // TODO: Send to data persitence Verticle 
                // vertx.eventBus().send("stats", stats);
            });

            
            // TODO: 
            // 1. get the list of data pool parameters from the msg object which contains the POST request payload
            /**
             * {
             *    expId: 1234,
             *    datasetId: 2,
             *    iterations: 10000,
             *    delay: 200,
             *    params: ["GNC_0005", "GNC_0011", "GNC_0007"]
             * }
             */

            // 2. Subscribe to the NMF Paramater Service to poll for data pool parameter values.

            // 3. Store them "somewhere", either in filesystem as CSV file or use a micro db java library.
            msg.reply("data feed subscription: done");
        });
  
    }
}