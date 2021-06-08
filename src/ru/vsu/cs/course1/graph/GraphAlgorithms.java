package ru.vsu.cs.course1.graph;

import java.util.Stack;

public class GraphAlgorithms
{
    public static Stack<Integer> findEulerCycle(boolean[][] matrix, int index)
    {
        Stack<Integer> vertexToVisit = new Stack<>();
        Stack<Integer> eulerCycle = new Stack<>();

        vertexToVisit.push(index);
        while (!vertexToVisit.isEmpty())
        {
            if (!hasNext(matrix, vertexToVisit.peek())) eulerCycle.push(vertexToVisit.pop());
            else
            {
                for (int j = 0; j < matrix.length; j++)
                {
                    if (matrix[vertexToVisit.peek()][j])
                    {
                        matrix[vertexToVisit.peek()][j] = false;
                        matrix[j][vertexToVisit.peek()] = false;
                        vertexToVisit.push(j);
                        break;
                    }
                }
            }
        }
        return eulerCycle;
    }

    private static boolean hasNext(boolean[][] matrix, int vertex)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            if (matrix[vertex][i])
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isEulerGraph(boolean[][] matrix)
    {
        boolean isVertexesEven = true;
        int edgeCount = 0;
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[i].length; j++)
            {
                if (matrix[i][j])
                {
                    edgeCount++;
                }
            }
            if (edgeCount % 2 != 0)
            {
                isVertexesEven = false;
            }
            edgeCount = 0;
        }
        return isVertexesEven;
    }
}
