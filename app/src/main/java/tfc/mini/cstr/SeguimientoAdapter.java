package tfc.mini.cstr;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SeguimientoAdapter extends FirestoreRecyclerAdapter<Seguimiento, SeguimientoAdapter.SeguimientoHolder> {
    private final Context context;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     * @param context
     */
    public SeguimientoAdapter(@NonNull FirestoreRecyclerOptions<Seguimiento> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final SeguimientoHolder holder, int position, @NonNull Seguimiento model) {
        //Dar valores a cada elemento de UI:  e.g holder.textViewTitle.setText(model.getTitle());
        holder.textView_dato_lista.setText(model.getDato());
        holder.textView_id_lista.setText(model.getIdImagen()) ;
        //
        String imgPath = "uploads/" + model.getIdImagen();//IMAGE_PATH;
        downLoadImage(imgPath, holder.imageView_thumbnail);
    }

     private StorageReference storageReference;
     public void downLoadImage(String imgPath, final ImageView imageView) {
        storageReference = FirebaseStorage.getInstance().getReference();
        // imgPath "users/me/profile.png"
        storageReference.child(imgPath).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Glide.with(context).load(bytes)
                        //.thumbnail(Glide.with(context).load(R.drawable.ic_image_loading))
                        //.error(R.drawable.ic_image_error)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
//    Object does not exist at location.
//    Code: -13010 HttpResult: 404
    @NonNull
    @Override
    public SeguimientoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_fragment_a,
                parent, false);
        return new SeguimientoHolder(v);
    }


    public class SeguimientoHolder extends RecyclerView.ViewHolder {
        //Declarar elementos UI: e.g: TextView textView;

        TextView textView_dato_lista;
        TextView textView_id_lista;
        ImageView imageView_thumbnail;

        public SeguimientoHolder(@NonNull View itemView) {
            super(itemView);

            //Instanciar elementos UI: e.g:   textView =  itemView.findViewById(R.id...);
            textView_dato_lista = itemView.findViewById(R.id.textView_dato_lista);
            textView_id_lista = itemView.findViewById(R.id.textView_id_imagen_lista);
            imageView_thumbnail = itemView.findViewById(R.id.imageView_thumbnail_imagen);
        }
    }
}