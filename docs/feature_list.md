#**HeavenMS _(MapleSolaxiaV2)_**

Credits:

Ronan - Developer

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

* HPQ/KPQ/LPQ/LMPQ/OPQ/EllinPQ/PiratePQ/MagatiaPQ/HorntailPQ/AmoriaPQ/TreasurePQ.
* CWKPQ as Expedition-based event.
* Expeditions: Scarga/Horntail/Showa/Balrog/Zakum/Pinkbean.
* GuildPQ 100% + Guild queue with multi-lobby systems available.
* Brand-new PQs: BossRushPQ, CafePQ.
* Mu Lung Dojo.
* Capt. Latanica remade as an event (parties can now fight the boss).

Skills:

* Maker skill features properly developed.
* Server is using heurisitics to calculate fee costs for the Maker (errors sums up to 8k mesos, reagent errors stacks up comformant with it's level).
* New skill: Chair Mastery (max lv 1) - Players having this passive skill can gain a significant boost of HP/MP recovery when sitting on a field/map chair.

Quests:

* Doll house quest functional.
* Quests can now reward properly items when matching a reward item with the player's job.
* Loads of quests have been patched.
* Quest rewards according to jobs works properly.
* Enchanced rewarding system: checks for stacking opportunities on the inventory before checking for new slots.

Player Social Network:

* Guild and Alliance system fully functional.
* Beginners can create and join a "beginner-only" party (characters up to level 10).
* Enhanced synchronization on Player Shops and Hired Merchants. Transactions made are instantly informed to the owner.

Cash & Items:

* EXP/DROP/Cosmetic Coupons.
* EXP/DROP coupons now appears as a buff effect when on active time.
* Great deal of cash items functional.
* New scroll: antibanish. For use only in cases where bosses send a player back to town.
* Inventory system properly checks for item slot free space and ownership.
* Storage with "Arrange Items" feature functional.
* Vega's spell.
* Owl of Minerva.
* Pet item ignore.
* New Year's card (New Year effect sometimes d/c's a player).

Monsters, Maps & Reactors:

* Every monsterbook card is now droppable by overworld mobs.
* Added meso drop data for basically every missing overworld mob.
* Monsterbook displays drop data info conformant with the underlying DB (needs custom wz). See more on the MobBookUpdate feature.
* Every skill/mastery book is now droppable by mobs.
* Added Boss HP Bar for dozens of bosses (needs provided custom wz).
* If multiple bosses are on the same area, client will prioritize Boss HP bar of the target of the player.
* Boats, elevator and other travelling mechanics fully working.
* PQs, Taxis and other event-driven situations warps players at random spawnpoints, GMS-like.
* Some reactors (PQ bonus boxes) now sprays items on the map, instead of dropping everything at once.
* Updated Crimsonwood, World Tour, Nihal Desert and Neo City, enabling quest completion and game progression in these areas.
* Giant Cake (anniversary-themed boss) drops Maple equipments, Maple scrolls, summoning bags and many more interesting items.

PQ potentials:

* Lobby system - Multiple PQ instances on same channel.
* Expedition system - Multiples parties can attempt on a same instance (lobbies and expeds are mutually-exclusive).
* Guild queue system - Guilds can register themselves on a queue for the GPQ.
* EIM Pool system - After the first instance setup, next event instances are loaded beforehand and set on a pooling queue, optimizing future loadouts.

Player potentials:

* Adventurer Mount quests functional.
* All Equipment levels up.
* Player level rates.
* Gain fame by quests.

Server potentials:

* Multi-worlds.
* Inventory auto-gather and auto-sorting feature.
* Enhanced auto-pot system: pet uses as many potions as necessary to reach the desired threshold.
* Enhanced buff system: smartly checks for the best available buff effects to be active on the player.
* Enhanced AP auto-assigner: exactly matches AP with the needed for the player's current level, surplus assigned to the primary attribute.
* Mastery book announcer displays droppers of needed books of a player, by reading underlying DB.
* Custom jail system (needs provided custom wz).
* Delete Character (requires ENABLE_PIC activated).
* Autosaver (periodically saves on DB current state of every player in-game).
* Both fixed and randomized versions of HP/MP growth rate available, regarding player job (enable one at ServerConstants). Placeholder for HP/MP washing feature.
* Accounts can be created automatically when trying to login on an inexistent account -- credits to shavit.
* Usage of Bcrypt (up-to-date) as the main password hashing algorithm, replacing old SHA's -- credits to shavit.

Admin/GM commands:

* Server commands layered by GM levels.
* Spawn Zakum/Horntail/Pinkbean.
* New commands.

External tools:

* MapleArrowFetcher - Updates min/max quantity dropped on all arrows drop data, calculations based on mob level and whether it's a boss or not.
* MapleCouponInstaller - Retrieves coupon info from the WZ and makes a SQL table with it. The server will use that table to gather info regarding rates and intervals.
* MapleIdRetriever - Two behaviors: generates a SQL table with relation (id, name) of the handbook given as input. Given a file with names, outputs a file with ids.
* MapleInvalidItemIdFetcher - Generates a file listing all inexistent itemid's currently laying on the DB.
* MapleMapInfoRetriever - Basic tool for detecting missing info nodes on the map field structures (maps failing to have an info node on the WZ is an critical issue).
* MapleMesoFetcher - Creates meso drop data for mobs with more than 4 items (thus overworld mobs), calculations based on mob level and whether it's a boss or not.
* MapleMobBookIndexer - Generates a SQL table with all relations of cardid and mobid present in the mob book.
* MapleMobBookUpdate - Generates a wz.xml that is a copy of the original MonsterBook.wz.xml, except it updates the drop data info in the book with those currently on DB.
* MapleQuestItemFetcher - Searches the SQL tables and project files and reports in all relevant data regarding missing/erroneous quest items.
* MapleQuestMesoFetcher - Searches the quest WZ files and reports in all relevant data regarding missing/erroneous quest fee checks.
* MapleSkillMakerFetcher - Updates the DB Maker-related tables with the current info present on the WZs.
* MapleSkillMakerReagentIndexer - Generates a new maker table describing all stat-improvements from the Maker reagents (those empowering crystals and jewels).

Project:

* Organized project code.
* Highly configurable server (see all server flags at ServerConstants).
* Fixed/added some missing packets for MoveEnvironment, summons and others.
* Reviewed many Java object aspects that needed concurrency protection.
* Heavily reviewed future task management inside the project. Way less trivial schedules are spawned now, relieving task overload on the TimerManager.
* ThreadTracker: embedded auditing tool for run-time deadlock scanning throughout the server source (relies heavily on memory usage, designed only for debugging purposes).

---------------------------