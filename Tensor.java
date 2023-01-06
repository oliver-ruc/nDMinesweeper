import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Tensor is an mutli-dimensional array that has a 
 * configuarable number of dimensions and length for
 * each of the dimensions of the Tensor. The Tensor 
 * can be indexed through an int array to get the
 * element of the Tensor at a certain set of indices.
 * <p>
 * A Tensor provides functionality over a regular
 * mutli-dimensional array in Java by allowing for
 * easier indexing (using int arrays) and methods 
 * to help iterate over all the elements in the 
 * Tensor or just specific elements.
 */
public class Tensor<T> {
    private ArrayList<T> members;
    private final int[] dimensions;
    /** total number of dimensions. Same as dimensions.length */
    private final int numDimensions;
    /** total number of elements. Multiplication of the elements of dimensions, 1 if empty */
    private final int dimensionTotal;

    /**
     * Creates a Tensor with the given dimensions, 
     * or a single element with no dimensions otherwise. 
     * Each element in the Tensor is initialized to null.
     * @param dimensions the dimensions of the tensor
     */
    public Tensor(int ... dimensions) {
        if (dimensions.length == 0) {
            this.dimensions = new int[] {};
            this.numDimensions = 0;
        } else {
            this.dimensions = dimensions;
            this.numDimensions = dimensions.length;
        }

        int dimensionMult = 1;
        for (int i = 0; i < this.dimensions.length; i++) {
            dimensionMult *= this.dimensions[i];
        }
        this.dimensionTotal = dimensionMult;

        this.members = new ArrayList<T>(dimensionTotal);

        for (int i = 0; i < dimensionTotal; i++) {
            members.add(null);
        }
    }

    /**
     * Creates a Tensor with the given dimensions, or a single element with no dimensions otherwise.
     * Each element in the Tensor is initialized to the result of initializer.get().
     * @param initializer a function to initialize the elements with. Tensor is traversed in row-major order
     * @param dimensions the dimensions of the tensor
     */
    public Tensor(Supplier<T> initializer, int ... dimensions) {
        this(dimensions);
        for (int i = 0; i < dimensionTotal; i++) {
            members.set(i, initializer.get());
        }
    }

    /**
     * Creates a Tensor with the given dimensions and the given intialization, or a single initialized element with no dimensions otherwise.
     * Each element in the Tensor is initialized to a value from the array using row-major order
     * @param initialTs an array to initialize the elements with. Tensor is traversed in row-major order
     * @param dimensions the dimensions of the tensor
     */
    public Tensor(T[] initialTs, int ... dimensions) {
        this(dimensions);
        if (initialTs.length != dimensionTotal) {
            throw new IllegalArgumentException("Wrong size of initialization array");
        }
        for (int i = 0; i < dimensionTotal; i++) {
            this.set(initialTs[i], i);
        }
    }

    /**
     * Returns a new Tensor will all elements initialized to the given value
     * @param <T> The type of the new Tensor and of the value
     * @param fillValue The value to initialize and fill the new Tensor with
     * @param dimensions The dimensions of the new Tensor
     * @return The new Tensor
     */
    public static <T> Tensor<T> fill(T fillValue, int ... dimensions) {
        return new Tensor<T>(() -> {return fillValue;}, dimensions);
    }

    /**
     * Gets the element in the Tensor specified by the indices
     * @param getIndices the indices of the requested element
     * @return the requested element
     * @throws IllegalArgumentException if the indices are out of bounds of the tensor, or if the number of indices are not equal to the Tensor's dimensions
     */
    public T get(int ... getIndices) {
        int index = getMemberIndex(getIndices);
        return members.get(index);
    }

    /**
     * Sets the element in the Tensor specified by the indices to the specified value
     * @param value the value to set the element to
     * @param setDimensions the indices of the element
     * @throws IllegalArgumentException if the indices are out of bounds of the tensor, or if the number of indices are not equal to the Tensor's dimensions
     */
    public void set(T value, int ... setDimensions) {
        int index = getMemberIndex(setDimensions);
        members.set(index, value);
    }

    /**
     * Gets the index in the underlying data structure from the given indices
     * @param indices the indices to get the index for
     * @return the index in the underlying data structure
     * @throws IllegalArgumentException if the indices are out of bounds of the tensor, or if the number of indices are not equal to the Tensor's dimensions
     */
    private int getMemberIndex(int ... indices) {
        errorIfInvalidIndices(indices);
        int index = 0;
        int multiplier = 1;
        for (int i = numDimensions - 1; i >= 0; i--) {
            index += indices[i] * multiplier;
            multiplier *= dimensions[i];
        }
        return index;
    }

    /**
     * Gets the indices given the index, using row-major ordering
     * @param index the one-dimensional index to the get the (possibly multi-dimensional) indices for
     * @return the indices of the supplied index in the Tensor
     * @throws IllegalArgumentException if the index is out of bounds for the Tensor
     */
    public int[] getDimensionIndices(int index) {
        if (index >= dimensionTotal) {
            throw new IllegalArgumentException("Index is out of bounds of the tensor");
        }

        int[] indices = new int[numDimensions];
        for (int i = numDimensions - 1; i >= 0; i--) {
            indices[i] = index % dimensions[i];
            index /= dimensions[i];
        }
        return indices;
    }

    /**
     * Gets a clone of the dimensions of the Tensor
     * @return a clone of the dimensions of the Tensor
     */
    public int[] getDimensions() {
        return dimensions.clone();
    }

    /**
     * Gets the total number of elements in (multiplication of all the dimensions of) the Tensor.
     * In the case of a zero-dimensional Tensor, returns one.
     * @return the total number of elements
     */
    public int getDimensionTotal() {
        return dimensionTotal;
    }

    /**
     * Calls the given consumer all the elements of the Tensor
     * @param consumer the function to apply to all the elements of the array
     */
    public void forEach(Consumer<T> consumer) {
        this.members.forEach(consumer);
    }

    /**
     * Returns a new Tensor where each element is the result of the given function applied to the corresponding element of the current Tensor
     * @param <R> The type of the new Tensor and the result of the function
     * @param function The function to apply to each element of the current Tensor to get the corresponding element of the new Tensor
     * @return The new Tensor
     */
    public <R> Tensor<R> forEach(Function<T, R> function) {
        Tensor<R> result = new Tensor<>(dimensions);
        for (int i = 0; i < result.getDimensionTotal(); i++) {
            int[] indices = result.getDimensionIndices(i);
            result.set(function.apply(this.get(indices)), indices);
        }
        return result;
    }

    /**
     * Returns the first indices of the given object. 
     * Specifically, returns the first element (when traversed in row-major order) where obj.equal(element) is true
     * @param obj the object to find in the Tnesor
     * @return the given indices of the object, or null if none is found
     */
    public int[] firstIndicesOf(T obj) {
        for (int i = 0; i < this.getDimensionTotal(); i++) {
            int[] indices = this.getDimensionIndices(i);
            T cmp = this.get(indices);
            if (obj.equals(cmp)) {
                return indices;
            }
        }
        return null;
    }

    /**
     * Checks whether the given indices are within the bounds of the Tensor. 
     * Does not check length of the given indices, but will throw an error if they are wrong
     * @param indices the indices to check
     * @return a boolean that is true if and only if the indices are within the bounds of the tensor
     */
    public boolean inBounds(int ... indices) {
        if (indices.length != numDimensions) {
            return false;
        }
        boolean inBounds = true;
        for (int i = 0; i < numDimensions; i++) {
            inBounds &= indices[i] >= 0 && indices[i] < dimensions[i];
        }
        return inBounds;
    }

    /**
     * Throws an error if the given indices are not within the bounds of the Tensor
     * @param indices the indices to check
     * @throws IllegalArgumentException if the number of indices do not match the number of dimensions of the Tensor or if the indices are out of bounds for the Tensor
     */
    private void errorIfInvalidIndices(int ... indices) {
        if (indices.length != numDimensions) {
            throw new IllegalArgumentException("Number of dimensions does not equal dimensions of tensor");
        }
        if (!inBounds(indices)) {
            throw new IllegalArgumentException(
                "Indices " + nDMinesweeper.printInts(indices) + " is out of bounds for tensor with dimensions " + nDMinesweeper.printInts(dimensions)
            );
        }
    }

    /* TODO: Finish
    @Override
    public String toString() {
        if (dimensionTotal == 1) {
            return "[" + members.get(0).toString() + "]";
        }

        String result = "";

        int lastDimension = dimensions[numDimensions - 1];
        for (int i = 0; i < dimensionTotal / lastDimension; i++) {
            result += "[";
            for (int j = 0; j < lastDimension; j++) {
                result += members.get(i * lastDimension + j).toString();
                if (j != lastDimension - 1) {
                    result += ", s"
                }
            }
        }
    }
    */
}
