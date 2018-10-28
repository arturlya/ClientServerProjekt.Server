package model;

import model.abitur.netz.Server;


import java.util.Random;

/**
 * Klasse des Servers zum TicTacToe spielen.
 */
public class TicTacToeServer extends Server {

    /** Anzahl der Clients auf dem Server*/
    private int numberOfClients;

    /** Anzahl der Clients, die eine neue Runde spielen wollen*/
    private int restartVote;

    /** Boolische Werte, die angeben, ob das Spiel vorbei ist oder Spieler 1 am Zug ist.*/
    private boolean player1turn, gameOver;

    /** Das Spielfeld, das in einem 2-Dimensionalen Array gespeichert wird*/
    private Field[][] map;

    private int prim = 0;

    private int N,e,phi;
    private Key publicKey;

    /**
     * Konstruktor der Klasse TicTacToeServer.
     * Erstellt einen Server auf dem Clients TicTacToe spielen können.
     *
     * @param port Port, den der Server belegt.
     */
    public TicTacToeServer(int port){
        super(port);
        System.out.println("running Server");
        restartVote = 0;
        numberOfClients =0;
        gameOver = false;
        map = new Field[3][3];
        createMap();


        publicKey = new Key();
        int p=0;
        int q=0;
        while(q==0){  //Solange ausführen, bis beide Vaiablen mit einer Zahl belegt sind
            primzahlen(1000); //1000=Primzahl darf nicht größer als 1000 sein
            if(!(prim==0) && !(prim==p)){
                if(p==0){
                    p=prim;
                    System.out.println(p);//TEST
                }else{
                    q=prim;
                    System.out.println(q);//TEST
                }
            }
        }

        //Schritt 3: Das Primzahlprodukt (N) bilden :
        N=p*q;

        //Schritt 4: Wert der phi-Funktion berechnen:
        phi=(p-1)*(q-1);

        //Schritt 5: Den öffentlichen Exponenten ermitteln (nötig zum codieren):
        e=(p*q)-phi; //Ist das richtig berechnet?


        publicKey.setKeys(e,N);
        System.out.println("Oeffentlicher Key:");
        System.out.println("e="+e);
        System.out.println("N="+N+"\n");

    }


    public void primzahlen(int bereich){
        Random random = new Random();
        for(int i=10;i<=bereich; i++){
            int tmp=0;
            for(int j=2; j<10; j++){
                if(i%j==0 && !(i==j)){
                    tmp++;
                }
            }
            if(tmp==0){
                if((1 + Math.abs(random.nextInt()) % 500)>480){
                    prim=i;
                    i=bereich;
                }
            }
        }
    }


    /**
     * Aktion des Servers bei Beitritt eines weiteren Clients.
     *
     * @param pClientIP IP des neuen Clients.
     * @param pClientPort Port des neuen Clients.
     */
    @Override
    public void processNewConnection(String pClientIP, int pClientPort) {
        switch(numberOfClients){
            case 0:
                send(pClientIP,pClientPort,"TEXTWarte auf Spieler");
                send(pClientIP,pClientPort,"SPIELER1");
                try {
                    //sendPrivateKeyData(pClientIP, pClientPort);
                    //sendPublicKeyData(pClientIP,pClientPort);
                    //send(pClientIP,pClientPort,infosPr);
                    //send(pClientIP,pClientPort,infosPu);

                }catch (Exception e){
                    System.err.println(e);
                }
                numberOfClients++;
                break;
            case 1:
                numberOfClients++;
                send(pClientIP,pClientPort,"SPIELER2");
                sendKeys(pClientIP,pClientPort);
                firstTurn();
                sendToAll("TEXTSpiel startet");
                if(player1turn){
                    sendToAll("TEXTSpieler 1 startet");
                    sendToAll("KREIS");
                }else{
                    sendToAll("TEXTSpieler 2 startet");
                    sendToAll("KREUZ");
                }
                break;
            case 2:
                send(pClientIP,pClientPort,"TEXTRunde ist schon voll");
                closeConnection(pClientIP,pClientPort);
                break;
        }
    }

    /**
     * Aktion des Servers bei einer Nachricht.
     *
     * @param pClientIP IP des aktuellen Clients.
     * @param pClientPort Port des aktuellen Clients.
     * @param pMessage Die empfangene Nachricht des Clients an den Server.
     */
    @Override
    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        if(pMessage.contains("ACTION")){
            String[] messageParts = pMessage.split("ACTION");
            for(int i=1;i<messageParts.length;i++){
                if(messageParts[i].contains("NEXT")){
                    String[] fields = messageParts[i].split("NEXT");
                    for(int j=1;j<fields.length;j++){
                        if(fields[j].contains("FIELD")){
                            String[] fieldData = fields[j].split("FIELD",3);
                            if( map[Integer.parseInt(fieldData[0])][Integer.parseInt(fieldData[1])].isEmpty()) {
                                if (Integer.parseInt(fieldData[2]) == 2) {
                                    sendToAll("KREIS");
                                    map[Integer.parseInt(fieldData[0])][Integer.parseInt(fieldData[1])].setCross();
                                } else if (Integer.parseInt(fieldData[2]) == 1) {
                                    sendToAll("KREUZ");
                                    map[Integer.parseInt(fieldData[0])][Integer.parseInt(fieldData[1])].setCircle();
                                }
                            }
                        }
                    }
                }
            }
        }else if(pMessage.contains("RESTART")){
            restartVote++;
            switch (restartVote){
                case 1:
                    sendToAll("TEXTWarte auf Spieler(1 verbleibend)");
                    break;
                case 2:
                    sendToAll("TEXTStarte neue Runde");
                    gameOver = false;
                    sendToAll("RESTART");
                    createMap();
                    sendToAll("UPDATE"+getMapInformation());
                    firstTurn();
                    for (int i = 0; i < 15; i++) {
                        sendToAll("TEXT   ");
                    }
                    if(player1turn == true){
                        sendToAll("TEXTSpieler 1 startet");
                        sendToAll("KREIS");
                    }else{
                        sendToAll("TEXTSpieler 2 startet");
                        sendToAll("KREUZ");
                    }
                    restartVote = 0;
                    sendToAll("UPDATE"+getMapInformation());
                    break;
            }
        }else if(pMessage.contains("CHAT")){
            try {
                sendToAll(pMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sendToAll("UPDATE"+getMapInformation());
        calculateWinner();

    }

    /**
     * Berechnet, ob ein und welcher Spieler gewonnen hat.
     * Sendet das Ergebnis an alle Clients.
     */
    private void calculateWinner(){
        for(int i = 0; i<map.length && !gameOver; i++) {
            for (int j = 0; j < map[i].length && !gameOver; j++) {
                if ((map[i][0].getValue() == 1 && map[i][1].getValue() == 1 && map[i][2].getValue() == 1)) {
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    gameOver = true;
                }else if ((map[0][j].getValue() == 1 && map[1][j].getValue() == 1 && map[2][j].getValue() == 1)) {
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    gameOver = true;

                }else if (map[0][0].getValue() == 1 && map[1][1].getValue() == 1 && map[2][2].getValue() == 1) {
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    gameOver = true;

                }else if (map[0][2].getValue() == 1 && map[1][1].getValue() == 1 && map[2][0].getValue() == 1) {
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    gameOver = true;

                }else if ((map[i][0].getValue() == 2 && map[i][1].getValue() == 2 && map[i][2].getValue() == 2)) {
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    gameOver = true;

                }else if ((map[0][j].getValue() == 2 && map[1][j].getValue() == 2 && map[2][j].getValue() == 2)) {
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    gameOver = true;

                }else if (map[0][0].getValue() == 2 && map[1][1].getValue() == 2 && map[2][2].getValue() == 2) {
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    gameOver = true;

                }else if (map[0][2].getValue() == 2 && map[1][1].getValue() == 2 && map[2][0].getValue() == 2) {
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    gameOver = true;
                }else if(isEveryFieldFilled()){
                    sendToAll("WIN0");
                    sendToAll("TEXTKein Spieler hat gewonnen!");
                    gameOver = true;
                }
            }
        }
    }


    /**
     * Prüft die Belegung des Feldes.
     *
     * @return Gibt zurück, ob jedes Feld bereits belegt ist.
     */
    private boolean isEveryFieldFilled(){
        boolean bool = true;
        int i=0;
        while(i<3){
            int j=0;
            while(j<3) {
                if (map[i][j].isEmpty()) {
                    bool = false;
                    return bool;
                }
                j++;
            }
            i++;
        }
        return bool;
    }

    /**
     * Aktion des Servers, wenn ein Client die Sitzung verlässt.
     *
     * @param pClientIP IP des verlassenden Clients.
     * @param pClientPort Port des verlassenden Clients.
     */
    @Override
    public void processClosingConnection(String pClientIP, int pClientPort) {
        switch(numberOfClients){
            case 1:
                numberOfClients--;
                System.out.println("close");
                close();
                break;
            case 2:
                numberOfClients--;
                createMap();
                gameOver = false;
                restartVote = 0;
                for (int i = 0; i < 15; i++) {
                    sendToAll("TEXT   ");
                }
                sendToAll("TEXTWarte auf Spieler");
                sendToAll("SPIELER1");
                sendToAll("UPDATE"+getEmptyMapInformation());
        }
    }

    /**
     * Zufällige Vergabe des ersten Spielzuges.
     */
    private void firstTurn(){
        if(numberOfClients == 2){
            int temp = (int)(Math.random()*2+1);
            if(temp == 1){
                player1turn = true;
            }else{
                player1turn = false;
            }
        }
    }

    /**
     * Erstellung eines leeren Spielfeldes.
     */
    private void createMap(){
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                map[i][j]= new Field();
            }
        }
    }


    /**
     * Gibt das Spielfeld in Form eines Strings zurück.
     * Ein Feld wird mit dem Stichwort 'NEXT' unterschieden.
     * Eine Feldinformation wird mit dem Stichwort 'FIELD' unterschieden.
     *
     * @return Aktuelles Spielfeld im String verkettet.
     */
    private String getMapInformation(){
        String mapInfo = "";
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                mapInfo = mapInfo+i+"FIELD"+j+"FIELD"+map[i][j].getValue();
                if(i!=map.length-1 || j!=map[i].length-1){
                    mapInfo = mapInfo+"NEXT";
                }
            }
        }
        return mapInfo;
    }

    /**
     * Macht das selbe wie die Methode dadrüber, bis auf, dass sie eine komplett leere Map zurückgibt.
     *
     * @return Aktuelles Spielfeld im String verkettet.
     */
    private String getEmptyMapInformation(){
        String mapInfo = "";
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                mapInfo = mapInfo+i+"FIELD"+j+"FIELD"+0;
                if(i!=map.length-1 || j!=map[i].length-1){
                    mapInfo = mapInfo+"NEXT";
                }
            }
        }
        return mapInfo;
    }

    private void sendKeys(String ip,int port){
        send(ip,port,"KEY"+publicKey.getKey1()+"#"+publicKey.getKey2()+"#"+phi);
    }
}
