package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 1,
    val brandName: String = "",
    val playerName: String = "",
    val funds: Double = 1000000.0, // Pre-seeded starting budget
    val currentLevel: Int = 1, // Progressing from Level 1 to 4+
    val fans: Int = 120, // Brand followers
    val phonesCreatedCount: Int = 0
)

@Entity(tableName = "created_phones")
data class CreatedPhone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,
    val colorNameName: String,
    val processorName: String,
    val cameraName: String,
    val designTypeName: String,
    val manufacturingCost: Double,
    val retailPrice: Double,
    val unitsSold: Int,
    val revenue: Double,
    val netProfit: Double,
    val starRating: Float,
    val feedbackMessage: String,
    val levelAssoc: Int,
    val timestamp: Long = System.currentTimeMillis()
)
