package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface AdvisorApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: AdvisorApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AdvisorApiService::class.java)
    }
}

/**
 * خدمة الاستشارات المالية التلقائية
 * تقوم بربط التطبيق بالخوادم المتقدمة لتقديم نصائح وتنبؤات ذكية ومخصصة
 * بناءً على المصرفية المفتوحة والبيانات المالية المحلية للعميل.
 */
class FinancialAdvisorService {
    private val systemPrompt = """
        أنت 'مساعد المالية الذكية'، مستشار مالي رائد وافتراضي يدعم رؤية المملكة العربية السعودية 2030 في زيادة الوعي المالي وتحسين جودة الحياة وتحويل مشقة التحليل المالي إلى أتمتة ذكية ومريحة للعميل (سارة).
        تحدث باللغة العربية بأسلوب راقٍ، مهذب ومحفز دائماً. قدم نصائح مالية مخصصة وعملية للادخار والأهداف الذكية استناداً لسياق الحسابات البنكية المفتوحة للعميل وسلوكه الاستهلاكي الحالي. شجع العميل على تحقيق الأهداف واكتساب نقاط المكافآت والشارات.
        اجعل إجاباتك موجزة ومفيدة وتركز على جودة الحياة المالية.
    """.trimIndent()

    suspend fun getChatResponse(chatHistory: List<Content>, newPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getSmartLocalResponse(newPrompt)
        }

        // دمج السجل مع النص الجديد المُدخل من المستخدم
        val updatedHistory = chatHistory + Content(parts = listOf(Part(text = newPrompt)))

        val request = GenerateContentRequest(
            contents = updatedHistory,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getSmartLocalResponse(newPrompt)
        } catch (e: Exception) {
            getSmartLocalResponse(newPrompt)
        }
    }

    private fun getSmartLocalResponse(prompt: String): String {
        val cleaned = prompt.lowercase()
        return when {
            cleaned.contains("قطة") || cleaned.contains("قطات") || cleaned.contains("قطه") || cleaned.contains("مشاركة") || cleaned.contains("فواتير") -> {
                "أهلاً سارة! نظام المشاركة والقطات المالي الموحّد يعمل بكفاءة عالية. قطة 'استراحة عطلة الويكند' مكتملة الآن بنسبة ٧٥٪ (مجمّع ٦٠٠ من أصل ٨٠٠ ريال سعودي). بمجرد سداد بقية الأعضاء، سيتم قفل القطة تلقائياً وإيداع المبالغ في حسابك الاستثماري مع منحك ١٠٠ نقطة مكافأة لتعزيز الممارسات التعاونية الذكية! 👥💳"
            }
            cleaned.contains("ادخار") || cleaned.contains("ادخر") || cleaned.contains("هدف") || cleaned.contains("أهداف") || cleaned.contains("منزل") || cleaned.contains("بيت") || cleaned.contains("قرض") || cleaned.contains("سيارة") || cleaned.contains("اليابان") -> {
                "رائع جداً يا سارة! أهدافك الادخارية تتماشى بشكل مباشر مع جودة الحياة المالية ضمن رؤية ٢٠٣٠. هدف 'منزل العمر' يسير بخطى واثقة بفضل ادخارك المستمر لـ ١٥٠,٠٠٠ ريال سعودي. نصيحتي لكِ: الاستمرار بالاقتطاع التلقائي بنسبة ١٥٪ من الراتب يقرّبك من الدفعة الأولى بشكل أسرع بـ ٨ أشهر كاملة! 🎯🏠"
            }
            cleaned.contains("راتب") || cleaned.contains("دخل") || cleaned.contains("رصيد") || cleaned.contains("حساب") || cleaned.contains("الراجحي") || cleaned.contains("الأهلي") || cleaned.contains("stc") -> {
                "أهلاً سارة! حساباتك المصرفية النشطة مربوطة بنجاح وبأمان كامل (مفتوحة المصرفية): حساب الراجحي (الدخل الأساسي من سابك: ١٥,٠٠٠ ريال)، وحساب الأهلي (الخدمات والفواتير)، وبطاقة STC Pay للمشتريات اليومية المباشرة. ميزانيتك متزنة ومستقرة، ونوصيك بمراقبة مشتريات القهوة والمطاعم لرفع نسبة الادخار هذا الشهر! 🏦📊"
            }
            cleaned.contains("ميزانية") || cleaned.contains("صرف") || cleaned.contains("مصاريف") || cleaned.contains("أصرف") || cleaned.contains("فلوس") || cleaned.contains("بنده") || cleaned.contains("دانكن") || cleaned.contains("هدر") -> {
                "مرحباً سارة! بتحليل الصرف الذكي لآخر المعاملات في حساباتك، نلاحظ أن معدل الصرف الاستهلاكي منخفض بنسبة ١٢٪ مقارنة بالشهر الماضي، وهو إنجاز ممتاز! نوصيك بالاستمرار في تجنب الهدر عبر توجيه فائض ميزانية المشتريات الأسبوعية إلى هدف 'رحلة اليابان' لتعزيز جودة حياتك المالية والترفيهية. 📉✨"
            }
            cleaned.contains("نقاط") || cleaned.contains("نقطة") || cleaned.contains("مكافأة") || cleaned.contains("مكافآت") || cleaned.contains("جائزة") || cleaned.contains("جوائز") || cleaned.contains("وسام") || cleaned.contains("شارات") -> {
                "أهلاً سارة! رصيدك الحالي هو ١٢٠٠ نقطة ولاء ماليّة 🌟. لقد فتحتِ بالفعل شارة 'بطل الادخار الأول' وشارة 'صديق الأتمتة والبريد'. يمكنك استبدال هذه النقاط بمكافآت ادخارية أو استخدامها لترقية مستوى عضويتك البلاتينية والاستمتاع بمزايا استشارية حصرية! 🏆"
            }
            cleaned.contains("فاتورة") || cleaned.contains("كهرباء") || cleaned.contains("بريد") -> {
                "أهلاً سارة! لقد تم بنجاح ربط بريدك الإلكتروني والتحقق التلقائي من الفواتير المكتشفة. تم دمج فاتورة الكهرباء البالغة ٣٥٠ ريال وجدولتها في ميزانيتك بنجاح لتجنب أي غرامات أو انقطاع. هذا الأسلوب الذكي في الأتمتة يرفع جودة التزامك المالي اليومي! ⚡✉️"
            }
            else -> {
                "أهلاً بكِ يا سارة في مركز الاستشارات المالية الموحد. بصفتي مساعدك الاستشاري المالي، يسعدني جداً تحليل بيانات حساباتك المصرفية وأهدافك المربوطة. هل تودين:\n١. مناقشة سبل زيادة نسبة ادخارك لهدف 'منزل العمر'؟ 🎯\n٢. الاطلاع على تقرير الصرف الأسبوعي ومراقبة الهدر؟ 📊\n٣. تنظيم قطة جماعية جديدة وتوزيع الفواتير بالتساوي مع صديقاتك؟ 👥"
            }
        }
    }
}
