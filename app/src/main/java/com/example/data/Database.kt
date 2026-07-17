package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val category: String,
    val tips: String
)

@Entity(tableName = "reward_badges")
data class RewardBadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val iconName: String,
    val pointsRequired: Int,
    val isUnlocked: Boolean,
    val pointsValue: Int
)

@Entity(tableName = "gatah")
data class GatahEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val totalAmount: Double,
    val amountPerPerson: Double,
    val collectedAmount: Double,
    val membersCount: Int,
    val paidMembersCount: Int,
    val isCompleted: Boolean,
    val date: String,
    val category: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val amount: Double,
    val description: String,
    val date: String,
    val category: String,
    val isExpense: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val financialLiteracy: Int = 55 // Starts at 55% out of 100% (Vision 2030 target is to raise it)
)

@Dao
interface FinanceDao {
    // Goals queries
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)

    // Rewards queries
    @Query("SELECT * FROM reward_badges ORDER BY id ASC")
    fun getAllBadges(): Flow<List<RewardBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<RewardBadgeEntity>)

    @Update
    suspend fun updateBadge(badge: RewardBadgeEntity)

    // Transactions queries
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    // Chat queries
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // User Profile queries
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Gatah (Split Bill) queries
    @Query("SELECT * FROM gatah ORDER BY id DESC")
    fun getAllGatahs(): Flow<List<GatahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGatah(gatah: GatahEntity): Long

    @Update
    suspend fun updateGatah(gatah: GatahEntity)

    @Query("DELETE FROM gatah WHERE id = :id")
    suspend fun deleteGatah(id: Int)
}

@Database(
    entities = [
        GoalEntity::class,
        RewardBadgeEntity::class,
        TransactionEntity::class,
        ChatMessageEntity::class,
        UserProfileEntity::class,
        GatahEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_intelligent_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
