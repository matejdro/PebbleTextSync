package com.matejdro.pebbletextsync.files.di

import app.cash.sqldelight.db.SqlDriver
import com.matejdro.pebbletextsync.files.sqldelight.generated.Database
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFileQueries
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface FilesDataProviders {
   @Provides
   fun provideMainDatabase(driver: SqlDriver): Database {
      return Database(driver)
   }

   @Provides
   fun provideFileQueries(database: Database): DbFileQueries {
      return database.dbFileQueries
   }
}
