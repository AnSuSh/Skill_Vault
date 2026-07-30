package com.quickthought.skillvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDAO {
    @Query("SELECT * FROM addresses ORDER BY dateCreated DESC")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id LIMIT 1")
    suspend fun getAddressById(id: Int): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)
}
