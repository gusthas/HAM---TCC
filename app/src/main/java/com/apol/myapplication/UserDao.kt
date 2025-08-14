package com.apol.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.apol.myapplication.data.model.User
import com.apol.myapplication.data.model.WeightEntry

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

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Insert
    suspend fun insertWeightEntry(entry: WeightEntry)

    @Query("SELECT * FROM weight_history WHERE userEmail = :email ORDER BY date ASC")
    suspend fun getWeightHistory(email: String): List<WeightEntry>
}

