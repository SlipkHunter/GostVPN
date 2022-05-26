package com.slipkprojects.gostvpn.di

import com.slipkprojects.gostvpn.domain.LocalRepository
import com.slipkprojects.gostvpn.data.LocalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindsProfileRepository(
        profileRepositoryImpl: LocalRepositoryImpl
    ): LocalRepository
}