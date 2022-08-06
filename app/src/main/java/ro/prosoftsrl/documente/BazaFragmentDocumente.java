package ro.prosoftsrl.documente;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import ro.prosoftsrl.agenti.FragmentReceiveActionsInterface;
import ro.prosoftsrl.agenti.R;

/**

 */
public class BazaFragmentDocumente extends Fragment
        implements FragmentReceiveActionsInterface,
        AdapterView.OnItemSelectedListener
{


    public BazaFragmentDocumente() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BazaFragmentDocumente.
     */
    // TODO: Rename and change types and number of parameters
//    public static BazaFragmentDocumente newInstance(String param1, String param2) {
//        BazaFragmentDocumente fragment = new BazaFragmentDocumente();
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_baza_documente, container, false);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void transmite_actiuni_la_fragment(View view, Bundle arg) {

    }
}