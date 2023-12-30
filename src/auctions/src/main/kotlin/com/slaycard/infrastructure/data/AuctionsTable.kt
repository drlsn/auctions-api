package com.slaycard.infrastructure.data

import PropertyList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object AuctionsTable : UUIDTable("auctions") {
    val sellingUserId           = integer("sellingUserId")
    val auctionItemName         = varchar("auctionItemName", 30)
    val auctionItemId           = uuid("auctionItemId").entityId()
    val quantity                = integer("quantity")
    val startingPrice           = integer("startingPrice")
    val currentPrice            = integer("currentPrice")
    val startTime               = datetime("startTime")
    val originalDurationHours   = integer("originalDurationHours")
    val description             = varchar("description", 2000)
    val properties              = json<PropertyList>(
                                    "propertyList",
                                    { Json.encodeToString(it as PropertyList) },
                                    { Json.decodeFromString(it) as PropertyList })
}