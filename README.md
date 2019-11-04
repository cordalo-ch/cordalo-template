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

Be patient!
When all Node and webserver are up, you can visit http://localhost:10801/?frames=10801+10802+10803,10804,10805

## Warning
The warning from CordaApp still apply (https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp)

**On Unix/Mac OSX, do not click/change focus until all seven additional terminal windows have opened, or some nodes may fail to start.**

# Understanding the demo

Per default the demo start 5 Corda nodes

| Name | RPC | Admin | Webserver |
| ------------- | ------------- | ------------- | ------------- | 
| `O=Company-A,L=Zurich,ST=ZH,C=CH`  | localhost:10006 | localhost:10046 | localhost:10801
| `O=Company-B,L=Winterthur,ST=ZH,C=CH`  | localhost:10009 | localhost:10049 | localhost:10802
| `O=Company-C,L=Zug,ST=ZG,C=CH`  | localhost:10012 | localhost:10052 | localhost:10803
| `O=Company-D,L=Geneva,ST=ZH,C=CH`  | localhost:10015 | localhost:10055 | localhost:10804
| `O=Company-E,L=Uster,ST=ZH,C=CH`  | localhost:10018 | localhost:10058 | localhost:10805

Each above node is able to start the following flow
* Buying some products (P) flow with different costs attached to it (25, 99, 34)
* Trigger some support (S) flow  with different costs attached to it (9, 10, 45)
* Trigger some Alarm Services (A) flow  with different costs attached to it (53, 87)

and one Corda notary

| Name | RPC | Admin | Webserver |
| ------------- | ------------- | ------------- | ------------- | 
| `O=Notary,L=Bern,ST=BE,C=CH`  | localhost:10003 | localhost:10043 | none

Example http://localhost:10801/?frames=10801+10802,10803+10804,10805
 ![foo bar](documentation/img/gui.png  "GUI"   )
 
