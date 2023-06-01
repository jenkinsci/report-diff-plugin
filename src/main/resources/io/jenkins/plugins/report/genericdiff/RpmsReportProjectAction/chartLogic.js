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
          labels: data_labels,
          datasets: [
          {
            label: titles[0],
            fillColor: "rgba(128,255,128,0.2)",
            strokeColor: "rgba(128,255,128,1)",
            pointColor: "rgba(128,255,128,1)",
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: "rgba(128,255,128,1)",
            data: data_installed
          },
          {
            label: titles[1],
            fillColor: "rgba(255,128,128,0.2)",
            strokeColor: "rgba(255,128,128,1)",
            pointColor: "rgba(255,128,128,1)",
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: "rgba(255,128,128,1)",
            data: data_removed
          },
          {
            label: titles[2],
            fillColor: "rgba(128,128,128,0.2)",
            strokeColor: "rgba(128,128,128,1)",
            pointColor: "rgba(128,128,128,1)",
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: "rgba(128,128,128,1)",
            data: data_total
          }
        ]
        };
          var options = {
            bezierCurve: false,
             multiTooltipTemplate: "<%= datasetLabel + \": \" + value %>"
             };
        // Get the context of the canvas element we want to select
        var ctx = document.getElementById("rpmsChart-"+id).getContext("2d");
        diffChartClick["rpmsChart-"+id] = new Chart(ctx).Line(allRpms, options);
        document.getElementById("rpmsChartContainer-" + id).onclick = function (evt) {
          var lid = event.target.id;
          var jid = lid.replace("rpmsChart-", "")
          var activePoints = diffChartClick["rpmsChart-"+jid].getPointsAtEvent(evt);
          var point = activePoints[0];
          var result = point.label;
          var buildId = result.substring(result.lastIndexOf(":") + 1)
          window.open("" + buildId + "/rpms#"+jid, "_blank");
        };
}
        // ]]>