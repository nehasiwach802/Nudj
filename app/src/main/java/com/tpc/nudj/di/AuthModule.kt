package com.tpc.nudj.di

import com.google.firebase.auth.FirebaseAuth
import com.tpc.nudj.repository.auth.AuthRepository
import com.tpc.nudj.repository.auth.FirebaseAuthRepository
import com.tpc.nudj.repository.user.UserRepository
import com.tpc.nudj.repository.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        userRepository: UserRepository
    ): AuthRepository {
        return FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance(),
            userRepository = userRepository
        )
    }
}