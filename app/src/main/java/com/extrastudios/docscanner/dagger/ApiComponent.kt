package com.extrastudios.docscanner.dagger

import com.extrastudios.docscanner.activity.BaseActivity
import com.extrastudios.docscanner.fragment.*
import com.extrastudios.docscanner.viewmodel.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (DbModule::class)])
interface ApiComponent {
    fun inject(activity: BaseActivity)
    fun inject(fragments: BaseFragments)
    fun inject(fragments: QRBarFragment)
    fun inject(fragments: ImageToPdfFragment)
    fun inject(fragments: SettingsFragment)
    fun inject(fragments: TextToPdfFragment)
    fun inject(fragments: AddTextFragment)
    fun inject(viewModel: HomeViewModel)
    fun inject(viewModel: SettingsViewModel)
    fun inject(viewModel: QrBarCodeViewModel)
    fun inject(viewModel: ImageToPdfViewModel)
    fun inject(viewModel: ExcelToPDFViewModel)
    fun inject(viewModel: TextToPdfViewModel)
    fun inject(viewModel: HistoryViewModel)
    fun inject(viewModel: ViewFilesViewModel)
    fun inject(viewModel: AddRemovePasswordViewModel)
    fun inject(viewModel: AddTextViewModel)
    fun inject(viewModel: ExcelToPdfFragment)
    fun inject(viewModel: MergeFilesFragment)
    fun inject(viewModel: PdfToImageFragment)
}
