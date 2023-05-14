package com.towsal.towsal.respository


import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.ExceptionHandle
import com.towsal.towsal.views.activities.EmailVerificationActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

/**
 * Project main class for handling business logic
 * */
class DataRepository : BaseActivity() {
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var mainResponseObservable: Observable<BaseResponse>
    private lateinit var mainResponseLifeData: MutableLiveData<BaseResponse>
    private var response: BaseResponse = BaseResponse()

    /**
     * Function for calling api
     * */
    fun callApi(
        responseObservable: Observable<BaseResponse>,
        tag: Int, callback: OnNetworkResponse, showLoader: Boolean
    ) {
        if (showLoader) {
            val intent = Intent(Constants.BROCAST_RECIEVER)
            intent.putExtra(Constants.TYPE, Constants.SHOW_LOADER)
            MainApplication.applicationContext().sendBroadcast(intent)
        }
        mainResponseLifeData = MutableLiveData()

        mainResponseObservable = responseObservable
        compositeDisposable.add(
            mainResponseObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn { throwable ->
                    val intent = Intent(Constants.BROCAST_RECIEVER)
                    intent.putExtra(Constants.TYPE, Constants.HIDE_LOADER)
                    MainApplication.applicationContext().sendBroadcast(intent)
                    Log.e("Data Repository Error=", ExceptionHandle.handleException(throwable))
                    //                        callBack.onFailure(ExceptionHandle.handleException(throwable));
                    null
                }
                .subscribeWith(object : DisposableObserver<BaseResponse>() {
                    override fun onComplete() {
                        val intent = Intent(Constants.BROCAST_RECIEVER)
                        intent.putExtra(Constants.TYPE, Constants.HIDE_LOADER)
                        if (showLoader) {
                            MainApplication.applicationContext().sendBroadcast(intent)
                        }
                        when (response.code) {
                            Constants.ResponseCodes.SUCCESS -> {
                                callback.onSuccess(response, tag)
                            }
                            Constants.ResponseCodes.UNAUTHORIZED -> {
                                intent.putExtra(
                                    Constants.TYPE,
                                    Constants.ResponseCodes.UNAUTHORIZED
                                )
                                MainApplication.applicationContext().sendBroadcast(intent)
                                preferenceHelper.clearAllPreferences()
                            }
                            Constants.ResponseCodes.ALREADY_VERIFIED -> {
                                EmailVerificationActivity.isAlreadyVerified = true
                                callback.onSuccess(response, tag)
                            }
                            Constants.ResponseCodes.NOT_FOUND -> {
                                callback.onFailure(response, tag)
                            }
                            else -> {
                                if (response.code == 0) {
                                    response.message = "Something Went Wrong Try Again"
                                }
                                callback.onFailure(response, tag)
                            }
                        }
                        mainResponseLifeData.value = response
                    }

                    override fun onNext(t: BaseResponse) {
                        response = t
                    }

                    override fun onError(e: Throwable) {
                        response.message = ExceptionHandle.handleException(e.cause)
                        if (e.cause is HttpException) {
                            val body = (e.cause as HttpException).response()!!.errorBody()
                            try {
                                response = Gson().fromJson(
                                    body?.string(),
                                    BaseResponse::class.java
                                )
                            } catch (e: Exception) {
                                response = BaseResponse()
                                response.message = e.cause?.message.toString()
                                e.printStackTrace()
                            }

                            if (response.message.isEmpty()) {
                                response.message = e.cause?.localizedMessage.toString()
                            }
                        }
                        val intent = Intent(Constants.BROCAST_RECIEVER)
                        intent.putExtra(Constants.TYPE, Constants.HIDE_LOADER)
                        MainApplication.applicationContext().sendBroadcast(intent)
                        if (!response.message.contains("Unable to resolve host"))
                            callback.onFailure(response, tag)
                        else {
                            response.message = "Please check your internet"
                            callback.onFailure(response, tag)
                        }
                    }
                })
        )
    }


}