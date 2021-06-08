package ru.vsu.kudinov_i_m;

import java.util.Arrays;
import java.util.Iterator;

public class AdjacencyMatrixGraph implements Graph
{

    private boolean[][] adjacencyMatrix = null;
    private int vertexCount = 0;
    private int edgeCount = 0;

    public AdjacencyMatrixGraph(int vertexCount)
    {
        adjacencyMatrix = new boolean[vertexCount][vertexCount];
        this.vertexCount = vertexCount;
    }

    public AdjacencyMatrixGraph()
    {
        this(0);
    }

    @Override
    public int vertexCount()
    {
        return vertexCount;
    }

    @Override
    public int edgeCount()
    {
        return edgeCount;
    }

    @Override
    public void addEdge(int v1, int v2)
    {
        int maxV = Math.max(v1, v2);
        if (maxV >= vertexCount()) {
            adjacencyMatrix = Arrays.copyOf(adjacencyMatrix, maxV + 1);
            for (int i = 0; i <= maxV; i++) {
                adjacencyMatrix[i] = i < vertexCount ? Arrays.copyOf(adjacencyMatrix[i], maxV + 1) : new boolean[maxV + 1];
            }
            vertexCount = maxV + 1;
        }
        if (!adjacencyMatrix[v1][v2]) {
            adjacencyMatrix[v1][v2] = true;
            edgeCount++;
        }
    }

    @Override
    public void removeEdge(int v1, int v2) {
        if (adjacencyMatrix[v1][v2]) {
            adjacencyMatrix[v1][v2] = false;
            edgeCount--;
        }
    }

    @Override
    public Iterable<Integer> findAdjacencyVertex(int v) {
        return new Iterable<Integer>() {
            Integer nextAdj = null;

            @Override
            public Iterator<Integer> iterator() {
                for (int i = 0; i < vertexCount; i++) {
                    if (adjacencyMatrix[v][i]) {
                        nextAdj = i;
                        break;
                    }
                }

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return nextAdj != null;
                    }

                    @Override
                    public Integer next() {
                        Integer result = nextAdj;
                        nextAdj = null;
                        for (int i = result + 1; i < vertexCount; i++) {
                            if (adjacencyMatrix[v][i]) {
                                nextAdj = i;
                                break;
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    @Override
    public boolean isAdjacency(int v1, int v2) {
        return adjacencyMatrix[v1][v2];
    }
}
