public class Tile {
    public TileState tileState;
    public boolean isBomb;
    public int numNeighbors;

    public Tile() {
        this.tileState = TileState.COVERED;
        this.isBomb = false;
        this.numNeighbors = 0;
    }

    public String getRepresentation() {
        if (this.tileState == null) {
            throw new IllegalStateException("TileState is null");
        }
        switch (this.tileState) {
            case COVERED:
                return "X";
            case FLAGGED:
                return "F";
            case UNCOVERED:
                if (this.isBomb)
                    return "B";
                else
                    return Integer.toString(numNeighbors);
            default:
                return "Z"; // compliation problem fixer
        }
    }
}
