package de.tum.hack.jb.interhyp.challenge.presentation.insights

/**
 * Data point representing projected savings at a specific month
 */
data class ProjectedSavingsPoint(
    val monthIndex: Int, // 0 = current month, 1 = next month, etc.
    val cumulativeSavings: Double
)

