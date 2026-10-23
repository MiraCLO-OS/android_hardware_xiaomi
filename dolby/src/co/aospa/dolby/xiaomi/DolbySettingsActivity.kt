/*
 * Copyright (C) 2023-25 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.xiaomi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.android.settingslib.spa.framework.compose.localNavController
import com.android.settingslib.spa.framework.theme.SettingsTheme
import com.android.settingslib.spa.widget.scaffold.SettingsScaffold

class DolbySettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val controller = DolbyController.getInstance(applicationContext)
        setContent {
            SettingsTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(navController.localNavController()) {
                    SettingsScaffold(title = stringResource(R.string.dolby_title)) { paddingValues ->
                        DolbySettingsScreen(
                            context = this@DolbySettingsActivity,
                            controller = controller,
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }
            }
        }
    }
}
