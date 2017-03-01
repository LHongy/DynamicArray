import java.util.NoSuchElementException;

public class DynamicQueue<T> {
    protected DynamicArray<T> out;  // These fields may be renamed
    protected DynamicArray<T> in;   // The methods getFront() and getRear() return them
    
    // Return the ¡°front¡± dynamic array of outgoing elements for final testing
    // Target complexity: O(1)
    protected DynamicArray<T> getFront() {
        return out;
    }
    
    // Return the ¡°rear¡± dynamic array of incoming elements for final testing
    // Target complexity: O(1)
    protected DynamicArray<T> getRear() {
        return in;
    }
    
    // Workhorse constructor. Initialize variables.
    public DynamicQueue() {
        in = new DynamicArray<T>();
        out = new DynamicArray<T>();
    }
    
    // Adds x to the rear of the queue
    // Target complexity: O(1)
    public void enqueue(T x) {
        // Add element to DynamicArray in.
        in.add(x);
    }
    
    // Removes and returns the element at the front of the queue
    // Throws NoSuchElementException if this queue is empty.
    // Target complexity: O(n)
    public T dequeue() {
        if(isEmpty()) {
            throw new NoSuchElementException();
        }
        if(out.size() == 0) {
            // Copy elements from DynamicArray in to DynamicArray out, in reverse order.
            // Ex: If we have ABC in DynamicArray in, 
            // we will have CBA in DynamicArray out after copy all elements.
            // Also remove the element in DynamicArray in after it's copied to DynamicArray out.
            for(int i = in.size() - 1; i >= 0; i--) {
                out.add(in.get(i));
                in.remove();
            }
        }
        // The last element in DynamicArray out is the first element added to DynamicArray in,
        // and that is the element we want to return and remove because of first in first out.
        T elementAtFront = out.get(out.size() - 1);
        out.remove();
        return elementAtFront;
    }
    
    // Returns true if the queue is empty
    public boolean isEmpty() {
        return size() == 0;
    }
    
    // Returns the size of the queue
    public int size() {
        return out.size() + in.size();
    }
    
    // Create a pretty representation of the DynamicQueue.
    // Example:
    // [A, B, C, D]
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(int i = 0; i < out.size(); i++) {
            builder.append(out.get(i) + ", ");
        }
        for(int i = 0; i < in.size() - 1; i++) {
            builder.append(in.get(i) + ", ");
        }
        builder.append(in.get(in.size() - 1) + "]");
        return builder.toString();
    }
    
    // Create a pretty representation of the DynamicQueue for debugging.
    // Example:
    // front.toString: [A, B] 
    // rear.toString: [C, D]
    protected String toStringForDebugging() {
        return "front.toString: " + out.toString() + "\nrear.toString: " + in.toString();
    }
}
