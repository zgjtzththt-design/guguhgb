package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val playerProgress: Flow<PlayerProgress?> = gameDao.getPlayerProgress()
    val allCreatedPhones: Flow<List<CreatedPhone>> = gameDao.getAllCreatedPhones()

    suspend fun savePlayerProgress(progress: PlayerProgress) {
        gameDao.insertPlayerProgress(progress)
    }

    suspend fun saveCreatedPhone(phone: CreatedPhone) {
        gameDao.insertCreatedPhone(phone)
    }

    suspend fun resetGame() {
        gameDao.clearPlayerProgress()
        gameDao.clearCreatedPhones()
    }
}
