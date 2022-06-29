package esa.mo.nmf.apps.verticles;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class TrainModelVerticle extends AbstractVerticle {

  // logger
  private static final Logger LOGGER = Logger.getLogger(TrainModelVerticle.class.getName());
  
  @Override
  public void start() throws Exception {

    vertx.eventBus().consumer("saasyml.training.classifier.bayesian.aode", msg -> {

        // the request payload (Json)
        JsonObject payload = (JsonObject)(msg.body());
        LOGGER.log(Level.INFO, "The POST request payload: " + payload.toString());

        // parse the Json payload
        final int expId = payload.getInteger("expId").intValue();
        final int datasetId = payload.getInteger("datasetId").intValue();

        // todo: 
        // 1. execute the training using the expId and datasetId to fetch traning data that was stored from AggregationWrite. 
        // Here we enter ML pipeline for the given algorithm
        //
        // 2. Serialize and save the resulting model.
        // Make sure it is uniquely identifiable with expId and datasetId, maybe as part of the toGround folder file system:
        //
        // 3. Return a message with unique identifiers of the serizalized model (or maybe just a path to it?)


        msg.reply("training: classifier.bayesian.aode");
    });

    vertx.eventBus().consumer("saasyml.training.classifier.bayesian.bestclassdistribution", msg -> {
        msg.reply("training: classifier.bayesian.aode");
    });

    vertx.eventBus().consumer("saasyml.training.classifier.boosting.bagging", msg -> {
        msg.reply("training: classifier.boosting.bagging");
    });

    vertx.eventBus().consumer("saasyml.training.classifier.boosting.samme", msg -> {
        msg.reply("training: classifier.boosting.samme");
    });

  }
}

