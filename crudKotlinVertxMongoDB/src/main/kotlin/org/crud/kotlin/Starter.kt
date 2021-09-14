package org.crud.kotlin

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import org.crud.kotlin.verticle.RestServer


object Starter {

    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(RestServer()) { res: AsyncResult<String> ->
            if (res.succeeded()) {
            } else {
            }
        }
    }
}


//
//object KotlinMongoConnectionExample {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        var mongoClient: com.mongodb.MongoClient? = null
//        try {
//            mongoClient = com.mongodb.MongoClient("localhost", 27017)
//            println("Connected to MongoDB!")
//        } catch (e: MongoException) {
//            e.printStackTrace()
//        } finally {
//            mongoClient!!.close()
//        }
//
////        val mongoDatabase = mongoClient.getDatabase("crud")
////        val mongoCollection = mongoDatabase.getCollection("motor")
//        val db: MongoDatabase = mongoClient.getDatabase("crud")
//        val collection: MongoCollection<Document> = db.getCollection("motor")
//
//
//        val vertx = Vertx.vertx()
//        val httpServer = vertx.createHttpServer()
//        val router = Router.router(vertx)
//
//
//
//        router.get("/json")
//            .handler({ routingContext ->
//                val response = routingContext.response()
//                response.putHeader("content-type", "application/json")
//                    .setChunked(true)
//                    .write(Json.encodePrettily(responObj("name")))
//                    .end()
//            })
//
//        router.post("/motor")
//            .handler({ routingConttext ->
//            })
//
//        httpServer
//            .requestHandler(router::accept)
//            .listen(8091)
//
//    }
//
//    data class responObj(var Name: String = "")
//}
