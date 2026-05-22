package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import com.fasterxml.jackson.databind.ObjectMapper
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class TrafficChartPanel(
	private val benchmarkService: BenchmarkService,
) : VerticalLayout() {

	private val chart = EchartsLineChart()
	private val emptyState = Span("No traffic history yet — click \"Refresh all\" to populate.")
	private val mapper = ObjectMapper()

	init {
		setPadding(false)
		setSpacing(true)
		setWidthFull()
		val title = H3("Organic traffic — last 12 months").apply { style.set("margin", "0") }
		add(title, chart, emptyState)
	}

	fun reload() {
		try {
			val series = benchmarkService.trafficHistory().filter { it.points.isNotEmpty() }
			if (series.isEmpty()) {
				chart.isVisible = false
				emptyState.isVisible = true
				return
			}
			chart.isVisible = true
			emptyState.isVisible = false
			val payload = series.map { s ->
				mapOf(
					"name" to s.competitor.name,
					"isOwn" to s.competitor.isOwn,
					"points" to s.points.map { p ->
						mapOf("month" to p.month, "organicTraffic" to p.organicTraffic)
					},
				)
			}
			chart.render(mapper.writeValueAsString(payload))
		} catch (ex: Exception) {
			Notification.show("Couldn't load traffic history: ${ex.message ?: ex.javaClass.simpleName}")
		}
	}
}
