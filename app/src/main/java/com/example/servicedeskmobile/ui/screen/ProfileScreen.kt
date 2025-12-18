package com.example.servicedeskmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.servicedeskmobile.data.model.UpdateProfileRequest
import com.example.servicedeskmobile.data.model.User
import com.example.servicedeskmobile.ui.viewmodel.AuthViewModel
import com.example.servicedeskmobile.ui.viewmodel.UserState
import com.example.servicedeskmobile.ui.viewmodel.UserViewModel
import com.example.servicedeskmobile.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Выход")
                    }
                }
            )
        }
    ) { padding ->
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (isEditing) {
                EditProfileContent(
                    user = currentUser!!,
                    userViewModel = userViewModel,
                    authViewModel = authViewModel,
                    onSave = { isEditing = false },
                    onCancel = { isEditing = false },
                    modifier = Modifier.padding(padding)
                )
            } else {
                ViewProfileContent(
                    user = currentUser!!,
                    themeViewModel = themeViewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun ViewProfileContent(user: User, themeViewModel: ThemeViewModel, modifier: Modifier = Modifier) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Темная тема",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { themeViewModel.toggleTheme() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ProfileField("Email", user.email)
                ProfileField("Фамилия", user.lastName)
                ProfileField("Имя", user.firstName)
                user.middleName?.let { ProfileField("Отчество", it) }
                ProfileField("Должность", user.position)
                user.mobilePhone?.let { ProfileField("Мобильный телефон", it) }
                user.internalPhone?.let { ProfileField("Внутренний телефон", it) }
                user.floor?.let { ProfileField("Этаж", it.toString()) }
                user.officeNumber?.let { ProfileField("Номер кабинета", it) }
                ProfileField("Роль", getRoleLabel(user.role.name))
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EditProfileContent(
    user: User,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var middleName by remember { mutableStateOf(user.middleName ?: "") }
    var mobilePhone by remember { mutableStateOf(user.mobilePhone ?: "") }
    var internalPhone by remember { mutableStateOf(user.internalPhone ?: "") }
    var floor by remember { mutableStateOf(user.floor?.toString() ?: "") }
    var officeNumber by remember { mutableStateOf(user.officeNumber ?: "") }
    var position by remember { mutableStateOf(user.position) }
    
    val userState by userViewModel.userState.collectAsState()
    
    LaunchedEffect(userState) {
        when (userState) {
            is UserState.Success -> {
                authViewModel.reloadCurrentUser()
                onSave()
                userViewModel.resetState()
            }
            else -> {}
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = position,
            onValueChange = { position = it },
            label = { Text("Должность") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = mobilePhone,
            onValueChange = { mobilePhone = it },
            label = { Text("Мобильный телефон") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = internalPhone,
            onValueChange = { internalPhone = it },
            label = { Text("Внутренний телефон") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = floor,
                onValueChange = { floor = it },
                label = { Text("Этаж") },
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            OutlinedTextField(
                value = officeNumber,
                onValueChange = { officeNumber = it },
                label = { Text("Номер кабинета") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
            
            Button(
                onClick = {
                    val request = UpdateProfileRequest(
                        firstName = firstName,
                        lastName = lastName,
                        middleName = middleName.ifBlank { null },
                        mobilePhone = mobilePhone.ifBlank { null },
                        internalPhone = internalPhone.ifBlank { null },
                        floor = floor.toIntOrNull(),
                        officeNumber = officeNumber.ifBlank { null },
                        position = position
                    )
                    userViewModel.updateProfile(user.id, request)
                },
                modifier = Modifier.weight(1f),
                enabled = userState !is UserState.Loading
            ) {
                if (userState is UserState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить")
                }
            }
        }
        
        if (userState is UserState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (userState as UserState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun getRoleLabel(role: String): String {
    return when (role) {
        "admin" -> "Администратор"
        "support" -> "Специалист техподдержки"
        "user" -> "Пользователь"
        else -> role
    }
}
