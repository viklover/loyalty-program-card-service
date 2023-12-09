package ru.viklover.cards.repository

import org.springframework.stereotype.Repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

import kotlinx.coroutines.flow.Flow

@Repository
interface CardRepository : CoroutineCrudRepository<Card, Long> {

    @Query("insert into card default values")
    suspend fun createCard()

    @Query("select * from card limit :limit offset :offset")
    fun findAllBy(limit: Int?, offset: Int?): Flow<Card>

    @Query("select * from card where is_owned = :isOwned limit :limit offset :offset")
    fun findByOwned(isOwned: Boolean, limit: Int?, offset: Int?): Flow<Card>

    @Query("update card set customer_id=:customerId, is_owned=true, released_at=now() where id=:cardId")
    suspend fun releaseCard(cardId: Long, customerId: Long)

    @Query("update card set is_blocked=true, blocked_at=now() where id=:cardId")
    suspend fun blockCard(cardId: Long)
}
