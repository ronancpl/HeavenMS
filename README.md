# HeavenMS
---

## Head developer: Ronan C. P. Lana

Besides myself for maintaining this repository, credits are to be given to Wizet/Nexon (owners of MapleStory & it's IP contents), the original MapleSolaxia staff and other colaborators, as just some changes/patches on the game were applied by myself, in which some of them diverged from the original v83 patch contents (alright, not just "some patches" by now since a whole lot of major server core changes have been applied on this development).

Regarding distributability and usage of the code presented here: like it was before, this MapleStory server is open-source. By that, it is meant that anyone is **free to install, use, modify and redistribute the contents**, as long as there is **no kind of commercial trading involved** and the **credits to the original creators are maintained** within the codes.

This is a NetBeans 8.0.2 Project, that MUST be built and run under JDK/JRE 7 in order to run properly. This means that it's easier to install the project via opening the server project folder inside NetBeans' IDE. Once installed, build this project on your machine and run the server using the "launch.bat" application.

In this project, many gameplay-wise issues generated from either the original WZ files and the server source have been partially or completely solved. Considering the use of the provided edited WZ's and server-side wz.xml files should be of the greatest importance when dealing with this instance of server source, in order to perceive it at it's full potential. My opinion, though! Refer to "README_wzchanges.txt" for more information on what has been changed from Nexon's v83 WZ files.

The main objective of this project is to try as best as possible to recreate what once was the original MapleStory v83, while adding up some flavors that spices up the gameplay. In other words, aim to get the best of the MapleStory of that era.

---
### Download items 

Server files: https://github.com/ronancpl/HeavenMS

Client files & general tools: https://drive.google.com/drive/folders/0BzDsHSr-0V4MYVJ0TWIxd05hYUk

**Important note about localhosts**: these executables are red-flagged by antivirus tools as __potentially malicious softwares__, this happens due to the reverse engineering methods that were applied onto these software artifacts. Those depicted here have been put to use for years already and posed no harm so far, so they are soundly assumed to be safe.

  Latest localhost: https://hostr.co/m2bVtnizCtmD

  The following list, in bottom-up chronological order, holds information regarding all changes that were applied from the starting localhost used in this development. Some lines have a link attached, that will lead you to a snapshot of the localhost at that version of the artifact. Naturally, later versions holds all previous changes along with the proposed changes.

**Change log:**

  * Removed block on applying attack-based strengthening gems on non-weapon equipments.

  * Set a higher cap for SPEED.

  * Removed the AP assigning block for beginners below level 10. https://hostr.co/AHAHzneCti9B

  * Removed block on party for beginners level 10 or below. https://hostr.co/JZq53mMtToCz

  * Removed block on MTS entering in some maps, rendering the buyback option available.

  * Removed "AP excess" popup and limited actions on Admin/MWLB, credits to kevintjuh93.

  * Removed "You've gained a level!" popup, credits to PrinceReborn.

  * Removed caps for WATK, WDEF, MDEF, ACC, AVOID.

  * 'n' problem fixed.

  * Fraysa's https://hostr.co/gJbLZITRVHmv

  * MapleSilver's starting on window-mode.

---
### Development information

Status: <span style="color:grey">__In development (4th round)__</span>.

#### Mission

With non-profitting means intended, provide nostalgic pre-BB MapleStory players world-wide a quality local server for freestyle entertainment.

#### Vision

By taking the v83 MapleStory as the angular stone, incrementally look forward to improve the gaming experience whilst still retaining the "clean v83" conservative ideal. Also, through reviewing distinguished aspects of the server's behavior that could be classified as a potential server threat, in the long run look for ways to improve or even stabilize some of it's uncertain aspects.

#### Values

* Autonomy, seek self-improvement for tackling issues head-on;
* Adventurous, take no fear of failures on the path of progress;
* Light-hearted support, general people out there didn't experience what you've already had;
* Humility, no matter how good you are, there's no good in boasting yourself over experiences only a few have had;

#### Announcements

HeavenMS development achieved an acceptable state-of-the-art and will get into a halt. A heartfelt thanks for everyone that contributed in some way for the progress of this server!

Although development is halted, support for fixing features that were implemented here is still up. You can still actively help us improve the server by issuing pull requests with informative details about what's changing.

If you liked this project, please don't forget to __star__ the repo ;) .

It's never enough to tell this, thanks to everyone that have been contributing something for the continuous improvement of the server! Be it through bug reports, donation, code snippets and/or pull requests.

Our Discord channel is still available on: https://discord.gg/Q7wKxHX

### Donation

If you REALLY liked what you have seen on the project, please feel free to donate a little something as a helping hand for my contributions towards Maple development. Also remember to **support Nexon**!

Paypal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3K8KVTWRLFBQ4

### Disclaimer

* HeavenMS staff has __no current intention__ to publicly open a server with this source, if that ever comes to happen this note will be lifted. __Don't be scammed!__

* This server source is __NOT intended to be stable__ as is. Proper deadlock review and other maintenance checks are needed in order to make it suitable for production use.

---
### Preparing the ambient 

The following link teaches on how to install a MapleStory v83 private server, however IT DIFFERS on what is used here: http://forum.ragezone.com/f428/maplestory-private-server-v83-741739/

Use that link ONLY AS AN ORIENTATION on where here things start to become ambiguous.

Firstly, install all the general tools required to run the server:

* WampServer2.0i.exe -> recipient of the MySQL server.
* mysql-query-browser.msi -> MySQL client component, visually shows the DB data and hubs queries.
* hamachi.msi -> used for establishing a tunnelling route for the server/client communication.


Now install the Java 7 Development Kit:

* jdk-7u79-windows-x64.exe
* netbeans-8.0.2-javase-windows.exe -> It's a NetBeans project, use other IDE at your own risk.

Now that the tools have been installed, test if they are working.

For WampServer:

* Once you're done installing it, run it and you will see the Wamp icon on the bottom right corner.
Left click it and click 'Put Online'.
* In case of ORANGE ICON, change port 80 at "httpd.conf" to another, as it clashes with a Windows default port. Then Left click it again and click 'Start All Services'.
* The Wamp icon must look completely green (if its orange or red, you have a problem).

For Hamachi:

* Try opening it. It's that simple.

Hamachi is optional, though. You don't have to install Hamachi if you want to make the server just for use on your own machine. However, if you want to let other players access your server, consider alternatively using port-forwarding methods.

---
### Installing the SERVER 

Set the "HeavenMS" folder on a place of your preference. It is recommended to use "C:\Nexon\HeavenMS".

Setting up the SQL: open MySQL Query Browser, then create a new session with the parameters below, then click OK.

Server Host: localhost		Port: 3306		Username: root

Now it must be done CAREFULLY:

1. File -> Open Script... -> Browse for "C:\Nexon\HeavenMS\sql" -> db_database.sql, and execute it.

2. File -> Open Script... -> Browse for "C:\Nexon\HeavenMS\sql" -> db_drops.sql, and execute it.

Now it is OPTIONAL, you don't need to run it if you don't want, as it will simply change some NPC shops to set some new goods, not present in the original MapleStory, to sell:

3. File -> Open Script... -> Browse for "C:\Nexon\HeavenMS\sql" -> db_shopupdate.sql, and execute it.

At the end of the execution of these SQLs, you should have installed a database schema named "heavenms". REGISTER YOUR FIRST ACCOUNT to be used in-game by **creating manually** an entry on the table "accounts" at that database with a login and a password.

Configure the IP you want to use for your MapleStory server in "configuration.ini" file, or set it as "localhost" if you want to run it only on your machine. Alternatively, you can use the IP given by Hamachi to use on a Hamachi network, or you can use a non-Hamachi method of port-forwarding. Neither will be approached here.

Now open NetBeans, and click "Open a project..." . Select then the "HeavenMS" folder, that should already be a project recognizable by NetBeans. If it isn't, you have a problem.

#### Inside the project, you may encounter some code errors.

These errors pops-up because you have not set yet the "cores" of the project. From the project hierarchy, right-click the project and select "Resolve Project Problems".

Locate the "cores" folder inside the root directory of this project and manually configure the missing files with the files that are there.

Also, a new Java7 platform must be defined to run the server. Click "Manage Platforms...", then "Add platform", browse through until you locate the Java7 folder in the file system, it should be at "C:\Program Files\Java". Then, name this new platform "JDK 1.7".

Finally, select "Clean and Build project" to build the JAR file for the MapleStory server. Once done, make sure both WampServer and Hamachi are on and functional, then execute "launch.bat" on the root of the project. If no errors were raised from this action, your MapleStory server is now online.

---
### Installing the CLIENT 

#### Setting up client-side ambient

The client's set-up is quite straightforward:

1. From "ManagerMsv83.exe", install MapleStory on your folder of preference (e.g. "C:\Nexon\MapleStory") and follow their instructions.
2. Once done, erase these files: "HShield" (folder), "ASPLauncher.exe", "MapleStory.exe" and "patcher.exe".
3. Extract into the client folder the "localhost.exe" from the provided link.
4. Overwrite the original WZ files with the ones provided from either one of those folders on the Google Drive:
	- "commit???_wz" (last published RELEASE, referring to commit of same number).
	- "current_wz" (latest source update).

#### Editing localhost IP target

If you are not using "localhost" as the target IP on the server's config file, you will need to HEX-EDIT "localhost.exe" to fetch your IP. Track down all IP locations by searching for "Text String" "127.0.0.1", and applying the changes wherever it fits.

To hex-edit, install the Neo Hex Editor from "free-hex-editor-neo.exe" and follow their instructions. Once done, open "localhost.exe" for editing and overwrite the IP values under the 3 addresses. Save the changes and exit the editor.

#### Testing the localhost

Open the "localhost.exe" client. If by any means the program did not open, and checking the server log your ping has been listened by the server and you are using Windows 8 or 10, it probably might be some compatibility issue.

In that case, extract "lolwut.exe" from "lolwut-v0.01.rar" and place it on the MapleStory client folder ("C:\Nexon\MapleStory"). Your "localhost.exe" property settings must follow these:

* Run in compatibility mode: Windows 7;
* Unchecked reduced color mode;
* 640 x 480 resolution;
* Unchecked disable display on high DPI settings;
* Run as an administrator;
* Opening "lolwut.exe", use Fraysa's method.

Important: should the client be refused a connection to the game server, it may be because of firewall issues. Head to the end of this file to proceed in allowing this connection through the computer's firewall. Alternatively, one can deactivate the firewall and try opening the client again.

---
### Creating an account and logging in the game

By default, the server source is set to allow AUTO-REGISTERING. This means that, by simply typing in a "Login ID" and a "Password", you're able to create a new account.

After creating a character, experiment typing in all-chat "@commands". This will display all available commands for the current GM level your character has.

To change a character's GM level, make sure that character is not logged in, then:

* Open MySQL Query Browser;
* Double-click "heavenms" schema;
* Double click "characters" table;
* Execute the selected query;
* Mark "Edit" flag on the MySQL Query Browser UI screen;
* Locate your character's row on the displayed ResultSet;
* Edit your character's GM level;
* Hit APPLY CHANGES.

---
### Some notes about WZ/WZ.XML EDITING 

NOTE: Be extremely wary when using server-side's XMLs data being reimporting into the client's WZ, as some means of synchronization between the server and client modules, this action COULD generate some kind of bugs afterwards. Client-to-server data reimporting seems to be fine, though.

#### Editing the v83 WZ's:

* Use the HaRepacker 4.2.4 editor, encryption "GMS (old)".
* Open the desired WZ for editing and use the node hierarchy to make the desired changes (copy/pasting nodes may be unreliable in rare scenarios).
* Save the changed WZ, **overwriting the original content** at the client folder.
* Finally, **RE-EXPORT (using the "Private Server..." exporting option) the changed XMLs into the server's WZ.XML files**, overwriting the old contents.

**These steps are IMPORTANT, to maintain synchronization** between the server and client modules.

#### The MobBookUpdate example

As an example of client WZ editing, consider the MapleMobBookUpdate tool project I developed, it updates all reported drop data on the Monster Book with what is currently being hold on the database:

To make it happen:

* Open the MobBookUpdate project on NetBeans, located at "tools\MapleMobBookUpdate", and build it.
* At the subfolder "lib", copy the file "MonsterBook.img.xml". This is from the original WZ v83.
* Paste it on the "dist" subfolder.
* Inside "dist", open the command prompt by alt+right clicking there.
* Execute "java -jar MobBookUpdate.jar". It will generate a "MonsterBook_updated.img.xml" file.
* At last, overwrite the "MonsterBook.img.xml" on "C:\Nexon\HeavenMS\wz\String.wz" with this file, renaming it back to "MonsterBook.img.xml".

At this point, **just the server-side** Monster Book has been updated with the current state of the database's drop data.

To **update the client as well**, open HaRepacker 4.2.2 and load "String.wz" from "C:\Nexon\MapleStory". Drop the "MonsterBook.img" node by removing it from the hierarchy tree, then import the server's "MonsterBook.img.xml".

**Note:** On this case, a server-to-client data transfer has been instanced. This kind of action **could cause** problems on the client-side if done unwary, however the nodes being updated on client-side and server-side provides no conflicts whatsoever, so this is fine. Remember, server-to-client data reimport may be problematic, whereas client-to-server data reimport is fine.

The client's WZ now has the proper item drops described by the DB updated into the MobBook drop list.

**Save the changes and overwrite the older WZ** on the MapleStory client folder.

---
### Portforwarding the SERVER

To use portforward, you will need to have permission to change things on the LAN router. Access your router using the Internet browser. URLs vary accordingly with the manufacturer. To discover it, open the command prompt and type "ipconfig" and search for the "default gateway" field. The IP shown there is the URL needed to access the router. Also, look for the IP given to your machine (aka "IPv4 address" field), which will be the server one. 

The default login/password also varies, so use the link http://www.routerpasswords.com/ as reference. Usually, login as "admin" and password as "password" completes the task well.

Now you have logged in the router system, search for anything related to portforwarding. Should the system prompt you between portforwarding and portriggering, pick the first, it is what we will be using.

Now, it is needed to enable the right ports for the Internet. For MapleSolaxia, it is basically needed to open ports 7575 to 7575 + (number of channels) and port 8484. Create a new custom service which enables that range of ports for the server's channel and opt to use TCP/UDP protocols. Finally, create a custom service now for using port 8484.

Optionally, if you want to host a webpage, portforward the port 80 (the HTTP port) as well.

It is not done yet, sometimes the firewalls will block connections between the LAN and the Internet. To overcome this, it is needed to create some rules for the firewall to permit these connections. Search for the advanced options with firewalls on your computer and, with it open, create two rules (one outbound and one inbound).

These rules must target "one application", "enable connections" and must target your MapleStory client (aka localhost).

After all these steps, the portforwarding process should now be complete.
