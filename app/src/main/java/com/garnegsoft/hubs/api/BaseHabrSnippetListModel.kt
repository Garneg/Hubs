package com.garnegsoft.hubs.api.article

import ArticlesListController
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.*
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.HabrSnippet
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


abstract class AbstractHabrSnippetListModel<T>(
    override val path: String,
    override val baseArgs: Map<String, String>,
    initialFilter: Map<String, String> = mapOf(),
    open val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : HabrSnippetListModel<T> where T : HabrSnippet {

    private var filterMap: Map<String, String> = initialFilter

    fun editFilter(newFilter: Map<String, String>) {
        filterMap = newFilter
        _data.postValue(_load())
    }

    private val _data = MutableLiveData<HabrList<T>?>()

    override val data: LiveData<HabrList<T>?>
        get() = _data

    private val _isLoading = MutableLiveData(false)
    override val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isRefreshing = MutableLiveData(false)
    override val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    private val _lastLoadedPage = MutableLiveData<Int>()
    override val lastLoadedPage: LiveData<Int>
        get() = _lastLoadedPage

    private var pageNumber = 1

    private fun _load(additionalArgs: Map<String, String> = mapOf()): HabrList<T>? {
        _isLoading.postValue(true)
        val result = load(filterMap + baseArgs)
        _isLoading.postValue(false)
        return result
    }

    override fun refresh() {
        coroutineScope.launch(Dispatchers.IO) {
            _isRefreshing.postValue(true)
            pageNumber = 1
            _lastLoadedPage.postValue(1)
            _data.postValue(_load())
            _isRefreshing.postValue(false)
        }
    }

    override fun loadNextPage() {
        coroutineScope.launch(Dispatchers.IO) {
            if (_data.value!!.pagesCount > pageNumber) {
                pageNumber++
                var doRetry = true
                while (doRetry) {
                    _load(mapOf("page" to pageNumber.toString()))?.let { nextPage ->
                        _data.value?.let {
                            _data.postValue(it + nextPage)
                            doRetry = false
                            _lastLoadedPage.postValue(pageNumber)
                        }
                    }
                }
            }

        }
    }

    override fun loadFirstPage() {
        coroutineScope.launch(Dispatchers.IO) {
            _data.postValue(_load())
        }
    }

}


interface HabrSnippetListModel<T> where T : HabrSnippet {
    val path: String
    val baseArgs: Map<String, String>
    val data: LiveData<HabrList<T>?>

    val lastLoadedPage: LiveData<Int>

    val isLoading: LiveData<Boolean>

    val isRefreshing: LiveData<Boolean>

    fun load(args: Map<String, String>): HabrList<T>?

    fun refresh()

    fun loadNextPage()

    fun loadFirstPage()

}



