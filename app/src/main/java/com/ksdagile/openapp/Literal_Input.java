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
    Map<String, String> contactDataMap = new HashMap<String, String>();

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

        return v;
    }

    ArrayList<String> getContacts() {

        try {
            ContentResolver cr = getActivity().getContentResolver();

            Uri contactData = ContactsContract.Data.CONTENT_URI;
            Cursor cursor = cr.query(contactData, null, null, null, null);
            while (cursor.moveToNext()) {

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));


                if (Integer.parseInt(cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    while (pCur.moveToNext()) {
                        //String number = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String number = pCur.getString(pCur.getColumnIndex("data1"));
                        contactDataMap.put((name != null) ? name : "", (number != null)? number: "");

                        break; // ? we want only 1 value
                    }
                    pCur.close();
                }
            }
            cursor.close();
        } catch (SQLiteException ex) {
            Log.d(Constants.TAG, ex.toString());
        } catch (Exception ex) {
            Log.d(Constants.TAG, ex.toString());
        }
        ArrayList<String> contacts = new ArrayList<>();
        contacts.addAll(contactDataMap.keySet());
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
