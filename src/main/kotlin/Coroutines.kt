package com.github.jasync.mysql.example

import com.github.jasync.sql.db.Connection
import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.general.ArrayRowData
import com.github.jasync.sql.db.pool.PoolConfiguration
import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    //just to make sure which log level is active
    logger.error("starting")
    logger.warn("starting warn")
    logger.info("starting info")
    logger.debug("starting debug")
    logger.trace("starting trace")
    val poolConfiguration = PoolConfiguration(
        100,                            // maxObjects
        TimeUnit.MINUTES.toMillis(15),  // maxIdle
        10_000,                         // maxQueueSize
        TimeUnit.SECONDS.toMillis(30)   // validationInterval
    )
    val host = "host.com"
    val port = 3306
    val database = "schema"
    val username = "username"
    val password = "password"
    val pool = PostgreSQLConnectionBuilder.createConnectionPool(
        "jdbc:postgresql://$host:$port/$database?user=$username&password=$password"
    )

    GlobalScope.launch {
        val connection = pool.asSuspending.connect()
        val queryResult = connection.sendPreparedStatementAwait("select * from table limit 2")
        println((queryResult.rows!![0] as ArrayRowData).columns.toList())
        println((queryResult.rows!![1] as ArrayRowData).columns.toList())
    }

    GlobalScope.launch {
        val connection = pool.asSuspending.connect()
        val future = connection.sendPreparedStatement("select * from table limit 2")
        val queryResult = future.await()
        println((queryResult.rows!![0] as ArrayRowData).columns.toList())
        println((queryResult.rows!![1] as ArrayRowData).columns.toList())
    }

    pool.disconnect().get()
}

suspend fun Connection.sendPreparedStatementAwait(query: String, values: List<Any> = emptyList()): QueryResult =
    this.sendPreparedStatement(query, values).await()

