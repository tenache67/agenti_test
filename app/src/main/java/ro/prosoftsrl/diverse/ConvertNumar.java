package ro.prosoftsrl.diverse;

import android.util.Log;
import ro.prosoftsrl.agenti.Biz;

public class ConvertNumar {
    public static String convert(Double nNumar ) {
    String sInt ="";
    String sZec  = "";
    String sCuvint = "";
    nNumar =Biz.round(nNumar, 2); 
    String sNumar=Siruri.str(nNumar, 15, 2);
    // Log.d("CONV",sNumar.length()+" "+sNumar);
    if (sNumar.indexOf(".")>=0) { 
        sInt = sNumar.substring(0,sNumar.indexOf("."));
        sZec = Siruri.padR(sNumar.substring(sNumar.indexOf(".")+1,sNumar.length()),2,"0");
    } else {
        sInt = sNumar;
        sZec = "";
    }

    if (sInt.length() <= 12 ) {
        sInt = Siruri.padL(sInt, 12, " ") ;
        sCuvint = miliarde(sInt.substring(0, 3));
        sCuvint = sCuvint + milioane(sInt.substring(3,3+3));
        sCuvint = sCuvint + mii(sInt.substring(6,6+ 3));
        sCuvint = sCuvint + sute(sInt.substring(9,9+3));
        sCuvint = sCuvint + " lei";
        if (!sZec.equals("")) {
            sCuvint = sCuvint + " si " ;
            sCuvint = sCuvint + zeci(sZec);
            sCuvint = sCuvint + " bani";
        }
    } else {
    	sCuvint = Biz.round(nNumar, 2) + "(Numar prea mare)" ;
    }
    return sCuvint ;
}

private static String miliarde(String sSir ) {
    String miliarde = "";
    if(!sSir.equals("   "))
        if(sSir.equals("  1")) 
            miliarde = "unmiliard";
        else
            miliarde = sute(sSir) + "miliarde";
    return miliarde;
}

private static String milioane(String sSir) {
    String milioane = "";
    if (!sSir.equals("   "))
        if (sSir.equals("  1"))
            milioane = "unmilion";
        else
            milioane = sute(sSir) + "milioane";
    return milioane;
}
    
private static String mii(String sSir ) {
    String mii = "";
    if (!sSir.equals("   "))
        if (sSir.equals("  1"))
            mii = "omie";
        else
            mii = sute(sSir) + "mii";
    return mii;
}

private static String sute(String sSir ) {
    String sute = "";
    // se extrage cifra de la stinga
    sute = cifre(sSir.substring(0, 1), 3) + zeci(sSir.substring(1,1+ 2));
    return sute ;
}

private static String zeci(String sSir ) {
    String zeci = "";
    //ssir are lungime 2 car si reprezinta zecile
    if (sSir.substring(0, 1).equals(" ")) {
        zeci = cifre(sSir.substring(1, 2), 1);
    } else if (sSir.substring(0, 1).equals("0")) {
        zeci=cifre(sSir.substring(1, 2), 1);
    } else if (sSir.substring(0, 1).equals("1")) {
    	if (sSir.substring(1,2).equals("0"))
    		zeci="zece";
    	else 
    		zeci=cifre(sSir.substring(1,2), 2) + "sprezece" ;
    } else if (sSir.substring(0, 1).equals("2")) {
        if (sSir.substring(1,2).equals("0"))
        	zeci = "douazeci";
        else
            zeci = "douazecisi" + cifre(sSir.substring(1, 2), 1);
    } else {
        zeci = cifre(sSir.substring(0, 1), 2) + "zecisi" + cifre(sSir.substring(1, 2), 1);
    }
   return zeci ;
}
    
private static String cifre(String sSir , int nPoz ) {
    String sOut  = "";
    String sCifra="";
    String sAtribut=(nPoz==3) ? "sute" : "" ;
    if (sSir.trim().length()!=0) {
        if (!sSir.equals("0")) {
            if (sSir.equals("1")) {
                if (nPoz==3) {
                    sCifra = "o";
                    sAtribut="suta";
                }
                else sCifra="unu";
            }
            else if (sSir.equals("2")) {
                if (nPoz==3) sCifra="doua";
                else sCifra="doi";
            }
            else if (sSir.equals("3")) sCifra = "trei";
            else if (sSir.equals("4")) sCifra = "patru";
            else if (sSir.equals("5"))  sCifra = "cinci";
            else if (sSir.equals("6"))  sCifra = "sase";
            else if (sSir.equals("7"))  sCifra = "sapte";
            else if (sSir.equals("8"))  sCifra = "opt";
            else if (sSir.equals("9"))  sCifra = "noua";
            sOut=sCifra+sAtribut;
        }
    }


/*
    if (sSir.equals(" ")) ;
    else if (sSir.equals("0")) ;
    else if (sSir.equals("1")) {
    	if (nPoz==3) sOut="osuta";
    	else sOut="unu";
    }
    else if (sSir.equals("2")) {
    	if (nPoz==3) sOut="douasute";
    	else sOut="doi";    	
    }
    else if (sSir.equals("3")) sOut = "trei";
    else if (sSir.equals("4")) sOut = "patru"; 
    else if (sSir.equals("5"))  {
        sOut = "cinci";
    }
    else if (sSir.equals("6"))  sOut = "sase";
    else if (sSir.equals("7"))  sOut = "sapte";
    else if (sSir.equals("8"))  sOut = "opt";
    else if (sSir.equals("9"))  sOut = "noua";
*/

    return sOut;
	}
}
