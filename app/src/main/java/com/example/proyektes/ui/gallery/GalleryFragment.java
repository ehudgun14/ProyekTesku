package com.example.proyektes.ui.gallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyektes.Artists;
import com.example.proyektes.MainActivity;
import com.example.proyektes.databinding.FragmentGalleryBinding;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    SharedPreferences  spf;
    EditText editText;
    Button IDButton;
    TextView nama;
    TextView tipe;
    TextView popularitas;
    String idnya = "";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editText = binding.edittext;
        IDButton = binding.button;
        nama = binding.artistname;
        tipe = binding.typeartist;
        popularitas = binding.popularity;
        MainActivity main = (MainActivity) getActivity();
        spf = main.getSp();

        IDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url ="https://api.spotify.com/v1/artists/";
                url = url.concat(editText.getText().toString());
                queue.add(new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        Artists artis = gson.fromJson(response,Artists.class);
                        nama.setText(artis.getName());
                        popularitas.setText(String.valueOf(artis.getPopularity()));
                        tipe.setText(artis.getType());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG","Tidak bisa");
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();

                        String token = spf.getString("token", "");
                        String auth = "Bearer " + token;

                        headers.put("Authorization", auth);
                        return headers;
                    }});
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}