package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FinanceViewModel
import com.example.ui.theme.*
import com.example.data.*
import androidx.compose.ui.draw.shadow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Wrap the entire app in RTL layout direction for pristine Arabic support!
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        FinanceAppScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceAppScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Analytics, 2: Chat, 3: Goals, 4: Gat'at

    // Observables
    val goals by viewModel.goals.collectAsState()
    val gatahs by viewModel.gatahs.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val emailBillDetected by viewModel.emailBillDetected.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    // Notification Dialog State
    var showNotificationDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        val screenWidth = this.maxWidth
        val isDesktop = screenWidth >= 600.dp

        if (isDesktop) {
            // Computer / Wide screen layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Persistent Desktop Sidebar
                DesktopSidebar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userPoints = userProfile?.points ?: 0,
                    hasUnresolvedNotification = emailBillDetected,
                    onNotificationClick = { showNotificationDialog = true }
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(SleekTextMuted.copy(alpha = 0.08f))
                )

                // Main Workspace
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                    ) {
                        HeaderDesktop(
                            selectedTab = selectedTab,
                            userPoints = userProfile?.points ?: 0,
                            onNotificationClick = { showNotificationDialog = true },
                            hasUnresolvedNotification = emailBillDetected
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTab) {
                                0 -> HomeTabContent(
                                    goals = goals,
                                    transactions = transactions,
                                    currentLocation = currentLocation,
                                    emailBillDetected = emailBillDetected,
                                    financialLiteracy = userProfile?.financialLiteracy ?: 68,
                                    onApproveBill = { viewModel.approveEmailBill() },
                                    onIgnoreBill = { viewModel.ignoreEmailBill() },
                                    onSimulateLocation = { viewModel.simulateLocation(it) }
                                )
                                1 -> AnalyticsTabContent(
                                    transactions = transactions,
                                    literacyRate = userProfile?.financialLiteracy ?: 68
                                )
                                2 -> ChatTabContent(
                                    chatMessages = chatMessages,
                                    isChatLoading = isChatLoading,
                                    onSendMessage = { viewModel.sendChatMessage(it) },
                                    onClearHistory = { viewModel.clearChat() }
                                )
                                3 -> GoalsAndRewardsTabContent(
                                    goals = goals,
                                    badges = badges,
                                    userPoints = userProfile?.points ?: 0,
                                    onAddGoal = { title, target, date, cat -> viewModel.addGoal(title, target, date, cat) },
                                    onDeposit = { id, amount -> viewModel.depositToGoal(id, amount) },
                                    onDelete = { id -> viewModel.deleteGoal(id) },
                                    onRedeem = { badgeId, points -> viewModel.redeemReward(badgeId, points) }
                                )
                                4 -> GatahTabContent(
                                    gatahs = gatahs,
                                    userPoints = userProfile?.points ?: 0,
                                    onAddGatah = { title, total, members, cat -> viewModel.addGatah(title, total, members, cat) },
                                    onRecordPayment = { id -> viewModel.recordMemberPayment(id) },
                                    onDeleteGatah = { id -> viewModel.deleteGatah(id) }
                                )
                            }
                        }
                    }

                    // On ultra-wide desktop displays, we show a persistent live helper split-pane
                    if (screenWidth >= 950.dp && selectedTab != 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(SleekTextMuted.copy(alpha = 0.08f))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "الاستشارات المالية والتحليل المباشر 📈",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SleekPrimary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    "مزامنة فورية ودعم استشاري ذكي لتحسين قرارات الصرف الجماعي والفردي:",
                                    fontSize = 11.sp,
                                    color = SleekTextMuted,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    ChatTabContent(
                                        chatMessages = chatMessages,
                                        isChatLoading = isChatLoading,
                                        onSendMessage = { viewModel.sendChatMessage(it) },
                                        onClearHistory = { viewModel.clearChat() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Standard Mobile view
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                HeaderSection(
                    userPoints = userProfile?.points ?: 0,
                    hasUnresolvedNotification = emailBillDetected,
                    onNotificationClick = { showNotificationDialog = true }
                )

                // Main content area based on current tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> HomeTabContent(
                            goals = goals,
                            transactions = transactions,
                            currentLocation = currentLocation,
                            emailBillDetected = emailBillDetected,
                            financialLiteracy = userProfile?.financialLiteracy ?: 68,
                            onApproveBill = { viewModel.approveEmailBill() },
                            onIgnoreBill = { viewModel.ignoreEmailBill() },
                            onSimulateLocation = { viewModel.simulateLocation(it) }
                        )
                        1 -> AnalyticsTabContent(
                            transactions = transactions,
                            literacyRate = userProfile?.financialLiteracy ?: 68
                        )
                        2 -> ChatTabContent(
                            chatMessages = chatMessages,
                            isChatLoading = isChatLoading,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onClearHistory = { viewModel.clearChat() }
                        )
                        3 -> GoalsAndRewardsTabContent(
                            goals = goals,
                            badges = badges,
                            userPoints = userProfile?.points ?: 0,
                            onAddGoal = { title, target, date, cat -> viewModel.addGoal(title, target, date, cat) },
                            onDeposit = { id, amount -> viewModel.depositToGoal(id, amount) },
                            onDelete = { id -> viewModel.deleteGoal(id) },
                            onRedeem = { badgeId, points -> viewModel.redeemReward(badgeId, points) }
                        )
                        4 -> GatahTabContent(
                            gatahs = gatahs,
                            userPoints = userProfile?.points ?: 0,
                            onAddGatah = { title, total, members, cat -> viewModel.addGatah(title, total, members, cat) },
                            onRecordPayment = { id -> viewModel.recordMemberPayment(id) },
                            onDeleteGatah = { id -> viewModel.deleteGatah(id) }
                        )
                    }
                }

                // Bottom Navigation Bar
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    emailBillDetected = emailBillDetected
                )
            }
        }
    }

    // Notifications Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("إغلاق", color = SleekPrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    "التنبيهات الاستباقية",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (emailBillDetected) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SleekAssistantContainer.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Mail", tint = SleekPrimary)
                                Column {
                                    Text("فاتورة غير مدمجة", fontWeight = FontWeight.Bold, color = SleekTextDark, fontSize = 12.sp)
                                    Text("رصدنا فاتورة كهرباء جديدة بقيمة ٣٥٠ ريال بالبريد الالكتروني.", color = SleekTextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekAlertBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = SleekAlertText)
                            Column {
                                Text("تنبيه جيو-سياق نشط", fontWeight = FontWeight.Bold, color = SleekAlertText, fontSize = 12.sp)
                                Text("موقعك الحالي يحاكي: $currentLocation للتنبيه الاستباقي قبل الصرف.", color = SleekTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        )
    }
}

// ==================== UI SECTIONS & COMPONENTS ====================

@Composable
fun HeaderSection(
    userPoints: Int,
    hasUnresolvedNotification: Boolean,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User Profile Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(SleekPrimary)
                    .clickable { /* Profile interaction */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "سارة",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "أهلاً، سارة 👋",
                    fontSize = 12.sp,
                    color = SleekTextMuted,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "المالية الذكية",
                        fontSize = 16.sp,
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(SleekPrimaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$userPoints نقطة 🌟",
                            color = SleekPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Notification active bell button
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, SleekBackground, CircleShape)
                .testTag("notification_button")
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "التنبيهات",
                    tint = if (hasUnresolvedNotification) SleekPrimary else SleekTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                if (hasUnresolvedNotification) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SleekAlertText, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    emailBillDetected: Boolean
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .border(0.5.dp, SleekTextMuted.copy(alpha = 0.12f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "الرئيسية"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekPrimary,
                selectedTextColor = SleekPrimary,
                indicatorColor = SleekPrimaryContainer,
                unselectedIconColor = SleekTextMuted,
                unselectedTextColor = SleekTextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("التحليلات", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "التحليلات"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekPrimary,
                selectedTextColor = SleekPrimary,
                indicatorColor = SleekPrimaryContainer,
                unselectedIconColor = SleekTextMuted,
                unselectedTextColor = SleekTextMuted
            ),
            modifier = Modifier.testTag("nav_analytics")
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("الاستشارات", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "المحادثة"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekPrimary,
                selectedTextColor = SleekPrimary,
                indicatorColor = SleekPrimaryContainer,
                unselectedIconColor = SleekTextMuted,
                unselectedTextColor = SleekTextMuted
            ),
            modifier = Modifier.testTag("nav_chat")
        )

        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text("الأهداف", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "الأهداف"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekPrimary,
                selectedTextColor = SleekPrimary,
                indicatorColor = SleekPrimaryContainer,
                unselectedIconColor = SleekTextMuted,
                unselectedTextColor = SleekTextMuted
            ),
            modifier = Modifier.testTag("nav_goals")
        )

        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            label = { Text("القطات", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "القطات"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekPrimary,
                selectedTextColor = SleekPrimary,
                indicatorColor = SleekPrimaryContainer,
                unselectedIconColor = SleekTextMuted,
                unselectedTextColor = SleekTextMuted
            ),
            modifier = Modifier.testTag("nav_gatahs")
        )
    }
}

// ==================== SCREEN 1: HOME TAB ====================

@Composable
fun HomeTabContent(
    goals: List<GoalEntity>,
    transactions: List<TransactionEntity>,
    currentLocation: String,
    emailBillDetected: Boolean,
    financialLiteracy: Int,
    onApproveBill: () -> Unit,
    onIgnoreBill: () -> Unit,
    onSimulateLocation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Open Banking Consolidated Balance Card
        OpenBankingBalanceCard(transactions = transactions)

        // 2. Email Bill Integration Dialog Card (If Detected)
        if (emailBillDetected) {
            EmailBillIntegrationCard(
                onApprove = onApproveBill,
                onIgnore = onIgnoreBill
            )
        }

        // 3. Proactive Location-Based Alert
        LocationProactiveAlertCard(
            currentLocation = currentLocation,
            onSimulate = onSimulateLocation
        )

        // 4. Financial Health Progress & Graph (Vision 2030 Quality of Life)
        Vision2030FinancialHealthCard(financialLiteracy = financialLiteracy)
        
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun OpenBankingBalanceCard(transactions: List<TransactionEntity>) {
    // Standard consolidated mockup balance: 42,850 SAR
    val totalBalance = 42850.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(SleekPrimary, SleekSecondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي الرصيد الموحد",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "٤٢,٨٥٠ ريال",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "المصرفية المفتوحة",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                // Connected Accounts balances horizontal view
                Text(
                    text = "الحسابات المصرفية المتصلة تلقائياً:",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Al Rajhi Bank Pill
                    ConnectedBankPill(
                        bankName = "الراجحي",
                        amount = "٢٥,٠٠٠ ريال",
                        dotColor = Color(0xFF4CAF50)
                    )

                    // SNB Bank Pill
                    ConnectedBankPill(
                        bankName = "الأهلي SNB",
                        amount = "١٢,٣٥٠ ريال",
                        dotColor = Color(0xFF2196F3)
                    )

                    // STC Pay Pill
                    ConnectedBankPill(
                        bankName = "STC Pay",
                        amount = "٥,٥٠٠ ريال",
                        dotColor = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectedBankPill(bankName: String, amount: String, dotColor: Color) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Column {
                Text(text = bankName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = amount, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun EmailBillIntegrationCard(
    onApprove: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekPrimary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SleekAssistantContainer, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "البريد الإلكتروني الذكي",
                        tint = SleekSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "المساعد الذكي (أتمتة البريد)",
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "لقد رصدت فاتورة كهرباء جديدة في بريدك الإلكتروني لشركة الكهرباء بقيمة ٣٥٠ ريال. هل أدمجها وأجدولها في ميزانيتك لشهر يونيو؟",
                        color = SleekTextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.textButtonColors(contentColor = SleekTextMuted)
                ) {
                    Text("تجاهل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("approve_bill_button")
                ) {
                    Text("نعم، أضفها", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LocationProactiveAlertCard(
    currentLocation: String,
    onSimulate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekAlertBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SleekAlertBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SleekAlertText, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "الموقع والإنفاق",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "تنبيه استباقي (سياق الموقع)",
                        color = SleekAlertText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (currentLocation) {
                            "دانكن دونتس" -> "أنت بالقرب من \"دانكن\". استهلاكك للقهوة شارف على تجاوز ميزانية الترفيه المقترحة (تجاوز ٩٠٪)!"
                            "كارفور" -> "أنت بالقرب من \"كارفور\". ميزانية المقاضي الغذائية المتبقية لهذا الأسبوع هي ١٥٠ ريال فقط."
                            "معرض السيارات" -> "أنت في معرض سيارات. تذكر أن ميزانية شراء سيارتك المحددة مسبقاً هي ٨٠,٠٠٠ ريال لتجنب المديونية."
                            "مكتب العقار" -> "أنت في مكتب عقاري. هدفك الادخاري الحالي المخصص للمنزل هو ١٥٠,٠٠٠ ريال كدفعة أولى."
                            else -> "تتبع سياقي نشط: سأقوم بحمايتك قبل الصرف وتنبيهك تلقائياً عند الاقتراب من المتاجر المفضلة."
                        },
                        color = SleekAlertText.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Divider(color = SleekAlertBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Location simulation switchers
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "محاكاة تغيير الموقع الجغرافي للعميل لتجربة التنبيهات الاستباقية:",
                    color = SleekTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val places = listOf("دانكن دونتس", "كارفور", "معرض السيارات", "مكتب العقار")
                    places.forEach { place ->
                        val isSelected = currentLocation == place
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) SleekAlertText else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SleekAlertText else SleekAlertBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSimulate(place) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = place,
                                color = if (isSelected) Color.White else SleekTextDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Vision2030FinancialHealthCard(financialLiteracy: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جودة الحياة المالية",
                        color = SleekTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "مؤشر الوعي والادخار للرؤية",
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SleekPrimaryContainer, RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "رؤية ٢٠٣٠",
                        color = SleekPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Literacy percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "معدل وعيك المالي الحالي: $financialLiteracy%",
                    color = SleekPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "الهدف الوطني: ٨٠%",
                    color = SleekTextMuted,
                    fontSize = 10.sp
                )
            }

            // Custom Linear Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SleekBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(financialLiteracy / 100f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SleekPrimary, SleekSecondary)
                            )
                        )
                )
            }

            // Graphic Bars simulation
            Text(
                text = "الاستهلاك الأسبوعي الموزون:",
                color = SleekTextDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val heights = listOf(0.4f, 0.65f, 0.5f, 0.9f, 0.3f, 0.75f)
                val days = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")

                heights.zip(days).forEach { (h, day) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(0.4f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (h > 0.8f) SleekPrimary else SleekTextMuted.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .fillMaxHeight(h)
                                    .background(if (h > 0.8f) SleekPrimary else SleekSecondary.copy(alpha = 0.7f))
                            )
                        }
                        Text(text = day, color = SleekTextMuted, fontSize = 9.sp)
                    }
                }
            }

            Text(
                text = "أداؤك هذا الأسبوع ساهم في رفع وعيك المالي بنسبة ١٥٪ وتوفير الفائض للادخار والأتمتة الذكية.",
                color = SleekTextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==================== SCREEN 2: ANALYTICS TAB ====================

@Composable
fun AnalyticsTabContent(
    transactions: List<TransactionEntity>,
    literacyRate: Int
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(searchQuery, transactions) {
        if (searchQuery.trim().isEmpty()) {
            transactions
        } else {
            transactions.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.bankName.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title
        item {
            Column {
                Text(
                    text = "التحليلات المالية والتنبؤات الاستباقية",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                Text(
                    text = "قراءات تنبؤية لمستقبل استقرارك المالي وجودة حياتك",
                    fontSize = 11.sp,
                    color = SleekTextMuted
                )
            }
        }

        // 2. Predictive Analytics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekAssistantContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SleekPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "التحليل التنبؤي",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "تحليلات تنبؤية (محرك الميزانية المتقدمة)",
                            color = SleekPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "بناءً على سلوكك الاستهلاكي للشهر الحالي ورصد فواتير بريدك الإلكتروني بنجاح، يُتوقع بإذن الله توفير ٢,٤٠٠ ريال إضافية في ميزانية الشهر القادم إذا التزمت بتنبيهات سياق الموقع وتجنبت الصرف العشوائي في المطاعم.",
                            color = SleekTextDark,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. Simple Search & Header
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search"),
                placeholder = { Text("ابحث في معاملات المصرفية المفتوحة...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = SleekTextMuted) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekTextMuted.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        // Transactions List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل العمليات الموحد (المصرفية المفتوحة)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                Text(
                    text = "${filteredTransactions.size} معاملات",
                    fontSize = 11.sp,
                    color = SleekTextMuted
                )
            }
        }

        // Transactions list from room database
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد معاملات تطابق بحثك حالياً.",
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            items(filteredTransactions) { transaction ->
                TransactionListItem(transaction = transaction)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TransactionListItem(transaction: TransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (transaction.isExpense) SleekAlertBg else SleekPrimaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isExpense) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = "عملية مدمجة",
                        tint = if (transaction.isExpense) SleekAlertText else SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = transaction.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "حساب ${transaction.bankName}",
                            fontSize = 10.sp,
                            color = SleekTextMuted
                        )
                        Box(
                            modifier = Modifier
                                .background(SleekBackground, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = transaction.category, fontSize = 8.sp, color = SleekTextMuted)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.isExpense) "-" else "+"}${transaction.amount} ريال",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isExpense) SleekAlertText else Color(0xFF4CAF50)
                )
                Text(
                    text = transaction.date,
                    fontSize = 9.sp,
                    color = SleekTextMuted
                )
            }
        }
    }
}

// ==================== SCREEN 3: ADVISOR TAB (INTEGRATIVE FINANCIAL ADVISOR) ====================

@Composable
fun ChatTabContent(
    chatMessages: List<ChatMessageEntity>,
    isChatLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chat Header with Clear action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "المساعد المالي الافتراضي الذكي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                Text(
                    text = "اتصال مشفّر وآمن بالخوادم الآمنة لمعالجة فواتيرك وتقديم المشورة",
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }
            TextButton(
                onClick = onClearHistory,
                colors = ButtonDefaults.textButtonColors(contentColor = SleekAlertText)
            ) {
                Text("مسح السجل", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Message List view
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(0.5.dp, SleekTextMuted.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { message ->
                    val isUser = message.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .background(
                                    if (isUser) SleekPrimary else SleekBackground,
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isUser) "أنتِ (سارة)" else "المساعد الذكي 🤖",
                                    fontSize = 10.sp,
                                    color = if (isUser) Color.White.copy(alpha = 0.8f) else SleekPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = message.text,
                                    fontSize = 11.5.sp,
                                    color = if (isUser) Color.White else SleekTextDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(SleekBackground, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = SleekPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "المساعد يحلل فواتيرك ويفكر...",
                                        fontSize = 11.sp,
                                        color = SleekTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                placeholder = { Text("اسأل المستشار عن ميزانيتك وأهدافك...", fontSize = 11.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekTextMuted.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                maxLines = 2,
                singleLine = false
            )

            FloatingActionButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        onSendMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                containerColor = SleekPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "إرسال",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== SCREEN 4: GOALS & REWARDS TAB ====================

@Composable
fun GoalsAndRewardsTabContent(
    goals: List<GoalEntity>,
    badges: List<RewardBadgeEntity>,
    userPoints: Int,
    onAddGoal: (String, Double, String, String) -> Unit,
    onDeposit: (Int, Double) -> Unit,
    onDelete: (Int) -> Unit,
    onRedeem: (Int, Int) -> Unit
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showRedeemSuccessDialog by remember { mutableStateOf(false) }
    var redeemedCode by remember { mutableStateOf("") }
    var redeemedTitle by remember { mutableStateOf("") }

    // Add Goal fields state
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalDate by remember { mutableStateOf("2026-12-31") }
    var newGoalCategory by remember { mutableStateOf("منزل") }

    // Quick deposit fields state
    var showDepositDialog by remember { mutableStateOf(false) }
    var activeDepositGoalId by remember { mutableStateOf(-1) }
    var activeDepositGoalTitle by remember { mutableStateOf("") }
    var depositAmountInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Loyalty Rewards Point Dashboard Hero Card
        item {
            RewardsHeroPointsCard(userPoints = userPoints)
        }

        // 2. Financial Goals Title and Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تتبع أهدافك الادخارية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextDark
                    )
                    Text(
                        text = "ادخر بانتظام لتكسب شارات ونقاط ولاء مضاعفة",
                        fontSize = 10.sp,
                        color = SleekTextMuted
                    )
                }

                Button(
                    onClick = { showAddGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_goal_floating_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "أضف", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("هدف جديد", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Goal Items list
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد لديك أهداف نشطة حالياً. ابدأ بإضافة أول هدف مالي!", color = SleekTextMuted, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(goals) { goal ->
                GoalListItemCard(
                    goal = goal,
                    onDepositRequest = { id, title ->
                        activeDepositGoalId = id
                        activeDepositGoalTitle = title
                        showDepositDialog = true
                    },
                    onDeleteRequest = { id -> onDelete(id) }
                )
            }
        }

        // 4. Badges and achievements section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "شاراتك التقديرية الافتراضية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                Text(
                    text = "افتح الإنجازات بوعيك المالي وادخارك المنتظم",
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badges.forEach { badge ->
                    BadgeItemPill(badge = badge)
                }
            }
        }

        // 5. Redeem rewards list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "استبدال النقاط بمكافآت وخصومات",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextDark
                )
                Text(
                    text = "استبدل نقاط الولاء المالية بقسائم حقيقية لشركائنا",
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }
        }

        // Predefined list of redeemable vouchers
        val mockVouchers = listOf(
            VoucherRedeemable("noon_50", "قسيمة شراء من نون بقيمة ٥٠ ريال", 2500, "NOON-50-SARAH"),
            VoucherRedeemable("consult_free", "جلسة استشارة مالية مجانية ثانية", 1000, "CONSULT-FREE-2026"),
            VoucherRedeemable("malaa_15", "خصم ١٥٪ على الفئة الذهبية في منصة ملاءة", 1500, "MALAA-GOLD-15")
        )

        items(mockVouchers) { voucher ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = voucher.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekTextDark)
                        Text(text = "تكلفة النقاط: ${voucher.pointsRequired} نقطة 🌟", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    val canAfford = userPoints >= voucher.pointsRequired
                    Button(
                        onClick = {
                            onRedeem(0, voucher.pointsRequired) // triggers points deduction
                            redeemedCode = voucher.code
                            redeemedTitle = voucher.title
                            showRedeemSuccessDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) SleekPrimary else SleekBackground
                        ),
                        enabled = canAfford,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (canAfford) "استبدال" else "غير كافٍ",
                            fontSize = 11.sp,
                            color = if (canAfford) Color.White else SleekTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("أضف هدفاً ادخارياً جديداً", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAmount = newGoalTarget.toDoubleOrNull() ?: 1000.0
                        onAddGoal(newGoalTitle, parsedAmount, newGoalDate, newGoalCategory)
                        showAddGoalDialog = false
                        newGoalTitle = ""
                        newGoalTarget = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    enabled = newGoalTitle.trim().isNotEmpty() && newGoalTarget.trim().isNotEmpty()
                ) {
                    Text("إضافة الهدف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("إلغاء", color = SleekTextMuted)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("عنوان الهدف (مثال: شراء سيارة)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newGoalTarget,
                        onValueChange = { newGoalTarget = it },
                        label = { Text("المبلغ المستهدف (ريال)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newGoalDate,
                        onValueChange = { newGoalDate = it },
                        label = { Text("التاريخ المستهدف (مثال: 2026-12-31)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("فئة الهدف:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("منزل", "قرض", "سفر", "سيارة", "أخرى")
                        categories.forEach { cat ->
                            val isSelected = newGoalCategory == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) SleekPrimary else SleekBackground,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { newGoalCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else SleekTextDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    // Deposit dialog
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("ادخار في الهدف: $activeDepositGoalTitle", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAmt = depositAmountInput.toDoubleOrNull() ?: 0.0
                        if (parsedAmt > 0) {
                            onDeposit(activeDepositGoalId, parsedAmt)
                        }
                        showDepositDialog = false
                        depositAmountInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    enabled = depositAmountInput.trim().isNotEmpty()
                ) {
                    Text("تأكيد الدفع والادخار", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("إلغاء", color = SleekTextMuted)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("حدد المبلغ الذي ترغب في نقله من حسابك في المصرفية المفتوحة إلى هذا الهدف الادخاري المخصص:", fontSize = 11.sp, color = SleekTextMuted)
                    OutlinedTextField(
                        value = depositAmountInput,
                        onValueChange = { depositAmountInput = it },
                        label = { Text("المبلغ للادخار (ريال)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        )
    }

    // Redeem success dialog
    if (showRedeemSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRedeemSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تم الاستبدال بنجاح! 🎉", fontWeight = FontWeight.Bold, color = SleekPrimary, fontSize = 16.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showRedeemSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)) {
                    Text("تمت العملية", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لقد قمت باستبدال نقاطك بنجاح مقابل:",
                        fontSize = 12.sp,
                        color = SleekTextDark
                    )
                    Text(
                        text = redeemedTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekSecondary,
                        textAlign = TextAlign.Center
                    )
                    Divider(color = SleekBackground, thickness = 1.dp)
                    Text(text = "كود الخصم المالي الخاص بك هو:", fontSize = 11.sp, color = SleekTextMuted)
                    Box(
                        modifier = Modifier
                            .background(SleekBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, SleekPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = redeemedCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = "*تم نسخ الكود تلقائياً. استمتع برحلتك لتحسين جودة الحياة المالية تماشياً مع الرؤية!",
                        fontSize = 9.sp,
                        color = SleekTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}

data class VoucherRedeemable(
    val id: String,
    val title: String,
    val pointsRequired: Int,
    val code: String
)

@Composable
fun RewardsHeroPointsCard(userPoints: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(SleekSecondary, SleekPrimary)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "رصيد نقاط الولاء التراكمي",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$userPoints نقطة ولاء 🌟",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "كل عملية ادخار مدمجة تزيد نقاطك لمستقبل مالي آمن!",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 9.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GoalListItemCard(
    goal: GoalEntity,
    onDepositRequest: (Int, String) -> Unit,
    onDeleteRequest: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, SleekTextMuted.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SleekPrimaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (goal.category) {
                                "منزل" -> Icons.Default.Home
                                "قرض" -> Icons.Default.Warning
                                "سفر" -> Icons.Default.Star
                                else -> Icons.Default.Info
                            },
                            contentDescription = goal.category,
                            tint = SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = SleekTextDark
                        )
                        Text(
                            text = "تاريخ الاستحقاق: ${goal.targetDate}",
                            fontSize = 9.sp,
                            color = SleekTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = { onDeleteRequest(goal.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الهدف",
                        tint = SleekAlertText.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Amounts
            val progressPercent = (goal.currentAmount / goal.targetAmount * 100).toInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "المدخر حالياً: ${goal.currentAmount.toInt()} ريال / المستهدف: ${goal.targetAmount.toInt()} ريال",
                    fontSize = 11.sp,
                    color = SleekTextDark
                )
                Text(
                    text = "$progressPercent%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SleekPrimary
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SleekBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (goal.currentAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0.01f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SleekPrimary, SleekSecondary)
                            )
                        )
                )
            }

            // Smart Advice box
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekBackground),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "نصيحة",
                        tint = SleekPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = goal.tips,
                        fontSize = 9.5.sp,
                        color = SleekTextMuted,
                        lineHeight = 13.sp
                    )
                }
            }

            // Save deposit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onDepositRequest(goal.id, goal.title) },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("ادخر الآن للهدف", color = SleekPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BadgeItemPill(badge: RewardBadgeEntity) {
    val isUnlocked = badge.isUnlocked
    Box(
        modifier = Modifier
            .background(
                if (isUnlocked) SleekPrimaryContainer.copy(alpha = 0.5f) else Color.White,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isUnlocked) SleekPrimary else SleekTextMuted.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .width(130.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isUnlocked) SleekPrimary else SleekBackground,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (badge.iconName) {
                        "savings" -> Icons.Default.Favorite
                        "shield" -> Icons.Default.Warning
                        "email" -> Icons.Default.Email
                        else -> Icons.Default.Star
                    },
                    contentDescription = badge.title,
                    tint = if (isUnlocked) Color.White else SleekTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = badge.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isUnlocked) SleekTextDark else SleekTextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isUnlocked) "تم تفعيلها 🔓 (+${badge.pointsValue}ن)" else "مغلقة 🔒 (${badge.pointsValue}ن)",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isUnlocked) SleekPrimary else SleekTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== DESKTOP LAYOUT COMPONENTS ====================

@Composable
fun DesktopSidebar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userPoints: Int,
    hasUnresolvedNotification: Boolean,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // App Branding Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SleekPrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Logo", tint = Color.White)
                }
                Column {
                    Text("المالية الذكية 💻", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SleekTextDark)
                    Text("نسخة جهاز الكمبيوتر واللابتوب", fontSize = 9.5.sp, color = SleekTextMuted)
                }
            }

            // Navigation Links
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val menuItems = listOf(
                    Triple(0, "الرئيسية الموحدة", Icons.Default.Home),
                    Triple(1, "تحليلات الصرف", Icons.Default.Info),
                    Triple(2, "الاستشارات المباشرة", Icons.Default.Star),
                    Triple(3, "الأهداف والمكافآت", Icons.Default.Favorite),
                    Triple(4, "القطات الجماعية 👥", Icons.Default.Share)
                )

                menuItems.forEach { (tabId, label, icon) ->
                    val isSelected = selectedTab == tabId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) SleekPrimaryContainer else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onTabSelected(tabId) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) SleekPrimary else SleekTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) SleekPrimary else SleekTextDark
                        )
                    }
                }
            }
        }

        // Profile and Loyalty status card inside Sidebar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SleekBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SleekPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "سارة", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("سارة الحربي", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekTextDark)
                        Text("عضو بلاتيني 🏆", fontSize = 9.sp, color = SleekPrimary)
                    }
                }

                HorizontalDivider(color = SleekTextMuted.copy(alpha = 0.08f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("رصيد نقاطك:", fontSize = 10.sp, color = SleekTextMuted)
                    Text("$userPoints ن 🌟", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekPrimary)
                }

                // Notification Bell Trigger
                Button(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasUnresolvedNotification) SleekSecondary else SleekTextMuted.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notif",
                        tint = if (hasUnresolvedNotification) Color.White else SleekTextDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (hasUnresolvedNotification) "تنبيه بريد نشط 🚨" else "لا يوجد تنبيهات جديدة",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUnresolvedNotification) Color.White else SleekTextDark
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderDesktop(
    selectedTab: Int,
    userPoints: Int,
    onNotificationClick: () -> Unit,
    hasUnresolvedNotification: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = when (selectedTab) {
                    0 -> "الرئيسية الموحدة لجميع حساباتك المصرفية 🏦"
                    1 -> "تحليلات الصرف الذكية ومراقبة الهدر المالي 📊"
                    2 -> "مساعدك الاستشاري المالي المتكامل لخدمتك المباشرة 💼"
                    3 -> "أهدافك الادخارية والمكافآت المرتبطة برؤية ٢٠٣٠ 🎯"
                    4 -> "نظام القطات والمشاركات الجماعية لتقسيم الفواتير بالتساوي 👥"
                    else -> "لوحة تحكم المالية الذكية"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SleekTextDark
            )
            Text(
                text = "المزامنة المباشرة مع البنك المركزي السعودي (SAMA Sandbox) مفعلة ونشطة.",
                fontSize = 11.sp,
                color = SleekTextMuted
            )
        }

        // Notification badge icon
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notif",
                    tint = if (hasUnresolvedNotification) SleekPrimary else SleekTextMuted
                )
                if (hasUnresolvedNotification) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SleekAlertText, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

// ==================== GATAH (SPLIT EXPENSE) COMPONENTS ====================

@Composable
fun GatahTabContent(
    gatahs: List<GatahEntity>,
    userPoints: Int,
    onAddGatah: (String, Double, Int, String) -> Unit,
    onRecordPayment: (Int) -> Unit,
    onDeleteGatah: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "ترفيه", "هدايا", "سفر", "مقاضي")
    val filteredGatahs = if (selectedCategoryFilter == "الكل") gatahs else gatahs.filter { it.category == selectedCategoryFilter }

    val totalCollected = gatahs.sumOf { it.collectedAmount }
    val totalGoalAmount = gatahs.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(SleekPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Gatah", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نظام القطات الجماعية الذكي 👥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SleekTextDark
                        )
                        Text(
                            text = "قسم المصاريف والقطات بالتساوي مع أصدقائك وعائلتك تلقائياً، واكسب نقاط ماليّة عند اكتمال الجمع وممارسة السلوكيات المالية التعاونية الذكية.",
                            fontSize = 11.sp,
                            color = SleekTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Stats summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total collected
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekTextMuted.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("إجمالي المبالغ المجمّعة", fontSize = 11.sp, color = SleekTextMuted)
                        Text("${String.format("%.0f", totalCollected)} ريال", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                    }
                }

                // Total goals
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekTextMuted.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("الهدف الإجمالي للقطات", fontSize = 11.sp, color = SleekTextMuted)
                        Text("${String.format("%.0f", totalGoalAmount)} ريال", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SleekSecondary)
                    }
                }
            }
        }

        // Category Filter & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filters
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) SleekPrimary else Color.White,
                                    RoundedCornerShape(50.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SleekPrimary else SleekTextMuted.copy(alpha = 0.2f),
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else SleekTextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Create Button
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("قطة جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // List of Gatahs (Dynamic Responsive Grid)
        if (filteredGatahs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Empty", tint = SleekTextMuted.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Text("لا يوجد أي قطات نشطة في هذا القسم حالياً.", color = SleekTextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            val chunked = filteredGatahs.chunked(2)
            chunked.forEach { rowGatahs ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowGatahs.forEach { gatah ->
                            Box(modifier = Modifier.weight(1f)) {
                                GatahCardItem(
                                    gatah = gatah,
                                    onRecordPayment = onRecordPayment,
                                    onDeleteGatah = onDeleteGatah
                                )
                            }
                        }
                        if (rowGatahs.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Create New Gatah Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var totalAmountStr by remember { mutableStateOf("") }
        var membersCountStr by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("ترفيه") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                        val members = membersCountStr.toIntOrNull() ?: 2
                        if (title.isNotEmpty() && total > 0.0) {
                            onAddGatah(title, total, members, selectedCategory)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("إنشاء القطة 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SleekTextMuted)
                }
            },
            title = {
                Text("إنشاء قطة ومشاركة جديدة 👤👥", fontWeight = FontWeight.Bold, color = SleekTextDark, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("أدخل تفاصيل الشراء لتقسيم التكاليف والحصول على نقاط الولاء الذكية بالتساوي مع أعضاء المجموعة.", fontSize = 11.sp, color = SleekTextMuted)

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("اسم القطة (مثلاً: عشاء الويكند، هدية تخرج خالد)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekTextMuted.copy(alpha = 0.3f)
                        )
                    )

                    // Total Amount
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = { totalAmountStr = it },
                        label = { Text("المبلغ الإجمالي (ريال سعودي)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekTextMuted.copy(alpha = 0.3f)
                        )
                    )

                    // Members Count
                    OutlinedTextField(
                        value = membersCountStr,
                        onValueChange = { membersCountStr = it },
                        label = { Text("عدد الأعضاء الإجمالي (يشملك أنت أيضاً)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekTextMuted.copy(alpha = 0.3f)
                        )
                    )

                    // Category Selector
                    Text("تصنيف القطة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekTextDark)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("ترفيه", "هدايا", "سفر", "مقاضي").forEach { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) SleekPrimary else SleekBackground, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.White else SleekTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun GatahCardItem(
    gatah: GatahEntity,
    onRecordPayment: (Int) -> Unit,
    onDeleteGatah: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SleekTextMuted.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                when (gatah.category) {
                                    "ترفيه" -> SleekPrimaryContainer
                                    "هدايا" -> SleekAssistantContainer
                                    "سفر" -> SleekPrimaryContainer.copy(alpha = 0.6f)
                                    else -> SleekBackground
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (gatah.category) {
                                "ترفيه" -> Icons.Default.Favorite
                                "هدايا" -> Icons.Default.Star
                                "سفر" -> Icons.Default.LocationOn
                                else -> Icons.Default.ShoppingCart
                            },
                            contentDescription = gatah.category,
                            tint = SleekPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(gatah.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextDark)
                        Text(gatah.date, fontSize = 9.sp, color = SleekTextMuted)
                    }
                }

                // Delete Button
                IconButton(
                    onClick = { onDeleteGatah(gatah.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف القطة", tint = SleekTextMuted.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }

            // Financial details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المبلغ الإجمالي للقطة", fontSize = 10.sp, color = SleekTextMuted)
                    Text("${String.format("%.0f", gatah.totalAmount)} ريال", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekTextDark)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("نصيب الفرد بالتساوي", fontSize = 10.sp, color = SleekTextMuted)
                    Text("${String.format("%.0f", gatah.amountPerPerson)} ريال", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                }
            }

            // Progress bar
            val progress = if (gatah.totalAmount > 0) (gatah.collectedAmount / gatah.totalAmount).toFloat() else 0f
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("المبالغ المجمّعة المودعة:", fontSize = 9.sp, color = SleekTextMuted)
                    Text(
                        "${String.format("%.0f", gatah.collectedAmount)} من ${String.format("%.0f", gatah.totalAmount)} ريال",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SleekPrimary,
                    trackColor = SleekBackground
                )
            }

            // Paid members details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Face, contentDescription = "Members", tint = SleekTextMuted, modifier = Modifier.size(14.dp))
                    Text(
                        "الدفع: ${gatah.paidMembersCount} من أصل ${gatah.membersCount} أعضاء",
                        fontSize = 11.sp,
                        color = SleekTextMuted
                    )
                }

                if (gatah.isCompleted) {
                    Box(
                        modifier = Modifier
                            .background(SleekPrimaryContainer, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("مكتملة بالكامل ✅", color = SleekPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onRecordPayment(gatah.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("سجل دفع عضو 💳", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
