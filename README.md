# cordalo-template

A simple way to run and vizualize multiple node in your browser.

# Running the demo

## Windows 

* Install GIT for windows and use GIT Bash or Cygwin
* Clone this project and run ./cleanAll.sh from script folder
```
git clone https://github.com/cordalo-ch/cordalo-template.git
cd script
./cleanAll.sh
```

## Linux / MacOsx
```
git clone https://github.com/cordalo-ch/cordalo-template.git
cd script
./cleanAll.sh
```

Be patient! I can take upto some minutes due to issues in the official CORDA starting procedures of nodes running in parallel. The scripts are re-trying until all is up and running.
The webservers are started after the nodes. If everything is done, the scripts stops and shows the following
```
---------------------------------------
CORDA and Webservers are UP and running
---------------------------------------
Currently 6 CORDA nodes running
Currently 5 Webservers  running
```

## Scripts
| Name | Description |
| ------------- | ------------- |
| cleanAll.sh  | stop nodes and web, git pull newest code and start again |
| cleanServers.sh  | stops webservers, git pull and restart web only. Nodes still started |
| stopForceAll.sh  | stops all nodes and web, killing all |
| stopServers.sh  | stop web servers |
| startAll.sh  | starts all node, web without cleaning, keep state of all nodes |
| startServers.sh  | starts web only |
| startNodes.sh  | starts nodes only |
| checkStates.sh | displays the status of nodes and web |
| tailServers.sh | tail all web server log files |
| tailNodes.sh | tail all log files of all nodes |


When all Node and webserver are up, 
you can visit http://localhost:10801/?frames=10801+10802+10803,10804,10805

## Warning
The warning from CordaApp still apply (https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp)

**On Unix/Mac OSX, do not click/change focus until all seven additional terminal windows have opened, or some nodes may fail to start.**

# Demo

## Configuration for the nodes

Configuration settings take place in build.gradle file and for the demo we start 5 Corda nodes and a Notary.

| Name | RPC | SSH | P2P | Admin | Webserver |
| ------------- | ------------- | ------------- | ------------- | ------------- | -------------- | 
| `O=Notary,L=Bern,ST=BE,C=CH`           | 10003 | 10103 | 10002 | 10043 | none
| `O=Company-A,L=Zurich,ST=ZH,C=CH`      | 10006 | 10106 | 10005 | 10046 | (http://localhost:10801)
| `O=Company-B,L=Winterthur,ST=ZH,C=CH`  | 10009 | 10109 | 10008 | 10049 | (http://localhost:10802)
| `O=Company-C,L=Zug,ST=ZG,C=CH`         | 10012 | 10112 | 10011 | 10052 | (http://localhost:10803)
| `O=Company-D,L=Geneva,ST=ZH,C=CH`      | 10015 | 10115 | 10014 | 10055 | (http://localhost:10804)
| `O=Company-E,L=Uster,ST=ZH,C=CH`       | 10018 | 10118 | 10017 | 10058 | (http://localhost:10805)

## Default preconditions in scripts
- RPC servers starts with 10006, increment by 3
- Admin servers starts with 10046, increment by 3
- ssh servers starts with 
- web servers


Each above node is able to start the following flow
* Buying some products (P) flow with different costs attached to it (25, 99, 34)
* Trigger some support (S) flow  with different costs attached to it (9, 10, 45)
* Trigger some Alarm Services (A) flow  with different costs attached to it (53, 87)


Example http://localhost:10801/?frames=10801+10802,10803+10804,10805
 ![foo bar](documentation/img/gui.png  "GUI"   )
 
