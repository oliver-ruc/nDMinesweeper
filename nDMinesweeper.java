import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class nDMinesweeper {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int[] dimensions;
        while (true) {
            System.out.println("Please put board dimensions (separated by spaces)");
            String[] inputDimensions = sc.nextLine().split(" ");
            dimensions = new int[inputDimensions.length];
            try {
                for (int i = 0; i < inputDimensions.length; i++) {
                    dimensions[i] = Integer.parseInt(inputDimensions[i]);
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Bad dimensions input");
            }
        }
        
        int total = 1;
        for (int e : dimensions) {
            total *= e;
        }

        int bombs;
        while (true) {
            System.out.println("Please put number of bombs");
            String inputBombs = sc.nextLine();
            try {
                bombs = Integer.parseInt(inputBombs);
            } catch (NumberFormatException e) {
                System.out.println("Bad input (number of bombs)");
                continue;
            }
            if (0 <= bombs && bombs <= total) {
                break;
            }
            System.out.println("Bad number of bombs");
        }

        Tensor<Tile> board = new Tensor<>(
            Tile::new,
            dimensions
        );
        System.out.println("Total Tiles: " + total);

        ArrayList<Integer> shuffledTileIndices = new ArrayList<>();
        IntStream.range(0, total).forEach(e -> shuffledTileIndices.add(e));
        Collections.shuffle(shuffledTileIndices);

        for (int i = 0; i < bombs; i++) {
            int[] indicies = board.getDimensionIndices(shuffledTileIndices.get(i));
            Tile tile = board.get(indicies);
            tile.isBomb = true;

            Set<int[]> neighbors = getNeighbors(
                indicies, 
                dimensions
            );

            for (int[] neighbor : neighbors) {
                Tile tileNeighbor = board.get(neighbor);
                tileNeighbor.numNeighbors += 1;
            }
        }

        printBoard(board);

        while(true) {
            System.out.println("Please Enter Selection Square"); 
            String input = sc.nextLine();
            String[] inputs = input.split(" ");
            boolean flagCommand = false;
            int[] indices = new int[dimensions.length];
            if (inputs.length == dimensions.length + 1) {
                System.out.println(inputs[0]);
                if (!inputs[0].equals("F")) {
                    System.out.println("Bad input (long but not flag)");
                    continue;
                }
                flagCommand = true;
                String[] newInputs = new String[dimensions.length];
                for (int i = 0; i < dimensions.length; i++) {
                    newInputs[i] = inputs[i+1];
                }
                inputs = newInputs;
            }
            if (inputs.length != dimensions.length) {
                System.out.println("Bad input (wrong amount of dimensions)");
                continue;
            }
            try {
                for (int i = 0; i < dimensions.length; i++) {
                    indices[i] = Integer.parseInt(inputs[i]);
                }
            } catch (NumberFormatException e) {
                System.out.println("Bad input (not integers)");
                continue;
            }
            if (!board.isInBounds(indices)) {
                System.out.println("Bad input (indicies out of bounds)");
                continue;
            }
            Tile chosen = board.get(indices);
            switch(chosen.tileState) {
                case COVERED:
                    if (flagCommand)
                        chosen.tileState = TileState.FLAGGED;
                    else
                        chosen.tileState = TileState.UNCOVERED;
                    break;
                case FLAGGED:
                    if (!flagCommand)
                        chosen.tileState = TileState.COVERED;
                    break;
                case UNCOVERED:
                    break;
            }
            if (chosen.tileState == TileState.UNCOVERED && chosen.isBomb) {
                System.out.println("BOOM! You lose!");
                break;
            }

            if (chosen.tileState == TileState.UNCOVERED && chosen.numNeighbors == 0) {
                Set<Tile> fillUncover = new HashSet<>();
                fillUncover.add(chosen);

                Set<Tile> checked = new HashSet<>();
                while (checked.size() < fillUncover.size()) {
                    Set<Tile> toAdd = new HashSet<>();
                    for(Tile tile : fillUncover) {
                        if (checked.contains(tile)) {
                            continue;
                        }
                        tile.tileState = TileState.UNCOVERED;
                        checked.add(tile);
                        if (tile.numNeighbors == 0) {
                            int[] tileIndices = board.firstIndicesOf(tile);
                            toAdd.addAll(
                                getNeighbors(tileIndices, dimensions).stream().map(
                                    (int[] ints) -> {return board.get(ints);}
                                ).collect(Collectors.toList())
                            );
                        }
                    }
                    fillUncover.addAll(toAdd);
                }
            }

            printBoard(board);
            
            boolean won = checkWin(board);
            if (won) {
                System.out.println("You won!");
                break;
            }
        }

        sc.close();
    }

    public static boolean areNeighbors(int[] first, int[] second) {
        if (first.equals(second)) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < first.length; i++) {
            result &= Math.abs(first[i] - second[i]) <= 1;
        }
        return result;
    }

    public static Set<int[]> getNeighbors(int[] indicies, int[] dimensions) {
        Set<int[]> neighbors = new HashSet<>();
        int maxNeighbors = 1;
        int self = 0;
        for (int i = 0; i < dimensions.length; i++) {
            maxNeighbors *= 3;
            self += 1;
            self *= 3;
        }
        self /= 3; //  1111...111 base 3 
            // max is 10000...000 base 3

        for (int i = 0; i < maxNeighbors; i++) {
            if (i == self) {
                continue;
            }
            int[] neighbor = new int[dimensions.length];
            int current = i;

            for (int j = dimensions.length - 1; j >= 0; j--) {
                neighbor[j] = current % 3 - 1;
                current /= 3;

                neighbor[j] += indicies[j];
            }
            if (isInBounds(neighbor, dimensions)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public static boolean isInBounds(int[] indices, int[] dimensions) {
        boolean inBounds = true;
        for (int i = 0; i < dimensions.length; i++) {
            inBounds &= indices[i] >= 0 && indices[i] < dimensions[i];
        }
        return inBounds;
    }
    
    public static String printInts(int[] ints) {
        String result = "";
        result += "[";
        for (int i = 0; i < ints.length; i++) {
            result += ints[i];
            if (i != ints.length - 1) {
                result += ", ";
            }
        }
        result += "]";
        return result;
    }

    public static int[] concatonateIntArrays(int[] first, int[] second) {
        int[] total = new int[first.length + second.length];
        for (int i = 0; i < first.length; i++) {
            total[i] = first[i];
        }
        for (int j = 0; j < second.length; j++) {
            total[first.length + j] = second[j];
        }
        return total;
    }

    public static void printBoard(Tensor<Tile> board) {
        int[] dimensions = board.getDimensions();
        Tensor<String[]> boards;
        int[] outerDimensions;
        int[] innerDimensions = new int[2];
        if (dimensions.length == 0) { // one tile
            System.out.println(board.get().getRepresentation());
            return;
        } else if (dimensions.length == 1) { // one line of tiles
            for (int i = 0; i < dimensions[0]; i++)
                System.out.print(board.get(i).getRepresentation());
            System.out.println();
            return;
        } else if (dimensions.length == 2) { // 2d board
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    System.out.print(board.get(i,j).getRepresentation());
                }
                System.out.println();
            }
            return;
        } 

        outerDimensions = new int[dimensions.length - 2];
        for (int i = 0; i < outerDimensions.length; i++) {
            outerDimensions[i] = dimensions[i];
        }

        innerDimensions = new int[] {
            dimensions[dimensions.length - 2],
            dimensions[dimensions.length - 1]
        };

        boards = new Tensor<String[]>(outerDimensions);
        
        for (int i = 0; i < boards.getDimensionTotal(); i++) {
            String[] singleBoard = new String[innerDimensions[0]];
            for (int j = 0; j < innerDimensions[0]; j++) {
                String line = "";
                for (int k = 0; k < innerDimensions[1]; k++) {
                    line += board.get(
                        concatonateIntArrays(
                            boards.getDimensionIndices(i),
                            new int[] {j, k}
                        )
                    ).getRepresentation();
                }
                singleBoard[j] = line;
            }
            boards.set(
                singleBoard,
                boards.getDimensionIndices(i)
            );
        }

        for (int i = 0; i < outerDimensions.length / 2; i++) {
            int[] newDimensions = new int[outerDimensions.length - 2*(i+1)];
            for (int j = 0; j < newDimensions.length; j++) {
                newDimensions[j] = outerDimensions[j];
            }

            Tensor<String[]> newBoards = new Tensor<String[]>(newDimensions);

            int[] twoD = new int[] {
                outerDimensions[outerDimensions.length - 2 - i*2],
                outerDimensions[outerDimensions.length - 1 - i*2]
            };
            for (int j = 0; j < newBoards.getDimensionTotal(); j++) {
                String[] twoDCollection = new String[0];
                for (int k = 0; k < twoD[0]; k++) {
                    String[] lineOfBoards = boards.get(
                        concatonateIntArrays(
                            newBoards.getDimensionIndices(j), 
                            new int[] {k, 0}
                        )
                    );
                    for (int l = 1; l < twoD[1]; l++) {
                        lineOfBoards = conjoinHorizontal(
                            lineOfBoards, 
                            boards.get(
                                concatonateIntArrays(
                                    newBoards.getDimensionIndices(j), 
                                    new int[] {k, l}
                                )
                            ), 
                            " "
                        );
                    }
                    if (k == 0) {
                        twoDCollection = lineOfBoards;
                    } else {
                        twoDCollection = conjoinVertical(twoDCollection, lineOfBoards, " ");
                    }
                }
                newBoards.set(
                    boxWrap(twoDCollection), 
                    newBoards.getDimensionIndices(j)
                );
            }
            boards = newBoards;
        }

        if (outerDimensions.length % 2 == 1) { // Extra dimension not counted in the for loop
            Tensor<String[]> newBoards = new Tensor<>();
            String[] lineCollection = boards.get(0);
            for (int i = 1; i < outerDimensions[0]; i++) {
                lineCollection = conjoinHorizontal(lineCollection, boards.get(i), " ");
            }
            newBoards.set(boxWrap(lineCollection));
            boards = newBoards;
        }

        String[] finished = boards.get();
        for (int i = 0; i <finished.length; i++) {
            System.out.println(finished[i]);
        }
    }

    public static String[] boxWrap(String[] contents) {
        int max = 0;
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].length() > max) {
                max = contents[i].length();
            }
        }

        String topAndBottom = "+";
        for (int i = 0; i < max; i++) {
            topAndBottom += "-";
        }
        topAndBottom += "+";

        String[] result = new String[contents.length + 2];
        result[0] = topAndBottom;
        result[result.length - 1] = topAndBottom;
        for (int i = 0; i < contents.length; i++) {
            result[i+1] = "|" + padRight(contents[i], max, " ") + "|";
        }
        return result;
    }

    public static String padRight(String unpadded, int paddedLength, String padding) {
        String result = unpadded;
        while (result.length() < paddedLength) {
            result += padding;
        }
        return result;
    }

    public static String[] conjoinHorizontal(String[] left, String[] right, String separator) {
        String[] combined = new String[left.length];
        for (int i = 0; i < left.length; i++) {
            combined[i] = left[i] + separator + right[i];
        }
        return combined;
    }

    public static String[] conjoinVertical(String[] top, String[] bottom, String lineSeperator) {
        if (lineSeperator == null) {
            String[] combined = new String[top.length + bottom.length];
            for (int i = 0; i < top.length; i++) {
                combined[i] = top[i];
            }
            for (int i = 0; i < bottom.length; i++) {
                combined[top.length + i] = bottom[i];
            }
            return combined;
        }
        
        
        String[] combined = new String[top.length + bottom.length + 1];
        for (int i = 0; i < top.length; i++) {
            combined[i] = top[i];
        }
        combined[top.length] = lineSeperator;
        for (int i = 0; i < bottom.length; i++) {
            combined[top.length + 1 + i] = bottom[i];
        }
        return combined;
    }

    public static boolean checkWin(Tensor<Tile> board) {
        boolean[] won = new boolean[] {true};
        board.forEach(
            (Tile t) -> {
                if (t.isBomb) {
                    won[0] &= t.tileState == TileState.COVERED || t.tileState == TileState.FLAGGED;
                } else {
                    won[0] &= t.tileState == TileState.UNCOVERED;
                }
            }
        );
        return won[0];
    }
}
