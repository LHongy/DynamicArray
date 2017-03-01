public class Block<T> {
    protected final int number; // Block number, as in Block1
    protected final T[] arrayOfElements; // Holds actual elements
    
    // Note that it is not possible to use the code
    // T a[] = new T[size]; 
    // which leads to Java¡¯s Generic Array Creation error on 
    // compilation. Consult the textbook for solution to surmount this 
    // minor problem
    
    // Number of elements that can be stored in this block;
    // this is equal to arrayOfElements.length
    protected final int capacity;
    
    // Number of spaces that have been allocated for storing elements;
    // initially 0. size <= capacity
    protected int size;
    
    @SuppressWarnings("unchecked")
    // Workhorse constructor. Initialize variables and create array.
    public Block(int number, int capacity) {
        this.number = number;
        this.capacity = capacity;
        this.arrayOfElements = (T[]) new Object[capacity];
    }
    
    // Returns Number
    public int getNumber() {
        return number;
    }
    
    // Returns capacity
    public int getCapacity() {
        return capacity;
    }
    
    // Returns size
    public int size() {
        return size;
    }
    
    // Increase the space allocated for storing elements. Increases 
    // size.
    public void grow() {
        size++;
    }
    
    // Set the last element to null and decrease the space allocated 
    // for storing elements. Decreases size.
    public void shrink() {
        arrayOfElements[size - 1] = null;
        size--;
    }
    
    // Returns the element at position index in arrayOfElements.
    public T getElement(int index) {
        return arrayOfElements[index];
    }
    
    // Sets the value at position i in arrayOfElements to x.
    public void setElement(int i, T x) {
        arrayOfElements[i] = x;
    }
    
    // Create a pretty representation of the Block.
    // Example: 
    // A 
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < size; i++) {
            builder.append(arrayOfElements[i]);
        }
        return builder.toString();
    }
    
    // Create a pretty representation of the Block for debugging.
    // Example: 
    // A
    // - capacity=1 size=1  
    protected String toStringForDebugging() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < size; i++) {
            builder.append(arrayOfElements[i]);
        }
        builder.append("\n- capacity=" + capacity + " size=" + size);
        return builder.toString();
    }
    
}
