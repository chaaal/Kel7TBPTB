package com.example.kerjapraktek

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.di.Injection
import com.example.kerjapraktek.ui.addReply.AddReplyViewModel
import com.example.kerjapraktek.ui.arsip.ArsipViewModel
import com.example.kerjapraktek.ui.home.HomeViewModel
import com.example.kerjapraktek.ui.info.InfoViewModel
import com.example.kerjapraktek.ui.login.LoginViewModel
import com.example.kerjapraktek.ui.profile.ProfileViewModel
import com.example.kerjapraktek.ui.reply.ReplyViewModel
import com.example.kerjapraktek.ui.replyDetail.ReplyDetailViewModel
import com.example.kerjapraktek.ui.requestDetail.RequestDetailViewModel

class ViewModelFactory(private val repository: UserRepository,
                       private val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(InfoViewModel::class.java) -> {
                InfoViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ArsipViewModel::class.java) -> {
                ArsipViewModel(repository, context) as T
            }
            modelClass.isAssignableFrom(RequestDetailViewModel::class.java) -> {
                RequestDetailViewModel(repository, context) as T
            }
            modelClass.isAssignableFrom(ReplyViewModel::class.java) -> {
                ReplyViewModel(repository, context) as T
            }
            modelClass.isAssignableFrom(ReplyDetailViewModel::class.java) -> {
                ReplyDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddReplyViewModel::class.java) -> {
                AddReplyViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }





            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(
                    Injection.provideRepository(context),
                    context.applicationContext // Gunakan applicationContext untuk menghindari memory leak
                ).also { INSTANCE = it }
            }
        }
    }
}