package com.madalin.notelo.core.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.auth.data.FirebaseAuthRepositoryImpl
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.presentation.passwordreset.PasswordResetViewModel
import com.madalin.notelo.auth.presentation.signin.SignInViewModel
import com.madalin.notelo.auth.presentation.signup.SignUpViewModel
import com.madalin.notelo.categories_list.presentation.CategoriesViewModel
import com.madalin.notelo.category_viewer.presentation.CategoryViewerViewModel
import com.madalin.notelo.core.data.repository.FirebaseContentRepositoryImpl
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.presentation.MainViewModel
import com.madalin.notelo.note_viewer.presentation.NoteViewerViewModel
import com.madalin.notelo.notes_list.presentation.NotesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin modules to define application components to be injected.
 */
val appModule = module {
    // firebase instances
    single { Firebase.auth }
    single { Firebase.firestore }

    // firebase repositories
    single<FirebaseAuthRepository> { FirebaseAuthRepositoryImpl(get(), get()) } // if FirebaseAuthRepository is used as a parameter
    single<FirebaseContentRepository> { FirebaseContentRepositoryImpl() }

    // other repositories

    // other instances
    //factory<CoroutineScope> { GlobalScope }
    //factory<CoroutineDispatcher> { Dispatchers.Default }
    //single { FirebaseRepositoryImpl() } // singleton component of FirebaseRepositoryImpl
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SignInViewModel(get()) } // injects LoginViewModel with the above dependencies
    viewModel { SignUpViewModel(get()) }
    viewModel { PasswordResetViewModel(get()) }
    viewModel { NotesViewModel(get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { NoteViewerViewModel(get()) }
    viewModel { CategoryViewerViewModel(get()) }
}