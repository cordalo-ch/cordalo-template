if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}


function get_me(title_selector = "#party_me", image_selector = "#image_me") {
    $get({
        url: cordaloEnv.API_URL("/api/v1/network/me"),
        data: {},
        success: function (result) {
            cordaloEnv.setME("X500", result.me.x500Principal.name);
            var x500name = result.me.x500Principal.name.split(",");
            var O = x500name[0].split("=")[1];
            var L = x500name[1].split("=")[1];
            var C = x500name[2].split("=")[1];
            var imageName = O.trim().replace(/[ ]/g, '_').replace(/[,\.]/g, '').toLowerCase();
            $(title_selector).html(O + ", " + L + " (" + C + ")");
            $(image_selector).html("<img style=\"width:80px\" src=\"images/node_" + imageName + ".jpeg\"/>");
            cordaloEnv.setME("name", O);
        }
    }).fail(function (e) {
        $(title_selector).html(e.statusText);
    });
}

function get_peers() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/network/peers"),
        data: {},
        success: function (result) {
            cordaloEnv.setME("peers", result.peers);
        }
    });
}

function get_random_peer() {
    var peers = cordaloEnv.ME("peers");
    if (peers) {
        return peers[getRandomInt[peers.length)].x500Principal.name;
    }
    return "";
}