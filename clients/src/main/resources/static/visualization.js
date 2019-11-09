if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}

function animationOff() {
    setWebSocketConnected(true, false);
}

function animationOn() {
    setWebSocketConnected(true, true);
}

function createNewService(self, service, spShort, price) {
    animationOn();
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/services/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "service-name=" + encodeURI(service) + "&data=" + encodeURI(getServiceData()) + "&price=" + encodeURI(price)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        alert(jqXHR.responseText);
    });
}

function deleteService(service) {
    var id = $(service).attr("value");
    if (confirm('Are you sure to delete service ' + id + '?')) {
        animationOn();
        $.ajax(
            {
                url: cordaloEnv.API_URL("/api/v1/cordalo/template/services/" + id),
                method: "DELETE",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: ""
            }
        ).done(function (result) {
            get_services();
            animationOff();
        }).fail(function (jqXHR, textStatus) {
            alert(jqXHR.responseText);
        });
    }
}

function getServiceData() {
    return "{\"test\": \"42\"}";
}

function strongS(i) {
    return (i < 10 ? "<strong>" : "");
}

function strongE(i) {
    return (i < 10 ? "</strong><br>" : "");
}

function makeOptions(id, list) {
    var keys = Object.keys(list);
    if (keys.length > 1) {
        var s = "<select id='"+id+"' onChange='onSelectionChanged(this)'><option>Action</option>";

        Object.entries(list).forEach(([key, value]) =>
            s = s + (key === "self" ? "" : "<br><option value=\""+value+"\">"+key+"</option>"));

        s = s + "</select>";
        return s;
    }
    return "";
}

function onSelectionChanged(select) {
    if ($(select).val() !== '') {
        var url = $(select).val();
        var action = url.split("/").reverse()[0];
        if (action !== "SHARE") {
            animationOn();
            $.ajax(
                {
                    url: url,
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: ""
                }
            ).done(function (result) {

            }).fail(function (jqXHR, textStatus) {
                alert(jqXHR.responseText);
            });
        } else {
            animationOn();
            $.ajax(
                {
                    url: url,
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: "service-provider=" + encodeURI(ME_RANDOM_PEER)
                }
            ).done(function (result) {

            }).fail(function (jqXHR, textStatus) {
                alert(jqXHR.responseText);
            });
        }
    }
};


function show_services(tagName, result) {
    var i = 0;
    $(tagName).jsGrid({
        height: "auto",
        width: "100%",

        sorting: true,
        paging: false,
        selecting: false,
        filtering: false,
        autoload: true,

        data: result.reverse(),

        fields: [

            /*
              */
            {
                title: "Service", name: "state.serviceName", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + item.state.serviceName + "<br>" + price(item.state.price) + strongE(i);
                }
            },
            {
                title: "Provider", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + X500toO(item.state.serviceProviderX500) + strongE(i);
                }
            },
            {
                title: "State", name: "state.state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + "<br>" + makeOptions(item.state.id.id, item.links) + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.id",
                type: "text",
                align: "center",
                width: 30,
                itemTemplate: function (value) {
                    var res = "<a target='_blank' href='" + cordaloEnv.API_URL("/api/v1/cordalo/template/services/" + value.id) + "'>o</a>&nbsp;"
                        + "<a value=" + value.id + " href=\"#\" onClick=\"deleteService(this)\"'>X</a>";
                    i = i + 10;
                    return strongS(i) + res + strongE(i);
                }
            }
        ]
    });
}


/**
 * @return {string}
 */
function X500toOL(x500) {
    if (x500 == null || x500 === "") return "";
    var DNs = x500.split(/[,=]/);
    return DNs[1] + ", " + DNs[3]
}

function X500toO(x500) {
    if (x500 == null || x500 === "") return "";
    var DNs = x500.split(/[,=]/);
    return DNs[1];
}

function price(price) {
    if (price == null) return "";
    return "CHF " + price;
}


var ME = "";
var ME_RANDOM_PEER = "";

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

function get_me() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/me"),
        data: {},
        success: function (result) {
            var x500name = result.me.x500Principal.name.split(",");
            var O = x500name[0].split("=")[1];
            var L = x500name[1].split("=")[1];
            var C = x500name[2].split("=")[1];
            var imageName = O.trim().replace(/[ ]/g, '_').replace(/[,\.]/g, '').toLowerCase();
            $("#party_me").html(O + ", " + L + " (" + C + ")");
            $("#image_me").html("<img style=\"width:80px\" src=\"images/node_" + imageName + ".jpeg\"/>");
            ME = O;
        }
    }).fail(function (e) {
        $("#party_me").html(e.statusText);
    });
}

function get_peers() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/peers"),
        data: {},
        success: function (result) {
            ME_RANDOM_PEER = result.peers[getRandomInt(result.peers.length)].x500Principal.name;
        }
    });
}

function get_services() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/services"),
        data: {},
        success: function (result) {
            show_services("#services-template", result);
        }
    });
}

function setWebSocketConnected(connected, running) {
    if (connected && running) {
        $("#image-socket").html("<img id='image-socket-ball' src='images/green.gif'>")
    } else if (connected) {
        $("#image-socket").html("<img id='image-socket-ball' src='images/green.png'>")
    } else {
        $("#image-socket").html("<img id='image-socket-ball' src='images/red.png'>")
    }
}


function connectWebSocket() {
    var socket = new SockJS(cordaloEnv.API_URL("/gs-guide-websocket"));
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function (frame) {
        setWebSocketConnected(true, false);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/vaultChanged/*', function (changes) {
            get_services();
            animationOff();
        });
    });
}


$(document).ready(function () {
    setWebSocketConnected(false);
    get_me();
    get_peers();
    get_services();

    connectWebSocket();

});


cordaloEnv.addMock(
    "/api/v1/cordalo/template/services",
    [{
        "state": {
            "id": {"externalId": null, "id": "3a3fe715-ef9e-4683-a932-7f67b2afbac0"},
            "state": "SHARED",
            "serviceName": "Tele Medicine",
            "serviceData": {"test": "42"},
            "price": 25,
            "initiatorX500": "O=Company-B,L=Winterthur,ST=ZH,C=CH",
            "serviceProviderX500": "O=Company-D,L=Zurich,ST=ZH,C=CH",
            "linearId": {"externalId": null, "id": "3a3fe715-ef9e-4683-a932-7f67b2afbac0"}
        },
        "links": {
            "UPDATE": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/UPDATE",
            "WITHDRAW": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/WITHDRAW",
            "SEND_PAYMENT": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/SEND_PAYMENT",
            "ACCEPT": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/ACCEPT",
            "DECLINE": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/DECLINE",
            "self": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0"
        },
        "error": null
    }, {
        "state": {
            "id": {"externalId": null, "id": "fc6f8a5f-dee3-48ad-a245-594e1b631cc9"},
            "state": "SHARED",
            "serviceName": "New Service",
            "serviceData": {},
            "price": 34,
            "initiatorX500": "O=Company-A,L=Zurich,ST=ZH,C=CH",
            "serviceProviderX500": "O=Company-D,L=Zurich,ST=ZH,C=CH",
            "linearId": {"externalId": null, "id": "fc6f8a5f-dee3-48ad-a245-594e1b631cc9"}
        },
        "links": {
            "UPDATE": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/UPDATE",
            "WITHDRAW": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/WITHDRAW",
            "SEND_PAYMENT": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/SEND_PAYMENT",
            "ACCEPT": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/ACCEPT",
            "DECLINE": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/DECLINE",
            "self": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9"
        },
        "error": null
    }]
);

cordaloEnv.addMock(
    "/api/v1/cordalo/template/me",
    {
        "me": {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-D",
            "locality": "Zurich",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-D,L=Zurich,ST=ZH,C=CH",
                "encoded": "ME4xCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDEPMA0GA1UEBwwGWnVyaWNoMSEwHwYDVQQKDBhTd2lzc2NhbnRvIFBlbnNpb25zIEx0ZC4="
            }
        }
    }
);
cordaloEnv.addMock(
    "/api/v1/cordalo/template/peers",
    {
        "peers": [{
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-A",
            "locality": "Zurich",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-A,L=Zurich,ST=ZH,C=CH",
                "encoded": "MEUxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDEPMA0GA1UEBwwGWnVyaWNoMRgwFgYDVQQKDA9Td2lzcyBMaWZlIEx0ZC4="
            }
        }, {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-B",
            "locality": "Winterthur",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-B,L=Winterthur,ST=ZH,C=CH",
                "encoded": "MEYxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDETMBEGA1UEBwwKV2ludGVydGh1cjEVMBMGA1UECgwMQVhBIExlYmVuIEFH"
            }
        }, {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-D",
            "locality": "Zug",
            "state": "ZG",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-C,L=Zug,ST=ZG,C=CH",
                "encoded": "MDYxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaRzEMMAoGA1UEBwwDWnVnMQwwCgYDVQQKDANGWkw="
            }
        }]
    }
);
