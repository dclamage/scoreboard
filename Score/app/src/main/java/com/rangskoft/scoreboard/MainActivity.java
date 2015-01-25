package com.rangskoft.scoreboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.wefika.horizontalpicker.HorizontalPicker;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
{
    private static class PlayerInfo implements Parcelable
    {
        PlayerInfo(String name)
        {
            this.name = name;
        }

        public int describeContents()
        {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags)
        {
            out.writeString(name);
            out.writeLong(score);
        }

        public static final Parcelable.Creator<PlayerInfo> CREATOR
                = new Parcelable.Creator<PlayerInfo>()
        {
            public PlayerInfo createFromParcel(Parcel in)
            {
                return new PlayerInfo(in);
            }

            public PlayerInfo[] newArray(int size)
            {
                return new PlayerInfo[size];
            }
        };

        private PlayerInfo(Parcel in)
        {
            name = in.readString();
            score = in.readLong();
        }

        public void createLayout(final MainActivity context)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tableRow = (TableRow) inflater.inflate(R.layout.tablerow, null);
            playerNameText = (TextView) tableRow.findViewById(R.id.playerName);
            playerScoreText = (TextView) tableRow.findViewById(R.id.playerScore);
            playerNameText.setText(name);
            playerScoreText.setText(Long.toString(score));

            tableRow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    context.setSelected(PlayerInfo.this);
                }
            });
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 15);
            tableRow.setLayoutParams(layoutParams);
            context.tableLayout.addView(tableRow);
        }

        TableRow tableRow = null;
        TextView playerNameText = null;
        TextView playerScoreText = null;
        public String name;
        public long score = 0;
    }

    ArrayList<PlayerInfo> playerInfos = null;
    PlayerInfo selectedInfo = null;
    Button plusButton = null;
    Button minusButton = null;
    MenuItem addPlayerItem = null;
    MenuItem removePlayerItem = null;
    TableLayout tableLayout = null;
    HorizontalPicker numberPicker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberPicker = (HorizontalPicker) findViewById(R.id.numberPicker);

        plusButton = (Button) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (selectedInfo != null)
                {
                    selectedInfo.score += numberPicker.getSelectedItem() + 1;
                    selectedInfo.playerScoreText.setText(Long.toString(selectedInfo.score));
                }
            }
        });

        minusButton = (Button) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (selectedInfo != null)
                {
                    selectedInfo.score -= numberPicker.getSelectedItem() + 1;
                    selectedInfo.playerScoreText.setText(Long.toString(selectedInfo.score));
                }
            }
        });

        tableLayout = (TableLayout) findViewById(R.id.scoreTable);

        setInteractionEnabled(false);

        if (savedInstanceState != null)
        {
            playerInfos = savedInstanceState.getParcelableArrayList("com.rangskoft.scoreboard.playerInfos");
            if (playerInfos == null)
            {
                playerInfos = new ArrayList<>();
            }

            for (PlayerInfo playerInfo : playerInfos)
            {
                playerInfo.createLayout(this);
            }

            int selectedInfoIdx = savedInstanceState.getInt("com.rangskoft.scoreboard.selectedPlayerInfo", -1);
            if (selectedInfoIdx == -1 || selectedInfoIdx >= playerInfos.size())
            {
                setSelected(null);
            }
            else
            {
                setSelected(playerInfos.get(selectedInfoIdx));
            }

            int selectedNumberPickerItem = savedInstanceState.getInt("com.rangskoft.scoreboard.selectedNumberPickerItem", 0);
            numberPicker.setSelectedItem(selectedNumberPickerItem);
        }
        else
        {
            if (playerInfos == null)
            {
                playerInfos = new ArrayList<>();
            }
            playerInfos.clear();

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int numPlayers = sharedPref.getInt("numPlayers", 0);
            for (int curPlayer = 0; curPlayer < numPlayers; curPlayer++)
            {
                String name = sharedPref.getString("name" + curPlayer, "");
                long score = sharedPref.getLong("score" + curPlayer, 0);
                if (name.length() > 0)
                {
                    PlayerInfo playerInfo = new PlayerInfo(name);
                    playerInfo.score = score;
                    playerInfo.createLayout(this);
                    playerInfos.add(playerInfo);
                }
            }

            int selectedInfoIdx = sharedPref.getInt("selectedInfoIdx", -1);
            if (selectedInfoIdx >= 0 && selectedInfoIdx < playerInfos.size())
            {
                setSelected(playerInfos.get(selectedInfoIdx));
            }
            else
            {
                setSelected(null);
            }
            numberPicker.setSelectedItem(sharedPref.getInt("selectedNumberPicker", 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        addPlayerItem = menu.findItem(R.id.action_add_player);
        removePlayerItem = menu.findItem(R.id.action_remove_player);
        removePlayerItem.setEnabled(selectedInfo != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_player)
        {
            final EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Player Name")
                    .setMessage("Input Player Name:")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            String name = input.getText().toString();
                            if (name.length() > 0)
                            {
                                for (PlayerInfo curPlayerInfo : playerInfos)
                                {
                                    if (curPlayerInfo.name.equalsIgnoreCase(name))
                                    {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Error")
                                                .setMessage("Player already exists!")
                                                .setPositiveButton("Ok", null);
                                        return;
                                    }
                                }

                                PlayerInfo playerInfo = new PlayerInfo(name);
                                playerInfo.createLayout(MainActivity.this);
                                playerInfos.add(playerInfo);
                            }
                        }
                    }).setNegativeButton("Cancel", null).show();

            return true;
        }
        else if (id == R.id.action_remove_player)
        {
            if (selectedInfo != null)
            {
                tableLayout.removeView(selectedInfo.tableRow);
                playerInfos.remove(selectedInfo);
                selectedInfo = null;
                setSelected(null);
            }
        }
        else if (id == R.id.action_new_game)
        {
            for (PlayerInfo playerInfo : playerInfos)
            {
                playerInfo.score = 0;
                playerInfo.playerScoreText.setText("0");
            }
            setSelected(null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelableArrayList("com.rangskoft.scoreboard.playerInfos", playerInfos);
        if (selectedInfo != null)
        {
            for (int i = 0; i < playerInfos.size(); i++)
            {
                if (playerInfos.get(i) == selectedInfo)
                {
                    outState.putInt("com.rangskoft.scoreboard.selectedPlayerInfo", i);
                }
            }
        }
        outState.putInt("com.rangskoft.scoreboard.selectedNumberPickerItem", numberPicker.getSelectedItem());
    }

    @Override
    protected void onPause()
    {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numPlayers", playerInfos.size());
        for (int playerNum = 0; playerNum < playerInfos.size(); playerNum++)
        {
            PlayerInfo playerInfo = playerInfos.get(playerNum);
            editor.putString("name" + playerNum, playerInfo.name );
            editor.putLong("score" + playerNum, playerInfo.score );
        }

        int selectedInfoIdx = -1;
        if (selectedInfo != null)
        {
            for (int i = 0; i < playerInfos.size(); i++)
            {
                if (playerInfos.get(i) == selectedInfo)
                {
                    selectedInfoIdx = i;
                }
            }
        }
        editor.putInt("selectedInfoIdx", selectedInfoIdx);

        editor.putInt("selectedNumberPicker", numberPicker.getSelectedItem());

        editor.commit();

        super.onPause();
    }

    void setSelected(PlayerInfo playerInfo)
    {
        if (selectedInfo != null && selectedInfo != playerInfo)
        {
            selectedInfo.tableRow.setBackgroundResource(R.drawable.tablecellbackground);
            selectedInfo.playerNameText.setTextColor(0xFF000000);
            selectedInfo.playerScoreText.setTextColor(0xFF000000);
        }

        selectedInfo = playerInfo;
        if (selectedInfo != null)
        {
            selectedInfo.tableRow.setBackgroundResource(R.drawable.tablecellbackground_selected);
            selectedInfo.playerNameText.setTextColor(0xFFFFFFFF);
            selectedInfo.playerScoreText.setTextColor(0xFFFFFFFF);
            setInteractionEnabled(true);
        }
        else
        {
            setInteractionEnabled(false);
        }
    }

    void setInteractionEnabled(boolean enabled)
    {
        if (removePlayerItem != null)
        {
            removePlayerItem.setEnabled(enabled);
        }
        plusButton.setEnabled(enabled);
        minusButton.setEnabled(enabled);
    }
}
