package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.OpportunityDto
import com.arrive.ai_training_5th_may_breakout_2.service.OpportunityService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class OpportunitiesPanel(
	private val opportunityService: OpportunityService,
	private val onPickCompetitor: (Long) -> Unit,
) : VerticalLayout() {

	private val grid = Grid<OpportunityDto>(OpportunityDto::class.java, false)
	private val toggle = Button("Show 25")
	private var currentLimit = 10

	init {
		setPadding(false)
		setSpacing(true)
		setWidthFull()

		val title = H3("Biggest opportunities").apply { style.set("margin", "0") }
		toggle.addClickListener {
			currentLimit = if (currentLimit == 10) 25 else 10
			toggle.text = if (currentLimit == 10) "Show 25" else "Show 10"
			reload()
		}
		val header = HorizontalLayout(title, toggle).apply {
			setWidthFull()
			defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
			expand(title)
		}

		configureGrid()
		add(header, grid)
	}

	private fun configureGrid() {
		grid.addColumn { it.keyword }.setHeader("Keyword").isAutoWidth = true
		grid.addColumn { it.gapType.name.lowercase().replaceFirstChar(Char::uppercase) }.setHeader("Gap")
		grid.addColumn { it.volume }.setHeader("Volume")
		grid.addColumn { it.kd?.toString() ?: "—" }.setHeader("KD")
		grid.addColumn { it.competitorName }.setHeader("Competitor")
		grid.addColumn { it.competitorPosition?.toString() ?: "—" }.setHeader("Their rank")
		grid.addColumn { row ->
			row.ourPosition?.toString() ?: if (row.gapType == GapType.MISSING) "not ranked" else "—"
		}.setHeader("Our rank")
		grid.addColumn { it.score }.setHeader("Score")
		grid.setHeight("300px")
		grid.setWidthFull()
		grid.addItemClickListener { event -> onPickCompetitor(event.item.competitorId) }
	}

	fun reload() {
		try {
			grid.setItems(opportunityService.topOpportunities(currentLimit))
		} catch (ex: Exception) {
			grid.setItems(emptyList())
			Notification.show("Couldn't load opportunities: ${ex.message ?: ex.javaClass.simpleName}")
		}
	}
}
