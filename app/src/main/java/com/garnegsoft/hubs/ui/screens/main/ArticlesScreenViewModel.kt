package com.garnegsoft.hubs.ui.screens.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.user.list.UserSnippet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArticlesScreenViewModel : ViewModel() {

    val myFeedArticlesModel = ArticlesListModel(
        path = "articles",
        coroutineScope = viewModelScope,
        "custom" to "true"
    )
    
    val articlesListModel = ArticlesListModel(
        path = "articles",
        coroutineScope = viewModelScope,
        initialFilter = mapOf("sort" to "rating")
    )
    
    var articlesFilter = MutableLiveData<ArticlesFilterState>(
        ArticlesFilterState(
            showLast = true,
            complexity = PostComplexity.None
        )
    )
    
    val newsListModel = ArticlesListModel(
        path = "articles",
        coroutineScope = viewModelScope,
        initialFilter = mapOf("sort" to "rating"),
        baseArgs = arrayOf("news" to "true")
    )

    fun updateArticlesFilter(newFilter: ArticlesFilterState) {
        if (articlesFilter.value?.hasChanged(newFilter) == true) {
            articlesFilter.postValue(newFilter)
            viewModelScope.launch(Dispatchers.IO) {
                var argsMap: Map<String, String> =
                    if (newFilter.showLast) {
                        if (newFilter.minRating == -1) {
                            mapOf(
                                "sort" to "rating",
                            )
                        } else {
                            mapOf(
                                "sort" to "rating",
                                "score" to newFilter.minRating.toString()
                            )
                        }
                    } else {
                        mapOf(
                            "sort" to "date",
                            "period" to when (newFilter.period) {
                                FilterPeriod.Day -> "daily"
                                FilterPeriod.Week -> "weekly"
                                FilterPeriod.Month -> "monthly"
                                FilterPeriod.Year -> "yearly"
                                FilterPeriod.AllTime -> "alltime"
                            },
                        )
                    }
                
                if (newFilter.complexity != PostComplexity.None) {
                    argsMap += mapOf(
                        "complexity" to when (newFilter.complexity) {
                            PostComplexity.Low -> "easy"
                            PostComplexity.Medium -> "medium"
                            PostComplexity.High -> "hard"
                            else -> throw IllegalArgumentException("mapping of this complexity is not supported")
                        }
                    )
                }
                articlesListModel.editFilter(argsMap)

            }

        }
    }

    val isLoadingArticles = MutableLiveData<Boolean>(false)

    var newsFilter = MutableLiveData<NewsFilterState>(
        NewsFilterState(
            showLast = true,
            minRating = -1,
            period = FilterPeriod.Day
        )
    )
    val isLoadingNews = MutableLiveData(false)

    fun changeNewsFilterAndLoadNews(newFilter: NewsFilterState) {
        if (newsFilter.value?.hasChanged(newFilter) == true) {
            newsFilter.postValue(newFilter)
            viewModelScope.launch(Dispatchers.IO) {
                val argsMap: Map<String, String> =
                    if (newFilter.showLast) {
                        if (newFilter.minRating == -1) {
                            mapOf(
                                "sort" to "rating",
                            )
                        } else {
                            mapOf(
                                "sort" to "rating",
                                "score" to newFilter.minRating.toString()
                            )
                        }
                    } else {
                        mapOf(
                            "sort" to "date",
                            "period" to when (newFilter.period) {
                                FilterPeriod.Day -> "daily"
                                FilterPeriod.Week -> "weekly"
                                FilterPeriod.Month -> "monthly"
                                FilterPeriod.Year -> "yearly"
                                FilterPeriod.AllTime -> "alltime"
                            },
                        )
                    } + mapOf("news" to "true")
                ArticlesListController.getArticlesSnippets(
                    path = "articles",
                    args = argsMap
                )?.let {
                    news.postValue(it)
                }

            }
        }
    }

    fun loadNews(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val filter = newsFilter.value!!
            val argsMap: Map<String, String> =
                if (filter.showLast) {
                    if (filter.minRating == -1) {
                        mapOf(
                            "sort" to "rating",
                        )
                    } else {
                        mapOf(
                            "sort" to "rating",
                            "score" to filter.minRating.toString()
                        )
                    }
                } else {
                    mapOf(
                        "sort" to "date",
                        "period" to when (filter.period) {
                            FilterPeriod.Day -> "daily"
                            FilterPeriod.Week -> "weekly"
                            FilterPeriod.Month -> "monthly"
                            FilterPeriod.Year -> "yearly"
                            FilterPeriod.AllTime -> "alltime"
                        },
                    )
                }

            ArticlesListController.getArticlesSnippets(
                path = "articles", args = argsMap
            )?.let {
                news.postValue(it)
            }
        }
    }

    fun ArticlesFilterState.hasChanged(newFilter: ArticlesFilterState): Boolean {
        if (showLast != newFilter.showLast)
            return true
        if (complexity != newFilter.complexity)
            return true
        if (showLast) {
            return minRating != newFilter.minRating
        } else
            return period != newFilter.period
    }

    fun NewsFilterState.hasChanged(newFilter: NewsFilterState): Boolean {
        if (showLast != newFilter.showLast)
            return true
        if (showLast) {
            return minRating != newFilter.minRating
        } else
            return period != newFilter.period
    }

    var news = MutableLiveData<HabrList<ArticleSnippet>>()
    var hubs = MutableLiveData<HabrList<HubSnippet>>()
    var authors = MutableLiveData<HabrList<UserSnippet>>()
    var companies = MutableLiveData<HabrList<CompanySnippet>>()
}