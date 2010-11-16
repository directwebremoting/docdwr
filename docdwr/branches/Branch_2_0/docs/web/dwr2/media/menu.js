
dojo.addOnLoad(function() {
	// Do JS hover handling (due to IE inabilities)
	dojo.query("#nav").connect("onmouseenter", function() {
		dojo.query("#nav").addClass("hovered");
	});
	dojo.query("#nav").connect("onmouseleave", function() {
		dojo.query("#nav").removeClass("hovered");
	});
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
