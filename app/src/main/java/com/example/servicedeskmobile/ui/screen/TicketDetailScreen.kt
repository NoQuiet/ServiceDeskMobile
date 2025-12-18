package com.example.servicedeskmobile.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.servicedeskmobile.data.model.Attachment
import com.example.servicedeskmobile.data.model.Comment
import com.example.servicedeskmobile.data.model.Ticket
import com.example.servicedeskmobile.data.model.TicketStatus
import com.example.servicedeskmobile.ui.viewmodel.CommentViewModel
import com.example.servicedeskmobile.ui.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: Int,
    ticketViewModel: TicketViewModel,
    commentViewModel: CommentViewModel,
    userRole: String?,
    userId: Int?,
    onNavigateBack: () -> Unit
) {
    val ticket by ticketViewModel.currentTicket.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val context = LocalContext.current
    var newComment by remember { mutableStateOf("") }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedCommentImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var lastCommentId by remember { mutableStateOf<Int?>(null) }
    
    val commentImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedCommentImages = uris.take(5)
        }
    }
    
    LaunchedEffect(ticketId) {
        ticketViewModel.loadTicket(ticketId)
        ticketViewModel.loadTicketAttachments(ticketId)
        commentViewModel.loadComments(ticketId)
    }
    
    val ticketAttachments by ticketViewModel.ticketAttachments.collectAsState()
    val commentAttachments by commentViewModel.commentAttachments.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявка #$ticketId") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TicketInfoCard(
                            ticket = ticket!!, 
                            userRole = userRole, 
                            ticketViewModel = ticketViewModel, 
                            attachments = ticketAttachments,
                            onShowStatusDialog = { showStatusDialog = true }, 
                            onShowRatingDialog = { showRatingDialog = true },
                            onShowAssignDialog = { showAssignDialog = true }
                        )
                    }
                    
                    item {
                        Text(
                            text = "Комментарии (${comments.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (comments.isEmpty()) {
                        item {
                            Text(
                                text = "Комментариев пока нет",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    items(comments) { comment ->
                        LaunchedEffect(comment.id) {
                            commentViewModel.loadCommentAttachments(comment.id)
                        }
                        CommentCard(
                            comment = comment,
                            currentUserId = userId,
                            attachments = commentAttachments[comment.id] ?: emptyList(),
                            onDelete = {
                                commentViewModel.deleteComment(comment.id, ticketId)
                            }
                        )
                    }
                }
                
                Divider()
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            placeholder = { Text("Добавить комментарий") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { commentImagePickerLauncher.launch("image/*") }
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Добавить фото")
                        }
                        
                        IconButton(
                            onClick = {
                                if (newComment.isNotBlank()) {
                                    commentViewModel.addComment(
                                        ticketId = ticketId,
                                        message = newComment,
                                        context = if (selectedCommentImages.isNotEmpty()) context else null,
                                        imageUris = if (selectedCommentImages.isNotEmpty()) selectedCommentImages else null
                                    )
                                    newComment = ""
                                    selectedCommentImages = emptyList()
                                }
                            },
                            enabled = newComment.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Отправить")
                        }
                    }
                    
                    if (selectedCommentImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedCommentImages) { uri ->
                                Box {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedCommentImages = selectedCommentImages.filter { it != uri }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showStatusDialog && ticket != null) {
        StatusChangeDialog(
            currentStatus = ticket!!.status,
            onDismiss = { showStatusDialog = false },
            onConfirm = { newStatus ->
                ticketViewModel.updateStatus(ticketId, newStatus)
                showStatusDialog = false
            }
        )
    }
    
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onConfirm = { rating ->
                ticketViewModel.rateTicket(ticketId, rating)
                showRatingDialog = false
            }
        )
    }
    
    if (showAssignDialog && ticket != null) {
        AssignTicketDialog(
            ticketViewModel = ticketViewModel,
            ticketId = ticketId,
            onDismiss = { showAssignDialog = false }
        )
    }
}

@Composable
fun TicketInfoCard(
    ticket: Ticket,
    userRole: String?,
    ticketViewModel: TicketViewModel,
    attachments: List<com.example.servicedeskmobile.data.model.Attachment>,
    onShowStatusDialog: () -> Unit,
    onShowRatingDialog: () -> Unit,
    onShowAssignDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Статус:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    StatusChip(status = ticket.status)
                }
                
                if (ticket.rating != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Оценка:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "⭐ ${ticket.rating}/5",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Создана: ${formatDate(ticket.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Автор заявки:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ticket.firstName ?: ""} ${ticket.lastName ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (ticket.email != null) {
                        Text(
                            text = ticket.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ticket.position != null) {
                        Text(
                            text = ticket.position,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ticket.mobilePhone != null) {
                        Text(
                            text = "Моб. тел: ${ticket.mobilePhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ticket.internalPhone != null) {
                        Text(
                            text = "Внутр. тел: ${ticket.internalPhone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (ticket.floor != null && ticket.officeNumber != null) {
                        Text(
                            text = "Кабинет: ${ticket.officeNumber}, этаж ${ticket.floor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (ticket.floor != null) {
                        Text(
                            text = "Этаж: ${ticket.floor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (ticket.officeNumber != null) {
                        Text(
                            text = "Кабинет: ${ticket.officeNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (ticket.supportFirstName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Исполнитель:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${ticket.supportFirstName} ${ticket.supportLastName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Фото (${attachments.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attachments) { attachment ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                com.example.servicedeskmobile.util.ImageUtils.getImageUrl(attachment.filePath)
                            ),
                            contentDescription = attachment.fileName,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            if ((userRole == "support" || userRole == "admin") && ticket.status != TicketStatus.closed && ticket.status != TicketStatus.archived) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (userRole == "support" && ticket.assignedTo == null) {
                        Button(
                            onClick = { 
                                ticketViewModel.assignTicket(ticket.id)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Взять в работу")
                        }
                    }
                    
                    if (userRole == "admin") {
                        Button(
                            onClick = onShowAssignDialog,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (ticket.assignedTo == null) "Назначить" else "Переназначить")
                        }
                    }
                    
                    Button(
                        onClick = onShowStatusDialog,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Изменить статус")
                    }
                }
            }
            
            if (userRole == "user" && (ticket.status == TicketStatus.resolved || ticket.status == TicketStatus.closed) && ticket.rating == null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onShowRatingDialog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Оценить работу")
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    currentUserId: Int?,
    attachments: List<Attachment>,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    android.util.Log.d("CommentCard", "Comment userId: ${comment.userId}, currentUserId: $currentUserId, match: ${currentUserId == comment.userId}")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (comment.role == "user") 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${comment.firstName} ${comment.lastName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (currentUserId == comment.userId) {
                    IconButton(
                        onClick = {
                            android.util.Log.d("CommentCard", "Delete button clicked for comment ${comment.id}")
                            showDeleteDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить комментарий",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = comment.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attachments) { attachment ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                com.example.servicedeskmobile.util.ImageUtils.getImageUrl(attachment.filePath)
                            ),
                            contentDescription = attachment.fileName,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить комментарий?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun StatusChangeDialog(
    currentStatus: TicketStatus,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить статус") },
        text = {
            Column {
                listOf("new", "in_progress", "resolved", "closed").forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getStatusLabel(status))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedStatus) }) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оценить работу") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выберите оценку от 1 до 5")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Оценка $star",
                                tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rating) }) {
                Text("Оценить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AssignTicketDialog(
    ticketViewModel: TicketViewModel,
    ticketId: Int,
    onDismiss: () -> Unit
) {
    var supportUsers by remember { mutableStateOf<List<com.example.servicedeskmobile.data.model.User>>(emptyList()) }
    var selectedSupportId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val userViewModel = com.example.servicedeskmobile.ui.viewmodel.UserViewModel()
        userViewModel.loadUsers("support")
        kotlinx.coroutines.delay(500)
        supportUsers = userViewModel.users.value
        isLoading = false
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Назначить исполнителя") },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (supportUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет доступных специалистов")
                }
            } else {
                LazyColumn {
                    items(supportUsers.size) { index ->
                        val user = supportUsers[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSupportId == user.id,
                                onClick = { selectedSupportId = user.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${user.lastName} ${user.firstName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = user.position,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedSupportId?.let { supportId ->
                        ticketViewModel.assignTicketToSupport(ticketId, supportId)
                    }
                    onDismiss()
                },
                enabled = selectedSupportId != null
            ) {
                Text("Назначить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun getStatusLabel(status: String): String {
    return when (status) {
        "new" -> "Новая"
        "in_progress" -> "В работе"
        "resolved" -> "Решена"
        "closed" -> "Закрыта"
        "archived" -> "Архив"
        else -> status
    }
}
