class DynamicArray<T> {
    protected Object[] arrayOfBlocks; 
    protected final int DEFAULTCAPACITY = 4;
    protected int sizeOfArrayOfBlocks; // number of Blocks in arrayOfBlocks
    protected int size; // number of elements in DynamicArray
    protected int numberOfEmptyDataBlocks;
    protected int numberOfNonEmptyDataBlocks;
    protected int numberOfDataBlocks;
    protected int indexOfLastNonEmptyDataBlock;
    protected int indexOfLastDataBlock;
    protected int numberOfSuperBlocks; 
    protected SuperBlock lastSuperBlock; // right-most SuperBlock
    
    // Workhorse constructor. Initialize variables, create the array
    // and the last SuperBlock, which represents SB0.
    DynamicArray( ) {
        arrayOfBlocks = new Object[DEFAULTCAPACITY];
        lastSuperBlock = new SuperBlock(0, 1, 1, 0); // SB0 only has one Block, and that Block can only have one element.
        arrayOfBlocks[0] = new Block<T>(0, 1); // The first Block, this Block is in SB0, so it can only have one element.
        lastSuperBlock.incrementCurrentNumberOfDataBlocks(); // Now SB0 contains a Block, incrementCurrentNumberOfDataBlocks.
        sizeOfArrayOfBlocks = 1;
        size = 0;
        numberOfEmptyDataBlocks = 1;
        numberOfNonEmptyDataBlocks = 0;
        numberOfDataBlocks = 1;
        indexOfLastNonEmptyDataBlock = -1;
        indexOfLastDataBlock = 0;
        numberOfSuperBlocks = 1;
    }
    
    // Returns the Location of element i, which is the index of the Block
    // and the position of i within that Block.
    protected Location locate(int index) { 
        int r = index + 1;
        // cast log2(r) to int give us the same value as floor(log2 r).
        int k = (int) log2(r);
        // Element i is located in Block p + b in position e.
        return new Location(computeP(k) + computeB(r, k), computeE(r, k));
    }
    
    // Helper method computing p.
    private int computeP(int k) {
        int p;
        if(k % 2 == 0) {
            // For even k
            // p = 2 * (2^floor(k/2) - 1)
            // k / 2 will give us floor(k / 2), 1 << k / 2 raise 2 to power floor(k / 2).
            p = (int) (2 * ((1 << k / 2) - 1));
        } else {   
            // For odd k
            // p = (2 * (2^floor(k/2) - 1)) + 2^floor(k/2)
            p = (int) (2 * ((1 << k / 2) - 1) + (1 << k / 2));
        }
        return p;
    }
    
    // Helper method computing b.
    private int computeB(int r, int k) {
        // The value of b is given by the base 10 value of the floor(k/2) bits of r immediately after the leading 1 bit in r.
        // k / 2 will give us floor(k / 2), this is the number of bits we want to capture.
        int numOfBitsToCapture = k / 2;
        // floor(log2 r) is the total number of bits immediately after the leading 1 bit in r,
        // and k = floor(log2 r)
        // k - numOfBitsToCapture gives us how many places we need to shift the bits to the right in r.
        r = r >> (k - numOfBitsToCapture);
        // mask depends on how many bits we want to capture.
        int mask = maskOfN(numOfBitsToCapture);  
        int b = r & mask;
        return b;
    }
    
    // Helper method computing e.
    private int computeE(int r, int k) {
        // The value of e is given by the base 10 value of the last ceiling(k/2) bits of r.
        // We want to capture the last ceil(k / 2) bits of r
        // Quickly compute ceil(k / 2)
        int numOfBitsToCapture = (int) (k + 1) >> 1;
        // mask depends on how many bits we want to capture.
        int mask = maskOfN(numOfBitsToCapture); 
        int e = r & mask;
        return e;
    }
    
    @SuppressWarnings("unchecked")
    // Returns the Block at position i in arrayOfBlocks.
    // Target complexity: O(1)
    protected Block<T> getBlock(int index) {
        return (Block<T>) arrayOfBlocks[index];
    }
    
    // Returns the element at position i in the DynamicArray.
    // Throws IllegalArgumentException if index < 0 or 
    // index > size -1;
    // Target complexity: O(1)
    public T get(int i) {
        if(i < 0 || i > size - 1) {
            throw new IllegalArgumentException();
        }
        // We need to find which Block contains the requested element, also in what position of that Block.
        // locate() gives us a Location object.
        // The object will contain the index of Block that we want,
        // and also the index of element in the Block.
        Location location = locate(i); 
        Block<T> block = getBlock(location.getBlockIndex()); // Use the blockIndex in the Location object to find the Block.
        return block.getElement(location.getElementIndex()); // Use the elementIndex in the Location object to find the element within the Block.
    }
    
    // Sets the value at position i in DynamicArray to x.
    // Throws IllegalArgumentException if index < 0 or 
    // index > size -1;
    // Target complexity: O(1)
    public void set(int index, T x) {
        if(index < 0 || index > size - 1) {
            throw new IllegalArgumentException();
        }
        Location location = locate(index);
        Block<T> block = getBlock(location.getBlockIndex());
        block.setElement(location.getElementIndex(), x); // Use the elementIndex in the Location object to set the element within the Block to x.
    }
    
    @SuppressWarnings("unchecked")
    // Allocates one more spaces in the DynamicArray. This may
    // require the creation of a Block and the last SuperBlock may change. 
    // Also, expandArray is called if the arrayOfBlocks is full when
    // a Block is created. 
    // Called by add.
    // Target complexity: O(1)
    protected void grow() {
        Block<T> lastDataBlock = (Block<T>) arrayOfBlocks[indexOfLastDataBlock]; 
        
        // If the last Block is full, we need to make a new Block.
        if(lastDataBlock.getCapacity() == lastDataBlock.size()) {
            if(sizeOfArrayOfBlocks == arrayOfBlocks.length) {
                // arrayOfBlocks is full, need to expand.
                expandArray();
            }
            
            // If the lastSuperBlock is full of Blocks, we need to create a new SuperBlock and increment numberOfSuperBlocks
            if(lastSuperBlock.getCurrentNumberOfDataBlocks() == lastSuperBlock.getMaxNumberOfDataBlocks()) {
                if(lastSuperBlock.getNumber() % 2 == 0) {
                    // If the number of the current full lastSuperBlock is even,
                    // The new SuperBlock will have the same MaxNumberOfDataBlocks as the old one,
                    // but twice the MaxNumberOfElementsPerBlock.
                    // This new superBlock currently has no Block in it.
                    lastSuperBlock = new SuperBlock(numberOfSuperBlocks++, lastSuperBlock.getMaxNumberOfDataBlocks(), 
                                                    lastSuperBlock.getMaxNumberOfElementsPerBlock() * 2, 0);
                } else {
                    // If the number of the current full lastSuperBlock is not even,
                    // The new SuperBlock will have the same MaxNumberOfElementsPerBlock as the old one,
                    // but twice the MaxNumberOfDataBlocks.
                    // This new superBlock currently has no Block in it.
                    lastSuperBlock = new SuperBlock(numberOfSuperBlocks++, lastSuperBlock.getMaxNumberOfDataBlocks() * 2, 
                                                    lastSuperBlock.getMaxNumberOfElementsPerBlock(), 0);
                }
            }
            
            // Create a new Block, use lastSuperBlock to figure out how many elements the Block can store.
            // Update the fields, also lastSuperBlock has one more Block in it, so incrementCurrentNumberOfDataBlocks.
            arrayOfBlocks[sizeOfArrayOfBlocks++] = new Block<T>(++indexOfLastDataBlock, lastSuperBlock.getMaxNumberOfElementsPerBlock());
            numberOfEmptyDataBlocks++;
            numberOfDataBlocks++;
            lastSuperBlock.incrementCurrentNumberOfDataBlocks();
            // Since we create a new Block, we need to update variable lastDataBlock. 
            lastDataBlock = (Block<T>) arrayOfBlocks[indexOfLastDataBlock];    
        } 
        lastDataBlock.grow();
        if(numberOfEmptyDataBlocks == 1) {
            // if numberOfEmptyDataBlocks = 1, it means our lastDataBlock was empty.
            // But it is supposed to not be empty now because we just grew the size of it.
            // So we have to update fields below.
            numberOfEmptyDataBlocks--;
            numberOfNonEmptyDataBlocks++;
            indexOfLastNonEmptyDataBlock++;
        }
            
    }
    
    // Grows the DynamicArray by one space, increases the size of the 
    // DynamicArray, and sets the last element to x.  
    // Target complexity: O(1)
    public void add(T x) {
        grow();
        size++;
        set(size - 1,x);
    }
    
    @SuppressWarnings("unchecked")
    // Write a null value to the last element, shrinks the DynamicArray by one 
    // space, and decreases the size of the DynamicArray. A Block may be 
    // deleted and the last SuperBlock may change.
    // Also, shrinkArray is called if the arrayOfBlocks is less than or equal
    // to a quarter full when a Block is deleted. 
    // Throws IllegalStateException if the DynamicArray is empty when remove is
    // called.
    // Target complexity: O(1)
    public void remove() {
        if(size == 0) {
            throw new IllegalStateException();
        }
        
        set((size - 1), null);
        Block<T> lastNonEmptyDataBlock = (Block<T>) arrayOfBlocks[indexOfLastNonEmptyDataBlock];  
        lastNonEmptyDataBlock.shrink();
        size--;
        
        if(lastNonEmptyDataBlock.size() == 0) {
            // The lastNonEmptyDataBlock is empty now after shrinking,
            // we have to update fields below. 
            numberOfEmptyDataBlocks++;
            numberOfNonEmptyDataBlocks--;
            indexOfLastNonEmptyDataBlock--;
        }
        
        // If we have two empty Blocks, we have to delete the last one.
        if(numberOfEmptyDataBlocks == 2) {
            // Set the last empty Block to null.
            arrayOfBlocks[--sizeOfArrayOfBlocks] = null; // --sizeOfArrayOfBlocks gives us the index of last Block, also decrement sizeOfArrayOfBlocks.
            // Update the fields, also lastSuperBlock has one less Block in it, so decrementCurrentNumberOfDataBlocks.
            numberOfDataBlocks--;
            numberOfEmptyDataBlocks--;
            indexOfLastDataBlock--;
            lastSuperBlock.decrementCurrentNumberOfDataBlocks();
            
            // The length of arrayOfBlocks should never be less than 4
            if(sizeOfArrayOfBlocks <= arrayOfBlocks.length / 4 && arrayOfBlocks.length > 4) {
                // Need to shrink
                shrinkArray();
            }
            
            // If the lastSuperBlock has no Blocks in it, 
            // we need to change lastSuperBlock to the previous superBlock and decrement numberOfSuperBlocks
            if(lastSuperBlock.getCurrentNumberOfDataBlocks() == 0) {
                if(lastSuperBlock.getNumber() % 2 == 0) {
                    // If the number of the current empty lastSuperBlock is even,
                    // The previous SuperBlock will have one half of MaxNumberOfDataBlocks as the old one,
                    // but the same MaxNumberOfElementsPerBlock.
                    // This previous superBlock currently is full of Blocks.
                    // --numberOfSuperBlocks - 1 gives us the number of this previous superBlock.
                    lastSuperBlock = new SuperBlock(--numberOfSuperBlocks - 1, lastSuperBlock.getMaxNumberOfDataBlocks() / 2, 
                                                    lastSuperBlock.getMaxNumberOfElementsPerBlock(),
                                                    lastSuperBlock.getMaxNumberOfDataBlocks() / 2);
                } else {
                    // If the number of the current empty lastSuperBlock is not even,
                    // The previous SuperBlock will have one half of MaxNumberOfElementsPerBlock as the old one,
                    // but the same MaxNumberOfDataBlocks.
                    // This previous superBlock currently is full of Blocks.
                    lastSuperBlock = new SuperBlock(--numberOfSuperBlocks - 1, lastSuperBlock.getMaxNumberOfDataBlocks(), 
                                                    lastSuperBlock.getMaxNumberOfElementsPerBlock() / 2,
                                                    lastSuperBlock.getMaxNumberOfDataBlocks());
                }
            }
        }
    }
    
    // Decreases the length of the arrayOfBlocks by half. Create a new
    // arrayOfBlocks and copy the Blocks from the old one to this new array.
    protected void shrinkArray() {
        Object[] newArrayOfBlocks = new Object[arrayOfBlocks.length / 2];
        for(int i = 0; i < sizeOfArrayOfBlocks; i++) {
            newArrayOfBlocks[i] = arrayOfBlocks[i];
        }
        arrayOfBlocks = newArrayOfBlocks;
    }
    
    // Doubles the length of the arrayOfBlocks. Create a new
    // arrayOfBlocks and copy the Blocks from the old one to this new array.
    protected void expandArray() {
        Object[] newArrayOfBlocks = new Object[arrayOfBlocks.length * 2];
        for(int i = 0; i < sizeOfArrayOfBlocks; i++) {
            newArrayOfBlocks[i] = arrayOfBlocks[i];
        }
        arrayOfBlocks = newArrayOfBlocks;
    }
    
    // Returns the size of the DynamicArray which is the number of elements that
    // have been added to it with the add(x) method but not removed.  The size 
    // does not correspond to the capacity of the array.
    public int size() {
        return size;
    }
    
    // Returns the log base 2 of n
    protected static double log2(int n) {
        return (Math.log(n) / Math.log(2));
    }
    
    // Returns a mask of N 1 bits; this code is provided below and can be used 
    // as is
    protected int maskOfN(int N) {
        int POW2ToN = 1 << N; // left shift 1 N places; e.g., 1 << 2 = 100 = 4
        int mask = POW2ToN - 1; // subtract 1; e.g., 1002 ¨C 12 = 0112 = 3
        // Integer.toString(mask,2); // a String with the bits of mask
        return mask;
    }
    
    
    // Create a pretty representation of the DynamicArray. This method should
    // return string formatted similarly to ArrayList
    // Examples: [], [X],  [A, B, C, D]
    // 
    // Target Complexity: O(N)
    //   N: number of elements in the DynamicArray
    public String toString() {
        if(size == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();       
        builder.append("[");
        for(int i = 0; i < size - 1; i++) {
            builder.append(get(i) + ", ");
        }
        builder.append(get(size - 1) + "]");
        return builder.toString();
    }
    
    @SuppressWarnings("unchecked")
    // Create a pretty representation of the DynamicArray for debugging
    // Example: 
    // DynamicArray: A B 
    // numberOfDataBlocks: 2
    // numberOfEmptyDataBlocks: 0
    // numberOfNonEmptyDataBlocks: 2
    // indexOfLastNonEmptyDataBlock: 1
    // indexOfLastDataBlock: 1
    // numberOfSuperBlocks: 2
    // lastSuperBlock: SB1
    // Block0: A 
    // - capacity: 1 size: 1
    // Block1: B 
    // - capacity: 2 size: 1
    // SB1:
    // - maxNumberOfDataBlocks: 1
    // - numberOfElementsPerBlock: 2
    // - currentNumberOfDataBlocks: 1
    
    protected String toStringForDebugging() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicArray: ");
        for(int i = 0; i < size; i++) {
            builder.append(get(i) + " ");
        }
        builder.append("\nnumberOfDataBlocks: " + numberOfDataBlocks);
        builder.append("\nnumberOfEmptyDataBlocks: " + numberOfEmptyDataBlocks);
        builder.append("\nnumberOfNonEmptyDataBlocks: " + numberOfNonEmptyDataBlocks);
        builder.append("\nindexOfLastNonEmptyDataBlock: " + indexOfLastNonEmptyDataBlock);
        builder.append("\nindexOfLastDataBlock: " + indexOfLastDataBlock);
        builder.append("\nnumberOfSuperBlocks: " + numberOfSuperBlocks);
        builder.append("\nlastSuperBlock: SB" + lastSuperBlock.getNumber());
        for(int i = 0; i < sizeOfArrayOfBlocks; i++) {
            builder.append("\nBlock" + i + ": ");
            Block<T> block = (Block<T>) arrayOfBlocks[i];
            builder.append(block.toStringForDebugging());
        }
        builder.append("\nSB" + lastSuperBlock.getNumber() + ":");
        builder.append("\n- maxNumberOfDataBlocks: " + lastSuperBlock.getMaxNumberOfDataBlocks());
        builder.append("\n- maxNumberOfElementsPerBlock: " + lastSuperBlock.getMaxNumberOfElementsPerBlock());
        builder.append("\n- currentNumberOfDataBlocks: " + lastSuperBlock.getCurrentNumberOfDataBlocks());
        
        return builder.toString();
    }
}
