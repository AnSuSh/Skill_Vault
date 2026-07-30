package com.quickthought.skillvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDAO {
    @Query("SELECT * FROM credit_cards ORDER BY dateCreated DESC")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCreditCardById(id: Int): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(creditCard: CreditCardEntity)

    @Update
    suspend fun updateCreditCard(creditCard: CreditCardEntity)

    @Delete
    suspend fun deleteCreditCard(creditCard: CreditCardEntity)
}
