package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.contracts.CompetitorDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup

class KeywordGapPanel(
	private val competitor: CompetitorDto,
	private val benchmarkService: BenchmarkService,
) : VerticalLayout() {

	private val toggle = RadioButtonGroup<GapType>().apply {
		setItems(GapType.MISSING, GapType.UNTAPPED)
		setItemLabelGenerator { it.name }
		value = GapType.MISSING
		// Inline the choices next to the label rather than stacking vertically.
		style.set("--vaadin-radio-group-orientation", "horizontal")
	}
	private val grid = Grid<KeywordGapRowDto>(KeywordGapRowDto::class.java, false)

	init {
		setWidthFull()
		setPadding(false)
		setSpacing(false)
		// Visually separate this section from the tabs above so it's obvious the filter and
		// table below are one unit.
		style
			.set("border", "1px solid var(--lumo-contrast-10pct)")
			.set("border-radius", "var(--lumo-border-radius-l)")
			.set("background", "var(--lumo-base-color)")
			.set("padding", "var(--lumo-space-m)")
			.set("margin-top", "var(--lumo-space-s)")

		val title = H3("Keyword gaps — ${competitor.name}").apply {
			style.set("margin", "0")
		}
		val filterLabel = Span("Show:").apply {
			style.set("color", "var(--lumo-secondary-text-color)")
				.set("font-weight", "500")
		}
		val header = HorizontalLayout(title, filterLabel, toggle).apply {
			setWidthFull()
			defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
			isSpacing = true
			expand(title)
		}

		configureGrid()
		toggle.addValueChangeListener { reload() }
		add(header, grid)
		reload()
	}

	private fun configureGrid() {
		grid.addColumn { it.keyword }.setHeader("Keyword")
		grid.addColumn { it.volume }.setHeader("Volume")
		grid.addColumn { it.kd?.toString() ?: "—" }.setHeader("KD")
		grid.addColumn { it.positionBase?.toString() ?: "—" }.setHeader("Arrive position")
		grid.addColumn { it.positionCompetitor?.toString() ?: "—" }.setHeader("Competitor position")
		grid.addColumn { it.cpc?.toPlainString() ?: "—" }.setHeader("CPC")
		grid.setHeight("320px")
		grid.setWidthFull()
	}

	private fun reload() {
		val id = competitor.id
		if (id == null) {
			grid.setItems(emptyList())
			return
		}
		try {
			val rows = benchmarkService.keywordGap(id, toggle.value ?: GapType.MISSING)
			grid.setItems(rows)
		} catch (ex: Exception) {
			grid.setItems(emptyList())
			Notification.show("Couldn't load gap rows: ${ex.message ?: ex.javaClass.simpleName}")
		}
	}
}
