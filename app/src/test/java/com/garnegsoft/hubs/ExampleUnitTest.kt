package com.garnegsoft.hubs

import android.app.appsearch.GlobalSearchSession
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.ui.screens.main.NewsFilter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

//    @Test
//    fun test_csrfToken(){
//        HabrApi.HttpClient = OkHttpClient.Builder()
//            //.addInterceptor(NoConnectionInterceptor(this))
//            .build()
//        assertEquals(HabrApi.getCsrfToken(), HabrApi.getCsrfToken())
//    }

    @Test
    fun test_listInsertion(){
        val originalList = mutableListOf<Int>(5, 6, 7)
        originalList.addAll(1, listOf(8, 9))
        print(originalList)
    }
    
    @Test
    fun test_filterEquality(){
        val filter1 = NewsFilter(
            true, -1, FilterPeriod.Day
        )
        val filter2 = NewsFilter(
            true, -1, FilterPeriod.Day
        )
        val filter3 = NewsFilter(
            false, -1, FilterPeriod.Day
        )
        assertEquals(filter1, filter2)
        assertNotEquals(filter2, filter3)
    }
    
    @Test
    fun test_newAvatarPlaceholderUrl(){
        val alias = "A"
        val totalNumberOfPlaceholders = 200
        
        var result = alias.toCharArray().map { it.code }
            .reduce { acc, i ->
                acc + i
            } % totalNumberOfPlaceholders
        result++
            
        val resultString = result.toString().padStart(3, '0')
        val endString = "https://assets.habr.com/habr-web/img/avatars/${resultString}.png"
        
        assertEquals("https://assets.habr.com/habr-web/img/avatars/076.png", endString)
        
    }
    
    @Test
    fun test_flowShit() {
        val flow = getFlow()
        GlobalScope.launch {
            flow.collectLatest {
                println(it)
            }
        }
        while (true){}
    }
    
    fun getFlow() = flow<Int> {
        var counter = 0
        while (true) {
            counter++
            emit(counter)
        }
    }
    

}