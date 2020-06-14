import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


//based on code snippets from original Java course
public class ChatClient {

  private TextArea output;
  private TextField input;
  private Button sendButton;
  private Button quitButton;
  private Frame frame;
  private TextField userName;
  private Button confirmButton;
  private String confirmUserName;
  
  private Socket connection = null;
  private BufferedReader serverIn = null;
  private PrintStream serverOut = null;

  public ChatClient() {
    output = new TextArea(10,50);
    input = new TextField(50);
    sendButton = new Button("Send");
    quitButton = new Button("Quit");
    userName = new TextField(20);
    confirmButton = new Button("Confirm");
    
  }
  
  private void doConnect(){
      String serverIP = System.getProperty("serverIP", "127.0.0.1");
      String serverPort = System.getProperty("serverPort", "2000");
      try {
          connection = new Socket(serverIP, Integer.parseInt(serverPort));
          InputStream is = connection.getInputStream();
          InputStreamReader isr = new InputStreamReader(is);
          serverIn = new BufferedReader(isr);
          
          serverOut = new PrintStream(connection.getOutputStream());
          
          Thread t = new Thread(new RemoteReader());
          t.start();
      } catch (IOException ex) {    
        System.err.println("Unable to connect to server!");
        ex.printStackTrace(); 
      }
  }

  public void launchFrame() {
    frame = new Frame("PPC Chat");
    sendButton.setEnabled(false);

    // Use the Border Layout for the frame
    frame.setLayout(new BorderLayout());

    frame.add(output, BorderLayout.WEST);
    frame.add(input, BorderLayout.SOUTH);

    // Create the button panel
    Panel p1 = new Panel(); 
    p1.setLayout(new GridLayout(4,2));
    p1.add(sendButton);
    p1.add(quitButton);
    p1.add(userName);
    p1.add(confirmButton);

    // Add the button panel to the center
    frame.add(p1, BorderLayout.CENTER);

    // Create menu bar and File menu
    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem quitMenuItem = new MenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	System.exit(0);
      }
    });
    file.add(quitMenuItem);
    mb.add(file);
    frame.setMenuBar(mb);

    // Add Help menu to menu bar
    Menu help = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    aboutMenuItem.addActionListener(new AboutHandler());
    help.add(aboutMenuItem);
    mb.setHelpMenu(help);

    // Attach listener to the appropriate components
    sendButton.addActionListener(new SendHandler());
    input.addActionListener(new SendHandler());
    frame.addWindowListener(new CloseHandler());
    quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
    });
    
    confirmButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { 
            if(userName.getText().compareTo("") == 0){
                confirmUserName = "User";
            }else{
                confirmUserName = userName.getText();      
            }
            
            sendButton.setEnabled(true);
            confirmButton.setEnabled(false);
            userName.setEnabled(false);
            doConnect();    
        }
    });

    frame.pack();
    frame.setVisible(true);  
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
    frame.setLocation(x, y);
  }

  private class SendHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String text = input.getText();
      text = confirmUserName +": " + text + "\n";
      serverOut.print(text);
      input.setText("");
    }
  }
  
  private class RemoteReader implements Runnable{
      public void run(){
         try{
          while(true){
             String nextLine = serverIn.readLine();
             output.append(nextLine+"\n");
            }
         }catch(Exception e){
             System.err.println("Error while reading from server.");
             e.printStackTrace();
         }
      }
  }

  private class CloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }

  private class AboutHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // Create the aboutDialog when it is requested
      JOptionPane.showMessageDialog(frame, "The ChatClient is a neat tool that allows you to talk to other ChatClients via a ChatServer");
    }
  }


  public static void main(String[] args) {
    ChatClient c = new ChatClient();
    c.launchFrame();
  }
}