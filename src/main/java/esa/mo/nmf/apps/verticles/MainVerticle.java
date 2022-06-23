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

public class MainVerticle extends AbstractVerticle {
  /*
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
  **/

  // TODO: implement getter
  //private Vertx vertx;

  // constructor
  public MainVerticle()
  {
    super.vertx = Vertx.vertx();
  }
  
  @Override
  public void start() throws Exception {

    // set instance count
    int port = 9999;

    // set instance count
    int instanceCount = 1;

    // deployment options for multi-core and multi-threded goodness
    // specify the number of verticle instances that you want to deploy
    // this is useful for scaling easily across multiple cores
    // TODO: make this configurable from a config file.
    DeploymentOptions dataPollDeployOpts = new DeploymentOptions()
      .setWorker(true)
      .setInstances(instanceCount);

    DeploymentOptions trainingDeployOpts = new DeploymentOptions()
      .setWorker(true)
      .setInstances(instanceCount);


    // deplopy the verticles
    vertx.deployVerticle("esa.mo.nmf.apps.verticles.TrainingDataPollingVerticle", dataPollDeployOpts);
    vertx.deployVerticle("esa.mo.nmf.apps.verticles.ModelTrainingVerticle", trainingDeployOpts);

    
    // define router and api paths
    Router router = Router.router(vertx);
    router.post("/api/v1/data/feed/subscribe")
      //TODO: validate json payload against schema, see https://vertx.io/docs/vertx-web-validation/java/ 
      .handler(BodyHandler.create())
      .handler(this::fetchTrainingData);

    // route: fetch data
    // POST request with JSON PAyload
    // router.get("/api/v1/subscribe").handler(this::trainClassfifier);

    // route: training
    // TODO: parametize the type "classifier" because not all algorithms are classifiers.
    // router.get("/api/v1/training/:type/:group/:algorithm").handler(this::trainClassfifier);

    router.get("/api/v1/training/classifier/:group/:algorithm").handler(this::trainClassfifier);

    // route: inference
    router.get("/api/v1/inference").handler(this::inference);
      
    // listen
    vertx.createHttpServer().requestHandler(router).listen(port);
  }

  void fetchTrainingData(RoutingContext ctx) {
    // response map
    Map<String, String> responseMap = new HashMap<String, String>();

    // payload
    JsonObject jsonObject = ctx.getBodyAsJson();

    try
    {
      // forward request to event bus to be handled in the Training Data Polling Verticle
      vertx.eventBus().request("saasyml.training.data.feed.subscribe", jsonObject, reply -> {

        // return response from the verticle
        ctx.request().response().end((String)reply.result().body());
      });
    } catch (Exception e) {
      // error object
      responseMap.put("request", "error");
      responseMap.put("message", "unsupported or invalid training request");

      // error response
      ctx.request().response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(responseMap));
    }
  }

  void trainClassfifier(RoutingContext ctx) {

      // response map
      Map<String, String> resMap = new HashMap<String, String>();

      // get api request url params
      // e.g. /api/v1/training/classifier/classifier.bayesian.aode
      //    type is "classifier"
      //    group is "bayesian"
      //    algorithm is "aode"

      
      // TODO: String type = ctx.pathParam(name: "type");
      String group = ctx.pathParam("group");
      String algorithm = ctx.pathParam("algorithm");

      // TODO: Request must be a POST request with a JSON payload message:
      /**
       * {
       *  expId: 123,
       *  traininDatasetId: 1,
       * }
       */

      // forward request to event bus
      try
      {
        vertx.eventBus().request("saasyml.training.classifier." + group + "." + algorithm , "", reply -> {
          ctx.request().response().end((String)reply.result().body());
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
