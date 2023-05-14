package com.towsal.towsal.app

import com.towsal.towsal.network.Network
import com.towsal.towsal.respository.DataRepository
import com.towsal.towsal.viewmodel.*

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Module File  for dependency injection
 * */
val mainModule = module {

    single { DataRepository() }
    single { Network() }
    viewModel { LoginViewModel() }
    viewModel { RegistrationViewModel() }
    viewModel { OTPViewModel() }
    viewModel { LanguageSelectionViewModel() }
    viewModel { ForgotPasswordViewModel() }
    viewModel { CarListViewModel() }
    viewModel { MainScreenViewModel() }
    viewModel { CarDetailViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { UserInformationViewModel() }
    viewModel { CheckoutCarBookingViewModel() }
    viewModel { TripsViewModel() }
    viewModel { ProfileViewModel() }

    /*....change password viewmodel .....*/
    viewModel { ChangePasswordViewModel() }
    viewModel { PaymentsViewModel() }


}

