package com.ebiznext.flume.interceptor

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flume.event.EventBuilder
import org.apache.flume.interceptor.Interceptor
import org.apache.flume.{Context, Event}

import scala.beans.BeanProperty

/**
  *
  * Created by smanciot on 15/05/16.
  */
class JSONInterceptor extends Interceptor{

  val mapper = new ObjectMapper()

  override def initialize(): Unit = {}

  override def close(): Unit = {}

  import scala.collection.JavaConversions._

  override def intercept(event: Event): Event = {
    val headers = event.getHeaders
    val jsonEvent = mapper.readValue[JSONEvent](new String(event.getBody), classOf[JSONEvent])
    headers.putAll(jsonEvent.headers)
    EventBuilder.withBody(jsonEvent.body.getBytes(), headers)
  }

  override def intercept(events: util.List[Event]): util.List[Event] = {
    events.map{intercept}
  }

}

class JSONEvent(){
  @BeanProperty var headers: util.Map[String, String] = null
  @BeanProperty var body: String = null
}

class JSONBuilder extends Interceptor.Builder{
  override def build(): Interceptor = new JSONInterceptor

  override def configure(context: Context): Unit = {}
}
