package tfc.mini.cstr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Bragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BFragment newInstance(String param1, String param2) {
        BFragment fragment = new BFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private StorageReference mStorageRef;
    private Activity activity;//Para poder mostrar el toast al terminar de subir la imagen, con getContext directamente no funciona

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //
        activity = getActivity();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpUIelemts(view);
    }

    private TextInputEditText textInputEditText;
    private FloatingActionButton fab;
    private Button button;
    private ImageView imageView;

    private void setUpUIelemts(View view) {
        textInputEditText = view.findViewById(R.id.textInputEditText_dato);
        fab = view.findViewById(R.id.fab_tomar_foto);
        button = view.findViewById(R.id.button_guardar_en_firebase);
        imageView = view.findViewById(R.id.imageView);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        //
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "onClick: continuar; tomando fotografía de etiqueta");
                //Intent a la cámara de fotos.
                Intent hacerFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (hacerFotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(hacerFotoIntent, 111);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardar datos en firebase...
                saveNote();
                NavController navcon = NavHostFragment.findNavController(BFragment.this);
                navcon.popBackStack();
            }
        });
    }

    private void saveNote() {
        String dato = textInputEditText.getText().toString();
        if (dato.trim().isEmpty()) {
            Toast.makeText(getContext(), "Por favor inserte dato.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Obtenemos los bytes de la imagen...
        byte[] dataImg = getImageBytes(imageView);
        if (dataImg == null) { // si podemos la subimos...
            Toast.makeText(getContext(), "Por favor tome fotografía.", Toast.LENGTH_SHORT).show();
            return;
        }
        String image_id = System.currentTimeMillis() + ".png";
        // Subimos la imagen
        // TODO Esto debería de ser una transacción. el registro y la fotografía juntos. // ver videos coding-in-flow transaction
        // TODO además debería guardar un thumbnail:
//        uploadFile(dataImg, image_id);
//
//        CollectionReference notebookRef = FirebaseFirestore.getInstance()
//                .collection("Seguimientos");
//        notebookRef.add(new Seguimiento(dato, image_id));

        Seguimiento s = new Seguimiento(dato, image_id);
        uploadFileSeq(dataImg, s);

        Toast.makeText(getContext(), "Dato Guardado", Toast.LENGTH_SHORT).show();
        //TODO una vez guardado deberían guardarse las imagenes en local
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_b, container, false);
    }

    /**
     * @param requestCode código de retorno (no necesario)
     * @param resultCode  resultado de la aplicación
     * @param data        contenido del intent de la cámara
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap imageBitmap;
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // compartidoviewModel.getCachedSeguimiento().setFotoEtiqueta(imageBitmap);
            // navController.navigate(R.id.action_asistente01InicioNuevaLineaSeguimientoFragment_to_asistente02ComporbacionFotoEtiqueta);

        } else {
            // Log.d(TAG, "onActivityResult: RESULT_CANCEL");
        }
    }


    /**
     * Configuración por defecto de firebase storage.
     * // https://console.firebase.google.com/u/0/project/minicstr-8478b/storage/minicstr-8478b.appspot.com/rules
     * rules_version = '2';
     * service firebase.storage {
     * match /b/{bucket}/o {
     * match /{allPaths=**} {
     * allow read, write: if request.auth != null;
     * }
     * }
     * }
     */

    private byte[] getImageBytes(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }
        return null;
    }


    private void uploadFile(byte[] dataImg, String fileRference) {
        StorageTask mUploadTask;
        StorageReference fileReference = mStorageRef.child(fileRference);
        mUploadTask = fileReference.putBytes(dataImg)
                //.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //  mProgressBar.setProgress(0);
                            }
                        }, 500);
                        Toast.makeText(activity, "Upload successful", Toast.LENGTH_LONG).show();
//                      Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
//                      taskSnapshot.getDownloadUrl().toString());
//                      String uploadId = mDatabaseRef.push().getKey();
//                      mDatabaseRef.child(uploadId).setValue(upload);
//                      guardar referencia a la imagen:
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        //  mProgressBar.setProgress((int) progress);
                    }
                });


    }

    // TODO este método podría optimizar con un contuneWith // continueWithTask
    private void uploadFileSeq(final byte[] dataImg, final Seguimiento s) {
        StorageTask mUploadTask;
        StorageReference fileReference = mStorageRef.child(s.getIdImagen());
        mUploadTask = fileReference.putBytes(dataImg)
                //.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //  mProgressBar.setProgress(0);
                            }
                        }, 500);
                        CollectionReference notebookRef = FirebaseFirestore.getInstance()
                                .collection("Seguimientos");
                        //notebookRef.getId();
                        notebookRef.add(s);
                        Toast.makeText(activity, "Upload successful", Toast.LENGTH_LONG).show();
                    }
                })
                . addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        //  mProgressBar.setProgress((int) progress);
                    }
                });

    }
    // TODO este método podría optimizar con un contuneWith // continueWithTask EXPERIMENTAR AQUI... VV
    private void uploadFileSeq_PRUEBA2(final byte[] dataImg, final Seguimiento s) {
        StorageTask mUploadTask;
        StorageReference fileReference = mStorageRef.child(s.getIdImagen());
        mUploadTask = fileReference.putBytes(dataImg)
                //.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //  mProgressBar.setProgress(0);
                            }
                        }, 500);
                        CollectionReference notebookRef = FirebaseFirestore.getInstance()
                                .collection("Seguimientos");
                        notebookRef.add(s);
                        Toast.makeText(activity, "Upload successful", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        //  mProgressBar.setProgress((int) progress);
                    }
                });

    }
/*    private void uploadFileBORRAME(final byte[] dataImg, String fileRference) {
        CollectionReference notebookRef = FirebaseFirestore.getInstance().collection("Seguimientos");
        ;

//        StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+ ".png");
        final StorageReference fileReference = mStorageRef.child(fileRference);

        mUploadTask = (StorageTask) fileReference.putBytes(dataImg)
                //.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //  mProgressBar.setProgress(0);
                            }
                        }, 500);
                        Toast.makeText(activity, "Upload successful", Toast.LENGTH_LONG).show();
//                      Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
//                      taskSnapshot.getDownloadUrl().toString());
//                      String uploadId = mDatabaseRef.push().getKey();
//                      mDatabaseRef.child(uploadId).setValue(upload);
//                      guardar referencia a la imagen:
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        //  mProgressBar.setProgress((int) progress);
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<DocumentReference>>() {
                    @Override
                    public Task<DocumentReference> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        CollectionReference notebookRef = FirebaseFirestore.getInstance().collection("Seguimientos");
                        return    notebookRef.add(new Seguimiento("dato", "image_id"));
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"  ok .getMessage()", Toast.LENGTH_SHORT).show();
                    }
                });

//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

    }*/
}