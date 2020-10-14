package tfc.mini.cstr;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SeguimientoAdapter extends FirestoreRecyclerAdapter<Seguimiento, SeguimientoAdapter.SeguimientoHolder>{
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public SeguimientoAdapter(@NonNull FirestoreRecyclerOptions<Seguimiento> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SeguimientoHolder holder, int position, @NonNull Seguimiento model) {
        //Dar valores a cada elemento de UI:  e.g holder.textViewTitle.setText(model.getTitle());
        holder. textView_dato_lista.setText(model.getDato());
    }

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

        public SeguimientoHolder(@NonNull View itemView) {
            super(itemView);
            //Instanciar elementos UI: e.g:   textView =  itemView.findViewById(R.id...);
              textView_dato_lista =  itemView.findViewById(R.id.textView_dato_lista);
        }
    }
}
