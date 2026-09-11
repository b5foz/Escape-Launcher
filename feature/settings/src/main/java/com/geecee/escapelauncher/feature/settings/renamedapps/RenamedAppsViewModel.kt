package com.geecee.escapelauncher.feature.settings.renamedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RenamedAppsViewModel @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository,
    appsRepository: AppsRepository
) : ViewModel() {

    val renamedApps: StateFlow<List<InstalledApp>> = combine(
        appsRepository.mainUserApps,
        modifiedAppsRepository.getAllModifiedAppsFlow()
    ) { allApps, modifiedApps ->
        val modifiedMap = modifiedApps.associateBy { it.packageId }
        allApps.mapNotNull { app ->
            val mod = modifiedMap[app.packageName]
            if (mod != null && mod.displayName != null) {
                app.copy(displayName = mod.displayName!!)
            } else {
                null
            }
        }.sortedBy { it.displayName.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateRename(packageName: String, newName: String) {
        viewModelScope.launch {
            modifiedAppsRepository.setDisplayName(packageName, newName.trim().ifBlank { null })
        }
    }
}
