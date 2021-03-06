package com.qre.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qre.R;
import com.qre.models.HospitalizationDTO;
import com.qre.models.MedicationDTO;
import com.qre.models.PathologyDTO;
import com.qre.models.UserContactDTO;
import com.qre.ui.components.DetailValueView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd / MM / yyyy");

    private final static int TYPE_HEADER = 1;
    private final static int TYPE_ALLERGY = 2;
    private final static int TYPE_HOSPITALIZATION = 3;
    private final static int TYPE_MEDICATION = 4;
    private final static int TYPE_PATHOLOGY = 5;
    private final static int TYPE_CONTACT = 6;

    private Context context;
    private LayoutInflater inflater;
    private List<?> items = Collections.emptyList();

    public ProfileContactAdapter(Context context, List<?> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false));
            case TYPE_ALLERGY:
                return new AllergyViewHolder(inflater.inflate(R.layout.item_allergy, parent, false));
            case TYPE_HOSPITALIZATION:
                return new HospitalizationViewHolder(inflater.inflate(R.layout.item_hospitalization, parent, false));
            case TYPE_MEDICATION:
                return new MedicationViewHolder(inflater.inflate(R.layout.item_medication, parent, false));
            case TYPE_PATHOLOGY:
                return new PathologyViewHolder(inflater.inflate(R.layout.item_pathology, parent, false));
            case TYPE_CONTACT:
                return new ContactViewHolder(inflater.inflate(R.layout.item_contact_with_delete_buttons, parent, false));
        }
        throw new IllegalArgumentException("Invalid view type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HEADER:
                int title = (Integer) items.get(position);
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.value.setText(context.getString(title));
                break;
            case TYPE_ALLERGY:
                String allergy = (String) items.get(position);
                AllergyViewHolder allergyViewHolder = (AllergyViewHolder) holder;
                allergyViewHolder.value.setText(allergy);
                break;
            case TYPE_HOSPITALIZATION:
                HospitalizationDTO hospitalizationDTO = (HospitalizationDTO) items.get(position);
                HospitalizationViewHolder hospitalizationViewHolder = (HospitalizationViewHolder) holder;
                hospitalizationViewHolder.institution.setValue(hospitalizationDTO.getInstitution());
                hospitalizationViewHolder.type.setValue(hospitalizationDTO.getType().toString());
                hospitalizationViewHolder.date.setValue(hospitalizationDTO.getDate().format(DATE_FORMATTER));
                hospitalizationViewHolder.reason.setValue(hospitalizationDTO.getReason());
                break;
            case TYPE_MEDICATION:
                MedicationDTO medicationDTO = (MedicationDTO) items.get(position);
                MedicationViewHolder medicationViewHolder = (MedicationViewHolder) holder;
                medicationViewHolder.name.setValue(medicationDTO.getName());
                medicationViewHolder.description.setValue(medicationDTO.getDescription());
                medicationViewHolder.amount.setValue(medicationDTO.getAmount().toString());
                medicationViewHolder.period.setValue(medicationDTO.getPeriod().toString());
                break;
            case TYPE_PATHOLOGY:
                PathologyDTO pathologyDTO = (PathologyDTO) items.get(position);
                PathologyViewHolder pathologyViewHolder = (PathologyViewHolder) holder;
                pathologyViewHolder.type.setValue(pathologyDTO.getType().toString());
                pathologyViewHolder.description.setValue(pathologyDTO.getDescription());
                pathologyViewHolder.date.setValue(pathologyDTO.getDate().format(DATE_FORMATTER));
                break;
            case TYPE_CONTACT:
                UserContactDTO userContactDTO = (UserContactDTO) items.get(position);
                ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
                contactViewHolder.name.setValue(userContactDTO.getFirstName() + " " + userContactDTO.getLastName());
                contactViewHolder.phone.setValue(userContactDTO.getPhoneNumber());
                if (userContactDTO.isPrimary()) {
                    contactViewHolder.primaryStar.setVisibility(View.VISIBLE);
                }
                contactViewHolder.position = position;
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return TYPE_ALLERGY;
        }
        if (item instanceof HospitalizationDTO) {
            return TYPE_HOSPITALIZATION;
        }
        if (item instanceof MedicationDTO) {
            return TYPE_MEDICATION;
        }
        if (item instanceof PathologyDTO) {
            return TYPE_PATHOLOGY;
        }
        if (item instanceof UserContactDTO) {
            return TYPE_CONTACT;
        }
        if (item instanceof Integer) {
            return TYPE_HEADER;
        }
        throw new IllegalStateException("Cannot determine view holder type for " + item);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.value)
        public TextView value;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class AllergyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.value)
        public TextView value;

        public AllergyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class HospitalizationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.institution)
        public DetailValueView institution;

        @BindView(R.id.type)
        public DetailValueView type;

        @BindView(R.id.date)
        public DetailValueView date;

        @BindView(R.id.reason)
        public DetailValueView reason;

        public HospitalizationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class MedicationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name)
        public DetailValueView name;

        @BindView(R.id.description)
        public DetailValueView description;

        @BindView(R.id.amount)
        public DetailValueView amount;

        @BindView(R.id.period)
        public DetailValueView period;

        public MedicationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class PathologyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.type)
        public DetailValueView type;

        @BindView(R.id.description)
        public DetailValueView description;

        @BindView(R.id.date)
        public DetailValueView date;

        public PathologyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_name)
        public DetailValueView name;

        @BindView(R.id.contact_phone)
        public DetailValueView phone;

        @BindView(R.id.image_primary_contact)
        public ImageView primaryStar;

        public int position;

        @OnClick(R.id.button_erase_contact)
        public void eraseButtonClicked() {
            final AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Borrar Contacto");
            alert.setMessage("¿Está seguro que desea borrar el contacto?");
            alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    dialog.dismiss();
                    items.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);


        }

    }

}