package de.aequinoktium.twedit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * This class provides the interface to the SQLite Database.
 * it contains the database structure and the default data
 */
class DatabaseConnect(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        Log.d("Info", "Create DB")
        first_run(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS 'money_transfers'")
        db.execSQL("DROP TABLE IF EXISTS 'accounts'")
        db.execSQL("DROP TABLE IF EXISTS 'char_items'")
        db.execSQL("DROP TABLE IF EXISTS 'items'")
        db.execSQL("DROP TABLE IF EXISTS 'char_traits'")
        db.execSQL("DROP TABLE IF EXISTS 'trait_vars'")
        db.execSQL("DROP TABLE IF EXISTS 'traits'")
        db.execSQL("DROP TABLE IF EXISTS 'trait_grp'")
        db.execSQL("DROP TABLE IF EXISTS 'traits_cls'")
        db.execSQL("DROP TABLE IF EXISTS 'char_skills'")
        db.execSQL("DROP TABLE IF EXISTS 'skills'")
        db.execSQL("DROP TABLE IF EXISTS 'char_info'")
        db.execSQL("DROP TABLE IF EXISTS 'char_core'")
    }

    fun first_run(db: SQLiteDatabase) {
        var sql = """
            CREATE TABLE char_core (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255),
                phy INT DEFAULT 0,
                men INT DEFAULT 0,
                soz INT DEFAULT 0,
                nk INT DEFAULT 0,
                fk INT DEFAULT 0,
                lp INT DEFAULT 0,
                ep INT DEFAULT 0,
                mp INT DEFAULT 0,
                lp_cur FLOAT DEFAULT 0,
                ep_cur FLOAT DEFAULT 0,
                mp_cur FLOAT DEFAULT 0,
                xp_used INT DEFAULT 0,
                xp_total INT DEFAULT 0,
                career_mode BOOLEAN DEFAULT FALSE,
                deleted BOOLEAN DEFAULT FALSE
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                name VARCHAR(255) DEFAULT "",
                dataset VARCHAR(255) DEFAULT "",
                txt TEXT DEFAULT "",
                FOREIGN KEY (char_id) REFERENCES char_core(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE skills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(128) NOT NULL,
                icon VARCHAR(255) DEFAULT '',
                parent_id INT DEFAULT 0,
                spec INT DEFAULT 0,
                is_active BOOLEAN DEFAULT TRUE
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_skills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT, 
                skill_id INT,
                lvl INT DEFAULT 0,
                note VARCHAR(255) DEFAULT '',
                is_signature BOOLEAN DEFAULT FALSE, 
                FOREIGN KEY (char_id) REFERENCES char_core(id),
                FOREIGN KEY (skill_id) REFERENCES skills(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE trait_cls (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255) NOT NULL UNIQUE 
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE trait_grp (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255) NOT NULL UNIQUE 
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE traits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255) NOT NULL UNIQUE, 
                cls INT, 
                grp INT,
                txt TEXT,
                min_rank INT DEFAULT 1, 
                max_rank INT DEFAULT 1,
                xp_cost INT,
                effects VARCHAR(255) DEFAULT '',
                FOREIGN KEY (cls) REFERENCES trait_cls(id)
                FOREIGN KEY (grp) REFERENCES trait_grp(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE trait_vars (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                trait_id INT,
                name VARCHAR(255),
                xp_factor FLOAT,
                oper INT, 
                grp VARCHAR(255),
                txt TEXT,
                FOREIGN KEY (trait_id) REFERENCES traits(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_traits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT, 
                trait_id INT,
                rank INT,
                variants VARCHAR(255),
                xp_cost INT,
                name VARCHAR(255),
                txt TEXT,
                is_reduced BOOLEAN DEFAULT FALSE,
                effects VARCHAR(255),
                FOREIGN KEY (char_id) REFERENCES char_core(id),
                FOREIGN KEY (trait_id) REFERENCES traits(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255),
                desc TEXT DEFAULT '',
                cls VARCHAR(255) DEFAULT 'item',
                grp VARCHAR(255) DEFAULT '',
                icon VARCHAR(255) DEFAULT '',
                price FLOAT DEFAULT 0,
                avail INT DEFAULT 0,
                weight INT DEFAULT 0,
                weight_limit INT DEFAULT 0,
                equip_loc TEXT DEFAULT '',
                extra_data TEXT DEFAULT ''
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                name VARCHAR(255),
                desc TEXT DEFAULT '',
                cls VARCHAR(255),
                icon VARCHAR(255) DEFAULT '',
                qty INT DEFAULT 1,
                weight INT DEFAULT 0,
                weight_limit INT DEFAULT 0,
                original_quality INT DEFAULT 6,
                current_quality INT DEFAULT 6,
                price FLOAT DEFAULT 0,
                equipped BOOLEAN DEFAULT false,
                equip_loc TEXT DEFAULT '',
                packed_into INT DEFAULT 0,
                sort_pos INT DEFAULT 0,
                extra_data TEXT DEFAULT '',
                FOREIGN KEY (char_id) REFERENCES char_core(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                name VARCHAR(255),
                FOREIGN KEY (char_id) REFERENCES char_core(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE money_transfers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                origin_acc INT,
                target_acc INT,
                amount FLOAT,
                purpose VARCHAR(255)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(255) UNIQUE NOT NULL,
                value VARCHAR(255)
            );
        """.trimIndent()
        db.execSQL(sql)

        // add basic settings
        sql = """
            INSERT INTO settings (name, value) VALUES
            ('core.initial_xp', '330'),
            ('inventory.check_weight_limit', '1'),
            ('inventory.check_caliber', '1')
        """.trimIndent()
        db.execSQL(sql)

        // add skill data ...
        sql = """
            INSERT INTO skills (id, name, parent_id, spec, is_active) VALUES 
            (1,'Athletik',0,1,1),
            (2,'Klettern',1,2,1),
            (3,'Hochgebirgsklettern',2,3,1),
            (4,'Freiklettern',2,3,1),
            (5,'Fassadenklettern',2,3,1),
            (6,'Höhlenklettern',2,3,1),
            (7,'Reiten',1,2,1),
            (8,'Schwimmen',7,2,1),
            (9,'Rettungsschwimmen',7,3,1),
            (10,'Gerätetauchen',7,3,1),
            (11,'Fliegen',1,2,1),
            (12,'Gleitschirme',11,3,1),
            (13,'Flügel',11,3,1),
            (14,'Überleben',1,2,1),
            (15,'Geschick',0,1,1),
            (16,'Beweglichkeit',15,2,1),
            (17,'Entfesseln',16,3,1),
            (18,'Winden',16,3,1),
            (19,'Körperkontrolle',15,2,1),
            (20,'Balance',19,3,1),
            (21,'0G-Manöver',19,3,1),
            (22,'Tanzen',15,2,1),
            (23,'Schleichen',15,2,1),
            (24,'Verfolgen',23,3,1),
            (25,'sich verstecken',23,3,1),
            (26,'Fingerfertigkeit',0,1,1),
            (27,'Medizin und Erste Hilfe',26,2,1),
            (28,'Diagnose',27,3,1),
            (29,'Erste Hilfe',27,3,1),
            (30,'Chirurgie',27,3,1),
            (31,'Behandlung',27,3,1),
            (32,'Bauen und Reparieren',26,2,1),
            (33,'Maschinen- und Anlagenbau',32,3,1),
            (34,'Reaktoren und XS-Systeme',32,3,1),
            (35,'Computer und Elektronik',32,3,1),
            (36,'Metallbau',32,3,1),
            (37,'Schlösser knacken',32,3,1),
            (38,'Malen und Zeichnen',26,2,1),
            (39,'Illustration',38,3,1),
            (40,'Karten zeichnen',38,3,1),
            (41,'Kalligraphie',38,3,1),
            (42,'Technisches Zeichnen',38,3,1),
            (43,'Modellieren',26,2,1),
            (44,'Steinmetz',43,3,1),
            (45,'Maskenbildner',43,3,1),
            (46,'Taschenspielerei',26,2,1),
            (47,'Kartentricks',46,3,1),
            (48,'Jonglieren',46,3,1),
            (49,'Taschendiebstahl',46,3,1),
            (50,'Organisation und Verwaltung',0,1,1),
            (51,'Administration',50,2,1),
            (52,'Kalkulation und Buchführung',51,3,1),
            (53,'Personaleinsatzplanung',51,3,1),
            (54,'Handel',50,2,1),
            (55,'Schätzen',54,3,1),
            (56,'Schwarzmarkt',54,3,1),
            (57,'Ladung und Fracht',50,2,1),
            (58,'Packen und Stauen',57,3,1),
            (59,'Gefahrgut',57,3,1),
            (60,'Lagerverwaltung',57,3,1),
            (61,'Expedition ausrüsten',57,3,1),
            (62,'Führung',50,2,1),
            (63,'Kommando',62,3,1),
            (64,'Computer & Technik',0,1,1),
            (65,'Computer',64,2,1),
            (66,'Computernutzung',65,3,1),
            (67,'Informationsrecherche',65,3,1),
            (68,'Programmierung',65,3,1),
            (69,'Hacking',65,3,1),
            (70,'Sensorsysteme',64,2,1),
            (71,'Radar',70,3,1),
            (72,'Sonar',70,3,1),
            (73,'Schiffssysteme',64,2,1),
            (74,'Reaktorsteuerung',73,3,1),
            (75,'Lebenserhaltung',73,3,1),
            (76,'Forschung und Wissenschaft',0,1,1),
            (77,'Experimente und Laborarbeit',76,2,1),
            (78,'Wissenschaftliche Methodik',77,3,1),
            (79,'Dokumentation',77,3,1),
            (80,'Forensik',77,3,1),
            (81,'Archäologie',76,2,1),
            (82,'Recherche',76,2,1),
            (83,'Datalinks',82,3,1),
            (84,'Bibliotheken ',82,3,1),
            (85,'Pilot',0,1,1),
            (86,'Bodenfahrzeuge',85,2,1),
            (87,'Motorräder und Quads',86,3,1),
            (88,'Automobile',86,3,1),
            (89,'Lastwagen',86,3,1),
            (90,'Baumaschinen und schweres Gerät',86,3,1),
            (91,'Hovercrafts und Schweber',86,3,1),
            (92,'Luftfahrzeuge',85,2,1),
            (93,'Flugzeuge',92,3,1),
            (94,'Schweber',92,3,1),
            (95,'Raumschiffe',85,2,1),
            (96,'Kampfmanöver',95,3,1),
            (97,'Asteroidenflug',95,3,1),
            (98,'Planetare Landungen',95,3,1),
            (99,'Schiffe',85,2,1),
            (100,'Segeln',99,3,1),
            (101,'Motorboote',99,3,1),
            (102,'Navigation',85,2,1),
            (103,'Karten lesen',102,3,1),
            (104,'Routenplanung',102,3,1),
            (105,'Astrogation',85,2,1),
            (106,'Torverbindungen',105,3,1),
            (107,'Schleichwege',105,3,1),
            (108,'Gefechtsastrogation',105,3,1),
            (109,'Asteroidenfelder',105,3,1),
            (110,'Kommunikation',0,1,1),
            (111,'Verhandlungen',110,2,1),
            (112,'Feilschen',111,3,1),
            (113,'Vertragsgestaltung',111,3,1),
            (114,'Überreden',110,2,1),
            (115,'Totlabern',114,3,1),
            (116,'Einschüchtern',114,3,1),
            (117,'Überzeugen',110,2,1),
            (118,'Verführen',117,3,1),
            (119,'Propaganda',117,3,1),
            (120,'Lügen',110,2,1),
            (121,'Ausfragen',110,2,1),
            (122,'Verhör',121,3,1),
            (123,'Lügen erkennen',121,3,1),
            (124,'Folter',121,3,1),
            (125,'Lehren & Unterrichten',0,1,1),
            (126,'Lehrer',125,2,1),
            (127,'Didaktik',126,3,1),
            (128,'Trainer',125,2,1),
            (129,'G-Ball Coach',128,3,1),
            (130,'Fahrlehrer',128,3,1),
            (131,'Professor',125,2,1),
            (132,'Militärausbilder',125,2,1),
            (133,'Drill-Sergeant',132,3,1),
            (134,'Schießausbilder',132,3,1),
            (135,'Schauspielerei',0,1,1),
            (136,'Verkleiden',135,2,1),
            (137,'MakeUp',136,3,1),
            (138,'Kostüm',136,3,1),
            (139,'Imitation',135,2,1),
            (140,'Stimmen imitieren',139,3,1),
            (141,'Dialekte und Lingos',139,3,1),
            (142,'Bewegungsabläufe',139,3,1),
            (143,'Etikette und Gebräuche',0,1,1),
            (144,'Höfisches Benehmen',143,3,1),
            (145,'Militärisches Verhalten',143,3,1),
            (146,'Unbewaffneter Nahkampf',0,1,1),
            (147,'Raufen',146,2,1),
            (148,'Boxen',146,2,1),
            (149,'Kung Fu',146,2,1),
            (150,'Klingenwaffen',0,1,1),
            (151,'Messer und Dolche',150,2,1),
            (152,'Kurzschwerter',150,2,1),
            (153,'Langschwerter',150,2,1),
            (154,'Bidenhänder',150,2,1),
            (155,'Säbel und Krummschwerter',150,2,1),
            (156,'Hiebwaffen',0,1,1),
            (157,'Knüppel und improvisierte Hiebwaffen',156,2,1),
            (158,'Baseballschläger',157,3,1),
            (159,'Kurzstöcke',157,3,1),
            (160,'Teleskopschlagstock',157,3,1),
            (161,'Beile und Hämmer',156,2,1),
            (162,'Vorschlaghämmer',161,3,1),
            (163,'Streitkolben',161,3,1),
            (164,'Äxte',161,3,1),
            (165,'Stangenwaffen',156,2,1),
            (166,'Kampfstäbe',165,3,1),
            (167,'Hellebarden',165,3,1),
            (168,'Speere',165,3,1),
            (169,'Pistolen und Revolver',0,1,1),
            (170,'Pistolen',169,2,1),
            (171,'Revolver',169,2,1),
            (172,'Maschinenpistolen',169,2,1),
            (173,'Gewehre',0,1,1),
            (174,'Schrotflinten',173,2,1),
            (175,'Flinten',173,2,1),
            (176,'Scharfschützengewehre',173,2,1),
            (177,'Sturmgewehre',173,2,1),
            (178,'Strahlenwaffen',0,1,1),
            (179,'Strahlenpistolen',178,2,1),
            (180,'Strahlengewehre',178,2,1),
            (181,'Schwere Waffen',0,1,1),
            (182,'Maschinengewehre',181,2,1),
            (183,'Maschinenkanonen',181,2,1),
            (184,'Raketen- und Granatwerfer',181,2,1),
            (185,'Geschütze',0,1,1),
            (186,'Artillerie und Fahrzeugkanonen',185,2,1),
            (187,'Bordgeschütz',185,2,1),
            (188,'Invasionsabwehrgeschütz',185,2,1),
            (189,'Sprühtanks',0,1,1),
            (190,'Flammenwerfer und Säuretanks',189,2,1),
            (191,'Wurfwaffen',0,1,1),
            (192,'Wurfklingen',191,2,1),
            (193,'Messer',192,3,1),
            (194,'Beile',192,3,1),
            (195,'Wurfsterne',192,3,1),
            (196,'Schleudern',191,2,1),
            (197,'Schleuder',196,3,1),
            (198,'Bola',196,3,1),
            (199,'Netz',191,2,1),
            (200,'Granaten',191,2,1),
            (201,'Handgranaten',200,3,1),
            (202,'Rauchgranaten',200,3,1),
            (203,'Schockgranaten',200,3,1),
            (204,'Naturwissenschaften und Technologie',0,1,0),
            (205,'Biologie',204,2,0),
            (206,'Zellbiologie',205,3,0),
            (207,'Genetik',205,3,0),
            (208,'Tierkunde',205,3,0),
            (209,'Pflanzenkunde',205,3,0),
            (210,'Biologie der Hazaru',205,3,0),
            (211,'Humanbiologie',205,3,0),
            (212,'Stellarbiologie',205,3,0),
            (213,'Biospären',205,3,0),
            (214,'Gaja-Hypothese',205,3,0),
            (215,'Chemie',204,2,0),
            (216,'Chemische Analyse und Synthese',215,3,0),
            (217,'Biochemie',215,3,0),
            (218,'Sprengstoffherstellung',215,3,0),
            (219,'Pharmazie',215,3,0),
            (220,'Drogenproduktion',215,3,0),
            (221,'Materialkunde',215,3,0),
            (222,'Nanochemie',215,3,0),
            (223,'Materialforschung',215,3,0),
            (224,'Medizin',204,2,0),
            (225,'Anatomie',224,3,0),
            (226,'Neurologie',224,3,0),
            (227,'Psychologie',224,3,0),
            (228,'Mensch-Maschine-Integration',224,3,0),
            (229,'Psionik',224,3,0),
            (230,'Mathematik',204,2,0),
            (231,'Logik',230,3,0),
            (232,'Algebra',230,3,0),
            (233,'Geometrie',230,3,0),
            (234,'Statistik',230,3,0),
            (235,'Wirtschaftsmathematik',230,3,0),
            (236,'Technische Mathematik',230,3,0),
            (237,'Physik',204,2,0),
            (238,'Mechanik',237,3,0),
            (239,'Elektrodynamik',237,3,0),
            (240,'Thermodynamik',237,3,0),
            (241,'Relativitätstheorie',237,3,0),
            (242,'Astrophysik',237,3,0),
            (243,'Quantenphysik',237,3,0),
            (244,'Extraspatialphysik',237,3,0),
            (245,'Technologie und Ingenieurwissenschaften',204,2,0),
            (246,'Maschinenbau',245,3,0),
            (247,'Feinmechanik',245,3,0),
            (248,'Waffentechnik',245,3,0),
            (249,'Elektrik und Energietechnik',245,2,0),
            (250,'Elektronik und Computer',245,3,0),
            (251,'Baustatik',245,3,0),
            (252,'Umweltsysteme',245,3,0),
            (253,'Raumschiffbau',245,3,0),
            (254,'Geowissenschaften',204,2,0),
            (255,'Geologie',254,3,0),
            (256,'Tektonik und Vulkanologie',254,3,0),
            (257,'Metereologie',254,3,0),
            (258,'Kartographie',254,3,0),
            (259,'Terraforming',254,3,0),
            (260,'Gesellschafts- und Sozialwissenschaften',0,1,0),
            (261,'Geschichte',260,2,0),
            (262,'Politik',260,2,0),
            (263,'Gesellschaftsentwürfe',262,3,0),
            (264,'Regierungsformen',262,3,0),
            (265,'Rechtswissenschaften',260,2,0),
            (266,'Theologie',260,2,0),
            (267,'Wirtschaftswissenschaften',260,2,0),
            (268,'Buchführung',267,3,0),
            (269,'Unternehmensführung',267,3,0),
            (270,'Bankwesen',267,3,0),
            (271,'Finanzierung',267,3,0),
            (272,'Sprachen und Linguistik',0,1,0),
            (273,'Sprache ',272,2,0),
            (274,'Alte Dialekte',273,3,0),
            (275,'Schriftlicher Ausdruck',273,3,0),
            (276,'Etymologie',273,3,0),
            (277,'Regionale Dialekte',273,3,0),
            (278,'Novababel',272,2,0),
            (279,'Technobabel ',278,3,0),
            (280,'AlphaGanimed-Babel ',278,3,0),
            (281,'Novasperanto',272,2,0),
            (282,'Anglesh',272,2,0),
            (283,'Araby',272,2,0),
            (284,'Gotick',272,2,0),
            (285,'Españal',272,2,0),
            (286,'Franças',272,2,0),
            (287,'Latin',272,2,0),
            (288,'Helleniki',272,2,0),
            (289,'Nippon',272,2,0),
            (290,'Rosska',272,2,0),
            (291,'Konklav-Hazaru',272,2,0),
            (292,'Szezaru',272,2,0),
            (293,'Chetra',272,2,0),
            (294,'Řhu',272,2,0),
            (295,'Orionischer Standard ',272,2,0),
            (296,'Straßenwissen und Allgemeines Wissen',0,1,0),
            (297,'Livestyle und Mode',296,2,0),
            (298,'Speisen und Getränke',296,2,0),
            (299,'Kunst',296,2,0),
            (300,'Musik (Künstler/Kultur/Epoche)',299,3,0),
            (301,'Malerei (Künstler/Kultur/Epoche)',299,3,0),
            (302,'Theater (Künstler/Kultur/Epoche)',299,3,0),
            (303,'Mythen der Grenzwelten',296,2,0),
            (304,'Sport und Sportwetten',296,2,0),
            (305,'Spiele und Glücksspiele',296,2,0),
            (306,'Psychokinetik',0,1,1),
            (307,'Heben',306,2,1),
            (308,'Levitation',306,2,1),
            (309,'Stoppen',306,2,1),
            (310,'Telepathie',0,1,1),
            (311,'Gedankenlesen',310,2,1),
            (312,'Emotionen fühlen',31,2,1),
            (313,'Thermische Effekte',0,1,1),
            (314,'Entzünden',313,2,1),
            (315,'Kochen',313,2,1),
            (316,'Metall erhitzen',313,2,1),
            (317,'Schockfrosten',313,2,1);
        """.trimIndent()
        db.execSQL(sql)

        // trait classes
        sql = """
            INSERT INTO trait_cls (id, name) VALUES
            (1, 'Körperlich');
            (2, 'Mental'),
            (3, 'Sozial'),
            (4, 'Psionisch');
        """.trimIndent()
        db.execSQL(sql)

        // trait groups
        sql = """
            INSERT INTO trait_grp (id, name) VALUES
            (1, 'Sehen'),
            (2, 'Hören'),
            (3, 'Tasten & Fühlen'),
            (4, 'Sonstige Wahrnehmung'),
            (5, 'Körperbau'),
            (6, 'Schmecken & Riechen'),
            (7, 'Spezialisierte Attribute'),
            (8, 'Natürliche Waffen'),
            (9, 'Krankheiten'),
            (10, 'Verhaltensweisen'),
            (11, 'Fertigkeiten'),
            (12, 'Kultur'),
            (13, 'Charisma'),
            (14, 'Kommunikation'),
            (15, 'Status'),
            (16, 'Information'),
            (17, 'Besitz');
        """.trimIndent()
        db.execSQL(sql)

        // inserting traits ...
        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (1, 'Blind', -18, 1, 1, 'Der Charakter kann Licht im für Menschen üblicherweise sichtbaren Spektrum nicht wahrnehmen.<br/>Dieser Nachteil schließt nicht aus, dass der Charakter möglicherweise durch den Vorteil <i>Infrarotsicht</i> oder <i>UV-Sicht</i> sehen kann.'),
            (2, 'Dämmerungssicht', 3, 1, 1, 'Dieser Vorteil erhöht die Lichtempfindlichkeit der Augen. Ein Modifikator für schlechte Lichtverhältnisse wird für diesen Charakter nur bei absoluter Dunkelheit angewendet. Bereits eine einzelne Leuchtdiode oder Sternenlicht reichen dem Charakter um sich zu orientieren.'),
            (3, 'Infrarotsicht', 6, 1, 1, 'Der Charaktere ist in der Lage Licht im Infrarotspektrum wahrzunehmen.'),
            (4, 'UV-Sicht', 6, 1, 1, 'Der Charakter ist in der Lage Licht im ultravioletten Spektrum zu sehen'),
            (5, 'Infraschallgehör', 3, 1, 2, 'Der Charakter ist in der Lage Schall mit Frequenzen von weniger als 20Hz zu hören.'),
            (6, 'Ultraschallgehör', 3, 1, 2, 'Ein Charakter mit diesem Vorteil kann Töne jenseits von 30kHz wahrnehmen.'),
            (7, 'Sensibles Gehör', 6, 1, 2, 'Die Hörschwelle liegt niedriger als bei einem normalen Menschen, der Charakter ist in der Lage auch sehr leise Töne wahrzunehmen. Wahrnehmungsproben werden um -1 erleichtert.'),
            (8, 'Schwerhörig', -6, 1, 2, 'Mit diesem Nachteil liegt die Hörschwelle des Charakters deutlich höher, als bei einem normalen Menschen. Eine schwerhörige Person kann leise Geräusche nicht wahrnehmen und hat ernsthafte Schwierigkeiten einem geflüsterten Gespräch zu folgen. Der entsprechende Probenmodifikator beträgt +1.'),
            (9, 'Empfindliches Gehör', -3, 1, 2, 'Das Hörorgan des Charakters verträgt laute Geräusche nicht so gut, die Schmerzschwelle liegt niedriger als bei den meisten Menschen. Bereits ab 100dB empfindet der Charakter Schmerzen und Unwohlsein.'),
            (10, 'Robustes Gehör', 3, 1, 2, 'Die Ohren des Charakters sind ziemlich unempfindlich und erst ein Schalldruck jenseits von 140dB verursacht Schmerzen und Unwohlsein.'),
            (11, 'Taub', -9, 1, 2, 'Ein Charakter mit diesem Nachteil kann Töne und Klänge im üblichen menschlichen Wahrnehmungsbereich nicht hören.'),
            (12, 'Vermindertes Schmerzempfinden', -3, 1, 3, 'Schmerzen sind ein Warnsignal, dass irgendetwas mit dem Körper nicht in Ordnung ist, somit handelt es sich um einen Nachteil. Ein Charakter mit diesem Nachteil muss eine unmodifizierte MEN-Probe ablegen, um eine Verletzung zu registrieren.'),
            (13, 'Kein Schmerzempfinden', -3, 1, 3, 'Dieser Charakter hat überhaupt kein Schmerzempfinden. Das kann zwar in gewissen Situationen durchaus von Vorteil sein, allerdings muss er eine um 2 erschwerte MEN-Probe ablegen, um eine Verletzung zu registrieren.'),
            (14, 'Hohe Schmerzempfindlichkeit', -3, 1, 3, 'Der Charakter empfindet bereits kleinere Verletzungen als schmerzhaft. Wird der Charakter verletzt und verliert LP, muss er eine unmodifizierte MEN-Probe bestehen. Bei einem Misserfolg ist er W10 Kampfrunden benommen und alle Proben werden um +1 erschwert.'),
            (15, 'Ausgeprägte Temperaturwahrnehmung', -3, 1, 3, 'Der Charakter kann sehr feine Temperaturgradienten unterscheiden und auch ziemlich genau abschätzen, wie warm oder kalt ein Gegenstand ist.'),
            (16, 'Keinerlei Temperaturempfindung', -3, 1, 3, 'Mit diesem Nachteil ist der Charakter nicht in der Lage festzustellen, ob ein Gegenstand oder eine Flüssigkeit möglicherweise zu heiß oder zu kalt ist. Sofern der Charakter allerdings über ein normales Schmerzempfinden verfügt, bemerkt er, wenn er sich verbrennt oder Erfrierungen zuzieht.'),
            (17, 'Synesthesie', 3, 1, 4, 'Der Charakter besitzt kombinierte Sinne - kann Farben schmecken oder Geräusche sehen.'),
            (18, '0-G-Intoleranz', -6, 1, 4, 'Der Charakter verträgt keine Schwerelosigkeit. Jedes Mal, wenn er Schwerelosigkeit ausgesetzt wird muss er eine PHY-Probe ablegen. Bei Gelingen sind alle Proben in Schwerelosigkeit um einen Punkt erschwert. Bei Misslingen hat der Charakter mit Schwindel, Übelkeit, Sturzgefühlen und ähnlichem zu kämpfen. Seine Proben sind um 3 Punkte erschwert.'),
            (19, 'Flügel', -6, 1, 5, 'Der Charakter besitzt einen Satz Flügel. Du liest das schon korrekt, das ist ein Nachteil! Flügel sind unhandlich und groß. Und allein der Umstand, dass der Charakter über diese Gliedmaßen verfügt, bedeutet noch lange nicht, dass er auch wirklich fliegen kann. Das wird über entsprechende Fertigkeiten und Talente abgewickelt, die zusätzlich erworben werden müssen.'),
            (20, 'Beidhändigkeit', 9, 1, 5, 'Der Charakter kann mit beiden Händen gleich gut umgehen. Insbesondere vermag er zwei gleichartige einhändige Waffen zu führen die als ein Angriff gewertet werden.');
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (21, 'Feine Nase', 3, 1, 6, 'Der Geruchssinn dieses Charakters ist deutlich besser ausgeprägt, als der eines Menschen. Er kann Aromastoffe in deutlich geringeren Spuren wahrnehmen. Wahrnehmungsproben die sich auf den Geruchssinn stützen, werden um einen Punkt pro Rang erleichtert. Um die Aromastoffe auch zuzuordnen, benötigt der Charakter passende Wissensfertigkeiten, beispielsweise <i>Geruchskenntnis (Drogen)</i> oder <i>Geruchskenntnis (Gewürze)</i>.', 1, 6),
            (22, 'Geruchsblind', -3, 1, 6, 'Der Charakter hat einen deutlich unterdurchschnittlich ausgeprägten Geruchssinn. Wahrnehmungsproben, die sich auf den Geruchssinn stützen (etwa auch das Wahrnehmen von Rauchgeruch) werden um einen Punkt pro Rang erschwert. Ein Charakter mit diesem Nachteil sollte nicht als Schiffskoch tätig werden, er neigt dazu beim Kochen deutlich zu überwürzen.', 1, 6),
            (23, 'Verminderter Tastsinn', -3, 1, 3, 'Der Charakter hat ein deutlich weniger ausgeprägtes Tastgefühl, als die meisten Menschen. Er weiß zwar, wenn sich seine Hände um einen Gegenstand schließen, kann aber höchstens durch Sicht abschätzen, wie fest er jetzt zudrückt. Proben bei denen es auf Fingerfertigkeit ankommt, werden um einen Punkt pro Rang erschwert. Das können auch Proben sein, bei denen es ganz allgemein auf einen festen uns sicheren Griff ankommen, wie etwa <i>Klettern</i>.', 1, 6),
            (24, 'Ausgeprägter Tastsinn', 3, 1, 3, 'Durch diesen Vorteil ist der Charakter in der Lage, deutlich feinere Strukturen zu erstasten, als ein Mensch das könnte. Der Charakter erkennt feinste Unebenheiten oder Risse in  Oberflächen. Entsprechende Proben werden um einen Punkt pro Rang erleichtert. Ab Rang zwei, kann ein Charakter einen gedruckten Text durch Tasten lesen, wenn er die entsprechende Übung hat (was sich in einer entsprechenden Wissensfertigkeit ausdrücken sollte).', 1, 6),
            (25, 'Kräftig', 6, 1, 7, 'Der Charakter ist besonders muskulös und athlethisch. Für Athletikproben und auf rohe Kraft bezogene Proben, ist das effektive PHY-Attribut um einen Punkt pro Rang höher.', 1, 3),
            (26, 'Schwach', -6, 1, 7, 'Die Kraft des Charakters lässt zu wünschen übrig. Für alle Fertigkeitsproben, bei denen es auf die Körperkraft ankommt, ist der effektive PHY-Wert um 1 pro Rang niedriger.', 1, 3),
            (27, 'Gelenkig', 6, 1, 7, 'Durch diesen Vorteil ist der Charakter besonders beweglich, er kann sich durch sehr schmale Ritzen schieben. Der effektive PHY-Wert für Proben, bei denen die Beweglichkeit eine Rolle spielt wird um eins pro Rang erhöht.', 1, 3),
            (28, 'Ungelenkig', -6, 1, 7, 'Dieser Charakter ist eher unbeweglich und hat Schwierigkeiten mit komplexen, koordinierten Körperbewegungen. Für Proben, bei denen es auf die Gewandtheit ankommt, ist der effektive PHY-Wert um ein pro Rang in diesem Nachteil niedriger.', 1, 3),
            (29, 'Fingerfertig', 6, 1, 7, 'Durch diesen Vorteil hat der Charakter besonders flinke Finger und eine gute Hand-Auge-Koordination. Der effektive PHY-Wert für Proben, bei denen es auf geschickte Finger ankommt, wird um eins pro Rang erhöht.', 1, 3),
            (30, 'Grobmotoriker', -6, 1, 7, 'Dieser Nachteil ist für Charaktere gedacht, die sich regelmäßig die Finger verknoten. Alle Proben in diesem Fertigkeitskomplex werden mit einem effektiven PHY-Attribut behandelt, das um eins pro Rang in diesem Nachteil gesenkt ist.', 1, 3),
            (31, 'Schnell', 3, 1, 7, 'Für alle Berechnungen zur Bewegungsgewschwindigkeit des Charakters, wird das PHY-Attribut um den Rang des Vorteils erhöht.', 1, 3),
            (32, 'Langsam', 3, 1, 7, 'Zur Berechnung der Bewegungsgeschwindigkeit des Charakters wird das PHY-Attribut pro Rang des Nachteils um einen Punkt gesenkt.', 1, 3),
            (33, 'Eiserner Lebenswille', 3, 1, 7, 'Der Charakter ist ein zäher Brocken. Er überlebt auch Verletzungen, die jeden anderen töten würden. Die Todesschwelle erhöht sich um zwei Punkte pro Rang.<br/><i>Beispiel: Ein Charakter mit 5 LP und 2 Rängen in diesem Vorteil hat eine Todesschwelle von -9</i>
            ', 1, 3);
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (34, 'Vermindertes Spektrum', -3, 1, 1, 'Durch diesen Nachteil werden verschiedene Spielarten der Farbenblindheit abgedeckt. Charaktere erleiden negative Modifikatoren auf Proben, bei denen es auch auf Farben ankommen - etwa die Beurteilung von Kunstwerken oder das Erkennen von gefälschten Geldscheinen, aber auch für Proben auf elektronische Reparaturen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (34, 'Rot-Grün-Blind', -3, 0, 'Ausprägung', 'Der Charakter kann Rot- und Grüntöne nur als hell/dunkel wahrnehmen'),
            (34, 'Farbenblind', -6, 0, 'Ausprägung', 'Ein Charakter mit diesem Nachteil kann Farben gar nicht unterscheiden, nur die Helligkeit.'),
            (34, 'Kurzes Spektrum', -6, 0, 'Ausprägung', 'Der Charakter kann den oberen oder unteren Bereich des für Menschen sichtbaren Spektrums gar nicht wahrnehmen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (35, 'Erweitertes Sichtfeld', 3, 1, 1,'Ein Charakter mit diesem Vorteil besitzt ein Sichtfeld von deutlich über 180°. Das heißt es ist schwieriger, den Charakter zu überraschen. Jeder Rang in diesem Vorteil erweitert das Sichtfeld um 30°. Für ein Sichtfeld von 270° also drei Ränge und für Rundumsicht von 360° sechs Ränge.', 1, 6),
            (36, 'Eingeschränktes Sichtfeld', -3, 1, 1, 'Der Charakter hat ein eingeengtes Gesichtfeld. Hierunter fallen Erkrankungen, die einen Tunnelblick verursachen, aber auch der Umstand, dass der Charakter nur ein Auge besitzt. Analog zum <i>Erweiterten Sichtfeld</i> bringt entspricht jeder Rang einer Reduktion des Blickfelds von 30°. Rang 2 entspricht mit 120° dem Sichtfeld eines Einäugigen. Ein Sichtfeld von weniger als 30° sollte über den Nachteil <i>Blind</i> abgehandelt werden.', 1, 6)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (37, 'Kurzsichtig', -1, 1, 1, 'Die Augen des Charakters sind nicht in der Lage auf große Entfernungen zu fokussieren. Ohne Brille oder Kontaktlinsen kann der Charakter weiter entfernte Objekte nur schwer oder gar nicht erkennen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (37, 'Geringe Beeinträchtigung', -1, 0, 'Ausprägung', 'Modifikator +1 für Wahrnehmungsproben auf Sicht bei einer Distanz von mehr als 5m.'),
            (37, 'Deutliche Sehschwäche', -3, 0, 'Ausprägung', 'der Modifikator steigt +1 alle 3m.'),
            (37, 'Extrem kurzsichtig', -6, 0, 'Ausprägung', 'Modifikator +1 pro Meter Entfernung.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (38, 'Weitsichtig', -1, 1, 1, 'Die Augen des Charakters können auf Nahe Objekte nicht korriekt fokussieren.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (38, 'Geringe Beeinträchtigung', -1, 0, 'Ausprägung', 'Modifikator +1 für Wahrnehmungsproben auf Sicht bei einer Distanz von weniger als 5m.'),
            (38, 'Deutliche Sehschwäche', -3, 0, 'Ausprägung', 'der Modifikator beträgt +3 für Objekte, die weniger als 2m entfernt sind. Alle 3m reduziert sich der Modifikator um 1.'),
            (38, 'Extrem weitsichtig', -6, 0, 'Ausprägung', 'der Modifikator liegt bei +6 für Objekte, die weniger als 2m entfernt sind. Alle 3m reduziert sich der Modifikator um 1.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (39, 'Riesenwuchs/Gigant', -3, 1, 5, 'Der Charakter ist nach dem Maßstab der Gesellschaft zu groß. Er hat Schwierigkeiten, passende Kleidung zu finden. Standardmäßige Betten und Räume sind ihm zu eng und zu niedrig. Er neigt dazu, sich an Türstöcken anzustoßen oder ständig gebeugt zu gehen. Zudem reagieren manche Menschen negativ auf ihn – vorwiegend mit Angst und entsprechender Abneigung.<br/>Ein Charakter mit diesem Nachteil sollte ein PHY-Attribut von mindestens 5 besitzen.')
        """.trimIndent()

        db.execSQL(sql)
        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (39, 'Riesenwuchs', -3, 0, 'Ausprägung', 'Für Charaktere mit einer Körpergröße von 2,10m - 2,50m.'),
            (39, 'Gigant', -6, 0, 'Ausprägung', 'Für Charaktere über 2,50m.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (40, 'Zwergenwuchs/Winzig', -3, 1, 5, 'Der Charakter ist zu klein für die Gesellschaft. Es ist eine Herausforderung, passende Kleidung zu finden, alle Möbel und Gerätschaften sind zu groß. Außerdem reagieren Fremde auf den Charakter mit Mitleid oder Vorurteilen...')
        """.trimIndent()

        db.execSQL(sql)
        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (40, 'Zwergenwuchs', -3, 0, 'Ausprägung', 'bei einer Körpergröße von 1,00m bis 1,50m'),
            (40, 'Winzig', -6, 0, 'Ausprägung', 'bei einer Größe unter 1,00m')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (41, 'Richtungssinn', 6, 1, 4, 'Der Charakter hat ein ausgesprochen gutes Gespür für Richtungen und Dimensionen, er weiß in welche Richtung er sich bewegt und wie weit. Selbst in auf dem Meer oder in der Wüste schafft er es geradeaus zu gehen. Auch kann er die Größe von Räumen oder Objekten ziemlich gut abschätzen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (41, 'unter Schwerkraft', 6, 0, 'Ausprägung', 'der Richtungssinn ist von einer stabilen Schwerkraftlage abhängig'),
            (41, 'bei Schwerelosigkeit', 9, 0, 'Ausprägung', 'der Charakter kann sich selbst bei Schwerelosigkeit mühelos orientieren.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (42, 'Fehlende oder verkrüppelte Gliedmaßen', -1, 1, 5, 'Hierbei handelt es sich um einen Nachteil, der in seinen Ausprägungen und auch seiner tatsächlichen Wirkung extrem vielschichtig ist.<br/>Die nachfolgende Auflistung erhebt aus diesem Grund auch keinen Anspruch auf vollständigkeit und soll nur einen groben Rahmen darstellen.<br/>Viele der Einschränkungen können prinzipiell mit Prothesen ausgeglichen werden. Die Wirkung verschiedener Hilfsmittel wird in der Ausrüstungsliste beschrieben. Natürlich ist es möglich, Prothesen zur Charaktererschaffung mit dem Vorteil <i>Startkapital</i> zu erwerben.<br/>Gliedmaßen, die aufgrund von Erkrankungen oder Verletzungen im Laufe des Lebens verloren wurden, können durchaus auch wiederhergestellt werden. Die entsprechende medizinische Prozedur ist allerdings eher unangenehm und sehr teuer.<br/>Besonders bei der mehrfachen Wahl dieses Nachteils, sollte man die Spielbarkeit des Charakters im Auge behalten.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (42, 'Einzelne verkrüppelte oder fehlende Finger', -1, 0, 'Ausprägung', 'Bei speziellen Fingerfertigkeitsproben, wie <i>Musizieren</i>, kann ein +1 Modifikator zum Tragen kommen.'),
            (42, 'Steifes Handgelenk', -3, 0, 'Ausprägung', 'Bestimmte Fingerfertigkeits oder Gewandtheitsproben können um +1 erschwert werden.'),
            (42, 'Verkrüppelte Hand', -6, 0, 'Ausprägung', 'Eine fingerlose oder stark verkrüppelte Hand mit eingeschränkter Beweglichkeit. Fingerfertigkeitsproben, bei denen üblicherweise beide Hände zum Einsatz kommen, werden um bis zu zwei Punkte erschwert. Dieser Nachteilsausprägung entsprechen auch Hände die zu <i>Klauen</i> ausgebildet sind.'),
            (42, 'Amputation Unterarm', -9, 0, 'Ausprägung', 'Der Arm endet unterhalb des Ellbogen und kann nur sehr eingeschränkt eingesetzt werden. Fertigkeitsproben wie Klettern werden um +2 erschwert. Gewisse Aufgaben können auch tatsächlich unmöglich sein, etwa die Verwendung zweihändiger Nahkampfwaffen.'),
            (42, 'Fehlender Arm', -12, 0, 'Ausprägung', 'Die Erschwernis für Athletikproben kann bis +3 reichen. Während der Charakter mit dem (-9) Nachteil ein Gewehr noch über den Unterarm legen kann, fällt das bei diesem Grad der Behinderung aus.'),
            (42, 'Steifes Knie', -6, 0, 'Ausprägung', 'Die effektive BEW des Charakters reduziert sich um 1, bestimmte Athletikproben können erschwert sein.'),
            (42, 'Fehlendes Bein', -9, 0, 'Ausprägung', 'Teil- oder Vollamputation eines Beines. Der Charakter benötigt Prothesen, Krücken, einen Rollstuhl oder einen Antigrav-Rucksack zur Fortbewegung. Bestimmte Athletische Proben können erschwert oder unmöglich sein, die effektive BEW sinkt um 3.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (43, 'Schwanz', 0, 1, 5, 'Der Charakter besitzt zusätzlich zu Armen und Beinen einen ausgeprägten Schwanz.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (43, 'Einfacher Schwanz', 0, 0, 'Ausprägung', 'Ein einfacher Schwanz zählt nur als kosmetische Modifikation und kostet keine Punkte.'),
            (43, 'Balanceschwanz', 3, 0, 'Ausprägung', 'Dieser Schwanz ist deutlich beweglicher und kontrollierter, als der einfache Schwanz. Er ist kräftig genug um einen Gegenstand gezielt wegzuschieben oder einen Hebel zu betätigen. Auf Balanceproben kann es eine Erleichterung geben.'),
            (43, 'Greifschwanz', 9, 0, 'Ausprägung', 'Der Charakter kann seinen Schwanz fast wie einen normalen Arm/Tentakel einsetzen. Er kann angreifen, eine entsprechend gearbeitete Waffe verwenden und gewöhnliche, nicht all zu filigrane Gegenstände umfassen und bewegen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (44, 'Tentakeln', 1, 1, 5, 'Der Charakter verfügt über Tentakeln. Die Kosten für diesen Vorteil setzen sich aus einer Reihe von Faktoren zusammen.<br/>Es mag durchaus Spezies gegeben, die statt Armen Tentakeln besitzen. Ein Satz von vier Tentakeln (4) mit einer Kraft von (6) und einfacher Armlänge (2) kostet insgesamt (12) und kompensiert somit genau einmal den Nachteil <i>Fehlender Arm</i>.<br/>Jeder Satz Tentakeln mit einer Anzahl von vier oder acht, kann als <i>Hand</i> gewertet werden.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (44, '2 Stück', 1, 0, 'Anzahl', 'Ein Satz von zwei Tentakeln'),
            (44, '4 Stück', 3, 0, 'Anzahl', 'Ein Satz von vier Tentakeln'),
            (44, '8 Stück', 6, 0, 'Anzahl', 'Ein Satz von acht Tentakeln'),
            (44, 'zart', 1, 0, 'Stärke', 'Die Tentakeln können ½x PHY kg heben'),
            (44, 'dürr', 2, 0, 'Stärke', 'Die Tentakeln können 1x PHY kg heben'),
            (44, 'schwach', 3, 0, 'Stärke', 'Die Tentakeln können 2x PHY kg heben'),
            (44, 'normal', 4, 0, 'Stärke', 'Die Tentakeln können 3x PHY kg heben'),
            (44, 'kräftig', 5, 0, 'Stärke', 'Die Tentakeln können 5x PHY kg heben'),
            (44, 'stark', 6, 0, 'Stärke', 'Die Tentakeln können 10x PHY kg heben'),
            (44, 'kurz', 2, 0, 'Länge', 'etwa so lang wie normale Arme'),
            (44, 'lang', 4, 0, 'Länge', 'bis etwa zwei Meter Länge'),
            (44, 'enorm', 6, 0, 'Länge', 'wirklich lange Tentakeln')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (45, 'Weiteres Armpaar', 9, 1, 5, 'Der Charakter besitzt mehr als zwei Arme. Diesen Vorteil gibt es in zwei Rängen.<br/>Auf Rang 1 sind diese Arme sind kürzer, schwächer oder weniger fingerfertig, als die Hauptarme. Dennoch ist der Charakter möglicherweise in der Lage, mit diesen Armen schon einmal den Ersatzclip aus der Gürteltasche zu ziehen, während er mit den anderen Armen noch schießen.<br/>Bei Rang 2 besitzt der Charakter ein weiteres voll ausgebildetes Armpaar. Er ist prinzipiell in der Lage, zwei zweihändige Waffen zu führen und bekommt deutliche Erleichterungen auf bestimmte Athletikproben, etwa <i>Klettern</i><br/>Charaktere die multitasken, etwa eine Leiter hochklettern und gleichzeitig mit den anderen Armen schießen, erhalten auf die Aktionen entsprechende Erschwernisse analog zu <i>Aktionen während der Bewegung</i>.<br/>Ja, es mag Spezies geben, bei denen der der doppelt gewählte Nachteil <i>Fehlende/Verkrüppelte Gliedmaßen: Bein</i> durch diesen Vorteil ausgeglichen wird.',1 ,2)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (46, 'Natürliche Panzerung', 9, 1, 5, 'Die Haut des Charakters besitzt eine besonders robuste Beschaffenheit und ist schwer zu verletzen. Der Charakter besitzt 0/+1 Schadensresistenz.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (47, 'Raubtiergebiss', 1, 1, 8, 'Der Charakter besitzt einen kräftigen Kiefer und besonders spitze, sowie scharfe Zähne. Bei einem erfolgreichen Angriff, verursacht sein Biss schwerere Verletzungen. Ein Raubtiergebiss findet man bei vielen Hazaru-Unterarten aber auch bei den Reynora und den Douwg. Insbesondere in den extremeren Ausprägungen, kann diese Eigenschaft auch Auswirkungen auf soziale Interaktionen haben.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (47, 'Starker Kiefer', 1, 0, 'Ausprägung', 'Der Angriff mit Biss hat einen Schaden von 1/-1'),
            (47, 'Böse Reißzähne', 3, 0, 'Ausprägung', 'Hat einen Schadenswert von 2/0'),
            (47, 'Kräftiges Raubtiergebiss', 6, 0, 'Ausprägung', 'Ein Angriff von 2/-2')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (48, 'Krallen und Klauen', 1, 1, 8, 'Die Finger und/oder Zehen des Charakters laufen in ernsthafte Krallen oder Klauen aus. Bei einem erfolgreichen Angriff verursachen sie klaffende Wunden und schwere Verletzungen.<br/>Je nach Länge der Krallen, bietet es sich an, dazu eine entsprechende Einschränkung der Fingerfertigkeit in Betracht zu ziehen')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (48, 'Gefährliche Pranken', 1, 0, 'Ausprägung', 'Der Hieb verursacht einen Schaden von 1/-1'),
            (48, 'Scharfe Krallen', 3, 0, 'Ausprägung', 'Hat einen Schadenswert von 2/0'),
            (48, 'Eindrucksvolle Klauen', 6, 0, 'Ausprägung', 'Ein Angriff von 2/-2')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (49, 'Allergien und Asthma', -1, 1, 9, 'Der Charakter ist gegen eine oder mehrere Substanzen allergisch. Der Punktwert hängt von der Schwere der Allergie und der Art des Auslösers ab.<br/>An dieser Stelle ist der Spielleiter gefragt. Macht euch gemeinsam Gedanken, welche Auswirkungen die Allergie auf den Charakter hat. Eine Kontaktallergie die nässende Ausschläge verursacht wird eher die handwerklichen Fähigkeiten des Charakters beeinflussen. Eine Atemwegsallergie kann sich im Normalfall auf Ausdauer oder Wahrnehmung niederschlagen. Wenn im folgenden Text von <i>betroffenen Fertigkeiten</i> die Rede ist, dann sind dei Fertigkeiten gemeint, die durch die jeweilige Allergie in Mitleidenschaft gezogen werden.<br/>Allergien lassen sich aus Symptomen und Auslösern kombinieren.<br/>Die Punkte für die Symptome werden mit dem Auslöserwert multipliziert. Das Ergebnis wird aufgerundet.<br/>Eine Kaffeeallergie(x3) die zu schwerer Atemnot(-4) führt hat einen Wert von (-12), eine tödliche Allergie, Allergischer Schock(-9) gegen einen extrem seltenen Auslöser(x½) hat hingegen nur einen Wert von (-5).<br/>Letztes Wort über Art und Auswirkung der Allergie hat der Spielleiter.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (49, 'Keine Hautirritation', 0, 0, 'Hautirritationen', 'keine Reaktion'),
            (49, 'leichtes Jucken', -1, 0, 'Hautirritationen', 'Der Charakter spürt ein deutliches Jucken.'),
            (49, 'Rötungen/Pusteln', -2, 0, 'Hautirritationen', 'Die Haut reagiert deutlich gereizt'),
            (49, 'Nässender Ausschlag', -3, 0, 'Hautirritationen', 'Alle Proben bei denen entsprechende Körperteile beteiligt sind, werden um 1 Punkt erschwert.'),
            (49, 'Keine Atemreizung', 0, 0, 'Atmung', 'keine Reaktion'),
            (49, 'Schnupfen', -1, 0, 'Atmung', 'Leichte Atemschwierigkeiten. Erschwernis von 3 Punkten auf Riechen oder Schmecken. Möglicherweise 1 Punkt Aufschlag auf anstrengende körperliche Tätigkeiten.'),
            (49, 'Niesen', -2, 0, 'Atmung', 'Der Charakter reagiert auf die Allergene durch starkes Niesen. Der Charakter kann versuchen, das Nießen durch eine PHY-Probe zu unterdrücken'),
            (49, 'Husten', -2, 0, 'Atmung', 'Immer wieder Husten, der den Charakter zeitweilig einschränkt. Für jede Spielrunde wird eine PHY-Probe gewürfelt. Misslingt diese, werden alle Proben für die nächste Spielrunde aufgrund von Hustenanfällen um 1 Punkt erschwert.'),
            (49, 'Schwerer Husten', -3, 0, 'Atmung', 'Der Charakter wird, während er dem Allergen ausgesetzt ist, ständig von Husten gestört (1 Punkt Erschwernis). Für jede Spielrunde wird eine PHY-Probe gewürfelt, misslingt diese werden die Proben für die nächste Spielrunde um 3 Punkte erschwert.'),
            (49, 'Atemnot', -3, 0, 'Atmung', 'Der Charakter ist kurzatmig und kann keine anstrengenden Aktionen ausführen. 1 Punkt Erschwernis für anstrengende Aktionen.'),
            (49, 'schwere Atemnot', -4, 0, 'Atmung', 'Während der Charakter dem Allergen ausgesetzt ist, ist er extrem kurzatmig und eventuell auf Inhalatoren angeweisen. Grundsätzlich sind alle Proben um 1 Punkt erschwert. Anstrengende Aufgaben werden um 3 Punkte erschwert. Jede Minute wird eine PHY-Probe fällig, misslingt die Probe, erleidet der Charakter 1/0 Energieschaden.'),
            (49, 'Es bringt mich nicht um', 0, 0, 'Sonstiges', 'Die Allergie ist nervig aber nicht wirklich gefährlich.'),
            (49, 'Schwindelanfälle', -2, 0, 'Sonstiges', 'Alle zehn Minuten eine PHY-Probe, bei Misslingen werden alle Proben um 1 Punkt erschwert. Alle Fertigkeiten die Koordination erfordern (Schießen, Nahkampf, Klettern, Feinmotorische Dinge, etc.) sind um 3 Punkte erschwert.'),
            (49, 'Allergischer Schock', -9, 0, 'Sonstiges', 'Tritt direkt nach dem Kontakt mit dem Allergen auf und wird jede Minute in der der Charakter nicht behandelt wird wiederholt. Der Charakter ist handlungsunfähig, gelingt die PHY-Probe erleidet er 1/0 Schaden, bei Misslingen der Probe 2/0 Schaden'),
            (49, 'Allgegenwertiger Auslöser', 3, 1, 'Auslöser', 'für allgegenwärtig vorkommende Chemikalien oder Nahrungsmittel, wie Nanopolymere, Kunststoffe, Erdnüsse, Kaffee oder Soja.'),
            (49, 'Verbreiteter Auslöser', 2, 1, 'Auslöser', 'für leichte körperliche Anstrengung, sowie häufig vorkommende Chemikalien oder Nahrungsmittel wie Nickel, Onavu oder andere übliche Nahrungsmittel.'),
            (49, 'Seltener Auslöser', 1, 1, 'Auslöser', 'bei schwerer körperlicher Anstrengung oder seltenen Auslösern, seltenen Chemikalien oder Pollen von Pflanzen die es nur auf wenigen Welten gibt.'),
            (49, 'Sehr seltener Auslöser', 0.5, 1, 'Auslöser', 'bei extrem selenen Auslösern wie Fell einer Spezies, die es nur auf einer Welt gibt.')
        """.trimIndent()
        db.execSQL(sql)


        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (50, 'Motorische Störungen', -1, 1, 9, 'Dieser Nachteil umfasst eine Vielzahl von unterschiedlichen Erkrankungen oder Verletzungsfolgen, die zur Folge haben, dass der Charakter nur eingeschränkte Kontrolle über seinen Körper oder Teile davon besitzt. Dazu gehören kleinere Ärgernisse, wie unwillkürliche Muskelzuckungen, bis hin zu schwerwiegenden Einschränkungen, wie einer kompletten Querschnittslähmung vom Hals abwärts. Die Untenstehende Auswahl bildet nur einen kleinen Teil der Möglichkeiten ab. Gerade bei schwerwiegenden Einschränkungen sollte man an die Spielbarkeit des Charakters denken und natürlich Rücksprache mit dem Spielleiter halten. Bei Charakteren die nicht über die übliche Anzahl von Armen und Beinen verfügen, sind die Punktekosten mit dem Spielleiter abzustimmen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (50, 'Leichtes Zittern', -1, 0, 'Auswirkung:', 'Ein leichtes Zittern, das gegebenfalls die Fingerfertigkeit einschränkt'),
            (50, 'Zuckungen', -2, 0, 'Auswirkung:', 'Stärkere Muskelkontraktionen, die je nach betroffenem Körperteil -1 Erschwernis auf körperliche Proben nach sich ziehen kann'),
            (50, 'Krämpfe', -4, 0, 'Auswirkung:', 'Muskelverkrmpfungen, die den Körperteil möglicherweise unbrauchbar machen und bis zu -3 Erschwernis auf körperliche Proben nach sich ziehen kann'),
            (50, 'Lähmung', -6, 0, 'Auswirkung:', 'Die Muskulatur lässt sich nicht willentlich kontrollieren, der betroffene Körperteil ist unbrauchbar körperliche Tätigkeiten, bei denen es auf die Nutzung der betroffenen Körperteile ankommt sind stark erschwert oder unmöglich.'),
            (50, 'sekundäre Muskelgruppe', 0.5, 1, 'Betrifft:', 'Die Einschränkung betrifft eine untergeordnete Muskelgruppe und ist eher ärgerlich als hinderlich'),
            (50, 'Unterarm/Hand', 0.75, 1, 'Betrifft:', 'eine einzelnen Arm unterhalb des Ellbogen.'),
            (50, 'ganzer Arm', 1, 1, 'Betrifft:', 'eine Arm von der Schulter abwärts.'),
            (50, 'Arme und Hände', 2, 1, 'Betrifft:', 'Hände und Arme des Charakters'),
            (50, 'Ein Bein', 0.75, 1, 'Betrifft:', 'eines der Beine des Charakters'),
            (50, 'Unterkörper', 2, 1, 'Betrifft:', 'die unteren Extremitäten und den Unterkörper'),
            (50, 'eine Körperseite', 2, 1, 'Betrifft:', 'die rechte oder linke Körperhälfte'),
            (50, 'den ganzen Körper', 3, 1, 'Betrifft:', 'den ganzen Körper des Charakters'),
            (50, 'selten', 0.5, 1, 'Häufigkeit:', 'Einmal pro Abenteuer PHY-Probe, ob es zu einem Anfall kommt'),
            (50, 'gelegentlich', 0.75, 1, 'Häufigkeit:', 'Einmal pro Tag PHY-Probe, ob es zu einem Anfall kommt'),
            (50, 'häufig/immer', 1, 1, 'Häufigkeit:', 'Wenn es keine dauerhafte Einschränkung ist, dann bis zu einmal pro Stunde eine PHY-Probe.'),
            (50, 'kurzer Anfall', 0.5, 1, 'Dauer:', 'Die Dauer der Einschränkung beträgt W12 Kampfrunden.'),
            (50, 'längerer Anfall', 1, 1, 'Dauer:', 'Die Einschränkung hält W12 Minuten an.'),
            (50, 'permanent', 2, 1, 'Dauer:', 'Der Charakter leidet permanent an der Einschränkung ')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (51, 'Tot', -100, 1, 9, 'Der Charakter ist gestorben. Dies kann als Abenteueraufhänger für andere Charaktere dienen. Nein, die Punkte sind nicht übertragbar.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (52, 'Aggressiv', -3, 2, 10, 'Der Charakter ist aggressiv und kann seine Gefühlsausbrüche nur schwer kontrollieren.<br/>Um nicht mit Gewalt auf ein Problem zu reagieren, muss dem Charakter eine MEN-Probe gelingen, diese wird pauschal um den Rang des Nachteils erschwert und kann zudem abhängig vom Auslöser (Beleidigung, Tätlichkeit ...) weiter modifiziert werden.', 1, 6),
            (53, 'Dickköpfig', -3, 2, 10, 'Der Charakter ist nur schwer von einer einmal getroffenen Entscheidung abzubringen. Was er angefangen hat, will er auch zuende bringen, ob er nun jemand verfolgt oder er der meinung ist, dass er den Reaktor selbst reparieren muss.<br/>Sollte die Entscheidung ihn oder die Gruppe in Gefahr bringen oder möchte der Charakter seine Aktion abbrechen, kann er eine MEN-Probe, erschwert um den Rang des Nachteils, ablegen. Modifikatoren nach Spielleiterentscheid sind möglich.<br/>Der Einsatz von Unterrichten, Überreden, Einschüchtern und verwandten Fertigkeiten gegen den Charakter werden um den Rang des Nachteils erschwert.', 1, 6),
            (54, 'Ehrlich', -3, 2, 10, 'Der Charakter ist kreuzehrlich. Und man sieht es ihm an. Er ist nicht in der Lage zu lügen. Bei jedem Versuch zu lügen oder die Wahrheit zu verschweigen, muss ihm zuvor eine (um den Rang des Nachteils erschwerte) SOZ-Probe gelingen, um nicht sofort als Lügner aufzufallen. Diese kann nach Spielleiterentscheid modifiziert werden, so sind etwa Händler, Polizisten oder Richter besser darin, Lügner zu entlarven. Die normale Fertigkeitsprobe auf Lügen wird anschließend abgehandelt.<br/>Allerdings gilt der Charakter auch als ehrlich und integer – entsprechend positiv reagieren Personen, die ihn kennen. SOZ-Proben können unter umständen erleichtert werden.', 1, 6),
            (55, 'Geizig', -3, 2, 10, 'Ein Charakter mit diesem Nachteil ist nicht einfach nur sparsam, er versucht jegliche Art von Ausgaben zu vermeiden. Er gibt kein Trinkgeld, er bietet nicht an die Rechnung zu übernehmen und wird dem Bettler an der Straßenecke keinen Cent geben.<br/>Natürlich hat ein solcher Charakter auch Schwierigkeiten damit, die Richtige Menge Schmiergeld zwischen die Dokumente der Zollabfertigung zu legen. Um zu erkennen, dass eine scheinbar sinnlose Geldverschwendung eine notwendige Investition ist, muss dem Charakter eine MEN-Probe gelingen, die um den Rang des Nachteils erschwert ist.', 1, 6),
            (56, 'Gierig', -3, 2, 10, 'Der Charakter giert nach Reichtum, Geld, Schätzen, Kunstwerken oder auch nach besonders gutem Essen, Machtpositionen, Titeln oder Orden. Die Art der Gier sollte festgelegt werden, der Spielleiter kann durchaus zulassen dass der Spieler mehr als eine <i>Gier</i> besitzt.<br/>Wann immer sich eine Gelegenheit für den Charakter bietet, an ein Objekt seiner Begierde zu gelangen, muss er versuchen, es zu bekommen. Wäre der Versuch illegal oder lebensgefährlich, kann der Spielleiter eine SOZ-Probe erlauben, die wieder um den Rang des Nachteils erschwert ist.<br/>Im übrigen ist Gierig nicht gleich <i>Geizig</i>. Ein passionierter Taschendieb kann durchaus das Geld mit vollen Händen ausgeben - immerhin hat er ja eine relativ sichere Quelle.', 1, 6),
            (57, 'Impulsiv', -3, 2, 10, 'Der Charakter neigt dazu schnell, konsequent und ... ohne Überlegung zu handeln.<br/>Wichtig bei diesem Nachteil ist, ihn richtig zu spielen. Der Charakter hasst lange Diskussionen. Er trifft seine Entscheidung schnell und handelt danach.<br/>Während der Rest noch redet, ist er schon fertig.<br/>Genauso reagiert der Charakter auch schnell und unüberlegt. Eine Beleidigung? Ohrfeige. Eine schwere Beleidigung? Eine handfeste Prügelei ist im Gange.<br/>Jemand bescheißt ihn beim Kartenspielen... nun ihr wisst, was wir meinen.<br/>Um einen impulsiven Charakter zum planen zu bringen, muss ihm erst eine MEN-Probe (erschwert um den Rang des Nachteils) gelingen. Es soll aber auch helfen, ihn an einen Stuhl zu fesseln.', 1, 6),
            (58, 'Vorurteile', -3, 2, 10, 'Der Charakter hat ein festgefügtes und engstirniges Weltbild und eine Abneigung gegen alles, was nicht in sein Weltbild passt oder nach seinen Werten lebt. Ein extremes Beispiel für Intoleranz und Vorurteile sind die Jünger des zürnenden Herren. Sie hassen alles und jeden.<br/>Dieser Nachteil kann sich gegen Vertreter einer bestimmten Art, eines Berufsstands, einer Fraktion, einer religiösen oder sexuellen Überzeugung richten - oder aber auch gegen bestimmte Technologien, Staatsformen, etc. Prinzipiell kann dieser Nachteil mehrfach gewählt werden.<br/>Dem Charakter muss eine MEN-Probe gelingen, erschwert um den Rang des Nachteils, um seine Abneigung zu überwinden. Fertigkeitsproben im Umgang mit dem Subjekt oder Objekt der Abneigung, können zudem erschwert werden.', 1, 6),
            (59, 'Neugier', -3, 2, 10, 'Jeder Charakter sollte eine gewisse Neugierde mitbringen, sonst kann er auch zuhause bleiben und einem ganz normalen Job nachgehen. Neugierig als Nachteil heißt, der Charakter ist <i>zu neugierig</i>.<br/>Du verlangsamst durchaus mal deinen Schritt um ein Gespräch etwas länger zu verfolgen. Du blätterst am Kopierer auch durch die Ausdrucke die nicht dir gehören. Du wirfst einen langen Blick in den geöffneten Tresor.<br/>Der Spielleiter kann dir eine, um den Rang des Nachteils erschwerte, MEN-Probe erlauben, um zu entscheiden, den an Wand befindlichen, unbeschrifteten roten Knopf nicht zu drücken ...', 1, 6),
            (60, 'Pazifist', -3, 2, 10, 'Der Charakter hat eine Abneigung gegen die Anwendung von Gewalt. Er wird vermeiden jemanden zu verletzen oder gar zu töten. Der Charakter wird von sich aus keinen Kampf beginnen.<br/>Natürlich kann auch hier der Charakter entscheiden, eine (vorzugsweise nichttödliche) Waffe zu verwenden, um sich in einer Gefahrensituation seiner Haut zu erwehren. Ist davon auszugehen, dass das primäre Ziel der Angreifer nicht der Tod des Charakters ist, muss ihm eine MEN-Probe, modifiziert um den Rang des Nachteils, gelingen, um aktiv in den Kampf einzugreifen.', 1, 6)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (61, 'Phobien', -1, 2, 9, 'Der Charakter hat vor irgendetwas eine objektiv irrationale Angst. Die Liste der möglichen Phobien ist lang und sie hier aufzuführen würde wohl den Rahmen sprengen, üblich sind Raumangst, Höhenangst, Angst von Nagetieren oder Angst vom Menschenmassen.<br/>Der konkrete Wert der jeweiligen Phobie sollte anhand der Symptome und der Häufigkeit des Auslösers ermittelt werden. Der Charakter kann eine MEN-Probe erschwert um den halben Wert der Phobie machen um zu widerstehen.<br/>Der Gesamtwert einer Phobie sollte zwischen (-1) und (-18) liegen, letztendlich liegt es im Ermessen des Spielleiters ob er eine bestimmte Phobie zulässt und welchen Wert sie hat.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (61, 'Leichte Symptome', -1, 0, 'Symptome', 'Allgemeines Unwohlsein, Gänsehaut. Proben die Konzentration erfordern werden um 1 Punkt erschwert.'),
            (61, 'Mäßige Symptome', -2, 0, 'Symptome', 'Schwindelgefühl, erschreckter Schrei. Proben werden allgemein um 1 Punkt erschwert.'),
            (61, 'Schwere Symptome', -3, 0, 'Symptome', 'Der Charakter muss eine MEN-Probe ablegen, ansonsten muss er versuchen, sich aus der <i>Gefahrenzone</i> zu entfernen. Alle Proben sind um 2 Punkte erschwert.'),
            (61, 'Extreme Symptome', -6, 0, 'Symptome', 'Katatonie, Schreikrampf, Panikattacke. Der Charakter ist handlungsunfähig, kann aber eine MEN-Probe ablegen um sich zu lösen. Er muss dann sofort versuchen sich aus der <i>Gefahrenzone</i> zu entfernen..'),
            (61, 'Allgegenwärtige Auslöser', 3, 1, 'Auslöser', 'Menschenmassen, enge Räume, Höhen, Dunkelheit, große Leere'),
            (61, 'Häufige Auslöser', 2, 1, 'Auslöser', 'Menschenmassen, enge Räume, Höhen, Dunkelheit, große Leere, Alleinsein'),
            (61, 'Seltene Auslöser', 1, 1, 'Auslöser', 'Nagetiere, große Insekten, Spinnen, Schlangen, Hunde, etc.'),
            (61, 'Sehr seltene Auslöser', 0.5, 1, 'Auslöser', 'seltene Tierarten, unübliche Farben'),
            (61, 'Allgegenwärtige Auslöser', -1, 0, 'Auslöser', 'Kleidung einer bestimmten Kultur, Tierarten die nur auf einer einzigen Welt heimisch sind')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (62, 'Drogensucht', -1, 2, 9, 'Der Charakter hat eine Abhängigkeit zu einer bestimmten chemischen Substanz entwickelt. Das kann durchaus auch ein körperlicher Nachteil sein, primär ist es aber ein mentaler Nachteil. Er ist gezwungen, die Substanz regelmäßig zu sich zu nehmen, um normal aggieren zu können. Ansonsten drohen Entzugserscheinungen, unkontrollierte Gefühlsausbrüche, Schmerzen oder sogar gesundheitliche Schäden.<br/>Wie stark sich dieser Nachteil auswirkt, hängt von der jeweiligen Droge ab. Der Punktwert bildet sich als Summe verschiedener Faktoren.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (62, 'weitgehend positiv', 0, 0, 'Wirkung und Nebenwirkungen:', 'Grundsätzlich leistungssteigernde Drogen mit eher geringen Nebenwirkungen, wie etwa Koffein.'),
            (62, 'so lala', -2, 0, 'Wirkung und Nebenwirkungen:', 'Die Hauptwirkung ist durchaus positiv, aber möglicherweise deutliche Nebenwirkungen und Einschränkungen durch die Nutzung.'),
            (62, 'überwiegend negativ', -4, 0, 'Wirkung und Nebenwirkungen:', 'Drogen mit möglicherweise beeindruckenden Highs, jedoch starken Nebenwirkungen, wie etwa LSD.'),
            (62, 'weitgehend legal', 0, 0, 'Legalität:', 'Die Substanz ist in weiten Teilen des Hyperion und der Kernwelten legal erhältlich'),
            (62, 'teilweise legal', -2, 0, 'Legalität:', 'In vielen Regionen legal, mancherorts bei kleineren Geldbußen verboten.'),
            (62, 'weitgehend illegal', -3, 0, 'Legalität:', 'In vielen Regionen nicht nur bei Geldstrafe verboten, der Besitz und oder Handel kann Haftstrafen nach sich ziehen'),
            (62, 'geächtet', -4, 0, 'Legalität:', 'Zumeist aus guten Gründen ist diese Droge in vielen Jurisdiktionen strengstens verboten und schon der Besitz kleiner Mengen zieht drastische Strafen nach sich.'),
            (62, 'günstig', 0, 0, 'Kosten und Verfügbarkeit:', 'Eine Dosis ist für wenige Cent erhältlich bis Rand erhältlich. Die Ware wird in Geschäften legal über den Tresen verkauft.'),
            (62, 'erschwinglich', -1, 0, 'Kosten und Verfügbarkeit:', 'Der Preis für eine Dosis liegt üblicherweise unter 10 Rand. Möglicherweise muss man in spezielle Läden.'),
            (62, 'kostspielig', -2, 0, 'Kosten und Verfügbarkeit:', 'Bis zu fünfzig Rand muss man pro Dosis rechnen. Die Drogen bekommt man nur auf der Straße, aber wer weiß wonach er sucht wird meistens fündig'),
            (62, 'teuer', -4, 0, 'Kosten und Verfügbarkeit:', 'Hundert Rand, wenn man gute Connections hat, kann aber auch deutlich teurer sein. Ohne gute Connections echt schwer zu bekommen.'),
            (62, 'gering', 0.5, 1, 'Abhängigkeit/Entzug:', 'Geringes Suchtpotential und leichte Entzugserscheinungen'),
            (62, 'mäßig', 1, 1, 'Abhängigkeit/Entzug:', 'Kann bei vielen Anwendern bereits nach wenigen Anwendungen Suchverhalten auslösen und beim Entzug deutliche Nebenwirkungen verursachen. Ein Charakter mit Entzugserscheinungen ist unkonzentriert und die meisten Proben werden um einen Punkt erschwert.'),
            (62, 'extrem', 1.5, 1, 'Abhängigkeit/Entzug:', 'Bereits eine Anwendung kann eine Abhängigkeit nach sich ziehen, die Auswirkungen vom Entzug sind extrem und können tatsächlich körperliche Schäden nach sich ziehen. Ein Charakter der unter Entzugserscheinungen leidet muss Proben mit bis zu drei Punkten Erschwernis ablegen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (63, 'Spielsucht / Wettsucht', -3, 2, 10, 'Deutliches Symptom für Spielsucht ist häufiges, auch episodenhaftes, Glücksspiel. Dabei ist der Betroffene überzeugt, dass er das System besiegt hat und bessere Gewinnchancen besitzt, als der Gelegenheitsspieler und möglicherweise sogar die Bank. Aus zunächst gelegentlichem Spiel werden häufigere Sitzungen am Spieltisch oder Automaten, dabei werden die Einsätze immer höher, in der Hoffnung, aufgelaufene Verluste wieder reinzuholen.<br/>Der Spielsüchtige befasst sich auch außerhalb des Spiels viel mit dem Spiel und versucht sein System weiter zu verfeinern oder zu verbessern. Auf lange Sicht, häufen sich oftmals beachtliche Schuldenberge an. Passende weitere Nachteile für einen Charakter mit Spielsucht sind <i>Schulden</i>, <i>Feind</i> oder <i>Auf der Flucht</i>.<br/>Dem Charakter muss eine MEN-Probe (erschwert um den Rang des Nachteils) gelingen, um dem Drang zu widerstehen an einem Glücksspiel teilzunehmen oder das Spiel abzubrechen.<br/>Dieser Nachteil gilt für eine bestimmte Art von Spiel (Kartenspiele, Automatenspiele, Sportwetten) und kann prinzipiell auch mehrfach gewählt werden.', 1, 6),
            (64, 'Paranoia', -3, 2, 10, 'Sie verfolgen deinen Charakter und er weiß es. Jeder gehört zu ihnen. Alle sind Spitzel und Häscher, die gekommen sind, um ihn mitzunehmen...<br/>Der Charakter leidet an krankhaftem Verfolgungswahn. In allem und jedem erkennt er die böse Verschwörung oder seine Verfolger. Der Charakter wird nur schwer mit anderen zusammenarbeiten. Vertrauen wird er sowieso niemals jemandem. Fremde reagieren auf den Charakter mit Abneigung, wenn sie seine Störung erkennen. Man wird ihn für einen Spinner halten und entsprechend behandeln.<br/>SOZ-Proben, bei denen das Vertrauen in das Gegenüber eine zentrale Rolle spielt, werden um den Rang der Paranoia erschwert, sofern dem Charakter nicht zuvor eine MEN-Probe, erschwert um den Rang des Nachteils gelungen ist.<br/>Der Spielleiter kann dem Charakter Erleichterungen in Höhe des Nachteilsrangs auf Proben gewähren, die dazu dienen eine tatsächliche Verschwörung oder einen Hinterhalt aufzudecken.<br/>Übrigens: Paranoid zu sein heißt nicht, dass der Charakter nicht verfolgt wird...', 1, 6),
            (65, 'Zwangsstörung/Manie', -3, 2, 10, 'In der Psychatrie ist der Begriff der Monomanien, zu denen <i>Krankheitsbilder</i> wie Kleptomanie oder Pyromanie zählen, längst überkommen. Das liegt einfach daran, dass sich jedes <i>abnorme Verhalten</i> innerhalb dieses Betrachtungsrahmens als <i>Erkrankungung</i> definieren ließe.<br/>Fakt ist, es gibt durchaus <i>reizvolle</i> Zwangsstörungen, Neurosen und Manien, die einen Charakter einzigartig machen und auch eine rollenspielerische Herausforderung darstellen. Leidet der Charakter vielleicht an Kaufsucht, muss er sein Werkzeug der Größe nach sortieren, muss er sich dreimal überzeugen, dass die Türe wirklich verschlossen ist?<br/>Der Spieler kann eine MEN-Probe, erschwert um den Rang des Nachteils ablegen, wenn der Charakter gegen seine Natur handeln muss.', 1, 6),
            (66, 'Adelskodex', -3, 3, 10, '<b>Adel verpflichtet!</b> Der Charakter gehört dem Adel der Novaropäischen Allianz an und hat einen Eid geschworen, dem Kaiserreich und dem Kaiser, sowie seinem Lehnsherrn gegenüber treu zu sein.', 1, 6),
            (67, 'Hippokratischer Eid', -3, 2, 10, 'Der Begriff ist eher historisch, der Eid der Mediziner hat sich über die Jahrtausende in seinem Wortlaut mehrfach verändert. Viele Kernaussagen sind allerdings über die Zeitalter geblieben.', 1, 6),
            (68, 'Piratenkodex', -3, 3, 10, 'Hierbei handelt es sich um einen Satz ungeschriebener Regeln, an den sich Freibeuter und auch die meisten Piraten halten.<br/><i>Mord ist ein Verbrechen des Feiglings</i> - Töte nur, wenn es sich nicht vermeiden lässt. Niemand mag feige Mörder.<br/><i>Nimm deinem Opfer nicht das letzte Hemd</i> - Jeder muss irgendwie über die Runden kommen. Nimm einem Matrosen nicht das Goldkettchen, das er von seiner Frau geschenkt bekommen hat, halte dich lieber an die Goldbarren im Frachtraum.', 1, 6),
            (69, 'Logisches Verständnis', 6, 2, 7, 'Der Charakter verfügt über ein ausgeprägtes logisches Verständnis, für Proben bei denen es auf logisches Verständnis ankommt, wird die Probe um einen Punkt erleichtert.', 1, 3),
            (70, 'Rechenschwäche', -6, 2, 7, 'Der Charakter hat kein gutes Verständnis für logisch-mathematische Zusammenhänge. Pro Rang ist der MEN-Wert für derartige Proben um einen Punkt erschwert', 1, 3),
            (71, 'Intuitives Gespür', 6, 2, 7, 'Der Charakter hat ein gutes Bauchgefühl, für Proben in denen die richtigen Entscheidungen aus dem Bauch raus getroffen werden, erhält der Charakter eine Erleichterung um einen Punkt', 1, 3),
            (72, 'Festgefahrenes Denken', 6, 2, 7, 'Dem Charakter fällt es schwer aus dem Bauch heraus zu entscheiden, pro Rang in diesem Nachteil sind Proben bei denen es auf Intuition ankommt um einen Punkt erschwert.', 1, 3)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (73, 'Unaufmerksam', -9, 2, 4, 'Der Charakter lässt sich leicht ablenken. Er hat eine kurze Aufmerksamkeistsspanne und kann sich auch unter Gefahrensituationen nur schwer konzentrieren.<br/>MEN-Proben, bei denen Konzentration eine Rolle spielt, werden um einen Punkt erschwert.'),
            (74, 'Kühler Kopf', 9, 2, 10, 'Der Charakter behält auch in stressigsten Situationen die Nerven. Negative Modifikatoren für <i>Unter Beschuss</i> oder andere Stresssituationen werden für den Charakter um die Hälfte reduziert.'),
            (75, 'Zeitgefühl', 6, 2, 4, 'Der Charakter hat ein instinktives Zeitgefühl. Er kann Zeiträume sehr genau abschätzen und er prägt sich Tagesabläufe auf verschiedenen Planeten sehr schnell ein. Wird er z.B. mit verbundenen Augen einen Weg entlang geführt, kann er aufgrund der Laufdauer die Entfernung abschätzen etc.<br/>Das Zeitgefühl kann nur durch Phasen der Bewusstlosigkeit, Tiefschlafphasen oder extreme Ablenkungen und Psionische Effekte eingeschränkt werden.'),
            (76, '6. Sinn', 9, 2, 4, 'Der Charakter spürt instinktiv Gefahren oder Merkwürdigkeiten. Er bemerkt Hinterhalte, Fallen oder Geheimtüren leichter als ein normaler Charakter.<br/>Der Charakter darf jeden Wurf zum Erkennen eines Hinterhaltes oder eines verborgenen Gegenstandes wiederholen, als hätte er eine entsprechende Fertigkeit.<br/>Optional würfelt der Erzähler verdeckt für den Spieler und teilt ihm mit, was er sieht oder zu sehen glaubt.'),
            (77, 'Gesunder Menschenverstand', 6, 2, 4, 'Der Charakter verfügt über ein gutes Maß an Bauernschläue und steht mit beiden Beinen auf dem Boden. Einmal pro Spielsitzung sollte der Spielleiter den Spieler darauf hinweisen, dass sein Charakter – oder die Gruppe – gerade im Begriff ist, eine Dummheit zu begehen. Natürlich nur, wenn die Helden wirklich eine Dummheit begehen würden.'),
            (78, 'Hervorragender Astrogator', 9, 4, 4, 'Der Charakter ist ein herausragend guter Astrogator. Er kennt Sprungrouten, geheime Wege und einige Gefahrenquellen im Grenzland.<br/>Darüber hinaus hat er etwas, was man auch als <i>sechsten Sinn</i> bezeichnen könnte – ein intuitives Gespür für sichere und unsichere Sprünge.<br/>Astrogations-Proben werden für den Charakter um die Hälfte erleichtert (Minimum 1).'),
            (79, 'Meister des ...', 9, 2, 11, 'Der Charakter ist in einer bestimmten Fertigkeit besonders gut. Negative Modifikatoren werden beim Einsatz dieser Fähigkeit halbiert.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (79, 'Aktionsfertigkeit', 18, 0, 'Fertigkeitsgruppe', ''),
            (79, 'Wissensfertigkeit', 9, 0, 'Fertigkeitsgruppe', '')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (80, 'Meister des Klebebandes', 9, 2, 11, 'Der Charakter hat ein natürliches Gespür für technische Dinge. Auch ohne Studium kann er Dinge basteln und reparieren. Oder zumindest irgendetwas zusammenschustern, was den Zweck erfüllt. Zumindest eine Zeit lang...<br/>Klebeband, Kaugummi, Gummizüge, Reiszwecken und noch mehr Klebeband sind die Mittel der Wahl.<br/>Manche großen Raumschiffe fliegen nur, weil ein solcher Bastler an Bord ist. Beruhigend nicht wahr?<br/>Alle negativen Modifikatoren für <i>Fehlendes Wissen</i>, <i>Fehlendes Werkzeug</i> und <i>fehlende Ersatzteile</i> werden um die Hälfte (höchstens auf ein Minimum von 1) Reduziert.<br/>Und fehlende Ersatzteile können dann natürlich von anderen - weniger wichtigeren - Komponenten des Raumschiffes stammen.'),
            (81, 'Alpträume', -9, 2, 4, 'Der Charakter leidet unter Alpträumen und Schlaflosigkeit. Die Regeneration von EP und LP wird halbiert.'),
            (82, 'Blutrausch', -6, 2, 10, 'Wenn der Charakter getroffen wird, muss dem Charakter eine MEN-Probe gelingen. Diese Probe wird jedesmal, wenn der Charakter in der selben Szene/Kampf erneut getroffen wird wiederholt und um 1 Punkt erschwert.<br/>Misslingt die Probe, so verfällt der Charakter in einen Blutrausch. Er greift jedes Ziel in Reichweite an - unabhängig ob es sich dabei um Feinde oder Kameraden handelt.<br/>Während des Blutrausches spürt der Charakter keine Schmerzen. Wunden werden ignoriert. Betäubungsangriffe zählen nur, wenn sie genug Schaden verursachen um den Charakter sofort bewustlos werden zu lassen, ansonsten werden sie gänzlich ignoriert.<br/>Der Blutrausch endet wenn der Charakter kampfunfähig oder bewustlos wird oder wenn für eine Spielrunde keine Ziele in Reichweite sind.<br/>Der Spieler kann versuchen den Charakter unter Kontrolle zu bringen. Dazu steht ihm alle 3 Runden eine MEN-Probe zu. Sie beginnt mit dem selben Aufschlag, mit dem der Blutrausch begonnen hat und wird jedes mal um 1 Punkt erleichtert.'),
            (83, 'Reflexlähmung', -9, 2, 10, 'In Stressituationen neigt der Charakter dazu, <i>einzufrieren</i>. Er kann nichts tun. Angst und Stress lähmen ihn einfach.<br/>Wenn der Charakter einer ernsthaften, bedrohlichen Stresssituation ausgesetzt wird, muss er eine MEN-Probe ablegen, misslingt diese ist er handlungsunfähig. Alle 3 Kampfrunden kann der Spieler eine weitere MEN-Probe würfeln. Jede Probe nach der ersten wird um je einen Punkt erleichtert.'),
            (84, 'Schneller Alternd', -1, 1, 9, 'Der Charakter wird deutlicher früher sterben als der durchschnittliche Vertreter seiner Spezies, er wird früher Alt werden und entsprechende Alterserscheinungen wie Attributsverluste, Erschwernisse auf Wissensfertigkeitswürfe oder auch Krankheiten oder Abhängigkeit von Medikamenten werden sich früher einstellen.<br/>Hinweis: Einen nahezu unsterblichen Glasmenschen von Glibor zu spielen, der jetzt nicht mehr nach 9000 Jahren altert sondern schon nach 4500 Jahren... ist kein Nachteil.'),
            (85, 'Gutes Aussehen', 3, 3, 13, 'Der Charakter sieht (für seine Spezies) besonders gut oder eindrucksvoll aus. Alle Fertigkeitsproben, bei denen Aussehen eine Rolle spielt werden gegenüber Mitgliedern der eigenen Spezies um 1 Punkt erleichtert.'),
            (86, 'Hervorragendes Aussehen', 9, 3, 13, 'Das Aussehen des Charakters ist von solch blendender Schönheit, dass sogar Wesen von anderen Spezies beeindruckt werden. Alle Fertigkeitsproben bei denen das Aussehen eine Rolle spielt werden für Mitglieder der eigenen Spezies um 3 Punkte erleichtert; für andere Spezies kann der Spielleiter einen Bonus von 1 – 2 Punkten gewähren.'),
            (87, 'Beeindruckende Narbe', 3, 3, 13, 'Der Charakter hat eine eindrucksvolle Narbe oder ein ähnliches Kampfmal an einer offensichtlichen Stelle. Alle Proben auf Kommandieren, vor allem im militärischen Bereich werden um 1 Punkt erleichtert.<br/>Zudem kann man auf Festen interessante Geschichten vom Krieg zum Besten geben...'),
            (88, 'Beeindruckende Stimme', 3, 3, 13, 'Der Charakter kann mit einer klangvollen Stimme sprechen oder Singen und weiß sein Gegenüber durch Wortwahl und Stimmmodulation zu überzeugen. Fertigkeitswürfe bei denen die Stimme wichtig ist (Feilschen, Schauspiel, Singen, Überreden...) werden um 1 Punkt erleichtert.'),
            (89, 'Sprachfehler', -3, 3, 14, 'Der Charakter leidet an einem Sprachfehler. Lispeln, stottern, sich ständig verhaspeln oder irgendwelche Geräusche in einen Satz einflechten etc. gelten als solche.<br/>Auf jedenfall wirkt er auf sein Gegenüber seltsam. SOZ-Proben bei denen Sprache eine Rolle spielt, beispielsweise <i>Verhandlung</i>, können je nach Situation um einen Punkt erschwert werden.<br/>Lispeln ist kein Sprachfehler für Hazaru.'),
            (90, 'Sprachtalent', 6, 2, 14, 'Einem sprachtalentierten Charakter fällt es leicht, neue Sprachen zu erlernen und er kann sich bald schnell und flüssig verständigen.<br/>Das Erlernen einer neuen Sprache dauert nur die Hälfte der normalen Zeit. Bereits mit einem Fertigkeitswert von 1 stehen dem Charakter zwei Wiederholungswürfe für Proben zur Verfügung.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (91, 'Hohe Schmerztoleranz', 9, 1, 3, 'Der Charakter hat eine überdurchschnittlich hohe Schmerztoleranz. Negative Modifikatoren aufgrund von Verletzungen werden um 1 Punkt je Stufe reduziert.<br/>Bei Widerstand gegen Schmerzen - Folter, PSI-Effekte, etc, kann der Charakter  die Phy-Probe einmal je Stufe wiederholen als hätte er eine entsprechende Fertigkeit.',1 ,3)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (92, 'Auf der Flucht ', -3, 3, 15, 'Irgend jemand ist hinter dem Charakter her und der Charakter will unter allen Umständen vermeiden, von seinen Verfolgern erwischt zu werden.<br/>Er kann sich nie lange an einem Ort aufhalten und muss verdeckt reisen. Er ist gezwungen aufwändig seine Spuren zu verwischen und hat selten Zeit zum Ausruhen.<br/>Dieser Nachteil kann auch auf die Gruppe übertragen werden!<br/>Mit diesem Nachteil wird er nirgends auf Dauer Frieden finden. Der Verfolger hat genügend Mittel und Motivation um den Charakter über den Rand des bekannten Universums zu verfolgen Um den Status <i>Auf der Flucht</i> los zu werden, muss der Charakter sich seinem Nemesis stellen und diesen auf die ein oder andere Art überwinden.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (92, 'Aus den Augen verloren', -3, 0, 'Abstand:', 'Der Gegner weiß nicht viel über den momentanen Status des Charakters. Jedoch wird sein Spitzelnetz früher oder später Informationen zusammentragen, wenn sich der Charakter in einer zivilisierten Umgebung aufhält.'),
            (92, 'Auf den Fersen', -9, 0, 'Abstand:', 'Die Verfolger haben eine Spur die sie zumindest in den gleichen Subsektor führt. Es wird nicht lange dauern, bis sie dem Charakter auf den Fersen sind.'), 
            (92, 'im Nacken', -18, 0, 'Abstand:', 'Die Feinde sind dem Charakter dicht auf den Fersen. Er kann praktisch ihren Atem im Nacken spüren und selbst eine Höhle auf einem Asteroiden in diesem Gebiet ist zur Zeit kein ausreichendes Versteck.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (93, 'Barbar', -9, 2, 12, 'Der Charakter stammt von einer Welt, die technisch weit zurückliegt und hat noch nicht lange Kontakt mit raumfahrenden Völkern. Er kann anfangs keine Standardsprache, die Technologien die im Torweltenuniversum eigentlich zum Standard gehören sind für ihn Mysterien.<br/>Als Richtlinie kann man sich eine Kultur zwischen Bronzezeit und Hochmittelalter vorstellen. Bei der Charaktererschaffung können keine Fertigkeiten gewählt werden, die sich auf Technologien beziehen die in der Kultur des Charakters nicht vertreten sind.<br/>Beim Einsatz von Feuerwaffen, Triebwerken, Energiefeldern und anderen lauten unheimlichen Dingen bricht der Charakter anfangs in Panik aus. Selbst wenn er schonend darauf vorbereitet wird, muss ihm eine MEN-Probe gelingen um sich zurückzuhalten.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (94, 'Adelig', 1, 3, 15, 'Der Charakter entstammt einem adeligen Haus. Er trägt ein <i>von</i> im Namen oder das entsprechende Äquivalent seiner Kultur. Das sagt zwar nichts über die wahre Macht des Charakters aus oder seinen Reichtum, aber er gibt ihm dennoch Ruf.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (94, 'Adelige Abstammung', 6, 0, 'Bezeichnung:', 'Auch ohne Aussicht auf Erbe kann man auf eine Familie blicken, zu der man Kontakte hat und deren Namen man trägt.'),
            (94, 'nichterblicher Titularadel', 9, 0, 'Bezeichnung:', 'Dem Charakter wurde ein Titel verliehen, der ihn persönlich in den Adelsstand erhebt, jedoch nicht vererbt werden kann.'), 
            (94, 'erblicher Titualadel', 12, 0, 'Bezeichnung:', 'Ohne Ländereien oder Untertanen, jedoch ist der Charakter Teil eines Hauses, welches über Generationen bestehen kann und prinzipiell die Chance hat nach oben zu ehelichen.'),
            (94, 'echte Verantwortung', 15, 0, 'Bezeichnung:', 'Der Charakter ist Lehnsherr einer kleinen Länderei.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (95, 'Militärischer Rang', 1, 3, 15, 'Der Charakter ist oder war Mitglied in einer regulären Armee oder einer größeren Söldnergruppierung. Man hat <i>Kontakt</i> zu Mitgliedern der (ehemaligen) Einheit.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (95, 'Soldat', 1, 0, 'Bezeichnung:', 'Ein Mitglied der Mannschaften, ein ordentlicher Soldat ohne besondere Verantwortung'),
            (95, 'Unteroffizier', 3, 0, 'Bezeichnung:', 'Trupp- oder Gruppenführer mit Verantwortung über einige Untergebene'), 
            (95, 'Feldwebel', 6, 0, 'Bezeichnung:', 'Zugführer oder technische Spezialisten'),
            (95, 'Leutnant', 9, 0, 'Bezeichnung:', 'Offizier mit Verantwortung für Zug oder eine Staffel'), 
            (95, 'Hauptmann', 12, 0, 'Bezeichnung:', 'Kompanieführer oder soldaten mit technisch verantwortlichen Aufgabenbereiche'),
            (95, 'Oberst', 15, 0, 'Bezeichnung:', 'Kommandant eines größeren Verbands (auch Kapitäne großer Schiffe)'), 
            (95, 'General', 18, 0, 'Bezeichnung:', 'Kommandanten über Brigaden, Armeen oder Flotten'),
            (95, 'inaktiv', .5, 1, 'Status:', 'Der Charakter ist ehrenhaft aus dem Dienst ausgeschieden, Reservist oder außer Dienst'),
            (95, 'aktiv', 1, 1, 'Status:', 'Der Charakter gehört einer militärischen Einheit aktiv an.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (96, 'Wissenschaftlicher Rang', 1, 3, 15, 'Der Charakter hat ein Studium absolviert und war wissenschaftlich tätig. Mindestens seine Abschlussarbeit ist in Datalinks-Netzwerk auffindbar.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (96, 'Diplom/Master', 6, 0, 'Bezeichnung:', 'Der Charakter hat ein vollwertiges Studium absolviert und erfolgreich abgeschlossen.'),
            (96, 'Doktortitel', 9, 0, 'Bezeichnung:', 'Der Charakter war im Wissenschaftsbetrieb einer Universität aktiv und hat promoviert.'), 
            (96, 'Professur', 12, 0, 'Bezeichnung:', 'Nach mehreren Jahren an einer Universität wurde der Charakter zumindest vorübergehend zum Professor berufen.'),
            (96, 'Provinzuniversität', .666, 1, 'Ruf:', 'Der Titel wurde an einer einfachen Universität auf irgendeiner Kolonie erlangt. Die tatsächlichen Kenntnisse des Charakters ergeben sich aus seinen Fertigkeiten, hier geht es nur um den Ruf.'),
            (96, 'Große Universität', 1, 1, 'Ruf:', 'Eine der großen Universitäten in den Kernwelten oder einer Hauptstadt.'),
            (96, 'Eliteuniversität', 1.5, 1, 'Ruf:', 'Ein klangvoller Name und ein bekanntes Siegel prangen auf der Urkunde und verleihen ihr einen ganz besonderen Wert.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (97, 'Sonstige Titel', 1, 3, 15, 'In einigen Berufszweigen kommt man ohne Titel nicht weit. Teilweise darf man überhaupt nicht tätig werden oder man bewegt sich in einer Grauzone. Einige Beispiele:')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (97, 'Handwerksgeselle', 3, 0, 'Bezeichnung:', 'Der Charakter hat die Prüfung einer Handwerkskammer absolviert.'),
            (97, 'Handwerksmeister', 6, 0, 'Bezeichnung:', 'Mit einer Meisterprüfung vor einer Handwerkskammer oder Zunft hat der Charakter sein Können unter Beweis gestellt.'), 
            (97, 'Arzt (ohne Doktortitel)', 9, 0, 'Bezeichnung:', 'Der Charakter hat ein Medizinstudium an einer ordentlichen Universität absolviert.'), 
            (97, 'Arzt (mit Doktortitel)', 12, 0, 'Bezeichnung:', 'Nach dem Abschluss seines Medizinstudiums hat der Charakter noch einen Doktortitel erworben.'),
            (97, 'Ingenieur (ohne Doktortitel)', 9, 0, 'Bezeichnung:', 'Der Charakter hat ein technisches Studium erfolgreich absolviert.'), 
            (97, 'Ingenieur (mit Doktortitel)', 12, 0, 'Bezeichnung:', 'Neben dem technischen Studium kann der Charakter auch wissenschaftlich etwas vorweisen.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (98, 'Berühmt oder Berüchtigt', 1, 3, 15, 'Der Charakter trägt zwar keinen besonderen Titel, doch er ist aus anderen Gründen bekannt. Schauspieler, Schriftsteller, Sportler, bekannte Händler oder legendäre Archäologen. Natürlich ist es nicht immer von Vorteil, wenn man sofort erkannt wird.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO trait_vars(trait_id, name, xp_factor, oper, grp, txt) VALUES 
            (98, 'innerhalb einer Subkultur', 1, 0, 'Ausprägung:', 'Der Charakter ist innerhalb von Vertretern seines Tätigkeitsfeldes bekannt'),
            (98, 'allgemein bekannt', 3, 0, 'Ausprägung:', 'Der Charakter ist allgemein bekannt.'), 
            (98, 'lokale Bekanntheit', 1, 1, 'Verbreitung:', 'innerhalb einer größeren Stadt / Raumstation'),
            (98, 'regionale Bekanntheit', 2, 1, 'Verbreitung:', 'auf einer Welt / in einem Sternensystem'),
            (98, 'interstellare Bekanntheit', 6, 1, 'Verbreitung:', 'so weit das Datalinks-Netzwerk reicht')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt) VALUES
            (99, 'Diplomatische Immunität', 18, 3, 15, 'Der Charakter genießt diplomatische Immunität. Diese schützt ihn vor Strafverfolgung bei den meisten Delikten (Etwa zu schnelles Fahren, falsch parken etc.). Sein Gepäck darf nicht vom Zoll untersucht werden und Mitglieder der eigenen Botschaft müssen ihm Hilfestellung gewähren.<br/>Dieser Vorteil ist mit äußerster Vorsicht zu genießen. Er ist für besondere Kampagnen interessant und sollte dort <i>auf Zeit</i> vergeben werden.<br/>Dieser Vorteil bietet sich eher für spezielle Kampagnen an und sollte im Vorfeld vom Spielleiter abgesegnet werden.<br/>Man muss als Spieler auch bedenken, Diplomatische Immunität macht nicht kugelsicher und die eigene Regierung kann einen immer noch für alle Taten als Botschafter zur Rechenschaft ziehen.<br/>Voraussetzung: Mächtige Freunde, Adelig, oder Verpflichtung gegenüber einem Adligen Haus.')
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO traits (id, name, xp_cost, cls, grp, txt, min_rank, max_rank) VALUES
            (100, 'Geheimes Wissen', 1, 2, 16, 'Geheimes Wissen umfasst alle abgegrenzten Informationen die als geheim, oder der Öffentlichkeit nicht zugänglich betrachtet werden können.<br/>Dies kann beispielsweise das Wissen um die Koordinaten eines Schmugglerraumhafens bedeuten, oder den geheimen Handschlag der Loge bei der man Mitglied ist.<br/>Im Grunde geht es um sehr spezielle Fakten oder Informationen, die einem Charakter zur Verfügung ohne dass darauf eine Fertigkeitsprobe abgelegt werden müsste.<br/>Den <i>Wert</i> einer bestimmten Information bestimmt letztendlich der Spielleiter.',1, 18),
            (101, 'Startkapital', 1, 3, 17, 'Dieser Vorteil bestimmt die finanzielle Grundausstattung des Charakters, die im weiteren Verlauf der Charaktererschaffung für Ausrüstung ausgegeben werden kann. Für jeden Rang in diesem Vorteil stehen dem Charakter 1.000 Rand zur Verfügung', 1, 150),
            (102, 'Schulden', -1, 3, 17, 'Durch diesen Nachteil bekommst du kein Geld, aber du kannst du Kosten deines Startkapitals reduzieren. Natürlich kann man mehr Schulden haben als Startkapital. Für jeden Rang in dieser Eigenschaft schuldet der Charakter jemandem 2.000 Rand.',1, 150),
            (103, 'Regelmäßiges Einkommen', 1, 3, 17, 'Der Charakter verfügt über eine regelmäßige Geldquelle. Die genaue Natur muss mit dem Spielleiter vereinbart werden. Beispiele wären Aktienanteile, eine feste Arbeit, oder Unterstützung durch Eltern oder einen Gönner.<br/>Pro Charakterpunkt erhältst du 100 Rand / Megasekunde.<br/>Abhängig von der gespielten Kampagne, ist es natürlich durchaus möglich, dass es nicht so ganz einfach ist, an das Geld zu kommen. Das kann etwa der Fall sein, wenn man irgendwo, in einer ganz anderen Ecke der Galaxis unterwegs ist.<br/>Selbstverständlich ist es durchaus möglich, dass diese Geldquelle im Laufe des Spiels versiegt - Firmen können Pleite machen, Anstellungen kann man verlieren, Eltern können sich zerstreiten ...',1 , 20),
            (104, 'Zugriff auf Ausrüstung', 1, 3, 17, 'Manche Ausrüstung ist selten und steht am Anfang einer Abenteurerkarriere eigentlich nicht zur Verfügung. Dein Charakter besitzt jedoch einen solchen seltenen Gegenstand. Üblicherweise kann man nur Gegenstände erwerben, deren Verfügbarkeit bei +3 oder mehr liegt. Wie viele Vorteilspunkte der Zugriff auf einen Gegenstand kostet hängt natürlich auch von der gespielten Kampagne ab.<br/>Üblicherweise kostet <i>Zugriff auf Ausrüstung</i> einen Rang für jeden Punkt, den die Verfügbarkeit über +3 liegt.<br/>Natürlich muss so ein Besitz auch irgendwie begründet werden.<br/><i>Hab ich auf der Straße gefunden</i> kann vielleicht für ein Päckchen Drogen funktionieren, wird aber spätestens bei einer Servo-Rüstung oder einem Panzerfahrzeug etwas unglaubwürdig. Darüber hinaus muss die Ausrüstung zusätzlich mit Startkapital bezahlt werden.',1, 6),
            (105, 'Akteur', 1, 3, 17, 'Mit der Wahl dieses Vorteils, übt der Charakter <i>Kontrolle über eine Fraktion</i> aus. Jeder Punkt, der in <i>Akteur</i> investiert wird, zählt als Resourcenpunkt für die Erschaffung einer Fraktion - dabei kann dies als Gruppenvorteil, von mehreren Spielern gemeinsam gewählt werden. <i>Akteur</i> bietet sich an, wenn die Charakter die Geschicke einer Organisation oder Firma aktiv in die Hand nehmen wollen. Für eine stille Teilhaberschaft oder ein Aktienpaket ist der Vorteil <i>Regelmäßiges Einkommen</i> besser geeignet.', 1, 150),
            (106, 'Gesellschaftlicher Makel', 1, 3, 15, 'Der Charakter ist aus irgend einem Grund in seiner gewöhnlichen Gesellschaft nicht akzeptiert. Das kann verschiedenste Gründe haben. Die Höhe der Punkte für diesen Nachteil hängt davon ab, wie stark der Charakter in der Gesellschaft zurückgesetzt wird.<br/>Der Nachteil kann von <i>vernachlässigbar</i> (ein Mann auf einer matriarchischen Welt) für 1 Punkt bis hin zu <i>lebensbedrohlich</i> (ein Agent hinter feindlichen Linien) für 18 Punkte gehen.', 1, 18)
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            UPDATE traits
            SET effects='money:1000'
            WHERE id=101
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            INSERT INTO items (name, price,weight,avail,desc,cls,grp,equip_loc,weight_limit,extra_data) VALUES 
            ('Unterhose', 10, 100, -5, '', 'clothing', 'casual', 'hips', 0, 'var.Ausführung:Standard,Einweg#|var.Design:Standard,Spitzen#.p200|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*'),
            ('Boxershorts', 10, 100, -5, '', 'clothing', 'casual', 'hips', 0, 'var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*'),
            ('Unterhemd', 10, 100, -5, '', 'clothing', 'casual', 'torso', 0, 'var.Design:Standard,Spitzen#.p200|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*'),
            ('Hose', 40, 400, -5, '', 'clothing', 'casual.formal.work.sports', 'hips.legs', 200, 'var.Variante:Standard,Cargo#.p150.wl500,Anzug#.p200,Sport#|var.Länge:Standard,Kurze# |var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*|cnt:Hosentaschen'),
            ('Leggins', 20, 200, -5, '', 'clothing', 'casual.sports', 'hips.legs', 0, 'var.Material:Kunstfaser|col:*'),
            ('Rock', 30, 300, -5, '', 'clothing', 'casual.formal', 'hips.legs', 0, 'var.Schnitt:Standard,Falten#,Bleistift#,Wickel#,Ballon#|var.Länge:Standard,Mini#,Kurzer #,Midi#,Maxi#,Langer #|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*'),
            ('T-Shirt', 15, 300, -5, '', 'clothing', 'casual.sports', 'torso', 0, 'var.Material:Kunstfaser,Baumwolle.p120|col:*'),
            ('Hemd', 40, 300, -5, '', 'clothing', 'casual.work.formal', 'torso.arms', 100, 'var.Ärmel:Standard,Kurzärmliges #|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*|cnt:Hemdtasche'),
            ('Bluse', 40, 300, -5, '', 'clothing', 'casual.work.formal', 'torso.arms', 0, 'var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*'),
            ('Pullover', 50, 500, -5, '', 'clothing', 'casual.work', 'torso.arms', 0, 'var.Variante:Standard,Rollkragen#,Kaputzen#|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75|col:*'),
            ('Overall', 75, 1000, -5, '', 'clothing', 'casual.work', 'torso.arms.legs', 750, 'var.Ärmel:Standard,Kurzärmliger #,Ärmelloser #|var.Material:Kunstfaser,Baumwolle.p120|col:*|cnt:Overalltaschen'),
            ('Kittel', 50, 1000,0, '', 'clothing', 'work', 'torso.arms', 250, 'var.Ausführung:Standard,Labor#,Arbeits#|var.Material:Kunstfaser,Baumwolle.p120|col:*|cnt:Kitteltaschen'),
            ('Kleid', 50, 500, -5, '', 'clothing', 'casual.work.formal', 'torso.legs', 0, 'var.Variante:Standard,Abend#.p500,Gala#.p1000|var.Länge:Standard,Kurzes #,Langes #.p150.w150|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*'),
            ('Weste', 50, 500, -5, '', 'clothing', 'casual.work.sports', 'torso', 500, 'var.Variante:Standard,Sport#,Outdoor.p200.w200#,Thermo#.p300.w300,Anzug#.p200,Taktische #.w200.p200.d±0/+1,Kugelsichere #.w500.p1000.d±0/+2,Panzer#.w1000.p5000.d-1/+2|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*|cnt:Jackentaschen'),
            ('Jacke', 50, 500, -5, '', 'clothing', 'casual.work.sports', 'torso.arms', 500, 'var.Variante:Standard,Sport#,Outdoor.p200.w200#,Thermo#.p300.w300,Anzug#.p200,##Jackett.p200,##Sakko.p200,##Frack.p200|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*|cnt:Jackentaschen'),
            ('Mantel', 50, 750, -5, '', 'clothing', 'casual.work.sports', 'torso.arms', 500, 'var.Länge:Standard,Kurzer #,Langer #.p150.w150|var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*|cnt:Manteltaschen'),
            ('Krawatte', 20, 50, -5, '', 'clothing', 'work.formal', 'neck', 0, 'var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*'),
            ('Fliege', 20, 50, -5, '', 'clothing', 'work.formal', 'neck', 0, 'var.Material:Kunstfaser,Baumwolle.p120,Wolle.p200.w150,Seide.p250.w75,Jeans,Leder.p500.w250|col:*'),
            ('Sandalen', 20, 200, -5, '', 'clothing', '', '', 0, 'col:*'),
            ('Schuhe', 50, 500, -5, '', 'clothing', 'casual.formal.work.sports', 'feet', 0, 'var.Variante:Standard,Arbeits#.p200.w150,Sicherheits#.p200.w200,Sport#,Lauf#.w75,|col:*'),
            ('High-Heels', 50, 400, 0, '', 'clothing', 'casual.formal.work.sports', '', 0, 'col:*'),
            ('Stiefel', 100, 1000, 0, '', 'clothing', 'casual.work.sports', 'feet', 0, 'var.Variante:Standard,Arbeits#.w120,Outdoor#.p150.w120,Gummi#.p50.w150,Kampf#.p200.w150,Bord#.p250.w150,High-Heel-#|var.Höhe:Standard,Niedrige #,Hohe #|var.Material:Kunstfaser,Leder.p150,Gummi|col:*'),
            ('Kleidung, komplett', 150, 2000, -5, '', 'clothing', 'casual', 'torso.arms.legs.feet', 200, 'col:*|cnt:Hosentaschen'),
            ('Anzug, komplett', 350, 3000, -4, '', 'clothing', 'work.formal', 'torso.arms.legs.feet', 200, 'col:*|cnt:Hosentaschen'),
            ('Rucksack', 50, 500, 0, 'Praktisches Transportbehältnis, das am Rücken getragen werden kann. ', 'container', '', 'back', 10000, 'var.Ausführung:Standard,Wander#,Reise#,Outdoor#|var.Größe:Standard,Kleiner #.p50.w50.wl-5000,Großer #.p150.w200.wl10000|var.Verschluss:Schnallen,Reißverschluss,Knöpfe,Schnürung|var.Material:Kunstfaser,Leder.p250.w200|col:*'),
            ('Tasche', 20, 250, 0, 'Ein üblicherweise weichwandiges Transportbehältnis, das oft einen Verschluss besitzt und meist Trageschlaufen zum Transport besitzt. ', 'container', '', 'hands1', 10000, 'var.Ausführung:Standard,Reise#,Sport#,Einkaufs#.p10|var.Größe:Standard,Kleine #.p50.w50.wl-5000,Große #.p150.w200.wl10000|var.Verschluss:Keiner,Schnürung,Reißverschluss,Klettverschluss|var.Material:Plastik,Kunstfaser,Leder.p250.w200|col:*'),
            ('Holster', 20, 100, 0, 'Ein Behälter für Waffen oder Ausrüstungsgegenständen. ', 'container', '', 'belt', 1000, 'var.Ausführung:Standard,Werkzeug#,Waffen#,##Messerscheide,##Schwertscheide|var.Material:Kunstfaser,Leder.p250.w200|col:*'),
            ('Gürteltasche', 10, 100, 0, 'Eine Tasche, die üblicherweise am Gürtel befestigt wird. ', 'container', '', 'belt', 1000, 'var.Größe:Standard,Kleine #.wl-500,Große #.wl1000|var.Material:Kunstfaser,Leder.p250.w200|col:*'),
            ('Geldbeutel', 10, 50, 0, 'Zum Verstauen von Bargeld und wichtigen Dokumenten. ', 'container', '', '', 200, 'var.Material:Textil,Leder.p250.w200,Metall.p250.w150|col:*'),
            ('Seesack', 20, 250, 0, 'Eine üblicherweise zylindrisch geschnittene Tasche, die auf einer der kurzen Seiten über eine verschließbare Öffnung verfügt. Es gibt oft Trageriemen, an welchem der Sack über die Schulter gehängt werden kann. ', 'container', '', 'hands1', 10000, 'var.Größe:Standard,Kleiner #.p50.w50.wl-5000,Großer #.p150.w200.wl10000|var.Verschluss:Schnürung,Reißverschluss,Klettverschluss|var.Material:Kunstfaser,Baumwolle|col:*'),
            ('Koffer', 20, 1000, 0, 'Ein robustes Reisegepäckstück', 'container', '', 'hands1', 10000, 'var.Ausführung:Standard,Hartschalen#.p150.w200,##Trolley.p200.w250|var.Größe:Standard,Dokumenten#.w50.wl-7500,Akten#.w75.wl-7500,Kleiner #.p50.w50.wl-5000,Großer #.p150.w200.wl10000|var.Verschluss:Schnapper,Schnallen,Zahlenschloss,Schloss|var.Material:Kunststoff,Textil,Leder.p250.w200,Metall.p250.w200|col:*'),
            ('Kiste', 10, 500, 0, 'Ein stapelbarer fester Behälter', 'container', '', 'hands1', 10000, 'var.Größe:Standard,Kleine #.p50.w50.wl-5000,Große #.p150.w200.wl10000|var.Verschluss:Ohne Deckel,Formschlüssig,Klipse,Schnapper,verschließbare Schnapper|var.Material:Kunststoff,Holz.p250.w500,Metall.p250.w200|col:*'),
            ('Truhe', 20, 500, 0, 'Truhen sind grundsätzlich Kisten mit einem festmontierten Deckel.', 'container', '', 'hands1', 10000, 'var.Größe:Standard,Kleine #.p50.w50.wl-5000,Große #.p150.w200.wl10000|col:*'),
            ('Werkzeugkasten', 10, 500, 0, 'Ein Behälter für Werkzeuge aller Art. ', 'container', '', 'hands1', 2000, 'var.Variante:##Werkzeugmappe.w20.wl-1600,##Werkzeugkasten.p200,##Werkzeugkoffer.p300.w300.wl6000|var.Material:Textil,Kunststoff,Leder.p250.w125,Metall.p250.w150|col:*'),
            ('Axt', 50, 5000, -5, '', 'tool', '', 'hands2', 0, 'dmg:2/0'),
            ('Skalpell', 2, 20, -4, 'Medizinisches Messer mit einer rasiermesserscharfen Klinge', 'tool', 'knife', 'hands1', 0, 'dmg:1/0'),
            ('Cutter', 10, 50, -4, 'Die Handwerkervariante des Skalpells', 'tool', 'knife', 'hands1', 0, 'dmg:1/0'),
            ('Taschenmesser', 20, 100, -4, 'Ein Taschenmesser mit einigen nützlichen Werkzeugen. ', 'tool', 'knife', 'hands1', 0, 'dmg:1/0'),
            ('Multitool', 25, 200, -4, 'Ein größeres Taschenmesser mit Kombizange, Säge, Schraubendrehern. Es zählt für Reparaturen als Werkzeug!', 'tool', 'knife', 'hands1', 0, 'dmg:1/0'),
            ('Küchenmesser', 25, 100, -5, '', 'tool', 'knife', 'hands1', 0, 'dmg:1/0'),
            ('Streitaxt', 250, 2000, -2, 'Eine schwarfe Hiebwaffe die wirklich unschöne Verletzungen verursacht. Gegenüber einer Werkzeugaxt, ist die Waffe deutlich weniger kopflastig, sie lässt sich schneller schwingen.', 'weapon', 'blunt', '', 0, 'dmg:2/-1'),
            ('Kriegshammer', 250, 2000, -2, 'Im Gegensatz zur Streitaxt, endet diese Waffe mit einem relativ stumpfen Ende. Trotzdem möchte man nicht auf der falschen Seite dieser Waffe stehen.', 'weapon', 'blunt', '', 0, 'dmg:2/0'),
            ('Streitkolben', 250, 2000, -2, 'Das hier ist die professionelle Variante des Knüppels. Metallbeschläge und scharfe Kanten machen aus einem ohnehin schon gefährlichen Gegenstand eine tödliche Waffe.', 'weapon', 'blunt', '', 0, 'dmg:2/0'),
            ('Morgenstern', 250, 2000, -2, 'In seiner Grundform handelt es sich bei dieser Waffe um einen stachelbewehrten Streitkolben, in einer abgewandelten Variante, befinden sich eine oder mehrere stachelbewehrte Kugeln, mit Ketten befestigt, frei schwingend am Ende des Stabes', 'weapon', 'blunt', '', 0, 'dmg:2/0'),
            ('Knüppel', 50, 1000, -5, 'Eine stumpfe Hiebwaffe', 'weapon', 'blunt', '', 0, 'dmg:2/1'),
            ('Vorschlaghammer', 50, 5000, -2, '', 'weapon', 'blunt', '', 0, 'dmg:3/0'),
            ('Betäubungsstab', 100, 1000, -2, '', 'weapon', 'blunt', '', 0, 'dmg:3/-1/E'),
            ('Taktisches Messer', 50, 150, -3, '', 'weapon', 'knife', '', 0, 'dmg:1/-1'),
            ('Vibromesser', 250, 250, 0, '', 'weapon', 'knife', '', 0, 'dmg:2/-1'),
            ('Kurzdolch', 50, 250, -3, '', 'weapon', 'knife', '', 0, 'dmg:1/-1'),
            ('Stilett', 100, 400, 0, '', 'weapon', 'knife', '', 0, 'dmg:1/-2'),
            ('Katar', 150, 250, 2, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Szizsan', 350, 600, -1, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Schattenklinge', 4500, 800, 3, '', 'weapon', 'blade', '', 0, 'dmg:4/-1'),
            ('Breitschwert', 500, 1400, -2, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Jian', 500, 1200, 1, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Entermesser', 200, 800, -2, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Infanteriesäbel', 400, 1200, -1, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Katana', 500, 1500, 0, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Szimitar', 500, 1500, 1, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('Laserschwert', 2000, 1000, 3, '', 'weapon', 'blade', '', 0, 'dmg:3/-2'),
            ('Schlagring', 25, 200, -3, '', 'weapon', 'blunt', '', 0, 'dmg:1/0'),
            ('Schockhandschuh', 150, 250, -2, '', 'weapon', 'stun', '', 0, 'dmg:3/-2/E'),
            ('Krallenhandschuhe', 150, 250, 0, '', 'weapon', 'blade', '', 0, 'dmg:2/0'),
            ('OMG Alpha 9mm', 350, 1200, -3, '', 'weapon', 'gun', '', 1000, 'caliber:pistol.medium|dist:15.50|dmg:2/0|chambers:1|clip:0'),
            ('F&DW Söldner', 400, 850, -2, '', 'weapon', 'gun', '', 1000, 'caliber:pistol.medium|dist:10.40|dmg:2/0|chambers:1|clip:0'),
            ('ParaTec Vulpus .38', 300, 1250, -3, '', 'weapon', 'gun.revolver', '', 1000, 'dist:20.60|dmg:2/0|chambers:6'),
            ('ParaTec Ursus .357', 500, 1250, -3, '', 'weapon', 'gun', '', 1000, 'dist:10.50|dmg:2/-1|chambers:1|clip:0'),
            ('ParaTec Ursus .500', 800, 1750, -1, '', 'weapon', 'gun', '', 1000, 'dist:15.75|dmg:3/0|chambers:1|clip:0'),
            ('ParaTec Ursus 12/76', 1200, 2050, 1, '', 'weapon', 'gun', '', 1000, 'dist:15.75|dmg:3/-1|chambers:1|clip:0'),
            ('II .357 Marshal Art', 750, 1250, -3, '', 'weapon', 'gun.revolver', '', 1000, 'dist:10.50|dmg:2/-1|chambers:6'),
            ('OMG Gamma 7.62', 1050, 3500, -1, '', 'weapon', 'rifle', '', 1000, 'dist:50.250|dmg:3/-2|chambers:1|clip:0'),
            ('Paratec Lupus', 950, 3500, 0, '', 'weapon', 'rifle', '', 1000, 'dist:50.250|dmg:2/-2|chambers:1|clip:0'),
            ('Insight Industries Ambassador .223', 1850, 3400, 2, '', 'weapon', 'rifle', '', 1000, 'dist:50.250|dmg:2/-2|chambers:1|clip:0'),
            ('Furthmayr & DeWal Musketier .308', 2150, 3800, 1, '', 'weapon', 'rifle', '', 1000, 'dist:50.250|dmg:3/-2|chambers:1|clip:0'),
            ('Paratec Gulo', 800, 4000, -2, '', 'weapon', 'shotgun', '', 1000, 'dist:20.50|dmg:3/1|chambers:1|clip:0'),
            ('Furthmayr & DeWal Wildschütz', 900, 4800, -2, '', 'weapon', 'shotgun', '', 1000, 'dist:20.50|dmg:3/1|chambers:1|clip:0'),
            ('OMG Beta 9mm', 750, 2800, -2, '', 'weapon', 'gun.automatic', '', 1000, 'dist:25.75|dmg:2/0|chambers:1|clip:0'),
            ('SMI Cadenca 9mm', 650, 2400, -2, '', 'weapon', 'gun.automatic', '', 1000, 'dist:25.75|dmg:2/0|chambers:1|clip:0'),
            ('Paratec Canis 9mm', 500, 2400, -2, '', 'weapon', 'gun.automatic', '', 1000, 'dist:25.75|dmg:2/0|chambers:1|clip:0'),
            ('OMG Delta 7.62', 1500, 4500, 0, '', 'weapon', 'rifle.automatic', '', 1000, 'dist:50.250|dmg:3/-2|chambers:1|clip:0'),
            ('AK 47', 1050, 3500, 0, '', 'weapon', 'rifle.automatic', '', 1000, 'dist:50.250|dmg:3/-2|chambers:1|clip:0'),
            ('Pulspistole SMI RC 1', 500, 1500, -1, '', 'weapon', 'gun.blaster', '', 1000, 'dist:25.150|dmg:2/-2/E|chambers:1'),
            ('Pulsgewehr SMI RC 2', 1200, 2800, -1, '', 'weapon', 'rifle.blaster', '', 1000, 'dist:50.500|dmg:3/-2/E|chambers:1'),
            ('Laserpistole SMI Prism', 650, 1400, -1, '', 'weapon', 'gun.blaster', '', 1000, 'dist:25.150|dmg:2/-1|chambers:1'),
            ('Lasergewehr ParaTec Lynx', 1300, 3500, 1, '', 'weapon', 'rifle.blaster', '', 1000, 'dist:800|dmg:3/-2|chambers:1'),
            ('Ko Punt Gun', 10000, 50000, -3, '', 'weapon', '', '', 1000, 'dist:100.5000|dmg:5/-6|chambers:1'),
            ('Pistolenmagazin', 20, 50, -4, '', 'clipsnmore', 'clips', '', 500, 'var.Kaliber:leicht.w75.p50,mittel,schwer.w150.p150|var.Kapazität:kleines #.c7.w90,mittleres #.c15,langes #.c30.w125.p150|caliber:pistol.'),
            ('Gewehrmagazin', 30, 100, -4, '', 'clipsnmore', 'clips', '', 500, 'var.Kaliber:leicht.w75.p50,mittel,schwer.w150.p150|var.Kapazität:kleines #.c7.w90,mittleres #.c15,langes #.c30.w125.p150|caliber:rifle.'),
            ('Hohlspitzgeschoss', 0.5, 10, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.d+0/-1.w200|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:2/0'),
            ('SecurGlas™-Geschoss', 0.5, 10, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.d+0/-1.w200|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:2/+1'),
            ('Vollmantelgeschoss', 1, 10, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.d+0/-1.w200|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:2/-1'),
            ('Panzerbrechendes Geschoss', 5, 15, -2, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.d+0/-1.w200|var.Kaliber:mittel,schwer.p150.w200.d+1/+0|dmg:2/-2'),
            ('Leuchtspurgeschoss', 3, 15, -1, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.w200,Schrotflinten.p250.w300|var.Kaliber:mittel,schwer.p150.w200.d+1/+0|dmg:3/0'),
            ('Signalpatrone', 5, 20, -3, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.w200,Schrotflinten.p250.w300|var.Kaliber:mittel,schwer.p150.w200.d+1/+0|dmg:3/+1'),
            ('Platzpatrone', 1, 7, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.w200,Schrotflinten.p250.w300|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:1/+1'),
            ('Gummigeschoss', 2, 20, -2, '', 'ammo', '', '', 0, 'var.Waffentyp:Faustfeuerwaffen,Langwaffen.p200.w200,Schrotflinten.p250.w300|var.Kaliber:mittel,schwer.p150.w200.d+1/+0|dmg:3/0/E'),
            ('Flintenlaufgeschoss', 3, 50, -2, '', 'ammo', '', '', 0, 'var.Waffentyp:Schrotflinten|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:3/-2'),
            ('Birdshotpatrone', 1, 50, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Schrotflinten|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:3/+2'),
            ('Buckshotpatrone', 1, 50, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Schrotflinten|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:3/+1'),
            ('Buckshotpatrone SecurGlas™', 1, 50, -4, '', 'ammo', '', '', 0, 'var.Waffentyp:Schrotflinten|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:3/+2'),
            ('Salzpatrone', 0.5, 50, -2, '', 'ammo', '', '', 0, 'var.Waffentyp:Schrotflinten|var.Kaliber:leicht.p75.w50.d-1/+0,mittel,schwer.p150.w200.d+1/+0|dmg:3/+2')
        """.trimIndent()
        db.execSQL(sql)

        /*
        sql = """

        """.trimIndent()
        db.execSQL(sql)
        */
        Log.d("info", "... tables created")
    }

    companion object {
        val VERSION = 1
        val DB_NAME = "torwelten.db"
    }
}