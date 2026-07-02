package com.tpc.nudj.ui.screen.auth.emailVerified

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tpc.nudj.R
import com.tpc.nudj.ui.theme.LocalAppColors
import com.tpc.nudj.ui.theme.NudjTheme
import com.tpc.nudj.viewmodels.auth.emailVerified.EmailVerifiedViewModel

@Composable
fun EmailVerifiedScreen(
    viewModel: EmailVerifiedViewModel = hiltViewModel(),
    onNavigateToUserDetailsInput: () -> Unit,
    onNavigateToClubVerificationScreen: () -> Unit
) {
    val snackbarHostState = remember {SnackbarHostState()}

   LaunchedEffect(Unit) {
       viewModel.OnScreenOpened()
   }
    LaunchedEffect(Unit) {
        viewModel.events.collect {event ->
            when (event) {
                is EmailVerifiedEvent.ShowSnackBar -> snackbarHostState.showSnackbar(event.message)
                EmailVerifiedEvent.NavigateToUserDetailsInput -> onNavigateToUserDetailsInput()
                EmailVerifiedEvent.NavigateToClubVerificationScreen -> onNavigateToClubVerificationScreen()
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = LocalAppColors.current.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val imageID = if(isSystemInDarkTheme()) R.drawable.email_verified_dark_theme
                            else R.drawable.emailverified
                Image(
                    painter = painterResource(imageID),
                    contentDescription = "Email Verified",
                    modifier = Modifier
                        .width(250.dp)
                        .height(200.dp)
                )
                Text(
                    text = "Your email address was successfully verified!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.onBackground
                )
            }
            Spacer(modifier = Modifier.height(112.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true , uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailVerifiedScreenPreview() {
    NudjTheme {
        EmailVerifiedScreen(
            onNavigateToUserDetailsInput = {},
            onNavigateToClubVerificationScreen = {}
        )
    }
}
