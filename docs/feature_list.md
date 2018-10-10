# HeavenMS

Credits:

Ronan - Head Developer

Vcoc - Freelance Developer

---------------------------
DISCLAIMER:
---------------------------

This is NOT intended to be a PURE v83 MapleStory server emulator (acting
under the clean WZ files, provided by Nexon/Wizet). There has been provided
a whole array of edited WZ on the Drive to be used for this server. Although
normal WZs will load properly, there is no guarantee in-game bugs and issues
may arise because of some incompatibilities with the clean files.

---------------------------
Feature list:
---------------------------

PQs:

* HPQ/KPQ/LPQ/LMPQ/OPQ/EllinPQ/PiratePQ/MagatiaPQ/HorntailPQ/AmoriaPQ/TreasurePQ/ElnathPQ/HolidayPQ.
* CWKPQ as Expedition-based event.
* Expeditions: Scarga/Horntail/Showa/Balrog/Zakum/Pinkbean.
* GuildPQ + Guild queue with multi-lobby system available.
* Brand-new PQs: BossRushPQ, CafePQ.
* Mu Lung Dojo.
* Capt. Latanica remade as an event (parties can now fight the boss).

Skills:

* Some skills behaving oddly have been patched, such as Steal, Venomous Star/Stab, Heal and Mystic Doors.
* Maker skill features properly developed.
* Improved current Battleship skill, now showing the HP properly on buff tab and making visible for others after changing maps.
* Server is using heuristics to calculate fee costs for the Maker (errors sums up to 8k mesos, reagent errors stacks up comformant with it's level).
* New skill: Chair Mastery (max lv 1) - Players having this passive skill can gain a significant boost of HP/MP recovery when sitting on a field/map chair.
* Mu Lung Dojo skills functional.

Quests:

* Doll house quest functional.
* Quests can now reward properly items when matching a reward item with the player's job.
* Quest rewards according to jobs works properly.
* Reward selection and randomed reward works properly.
* Loads of quests have been patched.
* Meso requirement for starting/completing quests now must be met by the player.
* Lots of job questlines (rewarding skills) have been patched/implemented.
* Enhanced rewarding system: checks for stacking opportunities on the inventory before checking for new slots.
* Improved the quest expiration system, one of the tweaks making the clock UI disappear when completing/expiring quests.
* Reviewed Aran questline.
* Reviewed 4th job skill questlines as a whole.
* Complete overhaul on the 3rd job quiz (explorers), with all 40-question pool now made available.

Player Social Network:

* Guild and Alliance system fully functional.
* Implemented Marriage system from the ground-up (excluding character packet encoding parts that were already present, proper credits given throughout the source files).
* Beginners can create and join a "beginner-only" party (characters up to level 10).
* HP bar of party members now properly calculates the HP gain from equipments.
* Enhanced synchronization on Player Shops and Hired Merchants. Transactions made are instantly informed to the owner.
* Player Shops and Hired Merchants properly displaying the correct shop image to other players, and informing whether the shop is available to visit or full.
* Game minirooms such as match cards and omok now has a functional password system.
* Item pickup cooldown on non-owned/non-partyowned items functional.
* Further improved the server's ranking system, now displaying properly daily player ranking movement.
* Automated support for Player NPCs and Hall of Fame.
* Protected concurrently and improved the face expression system, guarding from trivial packet spam and exploits.
* All upgradeable non-cash equipments in inventory with level & EXP information available for read by anyone, given proper visibility.
* Further improved the existent minigame mechanics: remarkably checking out for no-item match requests, allowing different omok/matchcard match layouts and status update on the player matchbox tooltips.

Cash & Items:

* EXP/DROP/Cosmetic Coupons.
* EXP/DROP coupons now appears as a buff effect when on active time.
* Code coupons functional, with support for multiple items on the same code.
* Merged unique ids for pets, rings and cash items, thus solving some cash shop inventory issues.
* Great deal of cash items functional.
* MapleTV mechanics stabilized and separated by world.
* GMS-esque omok/match card drop chances.
* New scroll: antibanish. For use only in cases where bosses send a player back to town.
* Inventory system properly checks for item slot free space and ownership.
* Storage with "Arrange Items" feature functional.
* Close-quarters evaluation mode for items (sandbox).
* Further improved Karma scissors & Untradeable items mechanics.
* Spikes on shoes.
* Vega's spell.
* Owl of Minerva.
* Pet item ignore.
* New Year's card (New Year effect sometimes d/c's a player).
* Kite.
* Cash Shop Surprise.
* Maple Life.

Monsters, Maps & Reactors:

* Every monsterbook card is now droppable by overworld mobs.
* Added meso drop data for basically every missing overworld mob.
* Monsterbook displays drop data info conformant with the underlying DB (needs custom wz). See more on the MobBookUpdate feature.
* Every skill/mastery book is now droppable by mobs.
* Mobs now can drop more than one of the same equipment (number of possible drops defined at droptime, uses the minimum/maximum quantity fields on DB).
* Mobs only drops items that are visible/collectable by the player's party.
* Redesigned HT mechanics for spawn and linked damage to the sponge.
* Reviewed aspects of MoveLifeHandler: implemented banish move, patched MP cost not contabilized on non-skill mob moves and slightly fixed mobs dropping from footholds in certain cases.
* Limited item count on maps, smartly expiring oldest registered items, preventing potential item flooding.
* Implemented Zombify disease status.
* Added Boss HP Bar for dozens of bosses (needs provided custom wz).
* If multiple bosses are on the same area, client will prioritize Boss HP bar of the target of the player.
* Boss HP Bar and Server Messages now toggles (server message disappears when a boss battle is detected, and returns afterwards). Idea thanks to GabrielSin.
* Improved map bounding checks for item drop points, assuring most of the items dropped will be available to pickup inside the accessible map area.
* Boats, elevator and other travelling mechanics fully working.
* HP decreasing overtime on maps and mechanics to prevent them (consumables, equips) fully functional.
* Crimson Balrog boat approaching visual effect made functional.
* Maps having everlasting items no longer expires them.
* PQs, Taxis and other event-driven situations warps players at random spawnpoints, GMS-like.
* Some reactors (PQ bonus boxes) spraying items on the map, instead of dropping everything at once.
* Reactors pick items up smartly, checking for an option to pick up on many-items-nearby scenario.
* Updated many scripted portals not implementing SFX properly.
* Updated Crimsonwood, World Tour, Nihal Desert and Neo City, enabling quest completion and game progression in these areas.
* Added world maps for Mushroom Castle, World Tour (Singapore, Malaysia and Zipangu) & Ellin Forest areas.
* Added World Tour and Masteria continents in the world map.
* Reviewed World Map's town/field tooltips and links from the main world map and Masteria region.
* Giant Cake (anniversary-themed boss) drops Maple equipments, Maple scrolls, summoning bags and many more interesting items.

PQ potentials:

* Advanced synchronization and smart management of the PQ registration system, as expected for a core server mechanic that is largely used by the players.
* Lobby system - Multiple PQ instances on same channel.
* Expedition system - Multiples parties can attempt on a same instance (lobbies and expeds are mutually-exclusive).
* Guild queue system - Guilds can register themselves on a queue for the GPQ.
* EIM Pool system - After the first instance setup, next event instances are loaded beforehand and set on a pooling queue, optimizing future loadouts.
* Recall system - Players can rejoin the last event instance they were in before disconnection.

Player potentials:

* Adventurer Mount quests functional.
* All Equipment levels up.
* Player level rates.
* Gain fame by quests and event instances.
* Pet evolutions functional (not GMS-like).
* Reviewed keybinding system.
* Account's Character slots: either each world has it's own count or there's a shared value between all worlds.
* Optional cash shop inventory separated by player classes or fully account-ranged.

Server potentials:

* Multi-worlds.
* Dynamic world rates, each world can hold it's own rates from server bootup.
* Dynamic World/Channel deployment. While not implemented here, new channel deployment sensitive to quantity of online players was originally resinate's idea.
* Inventory auto-gather and auto-sorting feature.
* Enhanced auto-pot system: pet uses as many potions as necessary to reach the desired threshold.
* Enhanced buff system: smartly checks for the best available buff effects to be active on the player.
* Enhanced AP auto-assigner: exactly matches AP with the needed for the player's current level, surplus assigned to the primary attribute.
* Enhanced inventory check: free slots on inventory smartly fetched on demand.
* Enhanced auto-loot handler: optimized the brute-force checks for some cash items on the player equipped inventory at every requisition.
* Added players-appointed bestsellers item ranking system for Owl of Minerva and Cash Shop.
* Tweaked pet/mount hunger: calculations for fullness/tiredness takes active time of the subject into account.
* Consistent experience and meso gain system.
* NPC crafters (equips, plates/jewels, etc) now won't take items freely if the requirement conditions are not properly met.
* Improved Duey mechanics: package received popup and reviewed many delivery mechanics.
* Pet item pickup now gives preference to player attacks rather than forcing attack disables when automatically picking up.
* Channel capacity bar functional and world servers with max capacity checks.
* Disease status are now visible for other players, even when changing maps.
* Players keep their current disease status saved when exiting the game, returning with them on login.
* Poison damage value are now visible for other players.
* Mastery book announcer displays droppers of needed books of a player, by reading underlying DB.
* Custom jail system (needs provided custom wz).
* Custom buyback system.
* Delete Character (requires ENABLE_PIC activated).
* Smoothed up view-all-char feature, now showing properly all available characters and not disconnecting players too often.
* Centralized getcurrenttime throughout several server handlers, boosting it's performance overall.
* Autosaver (periodically saves on DB current state of every player in-game).
* Both fixed and randomized versions of HP/MP growth rate available, regarding player job (enable one at ServerConstants). Placeholder for HP/MP washing feature.
* Implemented methods to get the current Players' MaxHP/MaxMP method with equipment HP/MP gains already summed up.
* Reallocated mapobjectids utilization throughout the source, preventing issues such as "NPC disappearing mysteriously after some server time" from happening.
* Implemented old GMS statup mechanic for novices level 10 or below. Usage of the edited localhost is mandatory on this.
* Accounts can be created automatically when trying to login on an inexistent account -- credits to shavit.
* Usage of Bcrypt (up-to-date) as the main password hashing algorithm, replacing old SHA's -- credits to shavit.

Custom NPCs:

* Spiegelmann: automatized rock-refiner.
* Asia: scroll & rarities shop NPC.
* Abdula: lists droppers of needed skill/mastery books.
* Agent E: accessory crafter.
* Donation Box: automatized item-buyer.
* Coco & Ace of Hearts: C. scroll crafters.

Server Commands:

* Server commands layered by GM levels.
* Spawn Zakum/Horntail/Pinkbean.
* Several new commands.
* Rank command highlighting users either by world or server-wide.
* Revamped command files layout -- thanks Arthur L.
* Optimized Search command, caching search range contents and added map search functionality.

External tools:

* MapleArrowFetcher - Updates min/max quantity dropped on all arrows drop data, calculations based on mob level and whether it's a boss or not.
* MapleBossHpBarFetcher - Searches the quest WZ files and reports in all relevant data regarding mobs that has a boss HP bar whilst not having a proper "boss" label.
* MapleCashDropFetcher - Searches the DB for any CASH drop data entry and lists them on a report file.
* MapleCouponInstaller - Retrieves coupon info from the WZ and makes a SQL table with it. The server will use that table to gather info regarding rates and intervals.
* MapleDojoUpdate - Patches the dojo WZ nodes with correct script names for onUserEnter and onFirstUserEnter fields.
* MapleEquipmentOmnileveler - Updates the equipment WZ nodes with item level information, allowing thus access for item level and EXP info for common equipments.
* MapleIdRetriever - Two behaviors: generates a SQL table with relation (id, name) of the handbook given as input. Given a file with names, outputs a file with ids.
* MapleInvalidItemIdFetcher - Generates a file listing all inexistent itemid's currently laying on the DB.
* MapleInvalidItemWithNoNameFetcher - Generates two files: one listing all itemid's with inexistent name and "cash" property. And other with a prepared XML to solve the name issue.
* MapleMapInfoRetriever - Basic tool for detecting missing info nodes on the map field structures (maps failing to have an info node on the WZ is an critical issue).
* MapleMesoFetcher - Creates meso drop data for mobs with more than 4 items (thus overworld mobs), calculations based on mob level and whether it's a boss or not.
* MapleMobBookIndexer - Generates a SQL table with all relations of cardid and mobid present in the mob book.
* MapleMobBookUpdate - Generates a wz.xml that is a copy of the original MonsterBook.wz.xml, except it updates the drop data info in the book with those currently on DB.
* MapleQuestItemCountFetcher - Searches the quest WZ files and reports in all relevant data regarding missing "count" labels on item acts at "complete quest".
* MapleQuestItemFetcher - Searches the SQL tables and project files and reports in all relevant data regarding missing/erroneous quest items.
* MapleQuestlineFetcher - Searches the quest WZ files and reports in all questids that currently doesn't have script files.
* MapleQuestMesoFetcher - Searches the quest WZ files and reports in all relevant data regarding missing/erroneous quest fee checks.
* MapleReactorDropFetcher - Searches the DB for reactors with drop data and reports in reactorids that are not yet coded.
* MapleSkillMakerFetcher - Updates the DB Maker-related tables with the current info present on the WZs.
* MapleSkillMakerReagentIndexer - Generates a new maker table describing all stat-improvements from the Maker reagents (those empowering crystals and jewels).

Project:

* Organized project code.
* Highly updated drop data.
* Highly configurable server (see all server flags at ServerConstants).
* Fixed/added some missing packets for MoveEnvironment, summons and others.
* Uncovered many Send/Recv opcodes throughout the source.
* Reviewed many Java object aspects that needed concurrency protection.
* Reviewed SQL data, eliminating duplicated entries on the tables.
* Improved login phase, using cache over DB queries.
* Usage of HikariCP to improve the DB connection management.
* Developed many survey tools for content profiling.
* Developed a robust anti-exploit login coordinator system.
* Protected many flaws with login management system.
* Channel, World and Server-wide timer management.
* Heavily reviewed future task management inside the project. Way less trivial schedules are spawned now, relieving task overload on the TimerManager.
* ThreadTracker: embedded auditing tool for run-time deadlock scanning throughout the server source (relies heavily on memory usage, designed only for debugging purposes).

Exploits patched:

* Player being given free access to any character of any account once they have authenticated their account on login phase.
* Player being given permission to delete any character of any account once they have authenticated their account on login phase.
* Player being able to start/complete any quest freely.
* Several assynchronous-oriented exploits patched, highlights on those involving Fredrick & Duey.

Localhost:

* Removed the 'n' problem within NPC dialog.
* Removed caps for MATK, WDEF, MDEF, ACC and AVOID.
* Removed "AP excess" popup and "Admin/MWLB" action block, original credits to kevintjuh93.
* Removed "You've gained a level!" popup, original credits to PrinceReborn.
* Removed "Cannot enter MTS from this map." popup on maps that blocks transitions (such change channel, CS/MTS), rendering the buyback option now available for all maps.
* Removed a check for players wishing to create/join a party being novices under level 10.
* Set a new high cap for SPEED.
* Removed the AP assign block for novices.
* Removed a block that would show up when trying to apply an attack gem on equipments that aren't weapons.

---------------------------