package model;

import model.abitur.netz.Client;
import model.abitur.netz.Server;

/**
 * Klasse des Servers zum TicTacToe spielen.
 */
public class TicTacToeServer extends Server {

    /** Anzahl der Clients auf dem Server*/
    int numberOfClients;

    /** Boolische Werte, die angeben, ob ein Spieler gewonnen hat oder Spieler 1 am Zug ist.*/
    boolean player1turn,playerWon;

    /** Das Spielfeld, das in einem 2-Dimensionalen Array gespeichert wird*/
    Field[][] map;

    /**
     * Konstruktor der Klasse TicTacToeServer.
     * Erstellt einen Server auf dem Clients TicTacToe spielen können.
     *
     * @param port Port, den der Server belegt.
     */
    public TicTacToeServer(int port){
        super(port);
        System.out.println("running Server");
        numberOfClients =0;
        playerWon = false;
        map = new Field[3][3];
        createMap();


        System.out.println(getMapInformation());
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
                numberOfClients++;
                break;
            case 1:
                numberOfClients++;
                send(pClientIP,pClientPort,"SPIELER2");
                firstTurn();
                sendToAll("TEXTSpiel startet");
                if(player1turn == true){
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
        }
        sendToAll("UPDATE"+getMapInformation());
        for(int i=0;i<map.length && !playerWon;i++){
            for(int j=0;j<map[i].length && !playerWon;j++){
                if((map[i][0].getValue()==1 && map[i][1].getValue()==1 && map[i][2].getValue()==1)){
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    playerWon = true;
                }
                if((map[0][j].getValue()==1 && map[1][j].getValue()==1 && map[2][j].getValue()==1)){
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    playerWon = true;

                }
                if(map[0][0].getValue()==1 && map[1][1].getValue() == 1 && map[2][2].getValue() == 1){
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    playerWon = true;

                }
                if(map[0][2].getValue() == 1 && map[1][1].getValue() == 1 && map[2][0].getValue() == 1){
                    sendToAll("WIN1");
                    sendToAll("TEXTSpieler 1 hat gewonnen!");
                    playerWon = true;

                }
                if((map[i][0].getValue()==2 && map[i][1].getValue()==2 && map[i][2].getValue()==2)){
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    playerWon = true;

                }
                if((map[0][j].getValue()==2 && map[1][j].getValue()==2 && map[2][j].getValue()==2)){
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    playerWon = true;

                }
                if(map[0][0].getValue()==2 && map[1][1].getValue() == 2 && map[2][2].getValue() == 2){
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    playerWon = true;

                }
                if(map[0][2].getValue() == 2 && map[1][1].getValue() == 2 && map[2][0].getValue() == 2){
                    sendToAll("WIN2");
                    sendToAll("TEXTSpieler 2 hat gewonnen!");
                    playerWon = true;

                }
            }
        }
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
                sendToAll("TEXTWarte auf Spieler");
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
}
