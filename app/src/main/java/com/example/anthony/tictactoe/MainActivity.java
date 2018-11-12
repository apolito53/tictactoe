package com.example.anthony.tictactoe;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView turnLabel;
    Context context = null;

    TTTButton btn1;
    TTTButton btn2;
    TTTButton btn3;
    TTTButton btn4;
    TTTButton btn5;
    TTTButton btn6;
    TTTButton btn7;
    TTTButton btn8;
    TTTButton btn9;
    private ArrayList<TTTButton> buttonArrayList = new ArrayList<>();

    LayoutInflater inflater;

    private Button startButton;
    private Button cancelButton;
    private Button restartButton;
    private TTTButton[] buttons = new TTTButton[9];
    private int current = 0;
    private int turns = 0;
    private Player[] players = new Player[2];

    boolean winner;

    boolean multiplayer;
    boolean selfReady;
    boolean otherPlayerReady;
    boolean isPlayer1;

    String otherPlayerNumber;
    String player2Name;

    SMSReceiver smsReceiver;
    SmsManager smsManager = SmsManager.getDefault();

    View main;

    ProgressDialog pd;
    AlertDialog.Builder builder;

    int exitCount = 0;

    Singleton singleton = Singleton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!isSmsPermissionGranted()) {
            requestReadAndSendSmsPermission();
        }

        super.onCreate(savedInstanceState);
        inflater = MainActivity.this.getLayoutInflater();
        main = inflater.inflate(R.layout.activity_main, null);
        setContentView(main);

        pd = new ProgressDialog(MainActivity.this);

        smsReceiver = new SMSReceiver(this);

        context = getApplicationContext();
        turnLabel = findViewById(R.id.turnLabel);

        btn1 = findViewById(R.id.btn1_1);
        btn1.setName("btn1");
        buttons[0] = btn1;
        btn1.index = 0;
        buttonArrayList.add(btn1);

        btn2 = findViewById(R.id.btn1_2);
        btn2.setName("btn2");
        buttons[1] = btn2;
        btn2.index = 1;
        buttonArrayList.add(btn2);

        btn3 = findViewById(R.id.btn1_3);
        btn3.setName("btn3");
        buttons[2] = btn3;
        btn3.index = 2;
        buttonArrayList.add(btn3);

        btn4 = findViewById(R.id.btn2_1);
        btn4.setName("btn4");
        buttons[3] = btn4;
        btn4.index = 3;
        buttonArrayList.add(btn4);

        btn5 = findViewById(R.id.btn2_2);
        btn5.setName("btn5");
        buttons[4] = btn5;
        btn5.index = 4;
        buttonArrayList.add(btn5);

        btn6 = findViewById(R.id.btn2_3);
        btn6.setName("btn6");
        buttons[5] = btn6;
        btn6.index = 5;
        buttonArrayList.add(btn6);

        btn7 = findViewById(R.id.btn3_1);
        btn7.setName("btn7");
        buttons[6] = btn7;
        btn7.index = 6;
        buttonArrayList.add(btn7);

        btn8 = findViewById(R.id.btn3_2);
        btn8.setName("btn8");
        buttons[7] = btn8;
        btn8.index = 7;
        buttonArrayList.add(btn8);

        btn9 = findViewById(R.id.btn3_3);
        btn9.setName("btn9");
        buttons[8] = btn9;
        btn9.index = 8;
        buttonArrayList.add(btn9);

        for (final TTTButton btn : buttonArrayList) {
            btn.setEnabled(false);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!multiplayer) {
                        if (current == 1 && btn.getStatus().equals("")) {
                            players[0].register(btn, btn.index);
                            if (checkBoardCondition()) {
                                current = 2;
                                turnLabel.setText(String.format("%s%s", players[1].name, getString(R.string.your_turn)));
                            }
                        } else if (current == 2 && btn.getStatus().equals("")) {
                            players[1].register(btn, btn.index);
                            if (checkBoardCondition()) {
                                current = 1;
                                turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                            }
                        }
                    } else {
                       if(current == 1&& btn.getStatus().equals("")) {
                           players[0].register(btn, btn.index);
                           if (checkBoardCondition()) {
                               if(isPlayer1) {
                                   smsManager.sendTextMessage(otherPlayerNumber, null, btn.getName(), null, null);
                                   if(noWinner() && !(turns == 9)) {
                                       showWaitingDialog();
                                   }
                               }
                               current = 2;
                               turnLabel.setText(String.format("%s%s", players[1].name, getString(R.string.your_turn)));
                           }
                       } else if(current == 2 && btn.getStatus().equals("")) {
                           players[1].register(btn, btn.index);
                           if (checkBoardCondition()) {
                               if(!isPlayer1) {
                                   smsManager.sendTextMessage(otherPlayerNumber, null, btn.getName(), null, null);
                                   if(noWinner() && !(turns == 9)) {
                                       showWaitingDialog();
                                   }
                               }
                               current = 1;
                               turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                           }
                       }
                    }
                }
            });
        }

            startButton = findViewById(R.id.startButton);

        //////////////////////////// START BUTTON /////////////////////////////////////////////////
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("New Game")
                            .setMessage("Select game mode:")
                            .setCancelable(false)
                            .setPositiveButton("Local Game", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    multiplayer = false;
                                    initPlayer1();
                                }
                            })
                            .setNegativeButton("Multiplayer Game", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    multiplayer = true;
                                    otherPlayerReady = false;
                                    setupMultiplayer();
                                }
                            })
                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    reset();
                                }
                            }).create().show();
                } catch (Exception ex) {}
                startButton.setEnabled(false);
                startButton.setText(R.string.game_in_progress);
            }
        });

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Cancel")
                        .setMessage("Cancel the game?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(multiplayer) {
                                    smsManager.sendTextMessage(otherPlayerNumber, null, "exit", null, null);
                                }
                                System.exit(0);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });

        restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Restart")
                        .setMessage("Restart the game?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                }).create().show();
            }
        });

        if(singleton.getFlag() == 1) {
            singleton.setFlag(0);
            otherPlayerNumber = singleton.getOtherPlayerNumber();
            smsManager.sendTextMessage(otherPlayerNumber, null, "accepted", null, null);
            players[0] = new Player(otherPlayerNumber, R.drawable.red_x);
            multiplayer = true;
            isPlayer1 = false;
            initPlayer2();
        }
    }


    public void initPlayer1() {
        View view1 = inflater.inflate(R.layout.player1_settings, null);
        ToggleButton x1 = view1.findViewById(R.id.blackXButton);
        ToggleButton x2 = view1.findViewById(R.id.blueXButton);
        ToggleButton x3 = view1.findViewById(R.id.redXButton);

        EditText input1 = view1.findViewById(R.id.player1Name);

        x1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x2.setChecked(false);
                x3.setChecked(false);
                Toast.makeText(MainActivity.this, "Black selected!", Toast.LENGTH_SHORT).show();
            }
        });

        x2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x1.setChecked(false);
                x3.setChecked(false);
                Toast.makeText(MainActivity.this, "Blue selected!", Toast.LENGTH_SHORT).show();
            }
        });


        x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x1.setChecked(false);
                x2.setChecked(false);
                Toast.makeText(MainActivity.this, "Red selected!", Toast.LENGTH_SHORT).show();
            }
        });

        builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog);
        builder.setView(view1);
        builder.setCancelable(false);
        if (multiplayer) {
            builder.setTitle("You are player 1")
                    .setMessage("Please enter your name and select your icon: ");
        } else {
            builder.setTitle("Player 1")
                    .setMessage("Player 1, please enter your name and select your icon: ");
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Boolean valid = false;
                if (!input1.getText().toString().equals("") && (x1.isChecked() || x2.isChecked() || x3.isChecked())) {
                    valid = true;
                } else if (!input1.getText().toString().equals("") && !(x1.isChecked() || x2.isChecked() || x3.isChecked())) {
                    Toast.makeText(MainActivity.this, "Please pick a symbol!", Toast.LENGTH_LONG).show();
                    initPlayer1(input1.getText().toString());
                } else {
                    Toast.makeText(MainActivity.this, "Please enter your name AND pick a symbol!", Toast.LENGTH_LONG).show();
                    initPlayer1();
                }

                if (valid) {
                    if (x1.isChecked()) {
                        players[0] = new Player(input1.getText().toString(), R.drawable.black_x);
                    } else if (x2.isChecked()) {
                        players[0] = new Player(input1.getText().toString(), R.drawable.blue_x);
                    } else if (x3.isChecked()) {
                        players[0] = new Player(input1.getText().toString(), R.drawable.red_x);
                    }

                    if (!multiplayer) {
                        initPlayer2();
                    } else {
                        smsManager.sendTextMessage(otherPlayerNumber, null, "ready", null, null);
                        selfReady = true;
                        if (!otherPlayerReady) {
                            showWaitingDialog();
                        } else {
                            current = 1;
                            turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                            for (TTTButton btn : buttonArrayList) {
                                btn.setEnabled(true);
                            }
                        }
                    }
                }
            }
        }).create().show();
    }

    public void initPlayer1(String name) {
        View view1 = inflater.inflate(R.layout.player1_settings, null);

        ToggleButton x1 = view1.findViewById(R.id.blackXButton);
        ToggleButton x2 = view1.findViewById(R.id.blueXButton);
        ToggleButton x3 = view1.findViewById(R.id.redXButton);

        EditText input1 = view1.findViewById(R.id.player1Name);
        input1.setText(name);
        x1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x2.setChecked(false);
                x3.setChecked(false);
                Toast.makeText(MainActivity.this, "Black selected!", Toast.LENGTH_SHORT).show();
            }
        });

        x2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x1.setChecked(false);
                x3.setChecked(false);
                Toast.makeText(MainActivity.this, "Blue selected!", Toast.LENGTH_SHORT).show();
            }
        });

        x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x1.setChecked(false);
                x2.setChecked(false);
                Toast.makeText(MainActivity.this, "Red selected!", Toast.LENGTH_SHORT).show();
            }
        });

        builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog);
        builder.setView(view1);
        builder.setCancelable(false);
        if(multiplayer) {
            builder.setTitle("You are player 1")
                    .setMessage("Please enter your name and select your icon: ");
        } else {
            builder.setTitle("Player 1")
                    .setMessage("Player 1, please enter your name and select your icon: ");
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean valid = false;

                        if (!input1.getText().toString().equals("") && (x1.isChecked() || x2.isChecked() || x3.isChecked())) {
                            valid = true;
                        } else if (!input1.getText().toString().equals("") && !(x1.isChecked() || x2.isChecked() || x3.isChecked())) {
                            Toast.makeText(MainActivity.this, "Please pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer1(input1.getText().toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter your name AND pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer1();
                        }

                        if (valid) {
                            if (x1.isChecked()) {
                                players[0] = new Player(input1.getText().toString(), R.drawable.black_x);
                            } else if (x2.isChecked()) {
                                players[0] = new Player(input1.getText().toString(), R.drawable.blue_x);
                            } else if (x3.isChecked()) {
                                players[0] = new Player(input1.getText().toString(), R.drawable.red_x);
                            }

                            if (!multiplayer) {
                                initPlayer2();
                            } else {
                                smsManager.sendTextMessage(otherPlayerNumber, null, "ready", null, null);
                                selfReady = true;
                                if (!otherPlayerReady) {
                                    showWaitingDialog();
                                } else {
                                    current = 1;
                                    turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                                    for (TTTButton btn : buttonArrayList) {
                                        btn.setEnabled(true);
                                    }
                                }
                            }
                        }
                    }
                }).create().show();
    }

    public void initPlayer2() {
        View view2 = inflater.inflate(R.layout.player2_settings, null);
        ToggleButton o1 = view2.findViewById(R.id.blackOButton);
        ToggleButton o2 = view2.findViewById(R.id.blueOButton);
        ToggleButton o3 = view2.findViewById(R.id.whiteOButton);

        EditText input2 = view2.findViewById(R.id.player2Name);

        o1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o2.setChecked(false);
                o3.setChecked(false);
                Toast.makeText(MainActivity.this, "Black selected!", Toast.LENGTH_SHORT).show();
            }
        });

        o2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o1.setChecked(false);
                o3.setChecked(false);
                Toast.makeText(MainActivity.this, "Blue selected!", Toast.LENGTH_SHORT).show();
            }
        });

        o3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o1.setChecked(false);
                o2.setChecked(false);
                Toast.makeText(MainActivity.this, "Gray selected!", Toast.LENGTH_SHORT).show();
            }
        });

        builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog);
        builder.setView(view2);
        if(multiplayer) {
            builder.setTitle("You are player 2")
                    .setMessage("Please enter your name and select your icon: ");
        } else {
            builder.setTitle("Player 2")
                    .setMessage("Player 2, please enter your name and select your icon: ");
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });
        builder.setCancelable(false)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean valid = false;
                        if (!input2.getText().toString().equals("") && (o1.isChecked() || o2.isChecked() || o3.isChecked())) {
                            valid = true;
                        } else if (!input2.getText().toString().equals("") && !(o1.isChecked() || o2.isChecked() || o3.isChecked())) {
                            Toast.makeText(MainActivity.this, "Please pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer2(input2.getText().toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter your name AND pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer2();
                        }

                        if (valid) {
                            if (o1.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.black_o);
                            } else if (o2.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.blue_o);
                            } else if (o3.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.white_o);
                            }

                            if (multiplayer) {
                                smsManager.sendTextMessage(otherPlayerNumber, null, "ready", null, null);
                                selfReady = true;
                                if (!otherPlayerReady) {
                                    showWaitingDialog();
                                } else {
                                    showWaitingDialog();
                                    current = 1;
                                    turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                                    startButton.setText(R.string.game_in_progress);
                                    startButton.setEnabled(false);
                                    for (TTTButton btn : buttonArrayList) {
                                        btn.setEnabled(true);
                                    }
                                }
                            } else {
                                current = 1;
                                turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                                for (TTTButton btn : buttonArrayList) {
                                    btn.setEnabled(true);
                                }
                            }

                        }
                    }
                }).create().show();
    }
    public void initPlayer2(String name) {
        View view2 = inflater.inflate(R.layout.player2_settings, null);
        ToggleButton o1 = view2.findViewById(R.id.blackOButton);
        ToggleButton o2 = view2.findViewById(R.id.blueOButton);
        ToggleButton o3 = view2.findViewById(R.id.whiteOButton);

        EditText input2 = view2.findViewById(R.id.player2Name);
        input2.setText(name);

        o1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o2.setChecked(false);
                o3.setChecked(false);
                Toast.makeText(MainActivity.this, "Black selected!", Toast.LENGTH_SHORT).show();
            }
        });

        o2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o1.setChecked(false);
                o3.setChecked(false);
                Toast.makeText(MainActivity.this, "Blue selected!", Toast.LENGTH_SHORT).show();
            }
        });

        o3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o1.setChecked(false);
                o2.setChecked(false);
                Toast.makeText(MainActivity.this, "Gray selected!", Toast.LENGTH_SHORT).show();
            }
        });

        builder = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Dialog);
        builder.setView(view2);
        if (multiplayer) {
            builder.setTitle("You are player 2")
                    .setMessage("Please enter your name and select your icon: ");
        } else {
            builder.setTitle("Player 2")
                    .setMessage("Player 2, please enter your name and select your icon: ");
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        });
        builder.setCancelable(false)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean valid = false;
                        if (!input2.getText().toString().equals("") && (o1.isChecked() || o2.isChecked() || o3.isChecked())) {
                            valid = true;
                        } else if (!input2.getText().toString().equals("") && !(o1.isChecked() || o2.isChecked() || o3.isChecked())) {
                            Toast.makeText(MainActivity.this, "Please pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer2(input2.getText().toString());
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter your name AND pick a symbol!", Toast.LENGTH_LONG).show();
                            initPlayer2();
                        }

                        if (valid) {
                            if (o1.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.black_o);
                            } else if (o2.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.blue_o);
                            } else if (o3.isChecked()) {
                                players[1] = new Player(input2.getText().toString(), R.drawable.white_o);
                            }

                            if (multiplayer) {
                                smsManager.sendTextMessage(otherPlayerNumber, null, "ready", null, null);
                                selfReady = true;
                                if (!otherPlayerReady) {
                                    showWaitingDialog();
                                } else {
                                    showWaitingDialog();
                                    current = 1;
                                    turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                                    startButton.setText(R.string.game_in_progress);
                                    startButton.setEnabled(false);
                                    for (TTTButton btn : buttonArrayList) {
                                        btn.setEnabled(true);
                                    }
                                }
                            } else {
                                current = 1;
                                turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                                for (TTTButton btn : buttonArrayList) {
                                    btn.setEnabled(true);
                                }
                            }
                        }
                    }
                }).create().show();
    }

    @Override
    public void onBackPressed() {
        exitCount++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Press back again to exit the application", Toast.LENGTH_LONG).show();
            }
        });

        if(exitCount == 2) {
            if(multiplayer) {
                smsManager.sendTextMessage(otherPlayerNumber, null, "exit", null, null);
            }
            System.exit(0);
        }
    }

    private boolean checkBoardCondition() {
        turns++;
        String p1 = String.valueOf(players[0].symbol);
        String p2 = String.valueOf(players[1].symbol);
        if        ((buttons[0].getText().equals(p1) && buttons[1].getText().equals(p1) && buttons[2].getText().equals(p1)
                || (buttons[3].getText().equals(p1) && buttons[4].getText().equals(p1) && buttons[5].getText().equals(p1))
                || (buttons[6].getText().equals(p1) && buttons[7].getText().equals(p1) && buttons[8].getText().equals(p1))
                || (buttons[0].getText().equals(p1) && buttons[3].getText().equals(p1) && buttons[6].getText().equals(p1))
                || (buttons[1].getText().equals(p1) && buttons[4].getText().equals(p1) && buttons[7].getText().equals(p1))
                || (buttons[2].getText().equals(p1) && buttons[5].getText().equals(p1) && buttons[8].getText().equals(p1))
                || (buttons[0].getText().equals(p1) && buttons[4].getText().equals(p1) && buttons[8].getText().equals(p1))
                || (buttons[2].getText().equals(p1) && buttons[4].getText().equals(p1) && buttons[6].getText().equals(p1)))) {
            winner = true;
            winnerFound(players[0]);
        } else if ((buttons[0].getText().equals(p2) && buttons[1].getText().equals(p2) && buttons[2].getText().equals(p2))
                || (buttons[3].getText().equals(p2) && buttons[4].getText().equals(p2) && buttons[5].getText().equals(p2))
                || (buttons[6].getText().equals(p2) && buttons[7].getText().equals(p2) && buttons[8].getText().equals(p2))
                || (buttons[0].getText().equals(p2) && buttons[3].getText().equals(p2) && buttons[6].getText().equals(p2))
                || (buttons[1].getText().equals(p2) && buttons[4].getText().equals(p2) && buttons[7].getText().equals(p2))
                || (buttons[2].getText().equals(p2) && buttons[5].getText().equals(p2) && buttons[8].getText().equals(p2))
                || (buttons[0].getText().equals(p2) && buttons[4].getText().equals(p2) && buttons[8].getText().equals(p2))
                || (buttons[2].getText().equals(p2) && buttons[4].getText().equals(p2) && buttons[6].getText().equals(p2))) {
            winner = true;
            winnerFound(players[1]);
        } else {
            winner = false;
            noWinner();
        }

        if (current == 0) {
            current = 1;
        } else {
            current = 0;
        }
        return true;
    }

    public void winnerFound(Player player) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!multiplayer) {
                    main.setBackgroundColor(Color.GREEN);
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Winner!");
                    builder.setCancelable(false);
                    builder.setMessage(player.name + " wins!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
                    builder.create().show();
                } else {
                    pd.dismiss();
                    if(isPlayer1 && (player.name.equals(players[0].name))) {
                        main.setBackgroundColor(Color.GREEN);
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Winner!");
                        builder.setCancelable(false);
                        builder.setMessage("You win!");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                        }).create().show();
                    } else if(!isPlayer1 && (player.name.equals(players[0].name))) {
                        main.setBackgroundColor(Color.RED);
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Loser!");
                        builder.setCancelable(false);
                        builder.setMessage("You lose!");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                        }).create().show();
                    }
                }
            }
        });
    }

    public boolean noWinner() {
        if(turns == 9) {
            pd.dismiss();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(turns == 9) {
                    main.setBackgroundColor(Color.YELLOW);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Draw!");
                    builder.setMessage("Cat's Game! It's a draw!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
                    builder.create().show();
                }
            }
        });
        return !winner;
    }

    public void receiveMessage(String number, String message) {
        if (message.equals("initiate")) {
            otherPlayerNumber = number;
            singleton.setOtherPlayerNumber(number);
            builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Game Request")
                    .setMessage(number + " wants to play! Accept?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            singleton.setFlag(1);
                            reset();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            smsManager.sendTextMessage(number, null, "rejected", null, null);
                            multiplayer = false;
                            reset();
                        }
                    }).create().show();
        }

        pd.dismiss();
        if (message.equals("accepted")) {
            builder  = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Accepted")
                    .setMessage(otherPlayerNumber + " accepted your game request!")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[1] = new Player(otherPlayerNumber, R.drawable.blue_o);
                            isPlayer1 = true;
                            initPlayer1();
                        }
                    }).create().show();
        }

        if (message.equals("rejected")) {
            pd.dismiss();
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Rejected")
                    .setMessage(otherPlayerNumber + " denied your game request.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    }).create().show();
        }
        if (message.equals("ready")) {
            pd.dismiss();
            otherPlayerReady = true;
            if(selfReady) {
                current = 1;
                turnLabel.setText(String.format("%s%s", players[0].name, getString(R.string.your_turn)));
                if(!isPlayer1) {
                    showWaitingDialog();
                }
                startButton.setText(R.string.game_in_progress);
                startButton.setEnabled(false);
                for (TTTButton btn : buttonArrayList) {
                    btn.setEnabled(true);
                }
            }
        }

        if (message.equals("btn1")) {
            pd.dismiss();
            btn1.callOnClick();
        } else if (message.equals("btn2")) {
            pd.dismiss();
            btn2.callOnClick();
        } else if (message.equals("btn3")) {
            pd.dismiss();
            btn3.callOnClick();
        } else if (message.equals("btn4")) {
            pd.dismiss();
            btn4.callOnClick();
        } else if (message.equals("btn5")) {
            pd.dismiss();
            btn5.callOnClick();
        } else if (message.equals("btn6")) {
            pd.dismiss();
            btn6.callOnClick();
        } else if (message.equals("btn7")) {
            pd.dismiss();
            btn7.callOnClick();
        } else if (message.equals("btn8")) {
            pd.dismiss();
            btn8.callOnClick();
        } else if (message.equals("btn9")) {
            pd.dismiss();
            btn9.callOnClick();
        }

        if (message.equals("exit")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Exiting game")
                            .setMessage("Other player has disconnected.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    reset();
                                }
                            }).create().show();
                }
            });
        }
    }

    public void setupMultiplayer() {
        View phoneNumberView = inflater.inflate(R.layout.setup_multiplayer, null);
        EditText phoneText = (EditText) phoneNumberView.findViewById(R.id.phoneText);

        builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Multiplayer Setup")
                .setView(phoneNumberView)
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        otherPlayerNumber = phoneText.getText().toString();
                        if(!isSmsPermissionGranted()) {
                            requestReadAndSendSmsPermission();
                        } else {
                            smsManager.sendTextMessage(otherPlayerNumber, null, "initiate", null, null);
                            pd.setMessage("Awaiting response...");
                            pd.show();
                            pd.setCancelable(false);
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create().show();
    }

    public void reset() {
        multiplayer = false;
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
        finish();
    }

    /** * Check if we have SMS permission */
    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    /** * Request runtime SMS permission */
    private void requestReadAndSendSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE}, 1);
    }

    public void showWaitingDialog() {
        pd.setMessage("Waiting for other player...");
        pd.setCancelable(false);
        pd.show();
    }
}