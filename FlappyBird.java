import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener, MouseListener {

    // Board Size
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image birdImg;
    Image backgroundImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;     // Scale ratio 1:6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityX = -4;     // Moves pipes left, simulates bird moving right.
    int velocityY = 0;      // Vertical movement, Up/Down.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;
    boolean paused = false;
    boolean showStartMenu = true;

    JButton playButton;
    JButton exitButton;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Load Images
        birdImg = new ImageIcon(getClass().getResource("images/flappybird.png")).getImage();
        backgroundImg = new ImageIcon(getClass().getResource("images/flappybirdbg.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("images/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("images/bottompipe.png")).getImage();

        // Bird
        bird = new Bird(birdImg);

        // Pipes
        pipes = new ArrayList<>();

        // Place Pipes Timer
        placePipesTimer = new Timer(1500, e -> placePipes());
        placePipesTimer.start();

        // Game Timer
        gameLoop = new Timer(1000 / 60, this);

        // Buttons
        playButton = new JButton("Play");
        exitButton = new JButton("Exit");
        initializeButtons();
    }

    public void initializeButtons() {
        int buttonWidth = (int) (boardWidth / 1.965f);
        int buttonHeight = (int) (boardHeight / 11.190f);
        int buttonSpacing = 10;

        int buttonX = (boardWidth - buttonWidth) / 2;
        int centerY = boardHeight / 2;

        int playButtonY = centerY - buttonHeight - buttonSpacing;
        int exitButtonY = centerY + buttonSpacing;

        // Customize Play Button
        playButton.setBounds(buttonX, playButtonY, buttonWidth, buttonHeight);
        playButton.setFont(new Font("JetBrains Mono", Font.BOLD, 24)); // JetBrains Mono font, bold, and larger
        playButton.setForeground(Color.WHITE); // White text
        playButton.setFocusPainted(false); // Remove focus border
        playButton.addActionListener(e -> startGame());
        playButton.setBackground(new Color(0, 255, 0)); // Green background for Play Button
        makeButtonRounded(playButton); // Apply rounded corners

        // Customize Exit Button
        exitButton.setBounds(buttonX, exitButtonY, buttonWidth, buttonHeight);
        exitButton.setFont(new Font("JetBrains Mono", Font.BOLD, 24)); // JetBrains Mono font, bold, and larger
        exitButton.setForeground(Color.WHITE); // White text
        exitButton.setFocusPainted(false); // Remove focus border
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setBackground(new Color(255, 0, 0)); // Red background for Exit Button
        makeButtonRounded(exitButton); // Apply rounded corners

        // Add buttons to panel
        setLayout(null); // Absolute positioning
        add(playButton);
        add(exitButton);

        revalidate();
        repaint();
    }

    // Utility method to round corners and center text
    public void makeButtonRounded(JButton button) {
        button.setOpaque(true); // Ensures background color is visible
        button.setContentAreaFilled(false); // Make background transparent
        button.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Custom button rendering with rounded corners and centered text
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the background with rounded corners
                g2.setColor(button.getBackground());
                g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 30, 30); // Rounded corners with radius of 30

                // Draw the text in the center (both x and y centered)
                g2.setColor(button.getForeground());
                FontMetrics fm = g2.getFontMetrics();
                String text = button.getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();  // Total height of the text

                // Horizontal centering
                int x = (button.getWidth() - textWidth) / 2;

                // Vertical centering (adjusted to align text properly)
                int y = (button.getHeight() - textHeight) / 2 + fm.getAscent();

                g2.drawString(text, x, y);

                g2.dispose();
            }
        });
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - (double) pipeHeight / 4 - Math.random() * ((double) pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showStartMenu) {
            drawStartMenu(g);
        } else {
            draw(g);
        }
    }

    public void drawStartMenu(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.setColor(Color.white);
        g.setFont(new Font("JetBrains Mono", Font.PLAIN, 50));

        String startText = "Flappy Bird";
        FontMetrics fm = g.getFontMetrics();
        int startTextWidth = fm.stringWidth(startText);
        int startTextX = (boardWidth - startTextWidth) / 2;
        int startTextY = (boardHeight / 4);

        g.drawString(startText, startTextX, startTextY);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("JetBrains Mono", Font.PLAIN, 32));
        g.drawString(String.valueOf((int) score), 10, 35);

        if (paused) {
            drawCenteredText(g, "Paused");
        } else if (gameOver) {
            drawCenteredText(g, "Game Over");
        }
    }

    public void drawCenteredText(Graphics g, String text) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int centerX = (boardWidth - textWidth) / 2;
        int centerY = boardHeight / 2;
        g.drawString(text, centerX, centerY);
    }

    public void startGame() {
        showStartMenu = false;

        // Remove buttons
        remove(playButton);
        remove(exitButton);

        repaint();

        // Start the game loop and pipe placement timer only once
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }

        if (!placePipesTimer.isRunning()) {
            placePipesTimer.start();
        }

        // Reset the game state
        score = 0;
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        paused = false;

        // Start countdown (optional)
        Timer countdownTimer = new Timer(1000, new ActionListener() {
            int countdown = 3;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (countdown > 0) {
                    // Optionally display a countdown here (e.g., "3", "2", "1")
                    countdown--;
                } else {
                    ((Timer) e.getSource()).stop(); // Stop the countdown timer
                }
            }
        });
        countdownTimer.start();
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&   //a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    //a's bottom left corner passes b's top left corner
    }

    public void move() {
        if (gameOver) {
            gameLoop.stop();          // Stop the game loop
            placePipesTimer.stop();   // Stop the pipe placement timer
            return;                   // Exit the method to freeze the game
        }

        /* Bird Movement */
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Prevent bird from going off-screen

        /* Pipe Movement */
        ArrayList<Pipe> toRemove = new ArrayList<>();

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            // Check for pipes that have moved past the bird
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;  // Score for passing pipes
            }

            // If pipe is off-screen, mark for removal
            if (pipe.x + pipe.width < 0) {
                toRemove.add(pipe);
            }

            // Check for collision
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Remove pipes that have gone off-screen
        pipes.removeAll(toRemove);

        // Check for bird falling below screen
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // Key & Mouse Events
    @Override
    public void keyPressed(KeyEvent e) {
        // Check for the correct key codes to make the bird move up
        if (e.getKeyCode() == KeyEvent.VK_SPACE ||
            e.getKeyCode() == KeyEvent.VK_W ||
            e.getKeyCode() == KeyEvent.VK_UP) {
            velocityY = -9;  // Move the bird up when any of these keys are pressed
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            } else if (paused) {
                paused = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // When the mouse is clicked, make the bird move up
        velocityY = -9; // Adjust the value to make the bird move upwards
        if (gameOver) {
            // Restart game when the bird is dead and mouse is clicked
            bird.y = birdY;
            velocityY = 0;
            pipes.clear();
            score = 0;
            gameOver = false;
            gameLoop.start();
            placePipesTimer.start();
        } else if (paused) {
            paused = false;
            gameLoop.start();
            placePipesTimer.start();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }
}
