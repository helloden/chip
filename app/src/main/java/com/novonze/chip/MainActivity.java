package com.novonze.chip;

import android.provider.MediaStore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText canadaTextbox = (EditText) findViewById(R.id.canadaTextbox);
        canadaTextbox.addTextChangedListener(filterTextWatcher);

        TextView result = (TextView) findViewById(R.id.result1);
        setCurrencyLabel(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId());
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            double originalValue = Double.parseDouble(s.toString());
            recalculate(originalValue);
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
        if(checked) {
            setCurrencyLabel(view.getId());
        }

        EditText canadaTextbox = (EditText) findViewById(R.id.canadaTextbox);
        recalculate(Double.parseDouble(canadaTextbox.getText().toString()));
    }
    private void recalculate(double originalValue) {
        TextView result = (TextView) findViewById(R.id.result1);
        double finalValue = convert(originalValue);
        result.setText(String.format("%.2f", finalValue));
    }

    private void setCurrencyLabel(int checkedOption)
    {
        TextView currencyLabel = (TextView) findViewById(R.id.currencyLabel);
        String currency = "";
        switch (checkedOption) {
            case (R.id.toCADRadioButton):
                currency = "CAD";
                break;
            case (R.id.toUSDRadioButton):
                currency = "USD";
                break;
            default:
                return;
        }
        currencyLabel.setText(currency);
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
