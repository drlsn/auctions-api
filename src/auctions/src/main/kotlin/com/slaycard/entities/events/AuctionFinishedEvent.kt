package com.slaycard.entities.events

import AuctionId
import com.slaycard.basic.domain.DomainEvent
import com.slaycard.basic.getUtcTimeNow
import com.slaycard.basic.uuid
import com.slaycard.entities.shared.Money
import com.slaycard.entities.shared.UserId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
class AuctionFinishedEvent(
    val auction: AuctionId,
    val itemName: String,
    val price: Money,
    val winner: UserId?,
    val endTime: LocalDateTime,
    override val id: String = uuid(),
    override val utcTimeOccurred: LocalDateTime = getUtcTimeNow()
) : DomainEvent()
