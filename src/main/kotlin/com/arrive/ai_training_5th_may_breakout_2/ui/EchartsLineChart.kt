package com.arrive.ai_training_5th_may_breakout_2.ui

import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.html.Div

/**
 * Thin wrapper around Apache ECharts (MIT). The JS bundle is loaded once via @NpmPackage; the
 * companion echarts-line-chart.js exports a single window.echartsLineChart.renderLineChart(el, json).
 * Kotlin side just hands serialised series data to the browser — no chart state lives on the server.
 */
@NpmPackage(value = "echarts", version = "5.5.1")
@JsModule("./echarts-line-chart.js")
class EchartsLineChart : Div() {

	init {
		setWidthFull()
		height = "320px"
	}

	fun render(seriesJson: String) {
		element.executeJs("window.echartsLineChart.renderLineChart(this, \$0)", seriesJson)
	}
}
