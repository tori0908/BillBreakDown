package com.example.billbreakdown;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.RoundingMode;
import android.widget.RelativeLayout;
//import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText totalBillEditText;
    private EditText numPeopleEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        totalBillEditText = findViewById(R.id.totalBillEditText);
        numPeopleEditText = findViewById(R.id.numPeopleEditText);
        resultTextView = findViewById(R.id.resultTextView);

        EditText totalBillEditText = findViewById(R.id.totalBillEditText);
        EditText numPeopleEditText = findViewById(R.id.numPeopleEditText);

        totalBillEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(v);
                }
            }
        });

        RelativeLayout mainLayout = findViewById(R.id.mainLayout); // Replace with your main layout ID
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        numPeopleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard(v);
                }
            }
        });

        Button equalBreakdownButton = findViewById(R.id.equalBreakdownButton);
        equalBreakdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateEqualBreakdown();
            }
        });

        Button customBreakdownButton = findViewById(R.id.customBreakdownButton);
        customBreakdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateCustomBreakdown();
            }
        });

        Button combinationBreakdownButton = findViewById(R.id.combinationBreakdownButton);
        combinationBreakdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateCombinationBreakdown();
            }
        });

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResultsToSharedPreferences(resultTextView.getText().toString());
                Toast.makeText(MainActivity.this, "Results saved!", Toast.LENGTH_SHORT).show();
            }
        });

        Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String results = resultTextView.getText().toString();
                if (!TextUtils.isEmpty(results)) {
                    shareResults(results);
                } else {
                    Toast.makeText(MainActivity.this, "No results to share.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void calculateEqualBreakdown() {
        BigDecimal totalBill = new BigDecimal(totalBillEditText.getText().toString());
        int numPeople = Integer.parseInt(numPeopleEditText.getText().toString());

        if (numPeople <= 0) {
            resultTextView.setText("Invalid number of people");
            return;
        }

        BigDecimal equalShare = totalBill.divide(BigDecimal.valueOf(numPeople), 2, RoundingMode.HALF_UP);
        resultTextView.setText("Each person pays: RM" + equalShare);
    }

    private void calculateCustomBreakdown() {
        BigDecimal totalBill = new BigDecimal(totalBillEditText.getText().toString());
        int numPeople = Integer.parseInt(numPeopleEditText.getText().toString());

        if (numPeople <= 0) {
            resultTextView.setText("Invalid number of people");
            return;
        }

        // Calculate individual shares based on user input
        StringBuilder breakdownResult = new StringBuilder();

        for (int person = 1; person <= numPeople; person++) {
            EditText personShareEditText = findViewById(getResources().getIdentifier("person" + person + "ShareEditText", "id", getPackageName()));
            String input = personShareEditText.getText().toString();

            if (!TextUtils.isEmpty(input)) {
                BigDecimal personShare = new BigDecimal(input);

                if (personShare.compareTo(BigDecimal.ZERO) < 0) {
                    resultTextView.setText("Invalid amount for Person " + person);
                    return;
                }

                breakdownResult.append("Person ").append(person).append(" pays: RM").append(personShare).append("\n");
            }
        }

        if (breakdownResult.length() == 0) {
            resultTextView.setText("No valid amounts entered");
        } else {
            resultTextView.setText(breakdownResult.toString());
        }
    }

    private void calculateCombinationBreakdown() {
        BigDecimal totalBill = new BigDecimal(totalBillEditText.getText().toString());
        int numPeople = Integer.parseInt(numPeopleEditText.getText().toString());

        if (numPeople <= 0) {
            resultTextView.setText("Invalid number of people");
            return;
        }

        // Calculate combination breakdown based on user input
        StringBuilder breakdownResult = new StringBuilder();

        for (int person = 1; person <= numPeople; person++) {
            EditText personPercentageEditText = findViewById(getResources().getIdentifier("person" + person + "PercentageEditText", "id", getPackageName()));
            EditText personAmountEditText = findViewById(getResources().getIdentifier("person" + person + "AmountEditText", "id", getPackageName()));

            String percentageInput = personPercentageEditText.getText().toString();
            String amountInput = personAmountEditText.getText().toString();

            if (!TextUtils.isEmpty(percentageInput) && !TextUtils.isEmpty(amountInput)) {
                BigDecimal percentage = new BigDecimal(percentageInput);
                BigDecimal amount = new BigDecimal(amountInput);

                if (percentage.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(BigDecimal.ZERO) < 0) {
                    resultTextView.setText("Invalid input for Person " + person);
                    return;
                }

                BigDecimal personShare = totalBill.multiply(percentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)).add(amount);
                breakdownResult.append("Person ").append(person).append(" pays: RM").append(personShare).append("\n");
            }
        }

        if (breakdownResult.length() == 0) {
            resultTextView.setText("No valid input entered");
        } else {
            resultTextView.setText(breakdownResult.toString());
        }
    }

    private void shareResults(String results) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, results);
        startActivity(Intent.createChooser(shareIntent, "Share Results via"));
    }

    private void saveResultsToSharedPreferences(String results) {
        SharedPreferences preferences = getSharedPreferences("BillResults", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("breakdownResults", results);
        editor.apply();
    }

    private String loadResultsFromSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("BillResults", MODE_PRIVATE);
        return preferences.getString("breakdownResults", "");
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
