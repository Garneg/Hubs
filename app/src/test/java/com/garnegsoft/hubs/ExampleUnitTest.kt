package com.garnegsoft.hubs

import com.garnegsoft.hubs.ui.screens.main.FilterPeriod
import com.garnegsoft.hubs.ui.screens.main.NewsFilter
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


}