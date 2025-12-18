package com.example.servicedeskmobile.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.servicedeskmobile.data.model.Ticket
import com.example.servicedeskmobile.data.model.TicketPriority
import com.example.servicedeskmobile.data.model.TicketStatus
import com.example.servicedeskmobile.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    ticketViewModel: TicketViewModel,
    userRole: String?,
    onTicketClick: (Int) -> Unit,
    onCreateTicket: () -> Unit
) {
    val tickets by ticketViewModel.tickets.collectAsState()
    val activeTickets = tickets.filter { 
        it.status != TicketStatus.resolved && 
        it.status != TicketStatus.closed && 
        it.status != TicketStatus.archived 
    }
    
    LaunchedEffect(Unit) {
        ticketViewModel.loadTickets()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявки") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTicket) {
                Icon(Icons.Default.Add, contentDescription = "Создать заявку")
            }
        }
    ) { padding ->
        if (activeTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Нет заявок")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeTickets) { ticket ->
                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    val isOverdue = isTicketOverdue(ticket.deadline)
    val isVip = ticket.priority == TicketPriority.vip
    
    val borderColor = when {
        isOverdue -> Color.Red
        isVip -> Color(0xFFFFD700)
        else -> Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (borderColor != Color.Transparent) BorderStroke(2.dp, borderColor) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (ticket.priority == TicketPriority.vip) {
                    AssistChip(
                        onClick = {},
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VIP")
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFFD700),
                            labelColor = Color(0xFF000000)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                StatusChip(status = ticket.status)
                
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = formatDate(ticket.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = getDeadlineText(ticket.deadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (ticket.supportFirstName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Исполнитель: ${ticket.supportFirstName} ${ticket.supportLastName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: TicketStatus) {
    val (label, color, icon) = when (status) {
        TicketStatus.new -> Triple("Новая", Color(0xFF2196F3), Icons.Default.FiberNew)
        TicketStatus.in_progress -> Triple("В работе", Color(0xFFFF9800), Icons.Default.Schedule)
        TicketStatus.resolved -> Triple("Решена", Color(0xFF4CAF50), Icons.Default.CheckCircle)
        TicketStatus.closed -> Triple("Закрыта", Color(0xFF9E9E9E), Icons.Default.Lock)
        TicketStatus.archived -> Triple("Закрыта", Color(0xFF9E9E9E), Icons.Default.Lock)
    }
    
    AssistChip(
        onClick = {},
        label = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(label)
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color,
            labelColor = Color.White
        )
    )
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

fun isTicketOverdue(deadlineString: String): Boolean {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val deadline = inputFormat.parse(deadlineString)
        deadline?.before(Date()) ?: false
    } catch (e: Exception) {
        false
    }
}

fun getDeadlineText(deadlineString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val deadline = inputFormat.parse(deadlineString) ?: return "Срок: неизвестно"
        val now = Date()
        val diffMillis = deadline.time - now.time
        
        if (diffMillis < 0) {
            val overdueDays = TimeUnit.MILLISECONDS.toDays(-diffMillis)
            val overdueHours = TimeUnit.MILLISECONDS.toHours(-diffMillis) % 24
            return when {
                overdueDays > 0 -> "Просрочено на $overdueDays дн."
                overdueHours > 0 -> "Просрочено на $overdueHours ч."
                else -> "Просрочено"
            }
        } else {
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
            
            return when {
                days > 0 -> "Осталось: $days дн. $hours ч."
                hours > 0 -> "Осталось: $hours ч. $minutes мин."
                else -> "Осталось: $minutes мин."
            }
        }
    } catch (e: Exception) {
        "Срок: неизвестно"
    }
}
