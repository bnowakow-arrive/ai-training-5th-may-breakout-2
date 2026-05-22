package com.arrive.ai_training_5th_may_breakout_2.ui

// Service contract assumed by this view (P3 must match these signatures):
//   CompetitorService.list(): List<CompetitorDto>
//   CompetitorService.create(req: CreateCompetitorRequest): CompetitorDto
//   RefreshService.refreshAll()
//   BenchmarkService.benchmark(): BenchmarkResponse
//   BenchmarkService.keywordGap(competitorId: Long, gapType: GapType): List<KeywordGapRowDto>

import com.arrive.ai_training_5th_may_breakout_2.contracts.BenchmarkResponse
import com.arrive.ai_training_5th_may_breakout_2.contracts.MetricRow
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import com.arrive.ai_training_5th_may_breakout_2.service.CompetitorService
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
	@Value("\${semrush.gap-row-limit:25}") private val gapRowLimit: Int,
) : AppLayout() {

	private val benchmarkGrid = Grid<MetricRow>(MetricRow::class.java, false)
	private val tabs = Tabs()
	private val tabContent = VerticalLayout().apply {
		setPadding(false)
		setSpacing(false)
		setWidthFull()
	}
	private val tabPanels = mutableMapOf<Tab, KeywordGapPanel>()
	private val timestampFormatter: DateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

	init {
		addToNavbar(buildHeader())
		setContent(buildBody())
		tabs.addSelectedChangeListener { event -> showTab(event.selectedTab) }
		refreshBenchmark()
		refreshTabs()
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
		return VerticalLayout(benchmarkGrid, tabs, tabContent).apply {
			setSizeFull()
			setPadding(true)
			setSpacing(true)
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
		tabContent.removeAll()

		val competitors = competitorService.list().filterNot { it.isOwn }
		if (competitors.isEmpty()) {
			tabContent.add(Span("Add a competitor to see keyword gaps."))
			return
		}

		var initial: Tab? = null
		competitors.forEach { competitor ->
			val tab = Tab(competitor.name)
			tabs.add(tab)
			tabPanels[tab] = KeywordGapPanel(competitor, benchmarkService)
			if (initial == null || competitor.name == previouslySelected) initial = tab
		}

		initial?.let { tabs.selectedTab = it }
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
		val estimate = competitorCount * (10 + 2 * gapRowLimit * 40)
		val dialog = ConfirmDialog().apply {
			setHeader("Refresh all from SEMRush?")
			setText(
				"This will refresh $competitorCount competitor(s). " +
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
		} catch (ex: Exception) {
			Notification.show("Refresh failed: ${ex.message ?: ex.javaClass.simpleName}")
		}
	}
}
