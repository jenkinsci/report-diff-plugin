        // <![CDATA[
var rpmcharts_ids = document.getElementById('rpmsChartContainer-ids').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
if (rpmcharts_ids != null) {
    for (let i = 0; i < rpmcharts_ids.length; i++) {
        var id = rpmcharts_ids[i]
        if (id == null) {
            continue
        }
        var data_labels_element = document.getElementById('rpmsChartContainer-labels-'+id)
        if (data_labels_element == null) {
            continue
        } else {
            var data_labels = data_labels_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_installed_element = document.getElementById('rpmsChartContainer-installed-'+id)
        if (data_installed_element == null) {
            continue
        } else {
            var data_installed = data_installed_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_removed_element = document.getElementById('rpmsChartContainer-removed-'+id)
        if (data_removed_element == null) {
            continue
        } else {
            var data_removed = data_removed_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var data_total_element = document.getElementById('rpmsChartContainer-total-'+id)
        if (data_total_element == null) {
            continue
        } else {
            var data_total = data_total_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
        var titles_element = document.getElementById('rpmsChartContainer-titles-'+id)
        if (titles_element == null) {
            continue
        } else {
            var titles = titles_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }
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
}
        // ]]>
