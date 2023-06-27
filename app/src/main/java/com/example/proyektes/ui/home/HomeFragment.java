package com.example.proyektes.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyektes.R;
import com.example.proyektes.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class HomeFragment extends Fragment {

    Button ambilFoto;
    Button ambilGaleri;
    ImageView gambarUser;
    TextView longitude;
    TextView latitude;
    Context context = getContext();
    private FragmentHomeBinding binding;

    FusedLocationProviderClient client;
    String photoPathSekarang;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyymmsshhmmss").format(new Date());
        String imageFileName = (String) timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        photoPathSekarang = image.getAbsolutePath();
        return image;
    }

    private String getUriPath(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int ci = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(ci);
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        ambilFoto = binding.ambilfoto;
        gambarUser = binding.gambaruser;
        ambilGaleri = binding.ambilgaleri;
        longitude = binding.longitude;
        latitude = binding.latitude;
        if (ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            ambilFoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File f = null;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getContext().getPackageManager()) != null){
                        try {
                            f = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (f != null){
                            Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider", f);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            resultLauncher.launch(intent);
                            galleryAddPic();
                        }
                    }

                    dapatkanlokasi();
                }
            });
            ambilGaleri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    imagePickResultLauncher.launch(intent2);
                }
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPathSekarang);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
        Toast.makeText(getContext(), "Dapatkan", Toast.LENGTH_SHORT).show();
    }

    public void dapatkanlokasi() {
        LocationManager locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locman.isProviderEnabled(LocationManager.GPS_PROVIDER) || locman.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more ils.deta
                return;
            }
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location lokasi = task.getResult();
                    if (lokasi != null) {
                        longitude.setText(String.valueOf(lokasi.getLongitude()));
                        latitude.setText(String.valueOf(lokasi.getLatitude()));
                        try {
                            String timeStamp = new SimpleDateFormat("yyyymmsshhmmss").format(new Date());
                            String textFileName = (String) timeStamp;
                            File root = new File(Environment.getExternalStorageDirectory(),"Notes");
                            if (!root.exists()){
                                root.mkdir();
                            }
                            File fp = new File(root,textFileName+".txt");
                            fp.createNewFile();
                            FileWriter fw = new FileWriter(fp);
                            fw.append(longitude.getText().toString()+","+latitude.getText().toString());
                            fw.flush();
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {

                        LocationRequest locreq =
                                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                                        .setWaitForAccurateLocation(false)
                                        .setIntervalMillis(100)
                                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                        .build();

                        LocationCallback loccall = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location loc1 = locationResult.getLastLocation();
                                longitude.setText(String.valueOf(loc1.getLongitude()));
                                latitude.setText(String.valueOf(loc1.getLatitude()));
                                try {
                                    String timeStamp = new SimpleDateFormat("yyyymmsshhmmss").format(new Date());
                                    String textFileName = (String) timeStamp;
                                    File root = new File(Environment.getExternalStorageDirectory(),"Notes");
                                    if (!root.exists()){
                                        root.mkdir();
                                    }
                                    File fp = new File(root,textFileName+".txt");
                                    fp.createNewFile();
                                    FileWriter fw = new FileWriter(fp);
                                    fw.append(longitude.getText().toString()+","+latitude.getText().toString());
                                    fw.flush();
                                    fw.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        client.requestLocationUpdates(locreq, loccall, Looper.myLooper());
                    }

                }
            });
        }
        else
        {

        }
    }
    public ActivityResultLauncher<Intent> resultLauncher =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                       /* final Bundle extras = result.getData().getExtras();
                        Thread thread =new Thread(()-> {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            gambarUser.post(()->{
                                gambarUser.setImageBitmap(bitmap);
                            });
                        });
                        thread.start();*/
                    }
                }
            });


    ActivityResultLauncher<Intent> imagePickResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Uri path = (Uri) result.getData().getData();
                    gambarUser.setImageURI(path);
                    String fpath = getUriPath(getContext(),path);
                    String fname = fpath.substring(fpath.lastIndexOf("/")+1);
                    String file;
                    if (fname.indexOf(".") > 0) {
                        file = fname.substring(0, 14);
                    } else {
                        file =  fname.substring(0, 14);
                    }
                    FileInputStream fin = null;
                    int ch;
                    StringBuffer sb = new StringBuffer();
                    String text = "";

                    try {
                        fin = new FileInputStream(Environment.getExternalStorageDirectory()+"/Notes/"+file+".txt");
                        while((ch = fin.read()) != -1) {
                            sb.append((char)ch);
                        }
                        text = sb.toString();
                        String[] sep = text.split(",");
                        longitude.setText(sep[0]);
                        latitude.setText(sep[1]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

   LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude.setText(String.valueOf(location.getLongitude()));
            latitude.setText(String.valueOf(location.getLatitude()));
        }
    };

    final  ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        dapatkanlokasi();
                    } else {

                    }
                }
            }
    );


}