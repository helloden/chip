package com.novonze.chip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    String fromCurrency = "CAD";

    String toCurrency = "USD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText textbox = (EditText) findViewById(R.id.textbox);
        textbox.addTextChangedListener(filterTextWatcher);

        TextView result = (TextView) findViewById(R.id.result);
        setCurrencyLabel(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId());
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() > 0) {
                recalculate(s.toString());
            }
            else {
                TextView result = (TextView) findViewById(R.id.result);
                result.clearComposingText();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        setCurrencyLabel(view.getId());

        EditText textbox = (EditText) findViewById(R.id.textbox);
        recalculate(textbox.getText().toString());
    }
    private void recalculate(String value) {
        if(value.length() > 0 ) {
            TextView result = (TextView) findViewById(R.id.result);
            double finalValue = convert(Double.parseDouble(value));
            result.setText(String.format("%.2f", finalValue));
        }
    }

    private void setCurrencyLabel(int checkedOption)
    {
        TextView toCurrencyLabel = (TextView) findViewById(R.id.toCurrencyLabel);
        TextView fromCurrencyLabel = (TextView) findViewById(R.id.fromCurrencyLabel);

        switch (checkedOption) {
            case (R.id.toCADRadioButton):
                toCurrency = "CAD";
                fromCurrency = "USD";
                break;
            case (R.id.toUSDRadioButton):
                toCurrency = "USD";
                fromCurrency = "CAD";
                break;
            default:
                return;
        }

        toCurrencyLabel.setText(toCurrency);
        fromCurrencyLabel.setText(fromCurrency);
    }

    private double convert(double originalValue) {
        double result = originalValue;
        boolean isFinalUsdValue = isFinalUSDValue();
        double taxRate = 1;

        if(isFinalUsdValue) {
            taxRate = 1.12;
        }
        else {
            taxRate = 1.095;
        }

        return convertToCurrency(originalValue) * taxRate;
    }

    private double convertToCurrency(double originalValue) {
        if(isFinalUSDValue()) {
            return originalValue / 0.74;
        }
        else {
            return originalValue * 0.74;
        }
    }

    private boolean isFinalUSDValue()
    {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        switch (radioGroup.getCheckedRadioButtonId()) {
            case (R.id.toCADRadioButton):
                return false;
            case (R.id.toUSDRadioButton):
                return true;
        }

        return false;
    }
}
