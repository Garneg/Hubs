package com.garnegsoft.hubs

import ArticleController
import com.garnegsoft.hubs.data.HabrApi
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.Test

class ArticleResourceTests {
	
	@Test
	fun getAllImages(){
		HabrApi.setHttpClient(OkHttpClient())
		
		ArticleController.get(729112)?.let {
			var urls = mutableListOf<String>()
			println("OG html: \n${it.contentHtml}")
			var imageCounter = 0
			val result = Jsoup.parse(it.contentHtml).forEachNode {
				if(it is Element && it.tagName() == "img"){
					var url = if (it.hasAttr("data-src")){
						it.attr("data-src")
					} else {
						it.attr("src")
					}
					
					urls.add(url)
					it.attr("data-src", "img$imageCounter.jpg")
					
					imageCounter++
				}
				
			} as Document
			
			println("\n\n\nafter jsoup: \n${result.body().html()}")
			
			println("\n\nOG URLS:\n${urls.joinToString("\n")}")
		}
		
	}
	
}