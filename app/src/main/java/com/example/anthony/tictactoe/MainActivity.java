package com.example.anthony.tictactoe;

import android.Manifest;
import android.support.v4.content.LocalBroadcastManager;
import java.util.Locale;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.telephony.PhoneNumberUtils;
import android.net.Uri;
import android.provider.Settings;
import java.util.UUID;
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

    private AlertDialog replyEntry;
    private String lastAutomaticReply = "No game text seen during this wait.";
    private final BroadcastReceiver gameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra("number");
            String command = intent.getStringExtra("command");
            if (multiplayer && otherPlayerNumber != null) {
                lastAutomaticReply = describeAutomaticReply(number, command);
            }
            receiveMessage(number, command);
            if (inviteProgress != null && inviteProgress.isShowing()) updateInviteMessage();
            if (pd.isShowing()) updateWaitMessage();
        }
    };
    SmsManager smsManager = SmsManager.getDefault();

    View main;

    ProgressDialog pd;
    AlertDialog.Builder builder;

    private static final int SMS_PERMISSION_REQUEST = 1;
    private static final String INVITE_SENT = "com.example.anthony.tictactoe.INVITE_SENT";
    private String pendingPermissionNumber;
    private String inviteId;
    private boolean inviteSent;
    private ProgressDialog inviteProgress;
    private final BroadcastReceiver inviteResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (inviteId == null || !inviteId.equals(intent.getStringExtra("invite"))) return;
            int result = getResultCode();
            if (result == Activity.RESULT_OK) {
                inviteSent = true;
                showInviteProgress();
            } else {
                String reason;
                switch (result) {
                    case SmsManager.RESULT_ERROR_RADIO_OFF: reason = "The mobile radio is off."; break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE: reason = "Your phone has no mobile service."; break;
                    case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED: reason = "Android's SMS sending limit was reached."; break;
                    default: reason = "Android returned SMS error " + result + ". Check mobile service and your default SIM for text messages.";
                }
                inviteFailed(reason);
            }
        }
    };

    int exitCount = 0;

    Singleton singleton = Singleton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(inviteResult, new IntentFilter(INVITE_SENT),
                getPackageName() + ".permission.INVITE_RESULT", null);
        inflater = MainActivity.this.getLayoutInflater();
        main = inflater.inflate(R.layout.activity_main, null);
        setContentView(main);

        pd = new ProgressDialog(MainActivity.this);
        pd.setButton(DialogInterface.BUTTON_NEUTRAL, "Enter received reply", (DialogInterface.OnClickListener) null);

        LocalBroadcastManager.getInstance(this).registerReceiver(gameReceiver, new IntentFilter(SMSReceiver.GAME_COMMAND));

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

        if (savedInstanceState != null) {
            otherPlayerNumber = savedInstanceState.getString("inviteNumber");
            pendingPermissionNumber = savedInstanceState.getString("pendingPermissionNumber");
            inviteId = savedInstanceState.getString("inviteId");
            inviteSent = savedInstanceState.getBoolean("inviteSent");
            if (pendingPermissionNumber != null || inviteId != null) {
                multiplayer = true;
                startButton.setEnabled(false);
                if (inviteId != null) showInviteProgress();
            }
        } else if (!isSmsPermissionGranted()) {
            requestSmsPermissions();
        }

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

    private boolean isOpponent(String number) {
        if (number == null || otherPlayerNumber == null) return false;
        if (PhoneNumberUtils.compare(otherPlayerNumber, number)) return true;
        // Compare complete international numbers when the carrier and the dialer use different formats.
        android.telephony.TelephonyManager telephony =
                (android.telephony.TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String country = telephony == null ? "" : telephony.getNetworkCountryIso();
        if (country == null || country.isEmpty()) country = Locale.getDefault().getCountry();
        String region = country.toUpperCase(Locale.ROOT);
        String expected = PhoneNumberUtils.formatNumberToE164(otherPlayerNumber, region);
        return expected != null && expected.equals(PhoneNumberUtils.formatNumberToE164(number, region));
    }

    private String describeAutomaticReply(String number, String command) {
        if (!isOpponent(number)) return "Game text arrived from a different number than the opponent.";
        if (command.startsWith("btn")) {
            if (!selfReady || !otherPlayerReady) return command + " arrived before both players were ready.";
            if (current != (isPlayer1 ? 2 : 1)) return command + " arrived during your turn.";
            if (!buttons[command.charAt(3) - '1'].getStatus().isEmpty()) return command + " named an occupied square.";
        }
        return "Received " + command + " from the opponent.";
    }

    private String receiveStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) return "Receive SMS permission is off.";
        return lastAutomaticReply;
    }

    private void updateInviteMessage() {
        String state = inviteSent ? "Invitation sent. Waiting for the other player..." : "Sending invitation by SMS...";
        inviteProgress.setMessage(state + "\n\n" + receiveStatus());
    }

    private void updateWaitMessage() {
        pd.setMessage("Waiting for the other player...\n\n" + receiveStatus());
    }

    public void receiveMessage(String number, String message) {
        if (message.equals("initiate")) {
            if (multiplayer || current != 0) return;
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

        if (!multiplayer || !isOpponent(number)) return;

        if (inviteId != null && (message.equals("accepted") || message.equals("ready"))) {
            clearInvite();
            closeReplyEntry();
            isPlayer1 = true;
            otherPlayerReady = message.equals("ready");
            players[1] = new Player(otherPlayerNumber, R.drawable.blue_o);
            initPlayer1();
            return;
        }

        if (message.equals("rejected")) {
            if (inviteId == null) return;
            clearInvite();
            closeReplyEntry();
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
            if (otherPlayerReady || inviteId != null) return;
            closeReplyEntry();
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

        if (message.matches("btn[1-9]")) {
            if (!selfReady || !otherPlayerReady || current != (isPlayer1 ? 2 : 1)) return;
            TTTButton square = buttons[message.charAt(3) - '1'];
            if (!square.getStatus().isEmpty()) return;
            closeReplyEntry();
            pd.dismiss();
            square.callOnClick();
        }

        if (message.equals("exit")) {
            closeReplyEntry();
            clearInvite();
            pd.dismiss();
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

        if (otherPlayerNumber != null) phoneText.setText(otherPlayerNumber);
        builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Multiplayer Setup")
                .setView(phoneNumberView)
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        otherPlayerNumber = PhoneNumberUtils.stripSeparators(phoneText.getText().toString().trim());
                        if (!PhoneNumberUtils.isGlobalPhoneNumber(otherPlayerNumber)) {
                            inviteFailed("Enter a phone number, including the country code if needed.");
                        } else if (isSmsPermissionGranted()) {
                            sendInvite();
                        } else {
                            pendingPermissionNumber = otherPlayerNumber;
                            requestSmsPermissions();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
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

    private boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code != SMS_PERMISSION_REQUEST || pendingPermissionNumber == null) return;
        otherPlayerNumber = pendingPermissionNumber;
        pendingPermissionNumber = null;
        if (isSmsPermissionGranted()) {
            sendInvite();
        } else {
            new AlertDialog.Builder(this).setTitle("SMS permission needed")
                    .setMessage("Multiplayer needs permission to send and receive text messages. Check SMS in the app's permission settings, then try the invite again.")
                    .setPositiveButton("App settings", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName())));
                        setupMultiplayer();
                    }).setNegativeButton("Cancel", (dialog, which) -> reset()).setCancelable(false).show();
        }
    }

    private void sendInvite() {
        lastAutomaticReply = "No game text seen during this wait.";
        inviteId = UUID.randomUUID().toString();
        inviteSent = false;
        Intent result = new Intent(INVITE_SENT).setPackage(getPackageName())
                .putExtra("invite", inviteId);
        PendingIntent sent = PendingIntent.getBroadcast(this, 0, result,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        showInviteProgress();
        try {
            smsManager.sendTextMessage(otherPlayerNumber, null, "initiate", sent, null);
        } catch (SecurityException | IllegalArgumentException | UnsupportedOperationException ex) {
            sent.cancel();
            inviteFailed("Android could not send the invitation. Check SMS permission, the phone number and your SIM.");
        }
    }

    private void showInviteProgress() {
        if (inviteProgress == null) {
            inviteProgress = new ProgressDialog(this);
            inviteProgress.setButton(DialogInterface.BUTTON_NEUTRAL, "Enter received reply", (DialogInterface.OnClickListener) null);
            inviteProgress.setCancelable(true);
            inviteProgress.setCanceledOnTouchOutside(false);
            inviteProgress.setOnCancelListener(dialog -> { clearInvite(); reset(); });
        }
        updateInviteMessage();
        inviteProgress.show();
        enableReplyEntry(inviteProgress);
    }

    private void clearInvite() {
        inviteId = null;
        inviteSent = false;
        if (inviteProgress != null) inviteProgress.dismiss();
    }

    private void inviteFailed(String reason) {
        clearInvite();
        new AlertDialog.Builder(this).setTitle("Invitation not sent").setMessage(reason)
                .setPositiveButton("Try again", (dialog, which) -> setupMultiplayer())
                .setNegativeButton("Cancel", (dialog, which) -> reset()).setCancelable(false).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("inviteNumber", otherPlayerNumber);
        state.putString("pendingPermissionNumber", pendingPermissionNumber);
        state.putString("inviteId", inviteId);
        state.putBoolean("inviteSent", inviteSent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(inviteResult);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gameReceiver);
        if (replyEntry != null) replyEntry.dismiss();
        if (inviteProgress != null) inviteProgress.dismiss();
        super.onDestroy();
    }

    private void closeReplyEntry() {
        if (replyEntry != null) replyEntry.dismiss();
        replyEntry = null;
    }

    private void enableReplyEntry(ProgressDialog progress) {
        Button button = progress.getButton(DialogInterface.BUTTON_NEUTRAL);
        button.setVisibility(View.VISIBLE);
        button.setText("Enter received reply");
        button.setOnClickListener(v -> {
            boolean inviting = inviteId != null;
            boolean awaitingReady = !otherPlayerReady;
            EditText input = new EditText(this);
            input.setSingleLine();
            input.setHint(inviting ? "accepted or ready" : awaitingReady ? "ready" : "btn1 to btn9");
            replyEntry = new AlertDialog.Builder(this)
                    .setTitle("Enter received reply")
                    .setMessage("Type the game message your opponent sent you in Messages.")
                    .setView(input).setPositiveButton("Use reply", null)
                    .setNegativeButton("Cancel", null).show();
            replyEntry.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(use -> {
                String command = input.getText().toString().trim().toLowerCase(Locale.ROOT);
                boolean valid = inviting ? command.equals("accepted") || command.equals("ready")
                        : awaitingReady ? command.equals("ready") : command.matches("btn[1-9]");
                if (!valid) {
                    input.setError("Enter the reply shown in the hint.");
                    return;
                }
                if (command.startsWith("btn") && !buttons[command.charAt(3) - '1'].getStatus().isEmpty()) {
                    input.setError("That square is already occupied.");
                    return;
                }
                closeReplyEntry();
                receiveMessage(otherPlayerNumber, command);
            });
        });
    }

    public void showWaitingDialog() {
        lastAutomaticReply = "No game text seen during this wait.";
        updateWaitMessage();
        pd.setCancelable(false);
        pd.show();
        enableReplyEntry(pd);
    }
}