import * as echarts from 'echarts';

function renderLineChart(container, seriesJson) {
  const series = typeof seriesJson === 'string' ? JSON.parse(seriesJson) : seriesJson;

  let chart = echarts.getInstanceByDom(container);
  if (!chart) {
    chart = echarts.init(container, null, { renderer: 'canvas' });
  }

  const months = [...new Set(series.flatMap(s => s.points.map(p => p.month)))].sort();

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' },
      confine: true,
    },
    legend: { data: series.map(s => s.name), top: 4 },
    grid: { left: 70, right: 30, top: 40, bottom: 40, containLabel: true },
    xAxis: { type: 'category', data: months, boundaryGap: false },
    yAxis: { type: 'value', name: 'Organic traffic' },
    series: series.map(s => ({
      name: s.name,
      type: 'line',
      smooth: true,
      symbol: s.isOwn ? 'circle' : 'emptyCircle',
      symbolSize: s.isOwn ? 8 : 5,
      lineStyle: { width: s.isOwn ? 4 : 2 },
      itemStyle: s.isOwn ? { color: '#1565c0' } : undefined,
      z: s.isOwn ? 10 : 5,
      connectNulls: true,
      data: months.map(m => {
        const pt = s.points.find(p => p.month === m);
        return pt ? pt.organicTraffic : null;
      }),
    })),
  }, true);

  // Force a layout pass so the canvas matches the now-sized container — fixes the case where
  // echarts initialised before Vaadin gave the container its real width/height (breaks hover).
  requestAnimationFrame(() => chart.resize());

  if (!chart.__resizeHooked) {
    const resize = () => chart.resize();
    window.addEventListener('resize', resize);
    if (typeof ResizeObserver !== 'undefined') {
      new ResizeObserver(resize).observe(container);
    }
    chart.__resizeHooked = true;
  }
}

window.echartsLineChart = { renderLineChart };
