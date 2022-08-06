package ro.prosoftsrl.agenti;
// clasa pentru descrierea unui element dintr-o lista , cum ar fi cea de l meniul principal
public class ListViewOption {
	String sPrompt ="";
	String sDescriere ="";
	int iIdImage=0 ;
	String sActivity =""; // numele activitatii care va fi pornita la apasare
	int iILD=0; // tipul de lista ce va fi reprezentat
	
	

	public ListViewOption (String sPrompt , int iIdImage) {
			this.sPrompt=sPrompt;
			this.iIdImage=iIdImage;
		}

	public ListViewOption (String sPrompt , String sDescriere ,int iIdImage) {
		this.sDescriere=sDescriere ;
		this.sPrompt=sPrompt;
		this.iIdImage=iIdImage;	
	}
	
	public ListViewOption (String sPrompt , String sDescriere ,int iIdImage, String sActivity) {
		this.sDescriere=sDescriere ;
		this.sPrompt=sPrompt;
		this.iIdImage=iIdImage;	
		this.sActivity=sActivity;
	}
	
	public ListViewOption (String sPrompt , String sDescriere ,int iIdImage, String sActivity, int iTLD) {
		this.sDescriere=sDescriere ;
		this.sPrompt=sPrompt;
		this.iIdImage=iIdImage;	
		this.sActivity=sActivity;
		this.iILD=iTLD;
	}

	@Override
	public String  toString () {
		return this.sPrompt ;
	}
	public String toDescriere() {
		return this.sDescriere ;
	}
	public int toImgid () {
		return this.iIdImage ;
	}
}
