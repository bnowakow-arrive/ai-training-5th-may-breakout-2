package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.contracts.CompetitorDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup

class KeywordGapPanel(
	private val competitor: CompetitorDto,
	private val benchmarkService: BenchmarkService,
) : VerticalLayout() {

	private val toggle = RadioButtonGroup<GapType>().apply {
		label = "Gap type"
		setItems(GapType.MISSING, GapType.UNTAPPED)
		setItemLabelGenerator { it.name }
		value = GapType.MISSING
	}
	private val grid = Grid<KeywordGapRowDto>(KeywordGapRowDto::class.java, false)

	init {
		setWidthFull()
		setPadding(false)
		setSpacing(true)
		configureGrid()
		toggle.addValueChangeListener { reload() }
		add(toggle, grid)
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
