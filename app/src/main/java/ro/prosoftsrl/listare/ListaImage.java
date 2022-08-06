package ro.prosoftsrl.listare;

import java.util.ArrayList;

// obiectul ce contine structura paginii 
public class ListaImage {
	int nLinii;
	int nColoane;
	int nLeft;
	int nTop;
	private String pad="                                                                                                    "+
			"                                                                                                    ";
	private int id=0;
	String[] img ;
	ArrayList <ListaRand> texte;


	public ListaImage(int nLinii, int nColoane, int nLeft, int nTop) {
		super();
		this.nLinii = nLinii; // reprezinta numarul de linii inclusiv nTop
		this.nColoane = nColoane; // reprezinta numarul de coloane inclusiv nleft
		this.nLeft = nLeft;
		this.nTop = nTop;
		img=new String[nLinii];
		for (int i = 0; i < img.length; i++) {
			img[i]="";
		}
		texte=new ArrayList<ListaRand>();
	}
	
	public void adText (int nTop, int nLeft,String text,Boolean laBaza) {
		adText(nTop, nLeft, 0, text,laBaza);
	}

	public void adText (int nTop, int nLeft, int nLat ,String text) {
		adText(nTop, nLeft, nLat, text,false);
	}
	
	// adauga un text in lista de texte. laBaza - daca este .t. se aliniaza fata de baza imaginii
	public void adText (int nTop, int nLeft, int nLat, String text, Boolean laBaza) {
		id=id+1;
		String sir= text==null ? " " : text;
		if (nLat!=0)
			//latimea mai mica decat textul
			if (nLat<=sir.length())
				sir=sir.substring(0,nLat);
			else 
				sir=sir+pad.substring(0, nLat-sir.length());
		if (laBaza)	nTop=nLinii-nTop+1;
		if (nTop > (this.nLinii-this.nTop)) nTop=this.nLinii-this.nTop;
		texte.add( new ListaRand(nTop, nLeft, nLat, sir));
	}
	// creeaza arrayul de srtinguri ce reprezinta imaginea
	public String[] format () {
		ordoneaza();
		int k=0;
		String linie="";
		int colcrt;
		int lincrt;
		while (k<texte.size()) {
			lincrt=texte.get(k).nTop;
			colcrt=1;
			linie="";
			while (k<texte.size() && (lincrt==texte.get(k).nTop)) {
				
				if (texte.get(k).nLeft>colcrt) 
					linie=linie+pad.substring(0,texte.get(k).nLeft-colcrt )+texte.get(k).text;
				else
					linie=linie.substring(0, texte.get(k).nLeft-1)+texte.get(k).text;
				colcrt=linie.length()+1;
				k=k+1;
			}
			img[lincrt+this.nTop-1]=linie;
		}
		for (int i = 0; i < img.length; i++) {
			// marginea din stanga
			img[i]=pad.substring(0, this.nLeft)+img[i];
			if (img[i].length()>(this.nColoane-this.nLeft))
				img[i]=img[i].substring(0,this.nColoane-this.nLeft);
		}
		return img;
	}
	
	// se ordoneaza elementele in arraylist in ordinea de linii si coloane
	private void ordoneaza () {
		Boolean lTermin=false;
		ListaRand ant=null;
		ListaRand crt=null;
		int pas=1;
		while (! lTermin && pas<100) {
			lTermin=true;
			ant=null;
			for (int i = 0; i < texte.size(); i++) {
				if (i>0) ant=texte.get(i-1);
				crt=texte.get(i);
				if (ant!=null) {
					if ((ant.nTop > crt.nTop) ||( (ant.nTop==crt.nTop) && (ant.nLeft>crt.nLeft)) ) {
						texte.set(i-1,crt);
						texte.set(i, ant);
						lTermin=false;
					}		
				}
			}
			pas=pas+1;
		}
	}
	
}
