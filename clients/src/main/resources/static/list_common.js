if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}


function mapX500(x500) {
    x500_map = {
        "O=Notary,L=Bern,ST=BE,C=CH" : "O=Notary, L=Bern, ST=BE,C=CH",
        "O=Company-A,L=Zurich,ST=ZH,C=CH" : "O=Retail, L=Zurich, ST=ZH,C=CH",
        "O=Company-B,L=Winterthur,ST=ZH,C=CH" : "O=Leasing, L=Winterthur, ST=ZH,C=CH",
        "O=Company-C,L=Zug,ST=ZG,C=CH" : "O=Regulator, L=Zug, ST=ZG,C=CH",
        "O=Company-D,L=Geneva,ST=ZH,C=CH" : "O=Insurance-A, L=Geneva, ST=ZH,C=CH",
        "O=Company-E,L=Uster,ST=ZH,C=CH" : "O=Insurance-M, L=Uster,S T=ZH,C=CH"
    }
    return x500_map[x500];
}

function get_me(title_selector = "#party_me", image_selector = "#image_me") {
    $get({
        url: cordaloEnv.API_URL("/api/v1/network/me"),
        data: {},
        success: function (result) {
            cordaloEnv.setME("X500", result.me.x500Principal.name);

            var x500name_image = result.me.x500Principal.name.split(",");
            var Organization_image = x500name_image[0].split("=")[1];
            var imageName = Organization_image.trim().replace(/[ ]/g, '_').replace(/[,\.]/g, '').toLowerCase();
            $(image_selector).html("<img style=\"width:60px\" src=\"images/node_" + imageName + ".jpeg\"/>");

            /* map X500 names to display different names */
            var x500name = mapX500(result.me.x500Principal.name).split(",");
            var Organization = x500name[0].split("=")[1];
            var Locality = x500name[1].split("=")[1];
            var Country = x500name[2].split("=")[1];
            $(title_selector).html(Organization + ", " + Locality + " (" + Country + ")");
            cordaloEnv.setME("name", Organization);
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
        return peers[getRandomInt(peers.length)].x500Principal.name;
    }
    return "";
}