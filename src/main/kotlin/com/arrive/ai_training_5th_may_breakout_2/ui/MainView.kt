package com.arrive.ai_training_5th_may_breakout_2.ui

import com.arrive.ai_training_5th_may_breakout_2.contracts.BenchmarkResponse
import com.arrive.ai_training_5th_may_breakout_2.contracts.MetricRow
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import com.arrive.ai_training_5th_may_breakout_2.service.CompetitorService
import com.arrive.ai_training_5th_may_breakout_2.service.OpportunityService
import com.arrive.ai_training_5th_may_breakout_2.service.RefreshService
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Value
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Route("")
@PageTitle("Arrive Competitor Intelligence")
class MainView(
	private val competitorService: CompetitorService,
	private val refreshService: RefreshService,
	private val benchmarkService: BenchmarkService,
	private val opportunityService: OpportunityService,
	@Value("\${semrush.gap-row-limit:25}") private val gapRowLimit: Int,
	@Value("\${semrush.history-months:12}") private val historyMonths: Int,
) : AppLayout() {

	private val trafficChartPanel = TrafficChartPanel(benchmarkService)
	private val benchmarkGrid = Grid<MetricRow>(MetricRow::class.java, false)
	private val opportunitiesPanel = OpportunitiesPanel(opportunityService) { competitorId ->
		tabsByCompetitor[competitorId]?.let { tabs.selectedTab = it }
	}
	private val tabs = Tabs().apply { isAutoselect = false }
	private val tabContent = VerticalLayout().apply {
		setPadding(false)
		setSpacing(false)
		setWidthFull()
	}
	private val tabPanels = mutableMapOf<Tab, KeywordGapPanel>()
	private val tabsByCompetitor = mutableMapOf<Long, Tab>()
	private val timestampFormatter: DateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

	init {
		addToNavbar(buildHeader())
		setContent(buildBody())
		tabs.addSelectedChangeListener { event -> showTab(event.selectedTab) }
		refreshBenchmark()
		refreshTabs()
		trafficChartPanel.reload()
		opportunitiesPanel.reload()
	}

	private fun buildHeader(): HorizontalLayout {
		val title = H2("Arrive Competitor Intelligence")
		val addBtn = Button("Add competitor") { openAddDialog() }.apply {
			addThemeVariants(ButtonVariant.LUMO_PRIMARY)
		}
		val refreshBtn = Button("Refresh all") { confirmRefreshAll() }
		return HorizontalLayout(title, addBtn, refreshBtn).apply {
			setWidthFull()
			isPadding = true
			isSpacing = true
			defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
			expand(title)
		}
	}

	private fun buildBody(): VerticalLayout {
		configureBenchmarkGrid()
		return VerticalLayout(trafficChartPanel, benchmarkGrid, opportunitiesPanel, tabs, tabContent).apply {
			setSizeFull()
			setPadding(true)
			setSpacing(true)
			// AppLayout's content slot doesn't scroll on its own; let this layout scroll its
			// children instead of clipping them when they exceed viewport height.
			style.set("overflow", "auto")
		}
	}

	private fun configureBenchmarkGrid() {
		benchmarkGrid.addColumn { row ->
			if (row.competitor.isOwn) "★ ${row.competitor.name}" else row.competitor.name
		}.setHeader("Name")
		benchmarkGrid.addColumn { it.competitor.domain }.setHeader("Domain")
		benchmarkGrid.addColumn { it.metrics?.organicKeywords ?: "—" }.setHeader("Organic keywords")
		benchmarkGrid.addColumn { it.metrics?.organicTraffic ?: "—" }.setHeader("Organic traffic")
		benchmarkGrid.addColumn { it.metrics?.organicCost?.toPlainString() ?: "—" }.setHeader("Organic cost")
		benchmarkGrid.addColumn { it.metrics?.top10Keywords ?: "—" }.setHeader("Top-10 keywords")
		benchmarkGrid.addColumn { row ->
			row.fetchedAt?.let { timestampFormatter.format(it) } ?: "never"
		}.setHeader("Fetched at")
		benchmarkGrid.setPartNameGenerator { row -> if (row.competitor.isOwn) "own-row" else null }
		benchmarkGrid.setHeight("280px")
		benchmarkGrid.setWidthFull()
	}

	private fun refreshBenchmark() {
		val response: BenchmarkResponse = benchmarkService.benchmark()
		val rows = buildList {
			response.own?.let { add(it) }
			addAll(response.competitors)
		}
		benchmarkGrid.setItems(rows)
	}

	private fun refreshTabs() {
		val previouslySelected = tabs.selectedTab?.label
		tabs.removeAll()
		tabPanels.clear()
		tabsByCompetitor.clear()
		tabContent.removeAll()

		val competitors = competitorService.list().filterNot { it.isOwn }
		if (competitors.isEmpty()) {
			tabContent.add(Span("Add a competitor to see keyword gaps."))
			return
		}

		var initial: Tab? = null
		competitors.forEach { competitor ->
			val tab = Tab(competitor.name)
			tabPanels[tab] = KeywordGapPanel(competitor, benchmarkService)
			competitor.id?.let { tabsByCompetitor[it] = tab }
			tabs.add(tab)
			if (initial == null || competitor.name == previouslySelected) initial = tab
		}

		initial?.let {
			tabs.selectedTab = it
			showTab(it)
		}
	}

	private fun showTab(tab: Tab?) {
		tabContent.removeAll()
		val panel = tabPanels[tab] ?: return
		tabContent.add(panel)
	}

	private fun openAddDialog() {
		AddCompetitorDialog { request ->
			try {
				competitorService.create(request)
				Notification.show("Added ${request.name}")
				refreshBenchmark()
				refreshTabs()
				trafficChartPanel.reload()
				opportunitiesPanel.reload()
			} catch (ex: Exception) {
				Notification.show("Couldn't add competitor: ${ex.message ?: ex.javaClass.simpleName}")
			}
		}.open()
	}

	private fun confirmRefreshAll() {
		val competitorCount = competitorService.list().size
		if (competitorCount == 0) {
			Notification.show("Nothing to refresh yet — add a competitor first.")
			return
		}
		// Phase 2 cost: gap rows (40 c × limit × 2 directions) + domain ranks (10) + monthly history (10 × months).
		val estimate = competitorCount * (10 + 2 * gapRowLimit * 40 + historyMonths * 10)
		val dialog = ConfirmDialog().apply {
			setHeader("Refresh all from SEMRush?")
			setText(
				"This will refresh $competitorCount competitor(s), including $historyMonths months of traffic history. " +
					"Estimated cost: ~$estimate credits. " +
					"Live calls run only when SEMRUSH_LIVE=true; otherwise the fake client serves the data.",
			)
			setCancelable(true)
			setConfirmText("Refresh")
			setConfirmButtonTheme("primary")
			addConfirmListener { runRefreshAll() }
		}
		dialog.open()
	}

	private fun runRefreshAll() {
		try {
			refreshService.refreshAll()
			Notification.show("Refresh complete")
			refreshBenchmark()
			refreshTabs()
			trafficChartPanel.reload()
			opportunitiesPanel.reload()
		} catch (ex: Exception) {
			Notification.show("Refresh failed: ${ex.message ?: ex.javaClass.simpleName}")
		}
	}
}
