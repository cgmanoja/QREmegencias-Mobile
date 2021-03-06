package com.qre.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.qre.R;
import com.qre.injection.Injector;
import com.qre.services.networking.NetCallback;
import com.qre.services.networking.NetworkService;
import com.qre.services.preference.impl.UserPreferenceService;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.qre.utils.Constants.INTENT_EXTRA_USER;

public class MedicalClinicalHistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = MedicalClinicalHistoryActivity.class.getSimpleName();

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd / MM / yyyy");

    private static final int CODE_DATE = 1;
    private static final int CODE_CAMERA = 2;
    private static final int CODE_GALLERY = 3;
    private static final int CAMERA_STORAGE_PERMISSIONS = 1000;

    @Inject
    UserPreferenceService userPreferenceService;

    @Inject
    NetworkService networkService;

    @BindView(R.id.input_name)
    EditText vName;

    @BindView(R.id.input_date)
    EditText vDate;

    @BindView(R.id.input_text)
    EditText vText;

    @BindView(R.id.btn_save)
    Button vSave;

    @BindView(R.id.appbar)
    Toolbar vToolbar;

    @BindView(R.id.image_file_attached)
    ImageView vImageAttached;

    private LocalDate date;
    private File file;

    public static Intent getIntent(final Context context) {
        Intent intent = new Intent(context, MedicalClinicalHistoryActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_clinical_history);

        ButterKnife.bind(this);

        Injector.getServiceComponent().inject(this);

        setSupportActionBar(vToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        date = LocalDate.now();
        vDate.setText(date.format(DATE_FORMATTER));
    }

    @OnClick(R.id.btn_save)
    public void save() {

        String name = vName.getText().toString();
        String text = vText.getText().toString();
        String user = getIntent().getStringExtra(INTENT_EXTRA_USER);

        boolean ok = true;

        if (name.isEmpty()) {
            ok = false;
            vName.setError(getString(R.string.required_field));
        }

        if (text.isEmpty()) {
            ok = false;
            vText.setError(getString(R.string.required_field));
        }

        if (file == null) {
            ok = false;
            Toast.makeText(this, getString(R.string.file_history_missing), Toast.LENGTH_SHORT).show();
        }

        if (ok) {

            vSave.setEnabled(false);
            final Drawable vSaveBackground = vSave.getBackground();
            vSave.setBackgroundColor(Color.GRAY);

            networkService.createMedicalRecord(name, text, date, user, file, new NetCallback<Map<String, String>>() {

                @Override
                public void onSuccess(Map<String, String> response) {
                    vSave.setEnabled(true);
                    vSave.setBackground(vSaveBackground);
                    Toast.makeText(MedicalClinicalHistoryActivity.this, getString(R.string.load_history_success), Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e(TAG, "Cannot create clinical history", e);
                    vSave.setEnabled(true);
                    vSave.setBackground(vSaveBackground);
                    Toast.makeText(MedicalClinicalHistoryActivity.this, getString(R.string.load_history_error), Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    @OnClick(R.id.input_date)
    public void openDateDialog() {
        DatePickerFragment dialog = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(DatePickerFragment.DATE, date);
        dialog.setArguments(bundle);
        dialog.setActivity(this);
        dialog.show(getSupportFragmentManager(), "datePicker");
    }

    @OnClick(R.id.btn_load)
    public void openPictureDialog() {
        List<String> permissionsList = new ArrayList<>(3);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.CAMERA);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!permissionsList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsList.toArray(new String[permissionsList.size()]), CAMERA_STORAGE_PERMISSIONS);
        } else {
            openPictureDialogWithPermissionsGrantedByUserInputFromAndroidDevice();
        }
    }

    private void openPictureDialogWithPermissionsGrantedByUserInputFromAndroidDevice() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getString(R.string.load_image));
        String[] pictureDialogItems = {getString(R.string.load_image_from_gallery), getString(R.string.load_image_from_camera)};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        choosePhotoFromGallery();
                        break;
                    case 1:
                        takePhotoFromCamera();
                        break;
                }
            }
        });
        pictureDialog.show();
    }

    public File saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "qr");
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }
        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this, new String[]{f.getPath()}, new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
            return f;
        } catch (IOException e) {
            Log.e(TAG, "Cannot save image", e);
        }
        return null;
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, CODE_GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CODE_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DATE) {
            date = (LocalDate) data.getSerializableExtra(DatePickerFragment.DATE);
            vDate.setText(date.format(DATE_FORMATTER));
        } else if (requestCode == CODE_GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentURI);
                    file = saveImage(bitmap);
                    Toast.makeText(MedicalClinicalHistoryActivity.this, getString(R.string.load_image_success), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot load image from gallery", e);
                    Toast.makeText(MedicalClinicalHistoryActivity.this, getString(R.string.load_image_error), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == CODE_CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            file = saveImage(thumbnail);
            Toast.makeText(MedicalClinicalHistoryActivity.this, getString(R.string.load_image_success), Toast.LENGTH_SHORT).show();
        }
        if (file != null) {
            vImageAttached.setImageResource(R.drawable.ic_file_check_grey600_24dp);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = LocalDate.of(year, month + 1, dayOfMonth);
        vDate.setText(date.format(DATE_FORMATTER));
    }

    public static class DatePickerFragment extends DialogFragment {

        public static final String DATE = "date";

        private MedicalClinicalHistoryActivity activity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LocalDate date = (LocalDate) getArguments().getSerializable(DatePickerFragment.DATE);
            date = date != null ? date : LocalDate.now();
            return new DatePickerDialog(getActivity(), activity, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        }

        public void setActivity(MedicalClinicalHistoryActivity activity) {
            this.activity = activity;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_STORAGE_PERMISSIONS: {
                boolean permissionsOk = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        permissionsOk = false;
                        break;
                    }
                }
                if (permissionsOk) {
                    openPictureDialogWithPermissionsGrantedByUserInputFromAndroidDevice();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Se necesitan permisos de cámara y almacenamiento para cargar historia clínica";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }
    }


}