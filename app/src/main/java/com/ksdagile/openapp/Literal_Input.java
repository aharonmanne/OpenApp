package com.ksdagile.openapp;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Literal_Input extends Fragment {

    private OnFragmentInteractionListener mListener;

    public Literal_Input() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_literal_input, container, false);

        AutoCompleteTextView phoneNumber = (AutoCompleteTextView) v.findViewById(R.id.editTextPhone);
        String phone = GateSettings.GetInstance(getActivity()).GetPhone();
        phoneNumber.setText(phone);

        ArrayAdapter<String> contactArrayAdapter =
                new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, getContacts());
        phoneNumber.setAdapter(contactArrayAdapter);
        phoneNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                AutoCompleteTextView phoneNumber = (AutoCompleteTextView) getActivity().findViewById(R.id.editTextPhone);
                imm.hideSoftInputFromWindow(phoneNumber.getWindowToken(), 0);
            }
        });

        return v;
    }

    ArrayList<String> getContacts() {
        Map<String, String> namesIDs = ((ConfigActivity) getActivity()).contactNameID;
        try {
            ContentResolver cr = getActivity().getContentResolver();
            Uri contactData = ContactsContract.Data.CONTENT_URI;
            Cursor cursor = cr.query(contactData, null, null, null, null);
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                namesIDs.put(name, id);
            }
            cursor.close();
        } catch (SQLiteException ex) {
            Log.d(Constants.TAG, ex.toString());
        } catch (Exception ex) {
            Log.d(Constants.TAG, ex.toString());
        }
        ArrayList<String> contacts = new ArrayList<>();
        contacts.addAll(namesIDs.keySet());
        return contacts;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
