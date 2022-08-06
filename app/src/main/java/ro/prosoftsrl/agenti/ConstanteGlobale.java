package ro.prosoftsrl.agenti;

public abstract class ConstanteGlobale {
	public static abstract class Actiuni_la_documente {
		// eticheta de folosit in bundle pt argumente
		public static final String ETICHETA_ACTIUNE="actiune_document";
		public static  final int ARATA_LISTA_ARTICOLE=1;
		public static final int SCHIMBA_TITLU=2 ;
		// se apeleaza la inchiderea listei de alegere la articole
		public static final int INCHIDE_LISTA_PRODUSE_CONTINUT=3;
		// se apeleaza la inchiderea dialogului pentru cantitate aleasa din lista de articole
		public static final int SET_CANTITATE_LA_DOCUMENT=4;
		// transmite la documenteActivity semnalul de inchidere
		public static final int INCHIDE_DOCUMENT=5;
		// transmite la activity rspunsul pozitiv de la dialog general 
		public static final int DIALOG_GENERAL_POZITIV=6;
		// transmite la activity rspunsul negativ de la dialog general 
		public static final int DIALOG_GENERAL_NEGATIV=7;
		// transmite la activity rspunsul neutru de la dialog general 
		public static final int DIALOG_GENERAL_NEUTRU=8;
		// afiseaza un cursor sub forma de tabela . se foloseste la rapoarte 
		// in arg se primeste "sqlsir" la care se gaseste instr sql pt query
		public static final int RAPOARTE_LIST_TABELA=9;
		// Vine de la dialogul aleger sablon . in arg se primesc  long id_ruta si id_cursa
		public static final int ALEGE_SABLON=10;
		// se apeleaza atunci cand se face click pe pozitia din document
		public static final int SET_CANTITATE_IN_LINIE=11;
		// se apeleaza din dialogul de initializare continut din sablon
		public static final int ADAUGA_DOCUMENT_DIN_SABLON=12;
		// se apeleaza din dialogul de initializare continut din sablon
		public static final int ADAUGA_DOCUMENT_FARA_SABLON=13;
		// se transmite flagul lFaraIncasare in cazul in care o factura a fost marcata cu incasare pe loc dar nu 
		// s-au incasat practic banii
		public static final int FACTURA_PE_LOC_FARA_INCAS=14;
		// setare agent activ pentru comenzi online
		public static final int ALEGE_AGENT=14;
		// arata fragmentul pentru ambalaje
		public static final int ARATA_AMBALAJE=15;
		// inchide fragmentul de ambalaje cu salvare
		public static final int INCHIDE_AMBALAJE_CU_SALVARE=16;
		// inchide fragmentul de ambalaje fara salvare
		public static final int INCHIDE_AMBALAJE_FARA_SALVARE=17;
		// actiune pozitiva la intrebarea daca se contiuna listarea nereusita a bonului sau a documentului 
		public static final int RELISTEAZA_DOCUMENT=18;
		// actiune negativa la intrebarea daca se contiuna listarea nereusita a bonului sau a documentului
		public static final int NU_RELISTEAZA_DOCUMENT=19;
        // la acesata actiune se activeaza fragmentul cu dialogul general
        public static final int ARATA_DIALOG_GENERAL=20;

	}
	
	public static abstract class Parametri_versiune {
		public static final int VERSIUNE_BETTY=5 ; // betty
	}
	
	public static abstract class Tipuri_case_marcat {
		public static final int DATECS_DP_05=1 ;
		public static final int ACTIVA_MOBILE_EJ=2;
	}
	
	
	// optiunile care apar la accesarea unui client din lista de clienti sau 
	// din alte liste similare ( produse , aviz inc desc)
	public static abstract class Optiuni_ListaClienti {
		public static final int ADAUGA_DOCUMENT=1 ; 
		public static final int ISTORIE_DOCUMENTE=2 ;
		public static final int ADAUGA_CLIENT=3 ;
		public static final int MODIFICA_CLIENT=4 ;
        public static final int BLOCHEAZA_CLIENT=13 ;
		public static final int RENUNTA=5 ;
		public static final int SABLON_CERERE_MARFA=8 ; // sablon cerere
		public static final int ACTUAL_AMBALAJE=12; // actualizare ambalaje
        public static final int ADAUGA_INCASARE=13;
		// pentru meniu incarcare desc
		public static final int GEN_AVIZ_INC=6 ; // in optiuni apare LISTA PRODUSE
		public static final int GEN_AVIZ_STOC_0=7 ; // aviz descarcare stoc
		// pentru articole
		public static final int ADAUGA_ARTICOL=9 ;
		public static final int MODIFICA_ARTICOL=10 ;
		public static final int GEN_TRANSFER_AMANUNT=11 ; // aviz descarcare stoc
		
		public static final long[] ID_OPTIUNI_CLIENT = 
			{Optiuni_ListaClienti.ADAUGA_DOCUMENT ,
			Optiuni_ListaClienti.ISTORIE_DOCUMENTE ,
			Optiuni_ListaClienti.SABLON_CERERE_MARFA ,
			Optiuni_ListaClienti.ACTUAL_AMBALAJE ,
			Optiuni_ListaClienti.RENUNTA 
			};
        public static final long[] ID_OPTIUNI_CLIENT_SOROLI =
                {Optiuni_ListaClienti.ADAUGA_DOCUMENT ,
                 Optiuni_ListaClienti.ISTORIE_DOCUMENTE ,
                 Optiuni_ListaClienti.ADAUGA_INCASARE,
                 Optiuni_ListaClienti.SABLON_CERERE_MARFA ,
                 Optiuni_ListaClienti.ADAUGA_CLIENT,
                 Optiuni_ListaClienti.MODIFICA_CLIENT,
                 Optiuni_ListaClienti.BLOCHEAZA_CLIENT,
                 Optiuni_ListaClienti.RENUNTA
                };

        public static final long[] ID_OPTIUNI_CLIENT_COMENZI_ONLINE =
			{Optiuni_ListaClienti.SABLON_CERERE_MARFA,
			Optiuni_ListaClienti.RENUNTA 
			};
		
		public static final long[] ID_OPTIUNI_INC_DESC = 
			{Optiuni_ListaClienti.ADAUGA_DOCUMENT ,
			Optiuni_ListaClienti.ISTORIE_DOCUMENTE ,
			Optiuni_ListaClienti.GEN_AVIZ_INC ,
			Optiuni_ListaClienti.GEN_AVIZ_STOC_0 ,
			Optiuni_ListaClienti.RENUNTA 
			};

		public static final long[] ID_OPTIUNI_TRANSFER_AMANUNT = 
			{
			Optiuni_ListaClienti.GEN_TRANSFER_AMANUNT ,
			Optiuni_ListaClienti.RENUNTA 
			};
	
		public static final long[] ID_OPTIUNI_ARTICOLE = 
			{Optiuni_ListaClienti.ADAUGA_ARTICOL ,
			Optiuni_ListaClienti.MODIFICA_ARTICOL ,
			Optiuni_ListaClienti.RENUNTA 
			};

		
	}
	// parametrul care se trimite la "tipdialog" in arg la dialog si care determina diferite actiuni in dialog
	public static abstract class TipDialogListaDenumiri {
		public static final int OPT_LISTA_CLIENTI=1 ; 
		public static final int OPT_ISTORIE_DOCUMENTE=2 ;
		public static final int OPT_INC_DESC=3 ;
		public static final int OPT_LISTA_PRODUSE=4 ;		
	}
    public static abstract class TipDialogGeneral {
        // etichetele sunt pt a transmite parametrii catre dialog si mai departe
        public static final String DIALOG_ETICHETA_TIP="tipdialog" ;
        public static final String DIALOG_ETICHETA_VALOARE_INI1="valoare_ini1" ;
        public static final String DIALOG_ETICHETA_VALOARE_INI2="valoare_ini2" ;
        public static final String DIALOG_ETICHETA_VALOARE_INI3="valoare_ini3" ;
        public static final String DIALOG_ETICHETA_VALOARE_RETUR1="valoare_retur1" ;
        public static final String DIALOG_ETICHETA_VALOARE_RETUR2="valoare_retur2" ;
        public static final String DIALOG_ETICHETA_VALOARE_RETUR3="valoare_retur3" ;
        public static final String DIALOG_ETICHETA_ID_EXPEDITOR="id_expeditor" ;

        public static final int DIALOG_CERE_PAROLA=1 ;
        public static final int DIALOG_CERE_DATA=2 ;
    }
	public static abstract class Mesaje {
		// se trimite dupa preluarea cu succes a sablonului din server 
		public static final int SUCCES_PREIA_SABLON=1 ;
		// in acest mesaj in campul val_intreg se salveaza si idul de antet
		public static final int SUCCES_SALVEAZA_SABLON=2 ;
	}
}
