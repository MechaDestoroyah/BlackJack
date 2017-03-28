import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;

import blackjack.game.Card;
import blackjack.message.*;


public class BlackJackClient extends JFrame {
	
	
	private static final long serialVersionUID = 1L;
	private Socket s;
	private String username;
	private JTextArea textArea=new JTextArea(5,30);
	private JTextArea reply=new JTextArea(5,30);
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean joined=false;
	public BlackJackClient(){
		try {
			initialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initialize() throws IOException, ConnectException, UnknownHostException{
		JFrame frame= new JFrame("BlackJack");
		JScrollPane scrollPane = new JScrollPane(textArea);
		JButton send =new JButton("Send");
		JButton hit =new JButton("Hit");
		JButton stay = new JButton("Stay");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scrollPane.setPreferredSize(new Dimension(300, 300));
		frame.add(scrollPane, BorderLayout.NORTH);
		frame.add(reply, BorderLayout.CENTER);
		
		username= JOptionPane.showInputDialog("Enter your name");
		try{
			s= new Socket("52.35.72.251", 8989);
		}catch(ConnectException e){
			e.printStackTrace();
		}
		
		out= new ObjectOutputStream(s.getOutputStream());
		out.writeObject(MessageFactory.getLoginMessage(username));
		out.flush();
		in=new ObjectInputStream(s.getInputStream());
		
		new Thread(new Reader(in)).start();;
		
		reply.addKeyListener(new KeyListener(){
			@Override
            public void keyTyped(KeyEvent e) {
            }
			 @Override
	            public void keyPressed(KeyEvent e) {
	                if ((e.getKeyCode() == KeyEvent.VK_ENTER) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
	                	try{
	                		addReply();
	                	}catch(IOException er){
	                		er.printStackTrace();
	                	}
	                }
	            }
			 @Override
	            public void keyReleased(KeyEvent e) {
	            }
		});
		
		frame.add(send, BorderLayout.SOUTH);
		frame.add(hit, BorderLayout.EAST);
		frame.add(stay, BorderLayout.WEST);
		frame.pack();
		frame.setVisible(true);
		
		
		
		
		send.addActionListener((e) -> {
			try{
			addReply();
			}catch (IOException er){
				er.printStackTrace();
			}
		});
		
		hit.addActionListener((e)->{
			try{
				out.writeObject(MessageFactory.getHitMessage());
				out.flush();
			}catch (IOException er){
				er.printStackTrace();
			}
		});
		hit.addActionListener((e)->{
			try{
				if(joined){
				out.writeObject(MessageFactory.getStayMessage());
				out.flush();
				}else{
					joined=true;
					out.writeObject(MessageFactory.getJoinMessage());
					out.flush();
					
				}
			}catch (IOException er){
				er.printStackTrace();
			}
		});
		
		
		
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
	}
	
	private void addReply() throws IOException{
		out.writeObject(MessageFactory.getChatMessage(reply.getText()));
		out.flush();
		reply.setText("");
	}
	
	private void GameAction(GameActionMessage gam){
		switch(gam.getAction()){
		case BUST:
			break;
		case HIT:
			break;
		case STAY:
			break;
		case WIN:
			break;
		default:
			break;
		
		}
	}
	private void GameState(GameStateMessage gsm){
		switch(gsm.getRequestedState()){
		case JOIN:
			textArea.append("You joined the game\n");
			break;
		case START:
			textArea.append("You started a game\n");
			break;
		default:
			break;
		
		}
	}
		
		public static void main(String[] args){
			new BlackJackClient();
		}
		
		private class Reader implements Runnable {
			private ObjectInputStream input;
			
			
			public Reader(ObjectInputStream is) {
				input = is;
				
			}
			
			
			public void run() {
				
				
				try {
					while (true) {
						Object obj=(Object) input.readObject();
						Message m=(Message)obj;
						switch (m.getType()){
						case LOGIN: 
							break;
						case ACK:
							StatusMessage am=(StatusMessage) m;
							textArea.append(am.getUsername()+ " login successful\n");
							
							break;
						case CARD:
							CardMessage c=(CardMessage) m;
							Card card =c.getCard();
							textArea.append("You drew a(n) " +card.toString());
							break;
						case CHAT:
							ChatMessage cm=(ChatMessage) m;
							textArea.append(cm.getUsername()+" : "+ cm.getText()+"\n");
							break;
						case DENY:
							StatusMessage dm=(StatusMessage) m;
							
							textArea.append(dm.getUsername()+", your login was terrible!\n");
							break;
						case GAME_ACTION:
							GameActionMessage gam=(GameActionMessage) m;
							GameAction(gam);
							break;
						case GAME_STATE:
							GameStateMessage gsm=(GameStateMessage) m;
							GameState(gsm);
							break;
						default:
							break;
						
						}
						
					}
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
					
				
			}
		}
	

}
