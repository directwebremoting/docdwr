
(function() {
    var menuId = 'nav';

    var init = function() {
        var link;
        var subMenus = document.getElementById(menuId).getElementsByTagName('ul');
        for (var j = 0; j < subMenus.length; j++) {
            link = subMenus[j].parentNode.getElementsByTagName('a')[0];
            link.onmouseover = mouseEnter(function(ev) {
                this.parentNode.getElementsByTagName('ul')[0].style.display = "block";
            });
            link.onmouseout = mouseEnter(function(ev) {
                this.parentNode.getElementsByTagName('ul')[0].style.display = "none";
            });
            subMenus[j].style.display = "none";
        }
    };

    var mouseEnter = function(action) {
        return function(ev) {
            if (!isAChildOf(this.parentNode, ev.relatedTarget)) {
                action.call(this, ev);
            }
        }
    };

    var isAChildOf = function(parent, child) {
        if (parent === child) return false;
        while (child && child !== parent) {
            child = child.parentNode;
        }
        return child === parent;
    }

    if (window.addEventListener) {
        window.addEventListener('load', init, false);
    }
    else if (window.attachEvent) {
        window.attachEvent("onload", init);
    }
init();
})();

