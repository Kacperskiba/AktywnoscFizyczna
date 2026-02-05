package com.example.projekt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projekt.data.database.entity.DailyStepsEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StepsBarChart(
    weeklySteps: List<DailyStepsEntity>,
    modifier: Modifier = Modifier
) {
    val maxSteps = weeklySteps.maxOfOrNull { it.steps } ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklySteps.forEach { daySteps ->
            val heightFraction = if (maxSteps > 0) {
                (daySteps.steps.toFloat() / maxSteps).coerceIn(0.05f, 1f)
            } else 0.05f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${daySteps.steps / 1000}k",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight(heightFraction)
                        .width(20.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(primaryColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getDayLabel(daySteps.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WeeklyProgressChart(
    weeklySteps: List<DailyStepsEntity>,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklySteps.forEach { daySteps ->
            val progressPercent = if (goal > 0) {
                (daySteps.steps.toFloat() / goal * 100).coerceAtMost(100f)
            } else 0f

            val heightFraction = (progressPercent / 100f).coerceIn(0.05f, 1f)
            val barColor = if (progressPercent >= 100f) secondaryColor else primaryColor

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${progressPercent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight(heightFraction)
                        .width(24.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getDayLabel(daySteps.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getDayLabel(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: return ""
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Pn"
            Calendar.TUESDAY -> "Wt"
            Calendar.WEDNESDAY -> "Sr"
            Calendar.THURSDAY -> "Cz"
            Calendar.FRIDAY -> "Pt"
            Calendar.SATURDAY -> "So"
            Calendar.SUNDAY -> "Nd"
            else -> ""
        }
    } catch (e: Exception) {
        ""
    }
}
