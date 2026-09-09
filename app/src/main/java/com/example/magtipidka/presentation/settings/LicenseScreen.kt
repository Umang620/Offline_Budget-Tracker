package com.example.magtipidka.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val licenseText = """
Non-Commercial Modification License (NCML)

Copyright (c) 2026 Umang620

1. DEFINITIONS
"Software" means the source code, application, documentation, and other materials covered by this license.
"Copyright Holder" means Umang620.

2. GRANT OF PERMISSION
Permission is granted to any person obtaining a copy of the Software to:
a. Use the Software for personal and educational purposes.
b. Copy the Software.
c. Modify, adapt, and create derivative works from the Software.
d. Distribute the original or modified Software free of charge.

3. RESTRICTIONS
The following activities are prohibited without written permission from the Copyright Holder:
a. Selling the Software.
b. Selling modified or derivative versions of the Software.
c. Using the Software for commercial purposes.
d. Charging fees for access to, distribution of, or use of the Software.
e. Sublicensing the Software for commercial purposes.

4. COPYRIGHT
The original copyright and license notices must not be removed or intentionally altered.

5. ATTRIBUTION
When distributing the Software or a modified version, the original copyright notice and this license must be included.

6. COMMERCIAL PERMISSION
The Copyright Holder may grant written permission for commercial use, sale, or commercial distribution on a case-by-case basis.

7. DISCLAIMER OF WARRANTY
The Software is provided "as is", without warranty of any kind. The Copyright Holder is not responsible for damages or losses resulting from the use of the Software.

8. LIMITATION OF LIABILITY
To the maximum extent permitted by applicable law, the Copyright Holder shall not be liable for any claim, damages, or other liability arising from the use of the Software.

9. TERMINATION
Any rights granted under this license terminate automatically if the licensee violates its terms.

10. ACCEPTANCE
By using, copying, modifying, or distributing the Software, the user agrees to the terms of this license.

Copyright (c) 2026 Umang620
All rights reserved except as expressly permitted by this license.
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & License", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Mag Tipid Ka",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Offline-First Personal Budget & Expense Tracker",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "License",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = licenseText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
