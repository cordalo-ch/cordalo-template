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
            animationOff();
            if (changes.headers.destination == "/topic/vaultChanged/chatMessage") {
                get_messages();
            }
            if (changes.headers.destination == "/topic/vaultChanged/serviceState") {
                get_services();
            }
            if (changes.headers.destination == "/topic/vaultChanged/e178") {
                get_e178();
            }
        });
    });
}


$(document).ready(function () {
    setWebSocketConnected(false);
    get_me();
    get_peers();

    get_services();
    get_messages();

    connectWebSocket();
});

