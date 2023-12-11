package com.example.project2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project2.db.AppDatabase;
import com.example.project2.db.ProjectDAO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {
    private static final String USER_ID_KEY = "com.example.project2.userIdKey";

    private List<Deposit> mDeposits;
    private ProjectDAO mProjectDAO;

    private Button mAddButton;
    private Button mRemoveButton;
    private Button mViewButton;

    private int mUserId=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        //wire up butons
        mAddButton=findViewById(R.id.addIncomeButton);
        mRemoveButton=findViewById(R.id.removeIncomeButton);
        mViewButton=findViewById(R.id.viewIncomeButton);
        //getting database
        getDatabase();
        //Get user ID from intent
        mUserId=getIntent().getIntExtra(USER_ID_KEY,-1);
        //set up tool bar and back button
        Toolbar mToolbar=findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //set actions for the buttons
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIncomeDialog();
            }
        });
        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeIncomeDialog();
            }
        });
        mViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewIncomeDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the back button click
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent intentFactory(Context context, int userId){
        Intent intent=new Intent(context, IncomeActivity.class);
        intent.putExtra(USER_ID_KEY,userId);
        return intent;
    }

    private void getDatabase(){
        mProjectDAO= Room.databaseBuilder(this, AppDatabase.class,AppDatabase.DB_NAME)
                .allowMainThreadQueries()
                .build()
                .getProjectDAO();
    }

    private void addIncomeDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout for the dialog
        View dialogView = inflater.inflate(R.layout.add_income_dialog, null);
        // Set up the views in the custom layout
        EditText mAmount = dialogView.findViewById(R.id.amountEditText);
        EditText mDate = dialogView.findViewById(R.id.dateEditText);
        EditText mDescription = dialogView.findViewById(R.id.descriptionEditText);
        Button mConfirmButton = dialogView.findViewById(R.id.confirmButton);
        Button mCancelButton=dialogView.findViewById(R.id.cancelButton);
        // Set up the dialog
        builder.setView(dialogView);
        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        // Set a listener for the cancel button
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when OK is clicked
                dialog.dismiss();
            }
        });
        // Set a listener for the confirm button
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double amount=0.0;
                Date date=null;
                String description;
                String dateInString;
                SimpleDateFormat formatter=new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

                description=mDescription.getText().toString();
                dateInString=mDate.getText().toString();
                try{
                    amount=Double.parseDouble(mAmount.getText().toString());
                }catch(NumberFormatException e){
                    Log.d("PROJECT2","Couldn't convert amount");
                }
                try{
                    date = formatter.parse(dateInString);
                }catch(ParseException e){
                    Log.d("PROJECT2","Couldn't convert date");
                }

                Deposit deposit = new Deposit(amount,date,description,mUserId);
                mProjectDAO.insert(deposit);
                Toast.makeText(IncomeActivity.this, "Expense Added Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        // Show the dialog
        dialog.show();
    }

    private void viewIncomeDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout for the dialog
        View dialogView = inflater.inflate(R.layout.view_income_dialog, null);
        // Set up the views in the custom layout
        TextView textViewInfo = dialogView.findViewById(R.id.viewIncomeEditText);
        Button mDismissButton = dialogView.findViewById(R.id.dismissButton);
        // Set the information you want to display
        mDeposits=mProjectDAO.getDepositsByUserId(mUserId);
        if(mDeposits.size()<=0){
            textViewInfo.setText("No records found.");
        }else{
            StringBuilder sb=new StringBuilder();
            for(Deposit deposit:mDeposits){
                sb.append(deposit);
                sb.append("\n");
                sb.append("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                sb.append("\n");
            }
            textViewInfo.setText(sb.toString());
        }
        // Set up the dialog
        builder.setView(dialogView);
        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        // Set a listener for the OK button
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when OK is clicked
                dialog.dismiss();
            }
        });
        // Show the dialog
        dialog.show();
    }

    private void removeIncomeDialog(){
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout for the dialog
        View dialogView = inflater.inflate(R.layout.remove_income_dialog, null);
        // Set up the views in the custom layout
        EditText mEditText = dialogView.findViewById(R.id.getTransactionIdEditText);
        Button mConfirmButton = dialogView.findViewById(R.id.confirmButton);
        Button mCancelButton=dialogView.findViewById(R.id.cancelButton);
        // Set up the dialog
        builder.setView(dialogView);
        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        // Set a listener for the cancel button
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when OK is clicked
                dialog.dismiss();
            }
        });
        // Set a listener for the confirm button
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTransactionId=mEditText.getText().toString();
                Deposit deposit=mProjectDAO.getDepositById(Integer.parseInt(mTransactionId));
                if(deposit==null){
                    Toast.makeText(IncomeActivity.this, "No transaction found. Try again.", Toast.LENGTH_SHORT).show();
                }else{
                    mProjectDAO.delete(deposit);
                    Toast.makeText(IncomeActivity.this, "Income deleted successfully", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        // Show the dialog
        dialog.show();
    }
}