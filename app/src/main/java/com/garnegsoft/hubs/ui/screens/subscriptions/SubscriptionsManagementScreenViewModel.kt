package com.garnegsoft.hubs.ui.screens.subscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.HubController
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SubscriptionsManagementScreenViewModel : ViewModel() {
    private val _developFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val developFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _developFlowHubs

    private val _adminFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val adminFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _adminFlowHubs

    private val _designFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val designFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _designFlowHubs

    private val _managementFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val managementFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _managementFlowHubs

    private val _marketingFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val marketingFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _marketingFlowHubs

    private val _popsciFlowHubs = MutableLiveData<HabrList<HubSnippet>?>()
    val popsciFlowHubs: LiveData<HabrList<HubSnippet>?> get() = _popsciFlowHubs

    private val _companies = MutableLiveData<HabrList<CompanySnippet>?>()
    val companies: LiveData<HabrList<CompanySnippet>?> get() = _companies

    private val _authors = MutableLiveData<HabrList<UserSnippet>?>()
    val authors: LiveData<HabrList<UserSnippet>?> get() = _authors

    // TODO: Implement blacklisted users list at UsersListController
    private val _blocklisted = MutableLiveData<List<UsersListController.BlockedUser>?>()
    val blocklisted: LiveData<List<UsersListController.BlockedUser>?> get() = _blocklisted

    fun loadData(userAlias: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _developFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/develop/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )
            _adminFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/admin/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )
            _designFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/design/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )
            _managementFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/management/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )
            _marketingFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/marketing/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )
            _popsciFlowHubs.postValue(
                HubsListController.get(
                    "users/$userAlias/flow/popsci/subscriptions/hubs",
                    mapOf("perPage" to "100")
                )
            )

            _companies.postValue(
                CompaniesListController.get("users/$userAlias/subscriptions/companies")
            )

            _authors.postValue(
                UsersListController.get(
                    "users/$userAlias/followed",
                    mapOf("perPage" to "100")
                )
            )

            _blocklisted.postValue(UsersListController.getListOfBlockedByUser(perPage = 100))
        }
    }

    suspend fun toggleHubSubscription(alias: String): Boolean = withContext(Dispatchers.IO) { HubController.subscription(alias) }

    suspend fun toggleUserSubscription(alias: String): Boolean = withContext(Dispatchers.IO) { UserController.subscription(alias) }

    suspend fun toggleCompanySubscription(alias: String): Boolean = withContext(Dispatchers.IO) {CompanyController.subscription(alias) }

    suspend fun toggleUserBlocked(alias: String): Boolean = withContext(Dispatchers.IO) { UserController.blockListToggle(alias) }

}