package com.garnegsoft.hubs.ui.screens.article.html

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.screens.article.CODE_ALPHA_VALUE
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview
@Composable
fun CodeElementPreview() {
	HubsTheme {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(16.dp)
		) {
			CodeElement(
				code = """
package main

import (
	"context"
	"crypto/tls"
	"fmt"
	"github.com/gorilla/mux"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"net/http"
	"os"
	"serbianDictionary/configuration"
	"serbianDictionary/handlers"
	"serbianDictionary/routes"
)

func main() {
	configFilePath, err := configuration.CheckConfigFileArgument()
	if err != nil {
		println("Error occured with the server config:\n\t" + err.Error())
		return
	}

	println("Server Started!")

	configBytes, _ := os.ReadFile(configFilePath)
	serverConfig, err := configuration.ParseConfig(configBytes)

	if err != nil {
		println("Error occured with the server config:\n\t" + err.Error())
		return
	}

	client, _ := InitializeMongoClient(serverConfig)
	db := client.Database("dictionary")
	wordsCollection := db.Collection("words")

	router := mux.NewRouter()
	router.HandleFunc("/api/ping", func(response http.ResponseWriter, request *http.Request) {
		fmt.Fprint(response, "Ping!")
	})
	routes.InitMainApiRoutes(router, serverConfig, wordsCollection)
	routes.InitAdminApiRoutes(router, serverConfig, db)
	var a = 0xFF
	var b = 0b110101
	if len(serverConfig.TLSCertificates1) > 0 {
		go func() {
			println(http.ListenAndServe(serverConfig.BindAddress+":80", handlers.HttpToHttpsHandler{}).Error())
		}()
	}

	mainFileHandler := http.FileServer(http.Dir(serverConfig.MainAppDirectory))
	adminFileHandler := http.FileServer(http.Dir(serverConfig.AdminAppDirectory))

	http.HandleFunc("/", func(responseWriter http.ResponseWriter, request *http.Request) {

		if request.Host == serverConfig.AdminHostAddress {
			adminFileHandler.ServeHTTP(responseWriter, request)
		} else {
			mainFileHandler.ServeHTTP(responseWriter, request)
		}

	})

	http.Handle("/api/", router)

	tlsConfig := tls.Config{}
	tlsConfig.Certificates = make([]tls.Certificate, len(serverConfig.TLSCertificates))

	for i, certificate := range serverConfig.TLSCertificates {
		tlsConfig.Certificates[i], _ = tls.LoadX509KeyPair(certificate.CertFile, certificate.KeyFile)
	}

	if len(tlsConfig.Certificates) > 0 {
		listener, listenerError := tls.Listen("tcp", serverConfig.BindAddress+":443", &tlsConfig)
		if listenerError != nil {
			println(listenerError.Error())

			return
		}

		server := http.Server{
			TLSConfig: &tlsConfig,
		}
		err = server.Serve(listener)
		if err != nil {
			println(err.Error())
		}
	} else {
		server := http.Server{}
		err := server.ListenAndServe()
		if err != nil {
			println(err.Error())
			return
		}
	}
}

func InitializeMongoClient(config configuration.ServerConfig) (*mongo.Client, error) {
	serverAPIVersion := options.ServerAPI(options.ServerAPIVersion1)
	options.Client()
	var opts *options.ClientOptions
	if config.DoMongoDBAuth {
		opts = options.Client().ApplyURI(config.MongoDBConnectionString).SetAuth(config.MongoDBCredentials).SetServerAPIOptions(serverAPIVersion)
	} else {
		opts = options.Client().ApplyURI(config.MongoDBConnectionString).SetServerAPIOptions(serverAPIVersion)
	}

	return mongo.Connect(context.Background(), opts)
}

type interceptedHandler struct {
	interceptionFunction func(response http.ResponseWriter, request *http.Request, handler http.Handler)
	interceptedHandler   http.Handler
}

func (handler interceptedHandler) ServeHTTP0x111(response http.ResponseWriter, request *http.Request) {
	responseRef := &response
	handler.interceptionFunction(*responseRef, request, handler.interceptedHandler)
}
		""".trimIndent().replace("\t", "    "), language = "go", spanStyle = SpanStyle()
			)
		}
	}
}

val stringLiteralSpanStyle = SpanStyle(
	color = Color(0xFF579737),
	
	)
val keywordSpanStyle = SpanStyle(
	color = Color(0xFF2F6BA7),
	
	)

val keywordsList = listOf(
	"any", "break", "default", "error", "func",
	"interface", "select", "case", "defer", "go",
	"map", "struct", "chan", "else", "goto",
	"package", "switch", "const", "fallthrough", "if",
	"range", "type", "continue", "for", "import",
	"return", "var", "nil",
	
	//base types
	"int", "int8", "int16", "int32", "int64",
	"uint", "uint8", "uint16", "uint32", "uint64", "uintptr",
	"float32", "float64",
	"complex64", "complex128",
	"string", "byte", "rune", "bool", "error"
)

val commentsSpanStyle = SpanStyle(
	color = Color.Gray,
	fontStyle = FontStyle.Italic
)

val functionCallSpanStyle = SpanStyle(
	color = Color(0xFF418071)
)

val numberLiteralSpanStyle = SpanStyle(
	color = Color(0xFF3265C4),
)

val builtInFunctions = listOf(
	"len", "append", "println", "print", "Error",
)

val excludeFromFunctionsCalls = listOf(
	"func", "import", "var"
)

enum class Lock {
	None,
	String,
	Char,
	Comment,
	MultilineString,
	MultilineComment
}

@Composable
fun CodeElement(
	code: String,
	language: String,
	spanStyle: SpanStyle,
) {
	var codeSpanStylesList by remember { mutableStateOf(listOf<AnnotatedString.Range<SpanStyle>>()) }
	LaunchedEffect(key1 = MaterialTheme.colors.isLight, block = {
		val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()
		
		var lastKeywordIndex = 0
		while (true) {
			val pair = code.findAnyOf(keywordsList, lastKeywordIndex)
			
			if (pair == null) break
			val indexOfLastChar = pair.first + pair.second.length
			if (pair.first > 0 && (code[pair.first - 1].isLetterOrDigit() || code[indexOfLastChar].isLetterOrDigit())) {
				lastKeywordIndex = indexOfLastChar
				continue
			}
			spanStylesList.add(
				AnnotatedString.Range(keywordSpanStyle, pair.first, indexOfLastChar)
			)
			lastKeywordIndex = indexOfLastChar
		}
		
		var lock = Lock.None
		var lineCommentStartIndex = -1
		var firstStringQuoteIndex = -1
		var firstCharApostropheIndex = -1
		var multilineCommentStartIndex = -1
		var multilineStringStartIndex = -1
		code.forEachIndexed { index, it ->
			
			if (it == '"' && !(index > 0 && code[index - 1] == '\\') && (lock == Lock.None || lock == Lock.String)) {
				if (firstStringQuoteIndex < 0) {
					firstStringQuoteIndex = index
					lock = Lock.String
				} else {
					spanStylesList.add(
						AnnotatedString.Range<SpanStyle>(
							stringLiteralSpanStyle,
							firstStringQuoteIndex,
							index + 1
						)
					)
					firstStringQuoteIndex = -1
					lock = Lock.None
				}
			}
			
			if (it == '\'' && !(index > 0 && code[index - 1] == '\\') && (lock == Lock.None || lock == Lock.Char)) {
				if (firstCharApostropheIndex < 0) {
					firstCharApostropheIndex = index
					lock = Lock.Char
				} else {
					spanStylesList.add(
						AnnotatedString.Range(
							stringLiteralSpanStyle,
							firstCharApostropheIndex,
							index + 1
						)
					)
					lock = Lock.None
				}
			}
			
			if (it == '/' && (index > 0 && code[index - 1] == '/') && lock == Lock.None) {
				lineCommentStartIndex = index - 1
				lock = Lock.Comment
			}
			if (it == '\n' && lineCommentStartIndex > -1) {
				spanStylesList.add(
					AnnotatedString.Range(commentsSpanStyle, lineCommentStartIndex, index)
				)
				lineCommentStartIndex = -1
				lock = Lock.None
			}
			
			if (it == '/') {
				if (code.length - 1 > index && code[index + 1] == '*' && lock == Lock.None) {
					multilineCommentStartIndex = index
					lock = Lock.MultilineComment
				}
				
				if (index > 0 && code[index - 1] == '*' && lock == Lock.MultilineComment) {
					spanStylesList.add(
						AnnotatedString.Range(
							commentsSpanStyle,
							multilineCommentStartIndex,
							index + 1
						)
					)
					multilineCommentStartIndex = -1
					lock = Lock.None
				}
			}
			
			if (it == '`') {
				if (lock == Lock.None) {
					multilineStringStartIndex = index
					lock = Lock.MultilineString
				} else if (lock == Lock.MultilineString) {
					spanStylesList.add(
						AnnotatedString.Range(
							stringLiteralSpanStyle,
							multilineStringStartIndex,
							index + 1
						)
					)
					lock = Lock.None
					multilineStringStartIndex = -1
				}
			}
			
			if (it.isDigit() && lock == Lock.None) {
				if (index > 0 && !code[index - 1].isLetter()){
				
						spanStylesList.add(
							AnnotatedString.Range(
								numberLiteralSpanStyle,
								index,
								index + 1
							)
						)
					}
				else if (index > 2 && listOf('x', 'o', 'b', '_').contains(code[index - 1]) && code[index - 2].isDigit() && !code[index - 3].isLetterOrDigit()) {
					spanStylesList.add(
						AnnotatedString.Range(
							numberLiteralSpanStyle,
							index-1,
							index + 1
						)
					)
				}
			}
			
			if (it == '(' && lock == Lock.None) {
				if (index > 0 && code[index - 1].isLetterOrDigit() || code[index - 1] == '_'){
					val functionName = code.slice(0..index - 1).trimEnd().takeLastWhile {
						it.isLetterOrDigit() || it == '_'
					}
					if (!excludeFromFunctionsCalls.contains(functionName)) {
						spanStylesList.add(
							AnnotatedString.Range(
								functionCallSpanStyle,
								index - functionName.length,
								index
							)
						)
					}
				}
			}
		}
		
		
		
		
		codeSpanStylesList = spanStylesList
	})
	val annotatedStringCode = remember(codeSpanStylesList) {
		AnnotatedString(
			text = code,
			spanStyles = codeSpanStylesList
		)
	}
	Column(Modifier.clip(RoundedCornerShape(8.dp))) {
		Surface(
			color = MaterialTheme.colors.onBackground.copy(CODE_ALPHA_VALUE),
			modifier = Modifier.fillMaxWidth()
		) {
			DisableSelection {
				Row(modifier = Modifier.padding(8.dp)) {
					Text(
						text = buildAnnotatedString { withStyle(spanStyle) { append(language) } },
						fontWeight = FontWeight.W600,
						fontFamily = FontFamily.SansSerif
					)
					Spacer(modifier = Modifier.width(6.dp))
				}
			}
			
			
		}
		val codeScrollState = rememberScrollState()
		Surface(
			color = MaterialTheme.colors.onBackground.copy(CODE_ALPHA_VALUE),
			modifier = Modifier.fillMaxWidth()
		) {
			Row() {
				Surface(
					color = MaterialTheme.colors.onBackground.copy(0f),
				) {
					Column(Modifier
						.drawBehind {
							val showDividerThreshold = 8.dp.toPx()
							drawRect(
								alpha = (codeScrollState.value / showDividerThreshold).coerceAtMost(
									1f
								) * 0.1f,
								color = Color.Black,
								topLeft = Offset(size.width - 3f, 0f),
								size = Size(3f, size.height)
							)
						}
						.padding(8.dp)
					) {
						var linesIndicator = String()
						for (i in 1..code.count { it == "\n"[0] } + 1) {
							linesIndicator += "$i\n"
						}
						linesIndicator = linesIndicator.take(linesIndicator.length - 1)
						
						DisableSelection {
							Text(
								text = buildAnnotatedString {
									withStyle(
										spanStyle.copy(
											color = MaterialTheme.colors.onBackground.copy(
												0.5f
											)
										)
									) { append(linesIndicator) }
								},
								lineHeight = 16.sp * 1.2f,
								color = MaterialTheme.colors.onBackground.copy(0.5f),
								fontFamily = FontFamily.Monospace,
								textAlign = TextAlign.End
							)
						}
					}
				}
				Row(
					modifier = Modifier
						.horizontalScroll(codeScrollState)
						.fillMaxWidth()
						.padding(8.dp)
				) {
					val jetbrainsMonoFontFamily = FontFamily(
						listOf(
							Font(R.font.jetbrains_mono_regular_nl, FontWeight.Normal),
							Font(R.font.jetbrains_mono_medium_nl, FontWeight.Medium),
							Font(
								R.font.jetbrains_mono_medium_nl_italic,
								FontWeight.Medium,
								FontStyle.Italic
							)
						)
					)
					val robotoMono = FontFamily(Font(R.font.roboto_mono_variable))
					
					
						SelectionContainer {
							Text(
								text = annotatedStringCode,
								fontFamily = jetbrainsMonoFontFamily,
								fontWeight = FontWeight.Normal,
								lineHeight = 16.sp * 1.2f,
								color = MaterialTheme.colors.onBackground
							)
						}
					
				}
			}
		}
	}
}