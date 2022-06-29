package esa.mo.nmf.apps.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import esa.mo.nmf.apps.PropertiesManager;


public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = Logger.getLogger(MainVerticle.class.getName());

  // constructor
  public MainVerticle() {
    super.vertx = Vertx.vertx();
  }

  @Override
  public void start() throws Exception {

    // todo: fetch from config file
    // set instance count
    int port = PropertiesManager.getInstance().getPort();

    // todo: make this configurable from a config file.
    // deployment options for multi-core and multi-threded goodness
    // specify the number of verticle instances that you want to deploy
    // this is useful for scaling easily across multiple cores
    DeploymentOptions fetchTrainingDataDeployOpts = new DeploymentOptions()
        .setWorker(true)
        .setInstances(PropertiesManager.getInstance().getVerticalInstanceCount(FetchTrainingDataVerticle.class.getSimpleName()));

    DeploymentOptions trainModelDeployOpts = new DeploymentOptions()
        .setWorker(true)
        .setInstances(PropertiesManager.getInstance().getVerticalInstanceCount(TrainModelVerticle.class.getSimpleName()));

    // deplopy the verticles
    LOGGER.log(Level.INFO, "Deploying Verticles.");
    vertx.deployVerticle("esa.mo.nmf.apps.verticles.FetchTrainingDataVerticle", fetchTrainingDataDeployOpts);
    vertx.deployVerticle("esa.mo.nmf.apps.verticles.TrainModelVerticle", trainModelDeployOpts);

    // define router and api paths
    Router router = Router.router(vertx);

    // route for training data feed subscription
    router.post("/api/v1/training/data/subscribe")
      // todo: validate json payload against schema, see
      // https://vertx.io/docs/vertx-web-validation/java/
      .handler(BodyHandler.create())
      .handler(this::subscribeToTrainingDataFeed);

    // route for training data feed unsubscription
    router.post("/api/v1/training/data/unsubscribe")
      // todo: validate json payload against schema, see
      // https://vertx.io/docs/vertx-web-validation/java/
      .handler(BodyHandler.create())
      .handler(this::unsubscribeFromTrainingDataFeed);

    // route for model training
    // todo: parametize the type "classifier" because not all algorithms are
    // classifiers.
    // router.get("/api/v1/training/:type/:group/:algorithm").handler(this::trainClassfifier);

    router.get("/api/v1/training/classifier/:group/:algorithm").handler(this::trainClassfifier);

    // route: inference
    router.get("/api/v1/inference").handler(this::inference);

    // listen
    vertx.createHttpServer().requestHandler(router).listen(port);
  }

  /**
   * Subscribe to training data feed
   * @param ctx
   */
  void subscribeToTrainingDataFeed(RoutingContext ctx) {
    // response map
    Map<String, String> responseMap = new HashMap<String, String>();

    // payload
    JsonObject jsonObject = ctx.getBodyAsJson();

    try {
      // forward request to event bus to be handled in the Training Data Polling
      // Verticle
      vertx.eventBus().request("saasyml.training.data.subscribe", jsonObject, reply -> {

        // return response from the verticle
        ctx.request().response().end((String) reply.result().body());
      });
    } catch (Exception e) {
      // error object
      responseMap.put("request", "error");
      responseMap.put("message", "unsupported or invalid training data request");

      // error response
      ctx.request().response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(responseMap));
    }
  }

  
  /**
   * unsubscribe to training data feed
   * @param ctx
   */
  void unsubscribeFromTrainingDataFeed(RoutingContext ctx) {
    // response map
    Map<String, String> responseMap = new HashMap<String, String>();

    // payload
    JsonObject jsonObject = ctx.getBodyAsJson();

    try {
      // forward request to event bus to be handled in the Training Data Polling
      // Verticle
      vertx.eventBus().request("saasyml.training.data.unsubscribe", jsonObject, reply -> {

        // return response from the verticle
        ctx.request().response().end((String) reply.result().body());
      });
    } catch (Exception e) {
      // error object
      responseMap.put("request", "error");
      responseMap.put("message", "unsupported or invalid training data request");

      // error response
      ctx.request().response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(responseMap));
    }
  }


  /**
   * train a model
   * @param ctx
   */
  void trainClassfifier(RoutingContext ctx) {

    // response map
    Map<String, String> resMap = new HashMap<String, String>();

    // get api request url params
    // e.g. /api/v1/training/classifier/classifier.bayesian.aode
    // type is "classifier"
    // group is "bayesian"
    // algorithm is "aode"

    // TODO: String type = ctx.pathParam(name: "type");
    String group = ctx.pathParam("group");
    String algorithm = ctx.pathParam("algorithm");

    // TODO: Request must be a POST request with a JSON payload message:
    /**
     * {
     * expId: 123,
     * traininDatasetId: 1,
     * }
     */

    // forward request to event bus
    try {
      vertx.eventBus().request("saasyml.training.classifier." + group + "." + algorithm, "", reply -> {
        ctx.request().response().end((String) reply.result().body());
      });
    } catch (Exception e) {
      // error object
      resMap.put("request", "error");
      resMap.put("message", "unsupported or invalid training request");

      // error response
      ctx.request().response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(resMap));
    }
  }


  /**
   * use a trained model to execute an inference
   * @param ctx
   */
  void inference(RoutingContext ctx) {
    // response map
    Map<String, String> resMap = new HashMap<String, String>();

    // populate map
    resMap.put("request", "inference");

    // response
    ctx.request().response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(resMap));
  }

}
