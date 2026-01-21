package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
    }

    private void initViews() {
        // Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Shared Preferences for saving toggles
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Notifications
        Switch switchNotif = findViewById(R.id.switchNotif);
        if (switchNotif != null) {
            switchNotif.setChecked(prefs.getBoolean("Notifications", true));
            switchNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("Notifications", isChecked).apply();
            });
        }

        // Location
        Switch switchLocation = findViewById(R.id.switchLocation);
        if (switchLocation != null) {
            switchLocation.setChecked(prefs.getBoolean("LocationAccess", true));
            switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("LocationAccess", isChecked).apply();
            });
        }

        // Account Actions
        LinearLayout btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        LinearLayout btnPrivacy = findViewById(R.id.btnPrivacy);
        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.dialog_privacy_title),
                    getString(R.string.privacy_content)));
        }

        // About Actions
        LinearLayout btnAboutApp = findViewById(R.id.btnAboutApp);
        if (btnAboutApp != null) {
            btnAboutApp.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.dialog_about_title),
                    getString(R.string.about_content)));
        }

        LinearLayout btnClearCache = findViewById(R.id.btnClearCache);
        if (btnClearCache != null) {
            btnClearCache.setOnClickListener(v -> clearCache());
        }
    }

    private void clearCache() {
        try {
            java.io.File dir = getCacheDir();
            deleteDir(dir);
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void showChangePasswordDialog() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        android.widget.EditText inputNew = new android.widget.EditText(this);
        inputNew.setHint(R.string.hint_new_password);
        inputNew.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        android.widget.EditText inputConfirm = new android.widget.EditText(this);
        inputConfirm.setHint(R.string.hint_confirm_password);
        inputConfirm.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = padding / 2;
        inputConfirm.setLayoutParams(params);

        container.addView(inputNew);
        container.addView(inputConfirm);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_password_title)
                .setView(container)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String newPass = inputNew.getText().toString().trim();
                    String confirmPass = inputConfirm.getText().toString().trim();

                    if (newPass.isEmpty() || newPass.length() < 6) {
                        Toast.makeText(this, R.string.error_password_short, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    user.updatePassword(newPass)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, R.string.success_password_changed, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, R.string.error_password_check, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showSimpleDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, null)
                .show();
    }

}
