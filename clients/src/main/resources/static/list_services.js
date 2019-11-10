if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
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



function onServiceSelectionChanged(select) {
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
                    data: "service-provider=" + encodeURI(this.get_random_peer())
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
                title: "Partners", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var x500_O = participantsWithoutMe(item.state.participantsX500).map(x => X500toO(x));
                    return strongS(i) + x500_O.join(",") + strongE(i);
                }
            },
            {
                title: "State", name: "state.state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + "<br>" + makeOptions(item.state.id.id, item.links, "Action", "onServiceSelectionChanged") + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.id",
                type: "text",
                align: "center",
                width: 25,
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

function get_services() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/services"),
        data: {},
        success: function (result) {
            show_services("#services-template", result);
        }
    });
}


