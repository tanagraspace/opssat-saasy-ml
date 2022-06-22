package esa.mo.nmf.apps.verticles;

import io.vertx.core.AbstractVerticle;

public class TrainingVerticle extends AbstractVerticle {
  
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("saasyml.training.classifier.bayesian.aode", msg -> {
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

