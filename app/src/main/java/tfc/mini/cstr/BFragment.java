package tfc.mini.cstr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
/**
 * <pre>
 * Configuración por defecto de firebase storage.
 *      // https://console.firebase.google.com/u/0/project/minicstr-8478b/storage/minicstr-8478b.appspot.com/rules
 * rules_version = '2';
 *      service firebase.storage {
 *          match /b/{bucket}/o {
 *              match /{allPaths=**} {
 *                  allow read, write: if request.auth != null;
 *              }
 *          }
 *      }
 *
 *  rules_version = '2';
 *      service firebase.storage {
 *          match /b/{bucket}/o {
 *              match /{allPaths=**} {
 *                  allow read, write, delete:  if true;
 *              }
 *          }
 *      }
 * </pre>
 */

/**
 *rules_version = '2';
 * service firebase.storage {
 *   match /b/{bucket}/o {
 *     match /{allPaths=**} {
 *       allow read, write, delete: if true;
 *     }
 *   }
 * }
 */

/**
 * At the time of writing, you cannot include write operations to Firestore
 * with write operations to Cloud Storage in one atomic operation.
 *
 * As you have mentioned, you can use a batched write for your write/update/delete
 * operations in Firestore and this will be atomic, but you cannot include the write to Storage.
 */

public class BFragment extends Fragment {
    private static final String TAG = "BFragment";
    //
    private TextInputEditText textInputEditText;
    private FloatingActionButton fab;
    private Button button;
    private ImageView imageView;
    //
    private StorageReference storageReference_uploads;
    private Activity activity;//Para poder mostrar el toast al terminar de subir la imagen, con getContext directamente no funciona
    private FirebaseStorage firebaseStorage;

    public BFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        activity = getActivity();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference_uploads = firebaseStorage.getReference("uploads");
    }

    public void delete(String s) {
        //StorageReference imageRef = firebaseStorage.getReferenceFromUrl( "gs://minicstr-8478b.appspot.com/uploads/1603294526876.png");
        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(s);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(activity, " ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.getMessage());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpUIelements(view);
    }

    private void setUpUIelements(View view) {
        textInputEditText = view.findViewById(R.id.textInputEditText_dato);
        fab = view.findViewById(R.id.fab_tomar_foto);
//        FloatingActionButton fab2 = view.findViewById(R.id.fab_borrar);
        button = view.findViewById(R.id.button_guardar_en_firebase);
        imageView = view.findViewById(R.id.imageView);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        //
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                saveSeguimiento();
            }
        });
        /*
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
*/
    }

    private void saveSeguimiento() {
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
        // Subimos la imagen
        // TODO Esto debería de ser una transacción. el registro y la fotografía juntos. // ver videos coding-in-flow transaction
        // TODO además debería guardar un thumbnail:
        Seguimiento s = new Seguimiento(dato);
        uploadSeguimiento(dataImg, s);
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
            Log.d(TAG, "onActivityResult: RESULT_CANCEL");
        }
    }


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


    private void fireStoreSeguimientoTransaction(final Uri imageUri, final Seguimiento seguimiento) {// Primero reads luego writes
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.runTransaction(new Transaction.Function<String>() {
            @Override
            public String apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                //LECTURAS
                //No ha de momento...
                //ESCRITURAS
                CollectionReference colRef_Seguimientos = db.collection("Seguimientos");
                DocumentReference docReference = colRef_Seguimientos.document();
                //
                seguimiento.setImageURL(imageUri.toString());
                //
                transaction.set(docReference, seguimiento);
                //
                return docReference.getId();
            }
        }).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getContext(), "onSuccess: ID:" + result, Toast.LENGTH_SHORT).show();
                //
                NavController navcon = NavHostFragment.findNavController(BFragment.this);
                navcon.popBackStack();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Si algo saliera mal se podría borrar la foto.
                if (imageUri != null) {
                    delete(imageUri.toString());
                    Toast.makeText(activity, "ImageUri" + imageUri.getPath() + "deleted.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "imageUri == null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //
    private void waiter(long millis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //  mProgressBar.setProgress(0);
            }
        }, millis);
    }

    /**
     *
     * @return
     */
    private String generateID() {
        return System.currentTimeMillis() + ".png";
    }

    private void uploadSeguimiento(final byte[] dataImg, final Seguimiento seguimiento) {
        seguimiento.setIdImagen(generateID());
        final StorageReference fileReference = storageReference_uploads.child(seguimiento.getIdImagen());
        fileReference.putBytes(dataImg)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure");
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {//TODO Agregar una Progressbar
                        // double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        // mProgressBar.setProgress((int) progress);
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri imageUri) {
                fireStoreSeguimientoTransaction(imageUri, seguimiento);
            }
        });
    }
}