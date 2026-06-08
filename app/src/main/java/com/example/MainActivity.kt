package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full-bleed content, including camera notches and bottom nav bars
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val gameUiState by viewModel.uiState.collectAsState()

                    when (val state = gameUiState) {
                        is GameUiState.Welcome -> {
                            WelcomeScreen(
                                onStartGame = { brandName, playerName ->
                                    viewModel.startNewGame(brandName, playerName)
                                }
                            )
                        }
                        is GameUiState.Dashboard -> {
                            DashboardScreen(
                                progress = state.progress,
                                history = state.history,
                                onSelectLevel = { levelNum ->
                                    viewModel.enterLevelBrief(levelNum)
                                },
                                onRestartGame = {
                                    viewModel.restartGameProgress()
                                },
                                onUpgradeWorker = { workerKey, cost ->
                                    viewModel.upgradeWorker(workerKey, cost)
                                }
                            )
                        }
                        is GameUiState.LevelBrief -> {
                            LevelBriefScreen(
                                progress = state.progress,
                                challenge = state.challenge,
                                onStartCreator = {
                                    viewModel.startPhoneCreator(state.challenge)
                                },
                                onBack = {
                                    viewModel.backToDashboard()
                                }
                            )
                        }
                        is GameUiState.Creator -> {
                            PhoneCreatorScreen(
                                progress = state.progress,
                                challenge = state.challenge,
                                selectedColor = state.selectedColor,
                                selectedProcessor = state.selectedProcessor,
                                selectedCamera = state.selectedCamera,
                                selectedDesign = state.selectedDesign,
                                selectedBattery = state.selectedBattery,
                                selectedTechs = state.selectedTechs,
                                phoneName = state.currentPhoneName,
                                onSpecsChangedRaw = { color, processor, camera, design, battery, techs, name ->
                                    viewModel.updateCreatorState(color, processor, camera, design, battery, techs, name)
                                },
                                onSubmitSpecs = {
                                    viewModel.advanceToValuation()
                                },
                                onCancel = {
                                    viewModel.backToDashboard()
                                }
                            )
                        }
                        is GameUiState.Valuation -> {
                            ValuationScreen(
                                progress = state.progress,
                                challenge = state.challenge,
                                stats = state.stats,
                                phoneName = state.phoneName,
                                selectedColor = state.selectedColor,
                                selectedProcessor = state.selectedProcessor,
                                selectedCamera = state.selectedCamera,
                                selectedDesign = state.selectedDesign,
                                selectedBattery = state.selectedBattery,
                                selectedTechs = state.selectedTechs,
                                proposedPrice = state.proposedPrice,
                                researchTier = state.researchTier,
                                manufacturingQuality = state.manufacturingQuality,
                                marketingCampaign = state.marketingCampaign,
                                onPriceChanged = { price ->
                                    viewModel.updateProposedPrice(price)
                                },
                                onBudgetChanged = { research, quality, marketing ->
                                    viewModel.updateValuationBudgets(research, quality, marketing)
                                },
                                onLaunch = {
                                    viewModel.simulateMarketLaunch()
                                },
                                onBackToCreator = {
                                    viewModel.backToCreator()
                                },
                                onBackToDashboard = {
                                    viewModel.backToDashboard()
                                }
                            )
                        }
                        is GameUiState.MarketSimulationResult -> {
                            MarketResultScreen(
                                challenge = state.challenge,
                                phoneName = state.phoneName,
                                specs = state.specs,
                                retailPrice = state.retailPrice,
                                unitsSold = state.unitsSold,
                                grossRevenue = state.grossRevenue,
                                netProfit = state.netProfit,
                                finalRating = state.finalRating,
                                customerReviews = state.customerReviews,
                                isLevelBeaten = state.isLevelBeaten,
                                competitorPhones = state.competitorPhones,
                                onFinish = {
                                    viewModel.backToDashboard()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
