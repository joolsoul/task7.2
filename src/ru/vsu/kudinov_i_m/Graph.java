package ru.vsu.kudinov_i_m;

public interface Graph
{
    int vertexCount();

    int edgeCount();

    void addEdge(int v1, int v2);

    void removeEdge(int v1, int v2);

    Iterable<Integer> findAdjacencyVertex(int v);

    boolean isAdjacency(int v1, int v2);
}
