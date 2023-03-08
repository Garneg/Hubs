package com.garnegsoft.hubs.api

import androidx.compose.runtime.Immutable


@Immutable
class HabrList<T>(val list: List<T>, val pagesCount: Int) where T : HabrSnippet  {

    operator fun plus(secondList: HabrList<T>): HabrList<T>{
        var templist = ArrayList<T>().apply {
            addAll(list)
            addAll(secondList.list)
        }
        templist = templist.distinctBy { it.id } as ArrayList<T>
        return HabrList(templist, this.pagesCount)
    }

}

interface HabrSnippet {
    val id: Int
}