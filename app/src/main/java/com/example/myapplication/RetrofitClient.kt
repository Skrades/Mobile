package com.example.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface CbrApiService {
    @GET("scripts/XML_daily.asp")
    suspend fun getDailyRatesXml(): String
}

object RetrofitClient {
    private const val BASE_URL = "https://www.cbr.ru/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val cbrApiService: CbrApiService by lazy {
        retrofit.create(CbrApiService::class.java)
    }

    suspend fun getGoldRate(): Double {
        return try {
            withContext(Dispatchers.IO) {
                val xmlString = cbrApiService.getDailyRatesXml()
                parseGoldRateFromXml(xmlString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            5000.0
        }
    }

    private fun parseGoldRateFromXml(xmlString: String): Double {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(xmlString.reader())

            var eventType = parser.eventType
            var inValute = false
            var foundGold = false
            var valueStr = ""
            var nominal = 1

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Valute" -> inValute = true
                            "CharCode" -> if (inValute) {
                                val charCode = parser.nextText()
                                println(charCode)
                                if (charCode == "USD") {
                                    foundGold = true
                                }
                            }
                            "Value" -> if (inValute && foundGold) {
                                valueStr = parser.nextText()
                            }
                            "Nominal" -> if (inValute && foundGold) {
                                nominal = parser.nextText().toIntOrNull() ?: 1
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Valute") {
                            inValute = false
                            if (foundGold) break
                        }
                    }
                }
                eventType = parser.next()
            }

            valueStr.replace(",", ".").toDouble() / nominal
        } catch (e: Exception) {
            e.printStackTrace()
            5000.0
        }
    }
}