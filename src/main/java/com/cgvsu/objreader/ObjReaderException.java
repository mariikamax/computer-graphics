package com.cgvsu.objreader;

public class ObjReaderException extends RuntimeException {
    public ObjReaderException(String errorMessage, int lineInd) {
        super(String.format("Error parsing OBJ file (line %d): %s", lineInd, errorMessage));
    }
}