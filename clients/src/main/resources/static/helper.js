if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}

function strongS(i) {
    return (i < 10 ? "<strong>" : "");
}

function strongE(i) {
    return (i < 10 ? "</strong><br>" : "");
}

function makeOptions(id, list, actionText = "Action", onSelectionMethod = "onSelectionChanged") {
    var keys = Object.keys(list);
    if (keys.length > 1) {
        var s = "<select id='"+id+"' onChange='"+onSelectionChanged+"(this)'><option>"+actionText+"</option>";
        Object.entries(list).forEach(([key, value]) =>
            s = s + (key === "self" ? "" : "<br><option value=\""+value+"\">"+key+"</option>"));
        s = s + "</select>";
        return s;
    }
    return "";
}

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


function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

function participantsWithoutMe(list) {
    return list.filter(x => x != cordaloEnv.ME("X500"));
}
