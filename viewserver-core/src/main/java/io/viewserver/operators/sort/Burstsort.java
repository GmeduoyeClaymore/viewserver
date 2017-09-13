/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.operators.sort;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Array-based implementation of the original Burstsort, now referred
 * to as P-burstsort. There are both single-threaded and multi-threaded
 * implementations, designed to take advantage of multiple CPU cores,
 * if available.
 *
 * @author Nathan Fiedler
 */
public class Burstsort {
    /**
     * Null terminator character.
     */
    private static final char NULLTERM = '\0';
    /**
     * Maximum number of elements in any given bucket; for null bucket set,
     * this is the size of each of the chained buckets).
     */
    private static final short THRESHOLD = 8192;
    /**
     * Used to store reference to next bucket in last cell of bucket.
     */
    private static final short THRESHOLDMINUSONE = THRESHOLD - 1;
    /**
     * Size of the alphabet that is supported.
     */
    private static final short ALPHABET = 256;
    /**
     * Initial size for new buckets.
     */
    private static final short BUCKET_START_SIZE = 16;
    /**
     * The bucket growth factor (replaces the bucket_inc array in the
     * original C implementation).
     */
    private static final short BUCKET_GROWTH_FACTOR = 8;

    /**
     * Creates a new instance of Burstsort.
     */
    private Burstsort() {
    }

    /**
     * Retrieve the character in string s at offset d. If d is greater
     * than or equal to the length of the string, return zero. This
     * simulates fixed-length strings that are zero-padded.
     *
     * @param s string.
     * @param d offset.
     * @return character in s at d, or zero.
     */
    private static char charAt(CharSequence s, int d) {
        return d < s.length() ? s.charAt(d) : NULLTERM;
    }

    /**
     * Inserts a set of strings into the burst trie structure, in
     * preparation for in-order traversal (hence sorting).
     *
     * @param root    root of the structure.
     * @param strings strings to be inserted.
     */
    private static void insert(Node root, CharSequence[] strings) {
        for (int i = 0; i < strings.length; i++) {
            // Start at root each time
            Node curr = root;
            // Locate trie node in which to insert string
            int p = 0;
            char c = charAt(strings[i], p);
            while (curr.size(c) < 0) {
                curr = (Node) curr.get(c);
                p++;
                c = charAt(strings[i], p);
            }
            curr.add(c, strings[i]);
            // is bucket size above the THRESHOLD?
            while (curr.size(c) >= THRESHOLD && c != NULLTERM) {
                // advance depth of character
                p++;
                // allocate memory for new trie node
                Node newt = new Node();
                // burst...
                char cc = NULLTERM;
                CharSequence[] ptrs = (CharSequence[]) curr.get(c);
                int size = curr.size(c);
                for (int j = 0; j < size; j++) {
                    // access the next depth character
                    cc = charAt(ptrs[j], p);
                    newt.add(cc, ptrs[j]);
                }
                // old pointer points to the new trie node
                curr.set(c, newt);
                // used to burst recursive, so point curr to new
                curr = newt;
                // point to character used in previous string
                c = cc;
            }
        }
    }

    /**
     * Sorts the set of strings using the original (P-)burstsort algorithm.
     *
     * @param strings array of strings to be sorted.
     */
    public static void sort(CharSequence[] strings) {
        sort(strings, null);
    }

    /**
     * Sorts the given set of strings using the original (P-)burstsort
     * algorithm. If the given output stream is non-null, then metrics
     * regarding the burstsort trie structure will be printed there.
     *
     * @param strings array of strings to be sorted.
     * @param out     if non-null, metrics are printed here.
     */
    public static void sort(CharSequence[] strings, PrintStream out) {
        if (strings != null && strings.length > 1) {
            Node root = new Node();
            insert(root, strings);
            if (out != null) {
                writeMetrics(root, out);
            }
            traverse(root, strings, 0, 0);
        }
    }

    /**
     * Uses all available processors to sort the trie buckets in parallel,
     * thus sorting the overal set of strings in less time. Uses a simple
     * ThreadPoolExecutor with a maximum pool size equal to the number of
     * available processors (usually equivalent to the number of CPU cores).
     *
     * @param strings array of strings to be sorted.
     * @throws InterruptedException if waiting thread was interrupted.
     */
    public static void sortThreadPool(CharSequence[] strings) throws InterruptedException {
        if (strings != null && strings.length > 1) {
            Node root = new Node();
            insert(root, strings);
            List<Callable<Object>> jobs = new ArrayList<Callable<Object>>();
            traverseParallel(root, strings, 0, 0, jobs);
            ExecutorService executor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());
            // Using ExecutorService.invokeAll() usually adds more time.
            for (Callable<Object> job : jobs) {
                executor.submit(job);
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }
    }

    /**
     * Traverse the trie structure, ordering the strings in the array to
     * conform to their lexicographically sorted order as determined by
     * the trie structure.
     *
     * @param node    node within trie structure.
     * @param strings the strings to be ordered.
     * @param pos     position within array.
     * @param deep    character offset within strings.
     * @return new pos value.
     */
    private static int traverse(Node node, CharSequence[] strings, int pos, int deep) {
        for (char c = 0; c < ALPHABET; c++) {
            int count = node.size(c);
            if (count < 0) {
                pos = traverse((Node) node.get(c), strings, pos, deep + 1);
            } else if (count > 0) {
                int off = pos;
                if (c == 0) {
                    // Visit all of the null buckets, which are daisy-chained
                    // together with the last reference in each bucket pointing
                    // to the next bucket in the chain.
                    int no_of_buckets = (count / THRESHOLDMINUSONE) + 1;
                    Object[] nullbucket = (Object[]) node.get(c);
                    for (int k = 1; k <= no_of_buckets; k++) {
                        int no_elements_in_bucket;
                        if (k == no_of_buckets) {
                            no_elements_in_bucket = count % THRESHOLDMINUSONE;
                        } else {
                            no_elements_in_bucket = THRESHOLDMINUSONE;
                        }
                        // Copy the string tails to the sorted array.
                        int j = 0;
                        while (j < no_elements_in_bucket) {
                            strings[off] = (CharSequence) nullbucket[j];
                            off++;
                            j++;
                        }
                        nullbucket = (Object[]) nullbucket[j];
                    }
                } else {
                    // Sort the tail string bucket.
                    CharSequence[] bucket = (CharSequence[]) node.get(c);
                    if (count > 1) {
                        MultikeyQuicksort.sort(bucket, 0, count, deep + 1);
                    }
                    // Copy to final destination.
                    System.arraycopy(bucket, 0, strings, off, count);
                }
                pos += count;
            }
        }
        return pos;
    }

    /**
     * Traverse the trie structure, creating jobs for each of the buckets.
     *
     * @param node    node within trie structure.
     * @param strings the strings to be ordered.
     * @param pos     position within array.
     * @param deep    character offset within strings.
     * @param jobs    job list to which new jobs are added.
     * @return new pos value.
     */
    private static int traverseParallel(Node node, CharSequence[] strings,
                                        int pos, int deep, List<Callable<Object>> jobs) {
        for (char c = 0; c < ALPHABET; c++) {
            int count = node.size(c);
            if (count < 0) {
                pos = traverseParallel((Node) node.get(c), strings, pos,
                        deep + 1, jobs);
            } else if (count > 0) {
                int off = pos;
                if (c == 0) {
                    // Visit all of the null buckets, which are daisy-chained
                    // together with the last reference in each bucket pointing
                    // to the next bucket in the chain.
                    int no_of_buckets = (count / THRESHOLDMINUSONE) + 1;
                    Object[] nullbucket = (Object[]) node.get(c);
                    for (int k = 1; k <= no_of_buckets; k++) {
                        int no_elements_in_bucket;
                        if (k == no_of_buckets) {
                            no_elements_in_bucket = count % THRESHOLDMINUSONE;
                        } else {
                            no_elements_in_bucket = THRESHOLDMINUSONE;
                        }
                        // Use a job for each sub-bucket to avoid handling
                        // large numbers of entries in a single thread.
                        // Note that this only works for the null buckets
                        // which do not require any sorting of the entries.
                        jobs.add(new CopyJob(nullbucket, no_elements_in_bucket, strings, off));
                        off += no_elements_in_bucket;
                        nullbucket = (Object[]) nullbucket[no_elements_in_bucket];
                    }
                } else {
                    // A regular bucket with string tails that need to
                    // be sorted and copied to the final destination.
                    CharSequence[] bucket = (CharSequence[]) node.get(c);
                    jobs.add(new SortJob(bucket, count, strings, off, deep + 1));
                }
                pos += count;
            }
        }
        return pos;
    }

    /**
     * Collect metrics regarding the burstsort trie structure and write
     * them to the given output stream.
     *
     * @param node root node of the trie structure.
     * @param out  output stream to write to.
     */
    private static void writeMetrics(Node node, PrintStream out) {
        Stack<Node> stack = new Stack<Node>();
        stack.push(node);
        int nodes = 0;
        int consumedStrings = 0;
        int bucketStrings = 0;
        int bucketSpace = 0;
        int nonEmptyBuckets = 0;
        int smallest = Integer.MAX_VALUE;
        int largest = Integer.MIN_VALUE;
        while (!stack.isEmpty()) {
            node = stack.pop();
            nodes++;
            for (char c = 0; c < ALPHABET; c++) {
                int count = node.size(c);
                if (count < 0) {
                    stack.push((Node) node.get(c));
                } else {
                    // Only consider non-empty buckets, as there will
                    // always be empty buckets.
                    if (count > 0) {
                        if (c == 0) {
                            int no_of_buckets = (count / THRESHOLDMINUSONE) + 1;
                            Object[] nb = (Object[]) node.get(c);
                            for (int k = 1; k <= no_of_buckets; k++) {
                                int no_elements_in_bucket;
                                if (k == no_of_buckets) {
                                    no_elements_in_bucket = count % THRESHOLDMINUSONE;
                                } else {
                                    no_elements_in_bucket = THRESHOLDMINUSONE;
                                }
                                bucketSpace += nb.length;
                                nb = (Object[]) nb[no_elements_in_bucket];
                            }
                            consumedStrings += count;
                        } else {
                            CharSequence[] cs = (CharSequence[]) node.get(c);
                            bucketSpace += cs.length;
                            bucketStrings += count;
                        }
                        if (count < smallest) {
                            smallest = count;
                        }
                        nonEmptyBuckets++;
                    }
                    if (count > largest) {
                        largest = count;
                    }
                }
            }
        }
        out.format("Trie nodes: %d\n", nodes);
        out.format("Total buckets: %d\n", nonEmptyBuckets);
        out.format("Bucket strings: %d\n", bucketStrings);
        out.format("Consumed strings: %d\n", consumedStrings);
        out.format("Smallest bucket: %d\n", smallest);
        out.format("Largest bucket: %d\n", largest);
        long sum = consumedStrings + bucketStrings;
        out.format("Average bucket: %d\n", sum / nonEmptyBuckets);
        out.format("Bucket capacity: %d\n", bucketSpace);
        double usage = ((double) sum * 100) / (double) bucketSpace;
        out.format("Usage ratio: %.2f\n", usage);
    }

    /**
     * A node in the burst trie structure based on the original Burstsort
     * algorithm, consisting of a null tail pointer bucket and zero or more
     * buckets for the other entries. Entries may point either to a bucket
     * or another trie node.
     *
     * @author Nathan Fiedler
     */
    private static class Node {
        /**
         * Reference to the last null bucket in the chain, starting
         * from the reference in ptrs[0].
         */
        private Object[] nulltailptr;
        /**
         * last element in null bucket
         */
        private int nulltailidx;
        /**
         * count of items in bucket, or -1 if reference to trie node
         */
        private final int[] counts = new int[ALPHABET];
        /**
         * pointers to buckets or trie node
         */
        private final Object[] ptrs = new Object[ALPHABET];

        /**
         * Add the given string into the appropriate bucket, given the
         * character index into the trie. Presumably the character is
         * from the string, but not necessarily so. The character may
         * be the null character, in which case the string is added to
         * the null bucket. Buckets are expanded as needed to accomodate
         * the new string.
         *
         * @param c character used to index trie entry.
         * @param s the string to be inserted.
         */
        public void add(char c, CharSequence s) {
            // are buckets already created?
            if (counts[c] < 1) {
                // create bucket
                if (c == NULLTERM) {
                    // allocate memory for the bucket
                    nulltailptr = new Object[THRESHOLD];
                    ptrs[c] = nulltailptr;
                    // insert the string
                    nulltailptr[0] = s;
                    // point to next cell
                    nulltailidx = 1;
                    // increment count of items
                    counts[c]++;
                } else {
                    CharSequence[] cs = new CharSequence[BUCKET_START_SIZE];
                    cs[0] = s;
                    ptrs[c] = cs;
                    counts[c]++;
                }
            } else {
                // bucket already created, insert string in bucket
                if (c == NULLTERM) {
                    // insert the string
                    nulltailptr[nulltailidx] = s;
                    // point to next cell
                    nulltailidx++;
                    // increment count of items
                    counts[c]++;
                    // check if the bucket is reaching the threshold
                    if (counts[c] % THRESHOLDMINUSONE == 0) {
                        // Grow the null bucket by daisy chaining a new array.
                        Object[] tmp = new Object[THRESHOLD];
                        nulltailptr[nulltailidx] = tmp;
                        // point to the first cell in the new array
                        nulltailptr = tmp;
                        nulltailidx = 0;
                    }
                } else {
                    // Insert string in bucket and increment the item counter.
                    CharSequence[] cs = (CharSequence[]) ptrs[c];
                    cs[counts[c]] = s;
                    counts[c]++;
                    // If the bucket is full, increase its size, but only
                    // up to the threshold value.
                    if (counts[c] < THRESHOLD && counts[c] == cs.length) {
                        CharSequence[] tmp = new CharSequence[cs.length * BUCKET_GROWTH_FACTOR];
                        System.arraycopy(cs, 0, tmp, 0, cs.length);
                        ptrs[c] = tmp;
                    }
                }
            }
        }

        /**
         * Retrieve the trie node or object array for character <em>c</em>.
         *
         * @param c character for which to retrieve entry.
         * @return the trie node entry for the given character.
         */
        public Object get(char c) {
            return ptrs[c];
        }

        /**
         * Set the trie node or object array for character <em>c</em>.
         *
         * @param c character for which to store new entry.
         * @param o the trie node entry for the given character.
         */
        public void set(char c, Object o) {
            ptrs[c] = o;
            if (o instanceof Node) {
                // flag to indicate pointer to trie node and not bucket
                counts[c] = -1;
            }
        }

        /**
         * Returns the number of strings stored for the given character.
         *
         * @param c character for which to get count.
         * @return number of tail strings; -1 if child is a trie node.
         */
        public int size(char c) {
            return counts[c];
        }
    }

    /**
     * A copy job to be completed after the trie traversal phase. Each job
     * is given a single bucket to be a processed. A copy job simply copies
     * the string references from the null bucket to the string output array.
     *
     * @author Nathan Fiedler
     */
    private static class CopyJob implements Callable<Object> {
        /**
         * True if this job has already been completed.
         */
        private volatile boolean completed;
        /**
         * The array from the null trie bucket containing strings as Object
         * references; not to be sorted.
         */
        private final Object[] input;
        /**
         * The number of elements in the input array.
         */
        private final int count;
        /**
         * The array to which the sorted strings are written.
         */
        private final CharSequence[] output;
        /**
         * The position within the strings array at which to store the
         * sorted results.
         */
        private final int offset;

        /**
         * Constructs an instance of Job which merely copies the objects
         * from the input array to the output array. The input objects
         * must be of type CharSequence in order for the copy to succeed.
         *
         * @param input  input array.
         * @param count  number of elements from input to consider.
         * @param output output array; only a subset should be modified.
         * @param offset offset within output array to which sorted
         *               strings will be written.
         */
        CopyJob(Object[] input, int count, CharSequence[] output, int offset) {
            this.input = input;
            this.count = count;
            this.output = output;
            this.offset = offset;
        }

        /**
         * Indicates if this job has been completed or not.
         *
         * @return true if job has been completed, false otherwise.
         */
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public Object call() throws Exception {
            System.arraycopy(input, 0, output, offset, count);
            completed = true;
            return null;
        }
    }

    /**
     * A sort job to be completed after the trie traversal phase. Each job
     * is given a single bucket to be a processed. A sort job first sorts the
     * the string "tails" and then copies the references to the output array.
     *
     * @author Nathan Fiedler
     */
    private static class SortJob implements Callable<Object> {
        /**
         * True if this job has already been completed.
         */
        private volatile boolean completed;
        /**
         * The array from the trie bucket containing unsorted strings.
         */
        private final CharSequence[] input;
        /**
         * The number of elements in the input array.
         */
        private final int count;
        /**
         * The array to which the sorted strings are written.
         */
        private final CharSequence[] output;
        /**
         * The position within the strings array at which to store the
         * sorted results.
         */
        private final int offset;
        /**
         * The depth at which to sort the strings (i.e. the strings often
         * have a common prefix, and depth is the length of that prefix and
         * thus the sort routines can ignore those characters).
         */
        private final int depth;

        /**
         * Constructs an instance of Job which will sort and then copy the
         * input strings to the output array.
         *
         * @param input  input array; all elements are copied.
         * @param count  number of elements from input to consider.
         * @param output output array; only a subset should be modified.
         * @param offset offset within output array to which sorted
         *               strings will be written.
         * @param depth  number of charaters in strings to be ignored
         *               when sorting (i.e. the common prefix).
         */
        SortJob(CharSequence[] input, int count, CharSequence[] output,
                int offset, int depth) {
            this.input = input;
            this.count = count;
            this.output = output;
            this.offset = offset;
            this.depth = depth;
        }

        /**
         * Indicates if this job has been completed or not.
         *
         * @return
         */
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public Object call() throws Exception {
            if (count > 0) {
                if (count > 1) {
                    // Sort the strings from the bucket.
                    MultikeyQuicksort.sort(input, 0, count, depth);
                }
                // Copy the sorted strings to the destination array.
                System.arraycopy(input, 0, output, offset, count);
            }
            completed = true;
            return null;
        }
    }
}
