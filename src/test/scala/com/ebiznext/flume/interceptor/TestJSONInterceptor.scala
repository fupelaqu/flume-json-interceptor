package com.ebiznext.flume.interceptor

import java.net.URL

import com.google.common.collect.Lists
import org.apache.flume.Context
import org.apache.flume.channel.{ChannelProcessor, ReplicatingChannelSelector, MemoryChannel}
import org.apache.flume.conf.Configurables
import org.apache.flume.source.{ExecSource, AbstractSource}
import org.junit.Assert._
import org.junit.{Test, Before}

/**
  *
  * Created by smanciot on 15/05/16.
  */
class TestJSONInterceptor {
  private var source: AbstractSource = null

  @Before
  def setUp(): Unit = {
    source = new ExecSource
  }

  @Test
  def testIntercept(): Unit = {
    val channel = new MemoryChannel
    val rcs = new ReplicatingChannelSelector
    rcs.setChannels(Lists.newArrayList(channel))

    val channelProcessor = new ChannelProcessor(rcs)

    val context = new Context

    // Configure Source
    val resource: URL = Thread.currentThread.getContextClassLoader.getResource("json.log")
    context.put("command", s"cat ${resource.getFile}")
    context.put("keep-alive", "1")
    context.put("capacity", "100000")
    context.put("transactionCapacity", "1000")

    // Configure Interceptors
    context.put("interceptors", "i1 i2")
    context.put("interceptors.i1.type", "timestamp")
    context.put("interceptors.i2.type", "com.ebiznext.flume.interceptor.JSONBuilder")

    Configurables.configure(source, context)
    Configurables.configure(channel, context)
    Configurables.configure(channelProcessor, context)

    source.setChannelProcessor(channelProcessor)
    source.start()

    val transaction = channel.getTransaction

    transaction.begin()

    val event = channel.take()
    val bytes: Array[Byte] = event.getBody
    assertEquals("hello world", new String(bytes))
    val headers = event.getHeaders
    assertEquals("index", headers.get("index"))
    assertEquals("type", headers.get("type"))
    assertNotNull(headers.get("timestamp"))

    assertNull(channel.take())

    transaction.commit()
    transaction.close()

    source.stop()
  }
}
