/*
Solution for Skynet revolution episode 1 & 2
https://www.codingame.com/ide/puzzle/skynet-revolution-episode-1
https://www.codingame.com/ide/puzzle/skynet-revolution-episode-2

Improvement idea:
Rework risklevel calculation. This is case if node has 1 connection to gateway, then getHighestRisk()
will return highest risk or first node in HashMap with connection to gateway.
--> removeRandomLink() will then not be needed and 1 looping through nodes will be saved.
 */

import java.util.*;

class Player {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();
        int L = in.nextInt();
        int E = in.nextInt();

//      Nodes and connections data
        GameData gd = new GameData(N);

//        Recording all connections between nodes from the website
        for (int i = 0; i < L; i++) {
            gd.setConnections(in.nextInt(), in.nextInt());
        }

//        Recording all gateways from the website
        for (int i = 0; i < E; i++) {
            gd.setIsGateway(in.nextInt());
        }

//        Calculating risk level
        gd.calculateRisk();

//        Creating game object for solving procedure and fowrding refrence to GameData
        Game game = new Game();
        game.setGd(gd);

//        Game loop. while loop terminated by website
        while (true) {
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            System.out.println(game.nextAnswer(SI));
        }
    }

    public static class GameData {
//        Count of nodes, including gateways
        Integer nodeCount = 0;

        private Map<Integer, Map<String, ArrayList<Integer>>> nodeData = new HashMap<>();

        public GameData(int inputNodeCount) {
            nodeCount = inputNodeCount;
            for (int i = 0; i < inputNodeCount; i++) {
                this.nodeData.put(i, new HashMap<String, ArrayList<Integer>>());
                this.nodeData.get(i).put("isGateway", new ArrayList<Integer>(Arrays.asList(0)));
                this.nodeData.get(i).put("riskLevel", new ArrayList<Integer>(Arrays.asList(0)));
                this.nodeData.get(i).put("connections", new ArrayList<Integer>());
            }
        }

        /*   Gateways status is held in ArrayList position 0
             isGateway: 0 - not gateway  , 1 - is gateway
         */
        public void setIsGateway(Integer currentNode) {
            nodeData.get(currentNode).get("isGateway").add(0, 1);
        }

//        Returns if the node is gateway
        public Integer getIsGateway(Integer currentNode) {
            return nodeData.get(currentNode).get("isGateway").get(0);
        }

//        Set riskLevel for node. Held in arraylist under positon 0
        public void setRiskLevel(Integer currentNode, Integer riskValue) {
            nodeData.get(currentNode).get("riskLevel").add(0, riskValue);
        }

//        Returns node riskLevel
        public Integer getRiskLevel(Integer currentNode) {
            return nodeData.get(currentNode).get("riskLevel").get(0);
        }
/*
          Sets connnection between nodes. Connection has two point (N1 and N2)
          So it is set, that N1 is connected to N2 and N2 is set to N1.
          this means ,that we can check the connection from both ends of link.
 */
        public void setConnections(Integer currentNode, Integer connectionNode) {
            nodeData.get(currentNode).get("connections").add(connectionNode);
            nodeData.get(connectionNode).get("connections").add(currentNode);
        }

//        Returns ArrayList object with all connections.
        public ArrayList<Integer> getConnections(Integer currentNode) {
            return nodeData.get(currentNode).get("connections");
        }

/*        Removes connection from both nodes. To remove connection N1 to N2 is removed
          from node N1 from connections N2 and from N2 node connections list node N1.
          For both nodes N1 and N2 risk level is set to 0.
*/
        public void removeConnection(Integer currentNode, Integer removeNode) {
            setRiskLevel(currentNode, 0);
            setRiskLevel(removeNode, 0);
            nodeData.get(currentNode).get("connections").remove(removeNode);
            nodeData.get(removeNode).get("connections").remove(currentNode);
        }

        public Integer getHighestRisk() {
            Integer currentRiskLevel = 0;
            Integer highestRiskNode = 0;

            for (Integer i = 0; i < nodeCount; i++) {
                if (getRiskLevel(i) > currentRiskLevel) {
                    currentRiskLevel = getRiskLevel(i);
                    highestRiskNode = i;
                }
            }

            return highestRiskNode;
        }

        public void calculateRisk() {
            for (Integer i = 0; i < nodeCount; i++) {
                Integer riskValue = 0;

                if (getIsGateway(i) == 0) {
                    riskValue = countGateways(i);

                    if (riskValue > 1) {
                        for (Integer node : getConnections(i)) {
                            riskValue += countConnectedGateways(node);
                        }
                        setRiskLevel(i, riskValue);
                    }
                }
            }
        }

        private Integer countConnectedGateways(Integer node) {
            Integer gatewaysConnected = 0;
            ArrayList<Integer> nodesToCheck = getConnections(node);

            for (Integer nodeToCheck : nodesToCheck) {
                if (getIsGateway(nodeToCheck) == 1) {
                    gatewaysConnected++;
                }
            }

            return gatewaysConnected;
        }

        private Integer countGateways(Integer node) {
            Integer riskValue = 0;

            ArrayList<Integer> connectedNodes = getConnections(node);

            for (Integer connectedNode : connectedNodes) {
                if (getIsGateway(connectedNode) == 1) {
                    riskValue++;
                }
            }

            return riskValue;
        }
    }

    private static class Game {

        private GameData gd;

        public void setGd(GameData gd) {
            this.gd = gd;
        }

        public String nextAnswer(Integer virusLocation) {
            String output = "";

            if (output.isEmpty()) {
                output = virusOneStepAway(virusLocation);
            }

            if (output.isEmpty()) {
                output = virusTwoStepAwayHighRisk(virusLocation);
            }


            if (output.isEmpty()) {
                System.err.println("removeHighestRisk()");
                output = removeHighestRisk();
            }

            if (output.isEmpty()) {
                System.err.println("removeRandomLink()");
                output = removeRandomLink();
            }

            return output;
        }

        private String virusOneStepAway(Integer virusLocation) {
            String output = "";
            ArrayList<Integer> firstLevelNodes = gd.getConnections(virusLocation);

            for (Integer node : firstLevelNodes) {
                if (gd.getIsGateway(node) == 1) {
                    output = "" + virusLocation + " " + node;
                    gd.removeConnection(virusLocation, node);
                    break;
                }
            }

            return output;
        }

        private String virusTwoStepAwayHighRisk(Integer virusLocation) {
//            firstLevelNodes == nodes 1 step away from virus location
//            secondLevelNodes == nodes 2 step away from virus location
            String output = "";
            Integer currentRiskLevel = 0;
            Integer currentHighestRiskNode = -1;
            Integer currentHighestRiskNode2 = -1;
            ArrayList<Integer> firstLevelNodes = gd.getConnections(virusLocation);

            for (Integer firstLevelNode : firstLevelNodes) {
                Integer checkingRiskLevel = gd.getRiskLevel(firstLevelNode);

                if (checkingRiskLevel > currentRiskLevel) {
                    currentRiskLevel = checkingRiskLevel;
                    currentHighestRiskNode = firstLevelNode;
                }
            }

            if (currentRiskLevel > 0) {
                for (Integer node : gd.getConnections(currentHighestRiskNode)) {
                    if (gd.getIsGateway(node) == 1) {
                        currentHighestRiskNode2 = node;
                        break;
                    }
                }
                output = currentHighestRiskNode + " " + currentHighestRiskNode2;
                gd.removeConnection(currentHighestRiskNode, currentHighestRiskNode2);
            }

            return output;
        }

        private String removeHighestRisk() {
            String output = "";
            Integer highestRiskNode = -1;
            highestRiskNode = gd.getHighestRisk();

            if (highestRiskNode > -1) {
                ArrayList<Integer> firstLevelNodes = gd.getConnections(highestRiskNode);

                for (Integer node : firstLevelNodes) {
                    if (gd.getIsGateway(node) == 1) {
                        output = "" + node + " " + highestRiskNode;
                        gd.removeConnection(node, highestRiskNode);
                        break;
                    }
                }
            }

            return output;
        }

        private String removeRandomLink() {
            String output = "";
            Integer currentGateway = -1;

            for (Integer i = 0; i < gd.nodeCount; i++) {
                if (gd.getIsGateway(i) == 1 && !gd.getConnections(i).isEmpty()) {
                    currentGateway = i;
                    break;
                }
            }

            ArrayList<Integer> currentConnections = gd.getConnections(currentGateway);
            output = "" + currentConnections.get(0) + " " + currentGateway;
            gd.removeConnection(currentConnections.get(0), currentGateway);

            return output;
        }
    }
}