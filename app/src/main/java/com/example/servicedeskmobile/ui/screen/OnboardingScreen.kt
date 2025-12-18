package com.example.servicedeskmobile.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добро пожаловать!") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(
                    page = when (page) {
                        0 -> OnboardingPageData(
                            icon = Icons.Default.Info,
                            title = "Service Desk Mobile",
                            description = "Добро пожаловать в систему управления заявками!\n\nЭто приложение поможет вам быстро и удобно создавать заявки в службу технической поддержки, отслеживать их статус и общаться со специалистами."
                        )
                        1 -> OnboardingPageData(
                            icon = Icons.Default.Add,
                            title = "Создание заявки",
                            description = "1. Нажмите кнопку \"+\" на главном экране\n\n2. Заполните название и описание проблемы\n\n3. Нажмите \"Создать\"\n\nВаша заявка будет автоматически отправлена в службу поддержки!"
                        )
                        2 -> OnboardingPageData(
                            icon = Icons.Default.List,
                            title = "Отслеживание заявок",
                            description = "На вкладке \"Заявки\" вы видите все активные заявки:\n\n• Новая - заявка создана\n• В работе - специалист работает над решением\n\nНажмите на заявку, чтобы увидеть детали и переписку."
                        )
                        3 -> OnboardingPageData(
                            icon = Icons.Default.Send,
                            title = "Общение со специалистом",
                            description = "В деталях заявки вы можете:\n\n• Просматривать комментарии специалиста\n• Отправлять свои сообщения\n• Уточнять детали проблемы\n\nВся переписка сохраняется в истории заявки."
                        )
                        4 -> OnboardingPageData(
                            icon = Icons.Default.Star,
                            title = "Оценка работы",
                            description = "После решения проблемы:\n\n1. Заявка переходит в статус \"Решена\"\n\n2. Вы можете оценить работу специалиста от 1 до 5 звезд\n\n3. После оценки заявка перемещается в архив\n\nВ архиве хранятся все завершенные заявки с полной историей."
                        )
                        else -> OnboardingPageData(
                            icon = Icons.Default.Info,
                            title = "",
                            description = ""
                        )
                    }
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Назад")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(8.dp)
                        ) {
                            Icon(
                                imageVector = if (index == pagerState.currentPage) 
                                    Icons.Default.Circle 
                                else 
                                    Icons.Default.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp),
                                tint = if (index == pagerState.currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                
                if (pagerState.currentPage < 4) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text("Далее")
                    }
                } else {
                    Button(onClick = onFinish) {
                        Text("Войти")
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(page: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val description: String
)
