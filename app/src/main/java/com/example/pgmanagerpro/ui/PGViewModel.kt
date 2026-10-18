package com.example.pgmanagerpro.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pgmanagerpro.data.local.PGDatabase
import com.example.pgmanagerpro.data.local.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PGViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PGDatabase.getDatabase(application)
    private val pgDao = db.pgDao()
    private val roomDao = db.roomDao()
    private val tenantDao = db.tenantDao()
    private val bookingDao = db.bookingDao()
    private val paymentDao = db.paymentDao()
    private val notificationPrefDao = db.notificationPrefDao()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (pgDao.getPGCount() == 0) {
                    PGDatabase.seedInitialData(db)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val allPGs: StateFlow<List<PGEntity>> = pgDao.getAllPGs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPgId = MutableStateFlow("pg-1")
    val selectedPgId: StateFlow<String> = _selectedPgId.asStateFlow()

    private val _themeMode = MutableStateFlow("System") // "System", "Light", "Dark"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPG: StateFlow<PGEntity?> = _selectedPgId
        .flatMapLatest { pgDao.getPGById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val rooms: StateFlow<List<RoomEntity>> = _selectedPgId
        .flatMapLatest { roomDao.getRoomsByPG(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tenants: StateFlow<List<TenantEntity>> = _selectedPgId
        .flatMapLatest { tenantDao.getTenantsByPG(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookings: StateFlow<List<BookingEntity>> = _selectedPgId
        .flatMapLatest { bookingDao.getBookingsByPG(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val payments: StateFlow<List<PaymentEntity>> = _selectedPgId
        .flatMapLatest { paymentDao.getPaymentsByPG(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notificationPrefs: StateFlow<NotificationPrefEntity?> = _selectedPgId
        .flatMapLatest { notificationPrefDao.getPreferences(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectPG(pgId: String) {
        _selectedPgId.value = pgId
    }

    // PG Operations
    fun addPG(name: String, address: String, city: String, totalRooms: Int) {
        viewModelScope.launch {
            val newId = "pg-${System.currentTimeMillis()}"
            val newPG = PGEntity(newId, name, address, city, totalRooms, 0, 0.0)
            pgDao.insertPG(newPG)
            _selectedPgId.value = newId
        }
    }

    fun updatePG(pg: PGEntity) {
        viewModelScope.launch {
            pgDao.updatePG(pg)
        }
    }

    // Room Operations
    fun addRoom(number: String, type: String, rent: Double, deposit: Double, floor: Int, status: String = "Available") {
        viewModelScope.launch {
            val currentPgId = _selectedPgId.value
            val room = RoomEntity(pgId = currentPgId, number = number, type = type, rent = rent, deposit = deposit, floor = floor, status = status)
            roomDao.insertRoom(room)
        }
    }

    fun updateRoom(room: RoomEntity) {
        viewModelScope.launch {
            roomDao.updateRoom(room)
        }
    }

    fun deleteRoom(roomId: Int) {
        viewModelScope.launch {
            roomDao.deleteRoom(roomId)
        }
    }

    // Tenant Operations
    fun addTenant(name: String, room: String, phone: String, email: String, status: String = "Active", joinDate: String) {
        viewModelScope.launch {
            val currentPgId = _selectedPgId.value
            val tenant = TenantEntity(pgId = currentPgId, name = name, room = room, phone = phone, email = email, status = status, joinDate = joinDate)
            tenantDao.insertTenant(tenant)
        }
    }

    fun updateTenant(tenant: TenantEntity) {
        viewModelScope.launch {
            tenantDao.updateTenant(tenant)
        }
    }

    fun deleteTenant(tenantId: Int) {
        viewModelScope.launch {
            tenantDao.deleteTenant(tenantId)
        }
    }

    // Booking Operations
    fun addBooking(
        room: String,
        tenant: String,
        email: String,
        phone: String,
        startDate: String,
        endDate: String,
        status: String,
        monthlyRent: Double,
        securityDeposit: Double,
        advance: Double,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyContactRelation: String,
        notes: String
    ) {
        viewModelScope.launch {
            val currentPgId = _selectedPgId.value
            val booking = BookingEntity(
                pgId = currentPgId,
                room = room,
                tenant = tenant,
                email = email,
                phone = phone,
                startDate = startDate,
                endDate = endDate,
                status = status,
                monthlyRent = monthlyRent,
                securityDeposit = securityDeposit,
                advance = advance,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                emergencyContactRelation = emergencyContactRelation,
                notes = notes,
                createdAt = "2026-08-09"
            )
            bookingDao.insertBooking(booking)
        }
    }

    fun updateBooking(booking: BookingEntity) {
        viewModelScope.launch {
            bookingDao.updateBooking(booking)
        }
    }

    fun deleteBooking(bookingId: Int) {
        viewModelScope.launch {
            bookingDao.deleteBooking(bookingId)
        }
    }

    // Payment Operations
    fun addPayment(
        tenant: String,
        room: String,
        amount: Double,
        dueDate: String,
        status: String,
        paymentMethod: String,
        paymentType: String,
        paidDate: String? = null,
        transactionId: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val currentPgId = _selectedPgId.value
            val receiptNo = "REC-${(1000..9999).random()}"
            val payment = PaymentEntity(
                pgId = currentPgId,
                tenant = tenant,
                room = room,
                amount = amount,
                dueDate = dueDate,
                status = status,
                paidDate = paidDate,
                paymentMethod = paymentMethod,
                paymentType = paymentType,
                receiptNumber = receiptNo,
                transactionId = transactionId,
                notes = notes,
                createdAt = "2026-08-09"
            )
            paymentDao.insertPayment(payment)
        }
    }

    fun updatePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            paymentDao.updatePayment(payment)
        }
    }

    fun deletePayment(paymentId: Int) {
        viewModelScope.launch {
            paymentDao.deletePayment(paymentId)
        }
    }

    // Notification Prefs
    fun updateNotificationPrefs(pref: NotificationPrefEntity) {
        viewModelScope.launch {
            notificationPrefDao.insertOrUpdatePreferences(pref)
        }
    }
}
