package com.uzabase.newspicks.api

import scala.collection.mutable.Map
import scala.collection.mutable.Set
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.search.aggregations.AggregationBuilders.max
import org.elasticsearch.search.aggregations.metrics.max.InternalMax
import com.typesafe.scalalogging.slf4j.Logging
import com.uzabase.newspicks.log.cli.Config
import com.uzabase.newspicks.user.DummyUser
import com.uzabase.newspicks.log.file.ElasticSearchRequest
import org.elasticsearch.action.index.IndexRequestBuilder


class ElasticsearchApi(implicit config: Config) extends Logging {
  var client: Client = new TransportClient(ImmutableSettings.settingsBuilder()
    .put("cluster.name", "pickslogsearch")
    .put("transport.tcp.compress", true)
    .put("client.transport.sniff", true)
    .build())
    .addTransportAddress(new InetSocketTransportAddress(config.host, 9300))

  def insert(index: String, typeOfDocument: String, id: String, document: String) = {
    var builder = client.prepareIndex(index, typeOfDocument, id)
    builder.setRouting(id)
    builder.setSource(document)
    var response = builder.execute().actionGet()
  }

  def insertBulk(index: String, typeOfDocument: String, list: List[ElasticSearchRequest]) = {
    var routing = ""
    val map = Map[String, Set[ElasticSearchRequest]]()

    //parentが存在しない場合は登録できないので、いったんuidのあるlogのみ登録する
    list.filter(l => l.routing.toInt > 0).foreach(e => {
      val entry = map.getOrElse(e.routing, Set[ElasticSearchRequest]())
      entry.add(e)
      map.put(e.routing, entry);
    })
    
    DummyUser.createNewUser(map)

    map.foreach {
      case (routing, set) => {
        var builder = client.prepareIndex(index, typeOfDocument)
        builder.setParent(routing)
        set.foreach(request => builder.setSource(request.json))

        try {
          var response = builder.execute().actionGet()
        } catch {
          case e: Exception =>
            logger.info(s"ERROR routing : ${routing} : ${set}")
            e.printStackTrace()
        }
      }
    }
    val builder = client.prepareBulk()
    list.foreach(req => {
    	builder.add(createIndexRequest(req))
    })
    builder.execute();
  }
  def createIndexRequest(request : ElasticSearchRequest) :IndexRequestBuilder = {

    val index =
        client
            .prepareIndex("newspicks", "log", request.routing)
            .setSource(request.json);

    index;
  }
  def close = {
    client.close()
  }

  def searchLastUser = {
    val builder = client.prepareSearch().setIndices("newspicks").setTypes("user")
      .setQuery(matchAllQuery())
      .addAggregation(max("uid").field("uid"))
    val response = builder.execute().get

    val results: InternalMax = response.getAggregations().get("uid")
    val uid = results.getValue().toInt

    if (uid >= 0) {
      uid.toInt
    } else {
      -1
    }
  }
}