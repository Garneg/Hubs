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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.screens.article.CODE_ALPHA_VALUE
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.internal.MutablePropertyReference
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0


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
	var a = 0xFF_ea1_sports
	var b = 0b110101_101f()
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
		""".trimIndent().replace("\t", "   "), language = "go", spanStyle = SpanStyle()
			)
		}
	}
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
					val dividerColor = MaterialTheme.colors.onBackground
					Column(Modifier
						.drawBehind {
							val showDividerThreshold = 8.dp.toPx()
							drawRect(
								alpha = (codeScrollState.value / showDividerThreshold).coerceAtMost(
									1f
								) * 0.1f,
								color = dividerColor,
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