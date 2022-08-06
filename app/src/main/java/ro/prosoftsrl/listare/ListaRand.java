package ro.prosoftsrl.listare;
// descrie un rand din lista 
public class ListaRand {
	int nLeft; // pozitia la stanga (coloana)
	int nTop; // pozitia sus (randul)
	int nLat; // latimea permisa ( daca nu se da sau e 0 nu se tine seama)
	String text; // textul de printat
	public ListaRand(int nTop, int nLeft, int nLat, String text) {
		super();
		this.nLeft = nLeft;
		this.nTop = nTop;
		this.nLat = nLat;
		this.text = text;
	}
}
