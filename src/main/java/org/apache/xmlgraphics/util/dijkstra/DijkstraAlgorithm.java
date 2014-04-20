/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: DijkstraAlgorithm.java 750418 2009-03-05 11:03:54Z vhennebert $ */

package org.apache.xmlgraphics.util.dijkstra;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is an implementation of Dijkstra's algorithm to find the shortest path
 * for a directed graph with non-negative edge weights.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">WikiPedia
 *      on Dijkstra's Algorithm</a>
 */
public class DijkstraAlgorithm {

    /** Infinity value for distances. */
    public static final int INFINITE = Integer.MAX_VALUE;

    /** Compares penalties between two possible destinations. */
    private final Comparator<Vertex> penaltyComparator = new Comparator<Vertex>() {
        @Override
        public int compare(final Vertex left, final Vertex right) {
            final int leftPenalty = getLowestPenalty(left);
            final int rightPenalty = getLowestPenalty(right);
            if (leftPenalty < rightPenalty) {
                return -1;
            } else if (leftPenalty == rightPenalty) {
                return left.compareTo(right);
            } else {
                return 1;
            }
        }
    };

    /** The directory of edges */
    private final EdgeDirectory edgeDirectory;

    /**
     * The priority queue for all vertices under inspection, ordered by
     * penalties/distances.
     */
    private final TreeSet<Vertex> priorityQueue = new TreeSet<>(
            this.penaltyComparator);

    /** The set of vertices for which the lowest penalty has been found. */
    private final Set<Vertex> finishedVertices = new HashSet<>();

    /** The currently known lowest penalties for all vertices. */
    private final Map<Vertex, Integer> lowestPenalties = new HashMap<>();

    /** Map of all predecessors in the spanning tree of best routes. */
    private final Map<Vertex, Vertex> predecessors = new HashMap<>();

    // Map<Vertex,Vertex>

    /**
     * Main Constructor.
     * 
     * @param edgeDirectory
     *            the edge directory this instance should work on
     */
    public DijkstraAlgorithm(final EdgeDirectory edgeDirectory) {
        this.edgeDirectory = edgeDirectory;
    }

    /**
     * Returns the penalty between two vertices.
     * 
     * @param start
     *            the start vertex
     * @param end
     *            the end vertex
     * @return the penalty between two vertices, or 0 if no single edge between
     *         the two vertices exists.
     */
    protected int getPenalty(final Vertex start, final Vertex end) {
        return this.edgeDirectory.getPenalty(start, end);
    }

    /**
     * Returns an iterator over all valid destinations for a given vertex.
     * 
     * @param origin
     *            the origin from which to search for destinations
     * @return the iterator over all valid destinations for a given vertex
     */
    protected Iterator<Vertex> getDestinations(final Vertex origin) {
        return this.edgeDirectory.getDestinations(origin);
    }

    private void reset() {
        this.finishedVertices.clear();
        this.priorityQueue.clear();

        this.lowestPenalties.clear();
        this.predecessors.clear();
    }

    /**
     * Run Dijkstra's shortest path algorithm. After this method is finished you
     * can use {@link #getPredecessor(Vertex)} to reconstruct the best/shortest
     * path starting from the destination backwards.
     * 
     * @param start
     *            the starting vertex
     * @param destination
     *            the destination vertex.
     */
    public void execute(final Vertex start, final Vertex destination) {
        if (start == null || destination == null) {
            throw new NullPointerException(
                    "start and destination may not be null");
        }

        reset();
        setShortestDistance(start, 0);
        this.priorityQueue.add(start);

        // the current node
        Vertex u;

        // extract the vertex with the shortest distance
        while (!this.priorityQueue.isEmpty()) {
            u = this.priorityQueue.first();
            this.priorityQueue.remove(u);

            if (destination.equals(u)) {
                // Destination reached
                break;
            }

            this.finishedVertices.add(u);
            relax(u);
        }
    }

    /**
     * Compute new lowest penalties for neighboring vertices. Update the lowest
     * penalties and the predecessor map if a better solution is found.
     * 
     * @param u
     *            the vertex to process
     */
    private void relax(final Vertex u) {
        final Iterator<Vertex> iter = getDestinations(u);
        while (iter.hasNext()) {
            final Vertex v = iter.next();
            // skip node already settled
            if (isFinished(v)) {
                continue;
            }

            final int shortDist = getLowestPenalty(u) + getPenalty(u, v);

            if (shortDist < getLowestPenalty(v)) {
                // assign new shortest distance and mark unsettled
                setShortestDistance(v, shortDist);

                // assign predecessor in shortest path
                setPredecessor(v, u);
            }
        }
    }

    private void setPredecessor(final Vertex a, final Vertex b) {
        this.predecessors.put(a, b);
    }

    /**
     * Indicates whether a shortest route to a vertex has been found.
     * 
     * @param v
     *            the vertex
     * @return true if the shortest route to this vertex has been found.
     */
    private boolean isFinished(final Vertex v) {
        return this.finishedVertices.contains(v);
    }

    private void setShortestDistance(final Vertex vertex, final int distance) {
        // Remove so it is inserted at the right position after the lowest
        // penalty changes for this
        // vertex.
        this.priorityQueue.remove(vertex);

        // Update the lowest penalty.
        this.lowestPenalties.put(vertex, distance);

        // Insert the vertex again at the new position based on the lowest
        // penalty
        this.priorityQueue.add(vertex);
    }

    /**
     * Returns the lowest penalty from the start point to a given vertex.
     * 
     * @param vertex
     *            the vertex
     * @return the lowest penalty or {@link DijkstraAlgorithm#INFINITE} if there
     *         is no route to the destination.
     */
    public int getLowestPenalty(final Vertex vertex) {
        final Integer d = this.lowestPenalties.get(vertex);
        return d == null ? INFINITE : d.intValue();
    }

    /**
     * Returns the vertex's predecessor on the shortest path.
     * 
     * @param vertex
     *            the vertex for which to find the predecessor
     * @return the vertex's predecessor on the shortest path, or
     *         <code>null</code> if there is no route to the destination.
     */
    public Vertex getPredecessor(final Vertex vertex) {
        return this.predecessors.get(vertex);
    }

}
