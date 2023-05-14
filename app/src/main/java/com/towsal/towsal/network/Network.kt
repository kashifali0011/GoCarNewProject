package com.towsal.towsal.network


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSource
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Api client class
 * */
class Network {
    private var networkClient: Retrofit? = null
    private var services: ApiInterface? = null

    private val errorInterceptor = object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            return try {
                chain.proceed(chain.request())
            } catch (e: IOException) {
                val intent = Intent(Constants.BROCAST_RECIEVER)
                intent.putExtra(Constants.TYPE, Constants.HIDE_LOADER)
                MainApplication.applicationContext().sendBroadcast(intent)
                Response.Builder()
                    .code(418)
                    .request(chain.request())
                    .body(object : ResponseBody() {
                        override fun contentLength() = 0L
                        override fun contentType(): MediaType? = null
                        override fun source(): BufferedSource = Buffer()
                    })
                    .message(e.message ?: e.toString())
                    .protocol(Protocol.HTTP_1_1)
                    .build()
            }
        }
    }

    init {
        val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this
            override fun responseBodyConverter(
                type: Type,
                annotations: Array<out Annotation>,
                retrofit: Retrofit
            ) = object : Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter =
                    retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)

                override fun convert(value: ResponseBody) =
                    if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
            }
        }

        //.baseUrl(BuildConfig.BASE_URL) was this in general
        //"http://gocar.sa/api/" for production
        val gson = GsonBuilder()
            .setLenient()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .create()
        networkClient = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.computation()))
            .client(initClient())
            .build()
        services = networkClient!!.create(ApiInterface::class.java)
    }


    /**
     * Function for initialization of ok http client
     * */
    private fun initClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor =
            HttpLoggingInterceptor { message -> //                    if (!Configuration.isProduction)
                longLog(message)
            }
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor { chain: Interceptor.Chain ->
            val userModel = PreferenceHelper()
                .getObject(
                    Constants.USER_MODEL,
                    UserModel::class.java
                ) as? UserModel
            if (userModel != null && userModel.access_token.isNotEmpty()) {
                val original = chain.request()
                val request = original.newBuilder()
                    .method(original.method, original.body)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer ${userModel.access_token}")
                    .build()
                return@addInterceptor chain.proceed(request)
            } else {
                val original = chain.request()
                val request = original.newBuilder()
                    .method(original.method, original.body)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()
                return@addInterceptor chain.proceed(request)
            }
        }

        builder.addInterceptor(httpLoggingInterceptor)
            .addInterceptor(
                RequestInterceptor(
                    MainApplication.applicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                )
            ).connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
        return builder.build()
    }

    /**
     * Function for returning instance of proxy interface
     * */
    fun apis(): ApiInterface? {
        return getApiInterface()
    }

    /**
     * Function for returning instance of proxy interface
     * */
    private fun getApiInterface(): ApiInterface? {
        return services
    }

    /**
     * Function for substring the value got in message to log
     * */
    private fun longLog(str: String) {
        if (str.length > 4000) {
            Log.e("NetWork Call", str.substring(0, 4000))
//            longLog(str.substring(4000))
        } else Log.e("NetWork Call", str)
    }
}