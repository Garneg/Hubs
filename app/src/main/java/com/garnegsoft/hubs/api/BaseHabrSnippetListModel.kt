package com.garnegsoft.hubs.api.article

import androidx.lifecycle.*
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.HabrSnippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


abstract class AbstractSnippetListModel<S>(
        open val path: String,
        open val baseArgs: Map<String, String>,
        initialFilter: Filter? = null,
        open val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : HabrSnippetListModel<S> where S : HabrSnippet {

    private var _filter: MutableLiveData<Filter?> = MutableLiveData(initialFilter)
    val filter: LiveData<Filter?> get() = _filter

    fun editFilter(newFilter: Filter) {
        if (filter.value?.equals(newFilter) == false) {
            _filter.value = newFilter
            coroutineScope.launch(Dispatchers.IO) {
                _data.postValue(_load())
                
            }
        }
    }

    private val _data = MutableLiveData<HabrList<S>?>()

    override val data: LiveData<HabrList<S>?>
        get() = _data

    private val _isLoading = MutableLiveData(false)
    override val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isRefreshing = MutableLiveData(false)
    override val isRefreshing: LiveData<Boolean> get() = _isRefreshing
    
    private val _isLoadingNextPage = MutableLiveData(false)
    override val isLoadingNextPage: LiveData<Boolean> get() = _isLoadingNextPage

    private val _lastLoadedPage = MutableLiveData<Int>()
    override val lastLoadedPage: LiveData<Int>
        get() = _lastLoadedPage

    private var pageNumber = 1

    private fun _load(additionalArgs: Map<String, String> = mapOf()): HabrList<S>? {
        _isLoading.postValue(true)
        val result = load((filter.value?.toArgsMap() ?: emptyMap()) + baseArgs + additionalArgs)
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
                _isLoadingNextPage.postValue(true)
                pageNumber++
                var doRetry = true
                while (doRetry) {
                    _load(mapOf("page" to pageNumber.toString()))?.let { nextPage ->
                        _data.value?.let {
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
        }
    }

}


interface HabrSnippetListModel<T> where T : HabrSnippet {
    val data: LiveData<HabrList<T>?>

    val lastLoadedPage: LiveData<Int>

    val isLoading: LiveData<Boolean>

    val isRefreshing: LiveData<Boolean>
    
    val isLoadingNextPage: LiveData<Boolean>

    fun load(args: Map<String, String>): HabrList<T>?

    fun refresh()

    fun loadNextPage()

    fun loadFirstPage()

}


