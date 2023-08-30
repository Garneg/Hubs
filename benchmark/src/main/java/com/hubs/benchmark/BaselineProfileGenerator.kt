package com.hubs.benchmark

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
	@OptIn(ExperimentalBaselineProfilesApi::class)
	@get:Rule
	val baselineProfileRule = BaselineProfileRule()
	
	@OptIn(ExperimentalBaselineProfilesApi::class)
	@Test
	fun startup() {
		baselineProfileRule.collectBaselineProfile(
			packageName = "com.garnegsoft.hubs",
			profileBlock = {
				startActivityAndWait()
				
			}
		)
	}
}