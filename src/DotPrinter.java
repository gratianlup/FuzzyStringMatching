// Copyright (c) 2013 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "FuzzyStringMatching" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "FuzzyStringMatching" nor
// may "FuzzyStringMatching" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DotPrinter {
    // The colors that can be used for shapes and lines.
    public static enum Color {
        Black,
        Gray,
        Red,
        Yellow,
        Green,
        Blue
    }

    // The form that a shape can take.
    public static enum Shape {
        Rectangle,
        Square,
        Circle,
        Ellipse,
        Diamond,
        Hexagon
    }

    private BufferedWriter writer;
    private Map<Object, Integer> objectIds;
    private int lastObjectId;

    public DotPrinter(BufferedWriter writer) {
        this.writer = writer;
        this.objectIds = new HashMap<Object, Integer>();
    }

    public DotPrinter(String filePath) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath);
        this.writer = new BufferedWriter(fileWriter);
        this.objectIds = new HashMap<Object, Integer>();
    }

    private String getColorString(Color color) {
        switch(color) {
            case Black: return "black";
            case Gray: return "gray88";
            case Red: return "lightpink";
            case Yellow: return "yellow";
            case Green: return "palegreen";
            case Blue: return "lightsteelblue1";
            default: return "black";
        }
    }

    private String getShapeString(Shape shape) {
        switch(shape) {
            case Rectangle: return "rectangle";
            case Square: return "rectangle";
            case Circle: return "circle";
            case Ellipse: return "ellipse";
            case Diamond: return "diamond";
            case Hexagon: return "hexagon";
            default: return "rectange";
        }
    }

    private void write(String value) {
        try {
            writer.write(value);
        }
        catch(Exception ex) {
            System.out.println("Failed to write to DOT file.");
            ex.printStackTrace();
        }
    }

    public void beginGraph() {
        write("digraph {\n");
    }

    public void endGraph() {
        write("\n}\n");
    }

    public void createNode(Object object, String label, Shape shape, Color color) {
        if(!objectIds.containsKey(object)) {
            int objectId = objectId = lastObjectId;
            objectIds.put(object, lastObjectId);
            lastObjectId++;
            write(String.format("n%d[shape=%s, style=filled, color=%s, label=\"%s\"];\n",
                    objectId, getShapeString(shape), getColorString(color), label));
        }
    }

    public void createNode(Object object, String label, Shape shape) {
        createNode(object, label, shape, Color.Gray);
    }

    public void createNode(Object object, String label) {
        createNode(object, label, Shape.Rectangle);
    }

    public void createNode(Object object) {
        createNode(object, "");
    }

    public void createLink(Object sourceObject, Object destObject,
                           String label, Color color, int penWidth, boolean dotted) {
        if(sourceObject == null) {
            throw new IllegalArgumentException("Link source object not specified!");
        }
        else if(destObject == null) {
            throw new IllegalArgumentException("Link destination object not specified!");
        }

        Integer nodeA = objectIds.get(sourceObject);
        Integer nodeB = objectIds.get(destObject);

        if(nodeA == null) {
            throw new IllegalArgumentException("Link source object shape not found!");
        }
        else if(nodeB == null) {
            throw new IllegalArgumentException("Link destination object shape not found!");
        }

        write(String.format("n%d -> n%d", nodeA, nodeB));
        write(String.format("[label=\"%s\", fontsize=12, labeldistance=10", label));
        write(String.format(", penwidth=%d", penWidth));
        if(dotted) write(", style=dotted");
        write(String.format(", color=%s];\n", getColorString(color)));
    }

    public void createLink(Object sourceObject, Object destObject,
                           String label, Color color, int penWidth) {
        createLink(sourceObject, destObject, label, color, penWidth, false);
    }

    public void createLink(Object sourceObject, Object destObject,
                           String label, Color color) {
        createLink(sourceObject, destObject, label, color, 2);
    }

    public void createLink(Object sourceObject, Object destObject, String label) {
        createLink(sourceObject, destObject, label, Color.Black);
    }

    public void createLink(Object sourceObject, Object destObject) {
        createLink(sourceObject, destObject, "");
    }
}
