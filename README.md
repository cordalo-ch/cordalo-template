# cordalo-template
(based on java)

A simple way to run and vizualize multiple Corda node in your browser in 1 view. 

We show in this example 
- how to write simple contracts to validate commands (using a builder notation)
- how to write simple Flows and no need to handle all the sync, collect, finalize functionality
- how to write easy and functional testcases for contracts and flow - with or without spinning of Mocknodes
- how to write a simple Statemachine to execute, valide actions and states.
- how to simply share an object with participants
- how to use vaultTracker and websockets to bring Vault changes automatically to the frontend
- how to visualize multiple nodes in a demo environment to see interaction between nodes

Example snapshot
 ![foo bar](documentation/img/gui.png  "GUI"   )


# Running the demo

## Docker (recommended)
to get access to 6 nodes, 6 webserver and 5 remote debugging node port
(windows user should use Git Bash)
```
docker run -t -d \
-p 10801:10801 \
-p 10802:10802 \
-p 10803:10803 \
-p 10804:10804 \
-p 10805:10805 \
-p 10006:10006 \
-p 10009:10009 \
-p 10012:10012 \
-p 10015:10015 \
-p 10018:10018 \
-p 5005:5005 \
-p 5006:5006 \
-p 5007:5007 \
-p 5008:5008 \
-p 5009:5009 \
cordalo-template:latest
```
Then point your browser to http://localhost:10801/?frames=10801+10802+10803,10804,10805

**Attention**
* if you are using docker desktop, dont forget to increase memory and number of cpu in docker setting to something like 16gb
* the -t option allocates a "pseudo-tty". This tricks bash into continuing to run indefinitely because it thinks it is 
  connected to an interactive TTY (even though you have no way to interact with that particular TTY if you don't pass -i).
* replace latest with any version available at https://hub.docker.com/repository/docker/cordalo/cordalo-template


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

---------------------------------------
CORDA and Webservers are UP and running
---------------------------------------
Currently 6 CORDA nodes running
Currently 5 Webservers  running
```

## Scripts
| Name | Description | Special note |
| ------------- | ------------- | ------------- |
| cleanAll.sh  | stop nodes and web, git pull newest code and start again | all CORDA data lost |
| cleanServers.sh  | stops webservers, git pull and restart web only. Nodes still running | Start only webservers |
| stopForceAll.sh  | stops all nodes and web, killing all | kill all processes |
| stopServers.sh  | stop web servers |
| startAll.sh  | starts all node, web without cleaning, keep state of all nodes | start with existing data |
| startServers.sh  | starts web only |
| startNodes.sh  | starts nodes only |
| checkStates.sh | displays the status of nodes and web |
| tailServers.sh | tail all web server log files |
| tailNodes.sh | tail all log files of all nodes |
```
## UI

When all Node and webserver are up, you can visit 
http://localhost:10801/?frames=10801+10802+10803,10804,10805

### Display & develop single frame

http://localhost:10801/frame.html
the following paramters can control the API and ports.
Use these parameters to debug locally (from IntelliJ or any other test webserver) to use the API from another location

| Parameter | Description | example |
| ------------- | ------------- | ------------- |
| port | port used for backend web API | port=10801 |
| local | using "true" for "localhost" or any other domain name or IP | local=true, local=123.456.0.789 |
| mock | use MOCK data within .js only instead backend | mock=true |

### Display all frames

http://localhost:10801/index.html
use parameters "frames" to control the different ports with the columns or rows

| Parameter | Description | example |
| ------------- | ------------- | ------------- |
| , | use , to seperate columns | frames=10801,10802,10803,10804,10805 - 5 columns |
| + | use + to seperate row | frames=10801+10802,10803,10804+10805 - 3 columns, 1st and last have 2 rows |


## Warning
The warning from CordaApp still apply (https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp)

**On Unix/Mac OSX, do not click/change focus until all seven additional terminal windows have opened, or some nodes may fail to start.**


# Configuration

## Configuration for the nodes

Configuration settings take place in build.gradle file and for the demo we start 5 Corda nodes and a Notary.

| Name | RPC | SSH | P2P | Admin | Webserver |
| ------------- | ------------- | ------------- | ------------- | ------------- | -------------- | 
| `O=Notary,L=Bern,ST=BE,C=CH`           | 10003 | 10103 | 10002 | 10043 | none
| `O=Company-A,L=Zurich,ST=ZH,C=CH`      | 10006 | 10106 | 10005 | 10046 | http://localhost:10801
| `O=Company-B,L=Winterthur,ST=ZH,C=CH`  | 10009 | 10109 | 10008 | 10049 | http://localhost:10802
| `O=Company-C,L=Zug,ST=ZG,C=CH`         | 10012 | 10112 | 10011 | 10052 | http://localhost:10803
| `O=Company-D,L=Geneva,ST=ZH,C=CH`      | 10015 | 10115 | 10014 | 10055 | http://localhost:10804
| `O=Company-E,L=Uster,ST=ZH,C=CH`       | 10018 | 10118 | 10017 | 10058 | http://localhost:10805

## Port configurations
- RPC   servers starts with 10003, increment by 3
- SSH   servers starts with 10103, increment by 3
- P2P   servers starts with 10002, increment by 3
- Admin servers starts with 10043, increment by 3 (not needed in the future by corda)
- Web   servers starts with 10801, increment by 1


# Demo

We want to 

Each above node is able to start the following flow
* Buying some products (P) flow with different costs attached to it (25, 99, 34)
* Trigger some support (S) flow  with different costs attached to it (9, 10, 45)
* Trigger some Alarm Services (A) flow  with different costs attached to it (53, 87)

# Docker

Building new image, from root of this directory

```
docker system prune -a
docker build -f Dockerfile . -t cordalo-template
```
to push image
```
docker login --username=cordalo
# password in keepass
docker tag cordalo-template cordalo/cordalo-template:latest
docker push cordalo/cordalo-template:latest
```
new version now available at https://hub.docker.com/repository/docker/cordalo/cordalo-template