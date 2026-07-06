package th.or.nectec.twskeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseActivity extends AppCompatActivity {

    private EditText licenseInput;
    private Button activateBtn;
    private ProgressBar progressBar;
    private TextView statusText;
    private SharedPreferences prefs;
    private boolean isFormatting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);

        // free flavor → ข้าม license ไปหน้าหลักเลย
        if (!BuildConfig.LICENSE_ENABLED) {
            startActivity(new Intent(this, KeyboardSettingActivity.class));
            finish();
            return;
        }

        // pro flavor → ถ้ามี key บันทึกไว้แล้ว → ข้ามหน้า license
        if (!prefs.getString(LicenseManager.PREF_LICENSE_KEY, "").isEmpty()) {
            startActivity(new Intent(this, KeyboardSettingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_license);

        licenseInput = (EditText) findViewById(R.id.licenseInput);
        activateBtn = (Button) findViewById(R.id.activateBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        statusText = (TextView) findViewById(R.id.statusText);

        // Auto-format: insert dashes every 5 chars
        licenseInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String raw = s.toString().replace("-", "").toUpperCase();
                if (raw.length() > 25) raw = raw.substring(0, 25);
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < raw.length(); i++) {
                    if (i > 0 && i % 5 == 0) formatted.append('-');
                    formatted.append(raw.charAt(i));
                }
                licenseInput.setText(formatted.toString());
                licenseInput.setSelection(formatted.length());
                isFormatting = false;
            }
        });

        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String license = licenseInput.getText().toString().trim();
                if (!LicenseManager.isValidFormat(license)) {
                    showError("รูปแบบ License ไม่ถูกต้อง\nกรุณากรอกในรูปแบบ XXXXX-XXXXX-XXXXX-XXXXX-XXXXX");
                    return;
                }
                new LicenseTask(license).execute();
            }
        });

    }

    // ------------------------------------------------------------------ UI helpers

    private void showError(String message) {
        statusText.setText(message);
        statusText.setTextColor(Color.parseColor("#C62828"));
        statusText.setBackgroundColor(Color.parseColor("#FFEBEE"));
        statusText.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        statusText.setText(message);
        statusText.setTextColor(Color.parseColor("#1B5E20"));
        statusText.setBackgroundColor(Color.parseColor("#E8F5E9"));
        statusText.setVisibility(View.VISIBLE);
    }

    private void showInfo(String message) {
        statusText.setText(message);
        statusText.setTextColor(Color.parseColor("#0D47A1"));
        statusText.setBackgroundColor(Color.parseColor("#E3F2FD"));
        statusText.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        activateBtn.setEnabled(!loading);
        licenseInput.setEnabled(!loading);
    }

    private void proceedToApp(final String licenseKey) {
        prefs.edit().putString(LicenseManager.PREF_LICENSE_KEY, licenseKey).apply();
        showSuccess("License ถูกต้อง กำลังเข้าสู่ระบบ...");
        activateBtn.postDelayed(new Runnable() {
            @Override public void run() {
                startActivity(new Intent(LicenseActivity.this, KeyboardSettingActivity.class));
                finish();
            }
        }, 800);
    }

    // ------------------------------------------------------------------ AsyncTask

    class LicenseTask extends AsyncTask<Void, Void, String> {
        private final String licenseKey;

        LicenseTask(String licenseKey) {
            this.licenseKey = licenseKey;
        }

        @Override
        protected void onPreExecute() {
            setLoading(true);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(LicenseManager.URL_ACTIVATED);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                // ส่งแค่ keyNum (ไม่ส่ง uid — server จะ CHANGE_SERIAL ถ้ามี uid ไม่ตรง)
                String rawKey = licenseKey.replace("-", "");
                String jsonBody = "{\"keyNum\":\"" + rawKey + "\"}";

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(),
                        "UTF-8"
                ));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                Log.d("LICENSE", "endpoint=" + LicenseManager.URL_ACTIVATED);
                Log.d("LICENSE", "rawKey=" + rawKey);
                Log.d("LICENSE", "response=" + sb.toString());
                return sb.toString();

            } catch (Exception e) {
                return "NETWORK_ERROR:" + e.getClass().getSimpleName();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setLoading(false);

            if (result.startsWith("NETWORK_ERROR:")) {
                showError("ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้\n" +
                        "กรุณาตรวจสอบการเชื่อมต่ออินเทอร์เน็ต แล้วลองใหม่อีกครั้ง");
                return;
            }

            String msg = extractMsg(result);

            if (LicenseManager.MSG_OK.equalsIgnoreCase(msg)) {
                proceedToApp(licenseKey);

            } else {
                prefs.edit().remove(LicenseManager.PREF_LICENSE_KEY).apply();
                String detail = (msg != null && !msg.isEmpty()) ? " (" + msg + ")" : "";
                showError("License ไม่ถูกต้องหรือหมดอายุแล้ว" + detail + "\n" +
                        "กรุณาตรวจสอบ License Key และลองใหม่อีกครั้ง");
            }
        }

        /** ดึงค่า msg จาก JSON response เช่น {"msg":"OK"} */
        private String extractMsg(String json) {
            try {
                Pattern p = Pattern.compile("\"msg\"\\s*:\\s*\"([^\"]+)\"",
                        Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(json);
                if (m.find()) return m.group(1);
            } catch (Exception ignored) {}
            return "";
        }
    }
}
