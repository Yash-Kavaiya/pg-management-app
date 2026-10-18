package com.example.pgmanagerpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pgmanagerpro.data.local.dao.*
import com.example.pgmanagerpro.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PGEntity::class,
        RoomEntity::class,
        TenantEntity::class,
        BookingEntity::class,
        PaymentEntity::class,
        NotificationPrefEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PGDatabase : RoomDatabase() {
    abstract fun pgDao(): PGDao
    abstract fun roomDao(): RoomDao
    abstract fun tenantDao(): TenantDao
    abstract fun bookingDao(): BookingDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationPrefDao(): NotificationPrefDao

    companion object {
        @Volatile
        private var INSTANCE: PGDatabase? = null

        fun getDatabase(context: Context): PGDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PGDatabase::class.java,
                    "pg_manager_pro_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(db: PGDatabase) {
            // Seed PG properties
            val pgs = listOf(
                PGEntity("pg-1", "Sunrise Executive Stays", "123 Tech Park Road, Whitefield", "Bangalore", 24, 20, 180000.0),
                PGEntity("pg-2", "Green View Luxury PG", "456 Cyber City, HSR Layout", "Bangalore", 18, 15, 135000.0),
                PGEntity("pg-3", "Urban Nest Co-Living", "789 Metro Pillar 42, Indiranagar", "Bangalore", 30, 28, 252000.0)
            )
            db.pgDao().insertAllPGs(pgs)

            // Seed Rooms for pg-1
            val roomsPg1 = listOf(
                RoomEntity(1, "pg-1", "101", "Single", 12000.0, 24000.0, "Occupied", 1),
                RoomEntity(2, "pg-1", "102", "Double", 8500.0, 17000.0, "Occupied", 1),
                RoomEntity(3, "pg-1", "103", "Double", 8500.0, 17000.0, "Available", 1),
                RoomEntity(4, "pg-1", "104", "Triple", 6500.0, 13000.0, "Occupied", 1),
                RoomEntity(5, "pg-1", "201", "Single", 12500.0, 25000.0, "Occupied", 2),
                RoomEntity(6, "pg-1", "202", "Double", 9000.0, 18000.0, "Available", 2),
                RoomEntity(7, "pg-1", "203", "Double", 9000.0, 18000.0, "Occupied", 2),
                RoomEntity(8, "pg-1", "204", "Triple", 7000.0, 14000.0, "Maintenance", 2),
                RoomEntity(9, "pg-1", "301", "Single", 13000.0, 26000.0, "Available", 3),
                RoomEntity(10, "pg-1", "302", "Double", 9500.0, 19000.0, "Occupied", 3)
            )
            db.roomDao().insertAllRooms(roomsPg1)

            // Seed Tenants for pg-1
            val tenantsPg1 = listOf(
                TenantEntity(1, "pg-1", "Rahul Sharma", "101", "+91 98765 43210", "rahul.s@gmail.com", "Active", "2023-01-15"),
                TenantEntity(2, "pg-1", "Priya Patel", "102", "+91 98765 43211", "priya.p@gmail.com", "Active", "2023-02-01"),
                TenantEntity(3, "pg-1", "Amit Kumar", "104", "+91 98765 43212", "amit.k@gmail.com", "Active", "2023-03-10"),
                TenantEntity(4, "pg-1", "Sneha Verma", "201", "+91 98765 43213", "sneha.v@gmail.com", "Active", "2023-04-05"),
                TenantEntity(5, "pg-1", "Vikram Singh", "203", "+91 98765 43214", "vikram.s@gmail.com", "Notice Period", "2022-11-20"),
                TenantEntity(6, "pg-1", "Ananya Roy", "302", "+91 98765 43215", "ananya.r@gmail.com", "Active", "2023-05-12")
            )
            db.tenantDao().insertAllTenants(tenantsPg1)

            // Seed Bookings
            val bookings = listOf(
                BookingEntity(1, "pg-1", "101", "Rahul Sharma", "rahul.s@gmail.com", "+91 98765 43210", "2023-01-15", "2024-01-14", "Active", 12000.0, 24000.0, 5000.0, "Ramesh Sharma", "+91 98765 00000", "Father", "2023-01-15", "", "Prefers quiet room"),
                BookingEntity(2, "pg-1", "103", "Karan Gupta", "karan.g@gmail.com", "+91 98765 43216", "2023-09-01", "2024-08-31", "Upcoming", 8500.0, 17000.0, 3000.0, "Suresh Gupta", "+91 98765 00001", "Father", "", "", "Check-in expected on 1st Sept"),
                BookingEntity(3, "pg-1", "202", "Neha Reddy", "neha.r@gmail.com", "+91 98765 43217", "2023-09-15", "2024-09-14", "Pending", 9000.0, 18000.0, 0.0, "Venkatesh Reddy", "+91 98765 00002", "Father", "", "", "Awaiting token amount")
            )
            db.bookingDao().insertAllBookings(bookings)

            // Seed Payments
            val payments = listOf(
                PaymentEntity(1, "pg-1", "Rahul Sharma", 1, "101", 12000.0, "2023-08-05", "Paid", "2023-08-03", "UPI", "Rent", "REC-1001", "TXN987654321", "August Rent"),
                PaymentEntity(2, "pg-1", "Priya Patel", 2, "102", 8500.0, "2023-08-05", "Paid", "2023-08-05", "UPI", "Rent", "REC-1002", "TXN987654322", "August Rent"),
                PaymentEntity(3, "pg-1", "Amit Kumar", 3, "104", 6500.0, "2023-08-05", "Pending", null, "", "Rent", "", "", "August Rent"),
                PaymentEntity(4, "pg-1", "Sneha Verma", 4, "201", 12500.0, "2023-08-05", "Overdue", null, "", "Rent", "", "", "August Rent"),
                PaymentEntity(5, "pg-1", "Vikram Singh", 5, "203", 9000.0, "2023-08-05", "Paid", "2023-08-04", "Bank Transfer", "Rent", "REC-1003", "TXN987654323", "August Rent")
            )
            db.paymentDao().insertAllPayments(payments)

            // Seed Notification Prefs
            val prefs = NotificationPrefEntity("pg-1", emailEnabled = true, smsEnabled = true, daysBefore = 3, sendOverdueReminders = true, reminderTime = "09:00")
            db.notificationPrefDao().insertOrUpdatePreferences(prefs)
        }
    }
}
