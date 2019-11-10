if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
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

function animationOff() {
    setWebSocketConnected(true, false);
}

function animationOn() {
    setWebSocketConnected(true, true);
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
    //get_messages();

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
    "/api/v1/network/me",
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
    "/api/v1/network/peers",
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
