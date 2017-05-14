package com.example.david.testsudoku;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.david.completesudoku.Sudoku;
import com.david.completesudoku.SudokuGame;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TestSudoku";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    private void startGame() {
        Log.d(TAG, "starting game");
        int[][] model = new int[9][9] ;

        // Create the initial situation

        model[0][0] = 9 ;
        model[0][4] = 2 ;
        model[0][6] = 7 ;
        model[0][7] = 5 ;

        model[1][0] = 6 ;
        model[1][4] = 5 ;
        model[1][7] = 4 ;

        model[2][1] = 2 ;
        model[2][3] = 4 ;
        model[2][7] = 1 ;

        model[3][0] = 2 ;
        model[3][2] = 8 ;

        model[4][1] = 7 ;
        model[4][3] = 5 ;
        model[4][5] = 9 ;
        model[4][7] = 6 ;

        model[5][6] = 4 ;
        model[5][8] = 1 ;

        model[6][1] = 1 ;
        model[6][5] = 5 ;
        model[6][7] = 8 ;

        model[7][1] = 9 ;
        model[7][4] = 7 ;
        model[7][8] = 4 ;

        model[8][1] = 8 ;
        model[8][2] = 2 ;
        model[8][4] = 4 ;
        model[8][8] = 6 ;

        DataResult.getInstance().setSudokuModel(new TestModel());
        DataResult.getInstance().setSudokuGame(new SudokuGame(new Sudoku(model)));
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
