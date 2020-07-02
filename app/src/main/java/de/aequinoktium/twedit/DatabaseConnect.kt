package de.aequinoktium.twedit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseConnect(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("Info", "Create DB")
        first_run(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
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
                xp_used INT DEFAULT 0,
                xp_total INT DEFAULT 0
            );
        """.trimIndent()
        db.execSQL(sql)

        sql = """
            CREATE TABLE char_info (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                char_id INT,
                species VARCHAR(255),
                sex VARCHAR(255),
                age INT,
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
                lvl INT,
                FOREIGN KEY (char_id) REFERENCES char_core(id),
                FOREIGN KEY (skill_id) REFERENCES skills(id)
            );
        """.trimIndent()
        db.execSQL(sql)

        // add core data ...
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

        Log.d("info", "... tables created")
    }

    companion object {
        val VERSION = 1
        val DB_NAME = "torwelten.db"
    }
}