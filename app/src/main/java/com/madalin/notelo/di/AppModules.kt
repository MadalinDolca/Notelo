package com.madalin.notelo.di

import com.madalin.notelo.MainViewModel
import com.madalin.notelo.feature.auth.passwordreset.PasswordResetViewModel
import com.madalin.notelo.feature.auth.signin.SignInViewModel
import com.madalin.notelo.feature.auth.signup.SignUpViewModel
import com.madalin.notelo.feature.notesandcategories.categories.CategoriesViewModel
import com.madalin.notelo.feature.notesandcategories.categoryviewer.CategoryViewerViewModel
import com.madalin.notelo.feature.notesandcategories.notes.NotesViewModel
import com.madalin.notelo.feature.notesandcategories.noteviewer.NoteViewerViewModel
import com.madalin.notelo.repository.FirebaseAuthRepository
import com.madalin.notelo.repository.FirebaseAuthRepositoryImpl
import com.madalin.notelo.repository.FirebaseContentRepository
import com.madalin.notelo.repository.FirebaseContentRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module to define application components to be injected.
 */
val appModule = module {
    //factory<CoroutineScope> { GlobalScope }
    //factory<CoroutineDispatcher> { Dispatchers.Default }

    //single { FirebaseRepositoryImpl() } // singleton component of FirebaseRepositoryImpl
    single<FirebaseAuthRepository> { FirebaseAuthRepositoryImpl() } // if FirebaseAuthRepository is used as a parameter
    single<FirebaseContentRepository> { FirebaseContentRepositoryImpl() }
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