package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlin.reflect.KMutableProperty0


abstract class CycleBasedComponent {
	val ranges = mutableListOf<AnnotatedString.Range<SpanStyle>>()
	abstract val spanStyle: SpanStyle
	abstract fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>)
	
}

class CycleBasedComponents {
	/**
	 * Made for strings that start and end with " (quotation mark)
	 */
	class QuotationMarkStringComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		
		private var firstStringQuoteIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '"' && !(index > 0 && code[index - 1] == '\\') && (lock.get() == Lock.None || lock.get() == Lock.String)) {
				if (firstStringQuoteIndex < 0) {
					firstStringQuoteIndex = index
					lock.set(Lock.String)
				} else {
					ranges.add(
						AnnotatedString.Range<SpanStyle>(
							spanStyle,
							firstStringQuoteIndex,
							index + 1
						)
					)
					firstStringQuoteIndex = -1
					lock.set(Lock.None)
				}
			}
		}
		
	}
	
	class ApostropheStringComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		private var firstStringApostropheIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '\'' && !(index > 0 && code[index - 1] == '\\') && (lock.get() == Lock.None || lock.get() == Lock.String)) {
				if (firstStringApostropheIndex < 0) {
					firstStringApostropheIndex = index
					lock.set(Lock.String)
				} else {
					ranges.add(
						AnnotatedString.Range<SpanStyle>(
							spanStyle,
							firstStringApostropheIndex,
							index + 1
						)
					)
					firstStringApostropheIndex = -1
					lock.set(Lock.None)
				}
			}
		}
	}
	
	class SinglelineCommentComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		var lineCommentStartIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '/' && (index > 0 && code[index - 1] == '/') && lock.get() == Lock.None) {
				lineCommentStartIndex = index - 1
				lock.set(Lock.SingleLineComment)
			}
			if (code[index] == '\n' && lock.get() == Lock.SingleLineComment) {
				ranges.add(AnnotatedString.Range(spanStyle, lineCommentStartIndex, index))
				lineCommentStartIndex = -1
				lock.set(Lock.None)
			}
			if (index == code.lastIndex && lock.get() == Lock.SingleLineComment){
				ranges.add(AnnotatedString.Range(spanStyle, lineCommentStartIndex, index + 1))
				lineCommentStartIndex = -1
				lock.set(Lock.None)
			}
		}
		
	}
	
	class IntegerComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		var lastNumberIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index].isDigit() && lock.get() == Lock.None && !(lastNumberIndex != index - 1 && code.elementAtOrNull(index - 1)?.isDigit() == true)) {
				if (index > 0 && !code[index - 1].isLetter()){
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							index,
							index + 1
						)
					)
					lastNumberIndex = index
				}
				else if (index > 2 && listOf('o', 'b', '_').contains(code[index - 1]) && code[index - 2].isDigit() && !code[index - 3].isLetterOrDigit()) {
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							index-1,
							index + 1
						)
					)
					lastNumberIndex = index
				}
			}
			
			// hex literals
			if (code[index] == 'x' && lock.get() == Lock.None && code.elementAtOrNull(index - 1) == '0' && index < code.lastIndex) {
				val allowedChars = "ABCDEFabcdef_".toList()
				
				var hexCharIndex = index + 1
				while(true){
					if (!allowedChars.contains(code[hexCharIndex]) && !code[hexCharIndex].isDigit()) {
						ranges.add(
							AnnotatedString.Range(
								spanStyle,
								index, hexCharIndex
							)
						)
						break
					}
					if (hexCharIndex < code.lastIndex)
						hexCharIndex++
					else {
						ranges.add(
							AnnotatedString.Range(
								spanStyle,
								index, hexCharIndex
							)
						)
						break
					}
					
				}
				
				
				
			}
			
			// floating point values
			if (code[index] == '.' && lock.get() == Lock.None && code.elementAtOrNull(index -1)?.isDigit() == true && code.elementAtOrNull(index + 1)?.isDigit() == true) {
				ranges.add(
					AnnotatedString.Range(spanStyle, index, index + 1)
				)
			}
		}
	}
	
	/**
	 * Has detecting most common multiline string literals pattern with triple quotes (`"""`).
	 *
	 * If language has other multiline string literals pattern **do not use** this component
	 */
	// TODO: Test against language with this ml string pattern
	class MultilineStringComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		var multilineStringStartIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '"' && code.substring(index, index + 3) == "\"\"\"") {
				if (lock.get() == Lock.None) {
					multilineStringStartIndex = index
					lock.set(Lock.MultilineString)
				} else if (lock.get() == Lock.MultilineString) {
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							multilineStringStartIndex,
							index + 3
						)
					)
					lock.set(Lock.None)
					multilineStringStartIndex = -1
				}
			}
		}
	}
	
	class CharComponent(
		override val spanStyle: SpanStyle
	
	) : CycleBasedComponent() {
		var firstCharApostropheIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '\'' && !(index > 0 && code[index - 1] == '\\') && (lock.get() == Lock.None || lock.get() == Lock.Char)) {
				if (lock.get() == Lock.None) {
					firstCharApostropheIndex = index
					lock.set(Lock.Char)
				} else {
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							firstCharApostropheIndex,
							index + 1
						)
					)
					lock.set(Lock.None)
				}
			}
		}
		
	}
	
	class MultilineComment(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		var multilineCommentStartIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '/') {
				if (code.length - 1 > index && code[index + 1] == '*' && lock.get() == Lock.None) {
					multilineCommentStartIndex = index
					lock.set(Lock.MultilineComment)
				}
				
				if (index > 0 && code[index - 1] == '*' && lock.get() == Lock.MultilineComment) {
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							multilineCommentStartIndex,
							index + 1
						)
					)
					multilineCommentStartIndex = -1
					lock.set(Lock.None)
				}
			}
		}
	}
	

}