package com.garnegsoft.hubs

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.junit.Test

class HtmlParserTests {
	
	val html = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>Инженер по графике Weta Workshop Джеймс Браун <a href=\"https://www.theverge.com/2024/9/6/24238004/doom-volumetric-voxel-display-ancient-james-brown\" rel=\"noopener noreferrer nofollow\">сконструировал</a> устройство в форме шара, который вращается и демонстрирует мерцающее трёхмерное изображение. Энтузиаст смог запустить на нём Doom.</p><figure class=\"full-width \"><img src=\"https://habrastorage.org/r/w780q1/getpro/habr/upload_files/dff/595/f21/dff595f21f8ca3827ebd5c13ef6f250c.jpg\" width=\"800\" height=\"600\" data-src=\"https://habrastorage.org/getpro/habr/upload_files/dff/595/f21/dff595f21f8ca3827ebd5c13ef6f250c.jpg\" data-blurred=\"true\"/></figure><p>Браун использовал версию Voxel Doom. В ней каждой точке игрового объекта назначена позиция в трёхмерном пространстве, как и точкам на объёмном дисплее. Сам дисплей не трёхмерный. «Это как голографический вентилятор, но вместо того, чтобы вращать 1D-полоску для создания 2D-изображения, он вращает 2D-панель для производства 3D-изображения», — пояснил инженер.</p><div class=\"tm-iframe_temp\" data-src=\"https://embedd.srv.habr.com/iframe/66dd432bd1b5b66d8d1cb82b\" data-style=\"\" id=\"66dd432bd1b5b66d8d1cb82b\" width=\"\"></div><p>Первоначально планировалось, что для создания объёмной картинки устройство должно вращаться со скоростью 300 оборотов в минуту, но потом выяснилось, что для вывода плавного движения этого недостаточно. </p><div class=\"tm-iframe_temp\" data-src=\"https://embedd.srv.habr.com/iframe/66dd434628dfc09d9e81d051\" data-style=\"\" id=\"66dd434628dfc09d9e81d051\" width=\"\"></div><p>Браун продемонстрировал несколько примеров работы своего 3D-экрана. </p><div class=\"tm-iframe_temp\" data-src=\"https://embedd.srv.habr.com/iframe/66dd43576748d7d46ff764ab\" data-style=\"\" id=\"66dd43576748d7d46ff764ab\" width=\"\"></div><p>Браун также создаёт компьютерные блоки Lego, на которых можно играть в Doom.</p><div class=\"tm-iframe_temp\" data-src=\"https://embedd.srv.habr.com/iframe/66dd4366ee68229db67f2a85\" data-style=\"\" id=\"66dd4366ee68229db67f2a85\" width=\"\"></div><p>Ранее разработчик <a href=\"https://habr.com/ru/news/810603/\" rel=\"noopener noreferrer nofollow\">представил</a> порт Doom под названием cyDoomGeneric для запуска игры в приложении Microsoft Paint под Windows XP.</p></div>"
	
	@Test
	fun test_basic() {
		fun nodeTreeInfo(level: Int, element: Node) {
			println("${"\t".repeat(level)}${element::class.java.name.split('.').last()}${if (element is Element) "(${element.tagName()})" else ""} - ${if(element is Element) element.isBlock else false}")
			element.childNodes().forEach { nodeTreeInfo(level + 1, it) }
		}
		nodeTreeInfo(0, Jsoup.parse(html).body().child(0))
	}
}