package model;

import model.abitur.netz.Client;
import model.abitur.netz.Server;

public class TicTacToeServer extends Server {

    int numberOfClients;

    boolean player1turn;

    Field[][] map;



    public TicTacToeServer(int port){
        super(port);
        System.out.println("running Server");
        numberOfClients =0;
        map = new Field[3][3];
        createMap();


        System.out.println(getMapInformation());
    }


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
    }

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

    private void createMap(){
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                map[i][j]= new Field();
            }
        }
    }


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
