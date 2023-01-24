package org.mushare.httper;

import org.mushare.httper.dialog.SaveFileDialog;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by dklap on 5/4/2017.
 */
public abstract class AbstractSaveFileFragment extends Fragment {
    final int CREATE_FILE_REQUEST = 0;

    protected void preSaveFile() {
        if (SDK_INT >= 19) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent
                    .CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE, defaultFileName()).setType
                    ("*/*");
            try {
                startActivityForResult(intent, CREATE_FILE_REQUEST);
            } catch (Exception e) {
                openSaveDialog();
            }
        } else {
            openSaveDialog();
        }
    }

    public abstract void saveFile(OutputStream outputStream) throws IOException;

    public abstract String defaultFileName();

    void openSaveDialog() {
        DialogFragment newFragment = new SaveFileDialog();
        newFragment.setTargetFragment(AbstractSaveFileFragment.this, 0);
        newFragment.setCancelable(false);
        newFragment.show(getFragmentManager(), "dialog");
        if (SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Activity activity = getActivity();
                Uri uri           = Uri.parse("package:" + activity.getPackageName());
                Intent intent     = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                activity.startActivity(intent);
            }
        }
        else if (SDK_INT >= 23) {
            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContext().getContentResolver()
                        .openOutputStream(data.getData());
                saveFile(outputStream);
                Toast.makeText(getContext(), R.string.save_file_success2, Toast.LENGTH_SHORT)
                        .show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
