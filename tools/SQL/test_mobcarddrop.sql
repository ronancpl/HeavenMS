delimiter //

CREATE PROCEDURE countMobCardsBySection()
BEGIN
  DECLARE x INT DEFAULT 0;
  DECLARE I INT DEFAULT 0;
  WHILE I < 10 DO
    SELECT count(*) INTO x FROM (
      SELECT DISTINCT itemid FROM drop_data WHERE itemid >= (2380000 + I*1000) and itemid < (2380000 + (I+1)*1000)
    ) AS anysqltab_;

    insert into `_mob_card_counter` (`keyid`,`count`) values (2380000 + I*1000 , x);
    SET I = I + 1;
  END WHILE;
END;

//

delimiter ;

CREATE TABLE `_mob_card_counter` (
    `keyid` INTEGER(10),
    `count` INTEGER(10)
);

CALL countMobCardsBySection();

# the query below recovers all MAIN mobs that drops card.
SELECT cardid, mobid FROM monstercarddata LEFT JOIN drop_data ON monstercarddata.cardid = drop_data.itemid WHERE mobid = dropperid;

# the query below recovers MISSING cards from mobs that drops card.
SELECT cardid, mobid FROM monstercarddata WHERE cardid NOT IN (
SELECT cardid FROM monstercarddata LEFT JOIN drop_data ON monstercarddata.cardid = drop_data.itemid WHERE mobid = dropperid
);

# LENGTHY query that also recovers MISSING cards from mobs that drops card.
SELECT id FROM handbook WHERE id NOT IN (
  SELECT DISTINCT itemid FROM drop_data
) AND id>=2380000 AND id<2390000;

# retrieves number of drops of each represented mob in the monster book
SELECT dropperid, count(*) FROM drop_data WHERE dropperid IN (SELECT mobid FROM monstercardwz) GROUP BY dropperid ORDER BY dropperid;

# retrieves the rows where the number of drops of those mobs on monsterbook is lacking
SELECT dropperid, count(*) FROM drop_data WHERE dropperid IN (SELECT mobid FROM monstercardwz) GROUP BY dropperid HAVING count(*) < 5 ORDER BY dropperid;