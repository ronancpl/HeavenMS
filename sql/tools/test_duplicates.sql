SELECT dropperid,itemid FROM drop_data d WHERE EXISTS
  (SELECT * FROM drop_data e WHERE (d.id != e.id AND d.itemid=e.itemid AND d.dropperid=e.dropperid)) group by itemid,dropperid;