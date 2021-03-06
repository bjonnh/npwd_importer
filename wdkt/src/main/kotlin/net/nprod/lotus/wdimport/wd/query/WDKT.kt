/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.query

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * A way to run queries directly using WDTK
 */
class WDKT : IWDKT {
    private val client: HttpClient = HttpClient(CIO) {
        engine {
            endpoint {
                keepAliveTime = KEEP_ALIVE_TIMEOUT
                connectTimeout = CONNECT_TIMEOUT
                connectAttempts = CONNECT_ATTEMPTS
            }
        }
    }

    override fun close(): Unit = client.close()

    @Deprecated("You should use searchForPropertyValue instead", level = DeprecationLevel.WARNING)
    override fun searchDOI(doi: String): QueryActionResponse = searchForPropertyValue(MainInstanceItems.doi, doi)

    override fun searchForPropertyValue(property: PropertyIdValue, value: String): QueryActionResponse {
        val out: String = runBlocking {
            client.get("https://www.wikidata.org/w/api.php") {
                parameter("action", "query")
                parameter("format", "json")
                parameter("list", "search")
                parameter("srsearch", "haswbstatement:\"${property.id}=$value\"")
            }
        }
        return Json.decodeFromString(QueryActionResponse.serializer(), out)
    }
}
