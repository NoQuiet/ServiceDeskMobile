package com.example.servicedeskmobile.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicedeskmobile.data.model.Ticket
import com.example.servicedeskmobile.data.repository.TicketRepository
import com.example.servicedeskmobile.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketViewModel : ViewModel() {
    
    private val repository = TicketRepository()
    
    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()
    
    private val _currentTicket = MutableStateFlow<Ticket?>(null)
    val currentTicket: StateFlow<Ticket?> = _currentTicket.asStateFlow()
    
    private val _ticketState = MutableStateFlow<TicketState>(TicketState.Idle)
    val ticketState: StateFlow<TicketState> = _ticketState.asStateFlow()
    
    private val _archivedTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val archivedTickets: StateFlow<List<Ticket>> = _archivedTickets.asStateFlow()
    
    private val _ticketAttachments = MutableStateFlow<List<com.example.servicedeskmobile.data.model.Attachment>>(emptyList())
    val ticketAttachments: StateFlow<List<com.example.servicedeskmobile.data.model.Attachment>> = _ticketAttachments.asStateFlow()
    
    fun loadTickets() {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.getTickets()
            if (result.isSuccess) {
                _tickets.value = result.getOrNull() ?: emptyList()
                _ticketState.value = TicketState.Idle
            } else {
                _ticketState.value = TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки")
            }
        }
    }
    
    fun loadTicket(id: Int) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.getTicket(id)
            if (result.isSuccess) {
                _currentTicket.value = result.getOrNull()
                _ticketState.value = TicketState.Idle
            } else {
                _ticketState.value = TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки")
            }
        }
    }
    
    fun createTicket(title: String, description: String) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.createTicket(title, description)
            _ticketState.value = if (result.isSuccess) {
                loadTickets()
                val ticketId = result.getOrNull()
                TicketState.Success("Заявка создана", ticketId)
            } else {
                TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка создания")
            }
        }
    }
    
    fun uploadTicketImages(ticketId: Int, context: Context, imageUris: List<Uri>) {
        viewModelScope.launch {
            val result = repository.uploadTicketAttachments(ticketId, context, imageUris)
            if (result.isFailure) {
                android.util.Log.e("TicketViewModel", "Failed to upload images: ${result.exceptionOrNull()?.message}")
            } else {
                loadTicketAttachments(ticketId)
            }
        }
    }
    
    fun loadTicketAttachments(ticketId: Int) {
        viewModelScope.launch {
            val result = repository.getTicketAttachments(ticketId)
            if (result.isSuccess) {
                _ticketAttachments.value = result.getOrNull() ?: emptyList()
            }
        }
    }
    
    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.updateTicketStatus(id, status)
            _ticketState.value = if (result.isSuccess) {
                loadTicket(id)
                loadTickets()
                TicketState.Success(result.getOrNull() ?: "Статус обновлен")
            } else {
                TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка обновления")
            }
        }
    }
    
    fun assignTicket(id: Int, supportId: Int? = null) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.assignTicket(id, supportId)
            _ticketState.value = if (result.isSuccess) {
                loadTicket(id)
                loadTickets()
                TicketState.Success(result.getOrNull() ?: "Заявка назначена")
            } else {
                TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка назначения")
            }
        }
    }
    
    fun assignTicketToSupport(ticketId: Int, supportId: Int) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.assignTicket(ticketId, supportId)
            _ticketState.value = if (result.isSuccess) {
                loadTicket(ticketId)
                loadTickets()
                TicketState.Success(result.getOrNull() ?: "Заявка назначена на специалиста")
            } else {
                TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка назначения")
            }
        }
    }
    
    fun rateTicket(id: Int, rating: Int) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.rateTicket(id, rating)
            _ticketState.value = if (result.isSuccess) {
                loadTicket(id)
                TicketState.Success(result.getOrNull() ?: "Оценка сохранена")
            } else {
                TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка оценки")
            }
        }
    }
    
    fun loadArchivedTickets() {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            val result = repository.getArchivedTickets()
            if (result.isSuccess) {
                _archivedTickets.value = result.getOrNull() ?: emptyList()
                _ticketState.value = TicketState.Idle
            } else {
                _ticketState.value = TicketState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки архива")
            }
        }
    }
    
    fun resetState() {
        _ticketState.value = TicketState.Idle
    }
}

sealed class TicketState {
    object Idle : TicketState()
    object Loading : TicketState()
    data class Success(val message: String, val data: Any? = null) : TicketState()
    data class Error(val message: String) : TicketState()
}
