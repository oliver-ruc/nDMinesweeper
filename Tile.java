public class Tile {
    /** the current state of the Tile */
    public TileState tileState;

    /** whether the given tile is a bomb */
    public boolean isBomb;

    /** number of bomb neighbors */
    public int numNeighbors;

    /**
     * Creates a new Tile with default state (covered, not a bomb, and zero bomb neighbors)
     */
    public Tile() {
        this.tileState = TileState.COVERED;
        this.isBomb = false;
        this.numNeighbors = 0;
    }

    /**
     * Gets the string representation of the Tile.
     * <ul>
     * <li> if covered, "X"
     * <li> if flagged, "F"
     * <li> if uncovered,
     *  <ul>
     *  <li> if bomb, "B"
     *  <li> if not bomb, the number of bomb neighbors
     *  </ul>
     * </ul>
     * @return
     */
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
