package com.patch.spray_can_pong

object App {
  import cc.spray.can._
  import akka.actor.{ Actor, Props, Scheduler}
  import HttpMethods._

  class PingService extends Actor {
    // self.id = id

    protected def receive = {
      case RequestContext(HttpRequest(GET, "/ping", _, _, _), _, responder) =>
        responder.complete(response("PONG"))
      case Timeout(_, _, _, _, _, complete) =>
        complete(HttpResponse().withBody("TIMEOUT"))
    }

    val defaultHeaders = List(HttpHeader("Content-Type", "text/plain"))
    def response(msg: String, status: Int = 200) = HttpResponse(status, defaultHeaders , msg.getBytes("ISO-8859-1"))
  }
}

/** embedded server */
object Server {
  import cc.spray.can._
  import akka.actor.ActorSystem
  import akka.actor.{ Actor, Props, Scheduler}
  import java.util.concurrent.CountDownLatch

  def main(args: Array[String]) {
    import App._
    import akka.dispatch.Await
    val latch = new CountDownLatch(1)
    val system = ActorSystem("ping_pong")
    val ping = system.actorOf(Props(new PingService), name = "ping-endpoint")
    val server = system.actorOf(Props(new HttpServer(ServerConfig(
      port = 8080,
      serviceActorName = "ping-endpoint",
      requestTimeout = 1000))))
    latch.await()
  }
}
