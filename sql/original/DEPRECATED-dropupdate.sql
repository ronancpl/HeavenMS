#OBS.: este sql só deverá ser utilizado se for descoberto falhas de listagem de dados em
#AMBOS O SPIDER E OS DROPS ORIGINAIS!!!

UPDATE drop_data SET chance=10000 WHERE (itemid >= 2380000 AND itemid < 2390000) AND (chance= 1000);

INSERT INTO `drop_data` (`dropperid`, `itemid`, `minimum_quantity`, `maximum_quantity`, `questid`, `chance`) VALUES
(120100, 2380002, 1, 1, 0, 10000),
(4230116, 2382042, 1, 1, 0, 10000),
(4230117, 2382055, 1, 1, 0, 10000),
(2230101, 4032399, 1, 1, 2251, 10000),
(100100, 4001352, 1, 1, 28205, 7000),
(100101, 4001352, 1, 1, 28205, 7000),
(120100, 4001352, 1, 1, 28205, 7000),
(130101, 4001352, 1, 1, 28205, 7000),
(1210100, 4001352, 1, 1, 28205, 7000);