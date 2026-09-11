package com.geecee.escapelauncher.core.domain.search

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import jakarta.inject.Inject

/***
 * Use case to return all installed apps filtered with a query
 */
class SearchAppsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    operator fun invoke(queryFlow: Flow<String>, showHiddenFlow: Flow<Boolean>): Flow<List<InstalledApp>> {
        return combine(
            appsRepository.mainUserApps,
            modifiedAppsRepository.getHiddenPackageIdsFlow(),
            modifiedAppsRepository.getAllModifiedAppsFlow(),
            queryFlow,
            showHiddenFlow
        ) { allApps, hiddenIds, modifiedApps, rawQuery, showHidden ->
            val query = rawQuery.trim()
            val hiddenSet = hiddenIds.toSet()
            val modifiedAppsMap = modifiedApps.associateBy { it.packageId }

            val appsWithRenames = allApps.map { app ->
                val customName = modifiedAppsMap[app.packageName]?.displayName
                if (customName != null) {
                    app.copy(displayName = customName)
                } else {
                    app
                }
            }

            val filtered = if (query.isBlank()) {
                appsWithRenames.filter { !hiddenSet.contains(it.packageName) }.sortedBy { it.displayName.lowercase() }
            } else {
                appsWithRenames.filter { app ->
                    val isHidden = hiddenSet.contains(app.packageName)
                    val matchesQuery = fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHidden)
                }
            }

            if (query.isNotBlank()) {
                sortAppsByRelevance(filtered, query)
            } else {
                filtered
            }
        }
    }
}
