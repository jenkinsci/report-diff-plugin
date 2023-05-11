        // <![CDATA[
        var data = document.getElementById('report-publisher-id-data').textContent.trim().split(/\s+/)
        for (let i = 0; i < data.length; i++) {
          var lidd = data[i];
          if ((lidd?.trim()?.length || 0) > 0) {
            document.getElementById(lidd).onclick = function(evt){
              diffPluginShowHideBlockOne("collapsable-" + this.id)
            };
          }
        }
        // ]]>
