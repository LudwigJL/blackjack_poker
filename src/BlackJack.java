import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    private class Card {
        String value;
        String type;

        Card(String value, String type){
            this.value = value;
            this.type = type;
        }

        public String toString(){
            return value + "-" + type;
        }

        public int getValue(){
            if ("AJQK".contains(value)){
                if (value == "A"){
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value);
        }
        
        public boolean isAce(){
            return value == "A";
        }

        public String getImagePath(){
            return "/cards/" + toString() + ".png";
        }
    }

    public class Player{
        String name;
        int accountBalance;

        Player(String name, int accountBalance){
            this.name = name;
            this.accountBalance = accountBalance;
        }

        public String StringOfAccountBalance() {
            return String.valueOf(accountBalance);
        }

        public int removeBet(int bet){
            return accountBalance -= bet;
        }
    }

    ArrayList<Card> deck; 
    Random random = new Random();

    //Player account
    Player currentPlayer = new Player("Adam Weston", 10000);
    int currentBet;

    //Dealer 
    ArrayList<Card> dealerHand;
    Card hiddenCard;
    int dealerSum;
    int dealerAceCount;

    //Player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //fönster
    int boardWidth = 800;
    int boardHeight = 800;

    int cardWidth = 110;
    int cardHeight = 154;

    JFrame frame = new JFrame("BlackJack Poker");
    JPanel gamePanel = new JPanel(){
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        try{
        //draw Hidden card
        Image hiddenCardImage = new ImageIcon(getClass().getResource("cards/BACK.png")).getImage();
        if (!stayButton.isEnabled()){
            hiddenCardImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
        }
        g.drawImage(hiddenCardImage, 20, 20, cardWidth, cardHeight, null);

        //draw Dealerhand
        for(int i = 0; i < dealerHand.size(); i++){
            Card card = dealerHand.get(i);
            Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
            g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
        }

        //draw Playerhand
        for(int i = 0; i < playerHand.size(); i++){
            Card card = playerHand.get(i);
            Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
            g.drawImage(cardImage, 20 + (cardWidth + 5)*i, 420, cardWidth, cardHeight, null);
            
        }

        //Draw new balance
        g.setFont(new Font("Helvetica", Font.PLAIN, 30));
        g.setColor(Color.white);
        g.drawString(currentPlayer.name, 580, 630);
        g.drawString(currentPlayer.StringOfAccountBalance(), 580, 665);


        
        //Draw game result

        if (!stayButton.isEnabled()){
            dealerSum = reduceDealerAce();
            playerSum = reducePlayerAce();
            System.out.println("STAY: ");
            System.out.println(dealerSum);
            System.out.println(playerSum);

            String message = "";
            if (playerSum > 21){
                message = "You Bust!";
            }
            else if (dealerSum > 21){
                message = "Dealer Bust!";
            }
            else if (playerSum == dealerSum){
                message = "Draw!";
            }
            else if (playerSum > dealerSum){
                message = "You Win!";
            }
            else if (playerSum < dealerSum){
                message = "You Lose!";
            }

            g.setFont(new Font("Helvetica", Font.PLAIN, 40));
            g.setColor(Color.white);
            g.drawString(message, 320, 340);
        }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
};

    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton replayButton = new JButton("Play Again!");
    JButton confirmBetButton = new JButton("Play again! Bet:");
    JTextField placedBet = new JTextField();


    BlackJack() {
    
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);

        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);

        confirmBetButton.setFocusable(false);
        confirmBetButton.setEnabled(false);
        buttonPanel.add(confirmBetButton);

        placedBet.setPreferredSize(new Dimension(70, 30)); 
        buttonPanel.add(placedBet);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        
        
        gamePanel.setLayout(null); 
        
        startGame();

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Card card = deck.remove(deck.size()- 1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                gamePanel.repaint();

                if (reducePlayerAce() > 21){
                    hitButton.setEnabled(false);
                }

                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);
                confirmBetButton.setEnabled(true);

                while (dealerSum < 17){
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }

                gamePanel.repaint();
            }
        });

        gamePanel.repaint();


        confirmBetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                

                //Wrap with try catch
                int currentBet = Integer.valueOf(placedBet.getText());
                System.out.println("BET");
                System.out.println(currentBet);

                //*** Insufficient funds
                currentPlayer.removeBet(currentBet);
                    
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                confirmBetButton.setEnabled(false);

                startGame();

                gamePanel.repaint();
            }
        });      
    }

    public void startGame(){
        buildDeck();
        shuffleDeck();

        //Dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1);
        dealerSum = hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;

        dealerHand.add(card);
        System.out.println("BALANCE");
        System.out.println(currentPlayer.accountBalance);

        System.out.println("DEALER:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //Player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++){
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount = card.isAce() ? 1 : 0;

            playerHand.add(card);
        }

        System.out.println("PLAYER");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);

    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++){
            for (int j = 0; j < values.length; j++){
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }
        
    }

    public void shuffleDeck(){

        for (int i = 0; i < deck.size(); i++){
            int randomInt = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(randomInt);

            deck.set(i, randomCard);
            deck.set(randomInt, currCard);

        }
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0){
            playerSum -= 10;
            playerAceCount -= 1;
        }

        return playerSum;
    }

    public int reduceDealerAce(){
        while(dealerSum > 21 && dealerAceCount > 0){
            dealerSum -= 10;
            dealerAceCount -= 1;
        }

        return dealerSum;
    }
}
