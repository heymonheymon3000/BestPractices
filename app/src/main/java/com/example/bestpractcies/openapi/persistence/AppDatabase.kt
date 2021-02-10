package com.example.bestpractcies.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bestpractcies.openapi.models.auth.AccountProperties
import com.example.bestpractcies.openapi.models.auth.AuthToken
import com.example.bestpractcies.openapi.models.main.blog.BlogPost
import com.example.bestpractcies.openapi.persistence.auth.AccountPropertiesDao
import com.example.bestpractcies.openapi.persistence.auth.AuthTokenDao
import com.example.bestpractcies.openapi.persistence.main.BlogPostDao

@Database(entities = [AuthToken::class, AccountProperties::class, BlogPost::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object {
        const val DATABASE_NAME = "app_db"
    }
}