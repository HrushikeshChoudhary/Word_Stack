/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private String playersWord1 = "";
    private String playersWord2 = "";
    private Stack<LetterTile> placedTiles = new Stack<>();

    Button undoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        undoButton = (Button) findViewById(R.id.undoButton);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                if(v.getId() == R.id.word1)
                    playersWord1 += tile.moveToViewGroup((ViewGroup) v);
                else
                    playersWord2 += tile.moveToViewGroup((ViewGroup) v);

                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }


                placedTiles.push(tile);


                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();

                    if (v.getId() == R.id.word1)
                        playersWord1 += tile.moveToViewGroup((ViewGroup) v);
                    else
                        playersWord2 += tile.moveToViewGroup((ViewGroup) v);

                    //tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        //TextView messageBox = (TextView) findViewById(R.id.message_box);
                        //messageBox.setText(word1 + " " + word2);
                        undoButton.setEnabled(false);

                        checkStatus();

                    }

                    placedTiles.push(tile);

                    return true;
            }
            return false;
        }


    }

    public boolean onStartGame(View view) {
        
        AssetManager assetManager = getAssets();
        try {
            words.removeAll(words);
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length() == WORD_LENGTH)
                    words.add(word);
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }

        if(WORD_LENGTH<12)
            WORD_LENGTH++;
        else
            WORD_LENGTH = 3;
        

        playersWord1 = "";
        playersWord2 = "";

        undoButton.setEnabled(true);

        ViewGroup word1LinearLayout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2LinearLayout = (ViewGroup) findViewById(R.id.word2);

        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();

        stackedLayout.clear();

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        int index1= random.nextInt(words.size());
        int index2;
        do {
            index2= random.nextInt(words.size());
        }
        while (index2 == index1);

       word1 = words.get(index1);
       word2 = words.get(index2);

        Log.d("Lengths:", word1 +"\t"+word2+":"+word2.length());
        String scrambledWord = "";
        index1 = 0;
        index2 = 0;

        while (index1 < word1.length() && index2 < word2.length()) {
            if (random.nextBoolean() && index1 - index2 <= 2) {
                scrambledWord += word1.charAt(index1);
                index1++;
            } else {
                scrambledWord += word2.charAt(index2);
                index2++;
            }
        }
        if (index1 == word1.length())
        {
            scrambledWord += word2.substring(index2);
        }

        else
        {
            scrambledWord += word1.substring(index1);
        }
        messageBox.setText(scrambledWord);

        for (int index = scrambledWord.length()-1; index >= 0; index--)
        {
            stackedLayout.push( new LetterTile(this, scrambledWord.charAt(index)));

        }
        return true;
    }

    public boolean onUndo(View view) {

        if(!placedTiles.empty()) {

            //placedTiles.pop().moveToViewGroup(stackedLayout);

            if (((View)placedTiles.peek().getParent()).getId() == R.id.word1){
                playersWord1 = new StringBuilder(playersWord1).deleteCharAt(playersWord1.length()-1).toString();
                placedTiles.pop().moveToViewGroup(stackedLayout);
            }
            else {
                playersWord2 = new StringBuilder(playersWord2).deleteCharAt(playersWord2.length()-1).toString();
                placedTiles.pop().moveToViewGroup(stackedLayout);
            }

        }
        return true;
    }

    public void checkStatus() {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        if((word1.equals(playersWord1) && word2.equals(playersWord2)) || (word1.equals(playersWord2) && word2.equals(playersWord1)) )
            messageBox.setText("You win! " + word1 + " " + word2);
        else if(words.contains(playersWord1) && words.contains(playersWord2)){
            messageBox.setText("You found alternative words! " + playersWord1 + " " + playersWord2);
        }
        else{
            messageBox.setText("Try again");
        }
    }

}


