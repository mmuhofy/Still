package com.still.app.di

import com.still.app.data.repository.DriveRepositoryImpl
import com.still.app.domain.repository.DriveRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DriveModule {

    @Binds
    @Singleton
    abstract fun bindDriveRepository(impl: DriveRepositoryImpl): DriveRepository
}