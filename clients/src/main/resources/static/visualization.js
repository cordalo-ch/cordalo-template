/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

