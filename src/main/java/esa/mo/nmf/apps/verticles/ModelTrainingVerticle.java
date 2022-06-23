package esa.mo.nmf.apps.verticles;

import io.vertx.core.AbstractVerticle;

public class ModelTrainingVerticle extends AbstractVerticle {
  
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("saasyml.training.classifier.bayesian.aode", msg -> {
        // TODO: 
        // 1. execute the training for the given experimenter using the training data id to fetch data that previously stored. 
        // Training data was stored with a unique identifier for that training dataset when the DataPollingVerticle was previously executed.
        // HEre we enter ML pipeline for the given algorithm
        //
        // 2. Serialize and save the resulting model.
        // Make sure it is uniquely identifiable with experiement Id and a training session id.
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

