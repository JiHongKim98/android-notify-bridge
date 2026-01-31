package com.example.kakaodiscord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.kakaodiscord.data.NotificationRule
import com.example.kakaodiscord.ui.screen.RuleEditScreen
import com.example.kakaodiscord.ui.screen.RuleListScreen
import com.example.kakaodiscord.ui.theme.NotifyBridgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotifyBridgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NotifyBridgeApp()
                }
            }
        }
    }
}

sealed class Screen {
    data object RuleList : Screen()

    data class RuleEdit(
        val rule: NotificationRule?,
    ) : Screen()
}

@Composable
fun NotifyBridgeApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.RuleList) }

    when (val screen = currentScreen) {
        is Screen.RuleList -> {
            RuleListScreen(
                onAddRule = {
                    currentScreen = Screen.RuleEdit(null)
                },
                onEditRule = { rule ->
                    currentScreen = Screen.RuleEdit(rule)
                },
            )
        }
        is Screen.RuleEdit -> {
            RuleEditScreen(
                existingRule = screen.rule,
                onSave = {
                    currentScreen = Screen.RuleList
                },
                onBack = {
                    currentScreen = Screen.RuleList
                },
            )
        }
    }
}
