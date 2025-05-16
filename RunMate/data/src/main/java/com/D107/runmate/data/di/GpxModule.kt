package com.D107.runmate.data.di

//import com.D107.runmate.data.utils.CadenceTracker
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//object GpxModule {
//    // TODO 파일 di 기능
////    @Provides
////    @Singleton
////    fun provideFile(@ApplicationContext context: Context): BluetoothAdapter? {
////        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
////        return bluetoothManager?.adapter
////    }
////
////    @Provides
////    @Singleton
////    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
//
//}