package com.apol.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)
    @Update
    suspend fun updateUser(user: User)
}

