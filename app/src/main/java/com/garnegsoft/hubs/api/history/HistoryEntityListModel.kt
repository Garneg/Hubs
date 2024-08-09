package com.garnegsoft.hubs.api.history

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.HabrSnippetListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryEntityListModel(
	val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
	val dao: HistoryDao
) : HabrSnippetListModel<HistoryEntity> {
	
	private val _data = MutableLiveData<HabrList<HistoryEntity>?>()
	
	override val data: LiveData<HabrList<HistoryEntity>?>
		get() = _data
	
	private val _isLoading = MutableLiveData(false)
	override val isLoading: LiveData<Boolean>
		get() = _isLoading
	
	private val _isRefreshing = MutableLiveData(false)
	override val isRefreshing: LiveData<Boolean> get() = _isRefreshing
	
	private val _isLoadingNextPage = MutableLiveData(false)
	override val isLoadingNextPage: LiveData<Boolean> get() = _isLoadingNextPage
	
	override fun load(args: Map<String, String>): HabrList<HistoryEntity> =
		HabrList(dao.getEventsPaged(pageNumber), pagesCount = dao.pagesCount())
	
	
	private val _lastLoadedPage = MutableLiveData<Int>()
	override val lastLoadedPage: LiveData<Int>
		get() = _lastLoadedPage
	
	private var pageNumber = 0
	
	private fun _load(): HabrList<HistoryEntity>? {
		_isLoading.postValue(true)
		val result = load(emptyMap())
		_isLoading.postValue(false)
		return result
	}
	
	override fun refresh() {
		coroutineScope.launch(Dispatchers.IO) {
			_isRefreshing.postValue(true)
			pageNumber = 0
			_lastLoadedPage.postValue(pageNumber)
			_data.postValue(_load())
			_isRefreshing.postValue(false)
		}
	}
	
	override fun loadNextPage() {
		coroutineScope.launch(Dispatchers.IO) {
			if (_data.value!!.pagesCount > pageNumber) {
				_isLoadingNextPage.postValue(true)
				pageNumber++
				var doRetry = true
				while (doRetry) {
					_load()?.let { nextPage ->
						_data.value?.let {
							delay(1000) // throttle
							_data.postValue(it + nextPage)
							doRetry = false
							_lastLoadedPage.postValue(pageNumber)
						}
					} ?: delay(500)
				}
				_isLoadingNextPage.postValue(false)
			}
		}
	}
	
	override fun loadFirstPage() {
		coroutineScope.launch(Dispatchers.IO) {
			_data.postValue(_load())
			_lastLoadedPage.postValue(0)
		}
	}
	
}