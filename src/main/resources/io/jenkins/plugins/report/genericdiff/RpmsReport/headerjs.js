        //<![CDATA[
        function diffPluginShowHideInlineOne(id) {
        diffPluginShowHideOne(id, 'inline')
        }

        function diffPluginShowHideBlockOne(id) {
        diffPluginShowHideOne(id, 'block')
        }

        function diffPluginShowHideOne(id, type) {
        var el = document.getElementById(id);
        if (el.style.display=='none') {el.style.display = type} else {el.style.display = 'none'};
        }
        // ]]>