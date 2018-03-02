package com.novonze.chip;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    SquareButton button1, button2, button3,
            button4, button5, button6,
            button7, button8, button9,
            button0, buttonDot, buttonDel;

    TextView inputText, resultText, inputCurrencyLabel, resultCurrencyLabel;

    DecimalFormat decimalFormat;

    CheckBox checkBox;

    Double UsdToCadBaseRate = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText = (TextView) findViewById(R.id.inputText);
        resultText = (TextView) findViewById(R.id.result);

        decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setGroupingUsed(false);

        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        recalculateFinalValue();
                    }
                }
        );

        inputCurrencyLabel = (TextView) findViewById(R.id.inputCurrencyLabel);
        resultCurrencyLabel = (TextView) findViewById(R.id.resultCurrencyLabel);
        new FetchExchangeRates().execute("USD");
    }

    public void onClickCalculatorButton(View v) {
        String currentText = inputText.getText().toString();
        Button b = (SquareButton) v;
        switch (v.getId()) {
            case R.id.buttonDel:
                handleBackspace(currentText);
                break;
            case R.id.buttonDot:
                handleDot(currentText);
                break;
            default:
                updateInputValue(b.getText().toString());
                break;
        }

        recalculateFinalValue();
    }

    public void onClickSwapButton(View v) {
        CharSequence oldInputCurrency = inputCurrencyLabel.getText();
        inputCurrencyLabel.setText(resultCurrencyLabel.getText());
        resultCurrencyLabel.setText(oldInputCurrency);
        recalculateFinalValue();

    }

    private void handleDot(String currentText) {
        if (!currentText.contains(".")) {
            inputText.setText(inputText.getText() + ".");
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Only one decimal point duh dummy.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private void handleBackspace(String currentText) {
        if (currentText.length() == 1) {
            inputText.setText("0");
        } else {
            inputText.setText(currentText.substring(0, currentText.length() - 1));
        }
    }

    private void updateInputValue(String value) {
        String currentText = inputText.getText().toString();
        if (currentText.equals("0")) {
            inputText.setText(value);
        } else {
            inputText.setText(inputText.getText() + value);
        }
    }

    private void recalculateFinalValue() {
        double currentInputDouble = Double.parseDouble(inputText.getText().toString());
        String originatingCurrency = inputCurrencyLabel.getText().toString();
        double finalValue = currentInputDouble * getCurrencyExchangeRate(originatingCurrency);

        if (checkBox.isChecked()) {
            finalValue *= getOriginatingTaxRate(originatingCurrency);
        }

        resultText.setText(decimalFormat.format(finalValue));
    }

    private double getCurrencyExchangeRate(String baseCurrency) {
        switch (baseCurrency) {
            case "USD":
                return UsdToCadBaseRate;
            case "CAD":
                return 1/UsdToCadBaseRate;
            default:
                return 1;
        }
    }

    private double getOriginatingTaxRate(String originating) {
        switch (originating) {
            case "USD":
                return 1.095;
            case "CAD":
                return 1.12;
            default:
                return 1;
        }
    }

    private class FetchExchangeRates extends AsyncTask<String, Integer, HashMap<String, Double>> {

        @Override
        protected HashMap<String, Double> doInBackground(String... params) {
            try {
                String baseCurrency = (String) params[0];
                URL myUrl = new URL("https://api.fixer.io/latest?base=" + baseCurrency);
                URLConnection urlConnection = myUrl.openConnection();
                return ConvertStream(urlConnection.getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new HashMap<String, Double>();
        }

        @Override
        protected void onPostExecute(HashMap<String, Double> result) {
            UsdToCadBaseRate = result.get("CAD");
        }

        protected HashMap<String, Double> ConvertStream(InputStream is) throws IOException {
            if (is != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
                try {
                    return ReadJson(reader);
                } finally {
                    reader.close();
                }
            } else {
                return new HashMap<String, Double>();
            }
        }

        public HashMap<String, Double> ReadJson(JsonReader reader) throws IOException {
            HashMap<String, Double> dictionary = new HashMap<String, Double>();

            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                if (name.equals("rates")) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        dictionary.put(reader.nextName(), reader.nextDouble());
                    }
                    reader.endObject();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return dictionary;
        }
    }
}