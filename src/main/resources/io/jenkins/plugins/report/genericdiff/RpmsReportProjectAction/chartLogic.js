        // <![CDATA[
var rpmcharts_ids = document.getElementById('rpmsChartContainer-ids').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
for (let i = 0; i < rpmcharts_ids.length; i++) {
        var id = rpmcharts_ids[i]
        var data_labels = document.getElementById('rpmsChartContainer-labels-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_installed = document.getElementById('rpmsChartContainer-installed-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_removed = document.getElementById('rpmsChartContainer-removed-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_total = document.getElementById('rpmsChartContainer-total-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var titles = document.getElementById('rpmsChartContainer-titles-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var allRpms = {
          type: 'line',
          data:   {
          labels: data_labels,
          datasets: [
          {
            label: titles[0],
            fill: true,
            backgroundColor: "rgba(128,255,128,0.2)",
            borderColor: "rgba(128,255,128,1)",
            pointBackgroundColor: "rgba(128,255,128,1)",
            pointBorderColor: "#fff",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgba(128,255,128,1)",
            pointRadius: 5,
            data: data_installed
          },
          {
            label: titles[1],
            fill: true,
            backgroundColor: "rgba(255,128,128,0.2)",
            borderColor: "rgba(255,128,128,1)",
            pointBackgroundColor: "rgba(255,128,128,1)",
            pointBorderColor: "#fff",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgba(255,128,128,1)",
            pointRadius: 5,
            data: data_removed
          },
          {
            label: titles[2],
            fill: true,
            backgroundColor: "rgba(128,128,128,0.2)",
            borderColor: "rgba(128,128,128,1)",
            pointBackgroundColor: "rgba(128,128,128,1)",
            pointBorderColor: "#fff",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgba(128,128,128,1)",
            pointRadius: 5,
            data: data_total
          }
        ]
        },
        options: {
          plugins: {
            legend: { display: false }
          },
          interaction: {
            mode: 'index',
            intersect: false
          },
          onClick: (e) => {
            var lid = e.chart.canvas.id
            var jid = lid.replace("rpmsChart-", "")
            var activePoints = diffChartClick[lid].getElementsAtEventForMode(e, 'index', { intersect: false }, true);
            var point = activePoints[0]
            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
            var index = point.index
            var result = diffChartClick[lid].config.data.labels[index]
            var buildId = result.substring(result.lastIndexOf(":") + 1)
            window.open("" + buildId + "/rpms#"+jid, "_blank");
          }
        }
        };
        // Get the context of the canvas element we want to select
        var ctx = document.getElementById("rpmsChart-"+id).getContext("2d");
        diffChartClick["rpmsChart-"+id] = new Chart(ctx, allRpms);
}
        // ]]>