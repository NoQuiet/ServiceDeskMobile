package com.example.servicedeskmobile.ui.screen

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
import androidx.compose.ui.unit.dp
import com.example.servicedeskmobile.data.model.User
import com.example.servicedeskmobile.ui.viewmodel.TicketViewModel
import com.example.servicedeskmobile.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userViewModel: UserViewModel,
    ticketViewModel: TicketViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Пользователи", "Специалисты")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Администрирование") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> UsersTab(userViewModel, "user")
                1 -> SupportTab(userViewModel)
            }
        }
    }
}

@Composable
fun UsersTab(userViewModel: UserViewModel, role: String) {
    val users by userViewModel.users.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    
    LaunchedEffect(role) {
        android.util.Log.d("AdminScreen", "Loading users with role: $role")
        userViewModel.loadUsers(role)
    }
    
    LaunchedEffect(users) {
        android.util.Log.d("AdminScreen", "Users list updated: ${users.size} users")
    }
    
    when {
        userState is com.example.servicedeskmobile.ui.viewmodel.UserState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        users.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет пользователей")
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(user = user, userViewModel = userViewModel)
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, userViewModel: UserViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.lastName} ${user.firstName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.position,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (user.isBlocked != 0) "Разблокировать" else "Заблокировать") },
                            onClick = {
                                showBlockDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (user.isBlocked != 0) Icons.Default.Check else Icons.Default.Block,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            if (user.isBlocked != 0) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("Заблокирован") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить пользователя?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.deleteUser(user.id)
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
    
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(if (user.isBlocked != 0) "Разблокировать пользователя?" else "Заблокировать пользователя?") },
            text = { Text(if (user.isBlocked != 0) "Пользователь сможет снова войти в систему" else "Пользователь не сможет войти в систему") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.blockUser(user.id, user.isBlocked == 0)
                        showBlockDialog = false
                    }
                ) {
                    Text(if (user.isBlocked != 0) "Разблокировать" else "Заблокировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SupportTab(userViewModel: UserViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val users by userViewModel.users.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    
    LaunchedEffect(Unit) {
        android.util.Log.d("AdminScreen", "Loading support users")
        userViewModel.loadUsers("support")
    }
    
    LaunchedEffect(users) {
        android.util.Log.d("AdminScreen", "Support users list updated: ${users.size} users")
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить специалиста")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserCard(user = user, userViewModel = userViewModel)
            }
        }
    }
    
    if (showCreateDialog) {
        CreateSupportDialog(
            userViewModel = userViewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun CreateSupportDialog(
    userViewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать специалиста") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Отчество") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    userViewModel.createSupport(
                        email, password, firstName, lastName,
                        middleName.ifBlank { null }
                    )
                    onDismiss()
                },
                enabled = email.isNotBlank() && password.isNotBlank() && 
                         firstName.isNotBlank() && lastName.isNotBlank()
            ) {
                Text("Создать")
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
fun ArchiveTab(ticketViewModel: TicketViewModel) {
    val archivedTickets by ticketViewModel.archivedTickets.collectAsState()
    
    LaunchedEffect(Unit) {
        ticketViewModel.loadArchivedTickets()
    }
    
    if (archivedTickets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Архив пуст")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(archivedTickets) { ticket ->
                TicketCard(ticket = ticket, onClick = {})
            }
        }
    }
}
