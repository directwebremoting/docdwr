
/*
(function() {
    var menuId = 'nav';

    var init = function() {
        var link;
        var subMenus = document.getElementById(menuId).getElementsByTagName('ul');
        for (var j = 0; j < subMenus.length; j++) {
            link = subMenus[j].parentNode.getElementsByTagName('a')[0];
            link.onclick = function(ev) {
                var parentUl = this.parentNode.getElementsByTagName('ul')[0];
                if (parentUl.style.display == "block") parentUl.style.display = "none";
                else parentUl.style.display = "block";
                return false;
            };
            subMenus[j].style.display = "none";
        }
    };

    if (window.addEventListener) {
        window.addEventListener('load', init, false);
    }
    else if (window.attachEvent) {
        window.attachEvent("onload", init);
    }
    else {
        init();
    }
})();
*/

dojo.addOnLoad(function() {
    // Hide all the lists in the nav menu and add open/close toggle onclick
    dojo.query("#nav ul").forEach(function(subMenu) {
        dojo.addClass(subMenu, "hidden");
        subMenu.parentNode.getElementsByTagName('a')[0].onclick = function(ev) {
            dojo.toggleClass(this.parentNode.getElementsByTagName('ul')[0], "hidden");
            return false;
        };
    });
    // Open all the menus that are parents of the current node
    dojo.query(".currentLink").forEach(function(node) {
        while (node != null) {
            dojo.removeClass(node, "hidden");
            node = node.parentNode;
        }
    });
});
