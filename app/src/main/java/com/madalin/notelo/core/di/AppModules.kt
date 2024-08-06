package com.madalin.notelo.core.di

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.article_viewer.presentation.ArticleViewerViewModel
import com.madalin.notelo.auth.data.FirebaseAuthRepositoryImpl
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.presentation.passwordreset.PasswordResetViewModel
import com.madalin.notelo.auth.presentation.signin.SignInViewModel
import com.madalin.notelo.auth.presentation.signup.SignUpViewModel
import com.madalin.notelo.category_viewer.presentation.CategoryViewerViewModel
import com.madalin.notelo.content.presentation.categories_list.CategoriesViewModel
import com.madalin.notelo.content.presentation.notes_list.NotesViewModel
import com.madalin.notelo.core.data.local.database.ContentDatabase
import com.madalin.notelo.core.data.local.repository.LocalContentRepositoryImpl
import com.madalin.notelo.core.data.remote.repository.FirebaseContentRepositoryImpl
import com.madalin.notelo.core.data.remote.repository.FirebaseUserRepositoryImpl
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseUserRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.MainViewModel
import com.madalin.notelo.discover.data.network.NewsApiService
import com.madalin.notelo.discover.data.repository.NewsRepositoryImpl
import com.madalin.notelo.discover.domain.repository.NewsRepository
import com.madalin.notelo.discover.presentation.DiscoverViewModel
import com.madalin.notelo.home.presentation.HomeViewModel
import com.madalin.notelo.note_viewer.presentation.NoteViewerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Koin modules to define application components to be injected.
 */
val appModule = module {
    single { GlobalDriver(get()) }
    single { Firebase.auth }
    single { Firebase.firestore }

    //factory<CoroutineScope> { GlobalScope }
    //factory<CoroutineDispatcher> { Dispatchers.Default }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), ContentDatabase::class.java, "content_db")
            //.addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<ContentDatabase>().noteDao() }
    single { get<ContentDatabase>().categoryDao() }
    single { get<ContentDatabase>().tagDao() }
    single { get<ContentDatabase>().noteTagDao() }
}

val networkModule = module {
    // Retrofit builder instance
    single {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // implementation of the API endpoints defined by the service interface
    single { get<Retrofit>().create(NewsApiService::class.java) }
}

val repositoryModule = module {
    // local repositories
    single<LocalContentRepository> { LocalContentRepositoryImpl(get(), get(), get(), get()) }

    // firebase repositories
    single<FirebaseAuthRepository> { FirebaseAuthRepositoryImpl(get(), get()) } // if FirebaseAuthRepository is used as a parameter
    single<FirebaseUserRepository> { FirebaseUserRepositoryImpl(get(), get()) }
    single<FirebaseContentRepository> { FirebaseContentRepositoryImpl(get()) }

    // other remote repositories
    single<NewsRepository> { NewsRepositoryImpl(get(), "a5e7ded12876444db2b12dccad073c6d") }
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SignInViewModel(get(), get()) } // injects LoginViewModel with the above dependencies
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { PasswordResetViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { NotesViewModel(get(), get()) }
    viewModel { CategoriesViewModel(get(), get()) }
    viewModel { (handle: SavedStateHandle) -> NoteViewerViewModel(handle, get(), get(), get()) }
    viewModel { (handle: SavedStateHandle) -> CategoryViewerViewModel(handle, get(), get()) }
    viewModel { DiscoverViewModel(get(), get(), get()) }
    viewModel { ArticleViewerViewModel(get(), get(), get(), get()) }
}