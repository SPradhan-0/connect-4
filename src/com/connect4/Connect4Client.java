package com.connect4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class Connect4Client extends JFrame {
    private String user;
    private int[][] board = new int[6][7];
    private char playerSymbol = 'X';
    private char serverSymbol = 'O';
    private String lastValidStrategy;

    private JLabel gameStateLabel;
    private JLabel usernameLabel;
    private JButton[][] gridButtons = new JButton[6][7];
    private JComboBox<String> strategyCombo;
    private JButton newGameButton;
    private JButton refreshButton;
    private JLabel errorLogLabel;

    public Connect4Client() {
        if (!promptForUsername()) {
            System.exit(0);
        }

        setTitle("Connect 4 Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupNorthPanel();
        setupCenterPanel();
        setupEastPanel();
        setupSouthPanel();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loadStrategies();
        startNewGame();
    }

    private boolean promptForUsername() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Enter username (alphanumeric, max 32 chars):", "Username Entry", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return false;
            if (input.matches("^[a-zA-Z0-9]{1,32}$")) {
                user = input;
                return true;
            }
            JOptionPane.showMessageDialog(this, "Invalid username. Please use alphanumeric characters and max length 32.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        gameStateLabel = new JLabel("Starting new game...");
        usernameLabel = new JLabel("User: " + user);
        northPanel.add(gameStateLabel, BorderLayout.WEST);
        northPanel.add(usernameLabel, BorderLayout.EAST);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(northPanel, BorderLayout.NORTH);
    }

    private void setupCenterPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(6, 7));
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(80, 80));
                button.setOpaque(true);
                button.setBorderPainted(true);
                button.putClientProperty("column", col);
                button.addActionListener(e -> {
                    int column = (int) ((JButton) e.getSource()).getClientProperty("column");
                    handleMove(column);
                });
                gridButtons[row][col] = button;
                gridPanel.add(button);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    private void setupEastPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        controlPanel.add(new JLabel("Server Strategy:"));
        strategyCombo = new JComboBox<>();
        strategyCombo.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                handleStrategyChange((String) e.getItem());
            }
        });
        controlPanel.add(strategyCombo);

        controlPanel.add(Box.createVerticalGlue());

        newGameButton = new JButton("New Game");
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.addActionListener(e -> startNewGame());
        controlPanel.add(newGameButton);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        refreshButton = new JButton("Refresh");
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshButton.addActionListener(e -> handleRefresh());
        controlPanel.add(refreshButton);

        add(controlPanel, BorderLayout.EAST);
    }

    private void setupSouthPanel() {
        errorLogLabel = new JLabel("");
        errorLogLabel.setForeground(Color.RED);
        errorLogLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLogLabel.setFont(errorLogLabel.getFont().deriveFont(Font.BOLD));
        add(errorLogLabel, BorderLayout.SOUTH);
    }

    private void handleMove(int column) {
        setGridEnabled(false);
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                return Connect4API.move(user, column);
            }

            @Override
            protected void done() {
                try {
                    processResponse(get());
                } catch (Exception e) {
                    showError("Error processing move: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void startNewGame() {
        setGridEnabled(false);
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                return Connect4API.newGame(user);
            }

            @Override
            protected void done() {
                try {
                    processResponse(get());
                } catch (Exception e) {
                    showError("Error starting new game: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void handleRefresh() {
        refreshButton.setEnabled(false);
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                return Connect4API.status(user);
            }

            @Override
            protected void done() {
                try {
                    processResponse(get());
                    refreshButton.setEnabled(true);
                } catch (Exception e) {
                    showError("Error refreshing status: " + e.getMessage());
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadStrategies() {
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                return Connect4API.listStrats();
            }

            @Override
            protected void done() {
                try {
                    Response resp = get();
                    if ("ok".equals(resp.status) && resp.strategies != null) {
                        for (String s : resp.strategies) {
                            strategyCombo.addItem(s);
                        }
                        lastValidStrategy = (String) strategyCombo.getSelectedItem();
                    }
                } catch (Exception e) {
                    showError("Error loading strategies: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void handleStrategyChange(String strategy) {
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() {
                return Connect4API.setStrat(user, strategy);
            }

            @Override
            protected void done() {
                try {
                    Response resp = get();
                    if ("ok".equals(resp.status)) {
                        gameStateLabel.setText("Strategy set to: " + strategy);
                        lastValidStrategy = strategy;
                        clearError();
                    } else {
                        showError(resp.message);
                        strategyCombo.setSelectedItem(lastValidStrategy);
                    }
                } catch (Exception e) {
                    showError("Error setting strategy: " + e.getMessage());
                    strategyCombo.setSelectedItem(lastValidStrategy);
                }
            }
        }.execute();
    }

    private void processResponse(Response resp) {
        if ("error".equals(resp.status)) {
            showError(resp.message);
            setGridEnabled(!resp.gameOver);
            return;
        }

        clearError();
        if (resp.board != null) {
            updateBoard(resp.board);
        }
        
        playerSymbol = resp.playerSymbol != 0 ? resp.playerSymbol : playerSymbol;
        serverSymbol = resp.serverSymbol != 0 ? resp.serverSymbol : serverSymbol;

        if (resp.gameOver) {
            String winnerMsg = "Draw";
            if ("player".equals(resp.winner)) winnerMsg = "Winner: Player (" + playerSymbol + ")";
            else if ("server".equals(resp.winner)) winnerMsg = "Winner: Server (" + serverSymbol + ")";
            gameStateLabel.setText(winnerMsg);
            setGridEnabled(false);
        } else {
            gameStateLabel.setText("Your Turn (" + playerSymbol + ")");
            setGridEnabled(true);
        }
    }

    private void updateBoard(int[][] board) {
        this.board = board;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                int val = board[r][c];
                if (val == 0) {
                    gridButtons[r][c].setBackground(Color.WHITE);
                    gridButtons[r][c].setText("");
                } else if (val == 1) {
                    gridButtons[r][c].setBackground(Color.RED);
                    gridButtons[r][c].setText(String.valueOf(playerSymbol));
                } else if (val == 2) {
                    gridButtons[r][c].setBackground(Color.YELLOW);
                    gridButtons[r][c].setText(String.valueOf(serverSymbol));
                }
            }
        }
    }

    private void setGridEnabled(boolean enabled) {
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                gridButtons[r][c].setEnabled(enabled);
            }
        }
    }

    private void showError(String message) {
        errorLogLabel.setText(message);
    }

    private void clearError() {
        errorLogLabel.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Connect4Client::new);
    }
}
