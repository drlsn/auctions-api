package com.slaycard.infrastructure

import com.slaycard.basic.domain.Entity
import com.slaycard.entities.roots.AuctionRepository
import io.ktor.util.collections.*


//class InMemoryRepository<TEntity, TId> : AuctionRepository<TEntity, TId>
//    where TEntity: Entity<TId> {
//
//    private val items: ConcurrentMap<TId, TEntity> = ConcurrentMap()
//
//    override fun get(id: TId): TEntity? = items[id]
//    override fun getAll(): List<TEntity> = items.values.toList()
//
//    override fun add(entity: TEntity): Boolean =
//        when (items.containsKey(entity.id)) {
//            true -> false
//            else -> { items[entity.id] = entity; true }
//        }
//
//    override fun update(entity: TEntity): Boolean =
//        when (!items.containsKey(entity.id)) {
//            true -> false
//            else -> {
//                entity.version++
//                items[entity.id] = entity; true
//            }
//        }
//
//    override fun delete(id: TId): Boolean =
//        items.remove(id) != null
//}
