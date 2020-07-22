package de.aequinoktium.twedit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


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

        db.execSQL("DROP TABLE IF EXISTS 'char_traits'")
        db.execSQL("DROP TABLE IF EXISTS 'trait_vars'")
        db.execSQL("DROP TABLE IF EXISTS 'traits'")
        db.execSQL("DROP TABLE IF EXISTS 'trait_grp'")
        db.execSQL("DROP TABLE IF EXISTS 'traits_cls'")
        db.execSQL("DROP TABLE IF EXISTS 'char_skills'")
        db.execSQL("DROP TABLE IF EXISTS 'skills'")
        db.execSQL("DROP TABLE IF EXISTS 'char_desc'")
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
                xp_used INT DEFAULT 0,
                xp_total INT DEFAULT 0,
                career_mode BOOLEAN DEFAULT FALSE
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                concept VARCHAR(255) DEFAULT "",
                species VARCHAR(255) DEFAULT "",
                culture VARCHAR(255) DEFAULT "",
                homeworld VARCHAR(255) DEFAULT "",
                sex VARCHAR(255) DEFAULT "",
                age INT DEFAULT 0,
                FOREIGN KEY (char_id) REFERENCES char_core(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_desc (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                skin_type INT DEFAULT 0,
                skin_color VARCHAR(255) DEFAULT "",
                eye_color VARCHAR(255) DEFAULT "",
                height INT DEFAULT 0,
                weight INT DEFAULT 0,
                appearance TEXT DEFAULT "",
                FOREIGN KEY (char_id) REFERENCES char_core(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE skills (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(128) NOT NULL UNIQUE,
                icon VARCHAR(255),
                base_skill INT,
                skill INT,
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
                signature_skill BOOLEAN DEFAULT FALSE, 
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
                var1_id INT,
                var2_id INT,
                var3_id INT,
                var4_id INT,
                xp_cost INT,
                name VARCHAR(255),
                txt TEXT,
                is_reduced BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (var1_id) REFERENCES trait_vars(id), 
                FOREIGN KEY (var2_id) REFERENCES trait_vars(id), 
                FOREIGN KEY (var3_id) REFERENCES trait_vars(id), 
                FOREIGN KEY (var4_id) REFERENCES trait_vars(id), 
                FOREIGN KEY (char_id) REFERENCES char_core(id),
                FOREIGN KEY (trait_id) REFERENCES traits(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        // add skill data ...
        sql = """
             INSERT INTO skills (id, name, base_skill, skill, is_active) VALUES 
             (10000,'Athletik',0,0,1),
             (10100,'Klettern',10000,10100,1),
             (10101,'Hochgebirgsklettern',10000,10100,1),
             (10102,'Freiklettern',10000,10100,1),
             (10103,'Fassadenklettern',10000,10100,1),
             (10104,'Höhlenklettern',10000,10100,1),
             (10200,'Reiten',10000,10200,1),
             (10300,'Schwimmen',10000,10300,1),
             (10301,'Rettungsschwimmen',10000,10300,1),
             (10302,'Gerätetauchen',10000,10300,1),
             (10400,'Fliegen',10000,10400,1),
             (10401,'Gleitschirme',10000,10400,1),
             (10402,'Flügel',10000,10400,1),
             (10500,'Überleben',10000,10500,1),
             (20000,'Geschick',0,0,1),
             (20100,'Beweglichkeit',20000,20100,1),
             (20101,'Entfesseln',20000,20100,1),
             (20102,'Winden',20000,20100,1),
             (20200,'Körperkontrolle',20000,20200,1),
             (20201,'Balance',20000,20200,1),
             (20202,'0G-Manöver',20000,20200,1),
             (20300,'Tanzen',20000,20300,1),
             (20400,'Schleichen',20000,20400,1),
             (20401,'Verfolgen',20000,20400,1),
             (20402,'sich verstecken',20000,20400,1),
             (30000,'Fingerfertigkeit',0,0,1),
             (30100,'Medizin und Erste Hilfe',30000,30100,1),
             (30101,'Diagnose',30000,30100,1),
             (30102,'Erste Hilfe',30000,30100,1),
             (30103,'Chirurgie',30000,30100,1),
             (30104,'Behandlung',30000,30100,1),
             (30200,'Bauen und Reparieren',30000,30200,1),
             (30201,'Maschinen- und Anlagenbau',30000,30200,1),
             (30202,'Reaktoren und XS-Systeme',30000,30200,1),
             (30203,'Computer und Elektronik',30000,30200,1),
             (30204,'Metallbau',30000,30200,1),
             (30205,'Schlösser knacken',30000,30200,1),
             (30300,'Malen und Zeichnen',30000,30300,1),
             (30301,'Illustration',30000,30300,1),
             (30302,'Karten zeichnen',30000,30300,1),
             (30303,'Kalligraphie',30000,30300,1),
             (30304,'Technisches Zeichnen',30000,30300,1),
             (30400,'Modellieren',30000,30400,1),
             (30401,'Steinmetz',30000,30400,1),
             (30402,'Maskenbildner',30000,30400,1),
             (30500,'Taschenspielerei',30000,30500,1),
             (30501,'Kartentricks',30000,30500,1),
             (30502,'Jonglieren',30000,30500,1),
             (30503,'Taschendiebstahl',30000,30500,1),
             (40000,'Organisation und Verwaltung',0,0,1),
             (40100,'Administration',40000,40100,1),
             (40101,'Kalkulation und Buchführung',40000,40100,1),
             (40102,'Personaleinsatzplanung',40000,40100,1),
             (40200,'Handel',40000,40200,1),
             (40201,'Schätzen',40000,40200,1),
             (40202,'Schwarzmarkt',40000,40200,1),
             (40300,'Ladung und Fracht',40000,40300,1),
             (40301,'Packen und Stauen',40000,40300,1),
             (40302,'Gefahrgut',40000,40300,1),
             (40303,'Lagerverwaltung',40000,40300,1),
             (40304,'Expedition ausrüsten',40000,40300,1),
             (40400,'Führung',40000,40400,1),
             (40401,'Kommando',40000,40400,1),
             (50000,'Computer & Technik',0,0,1),
             (50100,'Computer',50000,50100,1),
             (50101,'Computernutzung',50000,50100,1),
             (50102,'Informationsrecherche',50000,50100,1),
             (50103,'Programmierung',50000,50100,1),
             (50104,'Hacking',50000,50100,1),
             (50200,'Sensorsysteme',50000,50200,1),
             (50201,'Radar',50000,50200,1),
             (50202,'Sonar',50000,50200,1),
             (50300,'Schiffssysteme',50000,50300,1),
             (50301,'Reaktorsteuerung',50000,50300,1),
             (50302,'Lebenserhaltung',50000,50300,1),
             (60000,'Forschung und Wissenschaft',0,0,1),
             (60100,'Experimente und Laborarbeit',60000,60100,1),
             (60101,'Wissenschaftliche Methodik',60000,60100,1),
             (60102,'Dokumentation',60000,60100,1),
             (60103,'Forensik',60000,60100,1),
             (60200,'Archäologie',60000,60200,1),
             (60300,'Recherche',60000,60300,1),
             (60301,'Datalinks',60000,60300,1),
             (60302,'Bibliotheken ',60000,60300,1),
             (70000,'Pilot',0,0,1),
             (70100,'Bodenfahrzeuge',70000,70100,1),
             (70101,'Motorräder und Quads',70000,70100,1),
             (70102,'Automobile',70000,70100,1),
             (70103,'Lastwagen',70000,70100,1),
             (70104,'Baumaschinen und schweres Gerät',70000,70100,1),
             (70105,'Hovercrafts und Schweber',70000,70100,1),
             (70200,'Luftfahrzeuge',70000,70200,1),
             (70201,'Flugzeuge',70000,70200,1),
             (70202,'Schweber',70000,70200,1),
             (70300,'Raumschiffe',70000,70300,1),
             (70301,'Kampfmanöver',70000,70300,1),
             (70302,'Asteroidenflug',70000,70300,1),
             (70303,'Planetare Landungen',70000,70300,1),
             (70400,'Schiffe',70000,70400,1),
             (70401,'Segeln',70000,70400,1),
             (70402,'Motorboote',70000,70400,1),
             (70500,'Navigation',70000,70500,1),
             (70501,'Karten lesen',70000,70500,1),
             (70502,'Routenplanung',70000,70500,1),
             (70600,'Astrogation',70000,70600,1),
             (70601,'Torverbindungen',70000,70600,1),
             (70602,'Schleichwege',70000,70600,1),
             (70603,'Gefechtsastrogation',70000,70600,1),
             (70604,'Asteroidenfelder',70000,70600,1),
             (80000,'Kommunikation',0,0,1),
             (80100,'Verhandlungen',80000,80100,1),
             (80101,'Feilschen',80000,80100,1),
             (80102,'Vertragsgestaltung',80000,80100,1),
             (80200,'Überreden',80000,80200,1),
             (80201,'Totlabern',80000,80200,1),
             (80202,'Einschüchtern',80000,80200,1),
             (80300,'Überzeugen',80000,80300,1),
             (80301,'Verführen',80000,80300,1),
             (80302,'Propaganda',80000,80300,1),
             (80400,'Lügen',80000,80400,1),
             (80500,'Ausfragen',80000,80500,1),
             (80501,'Verhör',80000,80500,1),
             (80502,'Lügen erkennen',80000,80500,1),
             (80503,'Folter',80000,80500,1),
             (90000,'Lehren & Unterrichten',0,0,1),
             (90100,'Lehrer',90000,90100,1),
             (90101,'Didaktik',90000,90100,1),
             (90200,'Trainer',90000,90200,1),
             (90201,'G-Ball Coach',90000,90200,1),
             (90202,'Fahrlehrer',90000,90200,1),
             (90300,'Professor',90000,90300,1),
             (90400,'Militärausbilder',90000,90400,1),
             (90401,'Drill-Sergeant',90000,90400,1),
             (90402,'Schießausbilder',90000,90400,1),
             (100000,'Schauspielerei',0,0,1),
             (100100,'Verkleiden',100000,100100,1),
             (100101,'MakeUp',100000,100100,1),
             (100102,'Kostüm',100000,100100,1),
             (100200,'Imitation',100000,100200,1),
             (100201,'Stimmen imitieren',100000,100200,1),
             (100202,'Dialekte und Lingos',100000,100200,1),
             (100203,'Bewegungsabläufe',100000,100200,1),
             (110000,'Etikette und Gebräuche',0,0,1),
             (110100,'Höfisches Benehmen',110000,110100,1),
             (110200,'Militärisches Verhalten',110000,110200,1),
             (120000,'Unbewaffneter Nahkampf',0,0,1),
             (120100,'Raufen',120000,120100,1),
             (120200,'Boxen',120000,120200,1),
             (120300,'Kung Fu',120000,120300,1),
             (130000,'Klingenwaffen',0,0,1),
             (130100,'Messer und Dolche',130000,130100,1),
             (130200,'Kurzschwerter',130000,130200,1),
             (130300,'Langschwerter',130000,130300,1),
             (130400,'Bidenhänder',130000,130400,1),
             (130500,'Säbel und Krummschwerter',130000,130500,1),
             (140000,'Hiebwaffen',0,0,1),
             (140100,'Knüppel und improvisierte Hiebwaffen',140000,140100,1),
             (140101,'Baseballschläger',140000,140100,1),
             (140102,'Kurzstöcke',140000,140100,1),
             (140103,'Teleskopschlagstock',140000,140100,1),
             (140200,'Beile und Hämmer',140000,140200,1),
             (140201,'Vorschlaghämmer',140000,140200,1),
             (140202,'Streitkolben',140000,140200,1),
             (140203,'Äxte',140000,140200,1),
             (140300,'Stangenwaffen',140000,140300,1),
             (140301,'Kampfstäbe',140000,140300,1),
             (140302,'Hellebarden',140000,140300,1),
             (140303,'Speere',140000,140300,1),
             (150000,'Pistolen und Revolver',0,0,1),
             (150100,'Pistolen',150000,150100,1),
             (150200,'Revolver',150000,150200,1),
             (150400,'Maschinenpistolen',150000,150400,1),
             (160000,'Gewehre',0,0,1),
             (160100,'Schrotflinten',160000,160100,1),
             (160200,'Flinten',160000,160200,1),
             (160300,'Scharfschützengewehre',160000,160300,1),
             (160400,'Sturmgewehre',160000,160400,1),
             (170000,'Strahlenwaffen',0,0,1),
             (170100,'Strahlenpistolen',170000,170100,1),
             (170200,'Strahlengewehre',170000,170200,1),
             (180000,'Schwere Waffen',0,0,1),
             (180100,'Maschinengewehre',180000,180100,1),
             (180200,'Maschinenkanonen',180000,180200,1),
             (180300,'Raketen- und Granatwerfer',180000,180300,1),
             (190000,'Geschütze',0,0,1),
             (190100,'Artillerie und Fahrzeugkanonen',190000,190100,1),
             (190200,'Bordgeschütz',190000,190200,1),
             (190300,'Invasionsabwehrgeschütz',190000,190300,1),
             (200000,'Sprühtanks',0,0,1),
             (200100,'Flammenwerfer und Säuretanks',200000,200100,1),
             (210000,'Wurfwaffen',0,0,1),
             (210100,'Wurfklingen',210000,210100,1),
             (210101,'Messer',210000,210100,1),
             (210102,'Beile',210000,210100,1),
             (210103,'Wurfsterne',210000,210100,1),
             (210200,'Schleudern',210000,210200,1),
             (210201,'Schleuder',210000,210200,1),
             (210202,'Bola',210000,210200,1),
             (210300,'Netz',210000,210300,1),
             (210400,'Granaten',210000,210400,1),
             (210401,'Handgranaten',210000,210400,1),
             (210402,'Rauchgranaten',210000,210400,1),
             (210403,'Schockgranaten',210000,210400,1),
             (220000,'Naturwissenschaften und Technologie',0,0,0),
             (220100,'Biologie',220000,220100,0),
             (220101,'Zellbiologie',220000,220100,0),
             (220102,'Genetik',220000,220100,0),
             (220103,'Tierkunde',220000,220100,0),
             (220104,'Pflanzenkunde',220000,220100,0),
             (220105,'Biologie der Hazaru',220000,220100,0),
             (220106,'Humanbiologie',220000,220100,0),
             (220107,'Stellarbiologie',220000,220100,0),
             (220108,'Biospären',220000,220100,0),
             (220109,'Gaja-Hypothese',220000,220100,0),
             (220200,'Chemie',220000,220200,0),
             (220201,'Chemische Analyse und Synthese',220000,220200,0),
             (220202,'Biochemie',220000,220200,0),
             (220203,'Sprengstoffherstellung',220000,220200,0),
             (220204,'Pharmazie',220000,220200,0),
             (220205,'Drogenproduktion',220000,220200,0),
             (220206,'Materialkunde',220000,220200,0),
             (220207,'Nanochemie',220000,220200,0),
             (220208,'Materialforschung',220000,220200,0),
             (220300,'Medizin',220000,220300,0),
             (220301,'Anatomie',220000,220300,0),
             (220302,'Neurologie',220000,220300,0),
             (220303,'Psychologie',220000,220300,0),
             (220304,'Mensch-Maschine-Integration',220000,220300,0),
             (220305,'Psionik',220000,220300,0),
             (220400,'Mathematik',220000,220400,0),
             (220401,'Logik',220000,220400,0),
             (220402,'Algebra',220000,220400,0),
             (220403,'Geometrie',220000,220400,0),
             (220404,'Statistik',220000,220400,0),
             (220405,'Wirtschaftsmathematik',220000,220400,0),
             (220406,'Technische Mathematik',220000,220400,0),
             (220500,'Physik',220000,220500,0),
             (220501,'Mechanik',220000,220500,0),
             (220502,'Elektrodynamik',220000,220500,0),
             (220503,'Thermodynamik',220000,220500,0),
             (220504,'Relativitätstheorie',220000,220500,0),
             (220505,'Astrophysik',220000,220500,0),
             (220506,'Quantenphysik',220000,220500,0),
             (220507,'Extraspatialphysik',220000,220500,0),
             (220600,'Technologie und Ingenieurwissenschaften',220000,220600,0),
             (220601,'Maschinenbau',220000,220600,0),
             (220602,'Feinmechanik',220000,220600,0),
             (220603,'Waffentechnik',220000,220600,0),
             (220604,'Elektrik und Energietechnik',220000,220600,0),
             (220605,'Elektronik und Computer',220000,220600,0),
             (220606,'Baustatik',220000,220600,0),
             (220607,'Umweltsysteme',220000,220600,0),
             (220608,'Raumschiffbau',220000,220600,0),
             (220700,'Geowissenschaften',220000,220700,0),
             (220701,'Geologie',220000,220700,0),
             (220702,'Tektonik und Vulkanologie',220000,220700,0),
             (220703,'Metereologie',220000,220700,0),
             (220704,'Kartographie',220000,220700,0),
             (220705,'Terraforming',220000,220700,0),
             (230000,'Gesellschafts- und Sozialwissenschaften',0,0,0),
             (230100,'Geschichte',230000,230100,0),
             (230200,'Politik',230000,230200,0),
             (230201,'Gesellschaftsentwürfe',230000,230200,0),
             (230202,'Regierungsformen',230000,230200,0),
             (230300,'Rechtswissenschaften',230000,230300,0),
             (230400,'Theologie',230000,230400,0),
             (230500,'Wirtschaftswissenschaften',230000,230500,0),
             (230501,'Buchführung',230000,230500,0),
             (230502,'Unternehmensführung',230000,230500,0),
             (230503,'Bankwesen',230000,230500,0),
             (230504,'Finanzierung',230000,230500,0),
             (240000,'Sprachen und Linguistik',0,0,0),
             (240100,'Sprache ',240000,240100,0),
             (240101,'Alte Dialekte',240000,240100,0),
             (240102,'Schriftlicher Ausdruck',240000,240100,0),
             (240103,'Etymologie',240000,240100,0),
             (240104,'Regionale Dialekte',240000,240100,0),
             (240200,'Novababel',240000,240200,0),
             (240201,'Technobabel ',240000,240200,0),
             (240202,'AlphaGanimed-Babel ',240000,240200,0),
             (240300,'Novasperanto',240000,240300,0),
             (240400,'Anglesh',240000,240400,0),
             (240500,'Araby',240000,240500,0),
             (240600,'Gotick',240000,240600,0),
             (240700,'Españal',240000,240700,0),
             (240800,'Franças',240000,240800,0),
             (240900,'Latin',240000,240900,0),
             (241000,'Helleniki',241000,241000,0),
             (241100,'Nippon',241000,241100,0),
             (241200,'Rosska',241000,241200,0),
             (241300,'Konklav-Hazaru',241000,241300,0),
             (241400,'Szezaru',241000,241400,0),
             (241500,'Chetra',241000,241500,0),
             (241600,'Řhu',241000,241600,0),
             (241700,'Orionischer Standard ',241000,241700,0),
             (250000,'Straßenwissen und Allgemeines Wissen',0,0,0),
             (250100,'Livestyle und Mode',250000,250100,0),
             (250200,'Speisen und Getränke',250000,250200,0),
             (250300,'Kunst',250000,250300,0),
             (250301,'Musik (Künstler/Kultur/Epoche)',250000,250300,0),
             (250302,'Malerei (Künstler/Kultur/Epoche)',250000,250300,0),
             (250303,'Theater (Künstler/Kultur/Epoche)',250000,250300,0),
             (250400,'Mythen der Grenzwelten',250000,250400,0),
             (250500,'Sport und Sportwetten',250000,250500,0),
             (250600,'Spiele und Glücksspiele',250000,250600,0),
             (260000,'Psychokinetik',0,0,1),
             (260100,'Heben',260000,260100,1),
             (260200,'Levitation',260000,260200,1),
             (260300,'Stoppen',260000,260300,1),
             (270000,'Telepathie',0,0,1),
             (270100,'Gedankenlesen',270000,270100,1),
             (270200,'Emotionen fühlen',270000,270200,1),
             (280000,'Thermische Effekte',0,0,1),
             (280100,'Entzünden',280000,280100,1),
             (280200,'Kochen',280000,280200,1),
             (280300,'Metall erhitzen',280000,280300,1),
             (280400,'Schockfrosten',280000,280400,1);
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