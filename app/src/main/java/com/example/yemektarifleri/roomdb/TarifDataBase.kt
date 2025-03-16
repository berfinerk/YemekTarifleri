package com.example.yemektarifleri.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.yemektarifleri.model.Tarif
import com.example.yemektarifleri.room.TarifDAO

@Database(entities = [Tarif::class], version = 1)
abstract class TarifDataBase : RoomDatabase() {
    abstract fun TarifDao(): TarifDAO
}