MapleSolaxiaV2

Freelance developer: Ronan C. P. Lana

Credits are to be given to the original MapleSolaxia staff and other colaborators, as just some minor
changes/patches on the game were applied by myself, in which some of them diverged from the original v83
patch contents.

This is a NetBeans 8.0.2 Project. This means that it's easier to install the project via "NetBeans' import
new project using existing code". Once installed, build this project on your machine and run the server using
the "launch.bat" application.

---- Installing ----

DropBox client files: https://www.dropbox.com/sh/fo3tg3q9liqvfeg/AACSqrjeytQepBOTeMHkKahya?dl=0

For additional tools described here, refer to the link to this folder on the DropBox:
	- ManagerMsv83: original client for MapleStory v83.
	- Localhostv83: overrides "MapleStory.exe" with the assigned IP in it's binary program.
	- lolwut-v0.01: necessary for running on Windows 8/10.
	- HaSuite-211: haha01haha01's repacker.
	- free-hex-editor-neo: edit the "localhost.exe" binary file.

To SET-UP the client:
	- Open the Managermsv83.exe and proceed the installation.
	- Once done, erase these files: "HShield" (folder), "ASPLauncher.exe", "MapleStory.exe" and "patcher.exe".
	- Extract into the client folder the "localhost.exe" from Localhostv83.
	- Overwrite the original WZ files with the ones provided from "client_wz" folder on the DropBox.

	In need of changing the server IP fetch on the MapleStory client, "localhost.exe" uses the
following byte addresses to store the server's IP address:
	- 006FE084;
	- 006FE094;
	- 006FE0A4;

To SET-UP the server:
	- Simply move the server folder to the destination place;
	- Compile the project on NetBeans. The JAR will be created at "dist" folder. Let it stay there.
	- Run the SQL scripts in sequence: db_database.sql, db_drops.sql, (optional) db_shopupdate.sql;
	- Create an login and password account at the "accounts" table.

---- Important note about CLIENT EDITING ----

	DO NOT USE THE SERVER'S XMLs for importing into the client's WZ, it WILL generate some kind of bugs
afterwards.
	- Use instead the HaRepacker 4.2.4, encryption "GMS (old)".
	- Open the desired WZ for editing and, USING IT'S UI, make the necessary changes.
	- Save the changed WZ, overwriting the original content at the client folder.
	- Finally, re-export ("Private Server..." option) the changed XMLs into the server's WZ.XML files,
overwriting the old contents.

These steps are important to maintain synchronization between the server and client modules.

---- Running issues ----

Firstly, launch the server using the server's "launch.bat". Then, try running the "localhost.exe" client.
If all goes well, we're done.

To run it in Windows 10:
	- Install everything normally;
	- WampServer 2.0 -> change port 80 at "httpd.conf" to another, as it clashes with a Windows
default port.
	- It is recommended to run the MapleStory client using: "Windows XP (SP2)" & "(8 or 16)-bit color mode";
	- If all else fails, use "lolwut.exe" to launch it.