package com.example.six_fam

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ContactModel::class], version = 1, exportSchema = false)
public abstract class six_famDatabase :RoomDatabase() {

    abstract fun contactDao():ContactDao

    companion object{
        @Volatile
        private var INSTANCE :six_famDatabase?=null
        fun getDatabase(context: Context):six_famDatabase{
            INSTANCE?.let {
                return it
            }
            return synchronized(six_famDatabase::class.java){
                val instance = Room.databaseBuilder(context.applicationContext,six_famDatabase::class.java,"six_fam_db").build()
                INSTANCE=instance
                instance
            }
        }
    }
}