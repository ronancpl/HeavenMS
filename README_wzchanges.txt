Changes from the original v83 WZs:

Map.wz/MapX/*:
	Fixed entries of portals placed incorrectly.

Item.wz/*:
	Fixed lacking "slotMax" properties in some items.
	Set flag "Quest Item" for Springy Worm.

Quest.wz/*:
	Fixed a bunch of quests, now giving the proper output. Added new quests.

String.wz/*:
	Fixed some NPC speeches.
	Updated MonsterBook with current drop data on the sql's DB, using
Ronan Lana's MobBookUpdate facility.


P.S.:

Yeah, these explanations are pretty simplistic, I know. If one really wants to
compare the two WZ systems, I recommend do the following:
	- Open HaRepacker and, for each MapleSolaxiaV2's WZ file, extract all the XMLs
for "Private Server".
	- Now, install MapleStory from "ManagerMsv83.exe" on an other folder and do the
same procediment said above.
	- Finally use some app to compare folders, like WinMerge, tracking differences
for all WZs.

Simply edit out any modification that ranges too far from the original MapleStory server
and overwrite the given WZ file, assuming you want a server more "GMS-like".