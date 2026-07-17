package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.financeDao()
    private val advisorService = FinancialAdvisorService()

    // UI States
    val goals: StateFlow<List<GoalEntity>> = dao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gatahs: StateFlow<List<GatahEntity>> = dao.getAllGatahs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<RewardBadgeEntity>> = dao.getAllBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = dao.getChatHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = dao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Simulation & Live State
    private val _currentLocation = MutableStateFlow("دانكن دونتس")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _emailBillDetected = MutableStateFlow<Boolean>(true)
    val emailBillDetected: StateFlow<Boolean> = _emailBillDetected.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        // Seed default data if database is empty
        viewModelScope.launch {
            dao.getUserProfile().first()?.let {
                // Already seeded
            } ?: run {
                // Seed profile
                dao.insertUserProfile(UserProfileEntity(id = 1, points = 1200, financialLiteracy = 68))
                
                // Seed default Gatahs
                val defaultGatahs = listOf(
                    GatahEntity(
                        title = "قطة استراحة عطلة الويكند 🌴",
                        totalAmount = 800.0,
                        amountPerPerson = 100.0,
                        collectedAmount = 600.0,
                        membersCount = 8,
                        paidMembersCount = 6,
                        isCompleted = false,
                        date = "2026-06-30",
                        category = "ترفيه"
                    ),
                    GatahEntity(
                        title = "قطة هدية تخرج خالد 🎓",
                        totalAmount = 1500.0,
                        amountPerPerson = 300.0,
                        collectedAmount = 1500.0,
                        membersCount = 5,
                        paidMembersCount = 5,
                        isCompleted = true,
                        date = "2026-06-25",
                        category = "هدايا"
                    )
                )
                defaultGatahs.forEach { dao.insertGatah(it) }
                
                // Seed goals
                val defaultGoals = listOf(
                    GoalEntity(
                        title = "الادخار لشراء منزل العمر",
                        targetAmount = 500000.0,
                        currentAmount = 150000.0,
                        targetDate = "2029-12-31",
                        category = "منزل",
                        tips = "نصيحة ذكية: الاستمرار في اقتطاع ١٥٪ من الراتب يسرّع تحقيق هدف شراء المنزل بمقدار ٨ أشهر!"
                    ),
                    GoalEntity(
                        title = "سداد قرض السيارة المتبقي",
                        targetAmount = 80000.0,
                        currentAmount = 45000.0,
                        targetDate = "2027-06-30",
                        category = "قرض",
                        tips = "نصيحة ذكية: سداد دفعات إضافية بقيمة ٥٠٠ ريال شهرياً يوفّر عليك أرباحاً بقيمة ٣,٤٠٠ ريال."
                    ),
                    GoalEntity(
                        title = "رحلة عائلية لليابان",
                        targetAmount = 30000.0,
                        currentAmount = 18000.0,
                        targetDate = "2026-10-15",
                        category = "سفر",
                        tips = "نصيحة ذكية: دمج نقاط الولاء من مكافآت السفر يوفر لك ٢٠٪ من قيمة تذاكر الطيران."
                    )
                )
                defaultGoals.forEach { dao.insertGoal(it) }

                // Seed reward badges
                val defaultBadges = listOf(
                    RewardBadgeEntity(
                        title = "بطل الادخار الأول",
                        description = "تم تفعيلها لادخار أول ١٠,٠٠٠ ريال بنجاح.",
                        iconName = "savings",
                        pointsRequired = 0,
                        isUnlocked = true,
                        pointsValue = 200
                    ),
                    RewardBadgeEntity(
                        title = "حارس الميزانية الذكي",
                        description = "الاستجابة الفورية للتنبيهات الاستباقية لتجنب الهدر المالي.",
                        iconName = "shield",
                        pointsRequired = 100,
                        isUnlocked = false,
                        pointsValue = 300
                    ),
                    RewardBadgeEntity(
                        title = "صديق الأتمتة والبريد",
                        description = "ربط البريد الإلكتروني ودمج فاتورة الكهرباء تلقائياً للمرة الأولى.",
                        iconName = "email",
                        pointsRequired = 150,
                        isUnlocked = true,
                        pointsValue = 150
                    ),
                    RewardBadgeEntity(
                        title = "مستثمر الرؤية المستقبلية",
                        description = "إنشاء ٣ أهداف مالية ومتابعة خططها تماشياً مع رؤية ٢٠٣٠.",
                        iconName = "trending_up",
                        pointsRequired = 250,
                        isUnlocked = true,
                        pointsValue = 400
                    )
                )
                dao.insertBadges(defaultBadges)

                // Seed Transactions
                val defaultTransactions = listOf(
                    TransactionEntity(
                        bankName = "الراجحي",
                        amount = 15000.0,
                        description = "راتب شركة سابك لشهر يونيو",
                        date = "2026-06-25",
                        category = "دخل",
                        isExpense = false
                    ),
                    TransactionEntity(
                        bankName = "الأهلي",
                        amount = 350.0,
                        description = "الشركة السعودية للكهرباء - سداد فاتورة",
                        date = "2026-06-28",
                        category = "فواتير",
                        isExpense = true
                    ),
                    TransactionEntity(
                        bankName = "STC Pay",
                        amount = 25.0,
                        description = "قهوة دانكن دونتس المعتادة",
                        date = "2026-06-29",
                        category = "ترفيه",
                        isExpense = true
                    ),
                    TransactionEntity(
                        bankName = "الراجحي",
                        amount = 120.0,
                        description = "مشتريات سوبرماركت بنده",
                        date = "2026-06-29",
                        category = "مقاضي",
                        isExpense = true
                    )
                )
                defaultTransactions.forEach { dao.insertTransaction(it) }

                // Seed first assistant greeting message
                dao.insertChatMessage(
                    ChatMessageEntity(
                        sender = "assistant",
                        text = "أهلاً بك يا سارة في المالية الذكية المعتمدة على المصرفية المفتوحة والتحليلات التنبؤية. أنا مستشارك المالي الذكي، لقد قمت بربط حساباتك في (الراجحي، الأهلي، وSTC Pay) وقرأت فواتير بريدك الإلكتروني لدمج ميزانيتك تلقائياً. كيف يمكنني مساعدتك في أهدافك المالية اليوم؟"
                    )
                )
            }
        }
    }

    // Business functions
    fun addGoal(title: String, targetAmount: Double, targetDate: String, category: String) {
        viewModelScope.launch {
            val systemTips = when (category) {
                "منزل" -> "نصيحة ذكية: ادخار الدفعة الأولى بنسبة ٢٠٪ يقلل من تكلفة التمويل العقاري بشكل ملحوظ."
                "قرض" -> "نصيحة ذكية: استخدام استراتيجية كرة الثلج يساهم في التخلص من القروض والديون بشكل أسرع."
                "سفر" -> "نصيحة ذكية: الحجز المسبق لرحلتك قبل ٣ أشهر على الأقل يوفر ما يصل إلى ٣٥٪ من التكاليف."
                else -> "نصيحة ذكية: التوفير المنتظم بنسبة ١٠٪ من دخلك الشهري يسرع من تحقيق أهدافك المالية المتنوعة."
            }
            dao.insertGoal(
                GoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    targetDate = targetDate,
                    category = category,
                    tips = systemTips
                )
            )

            // Reward points for starting a new goal (+150 points)
            addPoints(150)
            
            // Check if we have more than 2 goals to unlock "مستثمر الرؤية المستقبلية" if locked
            val currentGoals = dao.getAllGoals().first()
            if (currentGoals.size >= 3) {
                unlockBadge("مستثمر الرؤية المستقبلية")
            }
            
            // Increment financial literacy by 2%
            incrementLiteracy(2)
        }
    }

    fun depositToGoal(goalId: Int, amount: Double) {
        viewModelScope.launch {
            val currentGoalsList = dao.getAllGoals().first()
            val goal = currentGoalsList.find { it.id == goalId } ?: return@launch
            val updatedAmount = goal.currentAmount + amount
            val finalAmount = if (updatedAmount > goal.targetAmount) goal.targetAmount else updatedAmount
            
            val updatedGoal = goal.copy(currentAmount = finalAmount)
            dao.updateGoal(updatedGoal)

            // Reward points for saving! (+100 points per 1000 SAR saved)
            val pointBonus = ((amount / 1000.0) * 100).toInt()
            if (pointBonus > 0) {
                addPoints(pointBonus)
            }

            // Check if goal is fully achieved!
            if (finalAmount >= goal.targetAmount) {
                addPoints(500) // Huge bonus points for achieving a goal!
                unlockBadge("بطل الادخار الأول")
                incrementLiteracy(5)
            }
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            dao.deleteGoal(goalId)
        }
    }

    fun approveEmailBill() {
        viewModelScope.launch {
            // Add utility payment transaction
            dao.insertTransaction(
                TransactionEntity(
                    bankName = "الأهلي",
                    amount = 350.0,
                    description = "الشركة السعودية للكهرباء - دمج فاتورة البريد",
                    date = "2026-06-30",
                    category = "فواتير",
                    isExpense = true
                )
            )
            // Hide the detected bill
            _emailBillDetected.value = false

            // Reward points for automation!
            addPoints(100)
            unlockBadge("صديق الأتمتة والبريد")
            incrementLiteracy(3)

            // Add Assistant comment
            dao.insertChatMessage(
                ChatMessageEntity(
                    sender = "assistant",
                    text = "ممتاز! تم دمج فاتورة الكهرباء المكتشفة بالبريد (٣٥٠ ريال) بنجاح وجدولتها في ميزانيتك عبر حسابك المفضل في البنك الأهلي. لقد حصلت على ١٠٠ نقطة ولاء!"
                )
            )
        }
    }

    fun ignoreEmailBill() {
        _emailBillDetected.value = false
    }

    fun simulateLocation(location: String) {
        _currentLocation.value = location
        // When location changes, add an alert simulation
        viewModelScope.launch {
            if (location == "معرض السيارات") {
                // auto unlock budget guardian if they simulated everything
                unlockBadge("حارس الميزانية الذكي")
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            // Save User message
            dao.insertChatMessage(ChatMessageEntity(sender = "user", text = text))

            _isChatLoading.value = true

            // جلب تاريخ المحادثة الحالي مباشرة من الحالة الذاكرية للتطبيق لتبسيط وسرعة الاستجابة
            val currentHistory = chatMessages.value
            val chatHistoryForApi = currentHistory.map { msg ->
                Content(parts = listOf(Part(text = "${if (msg.sender == "user") "سارة" else "المساعد"}: ${msg.text}")))
            }

            // إرسال الطلب للاستشارات المباشرة وتلقي استجابة الخادم الذكي للتحليل المالي المتقدم
            val aiResponse = advisorService.getChatResponse(chatHistoryForApi, text)

            // Save AI message
            dao.insertChatMessage(ChatMessageEntity(sender = "assistant", text = aiResponse))
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            dao.clearChatHistory()
            dao.insertChatMessage(
                ChatMessageEntity(
                    sender = "assistant",
                    text = "مرحباً سارة مجدداً! كيف تودين تنظيم ميزانيتك وأهدافك اليوم؟"
                )
            )
        }
    }

    fun redeemReward(badgeId: Int, pointsRequired: Int): Boolean {
        var success = false
        viewModelScope.launch {
            val profile = dao.getUserProfile().first() ?: return@launch
            if (profile.points >= pointsRequired) {
                dao.insertUserProfile(profile.copy(points = profile.points - pointsRequired))
                success = true
            }
        }
        return success
    }

    private suspend fun addPoints(value: Int) {
        val profile = dao.getUserProfile().first() ?: UserProfileEntity()
        dao.insertUserProfile(profile.copy(points = profile.points + value))
    }

    private suspend fun incrementLiteracy(percent: Int) {
        val profile = dao.getUserProfile().first() ?: UserProfileEntity()
        val current = profile.financialLiteracy + percent
        val finalVal = if (current > 100) 100 else current
        dao.insertUserProfile(profile.copy(financialLiteracy = finalVal))
    }

    private suspend fun unlockBadge(title: String) {
        val allBadges = dao.getAllBadges().first()
        val badge = allBadges.find { it.title == title }
        if (badge != null && !badge.isUnlocked) {
            val updatedBadge = badge.copy(isUnlocked = true)
            dao.updateBadge(updatedBadge)
            addPoints(badge.pointsValue)

            // Add automated chat alert for badge unlocked
            dao.insertChatMessage(
                ChatMessageEntity(
                    sender = "assistant",
                    text = "تهانينا الحارة يا سارة! 🎉 لقد فتحتِ شارة جديدة: '${badge.title}' لتميزك المالي، وتمت إضافة +${badge.pointsValue} نقطة ولاء إلى رصيدك لمستقبل مالي أكثر وعياً وفق جودة الحياة ورؤية ٢٠٣٠!"
                )
            )
        }
    }

    // Gatah management functions
    fun addGatah(title: String, totalAmount: Double, membersCount: Int, category: String) {
        viewModelScope.launch {
            val amountPerPerson = if (membersCount > 0) totalAmount / membersCount else totalAmount
            val newGatah = GatahEntity(
                title = title,
                totalAmount = totalAmount,
                amountPerPerson = amountPerPerson,
                collectedAmount = amountPerPerson, // Owner's share paid
                membersCount = membersCount,
                paidMembersCount = 1,
                isCompleted = membersCount <= 1,
                date = "2026-07-01",
                category = category
            )
            dao.insertGatah(newGatah)
            addPoints(50) // Reward points for creating a smart split
            incrementLiteracy(1)
        }
    }

    fun recordMemberPayment(gatahId: Int) {
        viewModelScope.launch {
            val currentGatahsList = dao.getAllGatahs().first()
            val gatah = currentGatahsList.find { it.id == gatahId } ?: return@launch
            if (gatah.isCompleted) return@launch

            val updatedPaidCount = gatah.paidMembersCount + 1
            val updatedCollected = gatah.collectedAmount + gatah.amountPerPerson
            val isNowCompleted = updatedPaidCount >= gatah.membersCount || updatedCollected >= gatah.totalAmount

            val updatedGatah = gatah.copy(
                paidMembersCount = updatedPaidCount,
                collectedAmount = if (updatedCollected > gatah.totalAmount) gatah.totalAmount else updatedCollected,
                isCompleted = isNowCompleted
            )
            dao.updateGatah(updatedGatah)

            // Reward loyalty points for updating splits
            addPoints(20)

            // If completed, huge bonus and notify via assistant chat!
            if (isNowCompleted) {
                addPoints(100)
                incrementLiteracy(2)
                dao.insertChatMessage(
                    ChatMessageEntity(
                        sender = "assistant",
                        text = "رائع! تم اكتمال جمع مبلغ القطة: '${gatah.title}' بالكامل بقيمة ${gatah.totalAmount} ريال من جميع الأعضاء المشاركين. تم قيدها في ميزانيتك، وحصلت على +100 نقطة ولاء! 🥳"
                    )
                )
            }
        }
    }

    fun deleteGatah(gatahId: Int) {
        viewModelScope.launch {
            dao.deleteGatah(gatahId)
        }
    }
}
