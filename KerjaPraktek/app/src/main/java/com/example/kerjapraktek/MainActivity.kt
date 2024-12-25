package com.example.kerjapraktek

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kerjapraktek.ui.login.LoginScreen
import com.example.kerjapraktek.ui.home.HomeScreen
import com.example.kerjapraktek.data.pref.UserPreference
import com.example.kerjapraktek.data.pref.dataStore
import com.example.kerjapraktek.ui.addInfo.AddInfoScreen
import com.example.kerjapraktek.ui.addReply.AddReplyScreen
import com.example.kerjapraktek.ui.arsip.ArsipScreen
import com.example.kerjapraktek.ui.info.InfoScreen
import com.example.kerjapraktek.ui.infoDetail.InfoDetailScreen
import com.example.kerjapraktek.ui.profile.ProfileScreen
import com.example.kerjapraktek.ui.reply.ReplyScreen
import com.example.kerjapraktek.ui.replyDetail.ReplyDetailScreen
import com.example.kerjapraktek.ui.requestDetail.RequestDetailScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable


class MainActivity : ComponentActivity() {
    private lateinit var pusherService: PusherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pusherService = PusherService(this)
        pusherService.initialize()

        setContent {
            EvelinApp()
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EvelinApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreference = UserPreference.getInstance(context.dataStore)
    val userSession = userPreference.getSession().collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userSession.value) {
        val isLoggedIn = userSession.value?.isLogin == true
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
        Log.d("EvelinApp", "isLoggedIn: $isLoggedIn")
    }



    AnimatedNavHost(navController = navController, startDestination = if (userSession.value?.isLogin == true) "home" else "login") {
//    AnimatedNavHost(navController = navController, startDestination = "home") {
        composable(
            "login",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { LoginScreen(context, navController) }
        composable(
            "home",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { HomeScreen(navController = navController) }
        composable(
            "info",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { InfoScreen(navController = navController) }
        composable(
            "addInfo",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
            ) { AddInfoScreen(navController = navController) }
        composable(
            "infoDetail/{id}",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            InfoDetailScreen(navController = navController, infoId = id.toString())
            Log.d("Kerja Praktek", "infoDetail id: $id")
        }
        composable(
            "arsip",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { ArsipScreen(navController) }
        composable(
            "profile",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { ProfileScreen(navController) }
        composable(
            "reply",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { ReplyScreen(navController) }
        composable(
            "addReply",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            AddReplyScreen(navController = navController)
        }
        composable(
            "replyDetail/{replyId}",
            arguments = listOf(navArgument("replyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val replyId = backStackEntry.arguments?.getString("replyId") ?: return@composable
            ReplyDetailScreen(navController = navController, replyId = replyId)
        }
        composable(
            "requestDetail/{id}",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            RequestDetailScreen(navController = navController, requestId = id.toString())
            Log.d("Kerja Praktek", "requestDetail id: $id")
        }

    }
}