package org.crud.kotlin.verticle

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.ReadStream
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.MongoClientDeleteResult
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine


public class RestServer : AbstractVerticle() {

    private var mongoClient: MongoClient? = null
    private val templateEngine: FreeMarkerTemplateEngine = FreeMarkerTemplateEngine.create()


    override fun start(future: Future<Void?>) {
        val steps: Future<Void?> = prepareMongoDB().compose { ar -> createServer() }
        steps.setHandler { ar: AsyncResult<Void?> ->
            if (ar.succeeded()) {
                future.complete()
            } else {
                future.fail(ar.cause())
            }
        }
    }
    override fun stop() {

    }
    private fun prepareMongoDB(): Future<Void>? {
        val future = Future.future<Void>()
        val mongoConfig: JsonObject = JsonObject()
            .put("connection_string", "mongodb://localhost:27017")
            .put("db_name", "crud")
        mongoClient = MongoClient.createShared(vertx, mongoConfig).getCollections { ar ->
            if (ar.succeeded()) {
                future.complete()
            } else {
                future.fail(ar.cause())
            }
        }
        return future
    }

    private fun createServer(): Future<Void>? {
        val future = Future.future<Void>()
        val router: Router = Router.router(vertx)

        router.get("/api/articles").handler(this::getArticles)
        router.get("/api/articles/save").handler(this::getSavePage)
        router.post("/api/articles/save").handler(this::saveDocument)
        router.post("/api/articles/remove").handler(this::removeDocument)
        vertx.createHttpServer().requestHandler(router::accept)
            .listen(
                config().getInteger("http.server.port", 8091)
            ) { result: AsyncResult<HttpServer?> ->
                if (result.succeeded()) {
                    future.complete()
                } else {
                    future.fail(result.cause())
                }
            }
        return future
    }

    private fun getArticles(routingContext: RoutingContext) {
        val objects = JsonArray()
        val stream: ReadStream<JsonObject> = fetchArticles()
        stream.exceptionHandler { throwable: Throwable ->
        }
            .handler { value: JsonObject? -> objects.add(value) }
            .endHandler { v: Void? ->
                routingContext.put("articles", objects.toString())
            }
    }

    private fun fetchArticles(): ReadStream<JsonObject> {
        return mongoClient!!.findBatch("motor", JsonObject())
    }

    private fun getSavePage(routingContext: RoutingContext) {
    }
    private fun saveDocument(routingContext: RoutingContext) {
        mongoClient!!.save("motor", routingContext.bodyAsJson) { req: AsyncResult<String> ->
            var result: String? = ""
            var statusCode: Int = 200
            if (req.succeeded()) {
                result = if (req.result()
                        .isEmpty()
                ) Json.encodePrettily("Document could not be inserted") else Json.encodePrettily("Inserted doc id: " + req.result())
            } else {
                result = "Internal Server Error" + req.cause()
                statusCode = 500
            }
            responseHandler(routingContext, statusCode)
        }
    }
    private fun removeDocument(routingContext: RoutingContext) {
        mongoClient!!.removeDocuments("motor", routingContext.bodyAsJson
        ) { req: AsyncResult<MongoClientDeleteResult?> ->
            if (req.succeeded()) {
                val objects = JsonArray()
                val stream = fetchArticles()
                stream.exceptionHandler { throwable: Throwable ->

                }
                    .handler { value: JsonObject? -> objects.add(value) }
                    .endHandler { v: Void? ->
                        responseHandler(
                            routingContext,
                            200,

                        )
                    }
            } else {
                responseHandler(routingContext, 200)
            }
        }
    }
    private fun responseHandler(routingContext: RoutingContext, statusCode: Int) {
        routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(statusCode)
            .end()
    }

}
