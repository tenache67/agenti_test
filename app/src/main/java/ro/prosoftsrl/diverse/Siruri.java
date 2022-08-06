package ro.prosoftsrl.diverse;

import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import ro.prosoftsrl.agenti.Biz;

public class Siruri {
	// genereaza un sir pt folosit la functii
	public static String pad(){
		return "          "+"          "+"          "+"          "+"          "+"          "+
		"          "+"          "+"          "+"          "+"          "+"          "+	
		"          "+"          "+"          "+"          "+"          "+"          "+
		"          "+"          "+"          "+"          "+"          "+"          "+
		"          "+"          "+"          "+"          "+"          "+"          "+
		"          "+"          "+"          "+"          "+"          "+"          "; 
	}
	public static String replicate(String sir, int nLung) {
		return pad().substring(0,nLung).replace(" ", sir);
	}
	public static String padL (String sir, int nLung, String sPad) {
		return sir.length()<nLung ? Siruri.replicate(sPad, nLung-sir.length())+sir : sir.substring(0,nLung) ;
	}

	public static String padL (String sir, int nLung) {
		return Siruri.padL(sir, nLung, " ");
		//return sir.length()<nLung ? Siruri.replicate(" ", nLung-sir.length())+sir : sir ;
	}

	public static String padR (String sir, int nLung, String sPad) {
		return sir.length()<nLung ? sir+Siruri.replicate(sPad, nLung-sir.length()) : sir.substring(0,nLung) ;
	}
	public static String padR (String sir, int nLung) {
		return Siruri.padR(sir, nLung, " ");
		//return sir.length()<nLung ? sir+Siruri.replicate(" ", nLung-sir.length()) : sir ;
	}
	
	
	// transforma un numar in sir 
	public static String str(double num, int nLung, int nZec) {
		String sir=String.valueOf(Biz.round(num, nZec));
		// Log.d("PRO&", "Numar: " + sir);
		if (nZec>0) {
			String zecimal=Siruri.padR(sir.substring(sir.indexOf(".")+1, sir.length()),nZec,"0");
			String intreg=padL(sir.substring(0,sir.indexOf(".")),nLung-zecimal.length()-1);
			return intreg+"."+zecimal;
		} 
		else {
			String intreg = padL(sir.substring(0, sir.indexOf(".")), nLung);
			return intreg ;
		}
	}

	public static String dtos(Calendar c, String sep) {
		return c.get(Calendar.YEAR) +sep+ Siruri.padL((c.get(Calendar.MONTH)+1)+"",2,"0") +sep+ Siruri.padL(c.get(Calendar.DAY_OF_MONTH)+"",2,"0") ;
	}
	
	public static String dtos (Calendar c) {
		return dtos(c,"");
		//return c.get(Calendar.YEAR) + ""+ Siruri.padL((c.get(Calendar.MONTH)+1)+"",2,"0") + c.get(Calendar.DAY_OF_MONTH) ;		
	}

	public static String dtoc (Calendar c) {
		return c.get(Calendar.DAY_OF_MONTH) + "-" + (c.get(Calendar.MONTH)+1)+ "-" + c.get(Calendar.YEAR) ;		
	}
	// determina o data din sir de forma yyyy-mm-zz sau dd-mm-yyyy
	// daca nu este - in sir este de forma yyyymmzz
	public static Calendar cTod (String d ) {
		Calendar c = Calendar.getInstance();
		try {
			if (d.indexOf("-")>0)
					// incepe cu an
				c.set(Integer.valueOf(d.substring(0,4)),Integer.valueOf(d.substring(5, 7))-1,Integer.valueOf(d.substring(8,10)));
			else
				c.set(Integer.valueOf(d.substring(0,4)),Integer.valueOf(d.substring(4, 6))-1,Integer.valueOf(d.substring(6,8)));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}
	// transforma cod ascii in sir
	public static String chr(int num) {
		return Character.toString ((char) num);
	}
	
	public static int getIntDinString(String sval) {
		int iRez=0;
		try {
			iRez=Integer.valueOf(sval);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iRez;
	}
	
	//determina valoare din imagine sirului 
	public static double getDoubleDinString(String sSir){
		double nval=0.0;
		try {
			nval=Double.valueOf(sSir);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return nval;
	}
	
	// determina numar din data calendaristica dupa modelul yyyymmzz
	public static int getNrAnLunaZi (Calendar d) {
		return d.get(Calendar.YEAR)*10000+(d.get(Calendar.MONTH)+1)*100+d.get(Calendar.DAY_OF_MONTH);
	}

	public static Calendar getDateTime() {
		return Calendar.getInstance(TimeZone.getDefault());
	}
	
	public static String ttos(Calendar t) {
		return dtos(t, "-")+" "+t.get(Calendar.HOUR_OF_DAY)+":"+t.get(Calendar.MINUTE)+":"+t.get(Calendar.SECOND);
	}


    // se extrage un subsir din sSir conform parametrului nAlign
    // nAlign =0 - aliniere la stanga 1 - dreapta 2 - centru
    // nRow - numarul randului
    /*public static String alignSir ( String sSir, int nRow, int nAlign, int nWidth ) {
        String sRez="";
        if (nWidth>=sSir.length()) {
            sSir=padR(sSir,nWidth);
        } else {

        }
        return sRez;
    }*/

    // exemple de cautare a unui subsir in sir
//	String text = "0123hello9012hello8901hello7890";
//	String word = "hello";
//
//System.out.println(text.indexOf(word)); // prints "4"
//System.out.println(text.lastIndexOf(word)); // prints "22"
//
//// find all occurrences forward
//for (int i = -1; (i = text.indexOf(word, i + 1)) != -1; i++) {
//		System.out.println(i);
//	} // prints "4", "13", "22"
//
//// find all occurrences backward
//for (int i = text.length(); (i = text.lastIndexOf(word, i - 1)) != -1; i++) {
//		System.out.println(i);
//	} // prints "22", "13", "4"
	// pentru listare , la pozitiile suplimentare pt promotii la betty se poate da linia de felul urmator
	// <text> C:=<cantitate> P:=<pret>
	// functia intoarce una dintre partile din pozitie : se da parametrul secundar T,C,sau P
	public static String getSuplimPart(String cSir, String cPart) {
		String cText=cSir.concat(" ");
		int nIndexC=cText.toUpperCase().indexOf("C:=");
		int nIndexP=cText.toUpperCase().indexOf("P:=");
		int nLastIndex=cText.length() ;
		if (nIndexC>=0 && nIndexP>=0) nLastIndex=Math.min(nIndexC,nIndexP);
		else if (nIndexC<0 && nIndexP>=0) nLastIndex=nIndexP ;
		else if (nIndexC>=0 && nIndexP<0) nLastIndex=nIndexC ;
		String cRez="";
		switch (cPart){
			case "T": // text
				cRez=cText.substring(0,nLastIndex).trim();
				break;
			case "C": // cantitate
				if (nIndexC>=0)
					cRez=cText.substring(nIndexC+3,cText.indexOf(" ",nIndexC)).trim();
					cRez=Siruri.padL(cRez,5);
				break;
			case "P": // pret
				if (nIndexP>=0)
					cRez=cText.substring(nIndexP+3,cText.indexOf(" ",nIndexP)).trim();
					cRez=Siruri.padL(cRez,7);
				break;
		}

		return cRez;
	};

}

