package com.example.udapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.example.udapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Retrieve the account information from the arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String email = bundle.getString("email");
            String id = bundle.getString("id");

            // Display the account information

            TextView emailTextView = view.findViewById(R.id.email);
            TextView idTextView = view.findViewById(R.id.id);


//            email = settings.getString("email", "");
//            id = settings.getString("id", "");

            emailTextView.setText(email);
            idTextView.setText(id);
        }
        TextView nameTextView = view.findViewById(R.id.name);
        SharedPreferences settings = getActivity().getSharedPreferences("LoginPrefs", 0);
        String name = settings.getString("username", "");
        Log.d("AccountFragment", "onCreate: " + name);
        nameTextView.setText("Name: "+name);

        // Find the logout button and set an OnClickListener
        Button logoutButton = view.findViewById(R.id.logout_btn);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove the user's login information
                SharedPreferences settings = getActivity().getSharedPreferences("LoginPrefs", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("logged");
                editor.remove("username");
                editor.apply();

                mAuth = FirebaseAuth.getInstance();
                gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                mAuth.signOut();
                mGoogleSignInClient.signOut();


                // Redirect to the login page
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
            }
        });
        return view;
    }
}