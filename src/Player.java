/*
Solution for Skynet revolution episode 1 & 2
https://www.codingame.com/ide/puzzle/skynet-revolution-episode-1
https://www.codingame.com/ide/puzzle/skynet-revolution-episode-2
 */

import java.util.*;

class Player {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int nodeCount = in.nextInt();
        int linksCount = in.nextInt();
        int gatewayCount = in.nextInt();

        GameData gd = new GameData(nodeCount);
        Game game = new Game();
        game.setGd(gd);

        for (int i = 0; i < linksCount; i++) {
            gd.setConnections(in.nextInt(), in.nextInt());
        }

        for (int i = 0; i < gatewayCount; i++) {
            gd.setIsGateway(in.nextInt());
        }

        gd.calculateRiskLevel();

//        Game loop. while without end condition. End condition controlled by website
        while (true) {
            int currentVirusLocation = in.nextInt();
            System.out.println(game.nextAnswer(currentVirusLocation));
        }
    }

    public static class GameData {
        Integer nodeCount = 0;

        private Map<Integer, Map<String, ArrayList<Integer>>> nodeData = new HashMap<>();

        public GameData(int inputNodeCount) {
            nodeCount = inputNodeCount;
            for (int i = 0; i < inputNodeCount; i++) {
                this.nodeData.put(i, new HashMap<>());
                this.nodeData.get(i).put("isGateway", new ArrayList<>(Collections.singletonList(0)));
                this.nodeData.get(i).put("riskLevel", new ArrayList<>(Collections.singletonList(0)));
                this.nodeData.get(i).put("connections", new ArrayList<>());
            }
        }

        // isGateway: 0 - not gateway  , 1 - is gateway
        public void setIsGateway(Integer currentNode) {
            nodeData.get(currentNode).get("isGateway").add(0, 1);
        }

        public Integer getIsGateway(Integer currentNode) {
            return nodeData.get(currentNode).get("isGateway").get(0);
        }

        public void setRiskLevel(Integer currentNode, Integer riskValue) {
            nodeData.get(currentNode).get("riskLevel").add(0, riskValue);
        }

        public Integer getRiskLevel(Integer currentNode) {
            return nodeData.get(currentNode).get("riskLevel").get(0);
        }

        // Sets the connnection between nodes to start node and end node
        public void setConnections(Integer currentNode, Integer connectionNode) {
            nodeData.get(currentNode).get("connections").add(connectionNode);
            nodeData.get(connectionNode).get("connections").add(currentNode);
        }

        public ArrayList<Integer> getConnections(Integer currentNode) {
            return nodeData.get(currentNode).get("connections");
        }

        // Removes connection from start and end node. Sets risklevel ror both to 0.
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

        public void calculateRiskLevel() {
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
                output = removeHighestRisk();
            }

            if (output.isEmpty()) {
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