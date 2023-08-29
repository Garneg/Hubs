package com.garnegsoft.hubs.ui.screens.company

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.user.UsersListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompanyScreenViewModel(val alias: String) : ViewModel() {
	var companyProfile = MutableLiveData<Company>()
	var companyWhoIs = MutableLiveData<Company.WhoIs>()
	
	val isRefreshing = MutableLiveData(false)
	fun refreshCompany() {
		isRefreshing.value = true
		viewModelScope.launch(Dispatchers.IO) {
			companyProfile.postValue(
				CompanyController.get(alias)
			)
			companyWhoIs.postValue(
				CompanyController.getWhoIs(alias)
			)
			isRefreshing.postValue(false)
		}
	}
	
	val blogArticlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("company" to alias),
		initialFilter = CompanyBlogArticlesFilter(true)
	)
	
	val blogNewsListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("companyNews" to alias)
	)
	val followersListModel = UsersListModel(
		path = "companies/$alias/fans/all",
		coroutineScope = viewModelScope
	)
	
	var employeesListModel = UsersListModel(
		path = "companies/$alias/workers/all",
		coroutineScope = viewModelScope
	)
}