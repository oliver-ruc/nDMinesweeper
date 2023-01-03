import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Tensor<T> {
    private ArrayList<T> members;
    private final int[] dimensions;
    private final int numDimensions;
    private final int dimensionTotal;

    public Tensor(int ... dimensions) {
        this((T[]) null, dimensions);
    }

    public Tensor(Supplier<T> initializer, int ... dimensions) {
        this(dimensions);
        for (int i = 0; i < dimensionTotal; i++) {
            members.set(i, initializer.get());
        }
    }

    public Tensor(T[] initialTs, int ... dimensions) {
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

        if (initialTs == null) {
            for (int i = 0; i < dimensionTotal; i++) {
                members.add(null);
            }
        } else {
            if (initialTs.length != dimensionTotal) {
                throw new IllegalArgumentException("Wrong size of initialization array");
            }
            for (T element : initialTs) {
                members.add(element);
            }
        }
    }

    public T get(int ... getDimensions) {
        if (!isInBounds(getDimensions)) {
            throw new IllegalArgumentException(
                "Indices " + nDMinesweeper.printInts(getDimensions) + " is out of bounds for tensor with dimensions " + nDMinesweeper.printInts(dimensions)
            );
        }
        int index = getMemberIndex(getDimensions);
        return members.get(index);
    }

    public void set(T value, int ... setDimensions) {
        if (!isInBounds(setDimensions)) {
            throw new IllegalArgumentException(
                "Indices " + nDMinesweeper.printInts(setDimensions) + " is out of bounds for tensor with dimensions " + nDMinesweeper.printInts(dimensions)
            );
        }
        int index = getMemberIndex(setDimensions);
        members.set(index, value);
    }

    private int getMemberIndex(int[] getVector) {
        if (getVector.length != numDimensions) {
            throw new IllegalArgumentException("Number of dimensions does not equal dimensions of tensor");
        }
        int index = 0;
        int multiplier = 1;
        for (int i = numDimensions - 1; i >= 0; i--) {
            index += getVector[i] * multiplier;
            multiplier *= dimensions[i];
        }
        return index;
    }

    public int[] getDimensionIndices(int index) {
        if (numDimensions == 0) {
            return new int[] {};
        }
        
        if (index >= dimensionTotal) {
            throw new IllegalArgumentException("Index is outside bounds of tensor");
        }

        int[] indices = new int[numDimensions];
        for (int i = numDimensions - 1; i >= 0; i--) {
            indices[i] = index % dimensions[i];
            index /= dimensions[i];
        }
        return indices;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public int getDimensionTotal() {
        return dimensionTotal;
    }

    public boolean isInBounds(int[] indices) {
        boolean inBounds = true;
        for (int i = 0; i < numDimensions; i++) {
            inBounds &= indices[i] >= 0 && indices[i] < dimensions[i];
        }
        return inBounds;
    }

    public void forEach(Consumer<T> consumer) {
        this.members.forEach(consumer);
    }

    public <R> Tensor<R> forEach(Function<T, R> function) {
        Tensor<R> result = new Tensor<>(dimensions);
        for (int i = 0; i < result.getDimensionTotal(); i++) {
            int[] indices = result.getDimensionIndices(i);
            result.set(function.apply(this.get(indices)), indices);
        }
        return result;
    }

    /*
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
