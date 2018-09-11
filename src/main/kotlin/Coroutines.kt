package com.github.jasync.mysql.example

import com.github.jasync.sql.db.Configuration
import com.github.jasync.sql.db.Connection
import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.general.ArrayRowData
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
  //just to make sure which log level is active
  logger.error("starting")
  logger.warn("starting warn")
  logger.info("starting info")
  logger.debug("starting debug")
  logger.trace("starting trace")
  val connection: Connection = PostgreSQLConnection(
      Configuration(
          username = "username",
          password = "password",
          host = "host.com",
          port = 3306,
          database = "schema"
      )
  )
  connection.connect().get()

  launch {
    val queryResult = connection.sendPreparedStatementAwait("select * from table limit 2")
    println((queryResult.rows!![0] as ArrayRowData).columns.toList())
    println((queryResult.rows!![1] as ArrayRowData).columns.toList())
  }

  launch {
    val future = connection.sendPreparedStatement("select * from table limit 2")
    val queryResult = future.await()
    println((queryResult.rows!![0] as ArrayRowData).columns.toList())
    println((queryResult.rows!![1] as ArrayRowData).columns.toList())
  }

  connection.disconnect().get()
}

suspend fun Connection.sendPreparedStatementAwait(query: String, values: List<Any> = emptyList()): QueryResult =
    this.sendPreparedStatement(query, values).await()

